package facets.facet;
import facets.util.Debug;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JComponent;
import javax.swing.Timer;
/**
	Requests focus for a Swing component. 
	<p>{@link FocusRequesterSwing} encapsulates the following process:
	<ol><li>requesting focus for a component
	<li>checking to see if focus has in fact been acquired
	<li>repeating the cycle as necessary
	<li>giving up if too many requests are made
</ol>
	 */
	class FocusRequesterSwing{
		private int maxRequests;
		private int requests;
		private final JComponent swing;
		private final Timer requester=new Timer(requestInterval(),
				new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if(++requests>maxRequests){
					if(false)throw new RuntimeException(
							"Too many focus requests for "+Debug.info(swing));
					else requester.stop();
				}
				swing.requestFocusInWindow();
			}
		});
		private final FocusAdapter listener=new FocusAdapter() {
			public void focusGained(FocusEvent e){
				requester.stop();
			}
		};
		/**
		Unique constructor. 
		@param swing to receive the focus
		 */
		public FocusRequesterSwing(JComponent swing){
			(this.swing=swing).addFocusListener(listener);
		}
		/**
		Starts a request cycle.
		<p>Returns immediately if {@link JComponent#isFocusOwner()} is <code>true</code>;
		otherwise calls {@link JComponent#requestFocusInWindow()} immediately, and 
		thereafter as specified by {@link #requestInterval()} until its internal
		{@link FocusListener} receives a {@link FocusEvent} from the component
		passed to the constructor.    
		 */
		final public void startRequesting(){
			if(swing.isFocusOwner())return;
			swing.requestFocusInWindow();
			maxRequests=maxRequests();
			requests=1;
			requester.start();			
		}
		/**
		The maximum number of focus requests to be made. 
		<p>Requesting will stop if this number is exceeded. 
		 */
		protected int maxRequests(){
			return 5;
		}
		/**
		The interval to be used between focus requests.
		 */
		protected int requestInterval(){
			return 300;
		}
	}