package facets.facet.kit.swing;
import static facets.core.superficial.app.SViewer.*;
import static javax.swing.SwingUtilities.*;
import static javax.swing.text.StyleConstants.*;
import facets.core.app.HtmlContent;
import facets.core.app.HtmlView;
import facets.core.app.HtmlContent.HtmlSelected;
import facets.core.app.HtmlView.SmartView;
import facets.core.superficial.Notifying.Impact;
import facets.core.superficial.app.SSelection;
import facets.core.superficial.app.ViewerTarget;
import facets.util.Debug;
import facets.util.StringFlags;
import facets.util.Times;
import facets.util.app.ProvidingCache;
import facets.util.app.ProvidingCache.ItemProvider;
import java.awt.AWTEvent;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.StringReader;
import java.net.URL;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.Caret;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.html.HTMLEditorKit;
/**
{@link ViewerMaster} wrapping a {@link JEditorPane}. 
<p>{@link HtmlSmartPaneMaster} is in some ways the most complex {@link ViewerMaster}:
<ul>
<li>It can display its own HTML source code by switching {@link EditorKit}s to match 
{@link HtmlView#showSource()}.
<li>In either display style line wrapping matches {@link HtmlView#wrapLines()}.  
<li>Content can be passed as as an {@link HtmlContent}, or as 
a {@link String} or {@link String}[] for setting as the source of an internal
 {@link HtmlContent}. 
<li>Where content is passed as a {@link String}[] and  
{@link HtmlView#quickLineHeight()} returns a useful value, it truncates 
the source of the internal {@link HtmlContent} to match the current pane size.
<li>Content renderings are cached as complete {@link JEditorPane}s, keyed against the  
{@link HtmlContent#source}.   
</ul>
<p>These features require the following content stack:
<ol>
<li>An {@link HtmlContent} wrapping HTML source which may have been passed as content, 
or created from a truncated {@link String}[] passed as content. 
<li>A {@link StyledDocument} created from {@link HtmlContent#getKitDocument(EditorKit)} for 
each change in HTML source. 
<li>A {@link JEditorPane} for each new {@link StyledDocument} to enable caching of 
its {@link View} tree. 
<li>A {@link HtmlWrapPane} to manage text wrap in the current {@link JEditorPane} and with a 
substitute {@link MouseListener} and {@link MouseWheelListener} to intercept attempts to 
adjust a {@link JEditorPane} displaying truncated content.    
</ol>
 */
