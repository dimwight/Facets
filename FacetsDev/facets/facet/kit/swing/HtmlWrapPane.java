package facets.facet.kit.swing;
import static facets.util.Debug.*;
import facets.util.Debug;
import facets.util.Tracer;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelListener;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JViewport;
final class HtmlWrapPane extends JPanel{
	private final Tracer t=Tracer.newTopped(HtmlWrapPane.class.getSimpleName(),false);
	private JEditorPane editor;
	private boolean wrapNow;
	HtmlWrapPane(JEditorPane editor){
		super(new BorderLayout());
		add(this.editor=editor);
	}
	void refreshForEditor(JEditorPane editor){
		if(this.editor==editor)return;
		else if(editor==null)editor=new JEditorPane("text","Disposal editor");
		t.trace(".refreshForEditor wrapNow="+wrapNow+" parent=",info(editor.getParent()));
		if(wrapNow)((JViewport)this.editor.getParent()).setView(editor);
		else{
			removeAll();
			add(editor);
		}
		this.editor=editor;
		findViewport().validate();
		editor.repaint();
		adjustUi(this,editor);
	}
	private void adjustUi(JComponent from,JComponent to){
		to.setCursor(from.getCursor());
		for(MouseListener l:to.getMouseListeners())to.removeMouseListener(l);
		for(MouseListener l:from.getMouseListeners())to.addMouseListener(l);
		for(MouseWheelListener l:to.getMouseWheelListeners())to.removeMouseWheelListener(l);
		for(MouseWheelListener l:from.getMouseWheelListeners())to.addMouseWheelListener(l);
	}
	void refreshForWrap(boolean wrapNow){
		if(this.wrapNow==wrapNow)return;
		Container parentThen=editor.getParent();
		boolean wrapThen=parentThen instanceof JViewport;
		if(wrapThen!=this.wrapNow)throw new IllegalStateException(
				"Inconsistent wrap state in "+info(this));
		else this.wrapNow=wrapNow;
		t.trace(".refreshForWrap wrapThen="+wrapThen+" wrapNow="+wrapNow);
		if(wrapNow){
			removeAll();
			((JViewport)getParent()).setView(editor);
			adjustUi(this,editor);
		}
		else{
			add(editor);
			adjustUi(editor,this);
			((JViewport)parentThen).setView(this);
		}
		findViewport().validate();
	}
	JViewport findViewport(){
		Container parent=getParent(),viewport=parent==null?editor.getParent():parent;
		if(viewport==null)throw new IllegalStateException(
				"Null viewport in "+info(this));
		else return(JViewport)viewport;
	}
	JEditorPane editor(){
		if(editor.getParent()==null)throw new IllegalStateException(
				"Not adjusted for editor="+info(editor)+" in "+info(this));
		else return editor;
	}
}