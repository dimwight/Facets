package facets.facet.kit.swing;
import static java.awt.Cursor.*;
import facets.util.Debug;
import facets.util.Tracer;
import facets.util.app.BusyCursor;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.JMenuBar;
final class BusyCursorSwing extends BusyCursor{
	private final JComponent swingMenus;
	private final Container swing;
	private final Tracer t=Tracer.newTopped(Debug.info(this),false);
	BusyCursorSwing(JComponent menuBar,Container swing){
		this.swingMenus=menuBar;
		this.swing=swing;
	}
	@Override
	protected void setCursors(boolean busy,Set<BusySettable>settables){
		Cursor set=getPredefinedCursor(busy?WAIT_CURSOR:DEFAULT_CURSOR);
		if(swingMenus!=null)swingMenus.setCursor(set);
		if(true||busy)for(BusySettable c:settables)((Component)c).setCursor(set);
	}
	@Override
	protected void addSettables(Set<BusySettable>settables){
		Component[]all=KitSwing.allComponents(swing);
		for(Component c:all)
			if(c instanceof BusySettable)settables.add((BusySettable)c);
		t.traceDebug(".addSettables: swing="+Debug.info(swing)
				+"\n\tall="+all.length+" settables=",settables.toArray());
	}
}