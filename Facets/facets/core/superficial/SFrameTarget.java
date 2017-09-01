package facets.core.superficial;
import facets.core.superficial.app.SView;
import facets.util.Debug;
import facets.util.ItemList;
import facets.util.OffsetPath;
import facets.util.Stateful;
import facets.util.Titled;
import facets.util.Util;
import facets.util.tree.TypedNode;

import java.io.Serializable;
/**
Extends {@link TargetCore} by framing 
  application content to be exposed directly to facets. 
 */
public class SFrameTarget extends TargetCore{
	private static int frames;
	/**Immutable framed framed by the {@link SFrameTarget}.*/
	final public Object framed;
	/**
	Convenience constructor creating a suitable title.
	 <p>If <code>framed</code> is a {@link facets.util.Titled} its <code>title</code>
	  is used, therwise a dummy title is created.   
	 */
	public SFrameTarget(Object framed){
		this(framed instanceof Titled?((Titled)framed).title()
				:Util.helpfulClassName(framed)+"#"+frames++,framed);
	}
	/**
 	Core constructor. 
  <p>Note that this passes no child target elements to the superclass; 
    elements can only be set by subclassing and  
  <ul>
    <li>in named subclasses where the elements are known at 
      construction, calling {@link #setElements(STarget[])} 
      from the constructor 
    <li>in other cases (in practice the large majority), overriding 
      {@link #lazyElements()} from {@link TargetCore} 
  </ul>
  <p>This limitation ensures that the effective type of 
    a {@link SFrameTarget} with child elements can be distinguished 
    by reference to the compiled type. Care must therefore be 
    taken in applications not vary the effective type of the 
    elements created by a subclass. 
  @param title passed to the superclass 
  @param toFrame must not be <code>null</code>
	 */
	public SFrameTarget(String title,Object toFrame){
	  super(title);
		if((framed=toFrame)==null)throw new IllegalArgumentException(
				"Null framed in "+Debug.info(this));
	}
  /**
	Set the state of the framed. 
	<p>Default implementation is an invalid stub.  
	@param stateSpec must define the new state of {@link #framed}
	 @param interim if <code>true</code> the edit forms part of a sequence 
	 */
	public void setFramedState(Object stateSpec, boolean interim){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	protected TypedNode[]newDebugChildren(){
    ItemList<TypedNode>items=new ItemList(TypedNode.class);
    items.addItems(super.newDebugChildren());
    if(framed!=null)items.addItem(newDebugSourcesNode("framed",framed));
    return items.items();
  }
	protected final boolean notifiesTargeter(){
		return true;
	}
	public String title(){
		return framed==null||!(framed instanceof Titled)?super.title()
			:((Titled)framed).title();
	}
}