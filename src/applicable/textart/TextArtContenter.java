package applicable.textart;

import static applicable.textart.TextArtConstants.*;
import static facets.facet.FacetFactory.*;
import facets.core.app.ActionViewerTarget;
import facets.core.app.AreaRoot;
import facets.core.app.Dialogs;
import facets.core.app.FeatureHost;
import facets.core.app.SAreaTarget;
import facets.core.app.SContentAreaTargeter;
import facets.core.app.SurfaceServices;
import facets.core.app.ViewableAction;
import facets.core.app.ViewableFrame;
import facets.core.app.ViewerContenter;
import facets.core.app.ViewerTarget;
import facets.core.app.AppSurface.ContentStyle;
import facets.core.app.FeatureHost.LayoutFeatures;
import facets.core.app.avatar.AvatarPolicies;
import facets.core.superficial.SFacet;
import facets.core.superficial.SFrameTarget;
import facets.core.superficial.SIndexing;
import facets.core.superficial.STarget;
import facets.core.superficial.app.FacetedTarget;
import facets.core.superficial.app.SSelection;
import facets.facet.AreaFacets;
import facets.facet.ViewerAreaMaster;
import facets.facet.app.FacetAppSurface;
import facets.util.app.AppValues;
import facets.util.tree.ValueNode;
import applicable.textart.TextArtFeatures.AdvanceFacets;

/**
Creates and exposes content for spike application. 
 */
public final class TextArtContenter extends ViewerContenter {

	//Package-visible immutable fields
	final Dialogs dialogs;	
	final AdvanceFacets advanceFacets;
	
	//Private immutable fields
	private final AppValues values;
	private final FacetAppSurface app;
	
	//Is a line selected? 
	private boolean noSelection;

	//Records state of grid indexing
	private int thenGrid;
	

	/**
	Unique constructor. 
	@param source passed to superclass
	@param app set immutably
	@param advanceFacets defined in surface to allow sharing between contenters
	in desktop application
	@param values set immutably, <code>null</code> in applet surface
	@param dialogs set immutably, <code>null</code> in applet surface
	 */
	public TextArtContenter(Object source, FacetAppSurface app, 
			AdvanceFacets advanceFacets, AppValues values, Dialogs dialogs, 
			FeatureHost host) {
		
		//Pass source to superclass
		super(source);
		
		//Set immutable references and flag passed
		this.app = app;
		this.values = values;
		this.dialogs = dialogs;
		this.advanceFacets = advanceFacets;
 	}
	
	/**
	Re-implementation of superclass stub. 
	@see ViewerContenter#newContentViewable(Object)
	 */
	protected ViewableFrame newContentViewable(Object source){
		
		//Root of content tree
		ValueNode node = (ValueNode)((ContentSource) source).newContent();
	
		//Viewable that creates the right kind of selection frame
		return new TextArtViewable(node, this,
				true?null:
				app.ff.statefulClipperSource(app.spec.hasSystemAccess()));
	}

	/**
	Re-implements framework method. 
	@see facets.core.app.ViewerContenter#lazyContentAreaElements(facets.core.app.SAreaTarget)
	 */
	@Override
	public STarget[] lazyContentAreaElements(SAreaTarget area) {
		return advanceFacets==null?new STarget[]{}:new STarget[]{
				advanceFacets.toolIndexing
			};
	}

	/**
	Implements abstract framework method. 
	@see facets.core.app.ViewerContenter#newContentViewers(
	facets.core.app.ViewableFrame)
	 */
	@Override
	protected FacetedTarget[] newContentViewers(ViewableFrame viewable) {
		
		//Usually create specialised avatar policies
		TextArtViewable lines = (TextArtViewable) viewable;
		AvatarPolicies policies = new TextArtAvatarPolicies(lines.xyPolicy(true), 
				lines.xyPolicy(false), 
				lines.anglePolicy(), lines.limitWidth, lines.limitHeight, 
				lines.gridSnap, lines.limits) {
			
			@Override
			protected boolean markInactiveSelection() {
				return false;
			}			
		};
		
		//Create indexing of zoomable views
		STarget zoomIndexing = TextArtView.newZoomIndexing(lines.limitWidth, 
				lines.limitHeight, policies);

		//Create subclass with defined actions and trigger groups
		ViewerTarget avatars = new ActionViewerTarget(title()+" Avatars",viewable, 
				zoomIndexing){
			@Override
			protected STarget[] newActionTriggerGroups(ViewableAction[] actions){
				
				return new STarget[]{					
					newActionTriggerGroup("Line|Core", 
							ACTION_GROUPS[ACTIONS_SELECTION_CORE]),
					newActionTriggerGroup("Line|Full", 
							ACTION_GROUPS[ACTIONS_SELECTION_FULL]),					
					newActionTriggerGroup("Edit|Core", 
							ACTION_GROUPS[ACTIONS_EDIT_CORE]),					
					newActionTriggerGroup("Edit|Full", 
							ACTION_GROUPS[ACTIONS_EDIT_FULL]),
					newActionTriggerGroup("Undo/Redo", 
							ACTION_GROUPS[ACTIONS_UNDO_REDO]),
				};
			}
		},
		tree = true?null:lines.newTreeViewer();
		
		//Return as single-member array
		return false?new SAreaTarget[]{
				SAreaTarget.newSingleViewerArea(avatars),
				SAreaTarget.newSingleViewerArea(tree)
			}
			:new ViewerTarget[]{avatars};
	}

