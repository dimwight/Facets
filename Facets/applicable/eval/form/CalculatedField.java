package applicable.eval.form;
import facets.util.ItemList;
import facets.util.tree.TypedNode;
import facets.util.tree.TypedNode;
import applicable.eval.EvalContext;
import applicable.eval.EvalCoded;
import applicable.eval.IfValue;
import applicable.eval.Value;
import applicable.treecode.TreeCodeContext;
import applicable.treecode.TreeCodeType;
import applicable.treecode.TreeCoded;
public final class CalculatedField extends EvalField{
	final public static TreeCodeType<CalculatedField>type=new TreeCodeType(
			CalculatedField.class.getSimpleName()){
		@Override
		public CalculatedField newCoded(TypedNode code,TreeCodeContext context){
			return new CalculatedField(code,(EvalContext)context);
		};
	};
	protected CalculatedField(TypedNode source,EvalContext context){
		super(source,type,context);
	}
	@Override
	public Value[]doEvaluation(){
		ItemList<Value>values=newValues();
		for(TreeCoded eval:codeds)
			if(eval instanceof IfValue)continue;
			else values.addItems(eval instanceof Value?asValues((Value)eval)
					:((EvalCoded)eval).evaluate());
		return values.items();
	}
}
