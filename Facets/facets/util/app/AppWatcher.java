package facets.util.app;
import static facets.util.Debug.*;
import static facets.util.app.WatchableOperation.CancelStyle.*;
import static java.lang.System.*;
import static java.lang.Thread.*;
import facets.util.Debug;
import facets.util.Objects;
import facets.util.Times;
import facets.util.Tracer;
import facets.util.Util;
import facets.util.app.WatchableOperation.CancelStyle;
import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
/** High-level monitor of application operation. 
<p>{@link AppWatcher} has three closely-related functions. 
<ul>
	<li><b>Busy checking</b>: Detecting blocking of the event loop. 
	<li><b>Watching</b>: Handling of exceptions thrown by {@link WatchableOperation}s. 
	<li><b>Cancellation</b>: Honouring {@link WatchableOperation#cancelStyle()}s. 
</ul>
<h3>Busy checking</h3> 
<p>By frequent calls to its (GUI toolkit dependent) implementation of {@link 
	#checkBusy()}, {@link AppWatcher} detects blocks in the event loop. This 
	enables it to 
<ul>
	<li>trigger changes in the current {@link BusyCursor} 
	<li>alert {@link WatcherCoupler} when a {@link WatchableOperation} requesting 
	{@link CancelStyle#Timeout} fails to complete as specified by {@link WatcherCoupler#systemSec()}. 
	<li>trigger a system timeout if the event loop blocks longer than specified 
	by {@link WatcherCoupler#systemSec()}
</ul>
<h3>Watching</h3>
<p>Exceptions thrown by {@link WatchableOperation}s are passed to {@link WatcherCoupler}. 
<h3>Cancellation</h3>
<p>While the {@link WatchableOperation} completes in a (high-priority) background 
	thread, client code can request cancellation as shown below. Cancellation 
	requests are passed to {@link WatcherCoupler} for confirmation. 
<table border="0" cellpadding="5">
	<tr> 
		<td width=30%><i>Style</i></td>
		<td><i>Enabled by</i></td>
		<td width=30%><i>Triggered by</i></td>
	</tr>
	<tr valign="top"> 
		<td>{@link CancelStyle#Dialog}</td>
		<td>Blocking the event loop by presenting a modal dialog</td>
		<td>Dialog cancelled</td>
	</tr>
	<tr valign="top"> 
		<td>{@link CancelStyle#Timeout} </td>
		<td>Blocking the event loop internally while checking for timeouts</td>
		<td>Timeout detected during busy checking</td>
	</tr>
</table>
*/
public abstract class AppWatcher extends Tracer{
	public static final String traceName=false?"AW":AppWatcher.class.getSimpleName();
	private static final boolean checkTimeouts=false,traceBusy=false;
	protected void traceOutput(String msg){
		if(!doTrace())return;
		msg=traceName+msg;
		if(true)Util.printOut(msg);
		else Times.printElapsed(msg);
	}
	protected boolean doTrace(){
		return traceBusy;
	}
	protected static AppWatcher single;
	private static int calls=0;
	protected Thread worker;
	private final List<BusyCursor>cursors=new ArrayList();
	private WatcherCoupler coupler;
	private Timer checks;
	private List<WatchableOperation>ops=new ArrayList();
	private long notBusyAt=currentTimeMillis();
	private boolean passingException,blocking;
	private Exception exception;
	private Object returnable;
	public static final String LIVE="1/1/18";
	final public void pushCursor(BusyCursor cursor){
		if(cursor==null)throw new IllegalArgumentException(
				"Null cursor in "+Debug.info(this));
		else cursors.add(0,cursor);
		if(true)forceBusy(false);
	}
	final public void popCursor(){
		if(true)forceBusy(false);
		cursors.remove(0);
	}
	final public void forceBusy(boolean on){
		for(BusyCursor c:cursors)c.forceSwitch(on);
	}
	/**
	Activates/deactivates the {@link AppWatcher}. 
	@param c if non-<code>null</code> is passed to a new checking {@link Timer}; 
	any previous {@link Timer} is cancelled.  
	 */
	final public void setCoupler(WatcherCoupler c){
		if(false)throw new RuntimeException("Disabled in "+Debug.info(this));
		if(checks!=null)checks.cancel();
		this.coupler=c;
		if(c==null)return;
		final long busyMillis=traceBusy?500:50,timeSystem=coupler.systemSec()*1000,
				timeBlock=(checkTimeouts?3:coupler.blockSec())*1000;
		if(timeSystem>0&&(timeBlock<=0||timeSystem<timeBlock))throw new IllegalStateException(
				"Invalid timeout value in "+Debug.info(this));
		final String taskTitle=traceName+".checks";
		if(checks!=null)checks.cancel();
		(checks=new Timer(taskTitle)).schedule(new TimerTask(){
			public String toString(){
				return taskTitle;
			}
			public void run(){
				boolean busy=checkBusy();
				if(cursors.size()>0)cursors.get(0).checkBusyState(busy);
				long nowAt=currentTimeMillis();
				if(!busy){
					notBusyAt=nowAt;
					return;
				}
				long busyAt=nowAt-notBusyAt;
				if(busyAt%100==0)trace(".checks: busyAt="+secs(busyAt)+(!blocking?"":(" "+ops.get(0))));
				if(timeSystem>0&&busyAt>timeSystem)
					coupler.handleSystemTimeout(ops.size()>0?ops.get(0):null,worker,blocking);
				else if(busyAt<timeBlock||!blocking||exception!=null)return;
				trace(".checks: busyAt="+secs(busyAt)+" "+worker);
				blocking=false;
			}
		},0,busyMillis);
	}
	public AppWatcher(){
		if(single!=null)throw new IllegalStateException(
				"Only one instance allowed in "+Debug.info(this));
		else single=this;
	}
	/**
	Runs (and possibly allows cancellation of) an {@link WatchableOperation}. 
	<p>Exceptions thrown are passed to the {@link WatcherCoupler} returned by {@link #coupler}.  
	@param op may be created within another {@link WatchableOperation} providing
	this specifies {@link CancelStyle#None}. 
	@return <code>null</code> or value returned by {@link WatchableOperation#doReturnableOperation()}
	 */
	public final Object runWatched(final WatchableOperation op){
		Thread current=currentThread();
		if(!EventQueue.isDispatchThread()){
			Util.printOut("AppWatcher.runWatched: Not event despatch thread="+current);
			exit(-1);
		}
		final Thread callThread=current;
		int callPriority=callThread.getPriority();
		returnable=null;
		final int call=++calls%100;
		final CancelStyle cancel=op.cancelStyle();
		trace(".runWatched: ops="+ops.size()+" @" +call+" "+op+" cancel="+cancel);
		try{
			if(cancel==Dialog||(cancel==Timeout&&(checkTimeouts||coupler.checkTimeouts()))){
				worker=new Thread(traceName+".worker: "+op){
					public void run(){		
						try{
							doOperations(op,call);
						}catch(Exception e){
							exception=e;
						}
						prioritiseBlockThreads(callThread,worker);
						worker=null;
						blocking=false;
						if((cancel==Dialog))cancelBlockingDialog();
						trace(".unblocked: exception="+exception+" worker=",this);
					}
				};
				worker.start();
				do{
					trace(".blocking @"+call +" cancel="+cancel);
					if((cancel==Dialog)){
						if(false&&cursors.size()>0)cursors.get(0).forceSwitch(true);
						openBlockingDialog(op);
					}
					else{
						notBusyAt=currentTimeMillis();
						blocking=true;
					}
					prioritiseBlockThreads(worker,callThread);
					while(blocking);
					if(worker!=null)prioritiseBlockThreads(callThread,worker);
				}while(exception==null&&worker!=null&&worker.isAlive()
						&&coupler.retryCancel(ops));
				if(worker!=null&&worker.isAlive())worker.setPriority(MIN_PRIORITY);
				worker=null;
			}
			else doOperations(op,call);
		}catch(Exception e){
			exception=e;
		}
		if(exception!=null){
			trace(".runWatched: ops="+ops.size()+" exception=",exception);
			if(ops.size()==1||worker!=null||passingException){
				Throwable t=passingException&&exception.getCause()!=null?exception.getCause()
						:exception;
				exception=null;
				passingException=false;
				coupler.handleException(ops,(Exception)t);
				ops.clear();
			}else{
				passingException=true;
				throw false?new RuntimeException(
						"Ops="+Objects.toStringWithHeader(ops.toArray()),exception)
					:new RuntimeException(exception);
			}
		}
		else{
			if(ops.size()==0||op!=ops.get(0))throw new IllegalStateException(
					"Invalid op="+op+" in ops="+ops);
			coupler.operationsCompleted(ops);
			ops.remove(0);
		}
		callThread.setPriority(callPriority);
		notBusyAt=currentTimeMillis();
		trace(".runWatched~ ops="+ops.size()+" @"+call+" returnable="+returnable);
		return returnable;
	}
	private void doOperations(WatchableOperation op,int call){
		ops.add(0,op);
		if(false)trace(".doOperations: @"+call+" ops="+ops.size()+" worker="+worker);
		coupler.operationsStarting(ops);
		returnable=op.callOperations(call);
		if(false)traceDebug(".doOperations~: @"+call+" returnable=",returnable);
	}
	private void prioritiseBlockThreads(Thread up,Thread down){
		boolean complain=false;
		if(complain&&up==null)throw new IllegalArgumentException("Null up for down="+down);
		else if(complain&&down==null)throw new IllegalArgumentException("Null down for up="+up);
		if(up!=null)up.setPriority(MAX_PRIORITY);
		if(down!=null)down.setPriority(MIN_PRIORITY);
		if(false)trace(".prioritiseBlockThreads: up="+up+"\n\tdown="+down);
	}
	protected abstract boolean checkBusy();
	protected void openBlockingDialog(WatchableOperation op){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	protected void cancelBlockingDialog(){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
}