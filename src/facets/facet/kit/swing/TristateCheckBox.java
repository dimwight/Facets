package facets.facet.kit.swing;
import facets.util.Debug;
import facets.util.Util;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ActionMapUIResource;
class TristateCheckBox extends JCheckBox {
	
	//Store enabled state
	private boolean enabled=true;

	//Listener on model changes to maintain correct focusability
	final private ChangeListener modelListener=new ChangeListener(){
		
		public void stateChanged(ChangeEvent e){

			//Get model state, do HK change if necessary
			boolean enabledNow=getModel().isEnabled();
			if(enabledNow!=enabled)
				TristateCheckBox.this.setFocusable(enabled=enabledNow);
		}
	};

	/**
	Convenience constructor setting no icon. 
	 @param text passed to core constructor
	 */
	public TristateCheckBox(String text) {
		this(text, null);
	}

	/**
	Core constructor. 
	 @param text passed to superclass 
	 @param icon passed to superclass
	 */
	public TristateCheckBox(String text, Icon icon) {
		super(text, icon);
		
		//Set default single model
		setModel(new TristateButtonModel(false));
		
		//Clever stuff from HK
		super.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				TristateCheckBox.this.iterateState();
			}
		});
		ActionMap actions = new ActionMapUIResource();
		actions.put("pressed", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				TristateCheckBox.this.iterateState();
			}
		});
		actions.put("released", null);
		SwingUtilities.replaceUIActionMap(this, actions);
	}
	public TristateCheckBox(){
		this("TristateCheckBox");
	}
	//Next two methods implement new API by delegation to model
	public void setIndeterminate() {
		triModel().setIndeterminate();
	}
	public boolean isIndeterminate() {
		return triModel().isIndeterminate();
	}
	
	//Overrides superclass method
	public void setModel(ButtonModel newModel){
		super.setModel(newModel);
		
		//Listen for enable changes
		if(model instanceof TristateButtonModel)
			model.addChangeListener(modelListener);
	}

	//Empty override of superclass method
	public void addMouseListener(MouseListener l) {}

	//Mostly delegates to model 
	private void iterateState() {

		//Maybe do nothing at all?
		if(!enabled)return;
		
		//From HK
		grabFocus();
		
		//Iterate state and fire ActionEvent
		String command=triModel().iterateState();		
		fireActionPerformed(new ActionEvent(this, 0, command));
	}

	//Convenience cast
	private TristateButtonModel triModel() {
		return (TristateButtonModel) getModel();
	}
}
