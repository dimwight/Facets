package facets.facet.kit.swing;
import static facets.facet.FacetFactory.*;
import static facets.facet.kit.swing.KitSwing.*;
import static facets.facet.kit.swing.ViewerBase.*;
import static java.awt.Color.*;
import static javax.swing.BorderFactory.*;
import facets.core.app.NestedView;
import facets.core.app.PagedContenter;
import facets.core.app.SAreaTarget;
import facets.core.app.ViewerTarget;
import facets.core.app.SContentAreaTargeter.ContentArea;
import facets.core.superficial.Notifying.Impact;
import facets.core.superficial.SFacet;
import facets.core.superficial.app.FacetedTarget;
import facets.facet.AreaFacets.PaneLinking;
import facets.facet.FacetFactory;
import facets.facet.kit.Decoration;
import facets.facet.kit.KMount;
import facets.facet.kit.KWrap;
import facets.facet.kit.KitCore;
import facets.facet.kit.KitFacet;
import facets.facet.kit.swing.KitSwing.Dimensioned;
import facets.facet.kit.swing.KitSwing.LaF;
import facets.util.Debug;
import facets.util.IndexingIterator;
import facets.util.Objects;
import facets.util.StringFlags;
import facets.util.Util;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.Painter;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
final class PaneTabs extends MountCore{
	private static UIDefaults nimbusUi;
	private final SFacet[]tabs;
	private final Pane pane;
	private final Container control;
	PaneTabs(KitCore kit,SFacet[]tabs,SFacet active,KWrap controlWrap){
		super(NO_FACET,null);
		this.tabs=tabs;
		JPanel base=new JPanel(new BorderLayout(){
			@Override
			public void addLayoutComponent(Component comp,Object constraints){
				if(constraints!=EAST)super.addLayoutComponent(comp,constraints);
			}
		}){
			@Override
			public void doLayout(){
				super.doLayout();
				if(control==null)return;
				Dimension size=getSize(),cSize=control.getPreferredSize();
				int tabGap=true?nimbusUi!=null?19:25
						:size.height-pane.getComponentAt(0).getHeight()-5,
					cHeight=cSize.height,padding=(tabGap-cHeight)/2;
				control.setBounds(new Rectangle(new Point(size.width-padding-cSize.width-2,
						size.height-padding-cHeight),cSize));
				setComponentZOrder(control,0);
			}
		};
		base.add(pane=new Pane(kit,active));
		pane.addMouseListener(mouse);
		if(controlWrap!=null){
			base.add(control=(Container)controlWrap.wrapped(),BorderLayout.EAST);
			for(Component c:allComponents(control))c.addMouseListener(mouse);
		}
		else control=null;
		if(true)adjustComponents(false,base);
		setSwing(base);
	}
	private final MouseAdapter mouse=new MouseAdapter(){
		@Override
		public void mousePressed(MouseEvent e){
			pane.ensureAreaActive();
		}
	};
	@Override
	public void setItem(KWrap item){
		if(item!=null)throw new IllegalArgumentException("Non-null item in "+this);
		pane.removeMouseListener(mouse);
		for(Component c:allComponents(control))c.removeMouseListener(mouse);
	}
	private static final int linkWidth=false?focusWidth():1;
	private static final Object atKey=new Object(){};
	final class Pane extends JTabbedPane implements Dimensioned{
		private int setAt;
		private boolean setting;
		private TabDragShower dragShow;
		public void paint(Graphics g){
			super.paint(g);
			if(dragShow!=null)dragShow.paint(g,this);
		}
		void setShowLinkable(JComponent c,boolean on){
			dragShow=on||TabDragShower.debug?
					new TabDragShower((int)c.getClientProperty(atKey)):null;
			repaint();
		}
		Pane(final KitCore kit,final SFacet active){
			super(BOTTOM);
			setFocusable(false);
			setAt=-1;
			new IndexingIterator<KitFacet>(Objects.newTyped(KitFacet.class,tabs)){
				protected void itemIterated(KitFacet item,int at){
					if(item==active||((SAreaTarget)item.target()).isActive())setAt=at;
					Decoration decorations=kit.decoration(item.title(),StringFlags.EMPTY);
					final JComponent tabbed=(JComponent)item.base().wrapped();
					JComponent tab=new JLabel(" "+decorations.caption+" ");
					tab.addMouseListener(new MouseAdapter(){
						public void mouseClicked(MouseEvent e){
							setSelectedComponent(tabbed);
							ensureAreaActive();
						}
					});
					tabbed.putClientProperty(PaneTabs.class,tab);
					tab.putClientProperty(Pane.class,Pane.this);
					tab.putClientProperty(atKey,at);
					addTab(null,true?null:(Icon)decorations.icon,tabbed);
					setTabComponentAt(at,tab);
					KitCore.widgets+=2;
				}
			}.iterate();
			if(setAt>=0)setSelectedIndex(setAt);
			addChangeListener(new ChangeListener(){
				public void stateChanged(ChangeEvent e){
					if(setting)ensureAreaActive();
				}
			});
		}
		@Override
		public void setSelectedIndex(int set){
			setting=true;
			super.setSelectedIndex(set);
			setting=false;
			int tabs=getTabCount();
			for(int at=0;at<tabs;at++){
				ViewerTarget viewer=(ViewerTarget)tabArea(at).activeFaceted();
				viewer.setLive(at==set||viewer.view()instanceof NestedView);
			}
			if(isDisplayable())tabArea(set).notifyParent(Impact.SELECTION);
		}
		void ensureAreaActive(){
			int tabAt=getSelectedIndex();
			if(tabAt<0)return;
			SAreaTarget target=tabArea(tabAt);
			target.ensureActive(Impact.ACTIVE);
			if(target instanceof ContentArea){
				Dimension size=((PagedContenter)((ContentArea)target).contenter).contentAreaSize();
				firePropertyChange(Dimensioned.PROPERTY,size.width,size.height<<8);
			}
		}
		private SAreaTarget tabArea(int at){
			return(SAreaTarget)((KitFacet)tabs[at]).target();
		}
		@Override
		public String toString(){
			return Debug.info(this)+" at="+getSelectedIndex();
		}
		public void updateUI(){
			super.updateUI();
			if(KitSwing.laf!=LaF.Nimbus)return;
			if(nimbusUi==null){
				buildNimbusUi();
			}
			putClientProperty("Nimbus.Overrides.InheritDefaults",true);
			putClientProperty("Nimbus.Overrides",nimbusUi);
		}
	}
	public void setActiveItem(KWrap item){
	  ((JTabbedPane)pane).setSelectedComponent((Component)item.wrapped());
	}
	public void setItems(KWrap... items){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	private static void buildNimbusUi(){
		UIDefaults laf=UIManager.getLookAndFeelDefaults();
		nimbusUi=new UIDefaults();
		for(final String state:new String[]{
				"Enabled+Pressed",
				"Disabled",
				"Enabled",
				"Enabled+MouseOver",
				"Enabled+Pressed",
		}){
			String areaKey="TabbedPane:TabbedPaneTabArea["+state+"].backgroundPainter";;
			final Painter area=(Painter)laf.get(areaKey);
			nimbusUi.put(areaKey,new Painter<JComponent>(){
				@Override
				public void paint(Graphics2D g,JComponent c,int width,int height){
					g=(Graphics2D)g.create();
					area.paint(g,c,width,height);
					final int depth=5,chop=2;
					g.translate(0,height-depth);
					g.setColor(false?red:c.getBackground());
					g.fillRect(width-=chop,0,chop,depth);
					g.fillRect(0,0,chop,depth);
					g.setColor(false?red:black);
					g.translate(0,1);
					g.drawLine(--width,0,width,depth);
					g.drawLine(chop,0,chop,depth);
				}
			});
		}
		nimbusUi.put("TabbedPane:TabbedPaneTab.contentMargins",new Insets(0,2,1,2));
		nimbusUi.put("TabbedPane:TabbedPaneTabArea.contentMargins",new Insets(1,20,3,20));
		if(false)nimbusUi.put("TabbedPane.tabOverlap",-2);
		for(final String state:new String[]{
				"Disabled",
				"Disabled+Selected",
				"Selected",
				"MouseOver+Selected",
				"Enabled",
				"Enabled+MouseOver",
				"Enabled+Pressed",
				"Focused+Selected",
				"Focused+Pressed+Selected",
				"Focused+MouseOver+Selected",
				"Pressed+Selected",
		}){
			String tabKey="TabbedPane:TabbedPaneTab["+state+"].backgroundPainter";
			final Painter tab=(Painter)laf.get(tabKey);
			nimbusUi.put(tabKey,new Painter<JComponent>(){
				@Override
				public void paint(Graphics2D g,JComponent b,int width,int height){
					g=(Graphics2D)g.create();
					tab.paint(g,b,width,height-1);
				}
			});
		}
	}
}