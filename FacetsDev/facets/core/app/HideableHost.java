package facets.core.app;
import facets.core.app.Dialogs.Response;
import facets.core.superficial.SFacet;
import facets.core.superficial.app.SHost;
/**
{@link SHost} for a {@link PagedSurface}.
 */
public interface HideableHost extends SHost{
	FacetLayout newLayout(SFacet content,SFacet buttons,SFacet extras);
	void hide(Response response);
}