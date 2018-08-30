package facets.facet.kit.swing;
import static java.lang.Math.*;
import facets.core.app.HtmlContent.HtmlSelected;
import facets.util.TextLines;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.File;
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
import javax.swing.text.EditorKit;
import javax.swing.text.Element;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.FormView;
import javax.swing.text.html.HTML.Attribute;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.ObjectView;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
final class HtmlTestPane extends IdiomPane{
	final JEditorPane code=new JEditorPane(),render=new JEditorPane();
	final JTree tree=new JTree();
	final JPanel panel=new JPanel(new GridLayout(0,2,10,10));
	private final TextLines lines;
	HtmlTestPane(TextLines lines,String[]args){
		super(args);
		setRubric("Test input submit.");
		this.lines=lines;
		render.setDragEnabled(true);
		render.setEditable(false);
		code.setEditable(true);
		code.setDragEnabled(true);
		render.setEditorKit(new HTMLEditorKit(){
			@Override
			public ViewFactory getViewFactory(){
				return false?super.getViewFactory():new HTMLFactory(){
					@Override
					public View create(Element elem){
						View view=super.create(elem);
						if(view instanceof FormView){
							Map atts=attributeMap(elem.getAttributes());
							trace(": ",atts.get(Attribute.NAME));
							return new FormView(elem){
								protected void submitData(String data){
									JTextField field=(JTextField)getComponent();
									String text=field.getText();
									trace(".submitData:" +" text="+text+" field=",field);
								};
							};
						}
						return !elem.getName().equals("object")?view:new ObjectView(elem){
							@Override
							protected Component createComponent(){
								Element element=getElement();
								if(false)trace(".create: atts=",attributeMap(element.getAttributes()).toString().split(","));
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
	static Map attributeMap(AttributeSet attributes){
		Map map=new HashMap();
		for(Object name:Collections.list(attributes.getAttributeNames()))
			map.put(name,attributes.getAttribute(name));
		return map;
	}
	public static void main(String[]args){
		TextLines.setDefaultEncoding(true);
		new HtmlTestPane(new TextLines(new File("_doc/HtmlTestPane.html")),args
				).buildAndLaunch(false);
	}
}