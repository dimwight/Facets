package facets.core.superficial;
import facets.util.Debug;
import facets.util.ItemList;
import facets.util.NumberPolicy;
import facets.util.Util;
import facets.util.tree.TypedNode;
/**
{@link STarget} representing a numeric value. 
<p>{@link SNumeric} represents a numeric value to 
  be exposed to user view and control in the surface; application-specific 
  mechanism and policy can be defined in a {@link facets.core.superficial.SNumeric.Coupler}. 
 */
final public class SNumeric extends TargetCore{
	public static boolean doRangeChecks=false;
	/**
	Connects a {@link SNumeric} to the application. 
	<p>A {@link Coupler} supplies policy and/or client-specific 
	  mechanism to a {@link SNumeric}. 
	 */
	public static class Coupler implements TargetCoupler{
		/**
		Returns the policy to be used by a {@link facets.core.superficial.SNumeric} 
		constructed with this {@link Coupler}.
			 */
		public NumberPolicy policy(SNumeric n){
			return new NumberPolicy(0,0);
		}
		/**
		Defines client-specific mechanism for a {@link facets.core.superficial.SNumeric}. 
		<p>This method is called whenever <code>setValue</code> is called on <code>n</code>.
		 */
		public void valueSet(SNumeric n){
			throw new RuntimeException("Not implemented in "+Debug.info(this));
		}
	}
	private final Coupler coupler;
	private final NumberPolicy policy;
	private double value=Double.NaN;
	/**
	Unique constructor. 
	@param title passed to superclass
	@param value the initial value
	@param coupler must supply application-specific mechanism and policy
	 */
	public SNumeric(String title,double value,Coupler coupler){
	  super(title);this.coupler=coupler;
		policy=coupler.policy(this);
		if(policy==null)throw new IllegalStateException("No policy in "+Debug.info(this));
	  setValue(value);
	}	/**
	Returns the last valid value set.
	 <p>The value returned will that set using <code>setValue</code> or 
	 during construction; if {@link #doRangeChecks} is set to <code>true</code>
	 it will be checked against the number policy minimum and maximum values.     
	 */
	public double value(){
		if(value!=value)throw new IllegalStateException("Not a number in "+Debug.info(this));
		else if(doRangeChecks){
			double min=policy.min(),max=policy.max();
			if(value<min||value>max)throw new IllegalStateException
				("Value "+value+" should be >="+min+" and <="+max+" in "+Debug.info(this));			
		}
		return value;
	}
	/**
	Sets the nearest valid value to <code>value</code>.
	 <p>Validity will be as defined by <code>validValue</code> in  
	 the {@link NumberPolicy} returned as <code>policy</code>. 
	 Subsequently calls <code>valueSet</code> in the {@link SNumeric.Coupler} with which the
	 {@link SNumeric} was constructed.
	 */
	public void setValue(double value){
  	boolean first=this.value!=this.value;
  	this.value=policy().validValue(this.value,value);
  	if(!first)coupler.valueSet(this);
  }
	/**
	Returns the current number policy. 
	<p>The policy is that returned by the {@link facets.core.superficial.SNumeric.Coupler} 
	with which the {@link SNumeric} was constructed.
	 */
	public NumberPolicy policy(){return policy;}
	public String toString(){
		NumberPolicy p=policy();
		return super.toString()+" value="+Util.sf(value)+" policy="+p;
	}
	protected TypedNode[]newDebugChildren(){
    ItemList<TypedNode>items=new ItemList(TypedNode.class);
    items.addItems(super.newDebugChildren());
    items.addItem(newDebugSourcesNode("value",new Double(Util.fx(value()))));
    return items.items();
  }
	@Override
	protected void traceOutput(String msg){
		traceOutputWithClass(msg);
	}
}
