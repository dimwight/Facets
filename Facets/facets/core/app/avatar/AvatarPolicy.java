package facets.core.app.avatar;
import facets.core.app.SViewer;
import facets.core.app.avatar.AvatarContent.State;
import facets.core.app.avatar.Painter.Style;
import facets.util.Stateful;
/**
Defines how a custom avatar should paint its content. 
<p>An {@link AvatarPolicy} supplies the {@link Painter}s needed 
  by an avatar to paint its content in different selection and 
  pick states. 
 */
public abstract class AvatarPolicy{
	/**
	Return painters for the unpicked avatar. 
	@param selected is the avatar selected?
	@param active is the view active?
	 */
	public abstract Painter[]newViewPainters(boolean selected,boolean active);
	/**
	Return motion painters to appear over the view painters. 
	@param selected is the avatar selected?
	@param hit defines where the hit occurred that triggered the change in painters
	 */
	public abstract Painter[]newPickPainters(Object hit,boolean selected);
	/**
	Defines the cursor to appear over the avatar in different 
    pick states. @param state an implementation-dependent object 
    @return an implementation-dependent object
	 */
	public Object stateCursor(State state){
		return state==Painter.Style.PickedSelected?SViewer.CURSOR_HAND:
				SViewer.CURSOR_DEFAULT;
	}
	/**
	Allows the policy to define a response where no avatar 
    has been picked. 
	 */
	public void avatarNotPicked(){}
}