package facets.facet.kit.swing;
import facets.core.superficial.Notifying;
import facets.core.superficial.SNumeric;
import facets.core.superficial.STarget;
import facets.core.superficial.Notifying.Impact;
import facets.facet.FacetFactory;
import facets.facet.kit.*;
import facets.util.Debug;
import facets.util.StringFlags;
import facets.util.Tracer;
import facets.util.Util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyEvent;
import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
final class SliderPanel extends Widget implements KTargetable{
	static Tracer t=Tracer.newTopped(SliderPanel.class.getSimpleName(),true);
  private static final class SwingSlider extends JSlider{
		private final int sliderWidth;
		private SwingSlider(BoundedRangeModel brm,int sliderWidth){
			super(brm);
			this.sliderWidth=sliderWidth;
		}
		public Dimension getPreferredSize(){
			return new Dimension(sliderWidth,super.getPreferredSize().height);
		}
    protected void paintComponent(Graphics g){
    	if(false&&getModel().getValueIsAdjusting())return;
    	Runnable doRun=()->super.paintComponent(g);
    	if(false)SwingUtilities.invokeLater(doRun);
    	else if(true)doRun.run();
			else if(ui!=null){
				Graphics scratchGraphics=g.create();
				setForeground(Color.LIGHT_GRAY);
				try{
					ui.update(scratchGraphics,this);
				}finally{
					scratchGraphics.dispose();
				}
			}
		}
		protected void processKeyEvent(KeyEvent e){
		  if(true)getModel().setValueIsAdjusting(false);
		  super.processKeyEvent(e);
		}
	}
	private final JPanel panel;
	private final KitSwing kit;
  SliderPanel(KitFacet facet,KitSwing kit, 
  		final int sliderWidth,KWrap label,KWrap box,StringFlags hints){
  	super(facet,null);
		this.kit=kit;
  	boolean ticks=hints.includeFlag(FacetFactory.HINT_SLIDER_TICKS),
			labels=ticks&&hints.includeFlag(FacetFactory.HINT_SLIDER_LABELS);
  	SNumeric numeric=(SNumeric)facet.target();
		JSlider swing=new SwingSlider(new SliderSetter(this,numeric,box,hints),sliderWidth);
		KitSwing.adjustComponents(false,swing);
    swing.setPaintTicks(ticks);
    swing.setPaintLabels(labels);
  	setSwing(swing);
  	retarget(numeric,Notifying.Impact.DEFAULT);
  	final boolean bare=label==null,boxed=box!=null;
  	panel=hints.includeFlag(FacetFactory.HINT_TALL)?
  			constructTall(boxed,bare,swing,box,label)
  			:hints.includeFlag(FacetFactory.HINT_SQUARE)?
  			constructSquare(boxed,bare,swing,box,label)
 				:constructWide(boxed,bare,swing,box,label);
		if(FacetFactory.panelShade!=null)
			panel.setBackground(new Color(FacetFactory.panelShade.rgb()));
  }
  public void retarget(STarget target,Notifying.Impact impact){
		if(impact==Notifying.Impact.MINI)return;
    JSlider slider=(JSlider)swing;
    ((SliderSetter)slider.getModel()).setNumeric
    	((SNumeric)target,slider,slider.getPreferredSize().width);
  }
  public void setEnabled(boolean enabled){
		if(false)trace("SliderPanel: ",enabled);
		super.setEnabled(enabled);
	}
  public void setIndeterminate(boolean on){
	  JSlider slider=(JSlider)swing;
	  if(on)slider.setValue(slider.getMinimum());
		if(true)super.setIndeterminate(on);
	}
  public STarget target(){
    throw new RuntimeException("Not implemented in "+Debug.info(this));
  }
  public Object wrapped(){return panel;}
  private JPanel constructTall(final boolean boxed,final boolean bare,
  		JSlider slider,KWrap box,KWrap label){
  	GridBagLayout grid=new GridBagLayout(){{
  	  columnWeights=new double[]{1,1};
  	}};
  	JPanel panel=new JPanel(grid);KitCore.widgets++;
  	if(boxed){
      if(!bare)panel.add(swingWrapped(label),new GridBagConstraints(){{
        anchor=EAST;ipadx=4;
      }});
      panel.add(swingWrapped(box),new GridBagConstraints(){{
        gridx=bare?0:1;anchor=bare?CENTER:WEST;
      }});
    }
    else if(!bare)
      panel.add(swingWrapped(label),new GridBagConstraints(){{
        gridx=0;
      }});
    panel.add(slider,new GridBagConstraints(){{
      gridy=1;gridx=0;gridwidth=bare?1:2;fill=BOTH;
      ipady=5;
    }});
    return panel;
  }
  private JPanel constructSquare(final boolean boxed,final boolean bare,
  		JSlider slider,KWrap box,KWrap label){
  	GridBagLayout grid=new GridBagLayout();
  	JPanel panel=new JPanel(grid);KitCore.widgets++;
  	if(boxed){
      RowMount boxBase=new RowMount(FacetFactory.NO_FACET,kit,0,5,
      		new StringFlags(FacetFactory.HINT_PANEL_CENTER));
      boxBase.setItems(bare?
          new KWrap[]{box}:new KWrap[]{label,KWrap.BREAK,box});
      panel.add(swingWrapped(boxBase),new GridBagConstraints(){{
        gridx=0;ipadx=5;
      }});
    }
    else if(!bare)
      panel.add(swingWrapped(label),new GridBagConstraints(){{
        gridx=0;anchor=EAST;ipadx=5;
      }});
    panel.add(slider,new GridBagConstraints(){{
      gridx=!boxed&&bare?0:(boxed&&bare)||true?1:2;fill=BOTH;
    }});
    return panel;
  }
  private JPanel constructWide(final boolean boxed,final boolean bare,
  		JSlider slider,KWrap box,KWrap label){
  	JPanel panel=new JPanel(new FlowLayout(FlowLayout.LEADING,0,0));
  	KitCore.widgets++;
  	if(!bare)panel.add((Component)label.wrapped());
  	if(boxed)panel.add((Component)box.wrapped());
    panel.add(slider);
    return panel;
  }
}