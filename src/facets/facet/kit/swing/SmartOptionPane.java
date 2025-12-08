package facets.facet.kit.swing;
import facets.util.Debug;
import facets.util.Util;
import facets.util.ByteStrings.FileBytes;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;
public class SmartOptionPane extends JOptionPane{
	public static final Point DEFAULT_AT=new Point(-100,-100);
	private JDialog d;
	private static Point showAt;
	private final boolean resize;
	public SmartOptionPane(Point showAt,boolean resize){
		if((this.showAt=showAt)==null)throw new IllegalArgumentException(
				"Null showAt in "+Debug.info(this));
		this.resize=resize;
	}
	public void updateMessage(Object...values){
		setMessage(values.length==0?null:values[0]);
	}
	public Point displayInDialog(Component parent,String title){
		d=createDialog(parent==null||!parent.isVisible()?null:parent,"");
		if(parent==null)d.setIconImage(new ImageIcon(
				new FileBytes("C:/eclipse/workspace/Facets/_image/icon/facets16.gif",
						"789C5BF39681B5B4888129DA69CD67F11F6C1C210F9818182A0A18189867B97BBA5958260A3008307C67606015FC3F79DBA5297B1E2CD87161C6813BD30E3C5870E0EAC6334F0E5D7F7BE2C1D76B4F3E5DFFF0E3C1CB0F4FBE7DFFFAF3375031C3281805C30828FE64616460E067D0017140798281A39C419E43C663C38283CD1CCC021C020E0E8C4CAC7C260A2F163830B1704B59146C483878AC914FCA64C70EC39466560E211D8B0687836EAC1C5E5D2E331B161F60602F4ABBA36194DED0C92FF2E088C665473F56D6D253361B2E281C9EA2242891D0F0A191B193775958ED8184039EABA259BB78B827ADB9B9999191C11A00EC735B89B5030000"
					).getBytes()).getImage());
		d.setTitle(title);
		d.setResizable(resize);
		if(showAt!=DEFAULT_AT)d.setLocation(showAt);
		d.pack();
		JRootPane root=getRootPane();
		if(false)for(Component c:KitSwing.allComponents(this))if(c instanceof JButton){
				JButton button=(JButton)c;
				if(button.getText().startsWith("No"))
					root.setDefaultButton(button);
				Util.printOut("SmartOptionPane.displayInDialog: ",
						root.getDefaultButton().getText());
			}
		if(false)Util.printOut("SmartOptionPane.displayInDialog: ",KitSwing.componentTree(this));
		else if(false)root.addPropertyChangeListener("defaultButton",new PropertyChangeListener(){
			@Override
			public void propertyChange(PropertyChangeEvent evt){
				throw new RuntimeException("Not implemented in "+Debug.info(this));
			}
		});
	  d.setVisible(true);
	  return showAt=d.getLocation();
	}
	public static void main(String[]args){
		if(false){
			JDialog d=new JDialog((Window)null);
			d.setLayout(new GridLayout());
			for(String text:new String[]{"This","That"}){
				JButton b=new JButton(new AbstractAction(text){
					@Override
					public void actionPerformed(ActionEvent a){
						Util.printOut(a.getActionCommand());
					}
				});
				d.add(b);
				d.getRootPane().setDefaultButton(b);
			}
			d.pack();
			d.setVisible(true);
		}
	}
}