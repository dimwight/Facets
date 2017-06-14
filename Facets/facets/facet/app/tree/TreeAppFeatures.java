package facets.facet.app.tree;
import static facets.facet.app.FileAppActions.*;
import static facets.facet.app.tree.TreeTargets.*;
import facets.core.app.FeatureHost;
import facets.core.app.MenuFacets;
import facets.core.app.SurfaceServices;
import facets.core.app.ViewerContenter;
import facets.core.superficial.Notice;
import facets.core.superficial.SFacet;
import facets.core.superficial.STargeter;
import facets.core.superficial.app.AreaTargeter;
import facets.core.superficial.app.SContentAreaTargeter;
import facets.core.superficial.app.ViewerTarget;
import facets.facet.FacetFactory;
import facets.facet.app.FacetAppSpecifier;
import facets.facet.app.FacetAppSurface;
import facets.facet.app.FileAppActions;
import facets.util.TitledList;
/**
{@link FacetFactory} for use with {@link TreeAppContenter}. 
<p>Like {@link TreeAppContenter}, effectively a package-private class 
exemplifying specialisation of {@link FacetFactory} 
to match a {@link FacetAppSpecifier} and {@link ViewerContenter}.  
 */
final public class TreeAppFeatures extends FacetFactory{
	private final FacetAppSurface app;
	private final SContentAreaTargeter area;
	/**
	Called from {@link TreeAppContenter#newContentFeatures(SContentAreaTargeter)}. 
	@param app created in {@link TreeAppSpecifier#newApp(FacetFactory, FeatureHost)}
	@param root as returned by {@link FacetAppSurface#activeContentTargeter()}
	 */
	protected TreeAppFeatures(FacetAppSurface app,SContentAreaTargeter root){
		super(app.ff);
		this.app=app;
		this.area=root;
	}
	/**
	Calls {@link #newAdjustedMenus(FacetAppSurface, SContentAreaTargeter)} to add 
	tree menu. 
	@return available standard menus with interpolated tree menu
	 */
	@Override
	public SFacet[]header(){
		return newAdjustedMenus(app,area);
	}
	/**
	Re-implementation based on {@link TreeAppContenter}. 
	 */
	@Override
	protected STargeter findPaneTargeter(SContentAreaTargeter area){
		return area.elements()[TreeAppContenter.TARGETS_PANE];
	}
	/**
	Adds tree menu.
	@return available standard menus with interpolated menu containing
	the return of {@link TreeAppSpecifier#newTreeMenuItems(FacetFactory, STargeter[], 
	STargeter[]) }
	 */
	@Override
	protected MenuFacets[]adjustMenuRoots(MenuFacets[]menus){
		STargeter tree=area.content();
		final SFacet[]treeItems=((TreeAppSpecifier)app.spec).newTreeMenuItems(this,
				tree.elements(),area.elements()[TreeAppContenter.TARGETS_CONTENT].elements()
			);
		return new MenuFacets[]{
			menus[MENU_APP],
			menus[MENU_EDIT],
			menus[MENU_PANE],
			new MenuFacets(tree,TreeTargets.TITLE_MENU){
				@Override
				public SFacet[]getFacets(){
					return treeItems;
				}
			},
			menus[MENU_WINDOW],
			menus[MENU_HELP],
		};
	}
/**
	Creates edit/save/restore toolbar. 
	 */
	@Override
	public SFacet toolbar(){
		SFacet[]editTools=editTools(area.viewer());
		if(!app.spec.canEditContent())return null;
		TitledList<STargeter>files=!app.spec.canSaveContent()?null
			:new TitledList(Notice.findElement(
					(STargeter)area.notifiable(),TARGETS_FILE).elements());
		return toolGroups(area,HINT_NONE,files==null?editTools
				:join(new SFacet[]{
						triggerButtons(files.titled(TITLE_SAVE),HINT_BARE),
						triggerButtons(files.titled(TITLE_REVERT),HINT_BARE),
					},
					editTools));
	}
	/**
	Creates specialised {@link MenuFacets} to match {@link TreeAppSpecifier}. 
	<p>Edit facets are appended where appropriate to tree expansion sub-menu.  
 */
	@Override
	public SFacet extras(){
		return false?null:appExtras(app);
	}
	@Override
	protected MenuFacets getServicesContextMenuFacets(){
		MenuFacets context=new MenuFacets(area,"Tree facets"){
			SFacet[]editFacets=new EditFacets(area).getFacets();
			public SFacet[]getContextFacets(ViewerTarget viewer,SFacet[]viewerFacets){
				return editFacets.length==0?viewerFacets
						:join(viewerFacets,join(new SFacet[]{BREAK},editFacets));
			}
		};
		return context;
	}
	/**
	Final implementation setting diagnostic status display. 
	 */
	@Override
	public SFacet status(){
		STargeter targeter=area.selection().elements()[0];
		return toolGroups(targeter,HINT_NONE,spacerTall(2),
			textualLabel(targeter,HINT_NONE));
	}
}