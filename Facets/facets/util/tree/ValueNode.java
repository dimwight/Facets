package facets.util.tree;
import facets.util.Debug;
import facets.util.Objects;
import facets.util.Stateful;
import facets.util.Strings;
/**
{@link DataNode} whose values can be stored and retrieved using keys. 
<p>{@link ValueNode} adds to the functionality of its direct superclass,
and thus indirectly to {@link TypedNode},
the capacity to store and retrieve a range of useful primitives and their arrays. 
<p>Values can be stored and retrieved as key pairs or by indexing into
 {@link #values()}.    
	 */
public class ValueNode extends DataNode{
	public static final String NULL_MARKER="NULL",KEY_EQUALS="=";
	public static final int NO_INT=Integer.MIN_VALUE;
	public static final long NO_LONG=Long.MIN_VALUE;
	final private static long serialVersionUID=3172830810316077503L;
	public ValueNode(String type,String title){
		super(type,title);
	}
	public ValueNode(String type,String title,Object[]contents){
		super(type,title,contents);
	}
	public ValueNode(String type,Object[]contents){
		super(type,UNTITLED,contents);
	}
	final public void deleteValueAt(int pathAt){
		Object[]then=contents();
		if(then.length==0)return;
		int at=0,valueAt=-1;
		for(;valueAt<pathAt;at++){
			if(false)trace(".deleteValueAt: pathAt=",pathAt+" valueAt="+valueAt+" at="+at);
			if(then[at]instanceof TypedNode)continue;
			valueAt++;
		}
		at--;
		if(false)trace(".~deleteValueAt: pathAt=",pathAt+" valueAt="+valueAt+" at="+at);
		Object[]now=new Object[then.length-1];
		for(int i=0,back=0;i<then.length;i++)
			if(i==at)back=1;
			else now[i-back]=then[i];
		setContents(now);
	}
	protected void traceOutput(String msg){
		if(true)super.traceOutput(msg);
	}
	public String toString(){
		Object[]values=false?children():values();
		return super.toString()+(shortStrings?""
				:true&&parent()==null?(" descendants="+Nodes.descendants(this).length)
				:(true?(" children="+children().length+" values="+values.length)
						:(values.length==0?" No values":" ["+
								Objects.toString(values,", ")//.replaceAll(",","\n")
								+"]")));
	}
	/**
	Re-implementation that preserves instance type. 
	@return a new {@link ValueNode} created from the return of {@link DataNode#copyState()}
	 */
	@Override
	public Stateful copyState(){
		DataNode copy=(DataNode)super.copyState();
		return new ValueNode(type(),title(),copy.contents());
	}
	public static String putCheckKey;
	/**
	Stores <code>value </code>under <code>key</code> (appending to 
	<code>contents </code>if not already present) as <code>key</code> concatenated with
	 <code>KEY_EQUALS</code> and the stringification of <code>value</code>.  
	 @param key must be XML-compatible <code>name</code>
	 @param value must be stringifiable by {@link #valueString(Object)}
	 */
	final public void put(String key,Object value){		
		if(key==null||!key.matches("\\w+"))throw new IllegalArgumentException(
				"Null or invalid key=" +key+" for value="+value);
		String values[]=values(),keyString=key+KEY_EQUALS+valueString(value);
		if(key.equals(putCheckKey)){
			trace("["+Debug.id(this)+"].putCheckKey: keyString="+keyString);
			if(false)Debug.printStackTrace(5);
		}
		boolean found=false;
		for(int i=0;i<values.length;i++){
			int equalsAt=values[i].indexOf(KEY_EQUALS);
			if(equalsAt>=0&&values[i].substring(0,equalsAt).equalsIgnoreCase(key)){
				values[i]=keyString;
				found=true;
			}
		}		
		setValues(found?values:Objects.join(String.class,values,
					new String[]{keyString}));
	}
	/**
	Returns stringification of value last stored under <code>key</code>.
	 @return value stored or <code>null</code>.
	 */
	final public String get(String key){
		if(key==null||key.trim().equals(""))throw new IllegalArgumentException(
				"Null or empty key in "+Debug.info(this));
		String[]values=values();
		for(int i=0;i<values.length;i++){
			String check=values[i];
			int equalsAt=check.indexOf(KEY_EQUALS);
			if(equalsAt<0)continue;
			if(check.substring(0,equalsAt).equalsIgnoreCase(key))
				return check.substring(equalsAt+1); 
		}		
		return null;
	}
	final public String getString(int at){
		return values()[at];
	}
	final public String getString(String key){
		String got=get(key);
		return got!=null?got:"";
	}
	final public String getOrPutString(String key,String defaultPut){
		String got=get(key);
		if(got==null)put(key,defaultPut);
		return getString(key);
	}
	final public boolean getBoolean(int at){
		return new Boolean(values()[at]);
	}
	final public boolean getBoolean(String key){
		return new Boolean(get(key));
	}
	final public boolean getOrPutBoolean(String key,boolean defaultPut){
		String got=get(key);
		if(got==null)put(key,defaultPut);
		return getBoolean(key);
	}
	final public void put(String key,boolean value){
		put(key,new Boolean(value));
	}
	final public long getLong(int at){
		return new Long(values()[at]);
	}
	final public long getLong(String key){
		String got=get(key);
		return got!=null?new Long(got):NO_LONG;
	}
	final public long getOrPutLong(int at,long defaultPut){
		String[]values=values();
		if(at>=values.length)putAt(at,defaultPut);
		String got=values()[at];
		return new Long(got);
	}
	final public long getOrPutLong(String key,long defaultPut){
		String got=get(key);
		if(got==null)put(key,defaultPut);
		return getLong(key);
	}
	final public void put(String key,long value){
		put(key,new Long(value));
	}
	final public int getInt(int at){
		return new Integer(values()[at]);
	}
	final public int getInt(String key){
		String got=get(key);
		return got==null?NO_INT:new Integer(got);
	}
	final public int getOrPutInt(String key,int defaultPut){
		String got=get(key);
		if(false)trace(".getOrPutInt: key="+key+" got=",got);
		if(got==null)put(key,defaultPut);
		return getInt(key);
	}
	final public void put(String key,int value){
		put(key,new Integer(value));
		if(false)trace(".put: key="+key+" value="+value+" got=",get(key));
	}
	final public double getDouble(int at){
		return doubleValue(values()[at]);
	}
	final public double getDouble(String key){
		String got=get(key);
		return got==null?Double.NaN:doubleValue(got);
	}
	final public double getOrPutDouble(String key,double defaultPut){
		String got=get(key);
		if(got==null)put(key,defaultPut);
		return getDouble(key);
	}
	final public void put(String key,double value){
		int intVal=(int)value;
		if(value==intVal)put(key,new Integer(intVal));
		else put(key,new Double(value));
	}
	final public int[]getInts(String key){
		String got=get(key);
		return got==null?new int[]{}:Strings.toInts(got);
	}
	final public double[]getDoubles(String key){
		String got=get(key);
		return got==null?new double[]{}:Strings.toDoubles(got);
	}
	final public double[]getDoubles(int at){
		return doublesValue(values()[at]);
	}
	final public void putAt(int at,Object value){
		String values[]=values(),valueString=valueString(value);
		if(at>=values.length)values=Objects.join(String.class,
		    values,new String[]{valueString});
		else values[at]=valueString;
		setValues(values);
	}
	private double[]doublesValue(String value){
		return Strings.toDoubles(value);
	}
	private double doubleValue(String value){
		return new Double(value);
	}
}