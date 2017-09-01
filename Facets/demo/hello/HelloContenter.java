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
import facets.core.superficial.app.SelectingFrame;
import facets.core.superficial.app.SHost.FacetLayout;
import facets.facet.FacetFactory;
import facets.facet.FacetFactory.AppletFeatures;
import facets.util.Debug;
import facets.util.Tracer;
import demo.DemoSurface;

/**
Implements {@link demo.DemoSurface.Contenter} with convenience callbacks.
<p>Minimal subclasses may be implemented using  
 <i>newBasicXXX</i> or <i>newContentXXX</i> methods.  
 */
public abstract class HelloContenter extends Tracer implements DemoSurface.Contenter{
	
	public interface DirectHost extends SHost{
		FacetLayout newLayout(SFacet rootFacet);
	}

	//Flag for layout methods
	private final boolean noContent;
	
	//Immutable reference		
	private final SFrameTarget contentFrame;
	
	//Set by factory method after instantiation
	protected FacetFactory ff;

	/**
	Factory method creating a {@link DemoSurface} exposing a concrete {@link HelloContenter}. 
	@param type the <i>[Type]</i> portion of a <i>Hello[Type]</i> subclass of 
	{@link HelloContenter} to be constructed by the surface  
	 @param ff will be passed to the {@link DemoSurface} and thence to the {@link HelloContenter}
	 @param host will be passed to the {@link DemoSurface}
	 @param contenterClass defines subtype of {@link HelloContenter} to construct
	@return a surface which may be initialised by calling {@link SSurface#buildRetargeted()}
	 */
	public static SSurface newSurface(String type,FacetFactory ff,SHost host, 
			Class contenterClass){
		String name="Hello"+(type==null||type.equals("null")?"Label":type);
		try{
			final HelloContenter contenter=(HelloContenter)Class.forName(
					contenterClass.getCanonicalName().replace(
							contenterClass.getSimpleName(),name)).newInstance();
			return new DemoSurface(name,ff,host){
				protected Contenter newContenter(FacetFactory ff){
					contenter.ff=ff;
					return contenter;
				}
			};
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}

	/**
	Core constructor. 
	<p>Creates a {@link SFrameTarget} content frame as 
	<ul><li>if <code>content</code> is <code>null</code>, an empty dummy 
	<li>otherwise, an {@link IndexingFrame} constructed from <code>content</code>
	and a suitable {@link SIndexing}, delegating creation of selection frames 
	to {@link #newSelectionFrame(Object)}
</ul>  
	@param content if non-<code>null</code> will be wrapped in an
	{@link IndexingFrame}
	 */
	protected HelloContenter(Object[]content){		
	
		//Get title from framework method
		String title = title();
		
		//Check for content (and set flag)
		if ((noContent = content == null)) 
			
			//If none, create dummy
			contentFrame = new SFrameTarget(title, "No Content!"); 
		
		else {			
			if(false)throw new RuntimeException("Not implemented in "+Debug.info(this));
			//Create indexing for content with suitable title
			SIndexing indexing = SIndexing.newDefault("Content", content);
			
			//Define and create content frame
			contentFrame = new IndexingFrame(title, content, indexing) {
				
				@Override
				protected SFrameTarget newIndexedFrame(Object indexed) {	
					
					//Delegate to enclosing class
					return newSelectionFrame(indexed);
				}
			};	
		}	
	}

	/**
	Convenience constructor. 
	<p>Passes <code>null</code> content to core constructor. 
	 */
	protected HelloContenter(){
		this(null);
	}

	/**
	Implements interface method. 
	<p>Creates a root {@link SAreaTarget} with 
	<ul><li>the content frame as its single child
	<li>creation of content root elements delegated to {@link #newBasicTargets()}
</ul>  
	@see SContenter#newContentArea(boolean)
	 */
	public SAreaTarget newContentArea(boolean faceted) {
		
		//Create root
		AreaRoot root = new SContentAreaTargeter.ContentArea(title(),this,
				new STarget[]{contentFrame}) {

			//Standard reimplementation
			@Override
			public STargeter newTargeter() {
				return new SContentAreaTargeter(getClass());
			}
		};
		return root;
	}
	//Create in concrete subclass
	final public STarget[]lazyContentAreaElements(SAreaTarget area){
		return newBasicTargets();
	}

	/**
	Return a {@link SFrameTarget} whose {@link STarget} <code>elements</code> 
	represent the selection passed. 
	<p>Called by any {@link IndexingFrame} created  
	in the constructor; default is invalid stub. 
	@param selection the currently indexed member of the array 
	framed by {@link #contentFrame}
	 */
	protected SFrameTarget newSelectionFrame(Object selection) {
		
		//Complain if called when not reimplemented
		throw new RuntimeException("Not implemented in "+this);
	}

	/**
	Return non-content {@link STarget}s to be exposed by the surface. 
	<p>For use by the {@link SAreaTarget} created in 
	{@link #newContentArea(boolean)}; default returns empty array. 
	 */
	protected STarget[] newBasicTargets() {
		return new STarget[]{};
	}

	/**
	Implements interface method, delegating as appropriate. 
	@see demo.DemoSurface.Contenter#newLayout(SHost,SContentAreaTargeter)
	 */
	public FacetLayout newLayout(SHost host, SContentAreaTargeter area) {
		
		SFacet rootFacet;
		
		//No factory available?
		if(ff==null){

			//Create and attach direct facets 
			rootFacet=newDirectPanel(area.elements());
			((SAreaTarget)area.target()).attachFacet(rootFacet);
			return ((DirectHost) host).newLayout(rootFacet);
		}
		
		//Otherwise attach factory mount
		else rootFacet = ff.areas().mount(area.areaTarget(),true);
		
		//Get references
		STargeter 
		basic[] = area.elements(),
		indexing = noContent? null 
				: ((IndexingFrame.FrameTargeter)area.content()).indexing(),
		selection = noContent? null 
				: area.selection();

		//Create panel facet using appropriate method, check return
		SFacet[] facets = noContent ? newBasicPanelFacets(ff, basic)
			: newContentPanelFacets(ff, basic, indexing, selection);
		if(facets == null)throw new IllegalStateException(
				"No panel facet in "+Debug.info(this));

		//Create and attach panel
		SFacet panel = ff==null? null: ff.rowPanel(area, facets);
		((MountFacet) rootFacet).setFacets(panel==null?facets
				:new SFacet[]{panel});
		
		//Create menu facet using appropriate method
		facets = noContent ? newBasicMenuFacets(ff, basic)
			: newContentMenuFacets(ff, basic, indexing, selection);
				
		//If created, wrap in single menu
		SFacet menus[] = facets == null ? null 
				: new SFacet[]{ff.menuRoot(area, "Menu", facets)};
		
		return ((FeatureHost) host).newLayout(rootFacet,
				new AppletFeatures( null, menus, false));
	}

	protected SFacet newDirectPanel(final STargeter[] targeters){
		
		//Complain if called when not reimplemented
		throw new RuntimeException("Not implemented in "+this);
	}

	/**
	Return facet attached to the targeters passed, for layout in panel. 
	<p>Default returns (invalid) <code>null</code>.
	@param ff passed from superclass
	@param targeters are retargeted on the targets returned by 
	{@link #newBasicTargets()} 
	 */
	protected SFacet[] newBasicPanelFacets(FacetFactory ff, 
			STargeter[] targeters) {
		
		//Will trigger exception
		return null;
	}

	/**
	Return facet attached to the targeters passed, for inclusion in menu. 
	<p>Default returns <code>null</code>. 
	@param ff passed from superclass
	@param targeters are retargeted on the targets returned by 
	{@link #newBasicTargets()} 
	 */
	protected SFacet[] newBasicMenuFacets(FacetFactory ff, 
			STargeter[] targeters) {
		
		//May not construct menu bar
		return null;
	}

	/**
	Return facet attached to the targeters passed, for layout in panel. 
	<p>Default returns (invalid) <code>null</code>.
	@param ff passed from superclass
	@param basic retargeted on the targets returned by
	{@link #newBasicTargets()}
	@param indexing retargeted on an {@link SIndexing} setting the 
	selection 
	@param selection retargeted on the frame returned by 
	{@link #newSelectionFrame(Object)} 
	 */
	protected SFacet[] newContentPanelFacets(FacetFactory ff, 
			STargeter[] basic, STargeter indexing, STargeter selection) {
		
		//Will trigger exception
		return null;
	}

	/**
	Return facet attached to the targeters passed, for inclusion in menu. 
	<p>Default returns <code>null</code>. 
	<p>Parameters are as for {@link #newContentPanelFacets(FacetFactory,
	STargeter[],STargeter, STargeter)}
	 */
	protected SFacet[] newContentMenuFacets(FacetFactory ff, 
			STargeter[] basic, STargeter indexing, STargeter selection) {
		
		//May not construct menu bar
		return null;
	}

	/**
	Convenience method for the currently indexed member of the array 
	framed by {@link #contentFrame}. 
	 */
	public Object selection() {
		
		//Extract reference from frame
		return ((SelectingFrame) contentFrame()).selection().single();
	}
	
	/**
	Implements abstract method. 
	@see SContenter#contentFrame()
	 */
	public SFrameTarget contentFrame() {
		
		//Return reference
		return contentFrame;
	}

	/**
	Implements interface method.
	<p>Returns the simple class name.  
	@see SContenter#title()
	 */
	final public String title() {
		return getClass().getSimpleName();
	}

	/**
	Empty implementation of interface method. 
	@see SContenter#areaRetargeted(SContentAreaTargeter)
	 */
	public void areaRetargeted(SContentAreaTargeter area) {}
	
	/**
	Implements interface method with invalid stub. 
	@see demo.DemoSurface.Contenter#getContextFacets()
	 */
	public MenuFacets getContextFacets() {
		throw new RuntimeException("Not implemented in "+this);
	}

	@Override
	public Class targetType(){
		return getClass();
	}
}
