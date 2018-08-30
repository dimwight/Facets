package facets.core.superficial;
import facets.util.Debug;
import facets.util.ItemList;
import facets.util.Titled;
import facets.util.Tracer;
import facets.util.tree.TypedNode;
/**
{@link STarget} representing a Boolean value. 
<p>{@link SToggling} represents a Boolean value to 
  be exposed to user view and control in the surface; application-specific 
  mechanism can be defined in a {@link facets.core.superficial.SToggling.Coupler}. 
 */
final public class SToggling extends TargetCore{
	/**
	A set of {@link SToggling}s based on an <code>int[]</code>.
	 */
	public static abstract class Togglings extends Tracer{
		public final SToggling[]togglings;
		private boolean setting;
		public Togglings(Object[]togglables,final int[]stateInts){
			if(togglables.length!=stateInts.length)throw new IllegalArgumentException(
					"Unequal arrays:\n\ttogglables="+togglables.length+" stateInts="+stateInts.length+
							" in "+Debug.info(this));
			final Coupler coupler=new Coupler(){
				public void stateSet(SToggling t){
					for(int i=0;!setting&&i<togglings.length;i++)
						if(togglings[i]==t)togglingSet(i);
				};
			};
			final boolean invert=zeroAsTrue();
			togglings=new SToggling[togglables.length];
			for(int i=0;i<togglings.length;i++)togglings[i]=new SToggling(
					newTogglingTitle(togglables[i]),(stateInts[i]>0)^invert,coupler);
			if(false)trace(".Togglings: invert="+invert+" togglings=",togglings);
		}
		protected boolean zeroAsTrue(){
			return false;
		}
		protected String newTogglingTitle(Object togglable){
			return togglable instanceof Titled?((Titled)togglable).title()
				:togglable.toString();
		}
		protected abstract void togglingSet(int at);
		final public int[]stateInts(){
			int[]ints=new int[togglings.length];
			final boolean invert=zeroAsTrue();
			for(int i=0;i<ints.length;i++)ints[i]=togglings[i].isSet()^invert?1:0;
			return ints;
		}
		final public void setStates(int[]stateInts){
			setting=true;
			final boolean invert=zeroAsTrue();
			for(int i=0;i<togglings.length;i++)togglings[i].set((stateInts[i]>0)^invert);
			setting=false;
		}
	}
	/**
	Connects a {@link SToggling} to the application. 
	<p>A {@link Coupler} supplies application-specific mechanism 
	for a {@link SToggling}.
	 */
	public static class Coupler implements TargetCoupler{
		/**
		Called by the toggling whenever its state is set. 
	 */
		public void stateSet(SToggling t){}
	}
	public final SToggling.Coupler coupler;
	private boolean state;
	/**
	Unique constructor. 
	@param title passed to superclass
	@param state initial state of the toggling
	@param coupler can supply application-specific mechanism
	 */
	public SToggling(String title,boolean state,Coupler coupler){
		super(title);
		this.state=state;
		this.coupler=coupler;
	}
	/**
	The Boolean state of the toggling. 
	 <p>The value returned will that set using 
	 <code>setState</code> or during construction.    
		 */
	public boolean isSet(){
		return state;
	}
	/**
	Sets the Boolean state. 
	<p> Subsequently calls {@link facets.core.superficial.SToggling.Coupler#stateSet(SToggling)}.
	*/
	public void set(boolean state){
		this.state=state;
		coupler.stateSet(this);
	}
	public String toString(){
		return super.toString()+(false?"":" "+state);
	}
	public boolean equals(Object obj){
		SToggling t=(SToggling)obj;
		return t.state==state&&t.title().equals(title());
	}
  protected TypedNode[]newDebugChildren(){
    ItemList<TypedNode>items=new ItemList(TypedNode.class);
    items.addItems(super.newDebugChildren());
    items.addItem(newDebugSourcesNode("isSet",state));
    return items.items();
  }
}