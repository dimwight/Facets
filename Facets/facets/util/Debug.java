package facets.util;
import static facets.util.Util.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.StringTokenizer;
/**
Utilities for use during development. 
 */
public final class Debug{
	/** Global flag.*/
	public static boolean natureDebug=true;
	public static boolean memCheck=false,withGc=true;
	/**
	A value that uniquely identifies the object within its class. 
	@return where available {@link Identified#identity()},
	otherwise the truncated {@link Object#hashCode()}.  
	 */
	public static String id(Object o){
		return o==null?"null"
				:o instanceof Identified?("#"+((Identified)o).identity())
		:(" id="+((false?o.hashCode():System.identityHashCode(o))&0xfff));
	}
	/**
		Returns basic information about an object's type and identity. 
		<p>This will be some combination of
	<ul>
		<li>the non-trivial simple class name
		<li>{@link #id(Object)}
		<li>{@link Titled#title()} if available
		</ul>
			 */
	public static String info(Object o){
		if(o==null)return "null";
		if(o instanceof String){
			String text=o.toString();
			int length=text.length();
			return text.substring(0,Math.min(length,60))
					+(": "+(false?("length="+length):id(o)));
		}
		return (false?o.getClass().getSimpleName():helpfulClassName(o))+" "+
			id(o)+(o instanceof Titled?(" "+((Titled)o).title()):false?" "+o:"");
	}
	/**
	Returns an array of <code>info</code>s. 
	 */
	public static String arrayInfo(Object[]array){
		if(array==null)return "null";
		StringBuilder sb=new StringBuilder(info(array)+"["+array.length+"]{\n");
		for(int i=0;i<array.length;i++)
			sb.append((true||array[i]!=null?info(array[i]):"null")
					+(i<array.length-1?"\n":""));
		return sb+"\n}";
	}
	public static String exceptionInfo(Exception e,String title){
		if(e==null)throw new IllegalArgumentException("Null e for title="+title);
		StringBuilder info=new StringBuilder(title+"\n");
		info.append(e+"\n");
		for(StackTraceElement line:Arrays.copyOf(e.getStackTrace(),5))
			info.append(line+"\n");
		info.append(e.getCause()+"\n");
		return info.toString();
	}
	public static String mapInfo(Map map){
		return "entries="+arrayPrintString(map.entrySet().toArray())+
				"\nkeys="+arrayInfo(map.keySet().toArray())+
				"\nkeys="+arrayPrintString(map.keySet().toArray());
	}
	public static void memCheck(String header){
		if(!memCheck)return;
		else if(false)throw new RuntimeException("Not implemented for "+header);
		Runtime rt=Runtime.getRuntime();
		String before=memMbs(rt.freeMemory(),rt.totalMemory());
		if(withGc)System.gc();
		String after=memMbs(rt.freeMemory(),rt.totalMemory());
		Util.printOut(header.trim()+" "+(false&&withGc?("before" +before+", after"):"")+after);
	}
	public static String memMbs(long free,long total){
		return (true?(" "+Util.mbs(total-free)):"")+
				(true?"":(" free="+Util.mbs(free)+(false?"":("/"+Util.mbs(total)))));
	}
	public static String secs(long millis){
		return false?String.valueOf(millis):(sfs(millis/1000d)+"s");
	}
	public static String[]stackTraceLines(Throwable t){
		StackTraceElement[]stackTrace=t.getStackTrace();
		String[]lines=new String[stackTrace.length];
		for(int i=0;i<lines.length;i++)
			lines[i]="at "+stackTrace[i];
		return lines;
	}
	/**
	Prints a useful,truncated stack trace. 
	@param printLines how many lines of the trace to print
	 */
	public static void printStackTrace(int printLines){
		Util.printOut(parseStackTrace(printLines,"xxxxxxxxxxxxxx",false));
	}
	/**
	Prints useful stack trace up to eg a method name. 
	@param stopText occurring in a line stops further lines appearing
	 */
	public static void printStackTrace(String stopText){
		Util.printOut(parseStackTrace(1000,stopText,false));
	}
	public static void printThisTrace(Object src,int printLines){
		Util.printOut(info(src)+":\n"+parseStackTrace(printLines,"xxxxxxxxxxxxxx",false));
	}
	public static void printThisTrace(Object src,String stopText){
		Util.printOut(info(src)+":\n"+parseStackTrace(0,stopText,false));
	}
	public static String[]readTraceLines(Throwable t,int stopLines,String stopText,
			int omitLines){
		ByteArrayOutputStream out=new ByteArrayOutputStream();
		t.printStackTrace(new PrintStream(out));
		StringTokenizer parser=new StringTokenizer(out.toString(),"\n\r\t");
		int limit=stopLines+omitLines;
		ItemList<String>lines=new ItemList(String.class);
		for(int i=0;i<limit&&parser.hasMoreTokens();i++){
			String line=parser.nextToken();
			if(i<omitLines) continue;
			else lines.addItem(line);
			if(line.indexOf(stopText)>0)break;
		}
		return lines.items();
	}
	public static String parseStackTrace(int stopLines,String stopText,boolean noLinks){
		String[]lines=readTraceLines(new Throwable(),stopLines,stopText,4);
		StringBuffer output=new StringBuffer(true?"":"\n");
		for(int i=0;i<lines.length&&lines[i]!=null;i++){
			String append=noLinks?lines[i].replaceAll("at([^\\(]*)\\([^\\)]*\\)","$1")
					:lines[i].replaceAll("^[^A-Z]+([A-Z])","$1");
			output.append(append);
			if(i<lines.length-1)output.append("\n");
		}
		if(false&&stopLines>1)output.append("\n");
		return output.toString()+"\n";
	}
	public static Thread[]getSortedThreads(){
		ArrayList<Thread> threads=new ArrayList(Thread.getAllStackTraces().keySet());
		Collections.sort(threads,new Comparator(){
			@Override
			public int compare(Object o,Object p){
				return o.toString().compareTo(p.toString());
			}
		});
		return threads.toArray(new Thread[]{});
	}
}
