package facets.util.app;
import static facets.util.Debug.*;
import facets.util.Debug;
import facets.util.Times;
import facets.util.Util;
import facets.util.tree.Nodes;
import facets.util.tree.TypedNode;
import facets.util.tree.ValueNode;
import java.util.Timer;
import java.util.TimerTask;
/**
Event tracing. 
 */
public final class Events{
	public static boolean events=true,trace=false;
	private static final boolean buildTimes=false;
	final public static String KEY_TRACE="trace",KEY_EVENTS="events",KEY_TIMES="times",
		KEY_TIMES_RESET="timesReset",KEY_ESTIMATE="buildEstimate",KEY_MEM="memCheck",
		KEY_FILTERS="filters",
		FILTERS[]={"Cursor","Notified","Retargeting","Selection","Retargeted","Refreshed",
			"Retrieved","Created","Attached","Storing"};
	public static final ValueNode DEFAULT_FILTERS=new ValueNode(KEY_FILTERS,
			TypedNode.UNTITLED);
	static{
		Object[]keyPairs=new String[FILTERS.length];
		for(int i=0;i<keyPairs.length;i++)keyPairs[i]=FILTERS[i]+"=true";
		DEFAULT_FILTERS.setContents(keyPairs);
	}
	static Events.EventTracer buildMonitor;
	static Timer timer;
	private static boolean canRestart=true;
	/**
	Can be notified of build etc messages.
	<p>An {@link EventTracer} passed to {@link Events#setBuildMonitor(EventTracer)}
	will be be updated with any message starting with "\gt>"  
	created with {@link Events#traceEvent(String)}.
	 */
	public static abstract class EventTracer{
		protected abstract void update(String msg,int elapsed);
		protected abstract void setBuildEstimate(int millis);
	}
	public static void setBuildMonitor(EventTracer monitor){
		buildMonitor=monitor;
		monitor.setBuildEstimate(AppValues.stateDebug.getInt(KEY_ESTIMATE));
	}
	public static void traceEvent(String msg){
		String firstWord=msg.replaceAll("(?s)~?(\\w+).*","$1"),
			noDetail=true?null:msg.replaceAll(
				"([A-Z][^A-Z]+).*","$1").replaceAll("\\b for\\b","").replaceAll(
				"\\b in\\b","").replaceAll("\\b from\\b","")
				.replaceAll("\\b to\\b","").trim();
		if(noDetail!=null&&noDetail.startsWith(">")){
			noDetail=noDetail.substring(1);
			if(timer!=null)timer.cancel();
			final int elapsed=true?0:(int)Times.elapsed();
			(timer=new Timer()).schedule(new TimerTask() {
				public void run(){
					AppValues.stateDebug.put(KEY_ESTIMATE,elapsed);
					timer.cancel();
				}			
			},2000);
			if(buildMonitor!=null)buildMonitor.update(noDetail,elapsed);
		}
		else if(!trace)return;
		ValueNode filters=AppValues.stateDebug==null?DEFAULT_FILTERS:Nodes.guaranteedChild(
				AppValues.stateDebug,KEY_FILTERS);
		if(events&&filters!=null&&!filters.getBoolean(firstWord)){
			long elapsed=Times.elapsed();
			String time=(elapsed==0&&canRestart?"\n":"")+(elapsed>10000?elapsed/1000+"s":elapsed);
			msg=msg.replaceAll("^>(.+)","$1");
			canRestart=false&&elapsed>0;
			if(memCheck)memCheck(msg);
			else Util.printOut(time+" "+msg);
		}
	}}
