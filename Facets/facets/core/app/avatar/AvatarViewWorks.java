package facets.core.app.avatar;
import facets.core.app.PathSelection;
import facets.core.app.SViewer;
import facets.core.superficial.app.SSelection;
import facets.core.superficial.app.SelectionView;
import facets.util.Debug;
/**
{@link SelectionView} that (partially) implements {@link AvatarView}.
	<p>Major policy methods delegate to the {@link AvatarPolicies} passed to 
	the constructor. 
 */
public abstract class AvatarViewWorks extends SelectionView implements AvatarView{
	protected final AvatarPolicies avatars;
  public AvatarViewWorks(String title,AvatarPolicies avatars){
		super(title);
		this.avatars=avatars;
	}
  @Override
  final public AvatarPolicies avatars(){
  	return avatars;
  }
  /**
	Implements interface method. 
	<p>Returns <code>false</code>. 
	 */
  @Override
	public boolean doesDnD(){
		return false;
	}
}
