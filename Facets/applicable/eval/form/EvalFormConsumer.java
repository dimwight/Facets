package applicable.eval.form;
import facets.util.tree.TypedNode;
import applicable.eval.EvalCoded;
import applicable.eval.EvalContext;
import applicable.treecode.TreeCodeType;
abstract public class EvalFormConsumer extends EvalCoded{
	@Override
	protected void traceOutput(String msg){
		System.out.println(msg);
	}
	protected EvalFormConsumer(TypedNode source,TreeCodeType type,EvalContext context){
		super(source,type,context);
	}
	public abstract void setForm(EvalForm form);
}