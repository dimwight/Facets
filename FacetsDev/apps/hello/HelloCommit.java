package apps.hello;

import static facets.facet.FacetFactory.*;
import facets.core.superficial.SFacet;
import facets.core.superficial.STarget;
import facets.core.superficial.STargeter;
import facets.core.superficial.STextual;
import facets.core.superficial.STrigger;
import facets.core.superficial.TargetCore;
import facets.facet.FacetFactory;




/**
Like {@link HelloField} but with commit and cancel buttons. 
 */
public final class HelloCommit extends HelloContenter {

	//Indexes into target and targeter arrays
	private static final int 
	TARGET_GREETING = 0, 
	TARGET_EDITS = 1, 
	TARGET_COMMIT_CANCEL = 2;
	
	//References to targets and group
	private STextual greeting, edits;
	private STrigger commit, cancel;
	private STarget commitCancel;

	/**
	Uses new target type and shared couplers. 
	@see HelloContenter#newBasicTargets()
	 */
	@Override
	protected STarget[] newBasicTargets() {
				
		//Coupler for textuals
		STextual.Coupler forTextuals = new STextual.Coupler(){

			//Sets update policy
			@Override
			public boolean updateInterim(STextual t) {
				
				//Accept interim updates
				return true;
			}
			
			//Respond to updates
			@Override
			public void textSet(STextual t) {
				
				//Call convenience method
				setTriggerLives();
			}
		};
		
		//The original label target
		greeting = new STextual("Untitled", 
				"Hello world!", forTextuals);
		
		//Separate target for edit field, with label text  
		edits = new STextual("Edit greeting here", 
				greeting.text(), forTextuals);
		
		//Coupler for triggers
		STrigger.Coupler forTriggers = new STrigger.Coupler() {

			//Respond to buttons or menu
			@Override
			public void fired(STrigger t) {

				//Copy state one way or other
				if (t == commit)greeting.setText(edits.text());
				else edits.setText(greeting.text());
			}
		};
		
		//Create grouped triggers
		commitCancel = new TargetCore("Commit or cancel?", 
			new STarget[]{
				commit = new STrigger("Commit", forTriggers), 
				cancel = new STrigger("Cancel", forTriggers)
			}
		);
		
		//Set initial live states 
		setTriggerLives();
		
		//Return array ordered to match TARGET_ constants
		return new STarget[] {
				greeting, 
				edits, 
				commitCancel
			};
	}
	
	/**
	Returns facet including button group. 
	@see HelloContenter#newBasicPanelFacets(
	FacetFactory, STargeter[])
	 */
	@Override
	protected SFacet[] newBasicPanelFacets(FacetFactory ff, 
			STargeter[] targeters) {
		
		//Get targets using TARGET_ constants		
		STargeter 
		greeting = targeters[TARGET_GREETING],
		edits = targeters[TARGET_EDITS],
		commitCancel = targeters[TARGET_COMMIT_CANCEL];
		
		//Create and return array of facet and dividers
		return new SFacet[]{
				//Label as before
				ff.textualLabel(greeting, HINT_NONE), BREAK, 
				//Text field for edits targeter
				ff.textualField(edits, 15, HINT_NONE), BREAK, 
				//Button group
				ff.triggerButtons(commitCancel, HINT_BARE)
			};
	}
	
	/**
	Creates trigger items for menu. 
	@see HelloContenter#newBasicMenuFacets(
	FacetFactory, STargeter[])
	 */
	@Override
	protected SFacet[] newBasicMenuFacets(FacetFactory ff, 
			STargeter[] targeters) {
		
		//Menu items to match button group
		return new SFacet[]{
				ff.triggerMenuItems(targeters[TARGET_COMMIT_CANCEL], 
						HINT_NONE)
			};
	}

	/**
	Sets trigger group live state based on textual states. 
	 */
	private void setTriggerLives() {
		
		//Compare textuals, set trigger group live state
		boolean oddTexts = !edits.text().equals(greeting.text());
		commitCancel.setLive(oddTexts);
	}
}