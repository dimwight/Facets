package applicable.eval.form;
import facets.util.tree.TypedNode;
import facets.util.tree.TypedNode;
import applicable.eval.EvalContext;
import applicable.treecode.TreeCodeContext;
import applicable.treecode.TreeCodeType;
public final class NumberInput extends InputField{
	final public static TreeCodeType<NumberInput>type=new TreeCodeType(
			NumberInput.class.getSimpleName()){
		@Override
		public NumberInput newCoded(TypedNode code,TreeCodeContext context){
			return new NumberInput(code,(EvalContext)context);
		}
	};
	private NumberInput(TypedNode source,EvalContext context){
		super(source,type,context);
	}
	@Override
	public void updateValue(String text){
		try {
			super.updateValue(Integer.valueOf(text).toString());
		} catch (Exception e) {
			System.out.println(e);
		};
	}
}
