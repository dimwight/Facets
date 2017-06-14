package demo.hello;

import facets.core.app.AreaRoot;
import facets.core.app.FeatureHost;
import facets.core.app.MenuFacets;
import facets.core.app.MountFacet;
import facets.core.superficial.SFacet;
import facets.core.superficial.SFrameTarget;
import facets.core.superficial.SIndexing;
import facets.core.superficial.STarget;
import facets.core.superficial.STargeter;
import facets.core.superficial.app.IndexingFrame;
import facets.core.superficial.app.SAreaTarget;
import facets.core.superficial.app.SContentAreaTargeter;
import facets.core.superficial.app.SContenter;
import facets.core.superficial.app.SHost;
import facets.core.superficial.app.SSurface;
import facets.core.superficial.app.SHost.FacetLayout;
import facets.facet.FacetFactory;
import facets.facet.FacetFactory.AppletFeatures;
import facets.util.Debug;
import demo.DemoApplet;
import demo.DemoSurface;

/**
Creates a {@link SSurface} with itself as 
{@link SContenter} for array content wrapped in an 
{@link IndexingFrame}. 
 */
abstract class IndexingApplet extends DemoApplet implements DemoSurface.Contenter{
	
	//Frames content passed to constructor
	private final SFrameTarget contentFrame;
	
	//Will be passed from superclass
	private FacetFactory ff;

	/**
	Constructor creates {@link SFrameTarget} wrapper for content. 
	 */
	protected IndexingApplet(Object... content){			
		
		//Create indexing for content with suitable title
		SIndexing indexing = SIndexing.newDefault("Content", content);
		
		//Define and create content frame
		contentFrame = new IndexingFrame(title(), content, indexing) {
			
			@Override
			protected SFrameTarget newIndexedFrame(Object indexed) {	
				
				//Delegate to enclosing class
				return IndexingApplet.this.newSelectionFrame(indexed);
			}
		};	
	}

	/**
	Return a {@link SFrameTarget} whose {@link STarget} 
	<code>elements</code> represent the selection passed. 
	<p>Called by the private subclass of {@link IndexingFrame} 
	created in the constructor. 
	@param selection the currently indexed member of the array
	framed by {@link #contentFrame}
	 */
	protected abstract SFrameTarget newSelectionFrame(Object selection);

	/** 
	Creates a private {@link SSurface} with the applet as 
	{@link SContenter}.
	<p>Implements abstract method called by superclass during 
	{@link #init()}. 
	@see DemoApplet#newSurface(FacetFactory,FeatureHost, boolean)
	 */
	@Override
	final protected SSurface newSurface(FacetFactory ff, FeatureHost host, 
			boolean inBrowser) {
		
		//Set facet builder reference
		this.ff = ff;
				
		//Define, create and return surface
		return new DemoSurface(title(), ff, host) {		
			
			//Implement abstract method
			@Override
			protected Contenter newContenter(FacetFactory ff) {
				
				//Return the enclosing applet
				return IndexingApplet.this;
			}
		};	
	}

	/**
	Creates the initial {@link STarget} tree. 
	@see SContenter#newContentArea(boolean)
	 */
	final public AreaRoot newContentArea(boolean faceted) {
		
		//Create area
		AreaRoot area = new AreaRoot(title(), contentFrame) {
			
			//Standard reimplementation
			@Override
			public STargeter newTargeter() {
				return new SContentAreaTargeter(getClass());
			}
		};
		
		//Attach mount facet and return
		ff.areas().mount(area,true);		
		return area;
	}

	/**
	Creates {@link SFacet}s attached to the {@link STargeter} tree. 
	@see demo.DemoSurface.Contenter#newLayout(SHost,SContentAreaTargeter)
	 */
	final public FacetLayout newLayout(SHost host, SContentAreaTargeter area) {

		//Get references
		STargeter 
		selection = area.selection(), 
		indexing = ((IndexingFrame.FrameTargeter) area.content()).indexing();
		
		SFacet areaFacet=area.areaTarget().attachedFacet(),
			panel = newPanel(ff, indexing, selection);
		if (panel == null) throw new IllegalStateException(
				"No panel in " + Debug.info(this));
		((MountFacet) areaFacet).setFacets(panel);
		
		//Get any menus
		SFacet[] menus = newMenus(ff, indexing, selection);
		
		//Create and return appropriate layout
		return ((FeatureHost) host).newLayout(areaFacet, 
				new AppletFeatures(null, menus, false));
	}

	/**
	Return panel with facet attached to the targeter trees passed. 
	@param ff passed from superclass
	@param indexing retargeted on the {@link SIndexing} setting the 
	selection 
	@param selection retargeted on the frame returned by 
	{@link #newSelectionFrame(Object)} 
	 */
	protected abstract SFacet newPanel(FacetFactory ff, STargeter indexing, 
			STargeter selection);

	/**
	Return panel with facet attached to the targeter trees passed. 
	<p>Parameters are as for {@link #newPanel(FacetFactory,STargeter,
	STargeter)} 
	 */
	protected abstract SFacet[] newMenus(FacetFactory ff, STargeter indexing, 
			STargeter selection);
	
	/**
	Implements interface method.
	 */
	final public String title() {
		return getClass().getSimpleName();
	}

	/**
	Remaining methods are empty or invalid to comply with interface. 
	 */
	final public SFrameTarget contentFrame() {
		throw new RuntimeException("Not implemented in "+this);
	}
	final public void areaRetargeted(SContentAreaTargeter area) {}

	final public MenuFacets getContextFacets() {
		throw new RuntimeException("Not implemented in "+this);
	}

	@Override
	public Class targetType(){
		return getClass();
	}

	public STarget[]lazyContentAreaElements(SAreaTarget area){
		return new STarget[]{};
	}
}
