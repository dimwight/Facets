package facets.facet.kit.swing;
import static facets.core.app.ActionAppSurface.*;
import static facets.facet.AreaFacets.*;
import static javax.swing.SwingUtilities.*;
import facets.core.app.ActionAppSurface;
import facets.core.app.AppConstants;
import facets.core.app.AppSpecifier;
import facets.core.app.AppWindowHost;
import facets.core.app.Dialogs;
import facets.core.app.SurfaceServices;
import facets.core.superficial.SFacet;
import facets.core.superficial.SNumeric;
import facets.core.superficial.app.SSurface;
import facets.core.superficial.app.SSurface.WindowAppSurface;
import facets.facet.FacetFactory.SimpleServices;
import facets.facet.app.FacetAppSpecifier;
import facets.facet.kit.KitCore;
import facets.util.NumberPolicy;
import facets.util.Times;
import facets.util.Tracer;
import facets.util.app.HostBounds;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
final class WindowHostSwing extends AppWindowHost{
	private static final boolean sequenceCheck=false;
	private final KitSwing kit;
	private final JFrame frame;
	private final JComponent headerBar;
	private final FeatureWrap features;
	private Component blockingPane;
	private Splash splash;
	private SimpleServices blocking;
	WindowHostSwing(WindowAppSurface app,AppSpecifier spec,KitSwing kit){
		super(app,spec);
		this.kit=kit;
		frame=new JFrame();
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter(){
		  public void windowClosing(WindowEvent e){
		  	WindowHostSwing.this.app.attemptClose();
			}
		});
		headerBar=((FacetAppSpecifier)spec).headerIsRibbon()?new RibbonBar():new JMenuBar();
		features=new FeatureWrap(kit,headerBar,new SNumeric(KEY_EXTRAS_SPLIT,
				stateApp.getOrPutDouble(KEY_EXTRAS_SPLIT,30),new SNumeric.Coupler(){
			public NumberPolicy policy(SNumeric n){
				return SASH_SPLIT_POLICY;
			}
			public void valueSet(SNumeric n){
				stateApp.put(KEY_EXTRAS_SPLIT,(int)n.value());
			}
		}));
		KitCore.widgets+=2;
		if(sequenceCheck)trace(".WindowHostSwing: ");
	}
	@Override
	protected void setWindowBounds(final Rectangle bounds){
		Runnable doSet=new Runnable(){public void run(){
			if(sequenceCheck)trace(".setWindowBounds.doSet");
			frame.setLocation(-100,-100);
			frame.setVisible(true);
			frame.setBounds(bounds);
			if(false)frame.invalidate();
		}};
		if(false)SwingUtilities.invokeLater(doSet);
		else doSet.run();
	}
	@Override
	public void openWindow(){
		super.openWindow();
		Runnable fillFrame=new Runnable(){public void run(){
			if(sequenceCheck)trace("openWindow.fillFrame:");
			Container content=frame.getContentPane();
			if(headerBar instanceof RibbonBar)content.add(headerBar,BorderLayout.NORTH);
			else frame.setJMenuBar((JMenuBar)headerBar);
			content.add((Component)features.wrapped(),BorderLayout.CENTER);
			ImageIcon icon=(ImageIcon)kit.getAppIcon(app.title());
			if(icon!=null)frame.setIconImage(icon.getImage());
			SwingUtilities.updateComponentTreeUI(frame);
		}};
		if(isEventDispatchThread())fillFrame.run();
		else invokeLater(fillFrame);
	}
	@Override
	public void openHostedSurface(){
		Runnable open=new Runnable(){public void run(){
			app.openApp();
		}};
		if(app.isSlave())open.run();
		else SwingUtilities.invokeLater(open);
	}
	@Override
	public void showExtras(boolean on){
		features.showExtras(on);		
	}
	@Override
	public FacetLayout newLayout(SFacet content,LayoutFeatures f){
		return features.newLayout(content,f);
	}
	@Override
	public void setLayout(FacetLayout layout){
		features.setLayout(layout);
		setInputBlocking(((FeatureWrap.Layout)layout).services);
		updateLayout(app);
	}
	private void setInputBlocking(final SurfaceServices services){
		final Tracer t=new Tracer("setInputBlocking"){
			@Override
			protected void traceOutput(String msg){
				if(false)super.traceOutput(msg);
				else if(false)Times.printElapsed("setInputBlocking"+msg);
			}
		};
		if(blockingPane==null){
			blockingPane=frame.getGlassPane();
			blockingPane.addMouseListener(new MouseAdapter(){});
			blockingPane.addFocusListener(new FocusListener(){
				@Override
				public void focusLost(FocusEvent e){
					t.trace(".focusLost: ",blockingPane.requestFocusInWindow());
				}
				@Override
				public void focusGained(FocusEvent e){
					t.traceDebug(".focusGained: ",e.getOppositeComponent());
				}
			});
			blockingPane.addKeyListener(new KeyAdapter(){
				@Override
				public void keyPressed(KeyEvent e){
					if(e.getModifiers()!=0)return;
					int code=e.getKeyCode();
					t.traceDebug(".keyPressed: ",KeyEvent.getKeyText(code));
					if(blocking!=null)blocking.handleBlockedKey(code);
				}
			});
		}
		blocking=services!=null&&services instanceof SimpleServices
			&&((SimpleServices)services).isBlocking()?(SimpleServices)services
				:null;
		t.traceDebug(".setInputBlocking: blocking=",blocking);
		blockingPane.setVisible(blocking!=null);
		if(blocking!=null)SwingUtilities.invokeLater(new Runnable(){public void run(){
			boolean focusInWindow=blockingPane.requestFocusInWindow();
			t.trace(".focusInWindow: ",focusInWindow);
		}});
	}
	@Override
	public HostBounds newBounds(){
		return new HostBounds(nature,stateApp){
			@Override
			protected Rectangle windowBounds(){
				Rectangle bounds=frame.getBounds();
				return frame.isVisible()?bounds:new Rectangle();
			}
			@Override
			protected Dimension screenSize(){
				if(true)return frame.getToolkit().getScreenSize();
				Rectangle virtualBounds=new Rectangle();
				GraphicsEnvironment ge=GraphicsEnvironment.getLocalGraphicsEnvironment();
				GraphicsDevice[]gs=ge.getScreenDevices();
				for(int j=0;j<gs.length;j++){
					GraphicsDevice gd=gs[j];
					GraphicsConfiguration[]gc=gd.getConfigurations();
					for(int i=0;i<gc.length;i++){
						virtualBounds=virtualBounds.union(gc[i].getBounds());
					}
				}
				return virtualBounds.getSize();
			}
			@Override
			protected boolean forSlave(){
				return app.isSlave();
			}
		};
	}
	@Override
	public void splashUp(String msg){
		(splash=new Splash(msg,
				kit.getDecorationIcon(AppConstants.NATURE_APP_ICON_LARGE, false))).up();
	}
	@Override
  public void splashDown(){
		if(splash!=null)splash.down();
	}
	@Override
	public void closeWindow(){
		frame.dispose();
	}
	@Override
	public Object wrapped(){
	  return frame;
	}
	@Override
	public void setTitle(String text){
		frame.setTitle(kit.decodeTitleText(text));
	}
	@Override
	public void updateLayout(SSurface surface){
		features.updateLayout(surface);		
	}
	@Override
	public SurfaceServices activeServices(){
		return features.activeServices();
	}
	@Override
	public final Dialogs newDialogs(ActionAppSurface app){
		return new DialogsSwing(app,kit);
	}
}
