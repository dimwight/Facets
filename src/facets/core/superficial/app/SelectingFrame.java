package facets.core.superficial.app;
import facets.core.superficial.SFrameTarget;
import facets.util.Debug;
import facets.util.ItemList;
import facets.util.Titled;
import facets.util.Util;
import facets.util.tree.TypedNode;
/** 
{@link SFrameTarget} that maintains a {@link SSelection}.
<p>{@link SelectingFrame} extends its superclass by  
<ul><li>maintaining a {@link SSelection} defining a portion of its {@link #framed} content 
<li>allowing for the creation of {@link SFrameTarget}s framing the content 
selected.</ul>
 */
abstract public class SelectingFrame extends SFrameTarget{
	private SSelection selection;
  /**
  Unique constructor. 
  @param title passed to superclass
  @param content passed to superclass
   */
  public SelectingFrame(String title,Object content){
		super(title,content);
	}
	/**
	Define a selection within the content <code>framed</code>. 
	@param definition in the default implementation is cast to a {@link SSelection} 
	and passed to {@link #setSelection(SSelection)}
  @return (via {@link #setSelection(SSelection)}) the new return of {@link #selection()}
	 */
	public SSelection defineSelection(Object definition){
		return setSelection((SSelection)definition);		
	}
	/**
	The current selection within the content <code>framed</code>. 
	@return the {@link SSelection} last set with {@link #setSelection(SSelection)}
	 */
	final public SSelection selection(){
		if(selection==null)throw new IllegalStateException(
				"Null selection in "+Debug.info(this));
		return selection;
	}
  /**
	Sets the selection to be returned by {@link #selection()}. 
	<p>This method is defined protected as it should only be called by subclasses;
	external callers should use the more general {@link #defineSelection(Object)}.
	@param selection must have a {@link SSelection#content()} equal to
	{@link #framed}. 
  @return the new return of {@link #selection()}
	 */
	final protected SSelection setSelection(SSelection selection){
		if(selection.content()!=framed)throw new IllegalArgumentException(
				"Bad selection content:\n" + selection.content()+
				"\nshould be "+framed+"\nin "+Debug.info(this));
		else this.selection=selection;		
		if(false)trace(".setSelection: ",selection.multiple());
		if(false)Debug.printStackTrace(10);
		return selection;
	}
	/**
  Return a {@link SFrameTarget} framing the currently selected content. 
  <p>Content framed should be that returned in the <code>selection</code>
   methods of {@link #selection()}.
   <p>Default frames a single selection, titled from the selected content or 
   else using {@link #title()}. 
   */
	public SFrameTarget selectionFrame(){
		Object selected=selection().single();
		String title=selected instanceof Titled?((Titled)selected).title():title();
		return new SFrameTarget(title,selected){};
	}
	@Override
	protected TypedNode[]newDebugChildren(){
    ItemList<TypedNode>items=new ItemList(TypedNode.class);
    items.addItems(super.newDebugChildren());
    if(selection!=null)
      items.addItem(newDebugSourcesNode("selectionFrame",selectionFrame()));
    return items.items();
  }
}
