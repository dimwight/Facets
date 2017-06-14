package applicable.eval;
import facets.util.Debug;
import facets.util.tree.TypedNode;
import facets.util.tree.TypedNode;
import applicable.eval.form.InputField;
import applicable.eval.form.TickInput;
import applicable.treecode.TreeCodeContext;
import applicable.treecode.TreeCodeType;
import applicable.treecode.TreeCoded;
public final class ValueMatching extends EvalCoded{
	final public static TreeCodeType<ValueMatching>type=new TreeCodeType(
			ValueMatching.class.getSimpleName()){
		@Override
		public ValueMatching newCoded(TypedNode code,TreeCodeContext context){
			return new ValueMatching(code,(EvalContext)context);
		};
	};
	private ValueMatching(TypedNode source,EvalContext context){
		super(source,type,context);
	}
	@Override
	public Value[]doEvaluation(){
		InputField field=(InputField)context.getLabelled(label);
		if(false)trace(".doEvaluation: codeds=",codeds);
		if(field instanceof TickInput){
			Values values=(Values)codeds[0];
			Value[]inputs=field.inputs();
			for(int i=0;i<inputs.length;i++)
				if(inputs[i].asText().equals(values.label.text)){
					return asValues((Value)values.codeds[field.evaluate()[i].asText(
							).equalsIgnoreCase(Value.TRUE.asText())?1:0]);
				}
			throw new RuntimeException("Not implemented in "+this);
		}
		int fieldAt=field.valueIndex();
		if(codeds.length==1){
			TreeCoded single=codeds[0];
			if(single instanceof Values)
				return asValues(((Values)single).evaluate()[fieldAt]);
			else if(single instanceof ValueMatching)
				return asValues(((ValueMatching)single).evaluate()[fieldAt]);
			else throw new RuntimeException("Not implemented for "+this);
		}else{
			TreeCoded coded=codeds[fieldAt];
			return coded  instanceof Value?asValues((Value)coded)
					:((EvalCoded)coded).evaluate();
		}
	}
}
