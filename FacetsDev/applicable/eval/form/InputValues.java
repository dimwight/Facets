package applicable.eval.form;
import facets.util.ItemList;
import facets.util.tree.TypedNode;
import applicable.eval.EvalCoded;
import applicable.eval.EvalContext;
import applicable.eval.Value;
import applicable.treecode.TreeCodeContext;
import applicable.treecode.TreeCodeType;
import applicable.treecode.TreeCoded;
final public class InputValues extends EvalCoded{
	final public static TreeCodeType<InputValues>type=new TreeCodeType(
			InputValues.class.getSimpleName()){
		@Override
		public EvalCoded newCoded(TypedNode code,TreeCodeContext context){
			return new InputValues(code,(EvalContext)context);
		};
	};
	private InputValues(TypedNode source,EvalContext context){
		super(source,type,context);
	}
	/**
	Assembles a {@link Value}[] from its contained
	<ul>
<li>{@link Value}s
<li>{@link EvalCoded}s
</ul>
	 */
	@Override
	protected Value[]doEvaluation(){
		ItemList<Value>values=newValueItems();
		for(TreeCoded eval:codeds)
			values.addItems(eval instanceof Value?asValues((Value)eval)
					:((EvalCoded)eval).evaluate());
		return values.items();
	}	
}
