package facets.core.app;
import facets.core.superficial.SFacet;
import facets.core.superficial.STarget;
import facets.core.superficial.Notifying.Impact;
import facets.core.superficial.app.SSelection;
import facets.core.superficial.app.SSurface;
import facets.util.Debug;
import facets.util.Tracer;
/**
Provides for creation and updating of a {@link SSurface} contained within 
a {@link SFacet}. 
 */
public abstract class FacetHostable extends Tracer{
	/**
	Enables requests to the containing {@link SFacet} to build and maintain 
	a suitable {@link SSurface}. 
	 */
	public interface Hosting extends SFacet{
		void refreshPaged(String title,PagedActions actions,Object source,AppSurface app);
		void refreshViewer(Object source);
	}
	/**
	To be called from {@link SFacet#retarget(STarget, Impact)} in the containing {@link SFacet}. 
	@param host will create and maintain a suitable {@link SSurface} via the appropriate
	callbacks 
	@param target should provide reference to new content for the {@link SSurface} hosted
	 */
	public abstract void facetRetargeted(Hosting host,STarget target,Impact impact);
	public PagedContenter[]newPagedContenters(Object source){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	public AppContenter newViewerContenter(Object source){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	public Object getSourceSelectionContent(SSelection selection){
		return selection.single();
	}
}