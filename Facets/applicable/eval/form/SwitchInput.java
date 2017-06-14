package applicable.eval.form;
import facets.util.IndexingIterator;
import facets.util.Objects;
import facets.util.tree.TypedNode;
import facets.util.tree.ValueNode;
import java.util.HashMap;
import java.util.Map;
import applicable.eval.EvalContext;
import applicable.field.OptionField;
import applicable.field.ValueField;
import applicable.treecode.TreeCodeContext;
import applicable.treecode.TreeCodeType;
public final class SwitchInput extends InputField{
	final public static TreeCodeType<SwitchInput>type=new TreeCodeType(
			SwitchInput.class.getSimpleName()){
		@Override
		public SwitchInput newCoded(TypedNode code,TreeCodeContext context){
			return new SwitchInput(code,(EvalContext)context);
		};
	};
	private SwitchInput(TypedNode source,EvalContext context){
		super(source,type,context);
	}
	@Override
	public ValueField[]newFormFields(String keyTitle,final boolean showCodes){
		final Map<String,String>codes=new HashMap(),texts=new HashMap();
		final String[]options=Objects.toString(inputs()
				).replaceAll("Value \\.\\.\\.=",""
				).replaceAll("#[^,]+,?","").split(",");
		new IndexingIterator<String>(options){
		protected void itemIterated(String option,int at){
			String splits[]=option.split(" ",2),
					code=splits[0],text=splits[splits.length>1&&!showCodes?1:0];
				texts.put(code,text);
				codes.put(text,code);
				options[at]=text;
		}}.iterate();
		return new ValueField[]{new OptionField(keyTitle,Objects.toString(options)){
			@Override
			protected String getValue(ValueNode values,String valueKey){
				return texts.get(values.get(valueKey));
			}
			@Override
			protected String parseInputText(String text){
				return codes.get(text);
			}
		}};
	}
}
