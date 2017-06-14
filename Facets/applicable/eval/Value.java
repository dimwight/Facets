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
	final public static TreeCodeType<Value>type=new TreeCodeType(
			Value.class.getSimpleName()){
		@Override
		public Value newCoded(TypedNode code,TreeCodeContext context){
			return new Value(code);
		};
	};
	public static final Value FALSE=newContentValue("false"),
			TRUE=newContentValue("true"),
			VOIDS[]=new Value[]{FALSE};
	public static Value newContentValue(String content){
		return new Value(new DataNode(type.name,UNTITLED,new String[]{content}));
	}
	private Value(TypedNode source){
		super(source,type);
	}
	public void updateSource(Value value){
		source.setContents(((TypedNode)value.source.copyState()).contents());
	}
	public String asText(){
		String[]values=(String[])source.values();
		return values.length==0?"":values[0];
	}
	@Override
	public boolean equals(Object that){
		return source.values()[0].equals(((Value)that).source.values()[0]);
	}
	public Value labelled(Label label){
		DataNode copy=(DataNode)source.copyState();
		copy.setTitle(label.text);
		return new Value(copy);
	}
	public Value unlabelled(){
		DataNode copy=(DataNode)source.copyState();
		copy.setTitle(UNTITLED);
		return new Value(copy);
	}
	@Override
	public String toString(){
		return super.toString()+"="+asText();
	}
	public boolean isFalse(){
		return asText().equals(FALSE.asText());
	}
}
