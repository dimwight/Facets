package facets.core.app;
import static facets.util.Debug.*;
import facets.core.superficial.FacetedTarget;
import facets.core.superficial.SIndexing;
import facets.core.superficial.STarget;
import facets.core.superficial.app.SAreaTarget;
import facets.core.superficial.app.SView;
import facets.core.superficial.app.SViewer;
import facets.core.superficial.app.ViewerTarget;
public class AreaRoot extends SAreaTarget{
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
	final void attachThenFacets(AreaRoot rootThen){
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
		Object pathTarget=new AreaPath(rootThen).target(this);
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
