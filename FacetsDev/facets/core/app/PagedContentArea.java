package facets.core.app;
import facets.core.app.SContentAreaTargeter.ContentArea;
import facets.core.superficial.Notifying;
import facets.core.superficial.SFacet;
import facets.core.superficial.SFrameTarget;
import facets.core.superficial.SIndexing;
import facets.core.superficial.STarget;
import facets.core.superficial.STargeter;
import facets.core.superficial.app.FacetedTarget;
import facets.util.Debug;
/**
Heads a content area tree for use in a dialog page. 
 */
public class PagedContentArea extends ContentArea{
  public final class PagedContentAreaTargeter extends SContentAreaTargeter{
		private SIndexing storeAreasIndexing;
		private boolean builtFacets=false;
		PagedContentAreaTargeter(Class type){
			super(type);
		}
		public void retarget(STarget target,Impact impact){
			super.retarget(target,impact);
			SAreaTarget area=(SAreaTarget)target;
			if(builtFacets||!(area.indexedTarget()instanceof FacetedTarget))return;
			storeAreasIndexing=area.indexing();
			area.setIndexing(SIndexing.newDefault(area.title(),
					new SFrameTarget[]{new SFrameTarget("Pre-build",
							contenter.contentFrame().framed)}));
			if(debug)finishBuild(area);
		}
		public void retargetFacets(Impact impact){
			super.retargetFacets(impact);
			SAreaTarget area=(SAreaTarget)target();
			if(!builtFacets&&area.isLive())finishBuild(area);
		}
		private void finishBuild(SAreaTarget area){
			if(storeAreasIndexing!=null)area.setIndexing(storeAreasIndexing);
			SFacet contentPanel=((PagedContenter)contenter).newContentPanel(this);
			if(contentPanel==null)throw new RuntimeException(
					"Illegal null content in "+Debug.info(this));
			((MountFacet)area.attachedFacet()).setFacets(new SFacet[]{contentPanel});
			builtFacets=true;
		}
	}
	protected static final boolean debug=false;
	public PagedContentArea(String title,STarget[]frames,PagedContenter contenter){
  	super(title,contenter,frames);
  }
  public final STargeter newTargeter(){
    return new PagedContentAreaTargeter(contenter.targetType());
  }
}