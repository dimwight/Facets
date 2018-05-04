package facets.util;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
/**
Iteration with an index.
 */
public abstract class IndexingIterator<T>{
	final private List<T>items;
	public final int itemCount;
	public IndexingIterator(List<T>items){
		this.items=Collections.unmodifiableList(items);
		itemCount=items.size();
	}
	public IndexingIterator(T[]items){
		this(Arrays.asList(items));
	}
	public void iterate(){
		int at=0,size=this.items.size();
		for(T src:this.items)itemIterated(src,at++);
	}
	protected abstract void itemIterated(T item,int at);
}
