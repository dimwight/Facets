package facets.util;
import static facets.util.Regex.*;
import facets.core.app.TextView;
public abstract class HtmlFormBuilder extends HtmlBuilder{
	public static final class FormInput{
		public final String name,value;
		public FormInput(String name,String value){
			this.name=name;
			this.value=value;
		}
	}
	protected String newDisplayText(String name,Object value){
		String text=value.toString();
		return text.startsWith("<a href=")?text
				:Regex.replaceAll(text,"<","&lt;",">","&gt;");
	}
	public enum FormTag{InputText,InputCheckbox,TextArea,Select;
		public String findValue(String code,String name){
			boolean area=this==TextArea;
			String _nameToEnd="[^>]*name='"+name+"'[^>]*>",
				tag=find(code,area?"<textarea"+_nameToEnd+"[^<]*<"
					:((this==Select?"<select":"<input")+_nameToEnd));
			if(tag.equals(""))throw new IllegalStateException(
					"No "+this+" with name="+name);
			String value=Regex.replaceAll(tag,
					area?"<textarea[^>]+(>[^<]*<)":".*value=([^>]+).*","$1",
					"&lt;","<",
					"&gt;",">");
			int chars=value.length();
			return chars==1?"":value.substring(1,chars-1);
		}
		public static String newInputText(String name,int size,String value){
		  if(value==TextView.DEBUG_TEXT)size=30;
			boolean singles=value.indexOf("'")>-1,doubles=value.indexOf('"')>-1;
			if(singles&doubles){
				if(false)throw new IllegalArgumentException("Can't quote value="+value);
				else value=value.replace("\"","'");
			}
			String quote=singles?"\"":"'";
			return "\n<input type='text' name='"+name+"' size='"+(value.trim().equals("")?1:size)+
					"' value="+quote+
					Regex.replaceAll(value,"<","&lt;",">","&gt;")
					+quote+">\n";
		}
		public static String newTextArea(String name,int cols,int rows,String value){
		  if(value==TextView.DEBUG_TEXT)cols=30;
			return "\n<textarea wrap='virtual' rows='"+rows+"'" +
					" cols='"+(value.trim().equals("")?1:cols)+"' name='"+name+"'>"+value+"</textarea>\n";
		}
		public static String newInputCheckbox(String name,String value){
			return "\n<input type='checkbox' name='"+name+"' value='"+value+"'>\n";
		}
		public static String newSelect(String name,String[]options,String value){
			StringBuilder code=new StringBuilder("\n<select name='" +name+"' value='"+value+"'>\n"); 
			for(String option:options)
				code.append("<option>"+option+"</option>\n" );
			return code.toString()+"</select>\n" ;
		}
		public static FormTag getTag(String name,String inputType){
			return name.equals("textarea")?TextArea:name.equals("select")?Select
				:inputType.equals("checkbox")?InputCheckbox:InputText;
		}
	}
	public static boolean layoutChanged(String then,String now){
		String[]_values={"(value=)[^>]+","$1","(<textarea[^>]+)[^<]+","$1"};
		return !replaceAll(now,_values).equals(replaceAll(then,_values));
	}
	final private static boolean boxBorders=false;
	final private static int cellBorder=0;
	private final String[]nameRows;
	public HtmlFormBuilder(RenderTarget rt,String...nameRows){
		super(rt);
		this.nameRows=nameRows;
	}
	@Override
	public String newPageContent(){
		return buildForm();
	}
	public final String buildForm(){
		StringBuilder form=new StringBuilder();
		form.append("<table border=0 cellspacing=3 cellpadding=0>");
		for(String rowNames:nameRows){
			StringBuilder row=new StringBuilder("<tr><td><table><tr>\n");
			for(String name:rowNames.split(",")){
				Object value=getValue(name);
				if(value==null)throw new IllegalStateException(
						"Null value for name="+name);
				else row.append(isNullField(name,value)&&hideNullField(name,value)?""
					:"<td><table cellspacing=3 cellpadding=0 border="+cellBorder+">\n" +
							"<tr"+(false&&isTextArea(name)?" valign=top":"")+"><td border="+cellBorder+"><b>"+
			name.replace("<","&lt;")+
							"</b></td><td>"+
			(useInputField(name)?newFieldCode(name,value)
				:"\n<table class=\"box\" cellspacing=0 border="+(boxBorders?1:0)+
				" cellpadding="+(boxBorders&&!isTextArea(name)?5:0)+">\n" +
				"<tr><td>&nbsp;"+newDisplayText(name,value)+"&nbsp;</td></tr></table>\n")+
					"</td></tr>\n</table></td>");
			}
			row.append("</tr></table></td></tr>\n");
			form.append(row.toString());
		}
		form.append("</table>");
		return form.toString();
	}
	protected boolean isNullField(String name,Object value){
		return value.toString().trim().equals("");
	}
	protected abstract Object getValue(String name);
	protected boolean hideNullField(String name,Object value){
		return false;
	}
	protected boolean isTextArea(String name){
		return false;
	}
	protected boolean useInputField(String name){
		return false;
	}
	protected String newFieldCode(String name,Object value){
		String text=value.toString();
		return FormTag.newInputText(name,inputCols(name),text);
	}
	protected int inputCols(String name){
		return 10;
	}
	public void readEdit(FormInput edit){
		throw new RuntimeException("Not implemented for "+edit);
	}
}