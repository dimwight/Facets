package pub.view;
import static applicable.ItemHistory.*;
import static facets.core.superficial.SIndexing.*;
import static java.awt.event.KeyEvent.*;
import static pub.PubValues.*;
import static pub.view.ListingContenter.*;
import static pub.view.ListingViewable.*;
import static pub.view.ListingViewableCore.*;
import static pub.view.PubsView.*;
import facets.core.app.FeatureHost.LayoutFeatures;
import facets.core.app.MenuFacets;
import facets.core.app.SurfaceServices;
import facets.core.app.TreeView;
import facets.core.superficial.Notice;
import facets.core.superficial.Notifying.Impact;
import facets.core.superficial.SFacet;
import facets.core.superficial.SIndexing;
import facets.core.superficial.STargeter;
import facets.core.superficial.app.AreaTargeter;
import facets.core.superficial.app.SContentAreaTargeter;
import facets.core.superficial.app.ViewerTarget;
import facets.facet.AppFacetsBuilder;
import facets.facet.AreaFacets;
import facets.facet.AreaFacets.PaneDialogStyle;
import facets.facet.FacetFactory;
import facets.facet.WindowFacetBuilder;
import facets.facet.app.FacetAppActions;
import facets.facet.app.FacetAppSpecifier;
import facets.facet.app.FacetAppSurface;
import facets.util.Debug;
import pub.PubValues;
import applicable.field.FieldSet.SidebarChooser;
class ListingFeatures extends FacetFactory{
	private final FacetAppSurface app;
	private final SContentAreaTargeter area;
	private final SidebarChooser chooser;
	private final STargeter content[],selection[],actions,open,setChooser;
	private final SFacet actionMenu,move;
	private final String tooltipsHint;
	private final boolean ribbon,forSlave;
	private final SimpleServices blocker;
	ListingFeatures(ListingContenter contenter){
		super(contenter.app.ff);
		this.app=contenter.app;
		this.chooser=contenter.chooser;
		forSlave=app.spec.forSlave();
		ribbon=((FacetAppSpecifier)app.spec).headerIsRibbon();
		tooltipsHint=searchView?HINT_TOOLTIPS:HINT_NONE;
		area=app.activeContentTargeter();
		if(!contenter.title().replace("&","").endsWith(area.title()))
			throw new IllegalStateException(
					"Mismatched area="+area.title()+" contenter="+contenter.title());
		content=area.content().elements();
		selection=area.selection().elements();
		actions=content[TARGETS_ACTION];
		open=content[TARGETS_OPEN];
		setChooser=area.view().elements()[TARGET_CHOOSER_SIDEBAR];
		SFacet export=triggerMenuItems(content[TARGETS_EXPORT],tooltipsHint);
		actionMenu=menuRoot(area,"A&ctions",searchView?new SFacet[]{
				triggerMenuItems(actions,tooltipsHint),
				export,
			}:new SFacet[]{
					triggerMenuItems(actions,tooltipsHint),
					export,
					triggerMenu(content[TARGETS_RECORD],tooltipsHint),
				});
		move=indexingIteratorButtons(content[TARGETS_MOVE],HINT_NONE);
		blocker=contenter.fieldsSpec==FieldsSpec.VIEW_WIP?null:new SimpleServices(){
			public boolean isBlocking(){
				return true;
			}
			public void handleBlockedKey(int code){
				SIndexing across=app.surfaceTargeter().areaTarget().indexing();
				switch(code){
				case VK_RIGHT:iterate(across,true);break;
				case VK_LEFT:iterate(across,false);break;
				case VK_DOWN:iterateIndexingButtons(move,true);break;
				case VK_UP:iterateIndexingButtons(move,false);break;
				default:return;
				}
				app.notify(new Notice(area,Impact.SELECTION));
			}
		};
	}
	private SFacet newSearchBar(){
		STargeter search[]=content[TARGETS_SEARCH].elements();
		return toolGroups(area,HINT_PANEL_MIDDLE,join(new SFacet[]{
				spacerWide(3),
				textualField(search[TARGET_SEARCH_BOX],20,HINT_NONE),
				spacerWide(3),
				triggerCodeButton(search[TARGET_SEARCH_GO]),
				spacerWide(5),
				indexingRadioButtons(search[TARGET_SEARCH_MATCH],HINT_NONE),
				togglingCheckboxes(search[TARGET_SEARCH_EXACT],HINT_BARE),
				togglingCheckboxes(content[TARGET_REFS],HINT_BARE)},
				searchView?new SFacet[]{actionMenu}
				:ribbon?new SFacet[]{}
				:new SFacet[]{spacerWide(10),move}
					));
	}
	@Override
	public SurfaceServices services(){
		if(searchView)return null;
		else if(false&&PubsView.dev)return blocker;
		boolean noSelection=selection.length<2;
		final SFacet actionTriggers=triggerMenuItems(actions,tooltipsHint),
			openMenu=indexingRadioButtonMenu(open,HINT_INDEXING_SELECT),
			priority=noSelection?null
				:indexingRadioButtonMenu(selection[TARGET_SELECTION_PRIORITY],HINT_NONE),
			comments=noSelection?null
				:triggerMenuItems(selection[TARGET_SELECTION_COMMENTS],HINT_NONE);
		final MenuFacets context=new MenuFacets(area,"ContextMenu"){
			@Override
			public SFacet[]getContextFacets(ViewerTarget viewer,SFacet[]viewerFacets){
				AreaTargeter active=area.areaAt(AreaTargeter.AREA_ACTIVE);
				STargeter resetPane=active.elements()[0].elements()[1];
				return viewer.view()instanceof TreeView?viewerFacets
					:FieldsSpec.VIEW_WIP.isTitle(viewer)?new SFacet[]{
						actionTriggers,openMenu,BREAK,priority,comments
					}
				:active.targetType()==RecordContenter.class?
						new SFacet[]{actionTriggers,openMenu,BREAK,
							triggerMenuItems(resetPane,HINT_TITLE1+HINT_NONE)}
					:new SFacet[]{actionTriggers,openMenu};
			}
		};
		return new SimpleServices(){
			@Override
			public MenuFacets getContextMenuFacets(){
				return context;
			}
		};
	}
	@Override
		public SFacet[]header(){
		if(PubValues.searchView)return null;
		else if(ribbon)return ribbon();
		SFacet appMenu=menuRoot(new AppFacetsBuilder(this,area).newMenuFacets());
		if(forSlave)return new SFacet[]{appMenu};
		STargeter panes[]=area.elements();
		return new SFacet[]{
			appMenu,
			false&&PubValues.userView?null:menuRoot(windowMenuFacets(area,false)),
			resultsMenu(),
			menuRoot(area,"Exp&ort",new SFacet[]{
					triggerMenuItems(content[TARGETS_EXPORT],tooltipsHint+HINT_TITLE1),
					triggerMenu(content[TARGETS_RECORD],tooltipsHint+HINT_TITLE1),
			}),
			menuRoot(area,"View",new SFacet[]{
				panes.length==0?null
					:togglingCheckboxMenuItems(panes[AreaFacets.PANE_SHOW].elements(
							)[PREVIEW_AT],HINT_NONE),
				togglingCheckboxMenuItems(setChooser,HINT_NONE),
				togglingCheckboxMenuItems((STargeter)Notice.findElement(
						app.surfaceTargeter(),FacetAppActions.TARGETS_LAYOUT,0),HINT_NONE),
//						triggerMenuItems(selection[TARGET_SELECTION_COMMENTS],HINT_NONE)
				}),			
		};
	}
	private SFacet[]ribbon(){
		AppFacetsBuilder app=new AppFacetsBuilder(this,area);
		WindowFacetBuilder window=new WindowFacetBuilder(this,area);
		return new SFacet[]{
			ribbonTab(area,"Home",app.ribbonNew(),window.ribbonList(),
					window.ribbonNew(),spacerWide(5),app.ribbonCore()),
			ribbonTab(area,"Search",newSearchBar(),resultsMenu())
		};
	}
	private SFacet resultsMenu(){
		STargeter results[]=content[TARGETS_RESULTS].elements(),
				resultsItems=results[TARGET_ITEMS],resultsRename=results[TARGET_RENAME];
		SFacet menu=menuRoot(resultsItems,resultsItems.title(),new SFacet[]{
			triggerMenuItems(actions,tooltipsHint+HINT_TITLE1),
			BREAK,
			indexingRadioButtonMenuItems(resultsItems,HINT_NO_MNEMONICS+HINT_INDEXING_SELECT),
			BREAK,
			triggerMenuItems(resultsRename,HINT_NONE),
			indexingRadioButtonMenu(content[TARGETS_CLONE],HINT_INDEXING_SELECT),
		});
		return ribbon?rowPanel(resultsItems,menu):menu;
	}
	@Override
	public SFacet toolbar(){
		return (false&&searchView)||ribbon||forSlave?null:newSearchBar();
	}
	@Override
	public SFacet sidebar(){
		return searchView||forSlave?null:sidebarHost(setChooser,chooser);
	}
	@Override
	public SFacet extras(){
		return true?null:appExtras(app);
	}
	static LayoutFeatures newForFields(final SContentAreaTargeter area,
			ListingContenter contenter){
		final FacetAppSurface app=contenter.app;
		switch(contenter.fieldsSpec){
		case LINKS:return new FacetFactory(app.ff){
			STargeter open=area.content().elements()[TARGETS_OPEN];
			@Override
			public SFacet[]header(){
				final SContentAreaTargeter top=area.contentAncestor(null);
				SFacet appMenu=menuRoot(new AppFacetsBuilder(this,top).newMenuFacets()),
						windowMenu=menuRoot(windowMenuFacets(top,false));
				return top.targetType()==RecordContenter.class?new SFacet[]{
					appMenu,windowMenu,menuRoot(areas().new PaneFacets("Panes",top))}
				:new SFacet[]{appMenu,windowMenu};
			}
			@Override
			public SFacet toolbar(){
				return toolGroups(area,HINT_NONE,menuRoot(newOpenFacets(false)));
			}
			@Override
			protected MenuFacets getServicesContextMenuFacets(){
				return newOpenFacets(true);
			}
			private MenuFacets newOpenFacets(boolean context){
				final SFacet[]facets=new SFacet[]{
						context?indexingRadioButtonMenu(open,HINT_INDEXING_SELECT)
						:indexingRadioButtonMenuItems(open,HINT_INDEXING_SELECT)
				};
				return new MenuFacets(open,"Open &Link"){
					@Override
					public SFacet[]getFacets(){
						return facets;
					}
				};
			}
		};
		default:
			return new ListingFeatures(contenter);
		}
	}
}