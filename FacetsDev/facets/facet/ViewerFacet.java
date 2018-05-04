package facets.facet;
import static facets.core.superficial.TargeterCore.*;
import static facets.facet.AreaFacets.*;
import static facets.facet.FacetFactory.*;
import static facets.util.app.Events.*;
import facets.core.app.SAreaTarget;
import facets.core.app.ViewerTarget;
import facets.core.superficial.Notifying;
import facets.core.superficial.SFacet;
import facets.core.superficial.SFrameTarget;
import facets.core.superficial.STarget;
import facets.core.superficial.STargeter;
import facets.core.superficial.TargetCore;
import facets.core.superficial.Notifying.Impact;
import facets.core.superficial.STarget.Targeted;
import facets.core.superficial.TargeterCore;
import facets.facet.kit.*;
import facets.util.Debug;
import facets.util.StringFlags;
import facets.util.tree.ValueNode;
final class ViewerFacet extends FacetCore{
	final ViewerAreaMaster vam;
	private final KViewer viewer;
	private final ValueNode areaState;
	final FacetFactory ff;
	ViewerFacet(ViewerTarget target,FacetFactory ff,ViewerAreaMaster vam,ValueNode areaState){
		super(target,ff.kit);
		this.ff=ff;
		this.vam=vam;
		this.areaState=areaState;
		target.attachFacet(this);
		viewer=ff.kit.masteredViewer(this,vam,target.view(),areaState);
	}
	@Override
	public void dispose(){
		if(false)viewer.refresh(Impact.DISPOSE);
	}
  @Override
	final public void retarget(STarget target,Impact impact){
		if(impact==Notifying.Impact.MINI)return;
		super.retarget(target,impact);
		if(!(target instanceof ViewerTarget)){
			if(true)trace(".retarget: NOT A VIEWER target=",target);
			else return;
		}
		else((KViewer)base()).refresh(impact);
		if(false&&!trace)trace(": Refreshed "+Debug.info(this)+" with "+Debug.id(target));
		else if(trace)traceEvent("Refreshed "+Debug.info(this)+" with "+Debug.id(target));
	}
	@Override
	public void targetNotify(Object msg,boolean interim){
		((KitFacet)((ViewerTarget)target).areaParent().attachedFacet()
				).targetNotify(msg,interim);
	}
  @Override
	KWrap lazyBase(){
		return viewer;
	}
  @Override
	public STarget[]targets(){
		if(viewer==null)throw new IllegalStateException(
				"Viewer not set (in constructor): "+Debug.info(this));
		else return((Targeted)viewer).targets();
	}
  @Override
	KWrap[]lazyParts(){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	SFacet newAreaFacet(SAreaTarget area){
		if(area==null)throw new IllegalArgumentException("Null area in "+Debug.info(this));
		final STargeter viewTargeter=newRetargeted(((ViewerTarget)target()).viewFrame(),true);
		return new AreaMountCore(area,kit){
			@Override
			protected KWrap lazyBase(){
			  SFacet tools=vam.newViewTools(viewTargeter);		
			  if(tools!=null)viewer.setTools(((KitFacet)tools).base());
			  KMount mount=kit.spreadMount(this,false);
				mount.setItem(viewer);
			  return mount;
			}
		  @Override
			protected void retargeted(SAreaTarget area,Impact impact){
				super.retargeted(area,impact);
				SAreaTarget areaParent=area.areaParent();
				SFacet panesLike=areaParent==null?null:areaParent.attachedFacet();
				if(panesLike!=null&&panesLike instanceof PaneSet)
					viewer.setPaneControl(((PaneSet)panesLike).getViewerControl(this));
				STarget view=((ViewerTarget)area.activeFaceted()).viewFrame(),
					viewTarget=viewTargeter.target();
				if(view!=viewTarget)viewTargeter.retarget(view,Impact.DEFAULT);
				view.setLive(area.isActive());
				viewTargeter.retargetFacets(impact);
			}
		  @Override
			public void setFacets(SFacet...facets){
				throw new RuntimeException("Not implemented in "+Debug.info(this));
			}
		};
	}
}