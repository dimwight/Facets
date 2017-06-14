package applicable.eval;
import static applicable.eval.Value.*;
import facets.util.ItemList;
import facets.util.tree.TypedNode;
import facets.util.tree.TypedNode;
import java.util.ArrayList;
import java.util.List;
import applicable.treecode.TreeCodeContext;
import applicable.treecode.TreeCodeType;
import applicable.treecode.TreeCoded;
/**
{@link EvalCoded} that just evaluates to its contained {@link Value}s. 
 */
final public class Values extends EvalCoded{
	final public static TreeCodeType<Values>type=new TreeCodeType(
			Values.class.getSimpleName()){
		@Override
		public EvalCoded newCoded(TypedNode code,TreeCodeContext context){
			return new Values(code,(EvalContext)context);
		};
	};
	protected Values(TypedNode source,EvalContext context){
		super(source,type,context);
		boolean singles=false;
	}
	@Override
	public Value[]doEvaluation(){
		ItemList<Value>values=newValues();
		for(TreeCoded eval:codeds)
			if(eval instanceof Values)throw new RuntimeException(
					"Not implemented for " +Values.type.name+" in "+this);
			else if(eval instanceof Value)values.add((Value)eval);
			else if(eval instanceof EvalCoded)
				values.addItems(((EvalCoded)eval).evaluate());
		return values.items();
	}	
}
