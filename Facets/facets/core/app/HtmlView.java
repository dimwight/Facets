package facets.core.app;
import static facets.util.tree.Nodes.*;
import facets.core.superficial.app.SSelection;
import facets.core.superficial.app.SViewer;
import facets.util.Debug;
import facets.util.OffsetPath;
import facets.util.Util;
import facets.util.tree.DataNode;
import facets.util.tree.Nodes;
import facets.util.tree.TypedNode;
import java.util.ArrayList;
import java.util.List;
/**
{@link TextView} displaying rich text defined using HTML. 
 */
public class HtmlView extends TextView{
	public static class InputView extends HtmlView{
		public InputView(String title){
			super(title);
		}
	}
	public static class SmartView extends HtmlView{
		public SmartView(String title){
			super(title);
		}
		/**
		Re-implementation returning <code>true</code> for rich text. 
		 */
		public boolean wrapLines(){
			return true;
		}
		/**
		Allows optimised rendering by truncation of content to fit viewer size. 
		<p>If the associated {@link SViewer} returns as {@link SSelection#content()} a 
		{@link String}[] of lines, the viewer facet can optimise by only rendering a subset of 
		these; to do so it needs an estimate of height of each line.   
		@return by default -1 to signal no value; or estimated pixel height of each line
		 */
		public int quickLineHeight(){
			return -1;
		}
	}
	public static boolean showAllSources;
	/** Key/node type for HTML source. */
	public static final String KEY_SOURCE="htmlSource";
	public HtmlView(String title){
		super(title);
	}
	/**
	Instructs the viewer to display the HTML source. 
	<p>Default returns {@link #showAllSources}. 
	 */
	public boolean showSource(){
		return showAllSources;
	}
}
