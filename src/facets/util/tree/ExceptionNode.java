package facets.util.tree;
import facets.util.Debug;
import facets.util.Strings;
import facets.util.Util;
/**
Wraps an exception in a {@link DataNode}.
<p>{@link ExceptionNode} enables the graceful handling of almost
any exception thrown during construction of a tree of {@link TypedNode}s. 
The exception can be caught by returning it wrapped in an {@link ExceptionNode},
allowing execution to continue while ensuring that the exception is recorded. 
<p>Typically the {@link ExceptionNode} is eventually displayed
in a tree viewer, facilitating debugging. 
 */
final public class ExceptionNode extends DataNode{
	public static final Object TYPE="Exception";
	/**
	 If set to <code>true</code>, the exception passed to the constructor
	 is rethrown.  
	 */
	public static boolean throwExceptions=true,alertExceptions=false;
	public ExceptionNode(Exception e){
		this(e,null);
	}
  public ExceptionNode(Exception e,TypedNode[]detail){
    super("Exception",e.getClass().getSimpleName());
    String[]trace=Debug.readTraceLines(e,20,"",0);
    if(throwExceptions)throw new RuntimeException
			("\n"+Strings.linesString(trace),e);
    else if(alertExceptions)
    	Util.printOut("ExceptionNode: ",true?e:trace);
    setContents(detail==null?(Object[])trace:detail);
  }
}
