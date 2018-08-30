package facets.core.superficial;

/**
Potential element of the Facets notification tree. 
<p>{@link Notifying} is the root of the Facets type hierarchy 
  for both targets and targeters. 
<p>Whenever a target acts on the element it represents in the 
  surface (usually though not necessarily in response to widget 
  input relayed by a surface) the surface must 
  be notified that a retargeting is needed to ensure view and 
  control consistency. 
<p>Notification is triggered in the Facets implementation of by calling {@link #notify(Notice)} 
  target, and relayed to the application root via a tree of {@link Notifying}s, 
  all either {@link facets.core.superficial.STarget}s or {@link facets.core.superficial.STargeter}s. 
<p>A {@link Notifying} is attached to the tree with {@link #setNotifiable(Notifiable)} 
  to set one of the following as its {@link #notifiable()}: 
<ul>
  <li>for a {@link STarget} for which {@link TargetCore#notifiesTargeter()} 
  returns <code>false</code>,
    the {@link STarget} of which it is a child</li>
  <li>for a {@link STarget} for which {@link TargetCore#notifiesTargeter()} 
  returns <code>true</code>, the {@link STargeter} 
    of which it is the target</li>
  <li>for a {@link STargeter}, its parent in the targeter 
    tree; for the root of the targeter tree, the surface itself</li>
</ul>
<p>In all three cases the {@link Notifiable} is set during retargeting; 
the Facets framework typically ensures that notification 
  is triggered whenever a target acts on the application element it represents. 

 */
public interface Notifying extends Notifiable{
	/**
  Allows a {@link Notifying} to refine its notification.
  <p>The members of {@link Impact} can define the scope of the event 
  triggering a notification;   
  each member guarantees that its impact will be less than any member
  with higher {@link #ordinal()} 
<ul>
	<li>{@link #MINI} no state change; typically a status message  
	<li>{@link #ACTIVE} active state eg of viewers may have changed
	<li>{@link #SELECTION} content selection state may have changed
	<li>{@link #CONTENT} content state may have changed
	<li>{@link #DEFAULT} any application element or state may have changed
	</ul> 
	 */
  public enum Impact{
  	MINI,
  	ACTIVE,
  	SELECTION,
  	CONTENT,
  	DEFAULT,
  	DISPOSE;
		public boolean exceeds(Impact other){
			return ordinal()>other.ordinal();
		}
	}
	/**
	Attach the {@link Notifying} to a {@link Notifiable} parent 
	in the notification tree. 
			 */
  void setNotifiable(Notifiable n);
	/**
	Call {@link Notifiable#notify(Notice)} on any parent in the notification tree, 
	typically with itself as parameter to the {@link Notice} passed. 
	<p>The usual means to trigger a surface retargeting, being called
	by an exposing {@link SFacet} on its target. 
	@param impact suggests the level of retargeting required 
  		 */
  void notifyParent(Impact impact);
  /**
	The current {@link Notifiable} set for this {@link Notifying}. 
	<p>As set with {@link #setNotifiable(Notifiable)} or <code>null</code> if none set. 
			 */
	Notifiable notifiable();
	/**
	Allows this {@link Notifying} to specify a default {@link Impact}.
	@return typically {@link Impact#DEFAULT} 
	 */
	Impact impact();
	Notifying[]elements();
}
