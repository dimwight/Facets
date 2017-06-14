package applicable.field;
import facets.util.Debug;
import facets.util.ValueProxy;
import facets.util.tree.ValueNode;
public class FieldProxy extends ValueProxy{
	public static final String FIELD_SEPARATOR="\t";
	public final ValueField[]fields;
	public FieldProxy(ValueNode source,ValueField[]fields){
		super(source);
		this.fields=fields;
	}
	public String newFieldTexts(){
		final StringBuilder texts=new StringBuilder();
		for(ValueField field:fields)texts.append(newFieldText(field));
		return texts.toString();
	}
	protected Object newFieldText(ValueField field){
		return getFieldValue(field)+FIELD_SEPARATOR;
	}
	final protected Object getFieldValue(ValueField field){
		for(int at=0;at<fields.length;at++)
			if(fields[at].equals(field))return get(at);
		throw new IllegalStateException("No field="+field);
	}
	protected ValueNode getFieldValues(){
		return(ValueNode)source;
	}
	@Override
	final protected Object[]lazyValues(){
		Object[]values=new Object[fields.length];
		ValueNode node=getFieldValues();
		for(int v=0;v<values.length;v++)values[v]=fields[v].newValue(node);
		return values;
	}
}