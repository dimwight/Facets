package applicable.eval.form;
import facets.util.Debug;
import facets.util.tree.TypedNode;
import facets.util.tree.TypedNode;
import java.util.HashMap;
import java.util.Map;
import applicable.eval.EvalCoded;
import applicable.eval.EvalContext;
import applicable.eval.Value;
import applicable.treecode.TreeCodeContext;
import applicable.treecode.TreeCodeType;
import applicable.treecode.TreeCoded;
/**
Container for input {@link Value}s of a {@link EvalForm}. 
 */
public final class EvalRecord extends EvalCoded{
	final public static TreeCodeType<EvalRecord>type=new TreeCodeType(
			EvalRecord.class.getSimpleName()){
		@Override
		public EvalRecord newCoded(TypedNode source,TreeCodeContext context){
			return new EvalRecord(source,(EvalForm)context);
		};
	};
	final DateStamp stamp;
	private EvalRecord(TypedNode source,EvalForm form){
		super(source,type,form);
		DateStamp formStamp=false?null:DateStamp.newNow();
		for(TreeCoded coded:codeds){
			if(coded instanceof DateStamp){
				formStamp=(DateStamp)coded;
				continue;
			}
		}
		if(formStamp==null)throw new IllegalStateException(
				"Null formStamp in "+Debug.info(this));
		else this.stamp=formStamp;
	}
	private void updateValues(EvalCoded from,EvalCoded to){
		TreeCoded[]fromCodeds=from.codeds,toCodeds=to.codeds;
		for(int i=0;i<toCodeds.length&&i<fromCodeds.length
				&&toCodeds[i]instanceof Value&&fromCodeds[i]instanceof Value;i++)
			((Value)toCodeds[i]).updateSource((Value)fromCodeds[i]);
	}
	void updateToFields(Map<Label,EvalCoded>fields){
		for(TreeCoded c:codeds)updateValues((EvalCoded)c,fields.get(c.label));
	}
	void updateFromFields(Map<Label,EvalCoded>fields){
		for(TreeCoded c:codeds)updateValues(fields.get(c.label),(EvalCoded)c);
	}
	@Override
	protected Value[]doEvaluation(){
		throw new RuntimeException("Not implemented in "+this);
	}
}
