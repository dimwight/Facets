package facets.core.app;

import facets.core.app.FeatureHost.LayoutFeatures;
import facets.core.superficial.SFacet;
import facets.core.superficial.app.SHost;
import facets.core.superficial.app.SSurface;
import facets.core.superficial.app.SHost.FacetLayout;
import facets.util.Debug;

/**
Empty {@link SHost} for a headless {@link SSurface}.
 */
public class HeadlessHost implements FeatureHost{
	private static final class Layout implements FacetLayout{}
	private final SSurface surface;
	HeadlessHost(SSurface surface){
		this.surface=surface;
	}
	@Override
	public void openHostedSurface(){
		((AppSurface)surface).openApp();
	}
	@Override
	public Object wrapped(){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	@Override
	public void setTitle(String title){}
	@Override
	public void setLayout(FacetLayout layout){}
	@Override
	public SurfaceServices activeServices(){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	@Override
	public FacetLayout newLayout(SFacet content,LayoutFeatures features){
		return new Layout();
	}
	@Override
	public void updateLayout(SSurface surface){}
	@Override
	public void showExtras(boolean on){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
}