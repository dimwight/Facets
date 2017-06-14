package facets.core.app;
import facets.core.superficial.SFacet;
import facets.core.superficial.STarget;
import facets.core.superficial.TargetCoupler;
import facets.core.superficial.app.SView;
/**
Can respond to requests from surface facets. 
<p>{@link SurfaceServices} defines useful functionality 
required by {@link SFacet}s from their enclosing surface. 
 */
public interface SurfaceServices{
	/**
	Return suitable facets for a viewer context menu.
	<p>The source of the request will generally be redundant as 
	the receiver will be able to determine which viewer is active;  
	in any case, other considerations such as the current selection will determine
	the facets to be returned.  
	 */
	MenuFacets getContextMenuFacets();
	/**
	Respond to an input to a facet which is invalid for its target. 
	<p>A simple facet will generally check validity by querying a 
	{@link TargetCoupler} attached to the target; a viewer facet by
	querying its {@link SView}.   
	@param target exposed by the facet
	@param input is known to be invalid 
	 */
	void handleInvalidInput(STarget target,Object input);
}
