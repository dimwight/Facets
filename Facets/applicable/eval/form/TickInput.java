package applicable.eval.form;
import facets.util.Debug;
import facets.util.IndexingIterator;
import facets.util.ItemList;
import facets.util.Objects;
import facets.util.tree.DataNode;
import facets.util.tree.TypedNode;
import facets.util.tree.ValueNode;
import java.text.Format;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import applicable.eval.EvalContext;
import applicable.eval.Value;
import applicable.eval.Values;
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
	public final class CheckInput extends InputField{
		CheckInput(TypedNode source,EvalContext context){
			super(source,type,context);
		}
		@Override
		protected Value[]doEvaluation(){
			for(Value value:TickInput.this.evaluate())
				if(value.equals(Value.newValue(title())))return asValues(Value.TRUE);
			return asValues(Value.FALSE);
		}
		@Override
		public void updateValue(String text){
			Boolean checked=Boolean.valueOf(text);
			Set<Value>values=new HashSet(Arrays.asList(TickInput.this.evaluate()));
			for(Value input:TickInput.this.inputs())
				if(input.asText().equals(this.label.text)){
					values.remove(input);
					if(checked)values.add(Value.newValue(input.asCode()));
					TickInput.this.updateValues(values);
				}
		}
	}	
	private void updateValues(Set<Value>values){
		values.remove(Value.NONE);
		if(values.isEmpty())values.add(Value.NONE.copyValue());
		codeds[0]=((Values)codeds[0]).updated(values);
	}
	private TickInput(TypedNode source,EvalContext context){
		super(source,type,context);
		debug=false&&label.text.equals("Alternative 240");
	}
	@Override
	public TypedNode newRecordCode(){
		TypedNode code=super.newRecordCode();
		code.setChildren(((TypedNode)codeds[0].source.copyState()).children());
		return code;
	}
	@Override
	public void updateValueState(TypedNode c){
		codeds[0].source.setChildren(((TypedNode)c.copyState()).children());
		codeds[0]=Values.type.newCoded(codeds[0].source,context);
	}
	@Override
	public EvalField[]formFields(){
		Value[]inputs=inputs();
		final EvalField[]formFields=new EvalField[inputs.length];
		for(int i=0;i<formFields.length;i++)
			formFields[i]=new CheckInput(inputs[i].source,context);
		return formFields;
	}
	@Override
	protected Value[]doEvaluation(){
		return ((Values)codeds[0]).evaluate();
	}
	@Override
	public void updateValue(String text){
		throw new RuntimeException("Not implemented in "+this);
	}
}
