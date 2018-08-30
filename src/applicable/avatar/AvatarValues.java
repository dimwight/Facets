package applicable.avatar;
import facets.util.Titled;
import facets.util.Tracer;
import facets.util.tree.ValueNode;
public abstract class AvatarValues extends Tracer implements Titled{
	protected final ValueNode data;
	protected AvatarValues(ValueNode data){
		this.data=data;
	}

}
