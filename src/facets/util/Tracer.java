package facets.util;
import java.util.Collection;
/**
Utility superclass that can issue trace messages.
 */
public class Tracer{
	private final String top;
	public static Tracer newTopped(final String top,final boolean live){
		return new Tracer(){
			@Override
			protected void traceOutput(String msg){
				if(live)Util.printOut(top+msg);
			}
		};
	}
	public Tracer(){
		this((String)null);
	}
	public Tracer(Class topClass){
		this(Util.helpfulClassName(topClass));
	}
	public Tracer(String top){
		this.top=top!=null?top:Util.helpfulClassName(this);
	}
	/**
	Outputs complete trace messages to console or elsewhere. 
	<p>Default prepends helpful classname to message.  
	@param msg passed from one of the <code>public</code> methods
	 */
	protected void traceOutput(String msg){
		traceOutputWithClass(msg);
	}
	final public void traceOutputWithClass(String msg){
		Util.printOut(top+msg);
	}
	final public void trace(String msg){
		traceOutput(msg);
	}
	final public void trace(String msg,Throwable t,boolean stack){
		if(stack&=t!=null){
			traceOutput(msg);		
			t.printStackTrace();
		}
		else traceOutput(msg+traceObjectText(t));		
	}
	final public void trace(String msg,Object o){
		traceOutput(msg+traceObjectText(o));		
	}
	final public void trace(String msg,Collection c){
		traceOutput(msg+traceArrayText(c.toArray()));		
	}
	final public void trace(String msg,Object[]array){
		traceOutput(msg+traceArrayText(array));		
	}
	final public void traceDebug(String msg,Object o){
		traceOutput(msg+Debug.info(o));
	}
	final public void traceDebug(String msg,Object[]array){
		traceOutput(msg+(false?Debug.info(array):Debug.arrayInfo(array)));		
	}
	private String traceObjectText(Object o){
		return o==null?null:o.toString();
	}
	private String traceArrayText(Object[]array){
		return Util.arrayPrintString(array);
	}
}