package apps.hello;

import static facets.facet.FacetFactory.*;
import facets.core.superficial.SFacet;
import facets.core.superficial.SFrameTarget;
import facets.core.superficial.STarget;
import facets.core.superficial.STargeter;
import facets.core.superficial.STextual;
import facets.facet.FacetFactory;




import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
Re-implements {@link HelloSelect} as subclass of {@link HelloListContenter}. 
 */
final public class HelloList extends HelloListContenter {

	/**
	Minimal container for mutable text. 
	 */
	private static class HelloText implements Serializable{

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
	Constructor creates {@link List} content. 
	 */
	HelloList(){
		
		//Create initial list and pass to superclass
		super(new ArrayList<Serializable>(Arrays.asList(
			new HelloText("Hello world!"),
			new HelloText("Hello Dolly!"),
			new HelloText("Hello, good evening and welcome!")
		)));		
	}

	/**
	Supply target framing selection. 
	@see HelloContenter#newSelectionFrame(Object)
	 */
	@Override
	protected SFrameTarget newSelectionFrame(Serializable indexed) {	
		
		//Define, create and return frame
		return new SFrameTarget(title(),indexed) {

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
	Implements abstract method. 
	@see HelloListContenter#newPanel(FacetFactory, 
	STargeter, STargeter,STargeter, STargeter)
	 */
	@Override
	protected SFacet newPanel(FacetFactory ff, STargeter root, 
			STargeter list, STargeter indexing, STargeter selection) {
		
		//Get reference
		STargeter greeting = selection.elements()[0];
			
		//Create facet
		SFacet[] facets ={				
			
			//Label and field as before
			ff.textualLabel(greeting, HINT_NONE), BREAK, 
			ff.textualField(greeting, 20, HINT_NONE), BREAK, 
			
			//List pane and buttons
			ff.indexingPaneSingle(indexing, 250, 6, HINT_TALL), BREAK, 
			ff.triggerButtons(list, HINT_USAGE_PANEL + HINT_BARE), BREAK, 
			ff.indexingIteratorButtons(indexing, HINT_BARE)
		};
		
		//Create and return panel
		return ff.rowPanel(selection, facets);
	}

	/**
	Implements abstract method. 
	@see HelloListContenter#newMenus(FacetFactory, 
	STargeter, STargeter,STargeter, STargeter)
	 */
	@Override
	protected SFacet[] newMenus(FacetFactory ff, STargeter root, 
			STargeter list, STargeter indexing, STargeter selection) {
		
		//Create facet
		SFacet[] facets ={				
			
			//Iterating items, divider, radio button items
			ff.indexingIteratorItems(indexing),
			ff.triggerMenu(list, HINT_NONE), BREAK, 
			ff.indexingRadioButtonMenuItems(indexing, HINT_NONE),
		};
		
		//Create and return single-item array
		return new SFacet[] {ff.menuRoot(selection, "Menu", facets)};
	}	
}
