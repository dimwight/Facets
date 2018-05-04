package facets.core.superficial.app;
import facets.core.app.SAreaTarget;
import facets.core.superficial.Facetable;
import facets.core.superficial.Notifying;
import facets.core.superficial.SFacet;
import facets.core.superficial.STarget;
/**
{@link STarget} representing a viewer or containing area
with an attached {@link SFacet}. 
<p>In addition to its two superinterfaces, {@link FacetedTarget}
defines other shared methods for the members of a area target tree. 
 */
public interface FacetedTarget extends STarget,Facetable{
	/**
	Attach an immutable facet. 
	<p>As a {@link STarget} representing its {@link #attachedFacet()},
	{@link FacetedTarget} can only attach a single viewer or area facet;
	it should complain if any attempt is made to attach another.  
   */
	void attachFacet(SFacet facet);
  /**
	The {@link SFacet} attached with {@link #attachFacet(SFacet)}. 
	<p>Return the facet attached to this {@link FacetedTarget},
	complaining if none yet attached. 
	 */
	SFacet attachedFacet();
	/**
	The parent in the area target tree. 
	 */
	SAreaTarget areaParent();
	/**
	Should the targeter tree treat the facet as having the GUI focus? 
	 */
	boolean isActive();
	/**
	Ensure that the targeter tree treats the faceted as having the GUI focus.  
	@see Notifying
	 */
	void ensureActive(Impact notify);
}
