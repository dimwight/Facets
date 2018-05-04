package facets.facet.app;
import facets.core.app.ActionAppSurface;
import facets.core.app.MountFacet;
import facets.core.app.PagedSurface;
import facets.core.app.SAreaTarget;
import facets.core.app.SContentAreaTargeter;
import facets.core.app.SimpleContenter;
import facets.core.superficial.SFacet;
import facets.facet.AreaFacets;
import facets.facet.FacetFactory;
import facets.util.Debug;
import facets.util.tree.ValueNode;
/**
{@link SimpleContenter} that creates content for a {@link FacetPagedSurface}. 
 */
public abstract class FacetPagedContenter extends SimpleContenter{
	/**
	{@link FacetFactory} for use by {@link FacetPagedContenter}.
	<p>Defines {@link #newContentPanel(SContentAreaTargeter)} which simplifies 
	implementation of {@link FacetPagedContenter#newContentPanel(SContentAreaTargeter)}
	 */
	public static abstract class PanelFactory extends FacetFactory{
		public PanelFactory(FacetFactory core){
			super(core);
		}
		/**
		Build panel for {@link FacetPagedContenter}. 
		@param t passed from {@link FacetPagedContenter#newContentPanel(SContentAreaTargeter)}
		 */
		public abstract SFacet newContentPanel(SContentAreaTargeter t);
	}
	/** As passed to the constructor.*/
	protected final FacetFactory ff;
	/**
	Unique constructor. 
	@param title passed to superclass
	@param ff set as immutable field {@link #ff}
	 */
	public FacetPagedContenter(String title,FacetFactory ff){
		super(title);
		this.ff=ff;
	}
	/**
	Implements abstract method. 
	<p>Attaches a {@link MountFacet} with {@link AreaFacets#mount(SAreaTarget, boolean)}. 
	 */
	protected void attachAreaMountFacet(SAreaTarget area){
		ff.areas().mount(area,false);				
	}
	/**
	Implements interface method using a {@link PanelFactory}.  
	@return the return of {@link PanelFactory#newContentPanel(SContentAreaTargeter)} 
	 */
	public SFacet newContentPanel(SContentAreaTargeter t){
		return newPanelFactory(ff).newContentPanel(t);
	}
	/**
	Create a {@link PanelFactory} for return by {@link #newContentPanel(SContentAreaTargeter)}. 
	@param core passed to factory constructor
	 */
	protected PanelFactory newPanelFactory(FacetFactory core){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	@Override
	public void setSurface(PagedSurface surface){}
}
