package apps.applet;

import facets.core.app.FeatureHost;
import facets.core.app.MenuFacets;
import facets.core.app.SAreaTarget;
import facets.core.app.SContentAreaTargeter;
import facets.core.app.ViewerContenter;
import facets.core.app.FeatureHost.LayoutFeatures;
import facets.core.superficial.Notice;
import facets.core.superficial.SFacet;
import facets.core.superficial.STarget;
import facets.core.superficial.app.SHost;
import facets.core.superficial.app.SSurface;
import facets.core.superficial.app.SHost.FacetLayout;
import facets.facet.FacetFactory;
import facets.util.Util;
import applicable.LiveExternalWindow;
import apps.DemoApplet;
import apps.DemoSurface;
import apps.codeview.CodeSourceView;
import apps.codeview.CodeViewContenter;
import apps.codeview.CodeViewFacets;
import apps.codeview.CodeViewContenter.SourceViewer;

/**
{@link DemoApplet} that displays demo code or
can be run headless to supply code to an external browser window. 
<p>{@link CodeViewer} defines applet functionality using a 
subclass of {@link CodeViewContenter}.   
 */
public final class CodeViewer extends DemoApplet{
	/**
	Subclass that implements demo interface
	 */
	private final class AppletContenter extends CodeViewContenter 
		implements DemoSurface.Contenter{

		//Stores flag
		private final boolean headless;
		
		//To enable context menu
		private CodeViewFacets codeFacets;

		AppletContenter(ContentSource source, LiveExternalWindow window, 
				CodeSourceView view, FacetFactory ff, String openingClass, 
				boolean headless) {
			
			//Most parameters to superclass, set flag
			super(source, window, view, ff, openingClass);
			this.headless = headless;
		}

		//Pass flag to superclass
		@Override 
		protected boolean sourcePaneOnly() {
			return headless;
		}

		//Implements interface method
		public FacetLayout newLayout(SHost host, SContentAreaTargeter area){
			
			//Not needed?
			if (headless) return null;
			
			//Custom facet builder
			codeFacets = new CodeViewFacets(ff, area);
			
			//Get menus 
			SFacet[] menus = codeFacets.contentMenuRoots();
			
			//Create and return simple layout
			return ((FeatureHost) host).newLayout(area.areaTarget().attachedFacet(),
					new FacetFactory.AppletFeatures(null,menus,false));
		}

		//Invalid stub for other interface
		public LayoutFeatures newContentFeatures(SContentAreaTargeter area) {
			throw new RuntimeException("Not implemented in "+this);
		}
		
		//Reimplement by further delegation to facet
		public MenuFacets getContextFacets() {
			return codeFacets.getContextMenuFacets();
		}
	}

	//Applet parameters 
	final public static String 
		PARAM_OPENING_CLASS = "openingClass",
		PARAM_HEADLESS = "headless";
	
	//For single instance
	private static CodeSourceView storeView;	
	
	/**
	Implements abstract method. 
	@see DemoApplet#newSurface(FacetFactory,FeatureHost, boolean)
	 */
	@Override
	protected SSurface newSurface(FacetFactory ff, FeatureHost host, 
			final boolean inBrowser) {
		
		//Get parameters
		final String openingClass = getParameter(PARAM_OPENING_CLASS);
		final boolean headless = new Boolean(getParameter(PARAM_HEADLESS));
		
		//Communicate with any applet browser window(s)
		final LiveExternalWindow window = !inBrowser ? null 
				: new LiveExternalWindow();	
		if(window != null) {
			window.connectToWindow(this);		
			if(!headless) window.startExternalChecks();	
			else Util.printOut("CodeViewer running headless");
		}
		
		//Create (maybe shared) source view
		final CodeSourceView sourceView = !inBrowser ? null 
				: getSourceView(true, window);
		
		//Create code viewer surface
		return new DemoSurface("CodeViewer", ff, host) {		
			
			//Implement abstract method
			@Override
			protected Contenter newContenter(FacetFactory ff) {

				//Create source that will load code tree
				ViewerContenter.ContentSource source = CodeViewContenter.newTreeSource();
				
				//Define, create and return contenter
				return new AppletContenter(source, window, sourceView, ff, openingClass, 
						headless);
			}
			
			//Override method to write HTML to external viewer
			@Override
			public void notify(Notice notice) {
				
				//Carry out complete surface retargeting, maybe quit
				super.notify(notice);
				if(!inBrowser)return;
				
				//Update any external window with latest HTML
				CodeViewer.this.findSourceViewer().writeExternalHTML();
			}
		};	
	}	

	/**
	Can be called by browser Javascript to get HTML for a class (eg from
	a headless instance). 
	 */
	public String getExternalHtml(String codeClass) {
		
		//Delegate to viewer
		return findSourceViewer().getExternalHtml(codeClass);
	}

	/**
	Convenience method that finds the {@link SourceViewer} in surface. 
	 */
	private SourceViewer findSourceViewer() {
		
		//Get viewer areas
		STarget[] rootChildren =  surface().surfaceTargeter().areaTarget().indexableTargets();
		
		//Return the right viewer
		return (SourceViewer) (rootChildren.length == 1 ? 
				rootChildren[0] : ((SAreaTarget)rootChildren[1]).indexedTarget());
	}	

	/**
	Creates and initialises class instances of LiveConnect browser proxy 
	and view, for sharing between instances. 
	@param shareView defines instance return policy
	@return if <code>shareView</code> is <code>true</code>, the first view created as
	{@link #storeView}; otherwise a new view
	 */
	private synchronized static CodeSourceView getSourceView(boolean shareView, 
			LiveExternalWindow window) {
		//Return existing view?
		if(shareView && storeView != null) return storeView; 
		
		//Create and return view wrapping window
		return storeView = new CodeSourceView("Code", window);
	}
}
