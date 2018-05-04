package applicable.eval;
import static applicable.eval.Value.*;
import facets.util.tree.TypedNode;
import facets.util.tree.TypedNode;
import applicable.treecode.TreeCodeContext;
import applicable.treecode.TreeCodeType;
import applicable.treecode.TreeCoded;
public final class IfValue extends EvalCoded{
	final public static TreeCodeType<IfValue>type=new TreeCodeType(
			IfValue.class.getSimpleName()){
		@Override
		public EvalCoded newCoded(TypedNode code,TreeCodeContext context){
			return new IfValue(code,(EvalContext)context);
		};
	};
	private IfValue(TypedNode source,EvalContext context){
		super(source,type,context);
		debug=false&&label.text.equals("Alternative 240");
	}
	@Override
	protected Value[]doEvaluation(){
		Value[]asFalse=new Value[]{FALSE},
				asTrue=new Value[]{TRUE};
		if(false&&codeds.length==0)return asTrue;
		Value[]checks=context.getLabelled(label).evaluate();
		if(debug)trace(".doEvaluation: checks=",checks);
		for(TreeCoded c:codeds)
			for(Value check:checks)if(c.equals(check))return asTrue;
		if(debug)trace(".doEvaluation: not found checks=",checks);
		return asFalse;
	}
}
