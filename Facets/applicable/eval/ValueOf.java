package applicable.eval;
import static facets.util.tree.TypedNode.*;
import facets.util.tree.TypedNode;
import facets.util.tree.TypedNode;
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
	public Value[]doEvaluation(){
		if(!label.text.equals(UNTITLED))
			return context.getLabelled(label).evaluate();
		StringBuffer text=new StringBuffer();
		for(TreeCoded c:codeds){
			Value value=c instanceof Value?(Value)c:
					((EvalCoded)c).evaluate()[0];
			text.append(value.asText());
		}
		return asValues(Value.newContentValue(text.toString()));
	}
}
