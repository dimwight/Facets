package demo.applet;

import static applicable.textart.TextArtConstants.*;
import static facets.facet.FacetFactory.*;
import facets.core.app.ActionViewerTarget;
import facets.core.app.AreaRoot;
import facets.core.app.MountFacet;
import facets.core.app.ViewerContenter;
import facets.core.app.FeatureHost.LayoutFeatures;
import facets.core.app.avatar.AvatarPolicies;
import facets.core.app.avatar.PlaneViewWorks;
import facets.core.superficial.FacetedTarget;
import facets.core.superficial.SFacet;
import facets.core.superficial.SFrameTarget;
import facets.core.superficial.SIndexing;
import facets.core.superficial.STarget;
import facets.core.superficial.STargeter;
import facets.core.superficial.app.SAreaTarget;
import facets.core.superficial.app.SContentAreaTargeter;
import facets.core.superficial.app.SSelection;
import facets.core.superficial.app.SView;
import facets.core.superficial.app.ViewableAction;
import facets.core.superficial.app.ViewableFrame;
import facets.core.superficial.app.ViewerTarget;
import facets.facet.FacetFactory;
import facets.facet.ViewerAreaMaster;
import facets.facet.FacetFactory.AppletFeatures;
import facets.util.geom.Vector;
import facets.util.tree.ValueNode;
import applicable.textart.TextArt;
import applicable.textart.TextArtAvatarPolicies;
import applicable.textart.TextArtContenter;

/**
Creates and exposes content for spike applet. 
<p>This class is a simplified version of {@link TextArtContenter} as used
in the spike application. Methods are final apart from those to
be overridden in the subclass {@link ViewsContenter}. 
 */
public abstract class SimpleLineContenter extends ViewerContenter {
	
	//Immutable fields
	protected final FacetFactory ff;
	private final MountFacet toolSwitchMount;
	private final SIndexing toolSwitchIndexing;
	
	/**
	Unique constructor. 
	@param ff as created in host surface
	 */
	public SimpleLineContenter(FacetFactory ff) {
		
		//Pass source to superclass
		super(TextArt.LINES_SOURCE.newContent());
		
		//Set immutable reference and flag
		this.ff = ff;
		
		//Create tools switcher and plumb in
		toolSwitchMount = ff.switchMount("Tools");
		toolSwitchIndexing = FacetFactory.switchMountIndexing(toolSwitchMount, 
					new SIndexing.Coupler() {
	
				@Override
				public String[] newIndexableTitles(SIndexing s) {
					return TOOL_TITLES;
				}
			}
		);
	}
	
	final protected AreaRoot newContentViewableArea(Object content, boolean faceted){
	
		//Viewable that creates the right kind of selection frame
		ViewableFrame viewable = new SimpleLineViewable((ValueNode) content, 
				toolSwitchIndexing);
		
		//Array containing framed view
		STarget[] viewTargets = newLineViews((SimpleLineViewable)viewable);

		//Create one or more viewers
		FacetedTarget[] viewers = newLineViewers(viewable,viewTargets);
		
		//Return suitable array to meet method contract
		AreaRoot area = new AreaRoot("Lines",viewers) {
			public STargeter newTargeter(){
				return new SContentAreaTargeter(getClass());
			}
			protected STarget[] lazyElements(){
				return new STarget[]{
						toolSwitchIndexing
					};
			}
		};
		
		//Attach single viewer facet
		attachLineAreaFacets(area);
		
		return area;
	}

	STarget[] newLineViews(SimpleLineViewable lines){
		
		//Create specialised avatar policies
		AvatarPolicies policies = new TextArtAvatarPolicies(
				lines.xyPolicy(true), lines.xyPolicy(false), lines.anglePolicy());
		
		final double 
			showWidth = lines.limitWidth, 
			showHeight = lines.limitHeight;
		
		//Create avatar view using policies
		SView view = new PlaneViewWorks("Actors", showWidth, showHeight, 
				new Vector(0,0),policies){
	
			@Override
			public boolean isLive() {
				
				//View is editabled
				return true;
			}

			@Override
			public boolean allowMultipleSelection() {		
				
				//To match simple viewable
				return false;
			}
		};
		return new STarget[]{					
				new SFrameTarget(view),
		};
	}

	FacetedTarget[]newLineViewers(ViewableFrame viewable,
			STarget[] viewTargets){
		return new ViewerTarget[]{newIteratingViewerTarget(viewable, viewTargets[0])};
	}

	void attachLineAreaFacets(SAreaTarget area){
		ff.areas().viewerArea(area, new ViewerAreaMaster() {

			protected String hintString(){
				return HINT_BARE + HINT_PANEL_BORDER + HINT_NO_FLASH;
			}
			
		});
	}

	/**
	 Re-implements framework method. 
	 @see facets.core.superficial.app.SContenter#areaRetargeted(
	 facets.core.superficial.app.SContentAreaTargeter)
	 */
	@Override
	final public void areaRetargeted(SContentAreaTargeter area) {
		
		//Look at selection and set flag
		SimpleLineViewable viewable = (SimpleLineViewable) area.content().target();
		SSelection selection=viewable.selection();
		boolean noSelection = selection.multiple()[0] == selection.content();
		
		//Set live states of selection and indexing
		area.selection().target().setLive(!noSelection);
		toolSwitchIndexing.setLive(!noSelection);
	}

	/**
	Creates a concrete viewer frame with iterating actions. 
	@param viewable passed to frame constructor
	@param viewFrame passed to frame constructor
	 */
	final ActionViewerTarget newIteratingViewerTarget(
			ViewableFrame viewable, STarget viewFrame) {
		
		//Define, construct, return frame
		return new ActionViewerTarget(viewFrame.title(),viewable, viewFrame){
				protected STarget[]newActionTriggerGroups(ViewableAction[]actions) {
		
					//Only needed to set buttons label!
					return new STarget[]{
							newActionTriggerGroup("Line",actions)
					};
				}			
		};
	}

	/**
	Implements interface method assuming use in applet. 
	@see facets.core.app.AppContenter#newContentFeatures(SContentAreaTargeter)
	 */
	public LayoutFeatures newContentFeatures(SContentAreaTargeter area) {
		
		//Create custom builder, get layout roots
		SimpleLineFacets lineFacets = new SimpleLineFacets(ff, area, toolSwitchMount);		
		SFacet 
		  menus[] = lineFacets.newMenuRoots(),
		  tools = lineFacets.newToolsPanel();
		
		//Create and return appropriate layout
		return new AppletFeatures(tools, menus, true);
	}

}