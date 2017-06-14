package facets.util.app;
import facets.util.Debug;
import facets.util.Util;
	/**
	Wrapper for code to be monitored by {@link AppWatcher}. 
	 */
	public abstract class WatchableOperation<T>{
		/**
		Defines how a {@link WatchableOperation} can be cancelled. 
		 */
		public enum CancelStyle{None,Dialog,Timeout,Input}
		private static int ids=1;
		private final int id=ids++%100;
		private int call;
		private final String title;
		public WatchableOperation(String title){
			this.title=title;
		}
		final T callOperations(int call){
			this.call=call;
			return doOperations();
		}
		public final T doOperations(){
			doSimpleOperation();
			return doReturnableOperation();
		}
		protected void doSimpleOperation(){}
		protected T doReturnableOperation(){
			return null;
		}
		public CancelStyle cancelStyle(){
			return CancelStyle.None;
		}
		/**
		Includes the title passed to the constructor and debug detail. 
		 */
		final public String toString(){
			return "#"+id+"c"+call+" "+title;
		}
		public String[]getBlockingCancelTexts(){
			return null;
		}
		protected String[]newContentCreationTexts(String appTitle,Object source){
			return new String[]{
					"Creating Content",
					appTitle+" is creating content from " +source+".",
					"Cancel Requested",
					"If you cancel this operation, $appTitle may slow down or close without warning.<br>"+
					"Wait for the operation to complete?",
							};
		}
	}