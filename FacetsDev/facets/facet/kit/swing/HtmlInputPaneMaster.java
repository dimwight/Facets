package facets.facet.kit.swing;
import static facets.facet.kit.swing.HtmlTestPane.*;
import static facets.util.Debug.*;
import static facets.util.Times.*;
import static java.awt.event.KeyEvent.*;
import static javax.swing.ScrollPaneConstants.*;
import facets.util.HtmlBuilder;
import facets.util.HtmlFormBuilder;
import facets.util.Tracer;
import facets.util.HtmlFormBuilder.FormInput;
import facets.util.HtmlFormBuilder.FormTag;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.FormView;
import javax.swing.text.html.HTML.Attribute;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.Option;
final class HtmlInputPaneMaster extends HtmlPaneMaster{
	private static final boolean focusTrace=false;
	private final Map<String,SmartView>views=new HashMap();
	private final Map<String,String>values=new HashMap();
	@Override
	protected JEditorPane newPane(){
		JEditorPane pane=new SmartPane() {
			@Override
			public boolean requestFocusInWindow(){
				if(focusTrace)traceDebug(".requestFocusInWindow: ",this);
				for(SmartView view:views.values())if(view.hasFocus())return false;
				return super.requestFocusInWindow();
			}
		};
		return pane;
	}
	@Override
	protected HTMLEditorKit newEditorKit(){
		return new HTMLEditorKit(){
			@Override
			public ViewFactory getViewFactory(){
				return new HTMLFactory(){
					@Override
					public View create(Element elem){
						View view=super.create(elem);
						return view instanceof FormView?new SmartView(elem):view;
					}
				};
			}
		};
	}
	@Override
	protected boolean updateRequired(String then,String now){
		boolean required=then==null||HtmlFormBuilder.layoutChanged(then,now);
		if(required){
			views.clear();
			if(false)trace(".updateRequired: now="+HtmlBuilder.normalisedContent(now,then,false).length());
		}
		else codeChanged(now);
		return required;
	}
	@Override
	protected void codeChanged(String code){
		boolean times=false;
		if(times)printElapsed("HtmlInputPaneMaster.codeChanged");
		if(false)traceDebug(".codeChanged: views=",views.values().toArray());
		for(SmartView view:views.values())view.refresh(code);
		if(times)printElapsed("HtmlInputPaneMaster.codeChanged~");
		if(times)SwingUtilities.invokeLater(new Runnable(){public void run(){
			printElapsed("HtmlInputPaneMaster.codeChanged~~");
		}});
	}
	private final class SmartView extends FormView implements FocusListener{
		private final Tracer t=Tracer.newTopped(info(this),true);
		private final String name;
		private final FormTag tag;
		private Component c;
		private TextSmarts ts;
		SmartView(Element elem){
			super(elem);
			if(false)t.trace(": atts=",mapInfo(attributeMap(elem.getAttributes())));
			AttributeSet atts=elem.getAttributes();
			name=(String)atts.getAttribute(Attribute.NAME);
			String tagName=atts.getAttribute(StyleConstants.NameAttribute).toString(),
				inputType=(String)atts.getAttribute(Attribute.TYPE);
			if(name==null)throw new IllegalStateException("Null name in "+info(this));
			else if(tagName==null)throw new IllegalStateException("Null tagName for "+name);
			else if(tagName.equals("input")&&inputType==null)throw new IllegalStateException(
					"Null inputType for "+name);
			else tag=FormTag.getTag(tagName.toLowerCase().trim(),
					inputType==null?null:inputType.toLowerCase().trim());
			if(false)t.trace(": name="+name+" tag="+tag);
			views.put(name,this);
		}
		@Override
		protected Component createComponent(){
			Component c=super.createComponent();
			boolean noScrolls=false;
			if(c instanceof JScrollPane){
				JScrollPane scroll=(JScrollPane)c;
				scroll.setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_AS_NEEDED);
				scroll.setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_AS_NEEDED);
				JTextArea field=(JTextArea)scroll.getViewport().getView();
				field.setWrapStyleWord(true);
				field.setLineWrap(true);
				if(false)trace(".createComponent: atts=",mapInfo(attributeMap(getAttributes())));
				noScrolls=getAttributes().getAttribute(Attribute.ROWS).toString().equals("1");
				this.c=field;
			}
			else this.c=c;
			if(this.c instanceof JTextComponent){
				final JTextComponent field=(JTextComponent)this.c;
				ts=new TextSmarts(field);
				field.addKeyListener(new KeyAdapter(){
					public void keyPressed(KeyEvent e){
						int keyCode=e.getKeyCode();
						switch(keyCode){
						case VK_ESCAPE:
							field.setText(value());
							field.selectAll();
							ts.resetUndo();
							break;
						case VK_ENTER:
							checkValue(field.getText());
							ts.resetUndo();
							break;
						default:
						}
					}
				});
				field.addFocusListener(new FocusListener(){
					@Override
					public void focusGained(FocusEvent e){
						field.selectAll();
					};
					@Override
					public void focusLost(FocusEvent e){
						Component focusTo=e.getOppositeComponent();
						if(focusTrace)t.traceDebug(".field.focusLost: name="+name+" >=",focusTo);
						String now=field.getText().replaceAll("\\s+"," ");
						for(SmartView view:views.values().toArray(new SmartView[]{}))
							if(view.c==focusTo)checkValue(now);
						String value=value();
						if(!now.equals(value))field.setText(value);
					};
				});
			}
			else if(c instanceof JComboBox){
				final JComboBox box=(JComboBox)c;
				box.addActionListener(new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent e){
						Object item=box.getSelectedItem();
						if(item instanceof Option)checkValue(((Option)item).getValue());
					}
				});
			}
			else if(c instanceof JCheckBox){
				final JCheckBox box=(JCheckBox)c;
				box.addActionListener(new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent e){
						checkValue(Boolean.valueOf(box.isSelected()).toString());
					}
				});
			}
			else if(this.c==c) 
				throw new RuntimeException("Not implemented for c="+info(c));
			this.c.addFocusListener(this);
			return this.c!=c&&noScrolls?this.c:c;
		}
		void refresh(String code){
			String value=tag.findValue(code,name);
			values.put(name,value);
			Component c=getComponent();
			if(c instanceof JScrollPane)c=((JScrollPane)c).getViewport().getView();
			if(c!=this.c)throw new IllegalStateException("Bad component in "+info(this));
			else if(c instanceof JTextComponent){
				JTextComponent field=(JTextComponent)c;
				field.setText(value);
				field.setCaretPosition(0);
				if(false&&field.hasFocus())field.selectAll();
				ts.resetUndo();
			}
			else if(c instanceof JComboBox){
				JComboBox box=(JComboBox)c;
				int itemCount=box.getItemCount(),at=0;
				for(;at<itemCount;at++){
					Object item=box.getItemAt(at);
					if(item instanceof Option&&((Option)item).getValue().equals(value)
							||item.equals(value)){
						box.setSelectedIndex(at);
						break;
					}
				}
				if(at==itemCount)box.insertItemAt(value,0);
			}
			else if(c instanceof JCheckBox)((JCheckBox)c).setSelected(value.equals("true"));
			else throw new RuntimeException("Not implemented for c="+info(c));
		}
		private String value(){
			return values.get(name);
		}
		private void checkValue(String check){
			if(check.equals(values.put(name,check)))return;
			viewerTarget().selectionEdited(null,new FormInput(name,check),false);
		}
		boolean hasFocus(){
			return c.isFocusOwner();
		}
		@Override
		public void focusGained(FocusEvent e){
			Object source=e.getSource();
			if(source!=c)throw new IllegalStateException("Bad source="+info(source));
			else if(focusTrace)t.traceDebug(".focusGained: ",source);
			base.focusListener.focusGained(e);
		}
		@Override
		public void focusLost(FocusEvent e){
			if(focusTrace)t.traceDebug(".focusLost: ",e.getSource());
		}
		@Override
		public void actionPerformed(ActionEvent e){}
	}
}