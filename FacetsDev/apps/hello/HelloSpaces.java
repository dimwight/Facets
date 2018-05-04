package apps.hello;

import static facets.facet.FacetFactory.*;
import facets.core.superficial.SFacet;
import facets.core.superficial.STarget;
import facets.core.superficial.STargeter;
import facets.core.superficial.STextual;
import facets.core.superficial.SToggling;
import facets.facet.FacetFactory;




/**
Like {@link HelloField} with a flag to change validation. 
 */
public final class HelloSpaces extends HelloContenter {

	//Indexes into target and targeter arrays
	private static final int 
	TARGET_GREETING = 0, 
	TARGET_SPACES= 1;
	
	//References to targets
	STextual greeting;
	SToggling spaces;

	/**
	Uses new target type. 
	@see HelloContenter#newBasicTargets()
	 */
	@Override
	protected STarget[] newBasicTargets() {
		
		//Defines validation policy of textual  
		spaces = new SToggling("Allow spaces", true, 
			
			//Custom coupler
			new SToggling.Coupler() {
				
				//Updates greeting text if required by flag state
				@Override
				public void stateSet(SToggling t) {
					
					//Has the flag been reset?
					if (!t.isSet()) {
						
						//Make sure text is valid
						String text = greeting.text();
						greeting.setText(text.replaceAll(" ", ""));
					}
				}
			}
		);
		
		//Target as before, with coupler supplying policy
		greeting = new STextual("Edit greeting here", 
				"Hello world!", 
				
			//Custom coupler
			new STextual.Coupler(){
				
				//Extends default validation logic
				@Override
				public boolean isValidText(STextual t, String text){
					
					//Get default and do new check
					boolean 
					superValid = super.isValidText(t, text),
					okSpaces = spaces.isSet() || text.indexOf(" ") < 0;
					
					//Return combined checks
					return superValid && okSpaces;
				}
				
				//Sets update policy
				@Override
				public boolean updateInterim(STextual t) {
					
					//Accept interim updates
					return true;
				}
			}
		);
		
		//Return array ordered to match TARGET_ constants
		return new STarget[] {greeting, spaces};
	}
	
	/**
	Returns facet including checkbox. 
	@see HelloContenter#newBasicPanelFacets(
	FacetFactory, STargeter[])
	 */
	@Override
	protected SFacet[] newBasicPanelFacets(FacetFactory ff, 
			STargeter[] targeters) {
		
		//Get targets using TARGET_ constants		
		STargeter 
		greeting = targeters[TARGET_GREETING],
		spaces = targeters[TARGET_SPACES];
		
		//Create and return array of facet and dividers
		return new SFacet[]{
				
				//Label and text field as before
				ff.textualLabel(greeting, HINT_NONE), BREAK, 
				ff.textualField(greeting, 15, HINT_NONE), BREAK, 
				
				//Check box
				ff.togglingCheckboxes(spaces, HINT_BARE)
			};
	}
	
	/**
	Creates single checkbox item for menu. 
	@see HelloContenter#newBasicMenuFacets(
	FacetFactory, STargeter[])
	 */
	@Override
	protected SFacet[] newBasicMenuFacets(FacetFactory ff, 
			STargeter[] targeters) {
		
		//Create and return menu facet in array 
		return new SFacet[]{
				ff.togglingCheckboxMenuItems(targeters[TARGET_SPACES], 
						HINT_NONE)
			};
	}
}