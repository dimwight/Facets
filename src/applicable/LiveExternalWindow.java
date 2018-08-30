package applicable;
import facets.core.superficial.Notifying;
import facets.core.superficial.SToggling;
import facets.facet.kit.*;
import facets.util.Util;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
/**
 {@link applicable.LiveWindow} that manages an external window in 
 applet browser using the LiveConnect API.
 */
public final class LiveExternalWindow extends LiveWindow{
	
	/**
	Names for Javascript routines on browser page. 
	 */
	public final static String 
		EXTERNAL_OPEN = "externalOpen",
		CLOSE_EXTERNAL = "closeExternal", 
		WRITE_EXTERNAL = "writeExternal";
	
	public final SToggling  externalToggling = 
		new SToggling("External Window", false, new SToggling.Coupler() {

		//Re-implement coupler method 
			public void stateSet(SToggling t) {
		
				//Get new toggle state
				final boolean toOpen = t.isSet();
				
				//Handle no browser
				if (!LiveExternalWindow.this.hasConnected()) {
					Util.printOut("LiveExternalWindow: ",
								 (toOpen ? WRITE_EXTERNAL : CLOSE_EXTERNAL));
					return;
				}
				
				//Make appropriate call to Javascript on hosting page, set live state
				if (toOpen)
					call(WRITE_EXTERNAL, html);
				else
					call(CLOSE_EXTERNAL);
				shrinkExternal.setLive(toOpen);
			}
		}
	),
	shrinkExternal = new SToggling("Use Smaller Fonts", Toolkit.inWindows, 
				new SToggling.Coupler());

	//HTML for writing to external window
	private String html;
	
	/**
	Unique constructor. 	
	 */
	public LiveExternalWindow() {
		
		//Toggling must be disabled until external window opened
		shrinkExternal.setLive(false);		
	}

	/**
	 Starts checking external window status. 
	 <p>Periodically checks the state of {@link #EXTERNAL_OPEN} in the hosting page; 
	 where necessary {@link #externalToggling} is updated and a 
	 retargeting triggered.   
	 */
	public synchronized void startExternalChecks() {

		//Does checking
		ActionListener checker = new ActionListener() {

			public void actionPerformed(ActionEvent e) {
		
				//Get toggle and window status
				boolean 
					toggled = externalToggling.isSet(), 
					open = externalOpen();

				//Return if no action needed
				if (toggled == open)
					return;
				
				//Set toggling to state of window, trigger retargeting
				externalToggling.set(open);
				externalToggling.notifyParent(Notifying.Impact.DEFAULT);
			}
		};

		//Create and start timer
		new Timer(1000, checker).start();
	}

	/**
	 Call Javascript to write HTML to any external window. 
	 @param html to set
	 @param forceOpen call even no window is open
	 */
	public synchronized void writeExternal(final String html, boolean forceOpen) {		
		
		//Maybe print message, quit if no browser
		if (!hasConnected()) {
			Util.printOut("LiveExternalWindow.writeExternal:" ,forceOpen);
			return;
		}
		
		//Update window if open or force
		boolean 
		newHtml = this.html == null || !this.html.equals(html),
		open = externalOpen();
		if ((open && newHtml) || (!open && forceOpen))
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					call(WRITE_EXTERNAL, html);
				}
			});
		
		//Store HTML ready for next check
		this.html = html;	
	}

	/**
	 Checks if there is a code viewer window open. 
	 */
	private boolean externalOpen() {
		try {
			return !hasConnected() ? false
					: ((Boolean) call(EXTERNAL_OPEN)).booleanValue();
		} catch (Exception e) {
			return false;
		}
	}

}	