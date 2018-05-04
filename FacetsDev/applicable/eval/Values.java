package applicable.eval;
import facets.util.ItemList;
import facets.util.tree.TypedNode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import applicable.treecode.TreeCodeContext;
import applicable.treecode.TreeCodeType;
import applicable.treecode.TreeCoded;
/**
{@link EvalCoded} that evaluates to a {@link Value}[]. 
 */
/**
. 
<p>{@link Values}
 */
final public class Values extends EvalCoded{
	final public static TreeCodeType<Values>type=new TreeCodeType(
			Values.class.getSimpleName()){
		@Override
		public EvalCoded newCoded(TypedNode code,TreeCodeContext context){
			return new Values(code,(EvalContext)context);
		};
	};
	private Values(TypedNode source,EvalContext context){
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
		for(TreeCoded coded:codeds)
			if(coded instanceof Value)values.add((Value)coded);
			else throw new RuntimeException(
						"Not implemented for " +coded+" in "+this);
		return values.items();
	}
	public Values updated(Collection<Value>values){
		List<TypedNode>update=new ArrayList();
		for(Value v:values)update.add(v.copyValue().source);
		source.setChildren(update.toArray(new TypedNode[]{}));
		return new Values(source,context);
	}	
}
