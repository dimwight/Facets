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
		debug=false&&label.text.equals("Mobility combo");
	}
	@Override
	protected Value[]doEvaluation(){
		InputField field=(InputField)context.getLabelled(label);
		if(field instanceof TickInput){
			EvalCoded values=(EvalCoded)codeds[0];
			Value[]options=values.evaluate();
			for(Value v:field.evaluate())
				if(v.label.equals(values.label))
					return asValues(options[1]);
			return asValues(options[0]);
		}
		if(debug)trace(".doEvaluation: field=",field);
		int fieldAt=field.valueIndex();
		if(debug)trace(".doEvaluation: fieldAt=",fieldAt);
		if(codeds.length==1){
			TreeCoded single=codeds[0];
			if(single instanceof Values||single instanceof ValueMatching)
				return asValues(((EvalCoded)single).evaluate()[fieldAt]);
			else throw new RuntimeException("Not implemented for "+single);
		}else{
			TreeCoded coded=codeds[fieldAt];
			return coded instanceof Value?asValues((Value)coded)
					:((EvalCoded)coded).evaluate();
		}
	}
}
