package facets.core.app;
import facets.core.superficial.SFacet;
import facets.core.superficial.STarget;
import facets.core.superficial.STarget.Targeted;
/**
Quasi-{@link SFacet} that contains other facets. 
<p>A {@link MountFacet} manages a widget containing the widgets 
  of other {@link facets.core.superficial.SFacet}s; it is thus not 
  strictly a true facet, as it does not expose any {@link STarget}. 
 */
public interface MountFacet extends SFacet{
	  /**
	Set the facets whose widgets will be contained by the 
	widget managed by this {@link MountFacet}. 
	   */
	void setFacets(SFacet...facets);
}