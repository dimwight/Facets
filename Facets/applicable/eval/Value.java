package applicable.eval;
import static facets.util.tree.TypedNode.*;
import facets.util.tree.DataNode;
import facets.util.tree.TypedNode;
import facets.util.tree.TypedNode;
import applicable.treecode.TreeCodeContext;
import applicable.treecode.TreeCodeType;
import applicable.treecode.TreeCoded;
/**
{@link TreeCoded} that can represent simple data, including a set of {@link Value}s.
 */
public final class Value extends TreeCoded{
	private static final boolean titles=true;
	final public static TreeCodeType<Value>type=new TreeCodeType(
			Value.class.getSimpleName()){
		@Override
		public Value newCoded(TypedNode code,TreeCodeContext context){
			return new Value(code);
		};
	};
	public static final Value FALSE=newContentValue("FALSE"),
			TRUE=newContentValue("TRUE");
	public static Value newContentValue(String content){
		return new Value(new DataNode(type.name,content));
	}
	private Value(TypedNode source){
		super(source,type);
	}
	public String asText(){
		return source.title();
	}
	public void updateSource(Value value){
		source.setTitle(value.asText());
	}
	@Override
	public boolean equals(Object that){
		return asText().equals(((Value)that).asText());
	}
	public Value copyValue(){
		return new Value((DataNode)source.copyState());
	}
	@Override
	public String toString(){
		return type+"="+asText().replaceAll(" .*","");
	}
}
