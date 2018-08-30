package facets.facet.kit.swing;
import static facets.facet.FacetFactory.*;
import static facets.util.Util.*;
import static java.lang.Math.*;
import facets.core.superficial.SNumeric;
import facets.facet.FacetFactory;
import facets.facet.kit.KField;
import facets.facet.kit.KWrap;
import facets.facet.kit.swing.KitSwing.PauseWaiter;
import facets.util.Debug;
import facets.util.Doubles;
import facets.util.NumberPolicy;
import facets.util.StringFlags;
import facets.util.Strings;
import facets.util.Times;
import facets.util.Util;
import facets.util.NumberPolicy.Ticked;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.Dictionary;
import java.util.Hashtable;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
final class SliderSetter extends DefaultBoundedRangeModel{
	private static final boolean avoidingBlankingBug=true;
  private final ChangeListener listener=new ChangeListener(){
    private final ActionListener dragNotify=new ActionListener(){		
    	public void actionPerformed(ActionEvent e){
    		numeric.setValue(validValue);
    		slider.notifyFacet(getValueIsAdjusting());
    	}
    };
	  private final PauseWaiter waiter=new PauseWaiter();
		public void stateChanged(ChangeEvent e){
      NumberPolicy p=numeric.policy();
      double validThen=numeric.value();
			validValue=p.validValue(validThen,getValue()/scale);
			if(validValue==validThen)return;
  		if(box!=null)box.setValue(validValue);
  		if(dragNotifyInterim&&true||SliderSetter.this.slider.swing.isFocusOwner())
  				waiter.startWait(dragNotifyPause,dragNotify);
  		else{
    		if(getValueIsAdjusting())return;
    		numeric.setValue(validValue);
   		  slider.notifyFacet(false);
  		}
    }
  };
  private final KField box;
  private final Widget slider;
  private final boolean local,debug;
	private SNumeric numeric;
	private double scale=Double.NaN,validValue,labelValues[];
	private Dictionary tickLabels;
	SliderSetter(final Widget slider,final SNumeric numeric,KWrap box,StringFlags hints){
	  this.slider=slider;
	  this.box=(KField)box;
		local=hints.includeFlag(HINT_SLIDER_TICKS)
			&&hints.includeFlag(HINT_SLIDER_LOCAL);
		debug=hints.includeFlag(HINT_DEBUG);
	  addChangeListener(listener);
	}
	void setNumeric(final SNumeric numeric,final JSlider swing,int sliderWidth){	
		if(false&&swing!=slider.wrapped())throw new RuntimeException("swing!=slider.wrapped()");
		this.numeric=numeric;
		if(swing.isFocusOwner()&&getValueIsAdjusting())return;
		Ticked ticked=(Ticked)numeric.policy();
		final Ticked policy=!local?ticked:ticked.localTicks();
		final double tickValue=policy.unit();
		int tickSpacing=policy.tickSpacing(),
			tickCount=(int)(policy.range()/(tickSpacing*tickValue)),
			labelCount=tickCount/policy.labelSpacing()+1;
		if(false)Util.printOut("SliderSetter: "+numeric+(!local?"":" local=" +policy));
		boolean tooDense=tickCount*3/2>sliderWidth,cycles=policy.canCycle();
		if(tooDense){
			if(false)throw new IllegalStateException("Ticks too dense for " +numeric+
					" in "+Debug.info(this));
			else tickCount/=2;
		}
		final int labelTicks=policy.labelSpacing()*(!tooDense?tickSpacing:10);
		double range=policy.range(),min=policy.min(),max=policy.max(),value=numeric.value(),
			rangeMin=(true?round(value/labelTicks)*labelTicks:value)-range/2,
			rangeMax=rangeMin+range,
			setMin=rangeMin>min||local&&cycles?rangeMin:min,
			setMax=rangeMax<max||local&&cycles?rangeMax:max;
		if(setMin==min)setMax=min+range;
		else if(setMax==max)setMin=max-range;
		scale=1/tickValue;
		final int scaleValue=toScale(value),scaleMin=toScale(setMin),scaleMax=toScale(setMax),
			increments=getMaximum()-getMinimum();
		swing.setMinorTickSpacing(!tooDense?tickSpacing:5);
		swing.setMajorTickSpacing(labelTicks);
		swing.setSnapToTicks(policy.snapType()!=Ticked.SNAP_NONE);
		final double newLabelValues[]=new double[labelCount];
		if(false)Util.printOut("SliderSetter.setNumeric:" +
				" increments="+increments+" labelTicks="+labelTicks+
				" newLabelValues="+newLabelValues.length);
		for(int i=0;i<newLabelValues.length;i++)
		  newLabelValues[i]=sf(setMin+i*tickValue*labelTicks);
		if(labelValues==null||labelValues.length!=newLabelValues.length)
		  labelValues=newLabelValues;
		else{
		  int i=0;for(;i<labelValues.length;i++)
		    if(newLabelValues[i]!=labelValues[i])break;
		  if(i!=labelValues.length)labelValues=newLabelValues;
		}
		if(false)Util.printOut("SliderSetter: "+
		  (true?fx(setMin)+"<="+fx(value)+"<="+fx(setMax)+", labels=":"")+
		      Strings.fxString(labelValues));
		Runnable setValues=new Runnable(){public void run(){
		  setRangeProperties(scaleValue,0,scaleMin,scaleMax,true);
		  boolean newValues=labelValues==newLabelValues;
		  int decimals=policy.format();
		  if(newValues)tickLabels=
				true?newTickValueLabels(decimals,labelTicks,scaleMin,newLabelValues)
						:_newTickLabels(decimals,labelTicks,scaleMin,increments,tickValue);		    
			if(avoidingBlankingBug||newValues)swing.setLabelTable(tickLabels);
		}};
		if(false)SwingUtilities.invokeLater(setValues);
		else setValues.run();
	}
  public String toString(){
		return Debug.info(this)+" "+getMinimum()+"<="+getValue()+"<="+getMaximum()
				+", "+Util.fx(scale);
	}
	private Dictionary newTickValueLabels(int decimals,int labelTicks,int offset,
			double[]labelValues){
		if(false)Util.printOut("SliderSetter.newTickValueLabels: decimals="+decimals);
		Dictionary labels=new Hashtable();
		JLabel master=new JLabel();
		KitSwing.adjustComponents(false,master);
		Font font=master.getFont();
		font=new Font(font.getName(),font.getStyle(),
				(int)(font.getSize()*(false?1:0.9)));
		for(int v=0,tick=offset;v<labelValues.length;v++,tick+=labelTicks){
			String text=""+sf(labelValues[v]);
			if(decimals==0)text=text.replaceAll("\\.0","");
			JLabel label=new JLabel(text);
			label.setFont(font);
			labels.put(new Integer(tick),label);
		}
		return labels;
	}
  private Dictionary _newTickLabels(int decimals,int labelTicks,int offset,
			int increments,double tickValue){
		Dictionary labels=new Hashtable();
		for(int tick=offset;tick<=increments+1;tick+=labelTicks)
			labels.put(new Integer(tick),new JLabel(""+sf(tick*tickValue)));
		return labels;
	}
	private int toScale(double value){
		return (int)Math.round(value*scale);
	}
}
