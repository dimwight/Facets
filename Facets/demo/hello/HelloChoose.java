package demo.hello;

import static facets.facet.FacetFactory.*;
import facets.core.superficial.SFacet;
import facets.core.superficial.SIndexing;
import facets.core.superficial.STarget;
import facets.core.superficial.STargeter;
import facets.core.superficial.STextual;
import facets.facet.FacetFactory;




/**
Like {@link HelloLabel} offering a choice of texts. 
 */
public final class HelloChoose extends HelloContenter {

	//Indexes into target and targeter arrays
	private static final int 
	TARGET_GREETING = 0, 
	TARGET_TEXTS = 1;
	
	//References to targets
	private STextual greeting;
	private SIndexing texts;

	//The texts to choose from
	private final String[] helloTexts = {
		"Hello world!",
		"Hello Dolly!",
		"Hello, good evening and welcome!"
	};

	/**
	New target offers choice of texts. 
	@see HelloContenter#newBasicTargets()
	 */
	@Override
	protected STarget[] newBasicTargets() {
		
		//Supplies texts for label
		texts = new SIndexing("Texts", helloTexts , 0, 
			new SIndexing.Coupler() {
			
				//Called when index changes
				@Override
				public void indexSet(SIndexing i) {
					
					//Set indexed text
					greeting.setText((String) i.indexed());
				}
			}
		);
		
		//Create label target with starting text
		greeting = new STextual("Untitled", 
				(String) texts.indexed(), new STextual.Coupler());
		
		//Return both targets 
		return new STarget[] {greeting, texts};
	}
	
	/**
	Exposes greeting choices and index in panel. 
	@see HelloContenter#newBasicPanelFacets(
	FacetFactory, STargeter[])
	 */
	@Override
	protected SFacet[] newBasicPanelFacets(FacetFactory ff, 
			STargeter[] targeters) {
		
		//Get references to both targeters
		STargeter 
		greeting = targeters[TARGET_GREETING], 
		texts = targeters[TARGET_TEXTS];
		
		//Create and return array of facet
		return new SFacet[]{				
				
			//Label as usual
			ff.textualLabel(greeting, HINT_NONE), BREAK, 
			
			//Dropdown selector
			ff.indexingDropdownList(texts, HINT_NONE), BREAK, 
			
			//Iterating buttons
			ff.indexingIteratorButtons(texts, HINT_BARE), BREAK, 
			
			//Radio buttons
			ff.indexingRadioButtons(texts, HINT_BARE + HINT_TALL)
		};
	}
	
	/**
	Exposes choices and index in menu. 
	@see HelloContenter#newBasicMenuFacets(
	FacetFactory, STargeter[])
	 */
	@Override
	protected SFacet[] newBasicMenuFacets(FacetFactory ff, 
			STargeter[] targeters) {
		
		//Get references 
		STargeter 
		greeting = targeters[TARGET_GREETING], 
		choices = targeters[TARGET_TEXTS];
		
		//Create and return suitable facet
		return new SFacet[]{				
				
			//Iterating items
			ff.indexingIteratorItems(choices), 
			
			//Divider
			BREAK, 
			
			//Radio button items
			ff.indexingRadioButtonMenuItems(choices, HINT_NONE)
		};
	}
	
	
}