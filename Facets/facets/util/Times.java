package facets.util;
import static facets.util.Debug.*;
import static java.lang.System.*;
import facets.util.app.Events;
/**
Simple timing for profiling and latency checks. 
 */
final public class Times{
	public static final long minute=1000l*60,HOUR=minute*60;
	public static boolean times=false,nanoTime=true;
	public static long resetWait=1000;
  private static long then,start;
	private static boolean restarted;
  private static final boolean debug=false;
  private static long newMillis(){
		return(nanoTime?nanoTime()/1000/1000:currentTimeMillis());
	}
  public static void setResetWait(int wait){
  	if(debug)Util.printOut("Times.setResetWait: wait=",wait);
  	start=newMillis();
		times=true;
		resetWait=wait;
	}
	/**
	The time since the last auto-reset. 
	<p>Interval for reset set by {@link #resetWait}. 
	 */
  public static long elapsed(){
  	long now=newMillis();
  	if(now-then>resetWait){
  		start=now;
  		restarted=true;
  		if(debug)Util.printOut("Times: reset resetWait="+resetWait);
  		if(false)System.gc();
  	}
  	else restarted=false;
		return(then=now)-start;
  }
  /**
	Print {@link #elapsed()} followed by the message. 
	 */
	public static void printElapsed(String msg){
    if(!times){
    	if(!Events.trace){
    		start=then=newMillis();
    		if(debug)Util.printOut("Times.printElapsed: times=",times);
    	}
    	return;
    }
		long elapsed=elapsed();
    String elapsedText=true&&elapsed>5*1000?(Doubles.fixed(elapsed/1000d,1)+"")
    		:(""+elapsed), 
    	toPrint=(restarted?"\n":"")+elapsedText+(msg!=null?":\t"+msg:"");
		if(memCheck)memCheck(toPrint);
		else Util.printOut(toPrint);
  }
}
