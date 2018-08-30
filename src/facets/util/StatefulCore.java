package facets.util;
import java.io.Serializable;
/**
Basic {@link Stateful} which stamps each state change. 
<p>Stamping is useful when
<ul>
	<li>monitoring the state of a {@link Stateful}
	<li> caching states where a {@link Stateful} cycles between states. 
</ul>
<p>Non-<code>transient</code> member variables in subclasses must be 
	{@link Serializable}; <code>transient</code> members may not be final. 
	Latency due to serialization can be minimised by defining a 
	small group of persistent members and recreating transient members during 
	<code>setState</code>. 
 */
public class StatefulCore extends Tracer implements Stateful{
  private static int stamps;
  private Integer stamp=0;
  private String title;
  /**
  Unique constructor. 
  <p>Also calls {@link #updateStateStamp()}. 
  @param title passed to {@link #setTitle(String)}
   */
  public StatefulCore(String title){
  	setTitle(title);
  	updateStateStamp();
	}
	/**
	Implements interface method. 
	<p>Wrapper for {@link Util#deserializedCopy(Serializable)} 
	 */
  @Override
	public Stateful copyState(){
		return(Stateful)Util.deserializedCopy(this);
	}
  /**
  Implements interface method.
  <p>Does not actually set any state, but does change the value returned 
  by {@link #stateStamp()}: if updating itself by calling {@link #updateStateStamp()}, 
  otherwise by casting <code>src</code> to a {@link Stateful} 
  and copying its {@link #stateStamp()} 
   */
  @Override
  public void setState(Object src){
		if(src==this)updateStateStamp();
		else stamp=((StatefulCore)src).stamp;
  }
  /**
  Invalid stub implementation. 
   */
  @Override
	public boolean stateEquals(Stateful s){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	/**
	Implements interface method. 
	@return an {@link Integer} unique amongst all states of all {@link Stateful}s
	 */
	@Override
	public Object stateStamp(){
		return stamp;
	}
  /**
	Implements interface method. 
	<p>Increments the value to be returned by {@link #stateStamp()}. 
	@return the new value
   */
  @Override
	final public Object updateStateStamp(){
  	if(false&&stamp>0){
  		trace(".updateStateStamp: "+Debug.info(this)
  		+ " stamp=",stamp);
  		Debug.printStackTrace("actionTriggered");
  	}
		return stamp=stamps++;
	}
  /**
	Implements interface method. 
	@return the text passed successfully to {@link #setTitle(String)}
   */
  @Override
	final public String title(){
		return title;
	}
	/**
	Sets the text to be returned by {@link #title()}
		 */
	public final void setTitle(String title){
		if(title==null||title.trim().equals(""))throw new IllegalArgumentException(
				"Null or empty title");
		else if(!title.matches("[\\S ]+"))throw new IllegalArgumentException(
			"Illegal whitespace in title="+title);
		else this.title=title;
	}
	/**
	Re-implementation wrapping {@link Debug#info(Object)}. 
	 */
	@Override
	public String toString(){
		return Debug.info(this);
	}
}
