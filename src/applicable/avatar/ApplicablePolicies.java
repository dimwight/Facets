package applicable.avatar;
import facets.core.app.PathSelection;
import facets.core.app.SViewer;
import facets.core.app.avatar.AvatarContent;
import facets.core.app.avatar.AvatarContent.Applicable;
import facets.core.app.avatar.AvatarPolicies;
import facets.core.app.avatar.AvatarPolicy;
import facets.core.app.avatar.AvatarView;
import facets.core.app.avatar.DragPolicy;
import facets.core.app.avatar.PainterSource;
import facets.core.superficial.app.SSelection;
import facets.util.ArrayPath;
import facets.util.tree.TypedNode;
import java.util.ArrayList;
import java.util.List;
public abstract class ApplicablePolicies extends AvatarPolicies{
	@Override
	final public SSelection newAvatarSelection(SViewer viewer,SSelection viewable){
		List<AvatarContent>content=new ArrayList();
		for(TypedNode item:((TypedNode)viewable.content()).children())
			content.add(newItemApplicable(item));
		return new PathSelection(content.toArray(new AvatarContent[0]),new ArrayPath(
		 ((PathSelection)viewable).paths[0].offsets));
	}
	protected abstract Applicable newItemApplicable(TypedNode item);
	@Override
	final public AvatarPolicy viewerPolicy(SViewer viewer, AvatarContent content,
                                           PainterSource p){
		return((Applicable)content).getAvatarPolicy(p);
	}
	@Override
	final public DragPolicy dragPolicy(AvatarView view,AvatarContent[]content,Object hit,
			PainterSource p){
		return((Applicable)content[0]).getDragPolicy(hit,p);
	}
}