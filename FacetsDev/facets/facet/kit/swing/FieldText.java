package facets.facet.kit.swing;
import static facets.facet.FacetFactory.*;
import facets.core.app.SurfaceStyle;
import facets.core.superficial.SNumeric;
import facets.core.superficial.STarget;
import facets.core.superficial.STextual;
import facets.core.superficial.STextual.Update;
import facets.facet.FacetFactory;
import facets.facet.SwingPanelFacet;
import facets.facet.FacetFactory.SuggestionsCoupler;
import facets.facet.kit.Decoration;
import facets.facet.kit.KField;
import facets.facet.kit.KitCore;
import facets.facet.kit.KitFacet;
import facets.util.Debug;
import facets.util.StringFlags;
import facets.util.Times;
import facets.util.tree.Nodes;
import facets.util.tree.ValueNode;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.Collection;
import javax.swing.Box;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.MenuElement;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.MenuKeyEvent;
import javax.swing.event.MenuKeyListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
/**
{@link FieldText} encapsulates some complex logic to enable intuitive
user interaction: 
<ul>
	<li>appropriate responses to input and to change in focus 
	<li>(optional) interim notification as field text is edited 
</ul>
<table width="100%" border="1">
	<tr> 
		<td width="20%"><b>Event</b></td>
		<td width="40%"><b>Interim off</b></td>
		<td width="40%"><b>Interim on</b></td>
	</tr>
	<tr> 
		<td><i>Focus gained</i></td>
		<td colspan="2" align="center"> Ready field</td>
	</tr>
	<tr> 
		<td><i>Edit</i></td>
		<td> None</td>
		<td> (Create notifier,) revert invalid field, notify interim</td>
	</tr>
	<tr> 
		<td><i>Esc</i></td>
		<td> Revert field</td>
		<td> Revert field; notify interim</td>
	</tr>
	<tr> 
		<td><i>Enter, focus lost</i></td>
		<td colspan="2"> Revert invalid field, notify final, ready field</td>
	</tr>
</table>
<p>It also supports undo/redo(Ctrl-Z/Y).
 */
