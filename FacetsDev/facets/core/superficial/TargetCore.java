package facets.core.superficial;
import static facets.util.app.Events.*;
import facets.util.Debug;
import facets.util.ItemList;
import facets.util.Objects;
import facets.util.tree.TypedNode;
/**
 Implements {@link STarget}. 
 <p>{@link TargetCore} is the core implementation of {@link STarget}, 
 extended both in this and other packages. It also serves as 
 a means of grouping other simple targets. 
 */
public class TargetCore extends NotifyingCore implements STarget{
	private STarget[]elements;
	private boolean live=true,wantsFocus;
	private final String title;
	public static int targets;
	/**
	 Convenience constructor that sets no child elements. 
	 */
	public TargetCore(String title){
		this(title,(STarget[])null);
	}
	/**
	 Core constructor. 
	 @param title should be suitable for return as the (immutable)
	 <code>title</code> property
	 @param elements may be <code>null</code> (in which case 
	 suitable elements may be created using <code>lazyElements</code>); otherwise
	 passed to {@link #setElements(STarget[])}
	 */
	public TargetCore(String title,STarget...elements){
		targets++;
		if((this.title=title)==null||title.equals(""))
			throw new IllegalArgumentException("Null or empty title in "+Debug.info(this));
		if(elements!=null)setElements(elements);
		if(trace)traceEvent("Created "+Debug.info(this));
	}
	/**
	 Sets the {@link STarget} children of the {@link TargetCore}. 
	 <p>Intended for use in specialised subclass construction; 
	 elements set are thereafter immutable. 
	 @param elements (which may not be <code>null</code> nor contain <code>null</code>
	 members) will be returned as the <code>elements</code> property. 
	 */
	final protected void setElements(STarget[]elements){
		if(this.elements!=null)throw new RuntimeException("Immutable elements in "
				+Debug.info(this));
		else if((this.elements=elements)==null)
			throw new IllegalArgumentException("Null elements in "+Debug.info(this));
		else for(int i=0;i<elements.length;i++)
			if(elements[i]==null)
				throw new IllegalArgumentException("Null element "+i+" in "+Debug.info(this));
	}
	/**
	 Implements interface method. 
	 <p>If no elements have been set, attempts to create them with 
	 <code>lazyElements</code>. 
	 <p>Each call to this method also sets the {@link TargetCore} 
	 as notification monitor of any element that is not a {@link 
	 facets.core.superficial.SFrameTarget}. 
	  
	 */
	final public STarget[]elements(){
		if(elements==null)setElements(lazyElements());
		if(elements==null)throw new IllegalStateException("No elements in "+Debug.info(this));
		for(int i=0;i<elements.length;i++)
			if(!(elements[i] instanceof SFrameTarget))elements[i].setNotifiable(this);
		return elements;
	}
	/**
	 Lazily creates <code>element</code>s for this target.  
	 <p>Called at most once from {@link #elements()}. 
	 <p>Though defined in {@link TargetCore} this method is primarily for use by
	 {@link SFrameTarget}s, which always create their elements dynamically 
	 by reimplementing this method.
	 Default implementation returns an empty {@link STarget}[]. 
	 */
	protected STarget[]lazyElements(){
		return new STarget[]{};
	}
	/**
	 Create and return a targeter suitable for retargeting to 
	 this target. 
	 <p>This is the key method used by Facets to implement dynamic 
	 creation of a surface targeter tree. During initial retargeting 
	 each {@link TargeterCore} queries its <code>target</code> 
	 for any child elements, and calls this method on each child 
	 to obtain suitable {@link STargeter} instances which 
	 it then adds to its elements. 
	 <p>This method may be also called on subsequent retargetings 
	 where the specific type of a target is subject 
	 to change (for instance when it represents a selection). 
	 Either the {@link STargeter} returned can be matched 
	 to an existing one to which facet have already been attached, 
	 or such facet can be attached and the surface layout adjusted 
	 accordingly. 
	 
	 */
	public STargeter newTargeter(){
		return new TargeterCore(getClass());
	}
	public boolean isLive(){
		Notifiable n=notifiable();
		boolean notifiesTarget=n!=null&&n instanceof STarget;
		return !notifiesTarget?live:live&&((STarget)n).isLive();
	}
	public void setLive(boolean live){
		this.live=live;
		if(false&&title.equals("P&aste")){
			trace(".setLive: ",Debug.info(this)+": "+live+": "+isLive());
			if(true)Debug.printStackTrace(5);
		}
	}
	public String title(){
		return title;
	}
	public String toString(){
		String add=false?"":true?(" "+isLive()):elements==null?""
				:Objects.toStringWithHeader(elements);
		return Debug.info(this)+add;
	}
	/**
	Used to construct the notification tree. 
	<p><b>NOTE</b> This method must NOT be overridden in application code. 
	 */
	protected boolean notifiesTargeter(){
		return false;
	}
	protected TypedNode[]newDebugChildren(){
		ItemList<TypedNode>items=new ItemList(TypedNode.class);
		items.addItems(super.newDebugChildren());
		if(elements!=null&&elements.length>0)
			items.addItem(newDebugSourcesNode("elements",(Object[])elements));
		return items.items();
	}
	final public static STarget[]join(STarget[]head,STarget[]tail){
		return Objects.join(STarget.class,head,tail);
	}
	private void setWantsFocus(){
		wantsFocus=true;		
	}
	public boolean wantsFocus(){
		return wantsFocus;
	}
	/**
	Utility for finding a {@link Notifiable} ancestor in the notification tree. 
	<p>Searches up the tree from the current target of <code>facet</code>.
	@param target in tree exposed by calling facet
	@return the first found, or <code>null</code>
	 */
	final public static <T extends Notifiable>T findNotifiableTyped(Class<T> type,
			STarget target){
		Notifiable check=target;
		while(check instanceof Notifying&&!type.isAssignableFrom(check.getClass()))
			try{
				check=((Notifying)check).notifiable();
				if(check==null)return null;
			}catch(Exception e){
				return null;
			}
		return type.isAssignableFrom(check.getClass())?(T)check:null;
	}
}
