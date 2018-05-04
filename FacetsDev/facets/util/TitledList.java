package facets.util;
import java.util.HashMap;
public class TitledList<T extends Titled>extends ItemList<T>{
	public TitledList(Class<T>type){
		super(type);
	}
	public TitledList(T[]elements){
		super(elements);
	}
	final public T titled(String title){
		return getTitled().get(title);
	}
	protected HashMap<String,T>getTitled(){
		HashMap<String,T>titled=new HashMap();
		for(T t:this)titled.put(t.title(),t);
		return titled;
	}
}
