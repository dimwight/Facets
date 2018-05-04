package facets.facet.kit.swing;
import static facets.facet.FacetFactory.*;
import static facets.facet.app.FacetAppActions.BarHide.*;
import static javax.swing.BorderFactory.*;
import facets.core.app.FeatureHost;
import facets.core.app.SurfaceServices;
import facets.core.app.SurfaceStyle;
import facets.core.superficial.SFacet;
import facets.core.superficial.SNumeric;
import facets.core.superficial.app.SSurface;
import facets.facet.FacetFactory;
import facets.facet.FacetFactory.AppletFeatures;
import facets.facet.app.FacetAppSurface;
import facets.facet.kit.KMount;
import facets.facet.kit.KWrap;
import facets.facet.kit.KitCore;
import facets.facet.kit.KitFacet;
import facets.util.Debug;
import facets.util.Tracer;
import facets.util.app.BusyCursor;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.JApplet;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
final class FeatureWrap extends Tracer implements FeatureHost{
	private static final CompoundBorder TOOLS_BORDER=new CompoundBorder(
			new MatteBorder(0,0,1,0,Color.GRAY),createEmptyBorder(2,2,2,2));
	private abstract class HidePanel extends BusyPanel{
		private final Border border;
		private final LayoutManager layout;
		private boolean hide;
		HidePanel(Border border,LayoutManager layout){
			super(layout);
			setBorder(this.border=border);
			setLayout(this.layout=layout);
			if(false)setBackground(Color.RED);
		}
		final public void setHidden(boolean on){
			hide=on;
			if(on){
				removeAll();
				setBorder(null);
				setLayout(null);
			}
			else{
				setBorder(border);
				setLayout(layout);
			}
		}
		protected abstract void setParts(Component...parts);
		final public Dimension getPreferredSize(){
			return hide?new Dimension(0,0):true?getMinimumSize():super.getPreferredSize();
		}
	}
	private final HidePanel swingSidebar=new HidePanel(createEtchedBorder(),
			new FlowLayout(FlowLayout.CENTER,5,3)){
		protected void setParts(Component... parts){
			add(parts[0]);
		}				
	},
	swingStatus=new HidePanel(createLoweredBevelBorder(),
			new FlowLayout(FlowLayout.LEFT,3,3)){
		protected void setParts(Component... parts){
			for(int i=0;i<parts.length;i++)add(parts[i]);
		}		
	},
	swingToolbar=new HidePanel(TOOLS_BORDER,null){		
		protected void setParts(Component... parts){
			KitSwing.addToolGroups(this,parts);
		}
	};
	final class Layout implements FacetLayout{
		final SurfaceServices services;
		private final JComponent menus[],toolbar,sidebar,status,extrasContent;
	  private final Component toolbarParts[],statusParts[];
		private boolean hideToolbar,hideSidebar,hideStatus;
		Layout(KWrap extrasContent,SFacet[]menus,SFacet toolbar,SFacet sidebar,
				SFacet status,SurfaceServices services,boolean toolsToSide){
			this.extrasContent=(JComponent)extrasContent.wrapped();
			if(menus!=null){
				List<JComponent>add=new ArrayList();
				for(SFacet m:menus)
					if(m!=null)add.add(facetBaseWrapped(m));
				this.menus=add.toArray(new JComponent[0]);
			}
			else this.menus=null;
			this.sidebar=sidebar==null?null:facetBaseWrapped(sidebar);	
			if(sidebar!=null)this.sidebar.setFont(headerBar.getFont());
			this.status=status==null?null:facetBaseWrapped(status);
			statusParts=status==null?null:this.status.getComponents();
			this.toolbar=toolbar==null?null:facetBaseWrapped(toolbar);
			toolbarParts=toolbar==null?null:this.toolbar.getComponents();
			this.services=services;
			if(appletStyle){
				Object toolsAt=toolsToSide?BorderLayout.EAST:BorderLayout.SOUTH,
					mainAt=toolsToSide?BorderLayout.WEST:BorderLayout.NORTH;
				swingBase.setLayout(new ToolsLayout(toolsAt));
				swingBase.add(swingContent,mainAt);
				swingBase.add(swingAppletTools,toolsAt);
			}
		}
		private void setHideToolbar(boolean hide){
			if(hideToolbar==hide)return;
			swingToolbar.setHidden(hideToolbar=hide||toolbarParts==null);
			if(!hideToolbar)swingToolbar.setParts(toolbarParts);
		}
		private void setHideSidebar(boolean hide){
			if(hideSidebar==hide)return;
			swingSidebar.setHidden(hideSidebar=hide||sidebar==null);
			if(!hideSidebar)swingSidebar.setParts(sidebar);
		}
		private void setHideStatus(boolean hide){
			if(hideStatus==hide)return;
			swingStatus.setHidden(hideStatus=hide||statusParts==null);
			if(!hideStatus)swingStatus.setParts(statusParts);
		}
		private JComponent facetBaseWrapped(SFacet facet){
			return(JComponent)((KitFacet)facet).base().wrapped();
		}
		void updateToSurface(SSurface surface){
			FacetAppSurface hides=(FacetAppSurface)surface;
			setHideToolbar(hides.hideLayoutBar(Toolbar));
			setHideSidebar(hides.hideLayoutBar(Sidebar));
			setHideStatus(hides.hideLayoutBar(Status));
			swingBase.validate();
		}
		void setSwingContents(){
			headerBar.setVisible(menus!=null);
			if(headerBar!=null){
				headerBar.removeAll();
				boolean normalMenubarPosition=headerBar.getParent()instanceof JLayeredPane;
				if(FacetFactory.surfaceStyle==SurfaceStyle.BROWSER&&!normalMenubarPosition){
					JApplet applet=(JApplet)SwingUtilities.getAncestorOfClass(JApplet.class,headerBar);
					if(applet!=null)applet.setJMenuBar(null);
				}
				if(menus!=null){
					MenuListener busyForce=new MenuListener(){
						@Override
						public void menuSelected(MenuEvent e){
							if(cursor!=null)kit.watcher.forceBusy(false);
						}
						@Override
						public void menuDeselected(MenuEvent e){}
						@Override
						public void menuCanceled(MenuEvent e){
							if(cursor!=null)kit.watcher.forceBusy(false);
						}
					};
					if(menus[0]instanceof JMenu){
						JMenuBar menuBar=headerBar instanceof JMenuBar?(JMenuBar)headerBar
							:((RibbonBar)headerBar).menus;
						for(JComponent item:menus){
					  	if(item==null)continue;
				  		JMenu menu=(JMenu)item;
				  		if(false&&!normalMenubarPosition)menu.getComponent(
				  				).setBackground(new Color(0xeeeeff));
				  		if(true)menu.addMenuListener(busyForce);
				  		menuBar.add(menu);
					  }
					}
					else((RibbonBar)headerBar).setTabPanels(menus);
				  headerBar.invalidate();
				  headerBar.repaint();
			  }
			}
			if(appletStyle&&toolbar!=null)swingAppletTools.add(toolbar);
			else{
				swingToolbar.setHidden(true);
				swingSidebar.setHidden(true);
				swingStatus.setHidden(true);
				if(toolbar!=null&&!hideToolbar){
					swingToolbar.setHidden(false);
					if(toolbarParts.length==0)throw new IllegalStateException(
							"Empty toolbar in "+Debug.info(this));
					swingToolbar.setParts(toolbarParts);
				}
				if(sidebar!=null&&!hideSidebar){
					if(sidebar.getComponentCount()==0)throw new IllegalStateException(
							"Empty sidebar in "+Debug.info(this));
					swingSidebar.setHidden(false);
					swingSidebar.setParts(new Component[]{sidebar});
				}
				if(status!=null&&!hideStatus){
					if(statusParts.length==0)throw new IllegalStateException(
							"Empty status in "+Debug.info(this));
					swingStatus.setHidden(false);
					swingStatus.setParts(statusParts);
				}
			}
			if(extrasMount==null)swingContent.add(extrasContent,BorderLayout.CENTER);
			swingBase.validate();
		}
	}
	private final class ExtrasMount extends MountCore{
		private final SNumeric ratio;
		private KWrap content,extras;
		private boolean hidden;
		ExtrasMount(SNumeric ratio){
			super(FacetFactory.NO_FACET,newSpreadPanel(false));
			this.ratio=ratio;
		}
		@Override
		public void setItems(KWrap...items){
			if((content=items[0])==null)throw new IllegalArgumentException(
					"Null content in "+Debug.info(this));
			extras=items[1];
			setHidden(true);
		}
		@Override
		public void setHidden(boolean hidden){
			if(content==null||this.hidden==hidden)return;
			this.hidden=hidden;
			if(hidden){
				super.setItems(content);
				return;
			}
			else if(extras==null)throw new IllegalStateException("Null extras in "+Debug.info(this));
			KMount mount=new SplitMount(FacetFactory.NO_FACET,true,ratio);
			super.setItems(mount);
			mount.setItems(new KWrap[]{extras,content});
		}
	}
	static BusyCursor cursor;
	private final Container swingBase=new BusyPanel(new BorderLayout()){},
	swingAppletTools=new JPanel(new BorderLayout()),swingContent;
	private final KitSwing kit;
	private final JComponent headerBar;
	private final boolean appletStyle;
	private final KMount extrasMount;
	private Layout active;
	private SFacet extras;
  @Override
	public FacetLayout newLayout(SFacet content,LayoutFeatures f){
		KWrap contentBase=facetBase(content);
		if(extrasMount!=null){
			if(extras==null)extras=f.extras();
			extrasMount.setItems(contentBase,extras==null?null:facetBase(extras));
		}
		return new Layout(extrasMount!=null?extrasMount:contentBase,
				f.header(),f.toolbar(),f.sidebar(),f.status(),f.services(),
				f instanceof AppletFeatures?((AppletFeatures)f).panelToSide:false);
	}
	FeatureWrap(KitSwing kit,JComponent headerBar,SNumeric extrasRatio){
		this.kit=kit;
		this.headerBar=headerBar!=null?headerBar:new JMenuBar();
		extrasMount=extrasRatio==null?null:new ExtrasMount(extrasRatio);
		swingContent=extrasMount==null?new JPanel(new BorderLayout()):(Container)extrasMount.wrapped();
		cursor=kit.watcher==null?null:
			cursor==null?kit.newBusyCursor(swingBase,headerBar):cursor;
		if(cursor!=null)kit.watcher.pushCursor(cursor);
		appletStyle=surfaceStyle!=SurfaceStyle.DESKTOP;
		if(appletStyle){
			if(surfaceStyle==SurfaceStyle.BROWSER){
	  		Color panelColor=new Color(false?0xff0000:FacetFactory.panelShade.rgb());
	  		swingBase.setBackground(panelColor);
	  		if(headerBar!=null){
	  			((JMenuBar)headerBar).setBorderPainted(false);
	  			headerBar.setBackground(panelColor);
	  			headerBar.setVisible(false);
	  		}
	  		swingContent.setBackground(panelColor);
	  		swingAppletTools.setBackground(panelColor);
			}
		}
		else if(false){
			Object toolsAt=BorderLayout.NORTH,restAt=BorderLayout.SOUTH;
	    swingBase.setLayout(new ToolsLayout(toolsAt));
			swingBase.add(swingToolbar,toolsAt);
			JPanel rest=new JPanel(new GridBagLayout(){{
				columnWeights=new double[]{1,0};
				rowWeights=new double[]{1,0};
			}});
			swingBase.add(rest,restAt);
			rest.add(swingContent,new GridBagConstraints(){{
				gridx=0;fill=BOTH;
			}});
			rest.add(swingSidebar,new GridBagConstraints(){{
				gridx=1;anchor=NORTHWEST;fill=BOTH;
			}});
			rest.add(swingStatus,new GridBagConstraints(){{
				gridx=0;gridy=1;gridwidth=2;
				anchor=WEST;fill=BOTH;
			}});
		}
		else{
			swingBase.setLayout(new ToolsLayout(BorderLayout.NORTH));
			swingBase.add(swingToolbar,BorderLayout.NORTH);
			JPanel rest=new JPanel(new BorderLayout());
			swingBase.add(rest,BorderLayout.SOUTH);
			rest.add(swingContent,BorderLayout.CENTER);
			rest.add(swingSidebar,BorderLayout.EAST);
			rest.add(swingStatus,BorderLayout.SOUTH);
		}
		KitCore.widgets+=5;
		KitSwing.debugSwing(swingBase,null,this);
	}
	@Override
	public void showExtras(boolean on){
		if(extrasMount!=null)extrasMount.setHidden(!on);
		((JComponent)swingContent).revalidate();
	}
	private KWrap facetBase(SFacet facet){
		return((KitFacet)facet).base();
	}
	@Override
	public void setLayout(FacetLayout layout){
		if(layout==null)throw new RuntimeException("Return or not implemented in "+Debug.info(this));
		else if(active==layout)return;
		if(kit.watcher!=null)kit.watcher.forceBusy(false);
		(this.active=(Layout)layout).setSwingContents();
		if(cursor!=null)SwingUtilities.invokeLater(new Runnable(){public void run(){
			kit.watcher.forceBusy(false);
		}});
	}
	@Override
	public void updateLayout(SSurface surface){
		if(active!=null)active.updateToSurface(surface);
	}
	@Override
	public SurfaceServices activeServices(){
		SurfaceServices services=active.services;
		if(false&&services==null)throw new IllegalStateException("No services set in "+Debug.info(active));
		return services;
	}
	@Override
	public String toString(){
		return super.toString()+(true?""
				:":{\n"+Debug.info(swingContent.getComponent(0))
		+"\n"+Debug.info(headerBar.getComponent(0))
		+"\n"+Debug.info(swingToolbar.getComponent(0))
		+"\n}");
	}
	@Override
	public Object wrapped(){
		return swingBase;
	}
	@Override
	public void setTitle(String title){}
	@Override
	public void openHostedSurface(){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
}