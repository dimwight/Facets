package facets.facet.kit.swing;
import static facets.core.app.AppConstants.*;
import static facets.core.app.Dialogs.Response.*;
import static facets.facet.FacetFactory.*;
import static facets.facet.kit.swing.SmartOptionPane.*;
import static javax.swing.JOptionPane.*;
import static javax.swing.ScrollPaneConstants.*;
import facets.core.app.ActionAppSurface;
import facets.core.app.AppWindowHost;
import facets.core.app.Dialogs;
import facets.core.app.HideableHost;
import facets.core.app.TextView;
import facets.core.app.Dialogs.MessageTexts;
import facets.core.superficial.app.SSurface.WindowAppSurface;
import facets.facet.kit.*;
import facets.util.Debug;
import facets.util.FileSpecifier;
import facets.util.Objects;
import facets.util.Strings;
import facets.util.TextLines;
import facets.util.Tracer;
import facets.util.Util;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.html.HTMLEditorKit;
final class DialogsSwing extends Dialogs{
	private static final Tracer t=Tracer.newTopped("DialogsSwing",true);
	private static final int TEXT_ROWS=0,TEXT_COLS=1,TEXT_CONTENT=2;
	private final static SmartOptionPane choicePane=new SmartOptionPane(DEFAULT_AT,false),
		exceptionPane=new SmartOptionPane(DEFAULT_AT,true),
		inputPane=new SmartOptionPane(DEFAULT_AT,false),
		htmlPane=new SmartOptionPane(DEFAULT_AT,true){
			JEditorPane text;
			@Override
			public
			void updateMessage(Object...values){
				if(text==null){
					text=new JEditorPane();
					text.setEditorKit(false?new StyledEditorKit():new HTMLEditorKit());
					text.setEditable(false);
					JPanel messagePane=new JPanel(new BorderLayout(0,10));
					messagePane.add(new JScrollPane(text,VERTICAL_SCROLLBAR_AS_NEEDED,
							HORIZONTAL_SCROLLBAR_AS_NEEDED),BorderLayout.CENTER);
					setMessage(messagePane);
				}
				ClassLoader loader=getClass().getClassLoader();
				String path=(String)values[0];
				URL url=loader.getResource(path);
				if(url==null)url=loader.getResource("Facets.html");
				String noContent="[Missing content: "+path+"]",html;
				try{
					html=false?noContent:new TextLines(url).readLinesString();
				}catch(Exception e){
					html=noContent;
				}
				double fontFactor=(Double)values[1];
				try{
					String _fontSize="font-size:\\s*(\\d+)";
					Pattern p=Pattern.compile(_fontSize);
					int from=0;
					Matcher m;
					while((m=p.matcher(html)).find(from)){
						String fontSize=m.group();
						double size=Util.sf(Double.valueOf(fontSize.replaceAll(_fontSize,"$1"))*fontFactor);
						fontSize=fontSize.replaceAll(_fontSize,"font-size:" +size);
						html=html.substring(0,m.start())+fontSize+html.substring(m.end());
						from=m.start()+fontSize.length();
					}
				}
				catch(Exception e){}
				if(false){
					Dimension size=text.getSize();
					t.trace(".updateMessage: size=",size);
					String[]sizeValues=html.replaceAll(
							"(?s).+<table.*width=\"(\\d+).*height=\"(\\d+).*>.*","$1 $2"
					).split(" ");
					try{
						size=new Dimension((int)(Integer.valueOf(sizeValues[0])*fontFactor),
							(int)(size.height<50?Integer.valueOf(sizeValues[1])*fontFactor:size.height));
					}catch(Exception e){
						size=new Dimension(800,200);
					}		
					t.trace(".updateMessage: ~size=",size);
					text.setMinimumSize(size);
				}
				text.setText(html);
				text.setCaretPosition(0);
				validate();
			}
		};
	private final KitSwing kit;
  private FileChooser fileChooser;
	DialogsSwing(WindowAppSurface app,KitSwing kit){
		super(app);
		KitCore.widgets+=2;
		this.kit=kit;
	}
	private static Response getChoiceSwing(String title,int messageType,int optionType,
				boolean zeroIsYes,Object content,Component parent,SmartOptionPane pane){		
			pane.setMessageType(messageType);
			pane.setMessage(content);
			pane.validate();
			pane.setOptionType(optionType);
			for(Component each:KitSwing.allComponents(choicePane)){
				JComponent c=(JComponent)each;
				InputMap inputMap=c.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
				if(inputMap.allKeys()==null)continue;
				if(true){
					c.addKeyListener(new KeyAdapter(){
						@SuppressWarnings("deprecation")
						public void keyPressed(KeyEvent e){
							if(Character.isLetter(e.getKeyChar()))
								e.setModifiers(e.getModifiers()|KeyEvent.ALT_DOWN_MASK);
						}
					});
					continue;
			}
			t.trace(": component=",c.hashCode());
			ActionMap actionMap=c.getActionMap();
			for(KeyStroke key:c.getRegisteredKeyStrokes())
				if(key.getModifiers()==0&&key.getKeyEventType()==KeyEvent.KEY_RELEASED
						&&key.getKeyCode()!=KeyEvent.VK_SPACE){
					if(true)t.trace(": key=" ,key+" action="+c.getActionForKeyStroke(key));
					KeyStroke addKey=KeyStroke.getKeyStroke(key.getKeyCode(),0,false);
					for(Object action:actionMap.allKeys())inputMap.put(addKey,action);
					t.trace(": ",actionMap.get(inputMap.get(addKey)));
			}
		}
		pane.displayInDialog(parent,title.replaceAll("&",""));
	  Object value=pane.getValue();
		int choice=value==null?-1:((Integer)value).intValue();
	  switch(choice){
	    case 0:return zeroIsYes?Yes:Ok;
	    case NO_OPTION:return No;
	    case CANCEL_OPTION:case -1:return Cancel;
	    default:throw new RuntimeException("Unhandled choice "+choice);
	  }
	}
	protected HideableHost newHost(){
		return new DialogHostSwing((AppWindowHost)app.host(),kit);
	}
	public String getTextInput(String title,String rubric,String proposal,int cols){
		Component parent=hostComponent();
		if(false)return showInputDialog(parent,rubric,proposal);
	  inputPane.setMessageType(QUESTION_MESSAGE);
	  inputPane.setOptionType(OK_CANCEL_OPTION);
	  inputPane.setMessage(rubric);
	  inputPane.setWantsInput(false);
	  inputPane.setWantsInput(true);
		inputPane.setInitialSelectionValue(proposal);
		inputPane.setInputValue(UNINITIALIZED_VALUE);
	  JTextField field=null;
	  for(Component c:KitSwing.allComponents(inputPane))
	  	if(c instanceof JTextField)field=(JTextField)c;
	  if(field==null)throw new IllegalStateException(
	  		"Null field in "+Debug.info(this));
	  else if(!field.getDragEnabled()){
	  	new TextSmarts(field);
	  	if(cols>0)field.setColumns(cols);
	  }
	  inputPane.displayInDialog(parent,kit.getDecorationText(title,true));
	  String input=(String)inputPane.getInputValue();
		return input==UNINITIALIZED_VALUE?null:input.trim();
	}
	public void htmlPane(String title,String path){
		htmlPane.updateMessage(path,fontFactor()*kit.layoutFactor());
		htmlPane.displayInDialog(hostComponent(),kit.getDecorationText(title,true));
	}
	public void textPane(String title,String rubric,int rows,int cols){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	public File confirmGetFile(String title,String path){
		return confirmYesNo(title,kit.decoration(title).rubric+path+"?")==Yes?
			 new File(path):null;
	}
	private Response getChoice(String title,String rubric,int messageType,
			int optionType,boolean zeroIsYes){
		if(title==null||title.trim().equals("")||rubric==null||rubric.trim().equals(""))
			throw new IllegalArgumentException("Null or empty title or rubric in "+Debug.info(this));
		if(rubric.equals(title))rubric=kit.decoration(title).rubric;
		String appTitle=app.title();
		rubric=rubric.replaceAll(FIND_APP_TITLE,appTitle);
		title=kit.getDecorationText(title,true).replaceAll(FIND_APP_TITLE,appTitle);
		return getChoiceSwing(title,messageType,optionType,zeroIsYes,"<html>"+rubric,
				hostComponent(),choicePane);
	}
	public Response warningYesNo(String title,String rubric){
	  return getChoice(title,rubric,WARNING_MESSAGE,YES_NO_OPTION,true);
	}
	public Response warningYesNoCancel(String title,String rubric){
	  return getChoice(title,rubric,WARNING_MESSAGE,YES_NO_CANCEL_OPTION,true);
	}
	public Response confirmOKCancel(String title,String rubric){
	  return getChoice(title,rubric,QUESTION_MESSAGE,OK_CANCEL_OPTION,false);
	}
	public Response confirmYesNo(String title,String rubric){
	  return getChoice(title,rubric,QUESTION_MESSAGE,YES_NO_OPTION,true);
	}
	public void infoMessage(MessageTexts texts){
		getChoice(texts.title,!texts.rubricsRaw.contains("\n")?texts.rubricsRaw
					:texts.rubricsRaw.replaceAll("\n","<br>"),
				INFORMATION_MESSAGE,DEFAULT_OPTION,false);
	}
	public void errorMessage(String title,String rubric){
		getChoice(title,rubric,ERROR_MESSAGE,DEFAULT_OPTION,false);
	}
	public File openFile(FileSpecifier[]filters){
		return fileChooser().getFile(null,filters);
	}
	public File saveFile(File proposed,FileSpecifier[]filters){
		return fileChooser().getFile(proposed,filters);
	}
	Component hostComponent(){
		return (Component)app.host().wrapped();
	}
	private FileChooser fileChooser(){
		return fileChooser==null?new FileChooser((ActionAppSurface)app):fileChooser;
	}
	static Response warningExceptionSwing(MessageTexts tt,Exception e,Component parent,
			boolean inOpen){
		String htmlTag=false?"":"<html>";
		Object content;
		if(tt.rubricBody.trim().equals(""))content=(htmlTag+tt.rubricTop+tt.rubricTail);
		else{
			JTextArea text=new JTextArea();
			text.setEditable(false);
			text.setFont(new Font("SansSerif",0,11).deriveFont(new JMenuBar().getFont().getSize2D()));
			text.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
			text.setRows(10);
			text.setColumns(50);
			text.setText(tt.rubricBody+
					"Stack trace:\n"+Strings.linesString(Debug.stackTraceLines(e))+
					"\nThreads:\n"+Objects.toString(Debug.getSortedThreads(),"\n"));
			text.setCaretPosition(0);
			JPanel messagePane=new JPanel(new BorderLayout(0,15)){
				public Insets getInsets(){
					return new Insets(5,0,5,40);
				}
			};
			messagePane.add(new JLabel(htmlTag+tt.rubricTop),BorderLayout.NORTH);
			messagePane.add(new JScrollPane(text,VERTICAL_SCROLLBAR_AS_NEEDED,
					HORIZONTAL_SCROLLBAR_AS_NEEDED),BorderLayout.CENTER);
			messagePane.add(new JLabel(htmlTag+tt.rubricTail),BorderLayout.SOUTH);
			KitSwing.adjustComponents(false,messagePane);
			content=messagePane;
		}
		Response response=getChoiceSwing(tt.title,ERROR_MESSAGE,
				inOpen?DEFAULT_OPTION:YES_NO_OPTION,true,content,parent,exceptionPane);
		exceptionPane.setMessage(null);
		return response;
	}
	public Response warningException(MessageTexts t,Exception e,boolean inOpen){
		return warningExceptionSwing(t,e,hostComponent(),inOpen);
	}
}
