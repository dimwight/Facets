package applicable.eval.form;
import facets.util.Debug;
import facets.util.tree.TypedNode;
import applicable.eval.EvalCoded;
import applicable.eval.EvalContext;
import applicable.treecode.TreeCodeType;
import applicable.treecode.TreeCoded.Label;;
/**
{@link EvalCoded} that can be referenced by its {@link Label}. 
 */
public abstract class EvalField extends EvalCoded{
	protected EvalField(TypedNode source,TreeCodeType type,EvalContext context){
		super(source,type,context);
		if(label.equals(Label.NONE))throw new IllegalStateException(
				Debug.info(this)+" not labelled");
	}
	public EvalField[]formFields(){
		return new EvalField[]{this};
	}
}
