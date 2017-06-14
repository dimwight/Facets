package facets.core.superficial.app;
import facets.core.superficial.Notifying;
import facets.core.superficial.SFrameTarget;
import facets.core.superficial.SIndexing;
import facets.core.superficial.STarget;
import facets.core.superficial.STargeter;
import facets.core.superficial.TargetCore;
import facets.core.superficial.TargeterCore;
import facets.util.Debug;
import facets.util.ItemList;
import facets.util.tree.TypedNode;
/**
{@link SelectingFrame} that selects with an {@link SIndexing}. 
<p>{@link IndexingFrame} specialises its superclass for
content that can be selected by index.  
 */
public abstract class IndexingFrame extends SelectingFrame{
	/**
	Targeter for an {@link IndexingFrame}.   
	<p>The {@link #indexing()} method provides a reference to the 
	{@link SIndexing} wrapped by the target. 
	 */
	public static class FrameTargeter extends TargeterCore{
		private STargeter indexing;
		/**
		Unique constructor. 
		@param indexed will be the first target of this instance
		 */
		public FrameTargeter(IndexingFrame indexed){
			super(indexed.getClass());
		}
	  /**
	  Retargeted to the {@link SIndexing} wrapped by the current target. 
	   */
	  public final STargeter indexing(){
	    if(indexing==null)throw new IllegalStateException(
	    		"No indexingLink in "+Debug.info(this));
	    return indexing;
	  }
		/**
	  Overrides superclass method. 
	   */
	  public void retarget(STarget target,Impact impact){
	    super.retarget(target,impact);
	    STarget ix=((IndexingFrame)target).indexing();
	    if(indexing==null)indexing=((TargetCore)ix).newTargeter();
	    indexing.retarget(ix,impact);
	  }
	  /**
	  Overrides superclass method. 
	   */
	  public void retargetFacets(Notifying.Impact impact){
	    super.retargetFacets(impact);
	    indexing().retargetFacets(impact);
	  }
	  protected TypedNode[]newDebugChildren(){
	    ItemList<TypedNode>items=new ItemList(TypedNode.class);
	    items.addItems(super.newDebugChildren());
	    if(indexing!=null)
	      items.addItem(newDebugSourcesNode("indexing",indexing));
	    return items.items();
	  }
	}
	private SIndexing indexing;
  /**
	Public constructor. 
	@param title passed to core 
  @param content  passed to core 
  @param children passed to {@link #setIndexing(SIndexing)}
	 */
	public IndexingFrame(String title,Object content,SIndexing children){
		this(title,content);
		setIndexing(children);
	}
	/**
	Core constructor. 
	<p><b>NOTE</b> Subclass constructors must
	also pass a suitable {@link SIndexing} to {@link #setIndexing(SIndexing)}. 
	@param title passed to superclass 
  @param content passed to superclass 
	 */
	protected IndexingFrame(String title, Object content){
		super(title,content);
	}
	/**
	Re-implementation that creates a suitable {@link SSelection}. 
	<p>Calls {@link SIndexing#setIndexed(Object)} in {@link #indexing()}
	with <code>definition</code>, then {@link #setSelection(SSelection)}
	with a new {@link SSelection} returning as {@link SSelection#single()}
	the new {@link SIndexing#indexed()} (which will be <code>definition</code>). 
	 */
	@Override
	public SSelection defineSelection(Object definition){
		indexing().setIndexed(definition);
		return setSelection(new SSelection(){
			public Object[]multiple(){
				throw new RuntimeException("Not implemented in " + this);
			}
			public Object single(){
				return indexing().indexed();
			}
			public Object content(){
				return framed;
			}
		});
	}
	/**
	Re-implementation. 
	<p>Returns the {@link SFrameTarget} created in 
	{@link #newIndexedFrame(Object)}. 
	 */
	final public SFrameTarget selectionFrame() {
		return newIndexedFrame(selection().single());
	}
	/**
	Create a {@link SFrameTarget} complying with {@link #selectionFrame()}. 
	@param indexed the currently indexed member of {@link #framed}
	 */
	protected abstract SFrameTarget newIndexedFrame(Object indexed);
	/** 
	Sets an {@link SIndexing} that can index into content of {@link #framed}.
	<p>Also calls {@link #defineSelection(Object)} with the current 
	{@link SIndexing#indexed()}.
	@param indexing must have {@link SIndexing#indexables()}
	matching the current state of {@link #framed}. 
	 */
	public void setIndexing(SIndexing indexing){
		if(indexing==null)throw new IllegalArgumentException(
				"Null children in "+Debug.info(this));
		(this.indexing=indexing).setNotifiable(this);
		defineSelection(indexing.indexed());
	}
	/**
	The indexing last set with {@link #setIndexing(SIndexing)}. 
	 */
	public final SIndexing indexing(){
	  if(indexing==null)throw new IllegalStateException("No indexing in "+Debug.info(this));
	  return indexing;
	}
  /**
	Overrides superclass method. 
	<p>Must return an {@link FrameTargeter}. 
	 */
	public STargeter newTargeter(){
		return new FrameTargeter(this);
	}
}