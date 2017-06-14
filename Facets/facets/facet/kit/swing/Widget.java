package facets.facet.kit.swing;
import facets.core.superficial.app.SurfaceStyle;
import facets.facet.FacetFactory;
import facets.facet.SwingPanelFacet;
import facets.facet.kit.Decoration;
import facets.facet.kit.KWidget;
import facets.facet.kit.KWrap;
import facets.facet.kit.KitCore;
import facets.facet.kit.KitFacet;
import facets.util.Debug;
import facets.util.Tracer;
import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.Timer;
class Widget extends Tracer implements KWidget,ActionListener{
	final class FocusRequester{
		private static final int limit=5;
		private int requests;
		private final JComponent swing;
		private final Timer requester=new Timer(300,new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if(++requests>limit)throw new RuntimeException(
						"Too many focus requests for "+Debug.info(swing));
				swing.requestFocusInWindow();
			}
		});
		private final FocusAdapter listener=new FocusAdapter() {
			public void focusGained(FocusEvent e){
				requester.stop();
			}
		};
		FocusRequester(JComponent swing){
			(this.swing=swing).addFocusListener(listener);
		}
		void startRequesting(){
			if(swing.isFocusOwner())return;
			swing.requestFocusInWindow();
			requester.start();			
		}
	}
	protected final KitFacet facet;
	protected Decoration decoration;
	protected Container swing;
	protected boolean debug;
	private boolean enabled;
	final Map<String,Container>swingMap=new HashMap();
	Widget(KitFacet facet,Container swing){
    if((this.facet=facet)==null)throw new IllegalArgumentException(
    		"Null facet in "+Debug.info(this));
		if(swing!=null)setSwing(swing);
	}
	final protected void setSwing(Container swing){
		if(swing==null)throw new IllegalArgumentException("Null swing in "+Debug.info(this));
		else this.swing=swing;
		if(FacetFactory.surfaceStyle==SurfaceStyle.BROWSER&&!(swing instanceof JButton))
			swing.setBackground(new Color(FacetFactory.panelShade.rgb()));
		swingMap.put(SwingPanelFacet.KEY_PANEL,swing);
		KitCore.widgets++;
		KitSwing.debugSwing(swing,facet,this);
	}
	public void redecorate(Decoration decorations){}
	private boolean isEnabled(){
		if(enabled!=swing.isEnabled())throw new IllegalStateException(
				"Bad enabled in "+Debug.info(this));
		else return enabled;
	}
	public void setEnabled(boolean enabled){
		swing.setEnabled(this.enabled=enabled);
	}
	public void setIndeterminate(boolean on){
		setEnabled(!on&&enabled);
	}
	public void actionPerformed(ActionEvent e){
		if(facet!=null)facet.targetNotify(this,false);
	}
	private boolean hasFocus(){
		return swing.isFocusOwner();
	}
	final public KitFacet facet(){
		return facet;
	}
  public Object wrapped(){
		if(swing==null)throw new IllegalStateException("Null swing in "+Debug.info(this));
		else return swing;
	}
  final public Object newWrapped(Object parent){
    throw new RuntimeException("Not implemented in "+Debug.info(this));
  }
  public String toString(){
		return Debug.info(this)+(facet==null?"":" "+facet);
	}
	final protected void notifyFacet(boolean interim){
		facet.targetNotify(this,interim);
	}
	final Container swingWrapped(KWrap wrap){
		if(wrap==null)throw new IllegalArgumentException("No wrap in "+Debug.info(this));
		else return(Container)wrap.wrapped();
	}
	final public Map<String,Container>components(){
		return swingMap;
	}
}
