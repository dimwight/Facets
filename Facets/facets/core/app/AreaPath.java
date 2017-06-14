package facets.core.app;
import facets.core.superficial.FacetedTarget;
import facets.core.superficial.SFrameTarget;
import facets.core.superficial.STarget;
import facets.core.superficial.app.IndexingTarget;
import facets.core.superficial.app.SAreaTarget;
import facets.util.Debug;
import facets.util.ItemList;
import facets.util.OffsetPath;
import facets.util.Util;
final class AreaPath extends OffsetPath{
	AreaPath(SAreaTarget root){
		super(newPathMembers(root));
	}
	private static Object[]newPathMembers(SAreaTarget root){
		FacetedTarget target=root;
		STarget child;
		ItemList<FacetedTarget>members=new ItemList(FacetedTarget.class);
		while(target instanceof SAreaTarget){
			members.addItem(target);
			child=((SAreaTarget)target).indexedTarget();
			if(!(child instanceof FacetedTarget))break;
			target=(FacetedTarget)child;
		}
		return members.items();
	}
	protected int[]newOffsets(Object[]members){			
		int[]indices=new int[members.length];
		indices[0]=0;
		for(int i=1;i<indices.length;i++)
			indices[i]=((IndexingTarget)members[i-1]).indexing().index();
		return indices;
	}
	protected Object[]newMembers(Object root,int[]indices){
    Object[]members=new Object[indices.length];
    members[0]=root;
    for(int i=1;i<members.length;i++)
    	members[i]=((IndexingTarget)members[i-1])
    		.indexableTargets()[indices[i]];
		return members;
	}
	public OffsetPath reconstructPath(Object root){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	public OffsetPath procrusted(Object root,Object to){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
}