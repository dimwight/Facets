package applicable.eval.form;
import facets.util.Debug;
import facets.util.tree.TypedNode;
import applicable.eval.EvalCoded;
import applicable.eval.EvalContext;
import applicable.field.TextField;
import applicable.field.ValueField;
import applicable.eval.EvalCoded;
import applicable.treecode.TreeCodeType;
import applicable.treecode.TreeCoded;
import applicable.treecode.TreeCoded.Label;
/**
{@link EvalCoded} that can be referenced by its {@link applicable.treecode.TreeCoded.Label}. 
 */
public abstract class EvalField extends EvalCoded{
	protected EvalField(TypedNode source,TreeCodeType type,EvalContext context){
		super(source,type,context);
		if(false&&label.equals(Label.NONE))throw new IllegalStateException(
				Debug.info(this)+" not labelled");
	}
	public ValueField[]newFormFields(String keyTitle,boolean showCodes){
		return new ValueField[]{new TextField(keyTitle){
			@Override
			public int inputCols(){
				return 10;
			}
		}};
	}
}
