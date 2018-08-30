package facets.core.superficial.app;
import facets.core.app.FeatureHost.LayoutFeatures;
import facets.core.superficial.SFacet;
/**
GUI context for a {@link SSurface}. 
<p>{@link SHost} must be implemented by a facets builder 
  for each GUI context in which a {@link SSurface} is to be constructed.
	<p>For an application this context will usually comprise   
  several widgets eg the content area and menu, tool and status bars.
  <p>Single widget contexts may include those for 
  <ul>
	<li>dialogs, wrapping a modal dialog
	<li>sidebar {@link SFacet}s that can hide themselves
	<li>viewer {@link SFacet}s attached to the area tree  
	</ul>
	*/
public interface SHost{
	/**
	Arrangement of {@link SFacet}s for a {@link SSurface}. 
	<p>{@link FacetLayout} defines no contract more specific than that it can
	be passed to {@link SHost#setLayout(SHost.FacetLayout)},  
	but implementations can encapsulate specific layout options
	provided by the {@link SHost}. This approach also allows 
	complete layouts to be switched eg in multi-content applications. 
	 */
	public interface FacetLayout{}
  /**
  The widget(s) wrapped by the {@link SHost}. 
    <p>Gives access to the actual widget(s) used by the GUI context 
      to attach the widget roots created by facet layouts.
   */
  Object wrapped();
	/**
	Attach widgets managed by the {@link SFacet}s of the {@link FacetLayout}
	to the GUI context wrapped by the {@link SHost}. 
	@param layout will generally have been created by the {@link SHost}
	 */
	void setLayout(FacetLayout layout);
	/**
	Update GUI based on the layout last set and other features as specified for the {@link SSurface}.  
	 */
	void updateLayout(SSurface surface);
	void setTitle(String title);
}
