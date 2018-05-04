package facets.util.geom;
import static facets.util.geom.Angle.*;
import facets.util.NumberPolicy;
import facets.util.NumberPolicy.Ticked;
/**
Validation and display of angle values.  
<p>{@link AnglePolicy} supplies policy for
angle values on a tick-based scale that can offer either
full-range or local adjustment. 
 */
public class AnglePolicy extends Ticked{
  /**
	Unique public constructor. 
	<p>Constructs a full-range policy displaying -180 to +180 degrees. 	
	 */
	public AnglePolicy(){this(Double.NaN);}
  private AnglePolicy(double range) {
		super(-CIRCLE_DEGREES / 2, CIRCLE_DEGREES / 2, range);
	}
  /**
	Re-implementation to return <code>true</code>. 
	 */
	final public boolean canCycle(){return true;}
  final public int format(){return FORMAT_DECIMALS_0;}
	public String[] incrementTitles() {
		return new String[]{"Anticlockwise","Clockwise"};
	}
  public int labelSpacing(){return 6;}
  /**
  Returns a local adjustment policy with a 60º range and its snap type
  coupled to that of its parent. 
  @see facets.util.NumberPolicy.Ticked#labelSpacing()
      */
  public Ticked localTicks(){
    final Ticked parent=this;
    return new AnglePolicy(CIRCLE_DEGREES/4){
      public int labelSpacing(){return 3;}
    	public int snapType(){return parent.snapType();}
    	public int tickSpacing(){return 5;}
    };
  }
	public int snapType(){return SNAP_TICKS;}
	public int tickSpacing(){return 15;}
  final protected boolean reverseIncrements(){return false;}
}