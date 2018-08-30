package apps.hello;

import static facets.facet.FacetFactory.*;
import facets.core.superficial.SFacet;
import facets.core.superficial.STarget;
import facets.core.superficial.STargeter;
import facets.core.superficial.STextual;
import facets.facet.FacetFactory;




/**
Like {@link HelloLabel} with a text field to edit 
the text. 
 */
public class HelloField extends HelloContenter {

	/**
	Same target type as before, with custom coupler. 
	@see HelloContenter#newBasicTargets()
	 */
	@Override
	protected STarget[] newBasicTargets() {
		
		//Same target for label and text field, with title
		STextual greeting = new STextual("Edit greeting here", 
				"Hello world!", 
		
			//Custom coupler
			new STextual.Coupler() {			
			
				//Sets update policy
				@Override
				public boolean updateInterim(STextual t) {
					
					//Accept interim updates
					return false;
				}			
			}
		);
		
		//Return in array
		return new STarget[] {greeting};
	}
	
	/**
	Attaches two facet to the same targeter. 
	@see HelloContenter#newBasicPanelFacets(
	FacetFactory, STargeter[])
	 */
	@Override
	protected SFacet[] newBasicPanelFacets(FacetFactory ff, 
			STargeter[] targeters) {
		
		//Get reference to single targeter
		STargeter greeting = targeters[0];
		
		//Create and return array of facet
		return new SFacet[]{	
				
			//Label as before
			ff.textualLabel(greeting, HINT_NONE), 
			
			//Divider to signal new line
			BREAK, 
			
			//Editable field
			ff.textualField(greeting, 15, HINT_NONE)
		};
	}
}