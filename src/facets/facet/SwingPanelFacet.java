package facets.facet;
import facets.core.superficial.SFacet;
import facets.core.superficial.STargeter;
import java.util.Map;
import javax.swing.JComponent;
/**
{@link SFacet} based on {@link JComponent}s. 
<p>A {@link SwingPanelFacet} returns as {@link #components()} a
map of references to {@link JComponent}s; these references should
only be to those {@link JComponent}s that it creates and manages, and 
may be returned under multiple keys. 
all {@link SFacet}s returned by simple facet methods will
implement {@link SwingPanelFacet}, with the following methods returning useful
implementations:
<ul><li>{@link FacetFactory#textualField(STargeter,int,String)} 
<li>{@link FacetFactory#indexingDropdownList(STargeter, String)} 
<li>{@link FacetFactory#simpleMastered(STargeter, facets.facet.FacetMaster.Simple)} 
</ul> 
<p>To obtain the components returned and keys used, query the {@link Map}
returned. 
 */
public interface SwingPanelFacet extends SFacet{
	/**
	Suitable key for use in component map. 
	 */
	String KEY_PANEL="Panel",KEY_LABEL="Label",
		KEY_LABELLED="Labelled";
	Map<String,JComponent> components();
}
