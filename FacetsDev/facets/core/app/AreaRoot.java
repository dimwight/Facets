package facets.core.app;
import static facets.util.Debug.*;
import facets.core.superficial.SIndexing;
import facets.core.superficial.STarget;
import facets.core.superficial.app.FacetedTarget;
import facets.core.superficial.app.IndexingTarget;
import facets.util.Debug;
import facets.util.ItemList;
import facets.util.OffsetPath;
public class AreaRoot extends SAreaTarget{
	final private static class AreaPath extends OffsetPath{
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
	AreaRoot(String title,SIndexing children){
		super(title,children);
	}
	public AreaRoot(String title,STarget...indexableChildren){
		this(title,SIndexing.newDefault(title,indexableChildren));
	}
	/**
	Attaches existing facets to the new area tree. 
	<p>Also applies the old area path to the new tree. 
	@param rootThen heads the area tree to which the existing facets are attached
	 */
	public final void attachThenFacets(SAreaTarget rootThen){
	  STarget[]descentThen=rootThen.descendants(),
	  	descentNow=false||mutableAreaFacets?descendants():expandDescendants();
		if(false)trace(".attachThenFacets: descentThen=",
					arrayInfo(descentThen)+"\ndescentNow="+arrayInfo(descentNow));
		if(descentNow.length!=descentThen.length)throw new IllegalStateException(
				"Unequal descents in "+this);
		if(false)trace(".attachThenFacets~: descentNow="+arrayInfo(descentNow));
		for(int i=0;i<descentNow.length;i++)
			if(descentNow[i]instanceof FacetedTarget){
				FacetedTarget then=(FacetedTarget)descentThen[i],
					now=(FacetedTarget)descentNow[i];
				now.attachFacet(then.attachedFacet());
				if(now instanceof SViewer)now.setLive(then.isLive());
				if(false)traceDebug(".attachThenFacets: ",now.attachedFacet());
			}
		Object pathTarget=new AreaPath(rootThen).target(this);//N0rmanDav1es
		if(false)trace(".attachThenFacets: ",
				info(rootThen.activeFaceted())+", "+info(pathTarget));
		if(pathTarget instanceof FacetedTarget)
			((FacetedTarget)pathTarget).ensureActive(Impact.MINI);
		if(false)retargetFacets(Impact.DEFAULT);
	}
	private STarget[]expandDescendants(){
		while(true){
			STarget[]descendants=descendants();
			int descended=descendants.length;
			if(false)trace(".expandDescendants: descendants=",descendants.length);
			for(STarget d:descendants){
				if(false&&d!=this&&d instanceof AreaRoot)break;
				if(d instanceof ViewerTarget){
					SView view=((SViewer)d).view();
					if(view instanceof NestedView){
						((NestedView)view).expandViewer((ViewerTarget)d);
						descendants=descendants();
						break;
					}
				}
			}
			if(descended==descendants.length)return descendants;
		}
	}
}
