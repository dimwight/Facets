package applicable.field;
import static applicable.field.DateField.*;
import static facets.util.HtmlFormBuilder.FormTag.*;
import static java.lang.Math.*;
import facets.util.HtmlFormBuilder;
import facets.util.Util;
import facets.util.HtmlBuilder.RenderTarget;
import facets.util.tree.ValueNode;
import java.awt.Font;
import java.awt.FontMetrics;
import java.text.Format;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.swing.JLabel;
import applicable.field.TextField.LongTextField;
public class FieldFormBuilder extends HtmlFormBuilder{
	public static final int TEXT_AREA_MULTIPLE_MAX=3;
	protected final ValueNode values;
	final FieldSet fields;
	public FieldFormBuilder(RenderTarget rt,FieldSet fields,ValueNode values,
			String...nameRows){
		super(rt,nameRows);
		this.fields=fields;
		this.values=values;
	}
	@Override
	protected Object getValue(String name){
		return fields.getNamed(name).newValue(values);
	}
	@Override
	public void readEdit(FormInput edit){
		fields.getNamed(edit.name).putInputValue(values,edit.value);
	}
	@Override
	protected int inputCols(String name){
		return fields.getNamed(name).inputCols();
	}
	@Override
	protected boolean isTextArea(String name){
		return fields.getNamed(name)instanceof LongTextField;
	}
	@Override
	final protected String newDisplayText(String name,Object value){
		Format format=fields.getNamed(name).format();
		return value.equals(nullDate)?"":format!=null?format.format(value)
			:super.newDisplayText(name,value);
	}
	@Override
	protected boolean isNullField(String name,Object value){
		return super.isNullField(name,value)||value.equals(nullDate);
	}
	private final static FontMetrics widthMetrics=new JLabel().getFontMetrics(
			new Font("SansSerif",0,10));
	protected int estimateTextCols(String text){
		int length=text.length(),cols=widthMetrics.stringWidth(text)*7/64;
		if(false)Util.printOut("FieldFormBuilder.estimateTextCols length="+length+" cols="+cols);
		return cols;
	}
	@Override
	final protected String newFieldCode(String name,Object value){
		ValueField field=fields.getNamed(name);
		String text=value.toString().trim();
		if(field instanceof LongTextField){
			LongTextField ltf=(LongTextField)field;
			int rows=ltf.inputRows(),cols=ltf.inputCols(),
				textCols=estimateTextCols(text);
			String code=textCols<cols?newInputText(name,max(1,cols),text)
				:newTextArea(name,cols,rows==1?rows:
					min(max(textCols/cols,rows),rows*TEXT_AREA_MULTIPLE_MAX),text);
			if(false)Util.printOut("FieldFormBuilder.newFieldCode: code=",code);
			return code;
		}
		if(field instanceof DateField){
			String formatted=value.equals(nullDate)?"":field.format().format(value);
			return newInputText(name,formatted.equals("")?1:5,formatted);
		}
		return field instanceof OptionField?newSelect(name,((OptionField)field).options,text)
			:field instanceof BooleanField?newInputCheckbox(name,text)
			:super.newFieldCode(name,value);
	};
}
