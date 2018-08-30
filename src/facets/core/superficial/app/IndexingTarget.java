package facets.core.superficial.app;
import facets.core.app.SViewer;
import facets.core.superficial.SIndexing;
import facets.core.superficial.STarget;
import facets.core.superficial.STargeter;
import facets.core.superficial.TargetCore;
import facets.util.Debug;
import facets.util.ItemList;
import facets.util.tree.TypedNode;
import java.util.Arrays;
import java.util.List;
/**
{@link STarget} with indexing of {@link STarget} children. 
<p>{@link IndexingTarget} defines key behaviour needed by 
{@link facets.core.app.SAreaTarget}, based on an 
{@link SIndexing} of {@link facets.core.superficial.STarget} children. 
 */
public abstract class IndexingTarget extends TargetCore{
	private SIndexing indexing;
	private IndexingTarget parent;
  /**
	Unique constructor. 
	@param title passed to the superclass 
	@param indexing passed to {@link #setIndexing(SIndexing)}
	 */
	protected IndexingTarget(String title,SIndexing indexing){
		super(title!=null?title:indexing!=null?indexing.title():null);
		setIndexing(indexing);
	}
	/**
	Sets the {@link SIndexing} containing the {@link STarget} children of 
	the {@link IndexingTarget}. 
	@param indexing will be returned by {@link #indexing()}; its indexables
	will be returned by {@link #indexableTargets()} 
	and its {@link SIndexing#indexed()} by {@link #indexedTarget()}; 
	the indexables must therefore be a {@link STarget}[]
	 */
	public void setIndexing(SIndexing indexing){
		if(indexing==null)throw new IllegalArgumentException("Null children in "+Debug.info(this));
		Object[]indexables=indexing.indexables();
		if(!(indexables instanceof STarget[]))
		  throw new IllegalArgumentException("Bad children "+Debug.info(indexables));
		if(this.indexing!=null){
			if(false)nullInvalidParents((STarget[])indexables);
			else if(false){
				trace(".setIndexing: indexableTargets=",indexableTargets());
				trace(" indexables=",indexables);
			}
		}
		(this.indexing=indexing).setNotifiable(this);
		for(STarget target:indexableTargets())
			if(target==this)throw new IllegalArgumentException("Bad child in "+Debug.info(this));
	  	else if(target instanceof IndexingTarget)
				((IndexingTarget)target).parent=this;
	}
	public final void nullInvalidParents(STarget[]targetsNow){
		List<STarget>now=Arrays.asList(targetsNow);
		for(STarget target:indexableTargets())
			if(target instanceof IndexingTarget&&!now.contains(target)){
				if(false)traceDebug(".nullInvalidParents: target=",target);
				else((IndexingTarget)target).parent=null;
			}
	}
	/**
	The indexing last set with {@link #setIndexing(SIndexing)}. 
	 */
	final public SIndexing indexing(){
	  if(indexing==null)throw new IllegalStateException("Null indexing in "+Debug.info(this));
	  else return indexing;
	}
  /**
  Convenience cast for the <code>indexables</code> of the indexing. 
  @return <code>indexing().indexables()</code> cast to a 
  <code>{@link STarget}[]</code>
   */
  final public STarget[]indexableTargets(){
		return(STarget[])indexing().indexables();
	}
  /**
  Convenience cast for the <code>indexed</code> of the indexing. 
  @return <code>indexing().indexed()</code> cast to a 
  {@link STarget}
   */
	final public STarget indexedTarget(){
		return(STarget)indexing().indexed();
	}
  /**
	Overrides superclass method. 
	<p>Must return an {@link IndexingTargeter}. 
	 */
	public abstract STargeter newTargeter();
	/**
	Any containing {@link IndexingTarget}. 
	@return the parent which may be <code>null</code>
	 */
	final public IndexingTarget parent(){
		return parent;
	}
	/**
  Overrides superclass method. 
  <p>Ensures that only the indexed child is live (but ignores viewers).  
   */
  final public void setLive(boolean live){
		super.setLive(live);
		STarget indexed=indexedTarget();
		for(STarget child:indexableTargets())
			if(!(child instanceof SViewer))child.setLive(child==indexed?isLive():false);
			else if(false)traceDebug(".setLive: child=",child);
	}
	public String toString(){
    STarget child=indexedTarget();
    return Debug.info(this)+(false&&child instanceof IndexingTarget?" \n\t<"+child+">":"");
  }
	protected final boolean notifiesTargeter(){
		return true;
	}
	protected TypedNode[]newDebugChildren(){
    ItemList<TypedNode>items=new ItemList(TypedNode.class);
    items.addItems(super.newDebugChildren());
    STarget[]indexables=indexableTargets();
    if(indexables.length>0) {
      items.addItem(newDebugSourcesNode("indexableTargets",(Object[])indexables));
      items.addItem(newDebugSourcesNode("indexedTarget",indexedTarget()));
    }
    if(indexing!=null)
      items.addItem(newDebugSourcesNode("indexing",indexing));
    return items.items();
  }
}
