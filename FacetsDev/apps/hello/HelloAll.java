package apps.hello;

import static facets.facet.FacetFactory.*;
import facets.core.superficial.SFacet;
import facets.core.superficial.SFrameTarget;
import facets.core.superficial.SIndexing;
import facets.core.superficial.SNumeric;
import facets.core.superficial.STarget;
import facets.core.superficial.STargeter;
import facets.core.superficial.STextual;
import facets.core.superficial.SToggling;
import facets.core.superficial.STrigger;
import facets.core.superficial.TargetCore;
import facets.core.superficial.STextual.Coupler;
import facets.facet.FacetFactory;
import facets.util.NumberPolicy;
import facets.util.Util;

/**
Combines functionality of {@link HelloSelect}, {@link HelloCommit},
{@link HelloSpaces} and {@link HelloLimit}. 
 */
final public class HelloAll extends HelloContenter{

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

	//Indexes into target and targeter arrays
	private static final int 
	BASIC_SPACES= 0, 
	BASIC_LIMIT = 1, 
	BASIC_COMMIT_CANCEL = 2, 
	SELECTION_GREETING = 0, 
	SELECTION_EDITS = 1;
	
	//References to basic targets
	private SToggling spaces;
	private SNumeric limit;
	private STrigger commit, cancel;
	private STarget commitCancel;
	
	//For checking selection change, storing edits
	private HelloText textThen;
	private String edits;

	/**
	Constructor creates array content. 
	 */
	HelloAll(){
		
		//Create content array and pass to superclass
		super(new Object[]{
			new HelloText("Hello world!"),
			new HelloText("Hello Dolly!"),
			new HelloText("Hello, good evening and welcome!")
		});		
	}
	
