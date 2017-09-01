package facets.util.app;
import static facets.util.app.Events.*;
import facets.util.Debug;
import facets.util.Times;
import facets.util.Tracer;
import facets.util.Util;
import facets.util.app.BusyCursor.BusySettable;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
/**
Sets the application busy cursor. 
<p>A {@link BusyCursor} passed to {@link AppWatcher#pushCursor(BusyCursor)}
will call {@link #setCursors(boolean, Set)} whenever the {@link AppWatcher} senses significant
change in the application's busy state.
<p>Concrete instances must implement {@link #setCursors(boolean,Set)} for a particular
combination of surface (eg application or dialog) and GUI toolkit.  
 */
public abstract class BusyCursor extends Tracer{
	private final static int onWait=150,offWait=250;
	private static Timer waits;
	private static int calls;
	private final Set<BusySettable>settables=new HashSet();
	private boolean state,busyThen;
	private TimerTask waiter;
	public interface BusySettable{}
	public BusyCursor(){
		if(waits!=null)waits.cancel();
		waits=new Timer("BusyCursor.waits");
		forceSwitch(false);
	}
	final protected void traceOutput(String msg){
		if(true)return;
		msg=(true?Debug.info(this):BusyCursor.class.getSimpleName())+msg;
		if(false)Util.printOut(msg);
		else Times.printElapsed(msg);
	}
	final void forceSwitch(boolean busy){
		if(false)return;
		trace(newCallMsg(calls++,"forceSwitch",busy));
		if(waiter!=null)waiter.cancel();
		if(busy!=state)switchWait(busy);
		busyThen=busy;
	}
	final void checkBusyState(boolean busy){
		if(busyThen==busy)return;
		if(busyThen&&!busy)traceEvent(">Cursor not busy");
		busyThen=busy;
		final int call=calls++;
		trace(newCallMsg(call,"checkBusyState",busy));
		if(state==busy&&waiter==null)return;
		else if(state!=busy&&waiter==null)switchWait(busy);
		else if(state==busy&&waiter!=null)waiter.cancel();	
		trace(newCallMsg(call,"~checkBusyState",busy));
	}
	private void switchWait(final boolean busy){
		final int call=calls++;
		try{
			waits.schedule(waiter=new TimerTask(){
				public void run(){
					if(false&&this!=waiter)throw new IllegalStateException(
							"Different waiters in "+Debug.info(this));
					trace(".waiter.run: ",this);
					int call=calls++;
					trace(newCallMsg(call,"SWITCH CURSOR ",busy));
					if(waiter!=null)waiter.cancel();
					if(busy)addSettables(settables);
					setCursors(state=busy,settables);
					if(!busy)settables.clear();
					if(trace&&busy)traceEvent("Cursor switched busy="+state);
					else trace(newCallMsg(call,"~switchWait ",busy));
				}
				public boolean cancel(){
					trace(".waiter.cancel: ",this);
					boolean cancel=super.cancel();
					waiter=null;
					return cancel;
				}
				public String toString(){
					return busy+" #"+call;
				}
			},busy?onWait:offWait);
		}catch(Exception e){
			Util.printOut("BusyCursor.switchWait: e=",e);
		}
	}
	protected abstract void addSettables(Set<BusySettable>settables);
	protected abstract void setCursors(boolean busy,Set<BusySettable> settables);
	private String newCallMsg(int call,String in,boolean busy){
		return " #"+call+" "+in+": busy="+busy+" state="+state+" waiter="+waiter;
	}
}