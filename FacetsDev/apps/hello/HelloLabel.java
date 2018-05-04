package apps.hello;

import facets.core.superficial.SFacet;
import facets.core.superficial.STarget;
import facets.core.superficial.STargeter;
import facets.core.superficial.STextual;
import facets.core.superficial.STextual.Coupler;
import facets.facet.FacetFactory;

/**
The simplest possible application. 
 */
public class HelloLabel extends HelloContenter {

	/**
	Defines target to be exposed in surface. 
	@see HelloContenter#newBasicTargets()
	 */
	@Override
	final protected STarget[] newBasicTargets() {
		
		//Create target to supply text for label
		String title = "Greeting";
		Coupler coupler = new STextual.Coupler();
		STextual greeting = new STextual(title, 
				"Hello world!", coupler);
		
		//Return in array
		return new STarget[] {greeting};
	}
	
	/**
	Attaches facet to targeter created from target. 
	@see HelloContenter#newBasicPanelFacets(
	FacetFactory, STargeter[])
	 */
	@Override
	final protected SFacet[] newBasicPanelFacets(FacetFactory ff, 
			STargeter[] targeters) {
		
		//Get facet that will create label
		STargeter greeting = targeters[0];
		SFacet label = ff.textualLabel(greeting, 
				FacetFactory.HINT_NONE);
		
		//Return in array
		return new SFacet[]{label};
	}
}