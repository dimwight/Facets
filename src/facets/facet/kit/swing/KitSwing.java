package facets.facet.kit.swing;
import static facets.core.app.TextView.FontFamily.*;
import static facets.facet.AreaFacets.*;
import static facets.facet.FacetFactory.*;
import static facets.facet.app.FacetConstants.*;
import static facets.facet.kit.swing.KitSwing.LaF.*;
import static facets.facet.kit.swing.ViewerBase.*;
import static facets.util.Debug.*;
import static java.awt.BorderLayout.*;
import static java.awt.Color.*;
import static java.lang.Math.*;
import static javax.swing.BorderFactory.*;
import static javax.swing.UIManager.*;
import facets.core.app.AppSpecifier;
import facets.core.app.AppWindowHost;
import facets.core.app.Dialogs;
import facets.core.app.FeatureHost;
import facets.core.app.HideableHost;
import facets.core.app.HtmlView;
import facets.core.app.HtmlView.InputView;
import facets.core.app.SContentAreaTargeter.ContentArea;
import facets.core.app.SContenter;
import facets.core.app.ListView;
import facets.core.app.PagedContenter;
import facets.core.app.SAreaTarget;
import facets.core.app.SView;
import facets.core.app.StatefulViewable;
import facets.core.app.StatefulViewable.Clipper;
import facets.core.app.StatefulViewable.ClipperSource;
import facets.core.app.TableView;
import facets.core.app.TextTreeView;
import facets.core.app.TextView;
import facets.core.app.TextView.FontFamily;
import facets.core.app.TreeView;
import facets.core.app.ViewerTarget;
import facets.core.app.avatar.AvatarView;
import facets.core.app.avatar.ZoomPanView;
import facets.core.superficial.Notifying.Impact;
import facets.core.superficial.SFacet;
import facets.core.superficial.SNumeric;
import facets.core.superficial.SNumeric.Coupler;
import facets.core.superficial.STarget;
import facets.core.superficial.app.SSurface.WindowAppSurface;
import facets.facet.AreaFacets;
import facets.facet.AreaFacets.PaneLinking;
import facets.facet.FacetFactory;
import facets.facet.FacetMaster.Viewer;
import facets.facet.ViewerAreaMaster;
import facets.facet.app.FacetAppSurface;
import facets.facet.kit.Decoration;
import facets.facet.kit.KButton;
import facets.facet.kit.KButton.Type;
import facets.facet.kit.KField;
import facets.facet.kit.KList;
import facets.facet.kit.KMount;
import facets.facet.kit.KTargetable;
import facets.facet.kit.KViewer;
import facets.facet.kit.KWrap;
import facets.facet.kit.KWrap.ItemSource;
import facets.facet.kit.KitCore;
import facets.facet.kit.KitFacet;
import facets.facet.kit.avatar.SwingPainterSource;
import facets.facet.kit.swing.PaneTabs.Pane;
import facets.facet.kit.swing.Spacer.Filler;
import facets.facet.kit.swing.tree.PathListPaneMaster;
import facets.facet.kit.swing.tree.PathTreeDnDMaster;
import facets.facet.kit.swing.tree.PathTreePaneMaster;
import facets.util.Debug;
import facets.util.IndexingIterator;
import facets.util.ItemList;
import facets.util.NumberPolicy;
import facets.util.StringFlags;
import facets.util.Strings;
import facets.util.Times;
import facets.util.Tracer;
import facets.util.Util;
import facets.util.app.AppValues;
import facets.util.app.AppWatcher;
import facets.util.app.BusyCursor;
import facets.util.tree.ValueNode;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIDefaults;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
final public class KitSwing extends KitCore{
	public static boolean debug;
	final static String sizeKey="JComponent.sizeVariant";
	enum LaF{Windows,Metal,Nimbus};
	static LaF laf=Windows;
	enum NimbusSize{mini,small,regular,large;
		int toolTipFontSize(){
			return this==mini?9:this==small?11:this==large?14:12;
		}
		NimbusSize smaller(){
			NimbusSize[]values=values();
			for(int i=1;i<values.length;i++)
				if(values[i]==this)return values[i-1];
			return this;
		}
		float layoutFactor(){
			return this==large?1.3f:this==regular?1.18f:1f;
		}
	}
	private final static NimbusSize nimbusSize=false?NimbusSize.large:NimbusSize.regular;
	private static final boolean windowsXpStdInLarge=true;
	private static final float windowsStdKitFontFloat=11f;
	private static float kitFontFloat;
	static Dimension TRIM;
	public KitSwing(boolean watchable,boolean appletStyle,boolean systemLookAndFeel){
		watcher=!watchable?null:AppWatcherSwing.getSingle();
		String nimbusName="com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel",
			lafName=appletStyle?null:systemLookAndFeel?getSystemLookAndFeelClassName()
					:nimbusName;
		if(lafName!=null&&!lafName.equals(getLookAndFeel().getClass().getName()))try{
			setLookAndFeel(lafName);
		}catch(Exception notSet){
			trace(":"+notSet);
		}
		if(false)trace(": ",getLookAndFeel().getClass().getSimpleName());
		boolean useNimbus=lafName!=null&&lafName.equals(nimbusName);
		if(inWindows&&!useNimbus)laf=Windows;
		else if(useNimbus){
			laf=Nimbus;
			UIDefaults ui=getLookAndFeelDefaults();
			ui.put("ToolTip[Disabled].backgroundPainter",
					ui.get("ToolTip[Enabled].backgroundPainter"));
			if(false)ui.put("ToolTip.font",new Font("SanSerif",0,nimbusSize.toolTipFontSize()));
			ui.put("CheckBox.contentMargins",new Insets(3,5,3,5));
			ui.put("RadioButton.contentMargins",new Insets(3,5,3,5));
			if(false)ui.put("Panel.contentMargins",new Insets(0,0,0,0));
			if(false)ui.put("TabbedPane.TabbedPaneTabArea.textForeground",true?Color.red:new Color(61,96,121));
			if(false)ui.put("Table.alternateRowColor",new Color(233,243,254));
		}
		float fontFloat=new JMenuBar().getFont().getSize2D();
		kitFontFloat=windowsXpStdInLarge?windowsStdKitFontFloat:fontFloat;
		TRIM=new Dimension(px(10),(int)(35*fontFloat/windowsStdKitFontFloat));//36+4
		if(false)trace(".KitSwing: layoutFactor="+Util.sf(layoutFactor()));
		FacetFactory.fillFontSizes((int)(kitFontFloat+(inWindows?1:0)));
		if(true)return;
		JFrame.setDefaultLookAndFeelDecorated(true);
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener(
				new PropertyChangeListener(){
					@Override
					public void propertyChange(PropertyChangeEvent e){
						trace(arrayInfo(new Object[]{e.getPropertyName(),e.getNewValue(),
								e.getOldValue()}
							).replace("\n",":").replaceAll("[^{]+\\{([^}]+).*","$1"));
					}
				});
	}
	public static void adjustComponents(boolean updateUi,Component...cs){
		if(laf==Metal)return;
		for(Component c:cs){
			if(laf==Windows&&!inWindows7){
				Font font=c.getFont();
				if(font!=null)c.setFont(font.deriveFont(kitFontFloat));
			}
			else if(c instanceof JComponent)
				((JComponent)c).putClientProperty(sizeKey,(
						false&&c instanceof JTree?NimbusSize.small:nimbusSize).name());
			for(Component d:((Container)c).getComponents())adjustComponents(updateUi,d);
			if(updateUi)updateNimbusUI(c);
		}
	}
	public static interface Dimensioned{
		String PROPERTY=Dimensioned.class.getSimpleName();
	}
	static class TabDragShower extends Tracer{
		static final boolean debug=false;
		private final int at;
		TabDragShower(int at){
			this.at=at;
		}
		void paint(Graphics g,JTabbedPane tabs){
			Graphics2D g2=(Graphics2D)g.create();
			Rectangle r=tabs.getBoundsAt(at);
			int line=focusWidth(),shift=line-1,
					alpha=line==2?0x08fffffff:0x0ffffffff,
				x=r.x+shift,y=r.y+shift,w=r.width-2-shift,h=r.height-2-shift;
			g2.translate(x,y);
			if(tabs.getTabPlacement()==JTabbedPane.BOTTOM){
				g2.translate(0,r.height-1-2*shift);
				g2.scale(1,-1);
			}
			g2.setStroke(new BasicStroke(shift));
			g2.setColor(false||debug?red:new Color(FOCUS_COLOR.getRGB()&alpha,true));
			g2.drawPolyline(new int[]{0,0,w,w},new int[]{h,0,0,h},4);
		}
	}
	public KMount paneLinksGroup(final PaneLinking panes){
		final DragLinks links=new DragLinks(){
			@Override
			protected void setShowLinkable(JComponent c,boolean on){
				((Pane)c.getClientProperty(Pane.class)).setShowLinkable(c,on);
			}
			@Override
			protected boolean canLink(JComponent c){
				return panes.canLink(tabWrap(c));
			}
			@Override
			protected void linkDefined(JComponent from,JComponent to){
				panes.linkDefined(tabWrap(from),tabWrap(to));
			}
			KWrap tabWrap(JComponent tab){
				return(KWrap)tab.getClientProperty(KWrap.class);
			}
		};
		return new KMount(){
			@Override
			public void setItems(KWrap...items){
				new IndexingIterator<KWrap>(items){
					@Override
					protected void itemIterated(KWrap item,int at){
						JComponent content=(JComponent)item.wrapped(),
								tabbed=(JComponent)content.getClientProperty(PaneTabs.class);
						if(tabbed==null)throw new IllegalArgumentException(
								"Null tabbed for "+item);
						tabbed.putClientProperty(KWrap.class,item);
						links.addComponent(tabbed);
					}
				}.iterate();
			}
			@Override
			public Object wrapped(){
				throw new RuntimeException("Not implemented in "+this);
			}
			@Override
			public Object newWrapped(Object parent){
				throw new RuntimeException("Not implemented in "+this);
			}
			@Override
			public KitFacet facet(){
				throw new RuntimeException("Not implemented in "+this);
			}
			@Override
			public void setItem(KWrap item){
				throw new RuntimeException("Not implemented in "+this);
			}
			@Override
			public void setHidden(boolean hidden){
				throw new RuntimeException("Not implemented in "+this);
			}
			@Override
			public void setActiveItem(KWrap item){
				throw new RuntimeException("Not implemented in "+this);
			}
		};
	}
	public KMount paneTabs(SFacet[]areas,SFacet active,KWrap control){
		return new PaneTabs(this,areas,active,control);
	}
	public KMount spreadMount(KitFacet facet,boolean inset){
		return new MountCore(facet,MountCore.newSpreadPanel(inset)){};
	}
	public KMount areaTabs(SFacet[]areas,StringFlags hints){
		return new AreaTabs(this,areas,hints);
	}
	@Override
	public KWrap ribbonTab(KitFacet tab,KitFacet[]panels){
		ItemList<KWrap>wraps=new ItemList(KWrap.class);
		for(KitFacet panel:panels)
			if(panel==null||(wraps.size()==0&&panel==FacetFactory.BREAK))continue;
			else wraps.addItem(panel==FacetFactory.BREAK?KWrap.BREAK:panel.base());
		return RibbonBar.newTabPanel(this,tab,wraps);
	}
	private static ViewerMaster newTextMaster(final TextView view){
		return new ViewerMaster(){
			private final boolean inputting=view.isLive();
			private final JTextArea pane=new JTextArea();
			private TextSmarts ts=inputting?new TextSmarts(pane):null;
			public void refreshAvatars(Impact impact){
				FontFamily font=view.fontFamily();
				pane.setFont(new Font(font.name(),Font.PLAIN,0).deriveFont(kitFontFloat*
						(laf!=Windows||kitFontFloat==windowsStdKitFontFloat?1:
								font==Serif?1.05f:font==Monospaced?1:0.95f)));
				ViewerTarget viewerNow=viewerTarget();
				if(pane.isFocusOwner()&&viewerNow==viewerThen)return;
				viewerThen=viewerNow;
				pane.getDocument().removeDocumentListener(inputs);
				newTextPaneUpdate(pane,viewerNow).updatePane();
				if(ts!=null)ts.resetUndo();
				pane.getDocument().addDocumentListener(inputs);
			}
			protected JComponent newAvatarPane(){
				pane.setEditable(inputting);
				pane.addFocusListener(base.focusListener);
				return pane;
			}
			private final DocumentListener inputs=new DocumentListener(){
			  final PauseWaiter waiter=new PauseWaiter();
				@Override
				public void removeUpdate(DocumentEvent e){
					textChanged(e.getDocument());
				}
				@Override
				public void insertUpdate(DocumentEvent e){
					textChanged(e.getDocument());
				}
				private void textChanged(final Document doc){
					waiter.startWait(500,new ActionListener(){
						@Override
						public void actionPerformed(ActionEvent arg0){
							try{
								String text=doc.getText(0,doc.getLength());
								viewerTarget().selectionEdited(null,text,true);
							}catch(BadLocationException e){
								throw new RuntimeException(e);
							}
						}
					});
				}
				@Override
				public void changedUpdate(DocumentEvent e){
					throw new RuntimeException("Not implemented in "+Debug.info(this));
				}
			};
			private ViewerTarget viewerThen;
			@Override
			protected void disposeAvatarPane(){
				JTextArea pane=(JTextArea)avatarPane();
				pane.setUI(null);
				pane.setDocument(new PlainDocument());
			}
		};
	}
	private static TextPaneUpdate newTextPaneUpdate(final JTextArea pane,
			final ViewerTarget viewerTarget){
		return new TextPaneUpdate(viewerTarget.selection()){
			protected void updatePaneText(String now){
				boolean wrap=((TextView)viewerTarget.view()).wrapLines();
				pane.setWrapStyleWord(wrap);
				pane.setLineWrap(wrap);
				String then=pane.getText();
				if(!then.equals(now)){
					pane.setText(now);
					pane.setCaretPosition(0);
				}
			}
		};
	}
	public double layoutFactor(){
		return layoutFactorSwing();
	}
	static double layoutFactorSwing(){
		return laf==Windows?kitFontFloat/windowsStdKitFontFloat:nimbusSize.layoutFactor();
	}
	public static int px(int raw){
		return (int)rint(raw*layoutFactorSwing());
	}
	static JLabel newPrettyLabel(final double scaleBy,final int style){
		return new JLabel(){
			Font font=getFont();
			@Override
			public void paint(Graphics g){
				if(font!=null)
					setFont(font.deriveFont(style
							).deriveFont((float)(font.getSize2D()*scaleBy*layoutFactorSwing())));
				font=null;
				super.paint(g);
			}
		};
	}
	static void setNimbusSmaller(JComponent swing){
		for(Component c:allComponents(swing))if(c instanceof JComponent)
			((JComponent)c).putClientProperty(sizeKey,nimbusSize.smaller().name());
		updateNimbusUI(swing);
	}
	static void updateNimbusUI(Component swing){
		SwingUtilities.updateComponentTreeUI(swing);
	}
	public static void flashButton(KButton b){
		((Button)b).flash();
	}
	public static final class PauseWaiter{
		private Timer waiter;
		public void startWait(int waitMillis,ActionListener action){
			if(waiter!=null)waiter.stop();
			(waiter=new Timer(waitMillis,action){
				public boolean isRepeats(){return false;}
			}).start();
		}
		public void abandonWait(){
			if(waiter!=null)waiter.stop();
		}
	}
	public KMount hideMount(KitFacet facet){
		return new MountCore(facet,MountCore.newSpreadPanel(false)){
			private KWrap item;
			@Override
			public void setItem(KWrap item){
				swing.removeAll();
				if((this.item=item)==null)return;
				setHidden(false);
			}
			@Override
			public void setHidden(boolean hidden){
				swing.removeAll();
				JComponent swingItem=(JComponent)item.wrapped();
				if(!hidden)swing.add(swingItem,BorderLayout.CENTER);
				if(debug)trace(".setHidden: hidden="+hidden+" swingItem="+Debug.info(swingItem)+
						" swing=",swing.getComponentCount());
				swing.validate();
				swingItem.setVisible(!hidden);
			}
		};
	}
	public KMount tabMount(final KWrap[]items,final String[]titles){
		return new MountCore(NO_FACET,new JTabbedPane(SwingConstants.TOP){{
				adjustComponents(false,this);
				new IndexingIterator<KWrap>(items){
					protected void itemIterated(KWrap item,int at){
						Decoration decorations=decoration(titles[at],StringFlags.EMPTY);
						KitCore.widgets++;
						addTab(decorations.caption,(Icon)decorations.icon,
								(Component)item.wrapped());
					}
				}.iterate();
			}
		}){};
	}
	public KMount switchMount(KitFacet facet,KWrap[]items,final StringFlags hints){
		final class CardPane extends JPanel implements Dimensioned{
			CardPane(CardLayout layout){
				super(layout);
			}
		}		
		final CardLayout layout=new CardLayout();
		final JPanel swing=new JPanel(new BorderLayout()),pane=new CardPane(layout);
		swing.add(pane,CENTER);
		final JLabel caption=newPrettyLabel(1.3,Font.BOLD),
			rubric=false?newPrettyLabel(1,Font.PLAIN):new JLabel();
		MountCore mount=new MountCore(facet,swing){
			public void setActiveItem(KWrap item){
				layout.show(pane,""+Debug.id(item));
				Decoration decoration=decoration(item.facet().title(),hints);
				caption.setText(decoration.caption);
				rubric.setText(decoration.rubric);
				STarget target=item.facet().target();
				if(target instanceof ContentArea){
					SContenter contenter=((ContentArea)target).contenter;
					if(contenter instanceof PagedContenter){
						Dimension size=((PagedContenter)contenter).contentAreaSize();
						pane.firePropertyChange(Dimensioned.PROPERTY,size.width,size.height<<8);
					}
				}
			}
		};
		if(items==null)return mount;
		if(items.length>1&&!hints.includeFlag(HINT_BARE)){
			JPanel headerBase=new JPanel(new BorderLayout(0,px(2)));
			headerBase.setBorder(createCompoundBorder(
					createEtchedBorder(EtchedBorder.LOWERED),
					createEmptyBorder(5,10,10,10)
				));
			headerBase.setBackground(Color.white);
			headerBase.add(caption,CENTER);
			headerBase.add(rubric,SOUTH);
			adjustComponents(false,headerBase);
			Font font=caption.getFont();
			swing.add(headerBase,BorderLayout.NORTH);
		}
		for(KWrap item:items)
			pane.add((Component)item.wrapped(),""+Debug.id(item));
		mount.setActiveItem(items[0]);
		return mount;
	}
	public KMount appMultiMount(KitFacet facet,FacetAppSurface app){
		return new AppMultiMount(facet,app,this);
	}
	public KMount rowMount(KitFacet facet,int hgap,int vgap,StringFlags hints){
		return new RowMount(facet,this,hgap,vgap,hints);
	}
	public KMount packedMount(KitFacet facet,StringFlags hints){
		return new PackedMount(facet,hints);
	}
	public KMount splitMount(KitFacet facet,boolean wide,SNumeric ratio){
		if(ratio==null)ratio=new SNumeric("splitMountDefault",50,new Coupler(){
			public NumberPolicy policy(SNumeric n){
				return SASH_SPLIT_POLICY;
			}
			public void valueSet(SNumeric n){}
		});
		return new SplitMount(facet,wide,ratio);
	}
	public KWrap wrapMount(KitFacet facet,KWrap[]wraps,int hgap,int vgap,
			StringFlags hints){
		return new WrapMount(facet,decoration(facet.title(),hints),wraps,hgap,vgap);
	}
	BusyCursor newBusyCursor(Container swing,JComponent menuBar){
		if(watcher!=null&&menuBar!=null)((AppWatcherSwing)watcher).blockParent=swing;
		return new BusyCursorSwing(menuBar,swing);
	}
	public KWrap menu(KitFacet facet,String title,ItemSource itemSource,StringFlags hints){
		return new MenuSwing(facet,title,itemSource,hints,this);
	}
	protected void traceOutput(String msg){
		if(true)super.traceOutput(msg);
		else if(false)Times.printElapsed("KitSwing: "+msg);
	}
	private KButton dropdownButton_(KitFacet facet,ActionListener listener){
		return new DropdownButton_(facet,listener,this);
	}
	public static void debugSwing(Container swing,KitFacet facet,Object wrap){
		if(!debug)return;
		STarget target=facet==null?null:facet.target();
		String msg=info(target)+">"+info(wrap)+">"+info(swing)+
			(false?"":(" "+info(swing.getLayout())));
		JComponent jc=(JComponent)swing;
		jc.setToolTipText(msg);
		jc.setBorder(true?createLineBorder(Color.blue):
			createTitledBorder(createLineBorder(Color.blue),
					"<html><font size=-3>"+Debug.id(swing)+"</font>",
					TitledBorder.LEFT,TitledBorder.BOTTOM));
		if(true)return;
		Util.printOut("KitSwing.debugSwing: "+msg);
		Debug.printStackTrace(1);
	}
	public FeatureHost newSwingAppletHost(JApplet applet){
		JMenuBar menuBar=new JMenuBar();
		applet.setJMenuBar(menuBar);
		FeatureWrap wrap=new FeatureWrap(this,menuBar,null);
		Container wrapped=(Container)wrap.wrapped(),
			content=applet.getContentPane();
		if(false){
			wrapped.setBackground(Color.red);
			content.setBackground(Color.blue);
			traceDebug(".newSwingAppletHost: wrapped=",wrapped.getLayout());
		}
		content.add(wrapped);		
		return wrap;
	}
	@Override
	public FeatureHost newAppletHost(int width,int height,String name){
		final JFrame frame=new JFrame(name);
		JMenuBar menuBar=new JMenuBar();
		frame.setJMenuBar(menuBar);
		FeatureWrap wrap=new FeatureWrap(this,menuBar,null);
		Container wrapped=(Container)wrap.wrapped(),
			extra=false?new JPanel(new BorderLayout()):null;
		if(extra!=null)extra.add(wrapped);
		Container content=frame.getContentPane();
		content.add(extra!=null?extra:wrapped);
		wrapped.setBackground(Color.white);
		if(false){
			traceDebug(".newAppletHost: wrapped=",wrapped.getLayout());
			content.setBackground(Color.blue);
		}
		frame.setSize(width+inAcross,height+inTop);
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter(){
		  public void windowClosing(WindowEvent e){
		  	if(false)trace(".windowClosing: size=",
		  			(frame.getWidth()-inAcross)+","+(frame.getHeight()-inTop));
		  	System.exit(0);
			}
		});
		frame.setVisible(true);
		return wrap;
	}
	public HideableHost newOrphanDialogHost(AppValues values){
		return new DialogHostSwing(null,this);
	}
	final public AppWatcher watcher;
	public void adjustMenuMnemonics(KWrap[]wraps){
		class ItemProxy{
			Decoration d;
			ItemProxy(KWrap wrap){
				d=((Widget)wrap).decoration;
				if(false)trace("ItemProxy: "+this);
			}
			public String toString(){
				return d.toString();
			}
		}
		List<ItemProxy>items=new ArrayList();
		for(KWrap wrap:wraps)if(wrap instanceof Widget)items.add(new ItemProxy(wrap));
	}
	protected KButton newButton(KitFacet facet,Type type,int usage){
		return new Button(facet,type,usage);
	}
	public KField colorShader(KitFacet facet,StringFlags hints){
		KButton button=button(facet,KButton.Type.Fire,KButton.USAGE_PANEL,
				"Set Shade",hints);
		return new FieldShader(facet,button);
	}
	public KList dropdownList(KitFacet dropdown,boolean asCombo,StringFlags hints){
		return new ListDropdown(this,dropdown,asCombo,hints);
	}
	public KWrap filler(KitFacet facet){
		return new Filler(facet);
	}
	public KViewer masteredViewer(KitFacet facet,ViewerAreaMaster vam,final SView view,
			ValueNode stateNode){
		StringFlags hints=vam.hints();
		Viewer master=vam.viewerMaster();
		if(master==null)master=
			view instanceof InputView?new HtmlInputPaneMaster()
			:view instanceof HtmlView?
					view instanceof HtmlView.SmartView?new HtmlSmartPaneMaster(this,hints)
				:new HtmlPaneMaster()
			:view instanceof TextView?newTextMaster((TextView)view)
			:view instanceof TextTreeView?new HtmlTreePaneMaster()
			:view instanceof ListView?new PathListPaneMaster()
			:view instanceof TreeView?
					false?new PathTreePaneMaster(hints.includeFlag(HINT_EXTRAS_PANE))
							:new PathTreeDnDMaster()
		  :view instanceof TableView?new TablePaneMaster(stateNode,vam,this)
		  :view instanceof AvatarView?SwingPainterSource.planeMaster(facet,
		  		view instanceof ZoomPanView,hints)
			:null;
		if(master==null)throw new IllegalStateException
			("Invalid view " + Debug.info(view)+" in "+Debug.info(this));
		return new ViewerBase(facet,(ViewerMaster)master,this,hints);
	}
	public KWrap label(KitFacet facet,String title,StringFlags hints){
		return new Label(facet,decoration(title,hints));
	}
	public KWrap nudgersPanel(KitFacet facet,KWrap[]buttons,KWrap[]boxes,
			KWrap[]labels,StringFlags hints){
		final boolean boxed=boxes!=null,bare=labels==null,
			tall=hints.includeFlag(HINT_TALL),
			square=hints.includeFlag(HINT_SQUARE),
			nudgersFirst=hints.includeFlag(HINT_NUMERIC_NUDGERS_FIRST),
			debug=hints.includeFlag(HINT_DEBUG);
		if(debug)trace(".nudgersPanel: ",
				"boxed="+boxed+",bare="+bare+",tall="+tall+",square="+square
		);
		JPanel swing=new JPanel(new GridBagLayout());
		for(int n=0;n<buttons.length/2;n++){
			JPanel nudgers=new JPanel(new GridLayout(tall?2:1,tall?1:2,0,0));
			KitCore.widgets++;
			if(panelShade!=null)nudgers.setBackground(new Color(panelShade.rgb()));
			nudgers.add((Component)buttons[n*2].wrapped());
			nudgers.add((Component)buttons[n*2+1].wrapped());
			final int nAt=n,wideX=(2+(bare?0:2)+(boxed?1:0))*n;
			swing.add(nudgers,new GridBagConstraints(){{
				gridx=tall||square?
						!boxed&&bare?0:(boxed&&bare)||tall?1:2
					:wideX+(bare?0:2)+(boxed&&!nudgersFirst?1:0);
				gridy=tall||square?nAt:0;
				fill=BOTH;
			}});
			Component swingLabel=labels==null?null:(Component)labels[n].wrapped();
			if(boxed){
				KWrap box=boxes[n];
				if(tall){
					RowMount boxBase=new RowMount(NO_FACET,this,0,5,
							new StringFlags(HINT_PANEL_CENTER));
					boxBase.setItems(bare?new KWrap[]{box}:new KWrap[]{labels[n],
							KWrap.BREAK,box});
					swing.add((Component)boxBase.wrapped(),new GridBagConstraints(){{
							gridx=0;
							gridy=nAt;
							ipadx=15;
						}});
				}else{
					Container swingBox=(Container)box.wrapped();
					((JTextField)allComponents(swingBox)[1]).setHorizontalAlignment(
							SwingConstants.CENTER);
					if(square){
						if(!bare)swing.add(swingLabel,
								new GridBagConstraints(){{
								gridx=0;
								gridy=nAt;
								ipadx=2;
								anchor=EAST;
							}});
						swing.add(swingBox,new GridBagConstraints(){{
								gridx=bare?0:1;
								gridy=0;
								ipadx=6;
							}});
					}
					else {
						if(!bare)
							swing.add(swingLabel,
									new GridBagConstraints(){{
									gridx=wideX;
									gridy=0;
									ipadx=4;
									anchor=EAST;
								}});
						swing.add(swingBox,new GridBagConstraints(){{
								gridx=wideX+(bare?0:1)+(nudgersFirst?2:0);
								gridy=0;
								ipadx=0;
							}});
					}
				}
			}
			else if(!bare)
				swing.add(swingLabel,new GridBagConstraints(){{
						gridx=0;
						gridy=nAt;
						anchor=EAST;
						ipadx=5;
					}});
		}
		return new Widget(facet,swing);
	}
	public KField numberField(KitFacet facet,NumberPolicy policy,
			StringFlags hints){
		return new FieldNumber(facet,policy,this,hints);
	}
	public KTargetable sliderPanel(KitFacet facet,int width,KWrap label,
			KWrap box,StringFlags hints){
		return new SliderPanel(facet,this,width,label,box,hints);
	}
	public KWrap spacer(KitFacet facet,int width,int height){
		return new Spacer(facet,px(width),px(height));
	}
	public KField textField(KitFacet facet,int cols,StringFlags hints){
		return new FieldText(facet,cols,this,hints);
	}
	public KField textLabel(KitFacet facet,StringFlags hints){
		return new Label(facet,hints);
	}
	public KWrap viewerTabs(final SFacet[]viewerAreas){
		final JTabbedPane tabBase=new JTabbedPane(SwingConstants.BOTTOM);
		for(int i=0;i<viewerAreas.length;i++){
			KitFacet area=(KitFacet)((ViewerTarget)((SAreaTarget)(
					(KitFacet)viewerAreas[i]).target()).activeFaceted()).attachedFacet();
			ViewerBase swing=(ViewerBase)area.base();
			Boolean wrapped=null;
			if(wrapped==null)throw new RuntimeException("Not implemented in "+Debug.info(this));
			Container base=(Container)swing.wrapped(),
				addTab=!wrapped?base:(Container)base.getComponent(0);
			if(wrapped) base.removeAll();
			String title="Title me!";
			final boolean isShared=false;
			Object icon=getDecorationIcon(isShared?AreaFacets.PAGES_SHARED
					:AreaFacets.PAGES_SINGLE, false);
			if(icon==null)tabBase.addTab(title,addTab);
			else tabBase.addTab(title,(Icon)icon,addTab);
		}
		tabBase.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e){
				((KitFacet)viewerAreas[tabBase.getSelectedIndex()]).targetNotify(tabBase,false);
			}
		});
		return new Widget(NO_FACET,tabBase);
	}
	public KList listPane(KitFacet facet,int width,int rows){
		return new ListPane(facet,this,width,rows,false);
	}
	public KList listPaneMultiple(KitFacet facet,int width,int rows){
		return new ListPane(facet,this,width,rows,true);
	}
	public KList listPaneChecked(KitFacet facet,int width,int rows){
		return new ListPaneTable(facet,this,width,rows);
	}
	protected Object getDecorationKeyStroke(String key){
		Integer code=keyCodes.get(key);
		if(code==null)return null;
		int keyValue=code&0xFFFF,
			mask=(code&KEY_CTRL)>>16|(code&KEY_SHIFT)>>16;
		if(false)trace("SwingKit: key=",key+",mask="+mask);
		return KeyStroke.getKeyStroke(keyValue,mask);
	}
	protected Object newFileIcon(String fileSpec){
		return new ImageIcon(fileSpec);
	}
	protected Object newDecorationIcon(URL url){
		return new ImageIcon(url);
	}
	public ClipperSource statefulClipperSource(final boolean useSystemClipboard){
		return new ClipperSource(){
			public Clipper newClipper(StatefulViewable viewable){
				return new ClipperSwing(viewable,useSystemClipboard);
			}
		};
	}
	public void warningCritical(Dialogs.ExceptionTexts tt,Exception e,boolean inOpen){
		DialogsSwing.warningExceptionSwing(tt,e,null,inOpen);
	}
	public AppWindowHost newWindowHost(WindowAppSurface app,AppSpecifier spec){
		return new WindowHostSwing(app,spec,this);
	}
	public FeatureHost newViewerHost(){
		return new FeatureWrap(this,null,null);
	}
	static Component[]allComponents(Container root){
		final class AllComponents extends ItemList<Component>{
			AllComponents(){
				super(Component.class);
			}
			void addAllToList(Container c){
				if(c==null)return;
				addItem(c);
				Component[]components=c.getComponents();
				if(components==null)return;
				for(Component d:components)
					if(d instanceof Container)addAllToList((Container)d);
					else addItem(d);
			}
		}
		AllComponents all=new AllComponents();
		all.addAllToList(root);
		return all.items();
	}
	public static String componentTree(Container root){
		final class AllComponents extends ArrayList<String>{
			void addAllToList(final Container c,int tabs){
				Component[]components=c.getComponents();
				String indent="";
				for(int i=0;i<tabs;i++)indent+="\t";
				add(indent+getInfo(c));
				if(components==null||components.length==0)return;
				for(Component d:components)
					if(d instanceof Container)addAllToList((Container)d,tabs+1);
					else add(indent+getInfo(d));
			}
			private String getInfo(Component c){
				String caption=c instanceof JComponent?((JComponent)c).getToolTipText():null;
				if(caption==null){
					if(c instanceof AbstractButton)caption=(((AbstractButton)c).getText());
					else if(c instanceof JLabel)caption=(((JLabel)c).getText());
				}
				return Debug.info(c)+(false?c.getFont():caption==null?"":(" "+caption));
			}
		}
		AllComponents all=new AllComponents();
		all.addAllToList(root,0);
		return false?(""+all.size()):Strings.linesString(all.toArray(new String[]{}));
	}
	static void addToolGroups(JComponent panel,Component...groups){
		final Border groupBorder=createEmptyBorder(0,0,0,5);
		panel.setLayout(new FlowLayout(FlowLayout.LEFT,2,0));
		if(groups.length==1){
			if(false)panel.setBorder(groupBorder);
			panel.add(groups[0]);
			panel.validate();
			return;
		}
		panel.setBorder(createEmptyBorder(2,2,2,2));
		for(int i=0;i<groups.length;i++) {
			((JComponent)groups[i]).setBorder(groupBorder);
			panel.add(groups[i]);
			panel.validate();
		}
	}
	public void dragLinkGroup(KWrap[]wraps){
		trace(".dragLinkGroup: wraps=",wraps);
	}
}
