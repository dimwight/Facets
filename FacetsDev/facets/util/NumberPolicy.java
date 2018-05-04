package facets.util;
import static facets.util.Util.*;
/**
 Validation and display of numbers.
 <p>{@link NumberPolicy} supplies the policy information required when 
 validating a number or displaying it in the application surface. 
 */
public class NumberPolicy extends Tracer{
	public static final double MIN_VALUE=Double.MAX_VALUE*-1,
		MAX_VALUE=Double.MAX_VALUE;
	public static final boolean debug=false;
	public static double UNIT_JUMP_DEFAULT=1;
	public static int COLUMNS_DEFAULT=3;
	/**
	 Validation and tick-based display of numbers.  
	 <p>{@link Ticked} supplies validation and display policy for
	 numbers displayed on a scale such as that of a slider. 
	 */
	public static class Ticked extends NumberPolicy{
		public static final int SNAP_NONE=0,SNAP_TICKS=1,SNAP_LABELS=2;
		public static int TICKS_DEFAULT=1,LABEL_TICKS_DEFAULT=10;
		private final double range;
		/**
		 Convenience constructor for a tick-based policy with local 
		 adjstment and infinite range.  
		 */
		public Ticked(double range){
			this(MIN_VALUE,MAX_VALUE,range);
		}
		/**
		 Convenience constructor for a full-range tick scale.
		 */
		public Ticked(double min,double max){
			this(min,max,Double.NaN);
		}
		/**
		 Construct a tick-based policy that can optionally display the current value
		 within a local adjustment range.     
		 <p>The policy is constructed from <code>min</code> to <code>max</code> as for its
		 superclass. If <code>range</code> is other than <code>NaN</code>, values 
		 will be displayed within this adjustment range, which will centre on
		 the current value unless constrained by <code>min</code> to <code>max</code>. 
		 */
		public Ticked(double min,double max,double range){
			super(min,max);
			this.range=range;
		}
		/**
		 Convenience constructor for a scale that dynamically defines its constraints.
		 */
		public Ticked(){this(Double.NaN, Double.NaN, Double.NaN);}
		public double validValue(double existing,double proposed){
			Ticked local=true?this:localTicks();
			return local==this?super.validValue(existing,proposed)
					:local.validValue(existing,proposed);
		}
		/**
		 The number of ticks between each label. 
		 */
		public int labelSpacing(){
			return LABEL_TICKS_DEFAULT;
		}
		/**
		 Returns the policy to be used for local adjustment. 
		 <p>The default is to return the {@link Ticked} itself; this can be
		 overridden to return a policy for local adjustment where the {@link Ticked}
		 is full-range but local adjustment is also required.    
		 */
		public Ticked localTicks(){
			return this;
		}
		/**
		 The range of possible values. 
		 <p>Set immutably during construction but only accessed via 
		 this method, enabling subclasses to define it dynamically by overriding. 
		 */
		public double range(){
			return range!=range?max()-min():range;
		}
		/**
		 Defines the snap-to-ticks contentStyle.
		 <p>The value returned should be one of
		 <ul><li>SNAP_NONE</li>  
		 <li>SNAP_TICKS</li>
		 <li>SNAP_LABELS</li> 
		 </ul>
		 */
		public int snapType(){
			return SNAP_TICKS;
		}
		/**
		 The spacing between each tick, in the value returned by {@link #unit()}. 
		 */
		public int tickSpacing(){
			return 1;
		}
		/**
		 Returns the multiple of <code>unit</code> defining the current minimum
		 valid value change, based on the current snap policy.
		 <p>The value returned is determined by the values of <code>snapType</code>
		 and <code>tickSpacing</code>.   
		 */
		final protected double unitJump(){
			int snap=snapType(),tick=tickSpacing();
			return snap==SNAP_LABELS?tick*labelSpacing():snap==SNAP_TICKS?tick:1;
		}
		public String toString(){
			Ticked local=localTicks();
			return super.toString()+(local==this?"":local);
		}
	}
	/**
	{@link NumberPolicy.Ticked} that adjusts to changes in the value managed. 
	<p>{@link ValueVariable}
	 */
	public static abstract class ValueVariable extends Ticked{
		private static final double minMaxFactor=3.33;
		public ValueVariable(double min,double max){
			super(min,max);
		}
		public double min(){
			return Math.max(super.min(),currentValue()/minMaxFactor);
		}
		public double max(){
			return Math.min(super.max(),currentValue()*minMaxFactor);
		}
		public int format(){
			return(int)Doubles.stepped(new double[]{0.01,0.1,1,10},
					new double[]{FORMAT_DECIMALS_4,FORMAT_DECIMALS_3,FORMAT_DECIMALS_2,
					FORMAT_DECIMALS_1,FORMAT_DECIMALS_0},currentValue());
		}
		final public double unit(){
			double u=super.unit();
			return 0.1*Doubles.stepped(new double[]{5*u,10*u,25*u,50*u,100*u},
				new double[]{1*u,2*u,5*u,10*u,20*u,25*u},currentValue());
		}
		protected abstract double currentValue();
		public String toString(){
			return super.toString()+" time="+System.currentTimeMillis();
		}
	}
	private final double min,max;
	public static final int FORMAT_DECIMALS_0=0,FORMAT_DECIMALS_1=1,
			FORMAT_DECIMALS_2=2,FORMAT_DECIMALS_3=3,FORMAT_DECIMALS_4=4,
			FORMAT_HEX=-1;
	/**
	 Construct a policy that constrains valid values within the immutable range 
	 <code>min</code> to <code>max</code>.
	 */
	public NumberPolicy(double min,double max){
		if(min>max) throw new IllegalArgumentException("Bad values "+min+">="+max);
		this.min=min;
		this.max=max;
	}
	/**
	 The highest possible value under the policy. 
	 <p>Set immutably during construction but only accessed via 
	 this method, enabling subclasses to define it dynamically by overriding. 
	 */
	public double max(){return max;}
	/**
	 The lowest possible value under the policy. 
	 <p>Set immutably during construction but only accessed via 
	 this method, enabling subclasses to define it dynamically by overriding. 
	 */
	public double min(){return min;}
	/**
	 Returns a valid increment to <code>existing</code> in the direction given
	 by <code>positive</code>, or zero if the incremented value is outside the range. 
	 <p>Calculation of the increment takes into account 
	 <code>unit</code>, <code>unitJump</code>, and <code>reverseIncrements</code> and  
	 its sum with <code>existing</code> is validated using <code>valueValue</code>. 
	 */
	final public double validIncrement(double existing,boolean positive){
		double increment=unit()*unitJump()*(positive?1:-1)
				*(reverseIncrements()?-1:1),
			proposed=existing+increment,
			validated=validValue(existing,proposed);
		if(debug())trace("NumericPolicy:",min+"<="+max+
				", existing="+existing+", nudged="+proposed
					+", validated="+validated);
		return validated!=cycledValue(proposed)?0:validated-existing;
	}
	/**
	 Returns the nearest valid value to <code>proposed</code>. 
	 <p>Though not used in the basic implementation, specifying <code>existing</code> 
	 as a parameter allows for non-linear validation in subclasses.  
	 */
	public double validValue(double existing,double proposed){
		double min=min(),max=max(),cycled=cycledValue(proposed),
			adjusted=cycled<min?min:cycled>max?max:cycled,
			jump=unit()*unitJump(),
			rounded=false?Math.rint(adjusted):Math.rint(adjusted/jump)*jump;
		if(debug()){
			trace("NumberPolicy: ",min+"<="+max+
					", existing="+existing+", proposed="
					+sf(proposed)+", adjusted="+sf(adjusted)+", jump="+jump
					+", rounded="+sf(rounded));
		}
		return rounded<min?min:rounded>max?max:rounded;
	}
	protected boolean debug() {return false;}
	/**
	 The smallest possible increment under the policy. 
	 <p>The default implementation deduces this value from {@link #format() }. 
	 */
	public double unit(){
		int format=format();
		if(format>FORMAT_DECIMALS_4)throw new IllegalStateException(
				format+" decimals not implemented in "+Debug.info(this));
		double unit=format<FORMAT_DECIMALS_1?1:format==FORMAT_DECIMALS_1?0.1
				:format==FORMAT_DECIMALS_2?0.01:format==FORMAT_DECIMALS_3?0.001:0.0001;
		return unit;
	}
	/**
	 If <code>true</code>, values outside the range will be normalised to within 
	 it. 
	 <p>Default is <code>false</code>.
	 */
	public boolean canCycle(){
		return false;
	}
	/**
	 Hint on formatting the number. 
	 <p>These are defined by the class FORMAT_x constants, which are mutually exclusive.  
	 <p>For FORMAT_PLACES_x constants 
	 the format should normally be consistent with <code>unit</code>.
	 */
	public int format(){
		return FORMAT_DECIMALS_0;
	}
	/**
	 The column width for text boxes in which values are to be displayed.
	 <p>Follows AWT convention. 
	 */
	public int columns(){
		return COLUMNS_DEFAULT;
	}
	/**
	 	Human-readable description of increment directions.  
	 */
	public String[]incrementTitles(){
		return new String[]{"Less|\u25c4","More|\u25ba"};
	}
	public String toString(){
		return //Debug.info(this)+" "+
		" format="+format()+" "+
		" unit="+sf(unit())+
		" "+sf(min())+"<="+sf(max());
	}
	private double cycledValue(double proposed){
		if(!canCycle()) return proposed;
		double min=min(),max=max(),range=max-min;
		return proposed<min?max-((max-proposed)%range):proposed>max?min
				+((proposed-min)%range):proposed;
	}
	/**
	 Reverses the normal increment direction. 
	 <p>Used by <code>validIncrement</code>, default is <code>false</code>. 
	 */
	protected boolean reverseIncrements(){
		return false;
	}
	/**
	 Returns the multiple of <code>unit</code> defining the current minimum
	 valid value change. 
	 */
	protected double unitJump(){
		return UNIT_JUMP_DEFAULT;
	}
}
