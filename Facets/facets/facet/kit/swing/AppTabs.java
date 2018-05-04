package facets.facet.kit.swing;
import static facets.core.app.AppActions.*;
import static facets.facet.kit.Toolkit.*;
import static javax.swing.BorderFactory.*;
import facets.core.superficial.Notifying.Impact;
import facets.core.app.SAreaTarget;
import facets.core.superficial.SFacet;
import facets.core.superficial.SNumeric;
import facets.core.superficial.STargeter;
import facets.facet.FacetFactory;
import facets.facet.app.FacetAppSurface;
import facets.facet.kit.KWrap;
import facets.facet.kit.KitCore;
import facets.facet.kit.KitFacet;
import facets.facet.kit.swing.KitSwing.TabDragShower;
import facets.util.Debug;
import facets.util.StringFlags;
import facets.util.Tracer;
import facets.util.app.BusyCursor.BusySettable;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
final class AppTabs extends JTabbedPane implements BusySettable{
	private final Tracer t=Tracer.newTopped("AppTabs",true);
	private final class Tab extends JPanel{
		private final MouseListener mouse=new MouseAdapter(){
			public void mouseEntered(MouseEvent e){
				close.setIcon(e.getSource()==close?closeIconFire:closeIconHi);
			}
			public void mouseExited(MouseEvent e){
				close.setIcon(itemArea().isActive()?closeIconHi:closeIconLo);
			}
			public void mouseClicked(MouseEvent e){
				setSelectedComponent((Component)item.wrapped());
				if(e.getSource()==close)app.tryCloseContent();
			}
			public void mouseReleased(MouseEvent e){
				if(e.isPopupTrigger())popupClicked(e);
			}
			public void mousePressed(MouseEvent e){
				if(e.isPopupTrigger())popupClicked(e);
			}
		};
		private final KWrap item;
		private final JLabel caption,close;
		private final Icon closeIconLo,closeIconHi,closeIconFire;
		void update(boolean active){
			String title=itemArea().title();
			caption.setText(" "+kit.decoration(title,StringFlags.EMPTY).tabCaption()+" ");
			caption.setIcon((Icon)kit.getDecorationIcon(title.replace("([^.]+).*","$1"),false));
			caption.setToolTipText(title.replaceAll("([^:]+).*","$1"));
			close.setIcon(active?closeIconHi:closeIconLo);
			if(setting)return;
			else if(active)itemArea().ensureActive(Impact.SELECTION);
		}
		private SAreaTarget itemArea(){
			return(SAreaTarget)item.facet().target();
		}
		private void popupClicked(MouseEvent e){
			if(getTabComponentAt(getSelectedIndex())!=this)return;
			Point at=e.getPoint();
			((JMenu)((KitFacet)popups.toolbar()).base().wrapped()).getPopupMenu().show(
					this,at.x,at.y);
		}
		Tab(KWrap[]items,final int at){
			super(new FlowLayout(0,0,0));
			item=items[at];
			final Component c=(Component)item.wrapped();
			AppTabs.this.addTab(null,c);
			AppTabs.this.setTabComponentAt(at,this);
			setOpaque(false);
			addMouseListener(mouse);
			caption=new JLabel(Debug.info(this));
			KitSwing.adjustComponents(false,caption);
			add(caption);
			closeIconLo=newTabIcon(kit,KEY_TAB_CLOSE_LO);
			closeIconHi=newTabIcon(kit,KEY_TAB_CLOSE_LIVE);
			closeIconFire=newTabIcon(kit,KEY_TAB_CLOSE_FIRE);
			close=new JLabel(closeIconLo,0);
			int w=2;
			caption.setBorder(createEmptyBorder(w,w,w,w));
			caption.putClientProperty(atKey,at);
			drags.addComponent(caption);
			if(items.length==1)return;
			caption.addMouseListener(mouse);
			if(!app.canCloseContent())return;
			add(close);
			close.addMouseListener(mouse);
			close.setToolTipText(kit.getDecorationText(KEY_TAB_CLOSE_LO,true));
			update(true);
		}
	}
	public void paint(Graphics g){
		super.paint(g);
		if(dragShow!=null)dragShow.paint(g,this);
	};
	private static final Object atKey=new Object(){};
	private final DragLinks drags;
	private final KitCore kit;
	private final FacetAppSurface app;
	private final FacetFactory popups;
	private boolean setting;
	private TabDragShower dragShow;
	AppTabs(KitCore kit,final FacetAppSurface app){
		super(SwingConstants.TOP);
		setFocusable(false);
		drags=new DragLinks(){
			@Override
			protected boolean canLink(JComponent c){
				return getComponentCount()>1&&getAt(c)==getSelectedIndex();
			}
			@Override
			protected void setShowLinkable(JComponent c,boolean on){
				dragShow=on||TabDragShower.debug?new TabDragShower(getAt(c)):null;
				repaint();
			}
			@Override
			protected void linkDefined(JComponent from,JComponent to){
				((SNumeric)windowTargeter(app).elements()[TARGET_WINDOW_MOVE].target()
						).setValue(getAt(to));
			}
			private int getAt(JComponent c){
				return(int)c.getClientProperty(atKey);
			}
		};
		this.kit=kit;
		this.app=app;
		addChangeListener(new ChangeListener(){
			@Override
			public void stateChanged(ChangeEvent e){
				int selectedAt=getSelectedIndex();
				if(setting||selectedAt<0)return;
				Tab tab=(Tab)getTabComponentAt(selectedAt);
				if(tab!=null)tab.update(true);
			}
		});
		popups=new FacetFactory(app.ff){
			@Override
			public SFacet toolbar(){
				STargeter window=windowTargeter(app),elements[]=window.elements();
				return menuRoot(window,window.title(),new SFacet[]{
					triggerMenuItems(elements[TARGET_WINDOW_CLOSE],HINT_NONE),
					numericNudgeMenu(elements[TARGET_WINDOW_MOVE],HINT_NONE),
				});
			};
		};
	}
	void setItems(KWrap[]items){
		setting=true;
    removeAll();
    for(int at=0;at<items.length;at++)new Tab(items,at);
	}
	public void setSelectedComponent(Component c){
		super.setSelectedComponent(c);
		int tabs=getTabCount(),set=getSelectedIndex();
		for(int at=0;at<tabs;at++)((Tab)getTabComponentAt(at)).update(set==at);
		SwingUtilities.invokeLater(new Runnable(){public void run(){
			setting=false;
		}});
	}
	private STargeter windowTargeter(final FacetAppSurface app){
		return app.surfaceTargeter().elements()[TARGETS_WINDOW];
	}
	private static Icon newTabIcon(KitCore kit,final String key){
		Icon got=(Icon)kit.getDecorationIcon(key,false);
		return got!=null?got:new Icon(){
			public int getIconHeight(){
				return 14;
			}
			public int getIconWidth(){
				return 14;
			}
			public void paintIcon(Component c,Graphics g,int x,int y){
				if(key.equals(""))return;
				Graphics2D g2=(Graphics2D)g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setFont(new Font("SansSerif",Font.BOLD,12));
				g2.scale(1.5,1);
				g2.setColor(key.equals(KEY_TAB_CLOSE_LO)?Color.LIGHT_GRAY:
					key.equals(KEY_TAB_CLOSE_LIVE)?Color.GRAY:Color.BLACK);
				g2.drawString("X",x,y+13);
			}
		};
	}
}