package facets.util;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
/**
An array-friendly {@link List}.  
<p>{@link ItemList} extends {@link ArrayList} with methods that encapsulate
the complications of adding and extracting simple arrays from a {@link List}.
<p>The problem of constructing generic arrays is circumvented by passing in the 
storage type during construction. Run-time checking protects against the unlikely
error of passing in the wrong type; compile-time checking of the generics
type protects against more subtle code weaknesses. 
<pre>
ItemList&ltInteger&gt ints = new ItemList(Integer.class);
ints.addItems(1,2,3);
ints.addItem(4);
Integer[] items = ints.items();
</pre>
 */
public class ItemList<T>extends ArrayList<T>{
	/**
	{@link ItemList} that can traverse a generic tree.
	 */
	public static abstract class TreeItems<C>extends ItemList<C>{
		public TreeItems(Class itemType,C root){
			super(itemType);
			addAllToList(root);
		}
		private void addAllToList(C parent){
			addItem(parent);
			C[]children=getChildren(parent);
			if(children==null) return;
			for(int i=0;i<children.length;i++)
				addAllToList(children[i]);
		}
		protected abstract C[]getChildren(C parent);
	}
  private static int lists;
  protected final static int defaultSize=25;
  public final Class itemType;
  /**
	Unique constructor.  
	<p>Sets initial size as specified by class constant <code>defaultSize</code>.  
@param itemType - the type of object to be allowed in the list
	 */
	public ItemList(Class itemType){
  	super(defaultSize);
    lists++;
    this.itemType=itemType;
  }
  public ItemList(T[]src){
  	this(src[0].getClass());
  	addItems(src);
	}
	/**
Add <code>item</code> which must be non-<code>null</code> and of the type passed to 
the constructor. 
<p>Wraps {@link List#add(Object)}.
  */
  final public void addItem(T item){
    if(item==null)throw new IllegalArgumentException("Null item in "+Debug.info(this));
    checkType(item);
    add(item);
  }
	/**
 Add <code>items</code> which must be an array of the type passed to the constructor. 
<p>Wraps {@link List#addAll(Collection)}.
    */
  final public void addItems(T...items){
  	if(items.length==0)return;
  	checkType(items[0]);
  	addAll(Arrays.asList(items));
  }
  /**
Returns an array of the storage type passed to the constructor. 
<p>Wraps {@link List#toArray(Object[]) )}.
	 */
	final public T[]items(){
		return toArray((T[])Array.newInstance(itemType,0));
	}
	private void checkType(Object item){
		if(!itemType.isInstance(item))
	  	throw new IllegalArgumentException(item.getClass().getName()+
	  			" should be "+itemType.getName());
	}
	static void main(String[]args){
		ItemList<Integer>ints=new ItemList(Integer.class);
		ints.addItems(new Integer[]{1,2,3});
		ints.addItem(4);
		Integer[]items=ints.items();
	}
	private Class findCommonItemType(){
  	if(true)throw new RuntimeException("Untested in "+Debug.info(this));
  	final int size=size(); 
    Class commonClass=null,checkClass=null;
    Object[]elements=toArray();
    int i=0;for(;i<size;i++)if(elements[i]!=null){
      checkClass=elements[i].getClass();
      for(int j=0;j<size;j++)
        if(elements[j]==null)continue;
        else if(!checkClass.isAssignableFrom(elements[j].getClass()))break;
    }
    if(i<size)return null;
    if(commonClass==null)throw new RuntimeException("No commonClass in "+this);
    return commonClass;
  }
  private Object[]uniqueClassItems(){
  	if(true)throw new RuntimeException("Untested in "+Debug.info(this));
    for(int i=0;i<size();i++)for(int j=i+1;j<size();j++)
      if(get(j).getClass()==get(i).getClass())remove(j--);
    return items();
  }
}
