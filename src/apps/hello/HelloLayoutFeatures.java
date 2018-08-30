package apps.hello;
import facets.core.app.SContentAreaTargeter;
import facets.facet.FacetFactory;
public abstract class HelloLayoutFeatures extends FacetFactory{
	public HelloLayoutFeatures(FacetFactory source){
		super(source);
	}
	protected abstract void buildAndAttachFacets(SContentAreaTargeter area);
	public abstract void layOutFacets();
}
