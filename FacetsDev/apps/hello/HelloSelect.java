package apps.hello;

import static facets.facet.FacetFactory.*;
import facets.core.superficial.SFacet;
import facets.core.superficial.SFrameTarget;
import facets.core.superficial.STarget;
import facets.core.superficial.STargeter;
import facets.core.superficial.STextual;
import facets.facet.FacetFactory;

/**
Adds functionality of {@link HelloField} to {@link HelloChoose}. 
 */
final public class HelloSelect extends HelloContenter {

	/**
	Minimal container for mutable text. 
	 */
	private static class HelloText{

		//Directly accessible state
		String text;

		//Initialise field
		HelloText(String text) {
			this.text = text;
		}
		
		//Makes text appear in indexing facet
		@Override
		public String toString(){
			return text;
		}
	}

	/**
	Constructor creates array content. 
	 */
	HelloSelect(){
		
		//Create content array and pass to superclass
		super(new HelloText[] {
			new HelloText("Hello world!"),
			new HelloText("Hello Dolly!"),
			new HelloText("Hello, good evening and welcome!")
		});		
	}

	/**
	Supply target framing selection. 
	@see HelloContenter#newSelectionFrame(Object)
	 */
	@Override
	protected SFrameTarget newSelectionFrame(Object selection) {	
		
		//Define, create and return frame
		return new SFrameTarget(title(),selection) {

			//Return single target representing selected text
			@Override
			protected STarget[] lazyElements(){	
				
				//Cast to right type
				final HelloText text = (HelloText)framed;
				
				//Target as before with current text state
				STextual greeting = new STextual("Edit greeting here", 
						text.text, 
				
					//Enhanced coupler sets state of text
					new STextual.Coupler() {			
						
						@Override
						public void textSet(STextual t){
							
							//Set text state
							text.text = t.text();
						}
						
						//Update policy as before
						@Override
						public boolean updateInterim(STextual t) {
							return true;
						}			
					}
				);
				
				//Return target in array
				return new STarget[] {greeting};
			}
		};
	}

	/**
	Create suitable panel facets@see CodingContenter#newContentPanelFacets
	(FacetsFactory, Targeter[],Targeter,Targeter)
	 */
	@Override
	protected SFacet[] newContentPanelFacets(FacetFactory ff, 
			STargeter[] basic, STargeter indexing, STargeter selection) {
		
		//Get reference
		STargeter greeting = selection.elements()[0];
		
		//Create and return facet
		return new SFacet[]{				
			
			//Label and field as before
			ff.textualLabel(greeting, HINT_NONE), BREAK, 
			ff.textualField(greeting, 20, HINT_NONE), BREAK, 
			
			//List pane and buttons
			ff.indexingPaneSingle(indexing, 250, 4, HINT_TALL), BREAK, 
			ff.indexingIteratorButtons(indexing, HINT_BARE)
		};
	}

	/**
	Create facet giving view and control of selection. 
	@see HelloContenter#newContentMenuFacets
	(FacetFactory, STargeter[],STargeter,STargeter)
	 */
	@Override
	protected SFacet[] newContentMenuFacets(FacetFactory ff, 
			STargeter[] basic, STargeter indexing, STargeter selection) {
		
		//Create and return facet
		return new SFacet[]{				
				
			//Iterating items, divider, radio button items
			ff.indexingIteratorItems(indexing), BREAK, 
			ff.indexingRadioButtonMenuItems(indexing, HINT_NONE)
		};
	}	
}
