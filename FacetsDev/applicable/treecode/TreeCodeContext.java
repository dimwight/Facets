package applicable.treecode;
import facets.util.Tracer;
import facets.util.tree.TypedNode;
import applicable.treecode.TreeCoded.Label;
/**
Host for {@link TreeCoded}s that provides for referencing of {@link TreeCoded}s by {@link TreeCoded.Label}.
 */
public abstract class TreeCodeContext<T extends TreeCoded>extends Tracer{
	public final TypedNode source;
	protected TreeCodeContext(TypedNode source){
		this.source=source;
	}
	public abstract T getLabelled(Label label);
}