	/**
	Creates constraints and triggers. 
	@see HelloContenter#newBasicTargets()
	 */
	@Override
	protected STarget[] newBasicTargets() {
		
		//Defines spaces policy
		spaces = new SToggling("Allow spaces", true, 
			
			//Custom coupler
			new SToggling.Coupler() {
				
				//Updates greeting text if required by flag state
				@Override
				public void stateSet(SToggling t) {
					
					//Call validation method
					checkEditsToConstraints();
				}
			}
		);
		
		//Define range of limits
		final int limitMin = 10, limitMax = 35;
	
		//Defines limit policy
		limit = new SNumeric("Length limit", limitMax, 
			
			//Custom coupler
			new SNumeric.Coupler() {
				
				//Ensures greeting text is within limit
				@Override
				public void valueSet(SNumeric n) {
					
					//Call validation method
					checkEditsToConstraints();
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
		
		//Shared coupler for triggers
		STrigger.Coupler forTriggers = new STrigger.Coupler() {

			//Respond to buttons or menu
			@Override
			public void fired(STrigger t) {

				HelloText selectionText = (HelloText) selection();
				
				//Copy state one way or other
				if (t == commit)selectionText.text = edits;
				else edits = selectionText.text;
			}
		};
		
		//Create grouped triggers
		commitCancel = new TargetCore("Commit or cancel?", 
			new STarget[]{
				commit = new STrigger("Commit", forTriggers), 
				cancel = new STrigger("Cancel", forTriggers)
			}
		);
		
		//Return array to match BASIC constants
		return new STarget[]{spaces, limit, commitCancel};
	}

	/**
	Creates selection targets. 
	@see HelloContenter#newSelectionFrame(Object)
	 */
	@Override
	protected SFrameTarget newSelectionFrame(Object selection) {	
		
		//Define, create and return frame
		return new SFrameTarget(title(),selection) {
			
			//References to targets and group
			private STextual greeting, edits;

			//Return target representing selected text and actions
			@Override
			protected STarget[] lazyElements(){	
				
				//Cast to right type
				final HelloText selectionText = (HelloText)framed;
				
				//Reset edits on selection change, reset check
				if(textThen != selectionText) 
					HelloAll.this.edits = selectionText.text;
				textThen = selectionText;
				
				//Ensure edits meet constraints
				checkEditsToConstraints();
				
				//Label target with selection text, default coupler
				greeting = new STextual("Untitled", selectionText.text, 
						new STextual.Coupler());
				
				//Edit field, with custom coupler  
				edits = new STextual("Edit greeting here", HelloAll.this.edits, 
						new STextual.Coupler(){			
					
					//Combines validation logic
					@Override
					public boolean isValidText(STextual t, String text){
						
						//Get default and checks
						boolean 
						superValid = super.isValidText(t, text),
						tooLong = text.length() > limit.value(),
						okSpaces = spaces.isSet() || text.indexOf(" ") < 0;
						
						//Return answer including new check
						return superValid && !tooLong && okSpaces;
					}

					//Sets update policy
					@Override
					public boolean updateInterim(STextual t) {
						
						//Accept interim updates
						return true;
					}
					
					//Respond to updates
					@Override
					public void textSet(STextual t) {

						//Set text state
						HelloAll.this.edits = t.text();					
					}
				});
				
				//Compare textuals, set trigger group live state
				boolean oddTexts = !edits.text().equals(greeting.text());
				commitCancel.setLive(oddTexts);
				
				//Return array ordered to match SELECTION constants
				return new STarget[] {greeting, edits};
			}
		};
	}

	/**
	Encapsulates checks. 
	 */
	private void checkEditsToConstraints() {
		
		//Make sure text is not too long
		int length = Math.min(edits.length(), (int) limit.value());
		edits = edits.substring(0, length);
		
		//Has the flag been reset?
		if (!spaces.isSet()) {

			//Make sure text is valid
			edits = edits.replaceAll(" ", "");
		}
	}
	

	/**
	Re-implementation to create suitable panel.
	@see HelloContenter#newContentPanelFacets
	(FacetFactory, STargeter[],STargeter, STargeter)
	 */
	@Override
	protected SFacet[] newContentPanelFacets(FacetFactory ff, 
			STargeter[] basic, STargeter indexing, STargeter selection) {
		
		//Get references
		STargeter 
		selectionElements[] = selection.elements(),
		greeting = selectionElements[SELECTION_GREETING],
		edits = selectionElements[SELECTION_EDITS],
		commitCancel = basic[BASIC_COMMIT_CANCEL],
		spaces = basic[BASIC_SPACES],
		limit = basic[BASIC_LIMIT];
		
		//Create and return facet
		return new SFacet[]{		
			
			//Label and field with spacer row
			ff.textualLabel(greeting, HINT_NONE), BREAK, 
			ff.textualField(edits, 20, HINT_NONE), BREAK, 
			ff.triggerButtons(commitCancel, HINT_NONE), BREAK, 
			
			//List pane and buttons
			ff.indexingPaneSingle(indexing, 200, 4, HINT_TALL), BREAK, 
			ff.indexingIteratorButtons(indexing, HINT_BARE), BREAK, 
			
			//Numeric slider and field
			ff.numericSliders(limit, 150, 
					HINT_SLIDER_FIELDS_TICKS_LABELS + HINT_TALL), 
			ff.spacerWide(15),
			
			//Nested panel with buttons and checkbox
			ff.rowPanel(limit, 
				ff.numericNudgeButtons(limit, HINT_BARE), BREAK, 
				ff.spacerTall(10), BREAK, 
				ff.togglingCheckboxes(spaces, HINT_BARE)
			)
		};
	}

	/**
	Re-implementation giving view and control of selection. 
	@see HelloContenter#newContentMenuFacets(
	FacetFactory, STargeter[],STargeter, STargeter)
	 */
	@Override
	protected SFacet[] newContentMenuFacets(FacetFactory ff, 
			STargeter[] basic, STargeter indexing, STargeter selection) {
		
		//Create and return facet
		return new SFacet[]{				
				
			//Commit items, divider
			ff.triggerMenuItems(basic[BASIC_COMMIT_CANCEL], 
						HINT_NONE), BREAK, 
				
			//Radio button menu, iterating items, divider 
			ff.indexingRadioButtonMenu(indexing, HINT_NONE), 
			ff.indexingIteratorItems(indexing), BREAK, 
			
			//Constraint item and menus
			ff.togglingCheckboxMenuItems(basic[BASIC_SPACES], HINT_NONE),
			ff.numericNudgeMenu(basic[BASIC_LIMIT], HINT_NONE)
		};
	}	
}
