package apps.hello;

import static facets.facet.FacetFactory.*;
import facets.core.superficial.SFacet;
import facets.core.superficial.SNumeric;
import facets.core.superficial.STarget;
import facets.core.superficial.STargeter;
import facets.core.superficial.STextual;
import facets.facet.FacetFactory;
import facets.util.NumberPolicy;





/**
Like {@link HelloSpaces} with a number to set text length. 
 */
public final class HelloLimit extends HelloContenter {

	//Indexes into target and targeter arrays
	private static final int 
	TARGET_GREETING = 0, 
	TARGET_LIMIT = 1;
	
	//References to targets
	STextual greeting;
	SNumeric limit;

	/**
	Uses new target type. 
	@see HelloContenter#newBasicTargets()
	 */
	@Override
	protected STarget[] newBasicTargets() {
	
		//Define range of limits
		final int limitMin = 10, limitMax = 30;
	
		//Defines validation policy of textual  
		limit = new SNumeric("Length limit", limitMax, 
			
			//Custom coupler
			new SNumeric.Coupler() {
				
				//Ensures greeting text is within limit
				@Override
				public void valueSet(SNumeric n) {
					
					//Make sure text is valid
					String text = greeting.text();
					int length = Math.min(text.length(), (int) n.value());
					greeting.setText(text.substring(0, length));
				}			
	
				//Supplies all policy
				@Override
				public NumberPolicy policy(SNumeric n) {
					
					//Use variant that can supply slider policy
					return new NumberPolicy.Ticked(limitMin, limitMax) {
						
						//Sets jump, tick spacing
						@Override
						public double unit() {
							return 5;
						}
						
						//Label at each tick
						@Override
						public int labelSpacing() {
							return 1;
						}
					};
				}
			}
		);
		
		//Target as before, with similar coupler
		greeting = new STextual("Edit greeting here", 
				"Hello world!", 
				
			//Custom coupler
			new STextual.Coupler(){
				
				//Extends default validation logic
				@Override
				public boolean isValidText(STextual t, String text){
					
					//Get default and do length check
					boolean 
					superValid = super.isValidText(t, text),
					tooLong = text.length() > limit.value();
					
					//Return answer including new check
					return superValid && !tooLong;
				}
				
				//Same update policy
				@Override
				public boolean updateInterim(STextual t) {
					
					//Accept interim updates
					return true;
				}
			}
		);
		
		//Return array 
		return new STarget[] {greeting, limit};
	}
	
	/**
	Returns facet sharing the numeric. 
	@see HelloContenter#newBasicPanelFacets(
	FacetFactory, STargeter[])
	 */
	@Override
	protected SFacet[] newBasicPanelFacets(FacetFactory ff, 
			STargeter[] targeters) {
		
		//Get targets 		
		STargeter 
		greeting = targeters[TARGET_GREETING],
		limit = targeters[TARGET_LIMIT];
		
		//Create and return array
		return new SFacet[]{
				
				//Label and text field followed by spacer
				ff.textualLabel(greeting, HINT_NONE), BREAK, 
				ff.textualField(greeting, 20, HINT_NONE), BREAK, 
				ff.spacerTall(10), BREAK, 
				
				//Numeric slider and field, buttons after spacer
				ff.numericSliders(limit, 150, 
						HINT_SLIDER_FIELDS_TICKS_LABELS + HINT_TALL), 
				ff.spacerWide(15),
				ff.numericNudgeButtons(limit, HINT_BARE),
			};
	}
	
	/**
	Creates numeric items for menu. 
	@see HelloContenter#newBasicMenuFacets(
	FacetFactory, STargeter[])
	 */
	@Override
	protected SFacet[] newBasicMenuFacets(FacetFactory ff, 
			STargeter[] targeters) {
		
		//Create and return sub-menu facet in array 
		return new SFacet[]{
				ff.numericNudgeMenu(targeters[TARGET_LIMIT], HINT_NONE)
			};
	}
}