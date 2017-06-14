package facets.util.tree;
import facets.util.Debug;
import facets.util.Objects;
import facets.util.Tracer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
public abstract class ValueMap<V>extends HashMap<String,V>{
	private static final String SEPARATOR=",";
	public static final String TAIL_KEYS="_keys",TAIL_VALUES="_values";
	private final Tracer t=Tracer.newTopped(getClass().getSimpleName(),false);
	private final ValueNode node;
	private final String keyKeys,keyValues;
	private ValueMap(ValueNode node,String keyTop){
		this.node=node;
		keyKeys=keyTop+TAIL_KEYS;
		keyValues=keyTop+TAIL_VALUES;
		String keyText=node.getString(keyKeys),valueText=node.getString(keyValues);
		if(keyText.equals("")||valueText.equals(""))return;
		String[]keys=keyText.split(SEPARATOR),values=valueText.split(SEPARATOR);
		if(keys.length==values.length)
			for(int i=0;i<keys.length;i++)put(keys[i],decodeValue(values[i]));
	}
	@Override
	public V get(Object key){
		V got=super.get(key);
		return got!=null?got:decodeValue(null);
	}
	public abstract V decodeValue(String v);
	public void updateNode(){
		List keys=new ArrayList(),values=new ArrayList();
		Set<Map.Entry<String,V>>entries=entrySet();
		for(Map.Entry<String,V>entry:entries){
			keys.add(entry.getKey());
			values.add(entry.getValue());
		}
		node.put(keyKeys,Objects.toString(keys.toArray(),SEPARATOR));
		node.put(keyValues,Objects.toString(values.toArray(),SEPARATOR));
		t.trace(".updateNode: ",this);
	}
	public static ValueMap<Integer>forIntegers(ValueNode node,String keyTop){
		return new ValueMap<Integer>(node,keyTop){
			@Override
			public Integer decodeValue(String v){
				return v==null?0:Integer.valueOf(v);
			}
		};
	}
	@Override
	public String toString(){
		return Debug.info(this)+"\n\tkeys="+node.get(keyKeys)+"\n\tvalues="+node.get(keyValues);
	}
}