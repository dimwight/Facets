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
	}
	@Override
	public Value[]doEvaluation(){
		Value[]asFalse=new Value[]{FALSE.labelled(label)},
				asTrue=new Value[]{TRUE.labelled(label)};
		if(false&&codeds.length==0)return asTrue;
		Value check=getSingleFieldValue(label);
		for(TreeCoded eval:codeds)
				if(eval.equals(check))return asTrue;
		if(false)trace(".doEvaluation: value=",check);
		return asFalse;
	}
}
