package facets.facet.kit.swing;
import facets.util.Debug;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.Icon;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;
import javax.swing.plaf.ActionMapUIResource;
final class TristateRadioButton extends JRadioButton{
  public TristateRadioButton(){this("Tristate",null);}
  public TristateRadioButton(String text,Icon icon){
    super(text,icon);
    if(true)throw new IllegalStateException("Needs updating in "+Debug.info(this));
    setModel(new TristateButtonModel(false));
    super.addMouseListener(new MouseAdapter(){
      public void mousePressed(MouseEvent e){
				grabFocus();
				triModel().iterateState();
				TristateRadioButton.this.fireActionEvent();
			}
    });
    ActionMap actions=new ActionMapUIResource();
    actions.put("pressed",new AbstractAction(){
      public void actionPerformed(ActionEvent e){
				grabFocus();
				triModel().iterateState();
				TristateRadioButton.this.fireActionEvent();
			}
    });
    actions.put("released",null);
    SwingUtilities.replaceUIActionMap(this,actions);
  }
  public void addMouseListener(MouseListener l){}
  public void setIndeterminate(){
  	triModel().setIndeterminate();
	}
  public void setSelected(boolean on){
		triModel().setSelected(on);
	}
	protected void fireActionEvent(){
		fireActionPerformed(new ActionEvent(this,0,"Tristate"));
	}
	private TristateButtonModel triModel(){
		return (TristateButtonModel)getModel();
	}
}
