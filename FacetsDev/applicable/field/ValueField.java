package applicable.field;
import static applicable.field.DateField.*;
import static facets.util.tree.ValueNode.*;
import facets.util.Debug;
import facets.util.HtmlFormBuilder;
import facets.util.Titled;
import facets.util.Tracer;
import facets.util.tree.Nodes;
import facets.util.tree.ValueNode;
import java.text.Format;
public abstract class ValueField<T>extends Tracer implements Titled{
	public final String name;
	@Override
	public String title(){
		return name;
	}
	public ValueField(String name){
		this.name=name;
	}
	final public T newValue(ValueNode values){
		String valueKey=valueKey();
		String got=getValue(values,valueKey);
		if(true)return got==null||got.equals(NULL_MARKER)?newNullValue(values)
				:textToValue(got,values);
		for(String each:values.values()){
			String keyPairs[]=Nodes.splitPair(each),key=keyPairs[0];
			if(keyPairs.length!=2||!key.equals(valueKey))continue;
			String text=keyPairs[1].trim();
			return text.equals(NULL_MARKER)?newNullValue(values)
					:textToValue(text,values);
		}
		return newNullValue(values);
	}
	protected String getValue(ValueNode values,String valueKey){
		return values.get(valueKey);
	}
	public void putInputValue(ValueNode values,String text){
		if(text==null)throw new IllegalArgumentException("Null text for "+name);
		T value=parseInputText(text);
		values.put(valueKey(),value.equals(newNullValue(values))?NULL_MARKER
			:formatInputValue(value));
	}
	protected String formatInputValue(T value){
		return value.toString();
	}
	protected T parseInputText(String text){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	protected abstract T textToValue(String text,ValueNode values);
	protected T newNullValue(ValueNode values){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	public TableComparator<T>sorter(boolean sortDown){
		return null;
	}
	public Format format(){
		return null;
	}
	protected String valueKey(){
		return name;
	}
	public String toString(){
		return name;
	}
	final public String shortName(int maxLength){
		return name.toLowerCase().substring(0,Math.min(maxLength,name.length()));
	}
	public static void addNullMarkers(ValueNode values,String[]keys){
		for(String key:keys)if(values.getString(key).trim().equals(""))values.put(key,NULL_MARKER);
		Nodes.sortValues(values);
	}
	public boolean isLocked(){
		return false;
	}
	public int inputCols(){
		return 10;
	}
}
