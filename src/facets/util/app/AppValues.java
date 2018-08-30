package facets.util.app;
import static facets.util.Debug.*;
import static facets.util.Times.*;
import static facets.util.app.Events.*;
import static facets.util.tree.Nodes.*;
import static facets.util.tree.TypedNode.*;
import facets.util.Debug;
import facets.util.Objects;
import facets.util.TextLines;
import facets.util.Tracer;
import facets.util.Util;
import facets.util.tree.DataConstants;
import facets.util.tree.DataNode;
import facets.util.tree.ExceptionNode;
import facets.util.tree.NodeList;
import facets.util.tree.Nodes;
import facets.util.tree.Nodes.TreeRoot;
import facets.util.tree.TypedNode;
import facets.util.tree.ValueNode;
import facets.util.tree.XmlDocRoot;
import facets.util.tree.XmlPolicy;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;
/**
Can persist/depersist configuration and session state/user preferences. 
<p>{@link AppValues} wraps two {@link ValueNode}s which can be used to 
read, maintain and store values on behalf of a client application. 
 */
public class AppValues extends Tracer{
	/**Type of root node, used by #{@link #storageFile(boolean)}. */
	public static final String TYPE_STATE="state",TYPE_NATURE="nature";
	/**
	Type/path of values node. 
	 */
	public static final String DIR_DEV="facets",KEY_DIR_USER="userDir",
		PATH_APP="app",PATH_DEBUG="debug",PATH_ARGS="args",PATH_RUNS="runs";
	/**
 	Key for setting {@link Debug#natureDebug}. 
	<p>Set to <code>false</code> for release binaries; to <code>true</code> in binary by passing
	 as argument. 
	 */
	public static final String NATURE_DEBUG="natureDebug";
	/** Key for flag specifying instance behaviour. */
	public static final String NATURE_WRITABLE="natureWritable",
		NATURE_RECORD_RUNS="recordRuns",
		KEY_TIMEOUT="timeoutBlock",KEY_TIMEOUT_SYSTEM="timeoutSystem";
	private static final DateFormat DATE_FORMAT=DateFormat.getDateInstance(),
		TIME_FORMAT=DateFormat.getTimeInstance();
	/**
	Global state node for debug values. 
	<p>Created empty, added as child of core state.  
	 */
	public static final ValueNode stateDebug=new ValueNode(PATH_DEBUG,UNTITLED);
	/**
	Simple name of the class passed to the constructor; expected to be the application name. 
	<p>Used by {@link #storageFile(boolean)}. 
	 */
	public final String appName;
	private final Tracer t=Tracer.newTopped(AppValues.class.getSimpleName(),false);
	private final ValueNode nature,state;
	private static AppValues oneOnly;
	private String stateSource;
	/**
	Core constructor. 
	<p>Builds data roots ready for {@link #readValues(String[])}:
	@param nameClass should generally be the client application class; its simple name is
	set as {@link #appName}
	 */
	public AppValues(Class nameClass){
		appName=nameClass.getSimpleName();
		if(false&&oneOnly!=null&&this!=oneOnly)throw new IllegalStateException(
			"Only one values allowed in "+Debug.info(this));
		else oneOnly=this;
		nature=new ValueNode(TYPE_NATURE,UNTITLED);
		state=new ValueNode(TYPE_STATE,UNTITLED);
	}
	public AppValues(AppValues master){
		appName=master.appName;
		nature=master.nature;
		state=master.state;
	}
	/**
	Reads values from (in order) defaults, storage and parameters. 
	<p>Returns without effect after the first call; calls to {@link #nature(String...)}
	and {@link #state(String...)} will fail before this method is called.  
	<p>Type path roots are created before reading for
	<ul>
	<li>{@link #nature}: {@link #PATH_ARGS} 
	<li>{@link #state}: {@link #PATH_DEBUG} and {@link #PATH_APP}. 
	</ul>
	@param args passed from <code>main</code> or constructed from
	applet parameters and accessible as  {@link #args()};
	any member not a <i>key=value</i> pair is interpreted as key for a value 
	set to <code>true</code> unless negated with initial <code>_</code>. 
	 */
	final public void readValues(String...args){
		if(nature.contents().length>0)return;
		boolean hasSystemAccess=hasSystemAccess();
		TextLines.setDefaultEncoding(hasSystemAccess);
		nature.setContents(new Object[]{"readValues=true",new ValueNode(PATH_ARGS,UNTITLED)});
		Object source;
		addNatureDefaults(nature);
		if((source=getClass().getClassLoader().getResource(storageFile(true).getPath()))!=null)
			readSource(source,new XmlDocRoot(nature,XML_POLICY));
		readAdjustedArgs(args(),args);
		readAdjustedArgs(nature(),args);
		state.setContents(new Object[]{stateDebug,new ValueNode(PATH_APP,UNTITLED)});
		addStateDefaults(state);
		if((source=!hasSystemAccess?stateSource:storageFile(false))!=null)
			readSource(source,new XmlDocRoot(state,XML_POLICY));
		adjustValues();
		minimiseTree(nature);
		minimiseTree(state);
		t.trace(": Nature and state values read ");
		updateRuns(true);
	}
	private void readSource(Object source,TreeRoot dataRoot){
		ValueNode values=(ValueNode)dataRoot.tree;
		Object[]defaults=values.contents();
		try{
			dataRoot.readFromSource(source instanceof String?
						TextLines.newBuffer(((String)source).split("\n"))
				:source instanceof File?new TextLines((File)source)
						:new TextLines((URL)source));
		}catch(Exception e){
			boolean alertExceptions=false;
			values.setContents(!alertExceptions?defaults
					:Objects.join(Object.class,defaults,new Object[]{new ExceptionNode(e)}));
		}
		ValueNode merge=new ValueNode(DataConstants.TYPE_DATA,"Merge",defaults);
		mergeContents(merge,values.contents());
		values.setContents(merge.contents());
	}
	/**
	Can set default configuration values.
	<p>Called from {@link #readValues(String[])}; 
	values added are treated as defaults for {@link #nature(String...)}.
	<p>Default implementation is empty.  
	 */
	protected void addNatureDefaults(ValueNode root){}
	/**
	Can set default session/preferences values. 
	<p>Called from {@link #readValues(String[])}; 
	values added are treated as defaults for {@link #state(String...)}.  
	<p>Default implementation is empty.  
	@param root of the {@link #state(String...)} tree
	 */
	protected void addStateDefaults(ValueNode root){}
	/**
	Can these values (and by implication the application) access the file system,
	clipboard etc?  
	@return by default <code>true</code> 
	 */
	public boolean hasSystemAccess(){
		return true;
	}
	/**
	Application configuration values. 
	 <p>Initial contents are set by 
	 <ol>
	 <li>reading the values returned by {@link #addNatureDefaults(ValueNode)}
	 <li>attempting to merge values from an XML <i>.nature</i> file 
	 named from {@link #appName} and accessible as a system resource. 
	 <li>Merging {@link #args()} values to {@link #PATH_ARGS} node
	 </ol>
	 <p>Any value updates are lost after each session.  
	@param typePath specifies a path within the tree to a node which is
	  created if required; empty parameters treated as a reference to the root itself.  
	 */
	final public ValueNode nature(String...typePath){
		return pathValues(nature,typePath);
	}
	/**
	Stores application session state and user preferences. 
	 <p>Initial contents are set by
	 <ol>
	 <li>reading the values returned by {@link #addStateDefaults(ValueNode)}
	 <li>attempting to merge values from an XML <i>.state</i> file 
	 named from {@link #appName} and located in {@link #userDir()}. 
	 </ol>
	 <p>Value updates can be written to the <i>.state</i> file 
	 with {@link #tryWriteValues(String)}. 
	@param typePath specifies a path within the tree to a node which is
	  created if required; empty parameters treated as a reference to the root itself.  
	 */
	final public ValueNode state(String...typePath){
		return pathValues(state,typePath);
	}
	private ValueNode pathValues(ValueNode root,String...path){
		if(nature.contents().length==0)
			throw new IllegalStateException("Values not read in "+Debug.info(this));
		else return path.length==0?root:guaranteedDescendant(root,path);
	}
	/**
	Values created from arguments (or applet parameters) by {@link #readValues(String...)}. 
	<p>Stored as {@link #nature(String...) } with {@link #PATH_ARGS}. 
	 */
	public final ValueNode args(){
		return nature(PATH_ARGS);
	}
	/**
	May specify acceptable arguments or possible applet parameters. 
	@return by default an empty array
	 */
	public String[]argumentKeys(){
		return new String[]{};
	}
	/**
	Encapsulates checking within {@link #argumentKeys()}. 
	@param key to check
	 */
	final public boolean hasArgument(String key){
		for(String arg:argumentKeys())
			if(arg.equals(key))return true;
		return false;
	}
	/**
	Can refresh class variables etc from state values. 
	<p>Called from {@link #readValues(String[])} once values have been read;
	 defined <code>public</code> to allow calls when values are changed.
	<p>Default implementation sets {@link Debug#natureDebug} and class values
	in {@link Events} and {@link AppWatcher}. 
	 */
	public void adjustValues(){
		boolean gotDebug=nature.get(NATURE_DEBUG)!=null;
		if(gotDebug)natureDebug=nature.getBoolean(NATURE_DEBUG);
		ValueNode debug=state(PATH_DEBUG);
		trace=debug.getBoolean(KEY_TRACE);
		events=debug.getBoolean(KEY_EVENTS);
		times=trace&&debug.getBoolean(KEY_TIMES);
		memCheck=trace&&(events||debug.getBoolean(KEY_MEM));
		resetWait=debug.getOrPutInt(KEY_TIMES_RESET,(int)resetWait);
		if(false)Util.printOut("AppValues.adjustValues: times="+times);
	}
	private static final XmlPolicy XML_POLICY=new XmlPolicy(){
		protected boolean treeAsXmlRoot(){
			return true;
		}
		protected boolean handleReadExceptions(){
			return false;
		} 
		protected ValueNode getTitleAttributeNames(){
			return newTitleAttributeNames("",new String[]{"Date=day"});
		}
	};
	/**
	Attempts to persist current values in XML format. 
<ul>
	<li>{@link #state(String...)} tree is minimized with {@link Nodes#minimiseTree(DataNode)}
	<li>If {@link #NATURE_RECORD_RUNS} is <code>true</code>, adds a child to the 
	{@link #PATH_RUNS} root child.
	<li>Will only write out the contents 
	of {@link #nature(String...)} if {@link #NATURE_WRITABLE} is <code>true</code>;
	</ul>
	 */
	final public void tryWriteValues(String msg){
		minimiseTree(state);
		updateRuns(false);
		if(!hasSystemAccess())t.trace("No system access to write values");
		else{
			File storage=storageFile(false);
			new XmlDocRoot(state,XML_POLICY).writeToSink(storage);
			String msgCore="State values written to "+storage.getAbsolutePath();
			if(!msg.trim().equals(""))Util.printOut(msg+msgCore);
			else t.trace(".tryWriteValues: msgCore=",msgCore);
			if(!nature().getBoolean(NATURE_WRITABLE))return;
			storage=storageFile(true);
			new XmlDocRoot(nature,XML_POLICY).writeToSink(storage);
			t.trace("Nature values written to "+storage.getAbsolutePath());
		}
	}
	final public void setStateSource(String text){
		this.stateSource=text;
	}
	final public String newStateSource()throws IOException{
		TextLines lines=TextLines.newBuffer();
		if(false)lines.writeLines(""+new Date());
		else new XmlDocRoot(state,XML_POLICY).writeToSink(lines);
		return lines.readLinesString();
	}
	private void updateRuns(boolean start){
		if(!nature().getBoolean(NATURE_RECORD_RUNS))return;
		DataNode runs=state(PATH_RUNS);
		TypedNode previous[]=runs.children(),
			latest=previous.length==0?null:previous[previous.length-1];
		ValueNode run=start||latest==null?new ValueNode(DataConstants.TYPE_DATE,
				DATE_FORMAT.format(new Date()))
			:(ValueNode)latest;
		String timeValue=TIME_FORMAT.format(new Date());
		if(run!=latest){
			appendChild(runs,run);
			run.put("start",timeValue);
		}
		else run.put("stop",timeValue);
	}
	/**
	Return rows of values each headed by its key. 
	<p>Default is invalid stub. 
	 */
	public Object[][]decorationValues(){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	protected static Object[][]joinDecorations(Object[][]head,Object[][]tail){
		return Objects.join(Object[].class,head,tail);
	}
	private File storageFile(boolean forNature){
		return forNature?new File(appName+"."+TYPE_NATURE+".xml")
			:new File(userDir(),appName+"."+TYPE_STATE+".xml");
	}
	/**
	Writable file system location where the application can store data. 
	<p>Used to store {@link #state(String...)} values; assumed to be available
	for any other private application data. 
	@return by default folder named {@link #DIR_DEV} beneath <i>user.home</i>
	 */
	public static File userDir(){
		String userDir=System.getProperty(KEY_DIR_USER);
		return Util.getDir(new File(System.getProperty("user.home")),
				userDir!=null?userDir:DIR_DEV);
	}
	/** 
	Reads value pairs from the console and writes them to XML. 
	<p>Output files are <i>AppValues.[nature|state].xml</i>; 
	implementations of {@link #addNatureDefaults(ValueNode)} and
	{@link #addStateDefaults(ValueNode)} demonstrate techniques for 
	constructing and modifying {@link ValueNode}s. 
	@param args read by {@link #readValues(String[])}
	 */
	static void main(String[]args){
		AppValues values=new AppValues(AppValues.class){
			protected void addStateDefaults(ValueNode root){
				super.addStateDefaults(root);
				root.put("aFlag",true);
				root.put("intArray",new int[]{1,2,3,4});
				if(false)new NodeList(root,true).add((TypedNode)nature().copyState());
			}
		};
		values.readValues(args);
		values.tryWriteValues("");
	}
	public static ValueNode newFromArgs(String[]args){
		AppValues values=new AppValues(AppValues.class);
		values.readValues(args);
		return values.args();
	}
}
