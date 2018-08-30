package facets.core.app;
import facets.core.superficial.app.SSelection;
import facets.core.superficial.app.SelectingFrame;
import facets.util.Debug;
import facets.util.app.Events;
/**
{@link SelectingFrame} that can respond to input from {@link SViewer}s and 
{@link ViewableAction}s.
<p>{@link ViewableFrame} extends its superclass by 
	allowing for response both to mouse-defined {@link SViewer} input 
		and to non-mouse {@link ViewableAction}s. 
 */
abstract public class ViewableFrame extends SelectingFrame{
  /**
  Unique constructor. 
  @param title passed to superclass
  @param content passed to superclass
   */
  public ViewableFrame(String title,Object content){
		super(title,content);
	}
  /**
  Return {@link SSelection} to be displayed by viewer. 
	 <p>Called by {@link ViewerTarget#selection()}, should return some combination of 
<ul>
	<li>a sub-selection within {@link #framed} 
	<li>an alternative presentation of {@link #framed} suitable for use by <code>viewer</code>
	</ul>	 
	@param viewer requiring selection for return by {@link SViewer#selection()}
	@return by default {@link #selection()}
   */
  protected SSelection newViewerSelection(SViewer viewer){
		return selection();
	}
	/**
	Respond to change in selection. 
	<p>Called from {@link ViewerTarget#selectionChanged(SSelection)}, must
	interpret a new viewer selection in terms of the complete content. 
	Default implementation calls {@link #setSelection(SSelection)} with 
	<code>selection</code>. 
	@param viewer in which the change took place 
	@param selection the new selection in the viewer
	 */
	protected void viewerSelectionChanged(SViewer viewer,SSelection selection){
		setSelection(selection);
	}
	/**
	Respond to viewer edit. 
	<p>Called from {@link ViewerTarget#selectionEdited(SSelection, Object, boolean)}, may
	respond by changing content state to match <code>edit</code>. 
	<p>Default implementation calls {@link #setFramedState(Object, boolean)} with
	<code>edit</code>.
	@param viewer proposing the edit
	 @param edit may be multiple edits to match implementor
	of {@link SSelection}.
	 @param interim if <code>true</code> the edit forms part of a sequence 
	 */
	protected void viewerSelectionEdited(SViewer viewer,Object edit,boolean interim){
		setFramedState(edit,interim);
	}
	/**
	Propose an action in the viewer passed. 
	<p>Default implementation is an invalid stub. 
	@param viewer the active viewer
	@param action specifies the action proposed
	 */
	protected void actionTriggered(SViewer viewer,ViewableAction action){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	/**
	 Should the action be exposed as live in the surface? 
	 <p>Default returns <code>false</code>.
	 @param viewer the active viewer
	 @param action to be exposed
	 */
	public boolean actionIsLive(SViewer viewer,ViewableAction action){
		return false;
	}
	/**
	Return actions to be made available in a viewer
	@param view controls the viewer
	 */
	protected ViewableAction[]viewerActions(SView view){
		return new ViewableAction[]{};
	}
}
