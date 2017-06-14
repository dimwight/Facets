package facets.core.app;
import facets.core.superficial.SFacet;
import facets.core.superficial.app.SHost;
/**
{@link SHost} that can create and set app-like surface layouts. 
<p>Layouts are returned by {@link #newLayout(SFacet, LayoutFeatures)}
	 */
public interface FeatureHost extends SHost{
	/**
	Defines layout to be returned from
	 {@link FeatureHost#newLayout(SFacet,LayoutFeatures)}.
	 <p><code>null</code> implementations are allowed for all methods.  
	*/
	public interface LayoutFeatures{
		/**
		Defines menu bar or ribbon to be displayed whenever this layout is active. 
		<p>Should include any desired application-level elements. 
		 */
		SFacet[]header();
		/**
		To appear above content area. 
		 */
		SFacet toolbar();
		/**
		To appear beside content area. 
		 */
		SFacet sidebar();
		/**
		To appear below content area. 
		 */
		SFacet status();
		/**
		Will be returned via {@link #activeServices()} when
	the layout was the last set with {@link SHost#setLayout(facets.core.superficial.app.SHost.FacetLayout)}. 
		 */
		SurfaceServices services();
		/**
		May appear in sash pane with content area, hosting independent surfaces such 
		as help, debug graph.  
		 */
		SFacet extras();
	}
	/**
	Create a layout exposing the content via the features passed. 
	 */
	FacetLayout newLayout(SFacet content,LayoutFeatures features);
	/**
	Open in GUI context the surface which either created 
	or was passed the {@link FeatureHost}. 
	 */
	void openHostedSurface();
	/**
	Return suitable services for facet in the active layout. 
	<p>These will generally have been passed to methods such as 
	{@link #newLayout(SFacet, LayoutFeatures)}, 
	enabling a hosted {@link AppSurface} to delegate definition 
	to an {@link facets.core.app.AppContenter}. 
	 */
	SurfaceServices activeServices();
	void showExtras(boolean on);
}
