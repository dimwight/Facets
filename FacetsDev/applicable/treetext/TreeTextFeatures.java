package applicable.treetext;
import static facets.facet.app.FileAppActions.*;
import facets.core.app.MenuFacets;
import facets.core.app.SContentAreaTargeter;
import facets.core.app.ViewerTarget;
import facets.core.superficial.Notice;
import facets.core.superficial.SFacet;
import facets.core.superficial.STargeter;
import facets.facet.FacetFactory;
import facets.facet.app.FacetAppSurface;
import facets.util.ItemList;
import facets.util.TitledList;
public class TreeTextFeatures extends FacetFactory{
	private final FacetAppSurface app;
	private final SContentAreaTargeter area;
	public TreeTextFeatures(FacetAppSurface app,SContentAreaTargeter root){
		super(app.ff);
		this.app=app;
		this.area=root;
	}
	@Override
	final public SFacet toolbar(){
		ItemList<SFacet>facets=new ItemList(SFacet.class);
		if(app.spec.canEditContent()){
			SFacet[]editTools=editTools(area.viewer());
			TitledList<STargeter>files=!app.spec.canSaveContent()?null
					:new TitledList(Notice.findElement(
							(STargeter)area.notifiable(),TARGETS_FILE).elements());
			if(files!=null)facets.addItems(
				triggerButtons(files.titled(TITLE_SAVE),HINT_BARE),
				triggerButtons(files.titled(TITLE_REVERT),HINT_BARE),
				spacerWide(5));
			facets.addItems(editTools);
		}
		return toolGroups(area,HINT_PANEL_MIDDLE,adjustToolbarItems(facets));
	}
	protected SFacet[]adjustToolbarItems(ItemList<SFacet>facets){
		return facets.items();
	}
	@Override
	public SFacet[]header(){
		return newAdjustedMenus(app,area);
	}
	@Override
	protected STargeter findPaneTargeter(SContentAreaTargeter area){
		return area.elements()[TreeTextContenter.TARGETS_PANE];
	}
	@Override
	protected MenuFacets[]adjustMenuRoots(MenuFacets[]menus){
		return new MenuFacets[]{
			menus[MENU_APP],
			menus[MENU_EDIT],
			menus[MENU_PANE],
			menus[MENU_WINDOW],
			menus[MENU_HELP],
		};
	}
	@Override
	public SFacet extras(){
		return false?null:appExtras(app);
	}
	@Override
	protected MenuFacets getServicesContextMenuFacets(){
		MenuFacets context=new MenuFacets(area,"Area facets!"){
			SFacet[]editFacets=new EditFacets(area).getFacets();
			public SFacet[]getContextFacets(ViewerTarget viewer,SFacet[]viewerFacets){
				return editFacets.length==0?viewerFacets
						:join(viewerFacets,join(new SFacet[]{BREAK},editFacets));
			}
		};
		return context;
	}
}
