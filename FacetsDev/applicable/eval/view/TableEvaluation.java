package applicable.eval.view;
import static facets.util.Objects.*;
import static facets.util.tree.Nodes.*;
import facets.util.Tracer;
import facets.util.tree.TypedNode;
import facets.util.tree.ValueNode;
import applicable.eval.EvalContext;
import applicable.eval.form.EvalForm;
import applicable.eval.form.EvalFormConsumer;
import applicable.treecode.TreeCodeContext;
import applicable.treecode.TreeCodeType;
public final class TableEvaluation extends EvalFormConsumer{
	final public static TreeCodeType<TableEvaluation>type=new TreeCodeType(
			TableEvaluation.class.getSimpleName()){
		@Override
		public TableEvaluation newCoded(TypedNode code,TreeCodeContext context){
			return new TableEvaluation(code,(EvalContext)context);
		};
	};
	private TableEvaluation(TypedNode source,EvalContext context){
		super(source,type,context);
	}
	private static final String NO_PICK="NONE";
	private final class RowWrap extends Tracer{
		private final String label,value,desc;
		RowWrap(String[]values){
			desc=values[1];
			label=values[2];
			value=values[3];
		}
		@Override
		public String toString(){
			return label+" "+value;
		}
		String pickValue(EvalForm form){
			return form.getLabelled(new Label(label)).evaluate()[0].asText().equals(value)?value:NO_PICK;
		}
	}
	@Override
	public void setForm(EvalForm form){
		for(ValueNode row:newTyped(ValueNode.class,children(
				child(source,TableRows.type.name),TableRow.type.name)))if(false){
				RowWrap wrap=new RowWrap(row.values());
				String pickValue=wrap.pickValue(form);
				if(!pickValue.equals(NO_PICK))trace(".setForm: picked ",wrap);
			}
			else{
				String values[]=row.values(),id=values[0],desc=values[1],label=values[2],value=values[3],
						pickValue=form.getLabelled(new Label(label)
								).evaluate()[0].asText().equals(value)?value:NO_PICK;;
				if(!pickValue.equals(NO_PICK))trace("",value+"="+id+": "+desc);
			}
	}
}
