package facets.core.app.avatar;
import facets.util.Identified;
import facets.util.Titled;
/**
Marker interface for content to be depicted by a custom avatar. 
 */
public interface AvatarContent{
	public interface Applicable extends AvatarContent,Pickable,Identified,Titled{
		AvatarPolicy getAvatarPolicy(PainterSource p);
		DragPolicy getDragPolicy(Object hit,PainterSource p);
	}
	public interface State{}
}
