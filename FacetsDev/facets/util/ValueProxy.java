package facets.util;
import facets.util.tree.DataNode;
import facets.util.tree.Nodes;
import facets.util.tree.ValueNode;
/**
Reads and writes contents of a {@link Stateful} source via a flat <code>Object[]</code>.
<p>Source will often be a {@link ValueNode}.  
 */
public abstract class ValueProxy extends Tracer implements Titled{
	@Override
	public String title(){
		return "["+Debug.info(source)+"]";
	}
	public final Stateful source;
	private Object[]values={};
	public ValueProxy(Stateful source){
		if((this.source=source)==null)
			throw new IllegalArgumentException("Null source in "+Debug.info(this));
	}
	final public Object get(int at){
		if(values.length==0)values=lazyValues();
		if(at>=values.length)throw new IllegalStateException(
				"Can't get at=" +at+" from values="+values.length+
				" in \n"+Debug.info(this));
		Object value=values[at];
		if(value==null)throw new IllegalStateException(
				"Null value in "+Debug.info(this));
		else return value;
	}
	final public void put(int at,Object value){
		if(values.length==0)get(at);
		if(value==null)throw new IllegalArgumentException("Null value in "+Debug.info(this));
		else if(values[at].getClass()!=value.getClass())
			throw new IllegalArgumentException("Wrong value class in "+Debug.info(this));
		else values[at]=value;
	}
	final public int valueCount(){
		return values.length;
	}
	final public void updateSource(){
		applyValuesToSource(values);
	}
	protected abstract Object[]lazyValues();
	protected void applyValuesToSource(Object[]values){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	public String toString(){
		return Debug.info(this);
	}
	public static ValueProxy sourceProxy(ValueProxy[]proxies,Object selected){
		for(int i=0;i<proxies.length;i++)
			if(proxies[i].source==selected)return proxies[i];
		throw new IllegalArgumentException(
				selected+" not found in "+Objects.toStringWithHeader(proxies));
	}
	public boolean equals(Object o){
		return stateString().equals(((ValueProxy)o).stateString());
	}
	public String stateString(){
		return source instanceof DataNode?Nodes.treeString((DataNode)source):source.toString();
	}
	public static String stateString(ValueProxy[]array){
		StringBuilder str=new StringBuilder();
		for(ValueProxy each:array)str.append(each.stateString());
		return str.toString();
	}
}
