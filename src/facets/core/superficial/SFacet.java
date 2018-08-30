package facets.core.superficial;
/**
Superficial facet. 
<p>{@link SFacet} abstracts an application surface 
	element that defines and manages widgets providing view and control of a {@link 
	STarget}. 
 */
public interface SFacet extends SRetargetable{
	void dispose();
}
