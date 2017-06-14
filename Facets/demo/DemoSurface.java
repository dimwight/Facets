package demo;

import static facets.util.Debug.*;
import static facets.util.app.Events.*;
import facets.core.app.MenuFacets;
import facets.core.app.SurfaceServices;
import facets.core.superficial.Notice;
import facets.core.superficial.Notifiable;
import facets.core.superficial.Notifying.Impact;
import facets.core.superficial.SFacet;
import facets.core.superficial.STarget;
import facets.core.superficial.STargeter;
import facets.core.superficial.app.AreaTargeter;
import facets.core.superficial.app.SAreaTarget;
import facets.core.superficial.app.SContentAreaTargeter;
import facets.core.superficial.app.SContenter;
import facets.core.superficial.app.SHost;
import facets.core.superficial.app.SHost.FacetLayout;
import facets.core.superficial.app.SSurface;
import facets.facet.FacetFactory;
import facets.util.Titled;
import facets.util.Tracer;

/** 
 Demonstrates construction of a minimal surface.
 <p>{@link DemoSurface} is a {@link SSurface} with
 <ul>
 <li>a single {@link SContenter} (which must be a {@link DemoSurface.Contenter})
 defining application-specific functionality 
 <li>a {@link #surfaceTargeter()} identical with the 
 {@link SContentAreaTargeter} created by its contenter
 <li>a {@link FacetFactory} to define {@link SFacet}s attached to this {@link STargeter} tree
 <li>a {@link SHost} that will supply GUI elements for these {@link SFacet}s
 </ul>
 <p>Methods are <code>final</code> apart from the abstract
 {@link #newContenter(FacetFactory)}. 
 */
public abstract class DemoSurface extends Tracer implements SSurface, SurfaceServices {

	/**
	For {@link SContenter}s to be created by {@link DemoSurface}.
	 */
	public interface Contenter extends SContenter{

		//Return a suitable layout
		FacetLayout newLayout(SHost host, SContentAreaTargeter area);

		//Supply context menu facet to surface
		MenuFacets getContextFacets();
	}

	//Immutable fields
	private final String title;
	private final FacetFactory ff;
	private final SHost host;
	
	//Fields set during build()
	private SContenter contenter;
	private SContentAreaTargeter targeter;

	/**
	 Unique constructor. 
	 @param title (may not appear except in debug trace)
	 @param ff surface builder for use by contenter
	 @param host context for GUI
	 */
	protected DemoSurface(String title, FacetFactory ff, SHost host) {
		
		//Set final references
		this.title = title;
		this.ff = ff;
		this.host = host;
		trace(".DemoSurface: host=",host);
		events=false;
		trace=true;
		traceEvent(">Created surface");
	}

	/**
	Implements interface method. 
	<p>Demonstrates the application build sequence. 
	@see SSurface#buildRetargeted()
	 */
	final public void buildRetargeted() {
		
		//Debug stuff
  	traceEvent(">Opening surface "+info(this));
		
		//Create contenter
		contenter = newContenter(ff);
		traceEvent(">Creating content for " + info(contenter));
		
		//Create area tree with attached facet
		SAreaTarget area = contenter.newContentArea(false);

		//Create, connect and retarget root targeter
		targeter = (SContentAreaTargeter) area.newTargeter();
		targeter.setNotifiable(this);
		traceEvent(">Creating targeter " + info(targeter));
		targeter.retarget(area,Impact.DEFAULT);
		
		//Create layout, pass to host
		FacetLayout layout = ((Contenter) contenter).newLayout(host, targeter);
		traceEvent(">Creating layout " + info(layout));
		host.setLayout(layout);
		
		//Finish build
		contenter.areaRetargeted(targeter);
		targeter.retargetFacets(Impact.DEFAULT);
  	traceEvent(">Opened surface "+info(this) + "\n");
	}

	/**
	Create the contenter which will define application-specfic functionality. 
	<p>Called from {@link #buildRetargeted()}
	@param ff the facet builder passed to the constructor
	 */
	protected abstract Contenter newContenter(FacetFactory ff);

	/**
	Implements interface method. 
	<p>Demonstrates retargeting of the targeter tree and attached facets. 
	@see Notifiable#notify(Notice)
	 */
	public void notify(Notice notice) {
		
		//Issue message?
		traceEvent("Surface " + info(this) + " notified by " + notice);
		
		//Retarget the complete targeter tree starting at the root
		targeter.retarget(targeter.target(),notice.impact);
		
		//Inform the contenter of the retargeting
		contenter.areaRetargeted(targeter);
		
		//Retarget facets in the updated tree
		targeter.retargetFacets(notice.impact);
		
		//Issue message?
		traceEvent("Completed retargeting in surface " + info(this) + "\n");
	}

	/**
	Implements interface method. 
	<p>Returns the {@link SContentAreaTargeter} created by the contenter.	
	@see SSurface#surfaceTargeter()
	 */
	final public AreaTargeter surfaceTargeter() {
		if (targeter == null) 
			throw new IllegalStateException("No targeter in " + info(this));
		else return targeter;
	}

	/**
	Implements interface method. 
	@see Titled#title()
	 */
	final public String title() {
		return title;
	}

	/**
	Implements interface method. 
	@see SSurface#host()
	 */
	final public SHost host(){
		return host;
	}

	/**
	Delegates interface method to contenter. 
	see {@link SurfaceServices#getContextMenuFacets()}
	 */
	public MenuFacets getContextMenuFacets() {
		return ((Contenter) contenter).getContextFacets();
	}
	
	/**
	Invalid implementation of interface method. 
	<p>Not used by any applet. 
	see {@link SurfaceServices#handleInvalidInput(STarget, Object)}
	 */
	final public void handleInvalidInput(STarget target, Object input) {
		throw new RuntimeException("Not implemented in "+info(this));		
	}

	@Override
	public boolean isBuilt(){
		return targeter!=null;
	}
}