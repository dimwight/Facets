package applicable.eval;
import facets.util.tree.TypedNode;
import java.util.ArrayList;
import java.util.List;
import applicable.treecode.TreeCodeContext;
import applicable.treecode.TreeCodeType;
import applicable.treecode.TreeCoded;
import applicable.treecode.TreeCoded.Label;
/**
{@link TreeCodeContext} that promises to return {@link EvalCoded}s. 
 */
public abstract class EvalContext<T extends EvalCoded>extends TreeCodeContext<EvalCoded>{
	private final EvalTypes types;
	protected EvalContext(TypedNode source,EvalTypes types){
		super(source);
		if((this.types=types)==null)throw new IllegalArgumentException(
				"Null types in "+this);
	}
	@Override
	public abstract EvalCoded getLabelled(Label label);
	final public TreeCoded[]newAliasedCodeds(TypedNode[]sources){
		List<TreeCoded>codeds=new ArrayList();
		for(TypedNode source:sources){
			TreeCodeType type=typeForCode(source);
			if(type==null)throw new IllegalStateException("Missing type for "+source);
			else codeds.add(type.newCoded(source,this));
		}
		return codeds.toArray(new TreeCoded[]{});
	}
	final public TreeCodeType typeForCode(TypedNode code){
		TreeCodeType type=types.aliasTypes.get(code.type());
		if(type==null)throw new IllegalStateException("Missing type for "+code);
		else return type;
	}
	final public String codeForType(TreeCodeType type){
		String code=types.typeAliases.get(type);
		if(code==null)throw new IllegalStateException("Missing code for "+type);
		else return code;
	}
}
