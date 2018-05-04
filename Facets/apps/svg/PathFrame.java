package apps.svg;
import static apps.svg.PathContenter.*;
import facets.core.superficial.SFrameTarget;
import facets.core.superficial.SNumeric;
import facets.core.superficial.SNumeric.Coupler;
import facets.core.superficial.STarget;
import facets.util.Debug;
import facets.util.NumberPolicy;
import facets.util.NumberPolicy.Ticked;
import applicable.SvgShape;
final class PathFrame extends SFrameTarget{
	PathFrame(Object framed){
		super(framed);
	}
	@Override
	protected STarget[]lazyElements(){
		String key="";
		PathContenter pc=(PathContenter)framed;
		for(SvgShape path:pc.paths.paths)key+=path.title();
		double[]values=pc.getPathValues(pc.valuesKey=key);
		if(false)trace(".lazyElements: key="+key+" values=" +Debug.info(values)+
				" "+pc.state.get(key));
		return new STarget[]{
			new SNumeric("X",values[VALUE_X],newCoupler(VALUE_X)),
			new SNumeric("Y",values[VALUE_Y],newCoupler(VALUE_Y)),
			new SNumeric("S&cale",values[VALUE_SCALE],newCoupler(VALUE_SCALE)),
			new SNumeric("S&pacing",values[VALUE_SPACING],newCoupler(VALUE_SPACING)),
		};
	}
	private Coupler newCoupler(final int at){
		return new Coupler(){
			final NumberPolicy[]policies={
					newValuePolicy(VALUE_X,0,DRAW_WIDTH),			
					newValuePolicy(VALUE_Y,0,DRAW_HEIGHT),			
					newValuePolicy(VALUE_SCALE,SCALE_MIN,SCALE_MIN+SCALE_RANGE),			
					newValuePolicy(VALUE_SPACING,0,ASPECT_X)			
			};
			@Override
			public NumberPolicy policy(SNumeric n){
				return policies[at];
			}
			@Override
			public void valueSet(SNumeric n){
				PathContenter pc=(PathContenter)framed;
				String key=pc.valuesKey;
				double[]values=pc.getPathValues(key);
				values[at]=n.value();
				pc.state.put(key,values);
				if(false)trace(".valueSet: n=" +Debug.info(n)+
						" values=" +Debug.info(values)+
						" ",pc.state.get(key));
			}
		};
	}
	private NumberPolicy newValuePolicy(final int at,double min,double max){
		final PathContenter pc=(PathContenter)framed;
		return new Ticked(min,max){
			private Ticked local;
			public Ticked localTicks(){
				final Ticked parent=this;
				return local==null?local=at!=VALUE_SCALE?super.localTicks()
						:new NumberPolicy.ValueVariable(0.05,SCALE_RANGE){
					private String keyThen;
					private double valueThen;
					protected double currentValue(){
						String pcKey=pc.valuesKey;
						return pc.valuesKey.equals(keyThen)?valueThen
								:(valueThen=pc.getPathValues(keyThen=pcKey)[VALUE_SCALE]);
					}
					public int tickSpacing(){
						return unit()<0.1?10:5;
					}
					public int labelSpacing(){
						return 5;
					}
					public int snapType(){
						return parent.snapType();
					}
				}:local;
			}
			public int format(){
				switch(at){
				case VALUE_SCALE:return FORMAT_DECIMALS_2;
				case VALUE_X:case VALUE_Y:case VALUE_SPACING:return FORMAT_DECIMALS_1;
				}
				throw new IllegalStateException("Impossible in "+Debug.info(this));
			}
			public int tickSpacing(){
				switch(at){
				case VALUE_SCALE:return 5;
				case VALUE_X:case VALUE_Y:return 2;
				case VALUE_SPACING:return 5;
				}
				throw new IllegalStateException("Impossible in "+Debug.info(this));
			}
			public int labelSpacing(){
				switch(at){
				case VALUE_SCALE:return 5;
				case VALUE_X:case VALUE_Y:return 5;
				case VALUE_SPACING:return 2;
				}
				throw new IllegalStateException("Impossible in "+Debug.info(this));
			}
			public int columns(){
				return 3;
			}
			public int snapType(){
				return pc.snap.isSet()?SNAP_TICKS:SNAP_NONE;
			}
		};
	}
}