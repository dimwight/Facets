package applicable.eval;
import static applicable.eval.Value.*;
import static facets.util.Objects.*;
import facets.util.Debug;
import facets.util.ItemList;
import facets.util.tree.TypedNode;
import applicable.treecode.TreeCodeType;
import applicable.treecode.TreeCoded;
/**
{@link EvalCoded} that creates {@link TreeCoded}s from any children of its source
using a supplied {@link EvalTypes}.
<p>The {@link EvalCoded} and all {@link EvalCoded} contents share 
the {@link EvalContext} passed to the constructor.  
 */
public abstract class EvalCoded extends TreeCoded{
	public final TreeCoded[]codeds;
	protected final EvalContext context;
	protected EvalCoded(TypedNode source,TreeCodeType type,EvalContext context){
		super(source,type);
		if(context==null)throw new IllegalArgumentException(
				"Null context in "+Debug.info(this));
		else codeds=(this.context=context).newAliasedCodeds(source.children());
		if(false)trace(".:\n",codeds);
	}
	public final Value[]evaluate(){
		if(false)trace(".evaluate:\n",codeds);
		for(TreeCoded c:codeds)
			if(c instanceof IfValue&&((EvalCoded)c).evaluate()[0].equals(FALSE))
				return asValues(FALSE);
		Value[]values=doEvaluation();
		if(false)trace(".~evaluate:\n",values);
		return values;
	}
	protected Value[]doEvaluation(){
		return asValues(FALSE);
	}
	final protected Value[]asValues(Value eval){
		return new Value[]{eval};
	}
	final protected ItemList<Value>newValues(){
		return new ItemList(Value.class);
	}
	final protected Value getSingleFieldValue(Label label){
		return context.getLabelled(label).evaluate()[0];
	}
	public boolean evaluatesFalse(){
		return evaluate()[0].asText().equals(Value.FALSE.asText());
	}
	final public boolean isIgnorable(){
		return label.isRem();
	}
}
