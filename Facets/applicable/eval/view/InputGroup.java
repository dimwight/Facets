package applicable.eval.view;
import facets.util.tree.TypedNode;
import applicable.eval.EvalContext;
import applicable.eval.IfValue;
import applicable.eval.Value;
import applicable.eval.form.InputField;
import applicable.treecode.TreeCodeContext;
import applicable.treecode.TreeCodeType;
import applicable.treecode.TreeCoded;
public final class InputGroup extends InputField{
	final public static TreeCodeType<InputGroup>type=new TreeCodeType(
			InputGroup.class.getSimpleName()){
		@Override
		public InputGroup newCoded(TypedNode code,TreeCodeContext context){
			return new InputGroup(code,(EvalContext)context);
		}
	};
	public InputGroup(TypedNode source,EvalContext context){
		super(source,type,context);
	}
	@Override
	protected Value[]doEvaluation(){
		for(TreeCoded c:codeds)
			if(c instanceof IfValue)return((IfValue)c).evaluate();
		return asValues(Value.TRUE);
	}
}
