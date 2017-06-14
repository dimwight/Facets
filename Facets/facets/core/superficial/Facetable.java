package facets.core.superficial;
import facets.core.superficial.Notifying.Impact;
/**
Application element that may have an attached facet. 
<p>{@link Facetable} captures the concept of an
object that can have one or more facets attached to it, which it applies
during each retargeting sequence. 
<p>No provision is made for removing attached facets, on the assumption that these are
costly to create.  
 */
public interface Facetable{
	/**
	Attach a facet. 
	<p>Implementors should allow either a single (and thereafter immutable)
	facet or an unlimited number. 
	@param facet will be applied during each retargeting sequence
	 */
	void attachFacet(SFacet facet);
	/**
	Retarget any attached facets. 
	<p>Facets (if any) will have been attached with {@link #attachFacet(SFacet)}
	 */
	void retargetFacets(Notifying.Impact impact);
}
