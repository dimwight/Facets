package facets.util;
import java.io.Serializable;
/**
Has state that can be set, copied and checked for deep equality.  
<p>{@link Stateful} encapsulates management of an object whose state may change,
defining methods for 
<ul>
<li>changing state while maintaining identity
<li>deep copying and equality checking
<li>recording and checking for state changes   
</ul>
 */
public interface Stateful extends Serializable,Titled{
	  /**
	Set state based on <code>src</code>. 
	@param src must be non-null and interpretable by the implementation 
		as state information; it will usually be another instance of the
	{@link Stateful}'s class. 
	   */
	void setState(Object src);
		/**
	Create a copy of the {@link Stateful}. 
		<p>Return one of: </p>
		<ul>
			<li>a deep copy of the {@link Stateful} suitable for passing to <code>setState</code></li>
			<li>the {@link Stateful} itself to enable such a copy to be created</li>
			<li> <code>null</code> to signal that the {@link Stateful} has 
			in fact no settable state </li>
		</ul>
	 */
	Stateful copyState();
  /**
	Does the other {@link Stateful} have the same state?
	<p>Allows deep checking to be implemented where doing so in 
	{@link Object#equals(Object)} is too expensive.  
	@param s to compare
	 */
	boolean stateEquals(Stateful s);
	/**
	Create a new value for return by {@link #stateStamp()}.  
 @return the new value
 */
	Object updateStateStamp();
  /**
	Return a value guaranteeing that any instance with the same value has 
	the same state. 
	 */
	Object stateStamp();
}
