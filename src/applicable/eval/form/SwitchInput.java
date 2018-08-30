package applicable.eval.form;
import facets.util.IndexingIterator;
import facets.util.Objects;
import facets.util.tree.TypedNode;
import facets.util.tree.ValueNode;
import java.util.HashMap;
import java.util.Map;
import applicable.eval.EvalContext;
import applicable.eval.Value;
import applicable.field.OptionField;
import applicable.field.ValueField;
import applicable.treecode.TreeCodeContext;
import applicable.treecode.TreeCodeType;
public class SwitchInput extends InputField{
	final public static TreeCodeType<SwitchInput>type=new TreeCodeType(
			SwitchInput.class.getSimpleName()){
		@Override
		public SwitchInput newCoded(TypedNode code,TreeCodeContext context){
			return new SwitchInput(code,(EvalContext)context);
		};
	};
	protected SwitchInput(TypedNode source,EvalContext context){
		super(source,type,context);
	}
}
