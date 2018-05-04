package apps.idiom;
import facets.facet.kit.swing.KitSwing;
import facets.facet.kit.swing.SmartOptionPane;
import facets.util.Debug;
import facets.util.Tracer;
import facets.util.app.AppValues;
import facets.util.tree.ValueNode;
import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
/**
Platform for desktop applets demonstrating Facets idioms. 
<p>As well as supporting other demonstrator applets, {@link IdiomPane}
itself demonstrates use of {@link AppValues}
to store its position between sessions. 
<p>{@link IdiomPane} is declared <code>abstract</code> because 
{@link #newPanel(Container)} is a invalid stub; 
incidentally demonstrating the more general idiom of an abstract
class that can be run immediately and messages which of its methods need valid implementations.    
 */
public abstract class IdiomPane extends Tracer{
	private static final String KEY_LAST_AT="lastAt";
	final public String idiomName=getClass().getSimpleName();
	protected final AppValues values;
	private String rubric="Your rubric here";
	protected IdiomPane(String[]args){
		values=new AppValues(IdiomPane.class);
		values.readValues(args);
	}
	final public void buildAndLaunch(boolean buildOnly){
		ValueNode state=values.state();
		int[]ints=state.getInts(KEY_LAST_AT);
		Point lastAt=ints.length==0?SmartOptionPane.DEFAULT_AT
				:new Point(ints[0],ints[1]);
		SmartOptionPane pane=new SmartOptionPane(lastAt,true);
		Component panel=newPanel(pane);
		if(buildOnly)return;
		pane.setOptions(new Object[]{panel});
		pane.setMessage(rubric);
		lastAt=pane.displayInDialog(null,idiomName);
		state.put(KEY_LAST_AT,lastAt.x+","+lastAt.y);
		values.tryWriteValues("");
		System.exit(0);
	}
	/**
	Set an explanatory text for the {@link IdiomPane}. 
	@param rubric will appear above the panel created in {@link #newPanel(Container)}
	 */
	final protected void setRubric(String rubric){
		this.rubric=rubric;
	}
	/**
	Create the content for the {@link IdiomPane}. 
	@param pane container for the content
	@return a {@link Component} to be passed to {@link JOptionPane#setOptions(Object[])};
	by default an invalid stub
	 */
	protected Component newPanel(Container pane){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}	
	/**
	Simple usage example. 
	 */
	public static void main(String[]args){
		new IdiomPane(args){
			protected Component newPanel(Container pane){
				String arg=values.nature().getString("arg");
				return new JLabel(!arg.equals("")?("arg="+arg):"No arg passed");
			};
		}.buildAndLaunch(false);
	}
}
