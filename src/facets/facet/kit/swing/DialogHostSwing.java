package facets.facet.kit.swing;
import static facets.facet.FacetFactory.*;
import static facets.facet.kit.swing.KitSwing.*;
import static java.awt.BorderLayout.*;
import static java.lang.Math.*;
import facets.core.app.AppConstants;
import facets.core.app.AppWindowHost;
import facets.core.app.Dialogs.Response;
import facets.core.superficial.SFacet;
import facets.core.superficial.app.SSurface;
import facets.core.superficial.app.SSurface.DialogSurface;
import facets.facet.kit.DialogHost;
import facets.facet.kit.KitFacet;
import facets.facet.kit.swing.KitSwing.Dimensioned;
import facets.util.Debug;
import facets.util.app.BusyCursor;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
final class DialogHostSwing extends DialogHost{
	private final Container swingBase=new BusyPanel(new BorderLayout()){
		private final KeyAdapter escaper=new KeyAdapter(){
			public void keyPressed(KeyEvent e){
				if(e.getKeyCode()==KeyEvent.VK_ESCAPE)closeDialog(Response.Cancel);
			}
		};
		public void doLayout(){
			super.doLayout();
			for(Component each:KitSwing.allComponents(this)){
				each.removeKeyListener(escaper);
				each.addKeyListener(escaper);
			}
		}
	};
	private final BusyCursor busy;
	private String lastTitle;
	private Container contentSwing,extendSwing,extrasSwing;
	private boolean extendSideways,hide;
	private int extendAmount;
	private JButton defaultable;
	DialogHostSwing(AppWindowHost appHost,KitSwing kit){
		super(appHost,kit);
		busy=kit.newBusyCursor(swingBase,null);
	}
	@Override
	protected Object newWrapped(){
		Window parent=appHost==null?null:(Window)appHost.wrapped();
		final JDialog swing=new JDialog((parent!=null&&parent.isVisible()?parent:null),"");
		if(false&&parent==null)swing.setIconImage(
				((ImageIcon)kit.getAppIcon(AppConstants.NATURE_APP_ICON_LARGE)).getImage());
		swing.setModal(true);
		swing.addWindowListener(new WindowAdapter(){
			@Override
			public void windowClosing(WindowEvent e){
				response=Response.Cancel;
			}
		});
		swing.add(swingBase);
		return swing;
	}
	@Override
	protected void buildDialog(KitFacet content,KitFacet buttons,KitFacet extras, 
			final Dimension trimmings){
		if(swingBase.getComponentCount()>0)throw new RuntimeException(
				"Update not implemented in "+Debug.info(this));
		Container swingButtons=new JPanel(new FlowLayout(FlowLayout.RIGHT));
		swingButtons.add((Component)buttons.base().wrapped());
		for(Component c:KitSwing.allComponents(swingButtons))if(c instanceof JButton){
				JButton b=(JButton)c;
				if(b.isDefaultCapable())defaultable=b;
			}
		swingBase.add(swingButtons,SOUTH);
		contentSwing=(Container)content.base().wrapped();
		for(final Component c:KitSwing.allComponents(contentSwing))
			if(c instanceof Dimensioned)
				c.addPropertyChangeListener(new PropertyChangeListener(){
					@Override
					public void propertyChange(PropertyChangeEvent e){
						if(hide||e.getPropertyName()!=Dimensioned.PROPERTY)return;
						Dimension size=new Dimension((Integer)e.getOldValue(),((Integer)e.getNewValue())>>8);
						final JDialog swing=(JDialog)wrapped;
						Dimension t=trimmings,check=new Dimension(px(t.width)+TRIM.width,
								px(t.height)+TRIM.height);
						Dimension sizeThen=swing.getSize(),sizeNow=new Dimension(
							max(sizeThen.width,px(size.width)+check.width),
							max(sizeThen.height,px(size.height)+check.height)	
						);
						trace(".propertyChange: sizeThen=",sizeThen.height);
						if(!sizeNow.equals(sizeThen))swing.setSize(sizeNow);
					}
				});
		if(extras!=null)extrasSwing=(Container)extras.base().wrapped();
	}
	@Override
	public void updateLayout(SSurface surface){
		boolean extras=extrasSwing!=null,showGraph=extras&&graphShowWhere==GRAPH_DIALOGS;
		if(extras)swingBase.remove(extrasSwing);
		swingBase.remove(contentSwing);
		if(showGraph)swingBase.add(extrasSwing,CENTER);
		swingBase.add(contentSwing,showGraph?EAST:CENTER);
		swingBase.validate();
	}
	@Override
	public void setWindowExtension(SFacet extension,boolean sideways){
		if(extension!=null&&extendSwing!=null)return;
		if(false)traceDebug(".setWindowExtension: extension=",extension);
		if(extendSwing!=null)swingBase.remove(extendSwing);
		extendSwing=extension==null?null:(Container)((KitFacet)extension).base().wrapped();
		if(!(extendSideways=sideways))throw new RuntimeException(
				"Not implemented for extendSideways=" +extendSideways+" in "+Debug.info(this));
		if(extendSwing!=null)swingBase.add(extendSwing,BorderLayout.EAST);
		swingBase.validate();
		if(wrapped!=null&&((JDialog)wrapped).isVisible())adjustExtend();
	}
	@Override
	protected void launchSurfaceInWindow(DialogSurface surface){
		hide=false;
		Rectangle bounds=scaledLaunchBounds(surface,kit.layoutFactor());
		JDialog swing=(JDialog)wrapped;
		swing.getRootPane().setDefaultButton(defaultable);
		if(swing.getParent()==null&&appHost!=null){
			Dimension box=bounds.getSize();
			bounds=new Rectangle(appHost.newBounds().calculateSmartDialogAt(box),box);
		}
		bounds.width+=TRIM.width;
		bounds.height+=TRIM.height;
		if(true)traceDebug(".launchSurfaceInWindow: noParent=" +(swing.getParent()==null)+
				" bounds="+bounds.height+" extendSwing=",extendSwing);
		swing.setBounds(bounds);
		swing.setResizable(surface.isResizable());
		swing.setTitle(kit.decodeTitleText(lastTitle));
		adjustExtend();
		((KitSwing)kit).watcher.pushCursor(busy);
		SwingUtilities.updateComponentTreeUI(swing);
		swing.setVisible(true);
		((KitSwing)kit).watcher.popCursor();
		adjustExtend();
	}
	@Override
	protected Rectangle getClosingBounds(){
		JDialog swing=(JDialog)wrapped;
		Rectangle bounds=swing.getBounds();
		if(false&&swing.getParent()==null)wrapped=null;
		if(true)trace(".getClosingBounds: bounds=",bounds.getSize().height);
		bounds.width-=TRIM.width;
		bounds.height-=TRIM.height;
		return bounds;
	}
	@Override
	public void hide(Response response){
		hide=true;
		closeDialog(response);
	}
	private void closeDialog(Response response){
		this.response=response;
		if(wrapped==null)throw new IllegalStateException(
				"Null wrapped in "+Debug.info(this));
		else((JDialog)wrapped).setVisible(false);
	}
	@Override
	final protected void traceOutput(String msg){
		if(false)super.traceOutput(msg);
	}
	private void adjustExtend(){
		final JDialog swing=(JDialog)wrapped;
		if(false)traceDebug(".adjustExtend: extendAmount="+extendAmount+" extendSwing=",extendSwing);
		Runnable adjustSize=new Runnable(){public void run(){
			Dimension then=swing.getSize();
			swing.setSize(then.width+(extendSideways?extendAmount:0),
					then.height+(extendSideways?0:extendAmount));
			trace(".adjustExtend: then.width="+then.width+" extendAmount="+extendAmount
					+" swing="+swing.getWidth());
		}};
		if(extendSwing!=null&&extendAmount==0){
			extendAmount=extendSideways?extendSwing.getWidth():extendSwing.getHeight();
			if(extendAmount==0)throw new IllegalStateException(
					"Zero extendAmount in "+Debug.info(this));
			adjustSize.run();
		}
		else if(extendAmount>0){
			extendAmount*=-1;
			adjustSize.run();
			extendAmount=0;
		}
		if(false)swingBase.validate();
		swing.validate();
	}
	@Override
	public final void setTitle(String title){
		lastTitle=title;
		if(wrapped!=null)((JDialog)wrapped).setTitle(kit.decodeTitleText(lastTitle));
	}
}