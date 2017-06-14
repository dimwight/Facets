package applicable.eval.app;
import static facets.facet.app.FileAppActions.*;
import static facets.facet.app.tree.TreeTargets.*;
import facets.core.app.MenuFacets;
import facets.core.app.SurfaceServices;
import facets.core.superficial.Notice;
import facets.core.superficial.SFacet;
import facets.core.superficial.STargeter;
import facets.core.superficial.app.SContentAreaTargeter;
import facets.core.superficial.app.ViewerTarget;
import facets.facet.AppFacetsBuilder;
import facets.facet.FacetFactory;
import facets.facet.app.FacetAppSurface;
import facets.util.ItemList;
import facets.util.TitledList;
class EvalFeatures extends FacetFactory{
	private final FacetAppSurface app;
	private final SContentAreaTargeter area;
	private final STargeter content,viewer;
	private final TitledList<STargeter> files;
	EvalFeatures(FacetAppSurface app,SContentAreaTargeter area){
		super(app.ff);
		this.app=app;
		this.area=area;
		content=area.content();
		viewer=area.viewer();
		files=!app.spec.canSaveContent()?null
				:new TitledList(Notice.findElement((STargeter)area.notifiable(),TARGETS_FILE).elements());
	}
	@Override
	public SFacet[]header(){
		return new SFacet[]{
			menuRoot(new AppFacetsBuilder(this,area).newMenuFacets()),
			files==null?null:menuRoot(new EditFacets(area)),
			files==null?null:menuRoot(new MenuFacets(content,"Selection"){
				@Override
				public SFacet[]getFacets(){
					return newSelectionFacets();
				}
			}),
			menuRoot(new MenuFacets(content,"View"){
				@Override
				public SFacet[]getFacets(){
					return new SFacet[]{
						triggerMenuItems(findLaunch(),HINT_NONE),
					};
				}
			}),
			menuRoot(windowMenuFacets(area,false)),
			menuRoot(helpMenuFacets(area))
		};
	}
	@Override
	public SFacet toolbar(){
		ItemList<SFacet>facets=new ItemList(SFacet.class);
		if(files!=null){
			facets.addItems(
					triggerButtons(files.titled(TITLE_SAVE),HINT_BARE),
					spacerWide(5),
					triggerButtons(files.titled(TITLE_REVERT),HINT_BARE));
			facets.addItems(editTools(area.viewer()));
			facets.addItem(spacerWide(5));
		}
		facets.addItem(triggerButtons(findLaunch(),HINT_BARE));
		return toolGroups(area,HINT_NONE,facets.items());
	}
	private STargeter findLaunch(){
		return area.elements()[0];
	}
	SFacet[]newSelectionFacets(){
		STargeter[]triggers=content.elements();
		return new SFacet[]{
			triggerMenuItems(triggers[TARGET_SEARCH],HINT_NONE),
			triggerMenuItems(triggers[TARGET_TYPE],HINT_NONE),
			triggerMenu(triggers[TARGET_ENCODE],HINT_NONE),
			triggerMenuItems(triggers[TARGET_TABLE],HINT_NONE),
		};
	}
	@Override
	public SFacet status(){
		STargeter targeter=area.selection().elements()[0];
		return true?null:toolGroups(targeter,HINT_NONE, new SFacet[]{spacerTall(15),textualLabel(
				targeter,HINT_NONE)});
	}
	@Override
	public SurfaceServices services(){
		MenuFacets context=new MenuFacets(area,"Tree facets"){
			SFacet[]editFacets=new EditFacets(area).getFacets();
			public SFacet[]getContextFacets(ViewerTarget viewer, SFacet[]viewerFacets){
				return FacetFactory.join(
						viewerFacets,FacetFactory.join(new SFacet[]{BREAK},editFacets));
			}
		};
		return app.newFullServices(context);
	}
}