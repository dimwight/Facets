package demo;

import facets.core.app.FeatureHost;
import facets.core.superficial.Notifying.Impact;
import facets.core.superficial.app.SHost;
import facets.core.superficial.app.SSurface;
import facets.core.superficial.app.SurfaceStyle;
import facets.facet.FacetFactory;
import facets.facet.kit.swing.KitSwing;
import facets.util.Debug;
import facets.util.Util;
import facets.util.app.Events;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.JApplet;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import demo.hello.HelloContenter;

/** 
 Applet that can host and provide a facet builder for {@link DemoSurface}). 
 <p>Also defines debug methods callable by Javascript in browser.  
 */
public class DemoApplet extends JApplet {

	//Opening message
	private static final String INIT_MSG = "Superficial - ";
	
	//Parameter keys
	public static final String PARAM_BROWSER = "browser",PARAM_TYPE="hello",
			GREETING="Hello world!";

	//Set during init()
	private boolean inBrowser;
	private SSurface surface;
	private JMenuBar menuBar;
	private JMenu debugMenu = null;

	/**
	 Unique constructor. 
	 */
	public DemoApplet() {
		
		//Set global flags
		Events.trace = false&&!inBrowser;
		KitSwing.debug=false;
	}

	/**
	 Reads applet parameter and builds surface returned by abstract method. 
	 <p>Calls {@link SSurface#buildRetargeted()} on the {@link SSurface} returned by
	  {@link #newSurface(FacetFactory, FeatureHost, boolean)}.
	 */
	@Override
	final public void init() {
		Events.traceEvent(">Initialising applet");
		
		//Set flag, maybe issue message
		inBrowser = Boolean.valueOf(getParameter(PARAM_BROWSER));
		if (inBrowser) Util.printOut(INIT_MSG + getClass().getSimpleName());	
		
		//Create toolkit and surface builder
		SurfaceStyle style = inBrowser ? SurfaceStyle.BROWSER 
				: SurfaceStyle.APPLET;
		KitSwing kit = new KitSwing(false,true,style==SurfaceStyle.APPLET);
		FacetFactory ff = FacetFactory.newAppletCore(kit,style);

		//Get installed host 
		FeatureHost host = kit.newSwingAppletHost(this);

		//Define and build surface		
		surface = newSurface(ff, host, inBrowser);				
		surface.buildRetargeted();
	
		Events.traceEvent(">Opened applet");
		
		if(false&&!inBrowser) appendDebugMenu();

	}

	/**
	Create the {@link SSurface} to be hosted within this applet. 
	<p>Called from {@link #init()}; 
	default implementation obtains a {@link SSurface} by passing the value 
	of {@link #PARAM_TYPE} to 
	{@link HelloContenter#newSurface(String, FacetFactory,SHost, Class)}
	 @param ff facet builder with appropriate L&F 
	 @param host represents the content pane and menu bar
	 @param inBrowser value of {@link #PARAM_BROWSER}
	 */
	protected SSurface newSurface(FacetFactory ff, FeatureHost host, 
			boolean inBrowser){
		
		//Define content
		String contentName = String.valueOf(getParameter(PARAM_TYPE));
		
		//Create and return surface
		return HelloContenter.newSurface(contentName, ff, host, HelloContenter.class);	
	}

	/**
	 Initialises the app hosted and set the debug trace mode. 
	 <p>Can be called using Javascript in the browser page
	 containing the applet instance, or from the debug menu. 
	 @param trace sets the mode on or off. 
	 */
	final public void traceInit(boolean trace) {
		
		Util.printOut("DemoApplet.traceInit: ");
		
		//If changed issue explanatory message 
		if (Events.trace != trace)
			Util.printOut("Initialising ",  Debug.info(surface) + 
					(trace ? "" : " with trace off\n"));
		
		//Set flag, reopen app 
		Events.trace = trace;
		surface.buildRetargeted();
		
		if(!inBrowser) appendDebugMenu();
	}	
	
	/**
	 Sets the debug graph mode. 
	 <p>Can be called using Javascript in the browser page
	 containing the applet instance, or from the debug menu. 
	 <p>If set on, the app viewer window will split to show
	 the current object graph. 
	 @param showGraph sets the mode on or off.
	 */
	final public void setDebugGraph(boolean showGraph) {
		
		//Set flag, trigger retargeting 
		if(true)throw new RuntimeException("Not implemented in "+Debug.info(this));
		surface.surfaceTargeter().notifyParent(Impact.DEFAULT);
	}

	/**
	 Allows debug flags to be set when not in browser. 
	 */
	private void appendDebugMenu() {
		
		//Only create once
		if (debugMenu == null) {
			
			//Menu (and flag)
			debugMenu = new JMenu("Debug");
			
			//Create and add items
			final JCheckBoxMenuItem 
			graphItem = new JCheckBoxMenuItem("Graph"), 
			traceItem = new JCheckBoxMenuItem("Trace");
			debugMenu.add(graphItem);
			debugMenu.add(traceItem);
			
			//Add listeners
			graphItem.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					setDebugGraph(graphItem.isSelected());
				}
			});
			traceItem.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					traceInit(traceItem.isSelected());
				}
			});
		}
		
		//Add menu to end of surface menu
		menuBar.add(Box.createHorizontalGlue());
		menuBar.add(debugMenu);
	}

	/**
	 Overrides default implementation (debug only). 
	 @see java.awt.Component#doLayout()
	 */
	@Override
	final public void doLayout() {
		super.doLayout();
		if (!isShowing() || inBrowser) return;
		
		//Debug actions triggered by resizing applet viewer
		if (false) Util.printOut("DemoApplet: ",  getSize());
		if (false) setDebugGraph(true);
	}
	
	
	/**
	Returns any surface created in 
	{@link #newSurface(FacetFactory, FeatureHost, boolean)}. 
	<p>Complains if none exists. 
	 */
	final public SSurface surface() {
		if (surface == null) throw new IllegalStateException(
				"No surface in " + Debug.info(this));
		return surface;
	}
}