final class HtmlSmartPaneMaster extends ViewerMaster{
	private final static boolean wrapPane=true,singleEditor=!wrapPane||true,timing=false;
	private static final Dimension screen=Toolkit.getDefaultToolkit().getScreenSize();
	final static EditorKit sourceKit=new StyledEditorKit(),renderKit=new HTMLEditorKit();
	private final class EditorProvider extends ItemProvider<JEditorPane>{
		private final boolean showSource;
		private final JEditorPane editorThen;
		private final HtmlContent html;
		EditorProvider(ProvidingCache c,Object source,boolean showSource,
				JEditorPane editorThen,HtmlContent html){
			super(c,source,HtmlSmartPaneMaster.class.getSimpleName()+".refreshAvatars");
			this.showSource=showSource;
			this.editorThen=editorThen;
			this.html=html;
		}
		@Override
		protected boolean passThrough(){
			return singleEditor;
		}
		@Override
		public CancelStyle cancelStyle(){
			return CancelStyle.Timeout;
		}
		@Override
		protected long buildByteCount(){
			return html.source().length()*3;
		}
		@Override
		protected long finalByteCount(JEditorPane item){
			return html.source().length();
		}
		@Override
		protected JEditorPane newItem(){
			if(timing)trace(": setting html=",html.source().length());
			JEditorPane editor=singleEditor?(JEditorPane)editorThen
					:newEditor(quickPaneHeight,viewerTarget());
			EditorKit kit=showSource?sourceKit:renderKit;
			Document doc=getKitDocument(kit);
			editor.setEditorKit(kit);
			editor.setDocument(doc);
			newEditor=true;
			return editor;
		}
		private Document getKitDocument(EditorKit kit){
			boolean notSource=kit instanceof HTMLEditorKit;
			Document doc=false?new HtmlSelectingDocument():kit.createDefaultDocument();
			if(doc.getLength()==0)try{
				kit.read(new StringReader(htmlInternal.source()),doc,0);
			}catch(Exception e){
				throw new RuntimeException(e);
			}	
			if(notSource)return HtmlSmartPaneMaster.this.doc=doc;
			SimpleAttributeSet atts=new SimpleAttributeSet();
			atts.addAttribute(Family,"Courier New");
			((StyledDocument)doc).setCharacterAttributes(0,doc.getLength(),atts,true);
			return doc;
		}
	}
	private final HtmlContent htmlInternal=new HtmlContent("htmlInternal",
		"Your source goes here");
	private final KitSwing kit;
	private final StringFlags hints;
	private boolean changingSelection,newEditor;
	private int quickPaneHeight;
	private Document doc;
	HtmlSmartPaneMaster(KitSwing kit,StringFlags hints){
		this.kit=kit;
		this.hints=hints;
	}
	@Override
	protected void traceOutput(String msg){
		if(timing)Times.printElapsed(Debug.info(this)+msg);
		else if(true)super.traceOutput(msg);
	}
	protected JComponent newAvatarPane(){
		final JEditorPane editor=newEditor(quickPaneHeight,viewerTarget());
		editor.addHyperlinkListener(new HyperlinkListener(){
			public void hyperlinkUpdate(final HyperlinkEvent e){
				if(e.getEventType()!=HyperlinkEvent.EventType.ACTIVATED)return;
				Caret caret=editor.getCaret();
				editor.setCaret(null);
				viewerTarget().selectionChanged(new SSelection(){
					public Object content(){
						return e.getURL();
					}
					public Object single(){
						throw new RuntimeException("Not implemented in "+Debug.info(this));
					}
					public Object[]multiple(){
						throw new RuntimeException("Not implemented in "+Debug.info(this));
					}
				});
				editor.setCaret(caret);
			}
		});
		if(!wrapPane)return editor;
		HtmlWrapPane pane=new HtmlWrapPane(editor);
		final class QuickParasListener extends MouseAdapter{
			public void mouseWheelMoved(MouseWheelEvent e){
				clearParas();
			}
			public void mouseReleased(MouseEvent e){
				clearParas();
			}
			public void mousePressed(MouseEvent e){
				clearParas();
			}
			public void mouseClicked(MouseEvent e){
				clearParas();
			}
			private void clearParas(){
				if(quickPaneHeight>0){
					JComponent pane=avatarPane();
					int wheelListeners=pane.getMouseWheelListeners().length;
					if(wheelListeners==0)return;
					if(wrapPane)trace(".clearQuickParas: newEditor=",newEditor);
					if(true||newEditor)pane.setCursor(Cursor.getPredefinedCursor(CURSOR_WAIT));
				}
				viewerTarget().ensureActive(Impact.ACTIVE);
			}
		}
		QuickParasListener qpl=new QuickParasListener();
		if(false)pane.addMouseWheelListener(qpl);
		pane.addMouseListener(qpl);
		return pane;
	}
	public void refreshAvatars(Impact impact){
		if(changingSelection)return;
		ViewerTarget viewer=viewerTarget();
		SmartView view=(SmartView)viewer.view();
		boolean wrapLines=view.wrapLines();
		if(!wrapLines&&!wrapPane)throw new IllegalStateException(
				"No unwrap in "+Debug.info(this));
		JComponent pane=avatarPane();
		final boolean wrapping=pane instanceof HtmlWrapPane;
		HtmlWrapPane wrapper=!wrapping?null:(HtmlWrapPane)pane;
		if(wrapper==null)throw new IllegalStateException(
				"Null wrapper in "+this);
		JViewport viewport=wrapping?wrapper.findViewport():(JViewport)pane.getParent();
		final JEditorPane editorThen=wrapping?wrapper.editor():(JEditorPane)pane;
		if(wrapping){
			if(false&&inInactivePane(editorThen))return;
			wrapper.refreshForWrap(wrapLines);
		}
		if(false)trace(":" +" wrapPane="+wrapPane+" singleEditor="+singleEditor+
				" editorThen=",Debug.id(editorThen));		
		final Object content=viewer.selection().content();
		final HtmlContent html=content instanceof HtmlContent?
				(HtmlContent)content:htmlInternal;
		boolean quickParas=false;
		if(html==htmlInternal){
			if(content instanceof String)html.setState(content);
			else{
				String[]paraLines=(String[])content;
				int portHeight=viewport.getSize().height,
					quickParaHeight=KitSwing.px(view.quickLineHeight());
				quickParas=!wrapLines&&quickParaHeight>=0&&portHeight>0&&!viewer.isActive();
				quickPaneHeight=!quickParas?-1:paraLines.length*quickParaHeight;
				int stopLineAt=!quickParas?paraLines.length:(screen.height/1/quickParaHeight),
					lineAt=0;
				boolean debug=false;
				if(debug)trace(".refreshAvatars: quickParaHeight="+quickParaHeight+
						" lines="+(paraLines.length-2)+" stopLineAt="+stopLineAt);
				StringBuilder setSource=new StringBuilder();
				for(String line:paraLines)if(lineAt++<stopLineAt){
						setSource.append(line.trim());
						setSource.append("\n<p>");
					}
					else break;
				setSource.append(paraLines[paraLines.length-1]);
				if(debug)trace(".refreshAvatars: quickPaneHeight="+quickPaneHeight+
						" setSource=",setSource.length());
				html.setState(setSource.toString());
			}
		}
		final boolean showSource=view.showSource();
		if(timing)trace(": getting editor source=",html.source().length());
		JEditorPane editorNow=false?editorThen:
			new EditorProvider(base().providingCache(),this,showSource,editorThen,
					html).getForValues(this,html.source(),showSource?1:0,wrapLines?1:0);
		if(timing)traceDebug(": got editorNow=",editorNow);
		if(wrapper!=null)wrapper.refreshForEditor(editorNow);
		editorNow.getCaret().setDot(-1);
		viewport.scrollRectToVisible(new Rectangle(0,0,0,0));
		((JScrollPane)viewport.getParent()).setWheelScrollingEnabled(!quickParas);
		newEditor=false;
		if(listener==null||!editorNow.isEditable())return;
		editorNow.getDocument().addDocumentListener(listener);
		HtmlSelected runs=(HtmlSelected)viewer.selection().single();
	}
	private boolean inInactivePane(final JComponent editor){
		AppDesktopFrame frame=(AppDesktopFrame)getAncestorOfClass(
				AppDesktopFrame.class,editor);
		return frame!=null&&!frame.isSelected();
	}
	private static JEditorPane newEditor(final int quickPaneHeight,ViewerTarget viewerTarget){
		JEditorPane editor=new JEditorPane(){
			public void paintComponent(Graphics g){
				super.paintComponent(g);
				SwingUtilities.invokeLater(new Runnable(){public void run(){
					setCursor(Cursor.getPredefinedCursor(CURSOR_TEXT));
				}});
			}
			public Dimension getPreferredSize(){
				Dimension superSize=super.getPreferredSize();
				return new Dimension(superSize.width,
						quickPaneHeight<=0?superSize.height:quickPaneHeight);
			}
			public void removeMouseWheelListener(MouseWheelListener l){
				super.removeMouseWheelListener(l);
				disableEvents(AWTEvent.MOUSE_WHEEL_EVENT_MASK);
			};
		};
		boolean live=((HtmlView)viewerTarget.view()).isLive();
		editor.setEditable(live);
		editor.setDragEnabled(true);
		if(false)editor.setCaret(new DefaultCaret(){
			public void focusLost(FocusEvent e){}
		});
		return editor;
	}
	@Override
	protected int defaultCursor(){
		return CURSOR_TEXT;
	}
	@Override
	protected
	void disposeAvatarPane(){
		HtmlWrapPane wrapPane=(HtmlWrapPane)avatarPane();
		JEditorPane editor=wrapPane.editor();
		editor.setDocument(editor.getEditorKit().createDefaultDocument());
		editor.setUI(null);
		editor.setEditorKit(null);
		wrapPane.refreshForEditor(null);
		Container parent=wrapPane.getParent();
		if(parent!=null)parent.remove(wrapPane);
	}
	private final DocumentListener listener=true?null:new DocumentListener(){
		public void changedUpdate(DocumentEvent e){
			traceDebug(".changedUpdate: ",e.getLength());
		}
		public void removeUpdate(DocumentEvent e){
			traceDebug(".removeUpdate: ",e.getLength());
		}
		public void insertUpdate(DocumentEvent e){
			traceDebug(".insertUpdate: ",e.getLength());
		}
	};
}