package applicable.eval.form;
import facets.util.Debug;
import facets.util.IndexingIterator;
import facets.util.ItemList;
import facets.util.Objects;
import facets.util.tree.TypedNode;
import facets.util.tree.ValueNode;
import java.text.Format;
import java.util.HashMap;
import java.util.Map;
import applicable.eval.EvalContext;
import applicable.eval.Value;
import applicable.field.BooleanField;
import applicable.field.OptionField;
import applicable.field.ValueField;
import applicable.treecode.TreeCodeContext;
import applicable.treecode.TreeCodeType;
import applicable.treecode.TreeCoded;
public final class TickInput extends InputField{
	final public static TreeCodeType<TickInput>type=new TreeCodeType(
			TickInput.class.getSimpleName()){
		@Override
		public TickInput newCoded(TypedNode code,TreeCodeContext context){
			return new TickInput(code,(EvalContext)context);
		};
	};
	private TickInput(TypedNode source,EvalContext context){
		super(source,type,context);
	}
	@Override
	protected Value[]doEvaluation(){
		ItemList<Value>values=newValues();
		for(TreeCoded c:codeds)
			if(c instanceof Value)values.add((Value)c);
		return values.items();
	}
	@Override
	public ValueField[]newFormFields(final String keyTitle,final boolean showCodes){
		final Map<String,String>codes=new HashMap(),texts=new HashMap();
		final String[]items=Objects.toString(inputs()
				).replaceAll("Value \\.\\.\\.=",""
				).replaceAll("#[^,]+,?","").split(",");
		final ItemList<ValueField>fields=new ItemList(ValueField.class);
		new IndexingIterator<String>(items){
		protected void itemIterated(String option,int at){
			String splits[]=option.split(" ",2),code=splits[0],
					text=splits[splits.length>1&&!showCodes?1:0];
				texts.put(code,text);
				codes.put(text,code);
				items[at]=text;
				fields.add(new BooleanField(keyTitle+text) {
					public void putInputValue(ValueNode values,String text){
						values.put(valueKey(),Boolean.valueOf(text));
					};
				});
		}}.iterate();
		return fields.items();
	}
	@Override
	public void updateValue(String text){
		String pair[]=text.split("="),key=pair[0],value=pair[1].toUpperCase();
		Value[]inputs=inputs();
		for(int i=0;i<inputs.length;i++){
			if(key.equals(inputs[i].asText()))
				codeds[i].source.setValues(new String[]{value});
		}
	}
}
