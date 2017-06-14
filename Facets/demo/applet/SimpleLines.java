package demo.applet;

import facets.core.app.FeatureHost;
import facets.core.app.MenuFacets;
import facets.core.superficial.app.SContentAreaTargeter;
import facets.core.superficial.app.SHost;
import facets.core.superficial.app.SSurface;
import facets.core.superficial.app.SHost.FacetLayout;
import facets.facet.FacetFactory;
import demo.DemoApplet;
import demo.DemoSurface;

/**
Spike applet. 
<p>{@link SimpleLines} extends its superclass by creating a 
trivial subclass of {@link DemoSurface} that 
defines applet functionality with a {@link SimpleLineContenter} subclass.   
 */
public class SimpleLines extends DemoApplet{
	
	/**
	Subclass to comply with interface. 
	 */
	private final class DemoLineContenter extends SimpleLineContenter 
		implements DemoSurface.Contenter{

		//Pass parameters on
		DemoLineContenter(FacetFactory ff) {
			super(ff);
		}

		@Override
		public FacetLayout newLayout(SHost host, SContentAreaTargeter area) {
			
			return ((FeatureHost)host).newLayout(area.areaTarget().attachedFacet(),
					newContentFeatures(area));
		}
		
		/**
		Implements interface method with invalid stub. 
		@see demo.DemoSurface.Contenter#getContextFacets()
		 */
		@Override
		public MenuFacets getContextFacets() {
			return null;
		}
	}

	/**
	Implements abstract method. 
	@see DemoApplet#newSurface(FacetFactory, FeatureHost, boolean)
	 */
	@Override
	protected SSurface newSurface(FacetFactory facets, FeatureHost host, boolean inBrowser) {
		 
		//Simplified text line surface
		return new DemoSurface("Simple Lines", facets, host) {				
			@Override
			protected Contenter newContenter(FacetFactory ff) {
				return new DemoLineContenter(ff);
			}
		};
	}
}
