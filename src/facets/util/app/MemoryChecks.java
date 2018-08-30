package facets.util.app;
import static facets.util.Debug.*;
import static facets.util.Util.*;
import static facets.util.app.AppValues.*;
import facets.util.Debug;
import facets.util.Times;
import facets.util.Tracer;
import facets.util.Util;
import facets.util.tree.ValueNode;
import java.util.Timer;
import java.util.TimerTask;
/**
Checks memory availability, especially for use by 
{@link ProvidingCache}.
 */
public class MemoryChecks extends Tracer{
	public static final int maxMemMbDefault=200;
	public static final String NATURE_MEM_MB="memMaxMb",ARG_TRACE="traceMem";
	private final static Runtime rt=Runtime.getRuntime();
	private static final int _setMax=10*MB;
	private long maxMem,checkedFree,total,free,freeThen=-1*MB;
	private static Timer checker=new Timer(MemoryChecks.class.getSimpleName());
	private static TimerTask check;
	private final boolean timing=false&&Times.times;
	private final AppValues values;
	private final boolean traceMem;
	public MemoryChecks(AppValues values){
		if(false)throw new RuntimeException("Disabled in "+Debug.info(this));
		if((this.values=values)==null)throw new IllegalArgumentException(
				"Null values in "+Debug.info(this));
		ValueNode nature=values.nature();
		maxMem=nature.getOrPutInt(NATURE_MEM_MB,maxMemMbDefault)*MB;
		traceMem=nature.getBoolean(ARG_TRACE);
		startChecks();
	}
	@Override
	protected void traceOutput(String msg){
		if(!traceMem)return;
		String text=MemoryChecks.class.getSimpleName()+": "+msg;
		if(timing)Times.printElapsed(text);
		else Util.printOut(text);
	}
	private void startChecks(){
		final int period=3;
		checker.schedule(check=new TimerTask(){
			private int grain=10,traces;
			private long traceThen=freeThen;
			@Override
			public void run(){
				updateStatus();
				if(timing)trace(newStatusText());
				rt.gc();
				updateStatus();
				if(false&&(maxMem<total||free<headroomMb((int)(maxMem/MB))*MB))setAndStoreMaxSafe();
				updateStatus();
				if(false)trace(".run: traces="+traces+" "+traceThen/MB+"-"+checkedFree/MB);
				if(timing||traceThen/MB/grain!=checkedFree/MB/grain||++traces%(10/period)==0){
					traces=0;
					traceThen=checkedFree;
					if(true)trace(newStatusText());
				}
				freeThen=free;
			}
		},0*1000,period*1000);
	}
	void pauseChecks(boolean on){
		if(check!=null)check.cancel();
		if(!on)startChecks();
	}
	long checkFree(String debugMsg){
		updateStatus();
		if(checkedFree<10*MB){
			trace("rechecking for "+newStatusText());
			rt.gc();
			updateStatus();
		}
		trace(newStatusText()+debugMsg);
		return checkedFree;
	}
	private void updateStatus(){
		total=rt.totalMemory();
		free=rt.freeMemory();
		long headroom=headroomMb((int)(maxMem/MB))*MB;
		checkedFree=total>=maxMem&&free<maxMem-headroom?0:(maxMem-total+free-headroom);
	}
	protected int headroomMb(int maxMemMb){
		return 20;
	}
	private String newStatusText(){
		return memMbs(free,total)+
		(false?(" maxMem="+Util.mbs(maxMem)+" headroom="+headroomMb((int)(maxMem/MB))):"")
			+" checkedFree="+Util.mbs(checkedFree);
	}
	public static void checkStatus(String msg){
		Util.printOut(msg+memMbs(rt.freeMemory(),rt.totalMemory()));
	}
	private void doStartupProbe(){
		boolean slow=maxMem==0;
		setAndStoreMaxSafe();
		while(freeThen/MB!=free/MB&&free>_setMax){
			freeThen=free;
			updateStatus();
			trace("Probing: "+newStatusText());
			if(maxMem<total)maxMem=total;
			updateStatus();
			int probe=(int)(checkedFree/100*98/MB);
			try{
				byte[][]allocate=new byte[probe][MB];
			} 
			catch(OutOfMemoryError e){
				System.err.println(e+": probe="+probe);
				rt.exit(-1);
			}
			if(slow)rt.gc();
			updateStatus();
			trace("Probed: probe="+probe+" "+newStatusText());
			slow|=free>_setMax;
		}
		setAndStoreMaxSafe();
	}
	private void setAndStoreMaxSafe(){
		values.state(PATH_APP).put(NATURE_MEM_MB,(int)((maxMem=total)/MB));
		values.tryWriteValues(true?""
			:("setMaxSafeMb="+values.state(PATH_APP).getInt(NATURE_MEM_MB)+": "));
	}
	static void main(String[]args){
		final AppValues values=new AppValues(MemoryChecks.class){
			protected void traceOutput(String msg){
				if(false)traceOutputWithClass(": "+msg);
			}
		};
		values.readValues(args);
		new MemoryChecks(values).doStartupProbe();
		System.exit(0);
	}
}