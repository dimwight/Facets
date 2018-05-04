package facets.facet.kit.swing;
import static java.awt.Cursor.*;
import facets.core.superficial.Notifying.Impact;
import facets.core.superficial.app.SSelection;
import facets.util.Debug;
import facets.util.HtmlBuilder;
import facets.util.Times;
import facets.util.app.BusyCursor.BusySettable;
import java.awt.Cursor;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.net.URL;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.Caret;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.HTMLEditorKit;
class HtmlPaneMaster extends ViewerMaster{
	protected class SmartPane extends JEditorPane implements BusySettable{
		SmartPane(){
			ToolTipManager.sharedInstance().registerComponent(this);
			setEditable(false);
			setEditorKit(newEditorKit());
			setDragEnabled(true);
			addHyperlinkListener(new HyperlinkListener(){
				public void hyperlinkUpdate(final HyperlinkEvent e){
					if(e.getEventType()!=HyperlinkEvent.EventType.ACTIVATED)return;
					Caret caret=getCaret();
					setCaret(null);
					final URL url=e.getURL();
					viewerTarget().selectionChanged(new SSelection(){
						public Object content(){
							return url==null||url.toString().substring("http:".length()).trim().equals("")?null
								:url;
						}
						public Object single(){
							throw new RuntimeException("Not implemented in "+Debug.info(this));
						}
						public Object[]multiple(){
							throw new RuntimeException("Not implemented in "+Debug.info(this));
						}
					});
					setCaret(caret);
				}
			});
		}
		@Override
		public String getToolTipText(MouseEvent e){
			if(getCursor()!=getPredefinedCursor(HAND_CURSOR))return null;
			int charAt=viewToModel(e.getPoint());
			Element para=((DefaultStyledDocument)getDocument()).getCharacterElement(charAt
					).getParentElement();
			int elemCount=para.getElementCount();
			Object anchor=null;
			for(int at=0;at<elemCount;at++){
				Element elem=para.getElement(at);
				Object maybe=elem.getAttributes().getAttribute(Tag.A);
				if(maybe==null)continue;
				else anchor=maybe;
				if(elem.getEndOffset()>charAt)break;
			}
			if(anchor==null)throw new IllegalStateException(
					"Null anchor in "+Debug.info(this));
			else return anchor.toString().replace("href="+HtmlBuilder.HTTP,"");
		}
		@Override
		public void setCursor(Cursor cursor){
			super.setCursor(cursor==getPredefinedCursor(DEFAULT_CURSOR)?
					getPredefinedCursor(TEXT_CURSOR):cursor);
		}
	}
	protected JEditorPane newPane(){
		SmartPane pane=new SmartPane();
		pane.setCursor(getPredefinedCursor(TEXT_CURSOR));
		return pane;
	}
	private String code;
	protected HTMLEditorKit newEditorKit(){
		return new HTMLEditorKit();
	}
	@Override
	final protected JComponent newAvatarPane(){
		return newPane();
	}
	@Override
	final public void refreshAvatars(Impact impact){
		if(impact==Impact.DISPOSE)return;
		JEditorPane editor=(JEditorPane)avatarPane();
		String code=(String)viewerTarget().selection().content();
		if(!updateRequired(this.code,code))return;
		if(false)Times.printElapsed("HtmlPaneMaster.refreshAvatars");
		editor.setText(this.code=code);
		Caret caret=editor.getCaret();
		if(caret!=null)caret.setDot(-1);
		((JViewport)editor.getParent()).scrollRectToVisible(new Rectangle(0,0,0,0));
		if(true)SwingUtilities.invokeLater(new Runnable(){public void run(){
			codeChanged(HtmlPaneMaster.this.code);
			if(false)Times.printElapsed("HtmlPaneMaster.refreshAvatars~");
		}});
	}
	protected boolean updateRequired(String then,String now){
		return !now.equals(then);
	}
	protected void codeChanged(String code){}
}