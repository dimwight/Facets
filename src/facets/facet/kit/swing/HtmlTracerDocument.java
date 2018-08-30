package facets.facet.kit.swing;
import static java.lang.Math.*;
import facets.util.Debug;
import facets.util.TextLines;
import facets.util.Tracer;
import facets.util.Util;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.PlainDocument;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTMLEditorKit.ParserCallback;
abstract class HtmlTracerDocument extends HTMLDocument{
	final public void trace(String msg){
		traceOutput(msg);
	}
	final public void trace(String msg,Throwable t,boolean stack){
		if(stack&=t!=null){
			traceOutput(msg);		
			t.printStackTrace();
		}
		else traceOutput(msg+traceObjectText(t));		
	}
	final public void trace(String msg,Object o){
		traceOutput(msg+traceObjectText(o));		
	}
	final public void trace(String msg,Collection c){
		traceOutput(msg+traceArrayText(c.toArray()));		
	}
	final public void trace(String msg,Object[]array){
		traceOutput(msg+traceArrayText(array));		
	}
	final public void traceDebug(String msg,Object o){
		traceOutput(msg+Debug.info(o));		
	}
	final public void traceDebug(String msg,Object[]array){
		traceOutput(msg+(false?Debug.info(array):Debug.arrayInfo(array)));		
	}
	/**
	Outputs complete trace messages to console or elsewhere. 
	<p>Default prepends helpful classname to message.  
	@param msg passed from one of the <code>public</code> methods
	 */
	protected void traceOutput(String msg){
		traceOutputWithClass(msg);
	}
	protected String traceObjectText(Object o){
		return o==null?null:o.toString();
	}
	private String traceArrayText(Object[]array){
		return Util.arrayPrintString(array);
	}
	final public void traceOutputWithClass(String msg){
		Util.printOut((false?getClass().getSimpleName()
				:Util.helpfulClassName(this))+msg);
	}
}