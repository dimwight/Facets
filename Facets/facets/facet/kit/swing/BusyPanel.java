package facets.facet.kit.swing;
import facets.util.app.BusyCursor.BusySettable;
import java.awt.LayoutManager;
import javax.swing.JPanel;
public abstract class BusyPanel extends JPanel implements BusySettable{
	public BusyPanel(LayoutManager layout){
		super(layout);
	}
}