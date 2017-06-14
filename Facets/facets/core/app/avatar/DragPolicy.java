package facets.core.app.avatar;
import facets.core.app.avatar.AvatarContent.State;
import facets.core.app.avatar.DragPolicy.Constraints;
import facets.core.superficial.app.SViewer;
import facets.util.Debug;
import facets.util.Stateful;
import facets.util.geom.Point;
import facets.util.geom.Vector;
/**
Defines how a custom viewer should drag an avatar selection. 
<p>A {@link DragPolicy} supplies the {@link Painter}s needed by 
  a viewer to paint the content of an avatar selection as it 
  is dragged, and specifies the effects on content of terminating 
  the drag. 
 */
public abstract class DragPolicy{
	public enum Constraints implements AvatarContent.State{
		None,Cross,ThreeAxial,SixAxial}
	/**
 	Return motion painters for the specified drag position. 
  <p>New painters will generally be requested for each change 
    in <code>dragAt</code>. 
    @param anchorAt starting point of the drag 
	 * @param dragAt latest (possibly constrained) drag position 
	 * @return a non-<code>null</code> {@link Painter}[] 
	 */
	public abstract Painter[]newDragPainters(Point anchorAt,Point dragAt);
	/**
	Return suitable edits for the drop specified. 
    <p>@param anchorAt starting point of the drag 
    @param dragAt final (possibly constrained) drag position before dropping 
      @return an {@link Object}[] specifying changes to the content 
      of the dragged selection  
	 */
	public abstract Object[]newDragDropEdits(Point anchorAt,Point dragAt);
	/**
	Defines the cursor to appear over different drag states. 
    @param state an implementation-dependent object 
    @return an implementation-dependent object
	 */
	public Object stateCursor(State state){
		return state==Constraints.None?SViewer.CURSOR_MOVE:
				SViewer.CURSOR_DEFAULT;
	}
	/**
	Specifies constraints on the movement of the drag. 
	@return an implementation-dependent object
	 */
	public Constraints constraints(){return Constraints.None;}
	/**
	Should the viewer attempt to snap the drag to non-selected 
    avatars?
	<p>Default returns <code>false</code>. 
	 */
	public boolean checkSnap(){return false;}
	/**
	Variant method for use in snapping. 
	<p>Invalid implementation
	 */
	public Object[]newSnapDropEdits(Point anchorAt,
			Point dragAt,Vector snapShift){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	/**
	Variant method for use in snapping. 
	<p>Invalid implementation
	 */
	public Painter[]newSnapPainters(Point anchorAt,
			Point dragAt,Vector snapShift){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
}