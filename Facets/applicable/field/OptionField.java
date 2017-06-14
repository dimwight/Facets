package applicable.field;
import facets.core.app.TableView;
import facets.util.Debug;
import facets.util.Objects;
import facets.util.tree.ValueNode;
public class OptionField extends ValueField<String>{
	public final String[]options;
	public OptionField(String name,String options){
		super(name);
		this.options=options.split(",");
	}
	@Override
	protected String textToValue(String text,ValueNode values){
		return text;
	}
	@Override
	protected String parseInputText(String text){
		return text;
	}
	@Override
	protected String newNullValue(ValueNode values){
		return " - ";
	}
}
