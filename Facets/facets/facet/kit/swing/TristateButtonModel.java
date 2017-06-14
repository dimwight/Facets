package facets.facet.kit.swing;

import javax.swing.ButtonModel;
import javax.swing.JToggleButton;

/**
Adds tri-state behaviour to  <code>JToggleButton.ToggleButtonModel</code>. 
 <p>{@link TristateButtonModel} is the model used by  
 {@link TristateCheckBox}; it could equally be used by an analogous 
{@link TristateRadioButton}. 
 <p>Adding to the interface of {@link ButtonModel} the methods
 {@link #setIndeterminate()} and {@link #isIndeterminate()}, 
 it can be similarly shared between button instances. 
 <p>Core functionality is adapted from code by Heinz Kabutz (HK)
 originally at http://www.javaspecialists.co.za/archive/Issue082.html. 
 */
final class TristateButtonModel extends JToggleButton.ToggleButtonModel {

	/*State constants and current value - private so no need for type 
	 safety.*/
	private static final int SELECTED = 0, NOT_SELECTED = 1, INDETERMINATE = 2;
	private int state;
	private final boolean tristateAction;

	public TristateButtonModel(boolean tristateAction) {
		this.tristateAction = tristateAction;
		setAndDisplayState(NOT_SELECTED);
	}

	/**
	Adds tri-state behaviour.    
	 */
	public void setIndeterminate() {
		setAndDisplayState(INDETERMINATE);
	}
	/**
	Returns <code>true</code> where <code>setIndeterminate</code> has been
	 called later than <code>setSelected</code>.
	 */
	public boolean isIndeterminate() {
		return state == INDETERMINATE;
	}

	//Overrides of superclass methods	
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		
		//Restore state display
		setAndDisplayState(state);
	}
	public void setSelected(boolean selected) {
		setAndDisplayState(selected
				? SELECTED : NOT_SELECTED);
	}

	//Empty overrides of superclass methods
	public void setArmed(boolean b) {
	}
	public void setPressed(boolean b) {
	}

	//Called by button on spacebar or mouse click.  
	String iterateState() {
		setAndDisplayState(state == NOT_SELECTED
				? SELECTED : state == SELECTED && tristateAction
						? INDETERMINATE : NOT_SELECTED);
		
		//Return command string for ActionEvent
		return "TristateButton " + (state == SELECTED
				? "SELECTED" : state == NOT_SELECTED
						? "NOT_SELECTED" : "INDETERMINATE");
	}

	//Stores state, displays using calls to superclass
	private void setAndDisplayState(int state) {

		//Set internal state
		this.state = state;
		
		//Adapted from HK
		super.setArmed(state == INDETERMINATE);
		super.setRollover(state == INDETERMINATE);
		super.setSelected(state != NOT_SELECTED);
	}
}