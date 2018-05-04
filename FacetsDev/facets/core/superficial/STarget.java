package facets.core.superficial;

/**
Superfical target. 
<p>{@link STarget} represents an application element to one or more 
facets; it is also a {@link Notifying} to enable it to form part of the 
	notification tree. 
 */
public interface STarget extends Notifying{
	/**
	Creates {@link STarget}s representing its data and logic. 
	<p>{@link Targeted} is primarily used to enable communication between
	application code and specialised facet actions, in particular those
	of viewers. 
	 */
	public interface Targeted{
		/**
		Return targets representing content and logic. 
		@return a non-<code>null</code> {@link STarget}[]
		 */
		STarget[]targets();
	}
	final STarget NONE=new TargetCore("No target"){};
	/**
	Dynamically-defined children. 
<p>Return {@link STarget} child elements of this {@link STarget}.
 
		@return a non-<code>null</code> {@link STarget}[]
	 */
	STarget[]elements();
	/**
	Indicates whether the {@link STarget} should
	be exposed by a surface facet as open to control eg 'enabled'.   
	<p>Returns <code>true</code> only if both the following conditions are met:</p>
	<ul>
	<li>the {@link STarget} itself is 'live' as constructed or  
	set by <code>setLive</code></li> 
	<li>any {@link STarget} monitor also returns <code>isLive</code> as
	<code>true</code></li>   
	</ul>   
	 
		 */
	boolean isLive();
	/**
	Sets the internal state used by <code>isLive</code>. 
  
	 */
  void setLive(boolean live);
	boolean wantsFocus();
}
