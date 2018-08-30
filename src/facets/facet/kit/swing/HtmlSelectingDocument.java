package facets.facet.kit.swing;
import static java.lang.Math.*;
import static javax.swing.text.DefaultStyledDocument.ElementSpec.*;
import facets.core.app.HtmlContent.HtmlSelected;
import facets.util.Debug;
import facets.util.HtmlPrettifier;
import facets.util.TextLines;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.PlainDocument;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTMLEditorKit.ParserCallback;
import javax.swing.text.html.ObjectView;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
final class HtmlSelectingDocument extends HtmlTracerDocument{
	private static final String SOURCE_AT="sourceAt";
	HtmlSelected newSourceSelected(int renderStart,int renderStop){
		HtmlSelected selected=new HtmlSelected(renderStart,renderStop,-1,-1){
			@Override
			public boolean fontIsBold(){
				boolean isSo=true;
				for(int pos=renderStart;pos<renderStop+(renderStart==renderStop?1:0);pos++)
					isSo&=StyleConstants.isBold((AttributeSet)getCharacterElement(pos));
				return isSo;
			}
			@Override
			public boolean fontIsItalic(){
				boolean isSo=true;
				for(int pos=renderStart;pos<renderStop+(renderStart==renderStop?1:0);pos++)
					isSo&=StyleConstants.isItalic((AttributeSet)getCharacterElement(pos));
				return isSo;
			}
		};
		if(true)return selected;
		else if(true)throw new RuntimeException("Not implemented in "+Debug.info(this));
		Element charsFirst=getCharacterElement(renderStart),
			charsLast=getCharacterElement(renderStop);
		String source=source();
		int charsSourceAt=(Integer)charsFirst.getAttributes().getAttribute(SOURCE_AT),
			charsFirstAt=charsFirst.getStartOffset(),
			startAt=charsSourceAt+renderStart-charsFirstAt+1,
			stopAt=(Integer)charsLast.getAttributes().getAttribute(SOURCE_AT)+
				renderStop-charsLast.getStartOffset()+1,
			lastCloseAt=source.substring(0,charsSourceAt).lastIndexOf("</"),
			lastTagAt=source.substring(0,charsSourceAt).lastIndexOf("<");
		if(charsFirstAt==renderStart&&lastTagAt>lastCloseAt){
			String fromLastClose=source.substring(lastCloseAt<0?charsSourceAt:lastCloseAt,
					charsSourceAt);
			startAt-=(fromLastClose.length()-fromLastClose.indexOf("<",1)+1);
		}
		return null;
	}
	@Override
	protected void traceOutput(String msg){
		if(true)super.traceOutput(msg);
	}
	final Document code=new PlainDocument(){{
		putProperty(tabSizeAttribute,2); 
	}};
	@Override
	public ParserCallback getReader(int pos){
		return new HTMLReader(pos){
			int sourceAt;
			@Override
			public void handleStartTag(Tag t,MutableAttributeSet atts,int pos){
				if(true||!t.isBlock())storeSourcePosition(pos,t+">");
				super.handleStartTag(t,atts,pos);
			}
			@Override
			public void handleEndTag(Tag t,int pos){
				super.handleEndTag(t,pos);
				if(true||!t.isBlock())storeSourcePosition(pos,"/"+t+">");
			}
			private void storeSourcePosition(int pos,String tagText){
				if(true||tagText.contains("/"))pos+=tagText.length();
				charAttr.addAttribute(SOURCE_AT,pos);
				if(false)trace(":" +pos+"="+source().substring(pos).trim());
			}
		};
	}
	private static final class SmartES extends ElementSpec{
		SmartES(ElementSpec in){
			super(in.getAttributes(),in.getType(),in.getArray(),in.getOffset(),in.getLength());
		}
		String text(){
			return !isContent()?"":(new String(getArray()));
		}
		private boolean isContent(){
			return getType()==ContentType;
		}
		public String toString(){
			return super.toString()+(!isContent()?"":(":'"+text()+"'"));
		}
		boolean isNewLineContent(){
			return isContent()&&text().equals("\n");
		}
	}
	@Override
	protected void insert(int offset,ElementSpec[]data)throws BadLocationException{
		SmartES[]in=new SmartES[data.length];
		for(int i=0;i<data.length;i++)in[i]=new SmartES(data[i]);
		trace(".insert: in=",in);
		boolean adjust=true&&in[0].isNewLineContent();
		if(!adjust){
			super.insert(offset,data);
			return;
		}
		SmartES[]out=new SmartES[in.length-3-3];
		for(int o=0;o<out.length;o++){
			SmartES next=in[o+3];
			if(next.isContent()&&o==0)
				next.setDirection(o==0?JoinPreviousDirection:JoinNextDirection);
			out[o]=next;
		}
		trace(".insert: out=",out);
		super.insert(offset,out);
	}
	@Override
	protected Element createLeafElement(Element parent,AttributeSet a,int p0,
			int p1){
		Integer pos=(Integer)a.getAttribute(SOURCE_AT);
		try{
			String text=getText(p0,p1-p0).trim();
			if(false&&!text.equals(""))trace(":" +pos+"="+source().substring(pos).trim());
		}catch(BadLocationException e){
			throw new RuntimeException(e);
		}
		return super.createLeafElement(parent,a,p0,p1);
	}
	void readBoth(EditorKit codeKit,EditorKit renderKit,InputStream stream)
		throws IOException,BadLocationException{
		codeKit.read(new HtmlPrettifier().prettyStream(stream),code,0);
		renderKit.read(new ByteArrayInputStream(source().getBytes()),this,0);
	}
	void updateCode(EditorKit codeKit,EditorKit renderKit){
		try{
			ByteArrayOutputStream bytes=new ByteArrayOutputStream();
			renderKit.write(bytes,this,0,getLength());
			code.remove(0,code.getLength());
			codeKit.read(new HtmlPrettifier().prettyStream(
					new ByteArrayInputStream(bytes.toByteArray())),code,0);
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	private String source(){
		try{
			return code.getText(0,code.getLength());
		}catch(BadLocationException e){
			throw new RuntimeException(e);
		}
	}
	final static class PaneTest extends IdiomPane{
		final JEditorPane code=new JEditorPane(),render=new JEditorPane();
		final JTree tree=new JTree();
		final JPanel panel=new JPanel(new GridLayout(0,2,10,10));
		private final TextLines lines;
		PaneTest(TextLines lines,String[]args){
			super(args);
			setRubric("Check list style.");
			this.lines=lines;
			render.setEditorKit(new HTMLEditorKit(){
				@Override
				public ViewFactory getViewFactory(){
					return true?super.getViewFactory():new HTMLFactory(){
						@Override
						public View create(Element elem){
							if(false)trace(".create: elem="+elem.toString().trim());
							return !elem.getName().equals("object")?super.create(elem)
									:new ObjectView(elem){
								@Override
								protected Component createComponent(){
									Element element=getElement();
									if(false)trace(".create: atts=",attributeMap(element).toString().split(","));
									return new JTextField("Hi I'm a field");
								}
								@Override
								public float getPreferredSpan(int axis){
									return axis==View.X_AXIS?100f:20f;
								}
								@Override
								public float getMaximumSpan(int axis){
									return getPreferredSpan(axis);
								}
							};
						}
					};
				}
			});
			render.setDragEnabled(true);
			code.setEditable(true);
			code.setDragEnabled(true);
			panel.setPreferredSize(new Dimension(500,300));
			JPanel texts=new JPanel(new GridLayout(2,0,10,10));
			panel.add(texts);
			texts.add(new JScrollPane(render));
			texts.add(new JScrollPane(code));
			panel.add(new JScrollPane(tree));
		}
		protected Component newPanel(Container pane){
			final EditorKit codeKit=code.getEditorKit(),renderKit=render.getEditorKit();
			final HtmlSelectingDocument doc=new HtmlSelectingDocument();
			doc.addDocumentListener(new DocumentListener(){
				public void changedUpdate(DocumentEvent e){
					updateCode();
				}
				public void removeUpdate(DocumentEvent e){
					updateCode();
				}
				public void insertUpdate(DocumentEvent e){
					updateCode();
				}
				private void updateCode(){
					doc.updateCode(codeKit,renderKit);
					tree.setModel(new DefaultTreeModel((TreeNode)render.getDocument(
							).getDefaultRootElement()));
				}
			});
			try{
				doc.readBoth(codeKit,renderKit,lines.newInputStream());
			}
			catch(Exception e){
				throw new RuntimeException(e);
			}
			if(false)render.setEditorKit(new StyledEditorKit());
			render.setDocument(doc);
			int selectAt=8;
			render.setSelectionStart(selectAt);
			render.setSelectionEnd(selectAt+0);
			code.setDocument(doc.code);
			code.selectAll();
			render.addCaretListener(new CaretListener(){
				@Override
				public void caretUpdate(CaretEvent e){
					int dot=e.getDot(),mark=e.getMark(),
						paneStart=min(mark,dot),paneStop=max(mark,dot);
					HtmlSelected runs=doc.newSourceSelected(paneStart,paneStop);
					if(false)trace(".caretUpdate: fontIsItalic="+runs.fontIsItalic()+
							" fontIsBold="+runs.fontIsBold());
					if(true)return;
					code.select(runs.codeStart,runs.codeStop);
					code.requestFocus();
				}
			});
			return panel;
		}
	}
	public static void main(String[]args){
		TextLines.setDefaultEncoding(true);
		new PaneTest(new TextLines(new File("_doc/HtmlSelected.html")),args
				).buildAndLaunch(false);
	}
	private static Map attributeMap(Element elem){
		Map map=new HashMap();
		AttributeSet attributes=elem.getAttributes();
		for(Object name:Collections.list(attributes.getAttributeNames()))
			map.put(name,attributes.getAttribute(name));
		return map;
	}
}