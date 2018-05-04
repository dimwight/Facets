package applicable.eval;
import static facets.util.tree.TypedNode.*;
import facets.util.tree.DataNode;
import facets.util.tree.TypedNode;
import facets.util.tree.TypedNode;
import applicable.treecode.TreeCodeContext;
import applicable.treecode.TreeCodeType;
import applicable.treecode.TreeCoded;
/**
{@link TreeCoded} that can represent a data primitive.
<p>Data is stored textually as the {@link TypedNode#title()} of the source. 
 */
public final class Value extends TreeCoded{
	final public static TreeCodeType<Value>type=new TreeCodeType(
			Value.class.getSimpleName()){
		@Override
		public Value newCoded(TypedNode code,TreeCodeContext context){
			return new Value(code);
		};
	};
	/** Constant */
	public static final Value FALSE=newValue("FALSE"),
			TRUE=newValue("TRUE"),NONE=newValue("NONE");
	public static Value newValue(String data){
		return new Value(new DataNode(type.name,data));
	}
	private Value(TypedNode source){
		super(source,type);
	}
	public String asText(){
		return source.title();
	}
	public String asCode(){
		return asText().replaceAll(" .*","");
	}
	public Value copyValue(){
		return new Value((DataNode)source.copyState());
	}
	@Override
	public String toString(){
		return type+"="+asCode();
	}
	@Override
	public boolean equals(Object that){
		return toString().equals(((Value)that).toString());
	}
	@Override
	public int hashCode(){
		return toString().hashCode();
	}
}
