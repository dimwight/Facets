package applicable.eval;
import static facets.util.tree.TypedNode.*;
import facets.util.tree.TypedNode;
import facets.util.tree.TypedNode;
import applicable.eval.form.EvalField;
import applicable.eval.form.TickInput;
import applicable.treecode.TreeCodeContext;
import applicable.treecode.TreeCodeType;
import applicable.treecode.TreeCoded;
public final class ValueOf extends EvalCoded{
	final public static TreeCodeType<ValueOf>type=new TreeCodeType(
			ValueOf.class.getSimpleName()){
		@Override
		public ValueOf newCoded(TypedNode code,TreeCodeContext context){
			return new ValueOf(code,(EvalContext)context);
		};
	};
	private ValueOf(TypedNode source,EvalContext context){
		super(source,type,context);
	}
	@Override
	protected Value[]doEvaluation(){
		if(!label.text.equals(UNTITLED))
			return ((EvalField)context.getLabelled(label)).evaluate();
		StringBuilder text=new StringBuilder();
		for(TreeCoded c:codeds){
			Value value=c instanceof Value?(Value)c:
					((EvalCoded)c).evaluate()[0];
			String asText=value.asText();
			if(!asText.equals(UNTITLED))text.append(asText);
		}
		return asValues(Value.newValue(text.toString()));
	}
}
