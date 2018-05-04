package facets.core.superficial.app;
import facets.core.app.SView;
import facets.core.app.SViewer;
import facets.core.app.ViewableFrame;
import facets.util.StatefulCore;
/**
{@link SSelection}-aware {@link SView} implementation. 
<p>{@link SelectionView} creates a suitable {@link SSelection} for use by 
the viewer(s) it controls, based on the complete selection within
viewable content. 
 */
public abstract class SelectionView extends StatefulCore implements SView{
	/**
	Unique constructor. 
	@param title passed to superclass
	 */
	protected SelectionView(String title){
		super(title);
	}
	/**
	Implements interface method. 
	<p>Default returns <code>false</code>. 
	 */
	@Override
	public boolean isLive(){
		return false;
	}
	/**
	Implements interface method. 
	<p>Default returns <code>false</code>. 
	 */
	@Override
	public boolean allowMultipleSelection(){
		return false;
	}
	/**
	Return content to be displayed by viewer. 
	<p>Enables delegation from implementations of {@link ViewableFrame#newViewerSelection(SViewer)};
	default returns <code>viewable</code>
	@param viewer controlled by this view 
	 @param viewable the current {@link ViewableFrame#selection()}
	 */
	public SSelection newViewerSelection(SViewer viewer,SSelection viewable){
		return viewable;
	}
	public boolean canChangeSelection(){
		return true;
	}
	public boolean contextClickSelects(){
		return false;
	}
}
