package facets.core.app;
import static facets.core.superficial.app.AreaTargeter.*;
import static facets.util.app.Events.*;
import facets.core.app.AppSurface.ContentStyle;
import facets.core.superficial.FacetedTarget;
import facets.core.superficial.Notice;
import facets.core.superficial.Notifying;
import facets.core.superficial.Notifying.Impact;
import facets.core.superficial.SFacet;
import facets.core.superficial.SFrameTarget;
import facets.core.superficial.SIndexing;
import facets.core.superficial.SIndexing.Coupler;
import facets.core.superficial.STarget;
import facets.core.superficial.app.AreaTargeter;
import facets.core.superficial.app.IndexingTarget;
import facets.core.superficial.app.SAreaTarget;
import facets.core.superficial.app.SContentAreaTargeter;
import facets.core.superficial.app.SContenter;
import facets.core.superficial.app.SContentAreaTargeter.ContentArea;
import facets.core.superficial.app.SHost.FacetLayout;
import facets.util.Debug;
import facets.util.ItemList;
import facets.util.Objects;
import facets.util.Tracer;
import facets.util.TypesKey;
import facets.util.app.WatchableOperation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
final class AppAreas extends Tracer{
	AreaTargeter targeter;
	TypesKey featuresKey;
	private final Map<TypesKey,FacetLayout>layouts=new HashMap();
	private final Headless headless;
	private final AppSurface app;
	private final SAreaTarget emptyArea;
	private FacetLayout layout;
	private MountFacet appFacet;
	AppAreas(AppSurface app){
		this.app=app;
		headless=app.isHeadless()?new Headless():null;
		emptyArea=((SContenter)app.emptyContent).newContentArea(true);
	}
	AreasUpdate newUpdate(){
		return new AreasUpdate(targeter==null?new SAreaTarget[]{}
			:(SAreaTarget[])appArea().indexableTargets());
	}
	final class AreasUpdate{
		final private ItemList<SAreaTarget>areas=new ItemList(SAreaTarget.class);
		final private SAreaTarget[]areasThen;
		private int areaAt;
		AreasUpdate(SAreaTarget[]areasThen){
			this.areasThen=areasThen;
			areaAt=areasThen.length==0?0:appArea().indexing().index();
		}
		void addFreshArea(AppContenter content){
	    if(areasThen.length>0&&areasThen[0]!=emptyArea)
	    	areas.addItems(areasThen);
	    SAreaTarget add=content==app.emptyContent?emptyArea
	    	:((SContenter)content).newContentArea(true);
			areas.addItem(add);
			areaAt=areas.size()-1;
			finishUpdate();
		}
		void addAlignedArea(AppContenter content){
	    int alignAt=appArea().indexing().index();
			SAreaTarget align=areasThen[alignAt],
	    	add=((SContenter)content).newContentArea(true);
	    content.alignContentAreas(align,add);
	    for(int i=0;i<areasThen.length;i++){
				areas.addItem(areasThen[i]);
				if(i!=alignAt)continue;
				areas.addItem(add);
				areaAt=i+1;
			}
			finishUpdate();
		}
		void moveActiveArea(int to){
			int from=appArea().indexing().index();
			if(false)trace(".moveActiveArea: from="+from+" to="+to);
			areas.addItems(areasThen);
		  areas.add(to,areas.remove(from));
			finishUpdate();
			appArea().indexing().setIndex(to);
		}
		void replaceActiveArea(AppContenter content){
			SAreaTarget areaThen=(ViewerContentArea)appArea().indexedTarget(),
				areaNow=((SContenter)content).newContentArea(false);
			content.alignContentAreas(areaThen,areaNow);
		  if(areaNow.getClass()!=areaThen.getClass())
		  	throw new RuntimeException("Non-matching areas in "+Debug.info(app));
		  else ((AreaRoot)areaNow).attachThenFacets(areaThen); 
		  for(ViewerContentArea area:viewerAreas(areasThen))
		  	if(area==areaThen){
					areas.addItem(areaNow);
					areaAt=areas.size()-1;
				}
		  	else if(area.contenter.contentFrame()
		  			!=((ViewerContentArea)areaThen).contenter.contentFrame())
		  		areas.addItem(area);
		  finishUpdate();
		}
		void removeArea(SAreaTarget area){
			for(int i=0;i<areasThen.length;i++)
				if(areasThen[i]==area)areaAt=i>0?i-1:0;
				else areas.addItem(areasThen[i]);
			finishUpdate();
			disposeFacets(area);
		}
		void removeContentAreas(AppContenter content){
			List<SAreaTarget>removed=new ArrayList();
			SFrameTarget frame=((ViewerContenter)content).contentFrame();
			for(ViewerContentArea area:viewerAreas(areasThen))
				if(area.contenter.contentFrame()==frame){
					removed.add(area);
					areaAt-=areaAt==0?0:1;
				}
				else areas.addItem(area);
			finishUpdate();
			for(SAreaTarget area:removed)disposeFacets(area);
		}
		private void disposeFacets(SAreaTarget area){
			if(true)for(STarget d:area.descendants())
				if(d instanceof FacetedTarget){
					SFacet facet=((FacetedTarget)d).attachedFacet();
					if(false)traceDebug(".disposeFacets: facet=",facet);
					facet.dispose();
				}
		}
		private void finishUpdate(){
			WatchableOperation op=new WatchableOperation("AreasUpdate.finishUpdate"){
		@Override
		public void doSimpleOperation(){
		  SAreaTarget[]areasNow=areas.items();
		  if(areasNow.length==0)areasNow=new SAreaTarget[]{emptyArea};
			boolean emptyNow=areasNow[0]==emptyArea;
		  MountFacet[]areaFacets=null;
		  if(!emptyNow){
				ContentArea[]viewers=viewerAreas(areasNow);
				Map<SFrameTarget,Integer>counts=new HashMap();
				for(ContentArea area:viewers){
					SFrameTarget frame=area.contenter.contentFrame();
					Integer count=counts.get(frame);
					int useCount=count==null?0:count;
					counts.put(frame,useCount+1);
				}
				for(ContentArea area:viewers){
					SFrameTarget frame=area.contenter.contentFrame();
					int count=counts.get(frame);
					if(count==1)counts.put(frame,0);
				}
				for(ViewerContentArea area:Objects.reverse(ViewerContentArea.class,viewers)){
					SFrameTarget frame=area.contenter.contentFrame();
					int useCount=counts.get(frame);
					area.multi=(useCount>0?":"+useCount:"");
					counts.put(frame,--useCount);
				}
				areaFacets=new MountFacet[areasNow.length];
			  for(int i=0;i<areaFacets.length;i++)
			  	areaFacets[i]=(MountFacet)areasNow[i].attachedFacet();
			}
			SAreaTarget appArea=new AppArea(app,areasNow,areaAt);
			if(appFacet==null)appFacet=app.contentStyle==ContentStyle.SINGLE?areaFacets[0]
			  		:app.newMultiContentFacet(appArea);
			appArea.attachFacet(appFacet);
			if(emptyNow){
				areasNow[0].attachFacet(appFacet);
			  appFacet.setFacets(new SFacet[]{});
			}
			if(areaFacets!=null&&appFacet!=areaFacets[0])appFacet.setFacets(areaFacets);
			if(targeter!=null)((IndexingTarget)targeter.target()).nullInvalidParents(areasNow);
			else if(targeter==null&&(targeter=(AreaTargeter)appArea.newTargeter())==null)
		    throw new IllegalStateException("Null targeter in "+Debug.info(this));
		  targeter.retarget(appArea,Impact.DEFAULT);
			targeter.setNotifiable(app);
			targeter.retargetFacets(Impact.DEFAULT);
			traceEvent(">Updated "+Debug.info(app)+" for areas="+areas.size());
			Debug.memCheck("AreasUpdate.finishUpdate: ");
		}};
			if(false)app.runWatched(op);
			else op.doOperations();
		}
	}
	final static class AppArea extends AreaRoot{
		private final AppSurface app;
		AppArea(AppSurface app,SAreaTarget[]areas,int areaAt){
			super(app.title(),newIndexing(areas));
		  indexing().setIndex(areaAt);
			this.app=app;
		}
		void _setNowChilden(SAreaTarget[]areas,int areaAt){
			setIndexing(newIndexing(areas));
		  indexing().setIndex(areaAt);
		}
		private static SIndexing newIndexing(SAreaTarget[]areas){
			return new SIndexing("AppAreas",areas,0,new Coupler(){
				@Override
				public String[]newIndexableTitles(SIndexing in){
					SAreaTarget[]areas=(SAreaTarget[])in.indexables();
					String[]titles=new String[areas.length];
					for(int i=0;i<titles.length;i++)
						titles[i]=(i+1)+" "+areas[i].title();
					return titles;
				}
			});
		}
		@Override
		protected STarget[]lazyElements(){
			return app.lazyAppAreaElements();
		}
	}
	final static class ViewerContentArea extends ContentArea{
	  String multi="";
		ViewerContentArea(String title,STarget[]viewers,SContenter contenter){
	  	super(title,contenter,viewers);
	  }
		final public String title(){
			if(contenter==null)return super.title();
			return contenter.title().replace("&","")+multi;
		}
	}
	void updateLayout(final List<AppContenter>contents){
		WatchableOperation op=new WatchableOperation("AppAreas.updateLayout"){
		public void doSimpleOperation(){
			traceEvent(">Laying out surface "+Debug.info(app)+" contents="+contents.size());
		  AppContenter first=contents.get(0),activeContent=first;
			if(first instanceof ViewerContenter){
				SFrameTarget frame=activeContentFrame();
				for(AppContenter content:contents)
					if(((ViewerContenter)content).contentFrame()==frame)
						activeContent=content;
			}
		  SContentAreaTargeter activeArea=(SContentAreaTargeter)targeter.areaAt(AREA_ACTIVE),
				useArea=activeContent.useActiveFeatures(activeArea)?activeArea
					:(SContentAreaTargeter)targeter.areaAt(AREA_CONTENT);
		  SContenter useContent=((ContentArea)useArea.target()).contenter;
		  useContent.areaRetargeted(useArea);
		  app.appRetargeted();
		  featuresKey=activeContent.featuresKey(useArea);
			traceEvent(">Getting layout for" +Debug.info(useArea)+" featuresKey="+featuresKey);
		  layout=layouts.get(featuresKey);
		  FeatureHost host=(FeatureHost)app.host();
			if(layout==null)layouts.put(featuresKey,layout=host.newLayout(appFacet,
					((AppContenter)useContent).newContentFeatures(useArea)));
			traceEvent(">Passing layout " +Debug.info(layout)+" of "+layouts.size()
					+" to host " +Debug.info(host));
			host.setLayout(layout);
			host.updateLayout(app);
		  app.notify(new Notice(targeter,Notifying.Impact.DEFAULT));
			traceEvent(">Updated surface for "+Debug.info(app));
		}};
		if(true)op.doOperations();
		else app.runWatched(op);
	}
	SAreaTarget appArea(){
		if(targeter==null)throw new IllegalStateException("No targeter in "+Debug.info(app));
		else return((SAreaTarget)targeter.target());
	}
	SFrameTarget activeContentFrame(){
		STarget target=appArea().indexedTarget();
		return target==emptyArea?app.emptyContent.contentFrame()
				:((ContentArea)target).contenter.contentFrame();
	}
	static ViewerContentArea[]viewerAreas(STarget[]targets){
		List<ViewerContentArea>viewers=new ArrayList();
		for(STarget t:targets)if(t instanceof ContentArea)viewers.add((ViewerContentArea)t);
		return viewers.toArray(new ViewerContentArea[]{});
	}
}