class FieldText extends Widget implements KField{
	private final JFormattedTextField field=new JFormattedTextField();
	private final JLabel label;
	private final FocusRequester focusRequestor=new FocusRequester(field);
	private final boolean formUsage,updateInterim;
	private final FieldTextSuggester suggester;
  protected String lastSetText="",lastEditText="";
	private boolean indeterminate;
  private Timer notifier;
	protected FieldText(KitFacet facet,int cols,KitCore kit,StringFlags hints,
			boolean alignRight){
		this(facet,cols,kit,hints);
		if(alignRight)field.setHorizontalAlignment(JFormattedTextField.RIGHT);
	}
	FieldText(final KitFacet facet,int cols,KitCore kit,StringFlags hints){
		super(facet,null);
		formUsage=hints.includeFlag(HINT_USAGE_FORM);
		STextual textual=findTextual();
		updateInterim=textual!=null&&textual.coupler.updateInterim(textual);
		boolean bare=hints.includeFlag(FacetFactory.HINT_BARE);
		JPanel base=new JPanel(new FlowLayout(FlowLayout.LEADING,0,0));
		KitSwing.adjustComponents(false,base);
		decoration=kit.decoration(facet.title(),hints);
		label=bare?null:new JLabel(decoration.caption);
		if(label!=null){
			if(!decoration.hints.includeFlag(HINT_TOOLTIPS)&&!decoration.rubric.equals(""))
				label.setToolTipText(decoration.rubric);
			if(FacetFactory.surfaceStyle==SurfaceStyle.DESKTOP)
				label.setDisplayedMnemonic(decoration.mnemonic);
			swingMap.put(SwingPanelFacet.KEY_LABEL,label);
			KitSwing.adjustComponents(false,label);
			if(false)base.add(Box.createHorizontalStrut(3));
			base.add(label);
			label.setLabelFor(field);
			base.add(Box.createHorizontalStrut(3));
			KitCore.widgets++;
			KitSwing.debugSwing(label,facet,this);
		}
		swingMap.put(SwingPanelFacet.KEY_LABELLED,field);
		base.add(field);
		KitSwing.debugSwing(field,facet,this);
		KitCore.widgets++;
		setSwing(base);
		KitSwing.adjustComponents(false,field);
		field.setColumns(cols);
		field.setEditable(false);
		field.addFocusListener(new FocusListener(){
			public void focusGained(FocusEvent e){
				if(true&&suggester==null)field.selectAll();
				else if(false)readyField(lastSetText=text());
			}
			public void focusLost(FocusEvent e){
				notifyFinalValid(Update.Simple);
			}
		});
		field.addKeyListener(new KeyAdapter(){
	    public void keyPressed(KeyEvent e){
	    	boolean interim=updateInterim&&(true||dragNotifyInterim);
	    	int keyCode=e.getKeyCode();
				switch(keyCode){
	    	case KeyEvent.VK_ESCAPE:
	    		readyField(lastSetText);
	    		field.selectAll();
					if(interim)facet.targetNotify(this,true);
	    		break;
	    	case KeyEvent.VK_ENTER:
	    		notifyFinalValid(Update.Commit);
	    		break;
	    	default:
	    		handleFieldInput(keyCode,interim);
	    	}
	    }
	    public void keyTyped(KeyEvent e){
	  		boolean ctrl=(e.getModifiers()&KeyEvent.CTRL_MASK)!=0;
	  		if(false)trace(".keyPressed: ",e.getKeyChar());
	    }
	  });
		suggester=new FieldTextSuggester(field);
	}
  private void notifyFinalValid(Update update){
		validateFieldText();
		if(notifier!=null)notifier.stop();
		notifier=null;
		String text=field.getText();
		if(false)trace(".notifyFinalValid: text="+text+" lastSetText="+lastSetText);
		if(!text.equals(lastSetText)||update==Update.Commit)
			facet().targetNotify(update,false);
	}
	public void setEnabled(boolean enabled){
  	field.setFocusable(enabled);
		if(formUsage){
			field.setEditable(enabled);
			if(label!=null)label.setDisplayedMnemonic(enabled?
					decoration.mnemonic:0);
		}
		else{
			field.setEnabled(enabled);
			if(label!=null)label.setEnabled(enabled);
		}
		STextual textual=findTextual();
		if(textual!=null&&textual.coupler instanceof SuggestionsCoupler)
			suggester.setSource((SuggestionsCoupler)textual.coupler);
	}
	private void handleFieldInput(int keyCode,boolean interim){
		lastEditText=field.getText();
		if(!interim)return;
		if(notifier==null)notifier=new Timer(dragNotifyPause,
				new ActionListener(){		
			public void actionPerformed(ActionEvent e) {
				validateFieldText();
				facet().targetNotify(this,true);
			}
		}){
			public boolean isRepeats(){return false;}
		};
		notifier.stop();
		notifier.start();
	}
	private void validateFieldText(){
		String text=field.getText();
		STextual textual=findTextual();
		if(textual!=null&&!textual.coupler.isValidText(textual,text))
			readyField(lastEditText);
	}
	private STextual findTextual(){
		KitFacet facet=facet();
		STarget target=facet.target();
		if(target instanceof STextual)return(STextual)target;
		if(target instanceof SNumeric)return null;
		target=target.elements()[0];
		return target instanceof STextual?(STextual)target:null;
	}
	public void setText(String text){
		if(text==null)throw new IllegalArgumentException(
				"Null text in "+Debug.info(this));
		if(notifier==null)readyField(lastSetText=text);
	}
	private void readyField(String text){
		field.setText(text);
		field.setCaretPosition(text.length());
	}
  public void setIndeterminate(boolean on){
		if(notifier!=null)return;
		readyField(lastSetText=(indeterminate=on)?"":lastSetText);
	}
	public String text(){
		return field.getText().replaceAll("\\s"," ");
	}
	public void makeEditable(){
		field.setEditable(true);
	}
	public void requestFocus(){
		focusRequestor.startRequesting();
	}
	public String toString(){
		return super.toString()+" "+text();
	}
	public void setValue(double value){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	public double value(){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
}
