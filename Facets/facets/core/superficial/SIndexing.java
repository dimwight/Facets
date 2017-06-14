package facets.core.superficial;
import facets.util.Debug;
import facets.util.ItemList;
import facets.util.Objects;
import facets.util.Times;
import facets.util.Titled;
import facets.util.Tracer;
import facets.util.Util;
import facets.util.tree.TypedNode;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
/**
{@link STarget} representing one or more indices into a list of items. 
<p>{@link SIndexing} represents a list of items to be exposed 
  to user view and control in the surface, together with an index/indices 
  into that list; application-specific mechanism and policy can be defined in 
	a {@link facets.core.superficial.SIndexing.Coupler}. 
 */
final public class SIndexing extends TargetCore{
	/**Allows an indexing to be functionally empty. */
	public static final Object[]NO_INDEXABLES={"Not indexable"};
	private static final String NOT_INDEXED="Not indexed";
	public static void iterate(SIndexing i,boolean forward){
		Coupler coupler=i.coupler;
		Object[]titles=coupler.newIndexableTitles(i);
		boolean cycles=coupler.canCycle(i);
		int then=i.index(),now=forward?
			then<titles.length-1?then+1:cycles?0:then
		  :then>0?then-1:cycles?titles.length-1:then;
		i.setIndex(now);
	}
	/**
	Connects an {@link SIndexing} to the application. 
	<p>A {@link Coupler} supplies client-specific mechanism and 
	  policy for an {@link SIndexing}. 
	 */
	public static class Coupler implements TargetCoupler{
		/**
		Returns strings to represent the indexables.  
		<p>These will appear in facet lists, or as labels for menu items or 
		radio buttons. 
		<p>The default returns titles based on the type of the {@link SIndexing}'s 
		<code>indexables</code>:
		<ul>
		<li>if they are {@link String}s, returns them</li> 
		<li>if they are {@link facets.util.Titled}s, returns their <code>title</code>  properties</li> 
		<li>otherwise, returns their <code>toString</code> properties</li> 
		<li>if they the {@link SIndexing#NO_INDEXABLES} constant, returns an 
		empty {@link String}[].
		</ul> 		 
		 */
		public String[]newIndexableTitles(SIndexing i){
		  Object[]indexables=i.indexables();
			if(indexables==NO_INDEXABLES)return new String[]{};
			else if(indexables instanceof String[])return(String[])indexables;
		  String[]titles=new String[indexables.length];
		  boolean titled=indexables instanceof Titled[];
		  for(int t=0;t<titles.length;t++)
		    titles[t]=titled?((Titled)indexables[t]).title():
		      indexables[t].toString();
		  return titles;
		}
		/**
		Returns an array defining the 'enabled' state of each item. 
		<p>May be needed for menu items or radio buttons. 
		Default implementation returns states to match <code>i</code>.
		 */
		public boolean[]liveStates(SIndexing i){
		  boolean lives[]=new boolean[newIndexableTitles(i).length],
		  	live=i.isLive();
		  for(int e=0;e<lives.length;e++)lives[e]=live;
		  return lives;
		}
		/**
		Called whenever an index changes. 
		 */
		public void indexSet(SIndexing i){}
		/**
		Can return dynamic indexables. 
		<p>Called by {@link SIndexing#indexables()} if none set during construction. 
		@return a non-empty array
		 */
		public Object[]getIndexables(){
			throw new RuntimeException("Not implemented in "+Debug.info(this));
		}
		/**
		Returns titles for iterating buttons or menu items.  
    <p>Default implementation returns "Previous","Next". 
    */
		public String[]iterationTitles(SIndexing i) {
			return new String[]{"Previous|\u25c4","Next|\u25ba"};
		}
		/**
		Can the indexing loop round when iterating through its indexables?
		 <p>Default returns <code>false</code>.
		 */
		public boolean canCycle(SIndexing i){return false;}
	}
	/**
	The <code>Indexing.Coupler</code> passed to the core constructor. 
	 */
	public final Coupler coupler;	
	private int[]indices=null;
	private transient Object[]indexables,indexings;  
	/**
	Core constructor. 
	<p>Declared visibly for documentation purposes only. 
	 @param title passed to superclass
	 @param indexables the objects to be indexed; may not be empty but 
	 may be <code>null</code> in which case they are 
	 read dynamically from the coupler
	 @param coupler supplies application-specific mechanism and policy
	 */
	protected SIndexing(String title,Object[]indexables,Coupler coupler){
		super(title);this.coupler=coupler;
		if(indexables!=null&&indexables.length==0)
			throw new IllegalArgumentException("Null or empty indexables in "+Debug.info(this));
		this.indexables=indexables;
	}
	/**
	Convenience constructor setting initial index. 
	 @param index the initial index
	 */
	public SIndexing(String title,Object[]indexables,int index,Coupler coupler){
		this(title,indexables,coupler);
		setIndex(index);
	}
	/**
	Convenience constructor setting initial indexed. 
	 @param toIndex the object to have the initial index
	 */
	public SIndexing(String title,Object[]indexables,Object toIndex,Coupler coupler){
		this(title,indexables,coupler);
		setIndexed(toIndex);
	}
	/**
	Convenience constructor forcing dynamic read of indexables. 
	 */
	public SIndexing(String title,Coupler coupler){
		this(title,null,coupler);
		setIndex(0);
	}
  /**
	The first index into the <code>indexables</code>. 
		 */
	public int index(){
		if(indices==null||indices.length==0)throw new IllegalStateException(
				"Null or empty indices in "+Debug.info(this));
		return indices[0];
	}
  /**
	The indices last set into the <code>indexables</code>. 
		 */
	public int[]indices(){
		if(indices==null)throw new IllegalStateException(
				"Null indices in "+Debug.info(this));
		return indices;
	}
	/**
	Sets the index to that of the item passed.
	<p>Complains if the item is not a member of <code>indexables</code>.  
		 */
  public void setIndexed(Object toSet){
    Object[]indexables=indexables();
    for(int i=0;i<indexables.length;i++)
      if(indexables[i].equals(toSet)) {
      	setIndex(i);
      	return;
      }
    throw new IllegalStateException(Debug.info(toSet)+"\n\t in "+Debug.info(this)
          +" not indexed in: \n"+Debug.arrayInfo(indexables));
  }
  /**
	The items exposed to indexing. 
	<p>These will either have been set during construction or be read 
	dynamically from the coupler. 
		 */
	public Object[]indexables(){
		Object[]indexables=this.indexables!=null?this.indexables
				:coupler.getIndexables();
	  if(indexables==null)throw new IllegalStateException(
	  		"Null indexables in "+this);
	  if(indexables.length==0)return NO_INDEXABLES;
	  if(new HashSet(Arrays.asList(indexables)).size()!=indexables.length)
	  	throw new IllegalStateException("Duplicate indexables in "+Debug.info(this));
	  else return indexables;
	}
	/**
	Sets a single index into the <code>indexables</code>. 
		 */
	public void setIndex(int index){
		setIndices(new int[]{index});
	}
	/**
	Sets indices into the <code>indexables</code>. 
	@param indices may be empty but may not be <code>null</code>
		 */
	public void setIndices(int[]indices){
		if(indices==null)throw new IllegalArgumentException(
				"Null indices in "+Debug.info(this));
	  boolean first=this.indices==null;
	  for(int i=0;indices.length>0&&i<indices.length;i++)
	  	if(indices[i]<0)throw new IllegalArgumentException(
	  			"Bad index in "+Debug.info(this));
	  this.indices=indices;
	  if(indexings!=null){
	    Object[]indexables=indexables(),old=indexings;
	    indexings=new Object[old.length];indexings[0]=indexables[indices[0]];
	    for(int i=0,indexion=1;i<old.length;i++)
	      if(old[i]!=indexings[0])indexings[indexion++]=old[i];
	  }
	  if(!first)coupler.indexSet(this);
	}
	/**
	The item denoted by the current <code>index</code>. 
		 */
	public Object indexed(){
	  if(indices==null)throw new IllegalStateException("No index in "+Debug.info(this));
	  return indices.length==0?NOT_INDEXED:indexables()[index()];
	}
	/**
	The <code>indexables</code> ordered by most recently indexed. 
		 */
	public Object[]indexings(){
	  if(indexings==null)throw new IllegalStateException("Null indexings in "+this);
	  return indexings;
	}
	public String toString(){
		return super.toString()+" "+indices().length;
	}
  protected TypedNode[]newDebugChildren(){
    ItemList<TypedNode>items=new ItemList(TypedNode.class);
    items.addItems(super.newDebugChildren());
    if(indexables!=null&&indexables.length>0){
      items.addItem(newDebugSourcesNode("indexables",indexables()));
      items.addItem(newDebugSourcesNode("index",indices[0]));
      items.addItem(newDebugSourcesNode("indexed",indexed()));
      items.addItem(newDebugSourcesNode("indexTitles",(Object[])
      		coupler.newIndexableTitles(this)));
    }
    return items.items();
  }
	/**
	Creates an indexing with a default coupler, set to zero.  
	@param title passed to core constructor
	 * @param indexables  passed to core constructor
	 */
	public static SIndexing newDefault(String title,Object[]indexables){
		return new SIndexing(title,indexables,0,new Coupler());
	}
}