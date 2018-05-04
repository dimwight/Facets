package facets.facet.kit.swing;
import static facets.facet.FacetFactory.*;
import static facets.facet.kit.swing.KitSwing.*;
import static facets.util.Debug.*;
import static facets.util.Times.*;
import static javax.swing.BorderFactory.*;
import static javax.swing.SwingUtilities.*;
import facets.core.app.AppSurface;
import facets.core.app.MenuFacets;
import facets.core.app.SAreaTarget;
import facets.core.app.SurfaceServices;
import facets.core.app.ViewerTarget;
import facets.core.app.SContentAreaTargeter.ContentArea;
import facets.core.superficial.Notifying.Impact;
import facets.core.superficial.SFacet;
import facets.core.superficial.STarget;
import facets.core.superficial.STrigger;
import facets.core.superficial.TargetCore;
import facets.facet.FacetFactory;
import facets.facet.app.FacetAppSurface;
import facets.facet.kit.KViewer;
import facets.facet.kit.KWrap;
import facets.facet.kit.KitCore;
import facets.facet.kit.KitFacet;
import facets.facet.kit.swing.KitSwing.LaF;
import facets.util.Debug;
import facets.util.ItemList;
import facets.util.StringFlags;
import facets.util.Tracer;
import facets.util.Util;
import facets.util.app.BusyCursor.BusySettable;
import facets.util.app.ProvidingCache;
import facets.util.app.WatchableOperation;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import javax.swing.plaf.PanelUI;
import javax.swing.plaf.synth.SynthContext;
import javax.swing.plaf.synth.SynthPanelUI;
public final class ViewerBase extends Tracer implements KViewer{
	static final Color FOCUS_COLOR=new Color(115,164,209);
	final static int focusWidth(){
		return laf==LaF.Nimbus?2:1;
	}
	private final class FocusMount extends JPanel implements BusySettable{
		private final JPanel border=new JPanel(new BorderLayout()){
			@Override
			public Insets getInsets(){
				return laf==LaF.Nimbus?new Insets(0,0,0,0):super.getInsets();
			}
			@Override
			public Insets getInsets(Insets insets){
				throw new RuntimeException("Not implemented in "+this);
			}
		},
		labelPanel=new JPanel(new BorderLayout());
		private final Color inactiveColor,activeColor;
		private final JLabel label;
		private final MouseAdapter mousePressed=new MouseAdapter(){
			public void mousePressed(MouseEvent e){
				focusListener.focusGained(new FocusEvent(label,label.hashCode()));
			}
		};
		private JComponent setControl;
		FocusMount(){
			super(new BorderLayout());
			inactiveColor=getBackground();
			activeColor=new Color[]{
					FOCUS_COLOR,
					new Color(177,197,218),
					inactiveColor.brighter(),inactiveColor.darker(),
				}[0];
			border.add(scroll==null?avatarPane:scroll);
			add(border);
			label=hints.includeFlag(HINT_BARE)?null:new JLabel();
			if(label==null)return;
			labelPanel.setBorder(createEmptyBorder(0,3,laf==LaF.Windows?0:2,2));		  
			labelPanel.add(label,BorderLayout.WEST);
			add(labelPanel,BorderLayout.SOUTH);
		  labelPanel.addMouseListener(mousePressed);
			KitSwing.adjustComponents(false,this);
		}
		void viewerRefreshed(ViewerTarget viewer){
			border.setBorder(createLineBorder(viewer.isActive()?activeColor
					:inactiveColor,focusWidth()));
			Container tabAncestor=getAncestorOfClass(JTabbedPane.class,focusMount);
			JTabbedPane tabs=tabAncestor==null||tabAncestor instanceof AppTabs?null
					:(JTabbedPane)tabAncestor;
			if(label==null)return;
			labelPanel.setVisible(tabs==null);
			label.setText(kit.decoration(viewer.title(),hints).caption+
					(false&&focusTrace?(" "+info(avatarPane)):""));
		}
		void setControl(JComponent control){
			if(control==null){
				if(setControl!=null)labelPanel.remove(setControl);
				return;
			}
			for(Component c:allComponents(control))c.addMouseListener(mousePressed);
			labelPanel.add(setControl=control,BorderLayout.EAST);
		}
		@Override
		public void paint(Graphics g){
			boolean times=false||timing;
			if(times)printElapsed(info(this)+".paint");
			super.paint(g);
			if(times)printElapsed(info(this)+".paint~");
		}
	}
	@Override
	public void setPaneControl(KWrap base){
		focusMount.setControl(base==null?null:(JComponent)base.wrapped());
	}
	@Override
	protected void traceOutput(String msg){
		msg=Debug.info(this)+msg;
		if(timing)printElapsed(msg);
		else Util.printOut(msg);
	}
	@Override
	public void refresh(final Impact impact){
		if(avatarPane instanceof JLabel)return;
		if(impact==Impact.DISPOSE){
			master.refreshAvatars(impact);
			return;
		}
		final ViewerTarget viewer=(ViewerTarget)facet.target();
		focusMount.viewerRefreshed(viewer);
		app=TargetCore.findNotifiableTyped(AppSurface.class,viewer);
		WatchableOperation op=new WatchableOperation("ViewerBase.refresh"){
			@Override
			public void doSimpleOperation(){
				boolean times=true;
				if(timing&&times)trace(".refresh");
				master.refreshAvatars(impact);
				if(timing&&times)trace(".refresh~");
			}
		};
		SAreaTarget parent=viewer.areaParent(),top=null;
		while(parent!=null){
			if(parent instanceof ContentArea)top=parent;
			parent=parent.areaParent();
		}
		if(plainRefresh?true
				:(top==null||top.isLive())&&viewer.isLive()&&(!listening||focusMount.isDisplayable())){
			if(false&&app!=null)app.runWatched(op);
			else if(false)try{
				op.doOperations();
			}
			catch(Exception e){
				throw new RuntimeException(e);
			}
			else op.doOperations();
		}
	  boolean active=viewer.isActive();
	  if(!active)return;
		setPaneTriggers(viewer.actionTriggers);
		if(forceFocus&&!avatarPane.isFocusOwner())invokeLater(new Runnable(){public void run(){
			if(!focuser.isRunning())focuser.start();
		}});
	}
	private static final boolean forceFocus=false,focusTrace=false,plainRefresh=false,timing=false;
	public final KitSwing kit;
	final FocusListener focusListener;
	final KitFacet facet;
	final JScrollPane scroll;
	JComponent avatarPane;
	private final FocusMount focusMount;		
	private final StringFlags hints;
	private AppSurface app;
	private ViewerMaster master;
	private boolean listening;
	public ViewerBase(KitFacet facet,ViewerMaster master,KitSwing kit,StringFlags hints){
	  this.facet=facet;
	  this.kit=kit;
	  this.hints=hints;
	  (this.master=master).base=this;
	  master.attachedToFacet();
	  boolean empty=false;
		avatarPane=empty?new JLabel(title()):master.newAvatarPane();
	  scroll=empty||!master.isScrollable()?null:new JScrollPane();
	  if(scroll!=null)scroll.setViewportView(avatarPane);
	  if(false){
	  	trace(".ViewerBase: ");
	  	Debug.printStackTrace(15);
	  }
	  if(avatarPane==null)throw new IllegalStateException(
				"Null avatarPane in "+info(this));
  	int cursor=master.defaultCursor();
  	if(cursor>0)avatarPane.setCursor(Cursor.getPredefinedCursor(cursor));
		ViewerTarget viewer=(ViewerTarget)facet.target();
		if(viewer==null)throw new IllegalStateException(
				"No viewer target in "+info(this));
  	if(hints.includeFlag(HINT_PANEL_BORDER))avatarPane.setBorder(
  			createLineBorder(new Color(127,157,185)));
  	if(false)setPaneTriggers(viewer.actionTriggers);
		avatarPane.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseReleased(MouseEvent e){
				if(e.isPopupTrigger())popupRequested(e.getX(),e.getY());
			}
			@Override
			public void mousePressed(MouseEvent e){
				if(e.isPopupTrigger())popupRequested(e.getX(),e.getY());
			}
		});
		avatarPane.addKeyListener(new KeyAdapter(){
			@Override
			public void keyPressed(KeyEvent e){
				if(e.getKeyCode()==KeyEvent.VK_CONTEXT_MENU)popupRequested(0,0);
			}
		});
    focusMount=new FocusMount();
		focusListener=new SmartFocusListener();
  	KitCore.widgets+=4;
  }
	private final class SmartFocusListener implements FocusListener{
		SmartFocusListener(){
			avatarPane.addFocusListener(this);
			if(scroll!=null){
				MouseAdapter mouse=new MouseAdapter(){
					Point thenAt=new Point(0,0);
					@Override
					public void mousePressed(MouseEvent e){
						focusGained(new FocusEvent(scroll,scroll.hashCode()));
					}
					@Override
					public void mouseWheelMoved(MouseWheelEvent e){
						Point nowAt=scroll.getViewport().getViewPosition();
						if(!nowAt.equals(thenAt))
							focusGained(new FocusEvent(scroll,scroll.hashCode()));
						thenAt=nowAt;
					}
				};
				scroll.addMouseWheelListener(mouse);
				for(JScrollBar bar:new JScrollBar[]{scroll.getHorizontalScrollBar(),
						scroll.getVerticalScrollBar()})bar.addMouseListener(mouse);
			}
			new Timer(1500,new ActionListener(){
	    	@Override
				public void actionPerformed(ActionEvent e){
					listening=true;
				}
			}){
				@Override
	    	public boolean isRepeats(){return false;}
	    }.start();
		}
		@Override
		public void focusGained(FocusEvent e){
			ViewerTarget viewer=(ViewerTarget)facet.target();
			boolean active=viewer.isActive();
			Object source=e.getSource();
			if(focusTrace)trace(".focusGained: active="+active+" source="+info(source));
			if(!listening||active)return;
			facet.targetNotify(this,false);
			if(source!=avatarPane&&!avatarPane.hasFocus())avatarPane.requestFocusInWindow();
		}
		@Override
		public void focusLost(FocusEvent e){
			if(focusTrace)traceDebug(".focusLost: > ",e.getOppositeComponent());
		}
	}
	private final Timer focuser=new Timer(50,new ActionListener(){
		public void actionPerformed(ActionEvent e){
			if(avatarPane==null||avatarPane.isFocusOwner())return;
			if(focusTrace)trace(".focuser: isFocusOwner=",avatarPane.isFocusOwner());
			avatarPane.requestFocusInWindow();
		}
	}){public boolean isRepeats(){return false;}};
	private void setPaneTriggers(STrigger[]triggers){
		boolean debug=false;
		if(false||triggers.length==0)return;
		if(debug)trace(".setPaneTriggers: facet="+facet.title()+" triggers=",triggers);
		KeyStroke[]strokes=new KeyStroke[triggers.length];
		for(int s=0;s<strokes.length;s++)
			strokes[s]=(KeyStroke)kit.getDecorationKeyStroke(triggers[s].title());
		InputMap paneInputs=avatarPane.getInputMap(),inputs=paneInputs;
		for(;inputs==paneInputs;inputs=inputs.getParent()){
			for(int st=0;st<strokes.length;st++){
				if(!triggers[st].isLive())continue;
				if(debug)trace(".setPaneTriggers: stroke=",strokes[st]);
				inputs.put(strokes[st],triggers[st].title());
			}
		}
		ActionMap paneActions=avatarPane.getActionMap(),actions=paneActions;
		for(;actions==paneActions;actions=actions.getParent()){
			if(debug)trace(".setPaneTriggers: actions=",actions.allKeys().length);
			for(int t=0;t<strokes.length;t++){
				final STrigger trigger=triggers[t];
				if(!trigger.isLive())continue;
				if(debug)trace(".setPaneTriggers: trigger=",trigger);
				actions.put(trigger.title(),new AbstractAction(){
					@Override
					public void actionPerformed(ActionEvent e){
						if(!trigger.isLive())return;
						trigger.fire();
						trigger.notifyParent(Impact.DEFAULT);
					}
				});
			}
			if(debug)trace(".setPaneTriggers: actions~=",actions.allKeys().length);
		}
	}
	public void popupRequested(int atX,int atY){
		final JPopupMenu popup=new JPopupMenu();
		focusListener.focusGained(new FocusEvent(popup,popup.hashCode()));
		if(app==null)return;
		ViewerTarget target=(ViewerTarget)facet.target();
		FacetFactory ff=true||app instanceof FacetAppSurface?
				((FacetAppSurface)app).ff:null;
		SFacet[]viewerFacets=master.getTargetFacets(ff);
		MenuFacets menuFacets=((SurfaceServices)app).getContextMenuFacets();
		SFacet[]facets=menuFacets==null?viewerFacets
				:menuFacets.getContextFacets(target,viewerFacets);
		if(facets==null)return;
		ItemList<KWrap>list=new ItemList(KWrap.class);
		for(int i=0;i<facets.length;i++)
			if(facets[i]!=null)list.addItems(((KitFacet)facets[i]).items());
		KWrap[]popups=list.items();
		if(panelShade!=null)popup.setBackground(new Color(panelShade.rgb()));
		popup.removeAll();
		for(int i=0;i<popups.length;i++)
			if(popups[i]==KWrap.BREAK)popup.addSeparator();
			else popup.add((JMenuItem)popups[i].wrapped());
		KitSwing.adjustComponents(true,popup);
		popup.show(scroll!=null?scroll:focusMount,atX,atY);
	}
	@Override
	public void setTools(KWrap tools){
		final JComponent panel=new JPanel();
		KitSwing.addToolGroups(panel,((JComponent)tools.wrapped()).getComponents());
		MouseAdapter clicks=new MouseAdapter(){
			public void mousePressed(MouseEvent e){
				focusListener.focusGained(
						new FocusEvent(panel,panel.hashCode()));
			}
		};
		for(Component c:KitSwing.allComponents(panel)){
			c.setFocusable(false);
			c.addMouseListener(clicks);
		}
		focusMount.add(panel,hints.includeFlag(HINT_PANEL_RIGHT)?BorderLayout.EAST
				:hints.includeFlag(HINT_PANEL_BELOW)?BorderLayout.SOUTH:BorderLayout.NORTH);
	}
	@Override
	public Object wrapped(){
		return focusMount;
	}
	public ProvidingCache providingCache(){
		return kit.providingCache();
	}
	@Override
	public KitFacet facet(){
		return facet;
	}
	@Override
	public String title(){
		return facet.title();
	}
	@Override
	public STarget[]targets(){
		return master.targets();
	}
	@Override
	public Object newWrapped(Object parent){
	  throw new RuntimeException("Not implemented in "+info(this));
	}
	public void disposeWrapped(){
		master.disposeAvatarPane();
		master=null;
		avatarPane=null;
		if(true)throw new RuntimeException("Not implemented in "+Debug.info(this));
		else trace(".disposeWrapped: ");
	}
}
