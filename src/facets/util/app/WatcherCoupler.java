package facets.util.app;
import static java.lang.System.*;
import facets.util.Debug;
import facets.util.Objects;
import facets.util.Util;
import java.util.List;
/**
Enables client code to communicate with {@link AppWatcher}. 
<p>The methods of {@link WatcherCoupler} enable client code to 
<ul>
<li>respond appropriately when a {@link WatchableOperation}  
throws an exception 
<li>tell {@link AppWatcher} whether or not actually to time operations
that request this 
<li>respond appropriately when either a {@link WatchableOperation} or the 
application as a whole is timed out
</ul>	 
*/
public class WatcherCoupler extends facets.util.Tracer{
	/**
	How long should {@link AppWatcher} wait before calling
	 {@link #handleSystemTimeout(WatchableOperation, Thread, boolean)}?
	<p>Used as reference by default implementations of {@link #blockSec()}. 
	@return by default 0 to inhibit system timeout
	 */
	protected int systemSec(){
		return 0;
	}
	protected int blockSec(){
		return systemSec()/4;
	}
	protected void handleSystemTimeout(WatchableOperation op,Thread worker,
			boolean blocking){
		Util.printOut("System timed out in "+AppWatcher.traceName+
			" after "+systemSec()+" sec:" +
			"\noperation="+op+
			"\nworker="+worker+
			"\nblocking="+blocking+
			"\nthreads="+Objects.toStringWithHeader(Debug.getSortedThreads()));
		exit(-1);
	}
	protected void operationsStarting(List<WatchableOperation>ops){
		trace(".operationsStarting: ops=",ops);
	}
	protected void operationsCompleted(List<WatchableOperation>ops){
		trace(".operationsCompleted: ops=",ops);
	}
	protected void handleException(List<WatchableOperation>ops,Exception e){
		throw new RuntimeException(ops.toString(),e);
	}
	protected boolean checkTimeouts(){
		return false;
	}
	protected boolean retryCancel(List<WatchableOperation>ops){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	@Override
	protected void traceOutput(String msg){
		if(false)super.traceOutput(msg);
	}
}