	/**
	Implements abstract framework method. 
	see {@link ViewerContenter#attachContentAreaFacets(AreaRoot)}
	 */
	protected void attachContentAreaFacets(AreaRoot area) {
		
		AreaFacets areas=app.ff.areas();
		final boolean singleViewer=area.indexableTargets().length==1;
		if(singleViewer)areas.viewerArea(area,new ViewerAreaMaster(){
			protected String hintString(){
				return(singleViewer?HINT_BARE:"")
					+(app.contentStyle!=ContentStyle.DESKTOP?HINT_PANEL_BORDER:"");
			}
			protected ViewerAreaMaster newChildMaster(SAreaTarget child){
				return child.title().contains("Avatars")?null
						:new ViewerAreaMaster() {
					protected String hintString(){
						return HINT_NONE;
					}
				};
			}
		});
		else areas.attachViewerAreaPanes(area,"",AreaFacets.PANE_SPLIT_VERTICAL);

		if(advanceFacets!=null)
			advanceFacets.attachSidebarTree(
					(SAreaTarget)area.contenterFrame().elements()[VIEWABLE_TREE]);
		
	}

	/**
	Implements interface method. 
	@see facets.core.app.ViewerContenter#alignContentAreas(facets.core.app.SAreaTarget, facets.core.app.SAreaTarget)
	 */
	public void alignContentAreas(final SAreaTarget existing, final SAreaTarget added) {
		
		//Encapsulates alignment code
		new Runnable() {
	
			//Do alignment
			public void run(){
				
				//Get references
				SIndexing 
				indexing = findViewsIndexing(added),
				srcIndexing = findViewsIndexing(existing);				
				TextArtView 
				base = findBaseView(indexing),
				srcBase = findBaseView(srcIndexing);
				
				//Align zoom, set grid state
				indexing.setIndex(srcIndexing.index());		
				base.gridShow.set(srcBase.gridShow.isSet());			
			}
	
			private SIndexing findViewsIndexing(SAreaTarget root){
	
				//Retrieve indexing from root 
				return (SIndexing) ((ViewerTarget) root.activeFaceted()).views;
			}
	
			private TextArtView findBaseView(SIndexing indexing){
				
				//Retrieve base view from indexing
				return (TextArtView) ((SFrameTarget)indexing.indexables()[0]).framed;
			}
			
		}.run();
	}

	/**
	Implements framework interface method. 
	 */
	public LayoutFeatures newContentFeatures(final SContentAreaTargeter area){
		
		//Create and return appropriate layout
		return new TextArtFeatures(app.ff, area, advanceFacets) {
			boolean minimal=advanceFacets==null;
			public SFacet[]header(){
				return minimal?null:newMenuRoots(newLineMenuRoots(),app);
			}
			public SFacet toolbar(){
				return minimal?null:newToolbar();
			}
			public SFacet sidebar(){
				return minimal?null:newSidebar();
			}
			public SFacet status(){
				return minimal?null:newStatus(app);
			}
			public SurfaceServices services(){
				return minimal?null:app.newFullServices(newContextFacets());
			}
		};
	}
	/**
	 Re-implements framework method. 
	 @see facets.core.app.SContenter#areaRetargeted(SContentAreaTargeter)
	 */
	@Override
	public void areaRetargeted(SContentAreaTargeter area) {
		
		//If no selection, disable selection facet, force tool mode etc
		TextArtViewable viewable = (TextArtViewable) area.content().target();
		SSelection selection = viewable.selection();
		noSelection = selection.multiple()[0] == selection.content();
		area.selection().target().setLive(!noSelection);
		if(advanceFacets!=null)advanceFacets.toolIndexing.setLive(!noSelection);
		viewable.angleSnap.setLive(!noSelection);
		viewable.gridSnap.setLive(!noSelection);
		viewable.limits.setLive(!noSelection);
		
		//Grid toggling?
		Object activeView = ((SFrameTarget) area.view().target()).framed;
		if(activeView instanceof TextArtView){
			int nowGrid = viewable.gridSnap.index();
			boolean noGrid = nowGrid == TextArtConstants.GRID_NONE;
			TextArtView textLineView=(TextArtView)activeView;
			if (noGrid) textLineView.gridShow.set(false);
			else if (nowGrid != thenGrid) textLineView.gridShow.set(true);
			thenGrid = nowGrid;
			textLineView.gridShow.setLive(!noGrid);
		}
		
		//Set menu item live states
		app.setLayoutTargetsLive(true);
		
	}

}