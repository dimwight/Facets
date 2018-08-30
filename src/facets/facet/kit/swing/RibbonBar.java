package facets.facet.kit.swing;
import static facets.facet.kit.swing.KitSwing.*;
import facets.facet.kit.Decoration;
import facets.facet.kit.KWrap;
import facets.facet.kit.KitFacet;
import facets.facet.kit.swing.KitSwing.LaF;
import facets.util.Debug;
import facets.util.Tracer;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.LookAndFeel;
import javax.swing.Painter;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.TabbedPaneUI;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
final class RibbonBar extends JTabbedPane{
	private static final Tracer t=Tracer.newTopped("RibbonBar",true);
	private final static class TabPanel extends JPanel{
		final Decoration decoration;
		TabPanel(Decoration decoration,KWrap[]wraps){
			super(new FlowLayout(FlowLayout.LEADING));
			this.decoration=decoration;
			for(KWrap wrap:wraps)add((Component)wrap.wrapped());
		}
	}
	private static Painter<JComponent>newNimbusTabPainter(final boolean empty){
		return new Painter<JComponent>(){
			@Override
			public void paint(Graphics2D g,JComponent object,int width,int height){
				if(empty)return;
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);
				Color base=new Color(214,217,223);
				g.setColor(base);
				g.fillRect(0,0,width,height);
				g.setColor(Color.black);
				g.drawRoundRect(0,0,width,height+5,10,10);
				g.setColor(base);
				g.fillRect(0,height,width+1,5);
			}
		};
	}
	private static UIDefaults nimbusUi;
	final JMenuBar menus=new JMenuBar(){
		@Override
		public JMenu add(JMenu menu){
			JPanel items=new JPanel(new FlowLayout(FlowLayout.LEADING));
			for(Component c:menu.getMenuComponents())
				if(c instanceof JMenuItem){
					JMenuItem item=(JMenuItem)c;
					String caption=item.getText();
					int mnemonic=item.getMnemonic();
					JButton button;
					if(c instanceof JMenu){
						String rubric=item.getToolTipText();
						button=MenuSwing.newPanelMenu(caption,mnemonic,rubric==null?"":rubric,
								((JMenu)c).getPopupMenu());
					}else{
						button=new JButton(caption);
						button.setMnemonic(mnemonic);
						button.addActionListener(item.getActionListeners()[0]);
					}
					items.add(button);
				}
			RibbonBar tabs=RibbonBar.this;
			tabs.add(menu.getText(),items);
			tabs.setSelectedComponent(items);
			tabs.setMnemonicAt(getSelectedIndex(),menu.getMnemonic());
			tabs.setSelectedIndex(0);
			adjustComponents(true,tabs);
			return menu;
		}
		
	};
	RibbonBar(){
		if(KitSwing.laf==LaF.Nimbus){
			if(nimbusUi==null){
				nimbusUi=new UIDefaults();
				UIDefaults laf=UIManager.getLookAndFeelDefaults();
				Painter<JComponent>selectedTab=newNimbusTabPainter(false),
					noTab=newNimbusTabPainter(true),
					areaPainter=new Painter<JComponent>(){
						@Override
						public void paint(Graphics2D g,JComponent object,int width,int height){
							g.setColor(Color.black);
							g.drawLine(0,height-=5,width,height);
						}
					};
				for(Object[]pair:new Object[][]{
						{":TabbedPaneTab[Selected].backgroundPainter,"+
							":TabbedPaneTab[MouseOver+Selected].backgroundPainter,"+
							":TabbedPaneTab[Pressed+Selected].backgroundPainter,"+
							":TabbedPaneTab[Focused+Selected].backgroundPainter,"+
							":TabbedPaneTab[Focused+MouseOver+Selected].backgroundPainter,"+
							":TabbedPaneTab[Focused+Pressed+Selected].backgroundPainter",
								false?null:selectedTab},
						{":TabbedPaneTab[Enabled].backgroundPainter",
								false?null:noTab},
						
						{":TabbedPaneTab[Pressed+Selected].textForeground",
								false?null:Color.black},						

						{":TabbedPaneTab.contentMargins",false?null
								:false?new Insets(2,8,3,8):new Insets(4,8,5,8)},
						{":TabbedPaneTabArea.contentMargins",true?null
								:true?new Insets(3,10,4,10):new Insets(3,10,4,10)},
						
						{".font",
								true?null:getFont()},

						{":TabbedPaneTab[Enabled+MouseOver].backgroundPainter,"+
							":TabbedPaneTab[Enabled+Pressed].backgroundPainter,"+
							":TabbedPaneTabArea[Enabled].backgroundPainter,"+
							":TabbedPaneTabArea[Enabled+MouseOver].backgroundPainter,"+
							":TabbedPaneTabArea[Enabled+Pressed].backgroundPainter",
								null},
									
				})for(String key:((String)pair[0]).split(",")){
						Object put=pair[1];
						if(put!=null)nimbusUi.put("TabbedPane"+key,put);
					}
				for(String mode:new String[]{"","+MouseOver","+Pressed"})
					nimbusUi.put("TabbedPane:TabbedPaneTabArea[Enabled"+mode+"].backgroundPainter",
							areaPainter);
				t.traceDebug(".RibbonBar: nimbusUi=",
						nimbusUi.get("TabbedPane:TabbedPaneTabArea.contentMargins"));
			}
			putClientProperty("Nimbus.Overrides.InheritDefaults",true);
			putClientProperty("Nimbus.Overrides",nimbusUi);
		}
		else if(laf!=LaF.Windows)throw new RuntimeException(
				"Not implemented for laf="+laf+" in "+Debug.info(this));
		setFocusable(false);
	}
	@Override
	public void setUI(TabbedPaneUI ui){
		super.setUI(laf!=LaF.Windows?ui:new BasicTabbedPaneUI(){
			@Override
			protected void installDefaults(){
				super.installDefaults();
				selectedTabPadInsets=tabInsets;
				lightHighlight=highlight=shadow;
	  	}
			@Override
			protected void setRolloverTab(int index){
				int tabThen=getRolloverTab();
				super.setRolloverTab(index);
				if(tabThen!=index)repaint();
			}
			@Override
			protected void paintContentBorderTopEdge(Graphics g,int tabPlacement,
					int selectedIndex,int x,int y,int w,int h){
				super.paintContentBorderTopEdge(g,tabPlacement,-1,x,y,w,h);
			}
			@Override
			protected void paintTabBackground(Graphics g,int tabPlacement,
					int tabIndex,int x,int y,int w,int h,boolean isSelected){
			  boolean isRollover=getRolloverTab()==tabIndex&&!isSelected;
			  if(isSelected){
			  	x+=3;w-=3;
			  }
				h+=(isSelected?10:0);
				g.setColor(isRollover?new Color(234,234,234):getBackground());
				g.fillRect(x+1,y+1,w-3,h-1);
			}
			@Override
			protected void paintTabBorder(Graphics g,int tabPlacement,int tabIndex,
					int x,int y,int w,int h,boolean isSelected){
			  boolean isRollover=getRolloverTab()==tabIndex&&!isSelected;
				if(isSelected||isRollover)
			  	super.paintTabBorder(g,tabPlacement,tabIndex,x+(isRollover?0:3),y,
			  			w-(isRollover?0:5),h,isSelected);
			}
			@Override
			protected void paintText(Graphics g,int tabPlacement,Font font,
					FontMetrics metrics,int tabIndex,String title,Rectangle textRect,
					boolean isSelected){
				if(tabIndex==0&&!isSelected)textRect.translate(1,0);
				super.paintText(g,tabPlacement,font,metrics,tabIndex,title,textRect,isSelected);
			}
			@Override
			protected int getTabLabelShiftX(int tabPlacement,int tabIndex,boolean isSelected){
				return 0;
			}
			@Override
			protected int getTabLabelShiftY(int tabPlacement,int tabIndex,boolean isSelected){
				return 0;
			}
		});
	}
	static Widget newTabPanel(KitSwing kit,KitFacet tab,List<KWrap>contents){
		return new Widget(tab,new TabPanel(kit.decoration(tab.title()),
				contents.toArray(new KWrap[0])));
	}
	void setTabPanels(JComponent[]panels){
		for(JComponent panel:panels){
			Decoration decoration=((TabPanel)panel).decoration;
			add(decoration.caption,panel);
			setSelectedComponent(panel);
			setMnemonicAt(getSelectedIndex(),decoration.mnemonic);
		}
		setSelectedIndex(0);
	}
}