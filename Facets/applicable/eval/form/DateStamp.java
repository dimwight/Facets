package applicable.eval.form;
import facets.util.Debug;
import facets.util.tree.DataNode;
import facets.util.tree.TypedNode;
import facets.util.tree.TypedNode;
import facets.util.tree.ValueNode;
import applicable.treecode.TreeCodeContext;
import applicable.treecode.TreeCodeType;
import applicable.treecode.TreeCoded;
/**
Provides a unique sequenced ID for <b>Eval</b>code. 
 */
public final class DateStamp extends TreeCoded{
	final public static TreeCodeType<DateStamp>type=new TreeCodeType(
			DateStamp.class.getSimpleName()){
		@Override
		public DateStamp newCoded(TypedNode code,TreeCodeContext context){
			return new DateStamp((DataNode)code);
		};
	};
	private final long stamp;
	private DateStamp(DataNode source){
		super(source,type);
		stamp=Long.valueOf(source.values()[0]);
	}
	@Override
	public boolean equals(Object that){
		return stamp==((DateStamp)that).stamp;
	}
	@Override
	public int hashCode(){
		return(int)stamp;
	}
	@Override
	public String toString(){
		return Debug.info(this)+" stamp="+stamp;
	}
	public static DateStamp newNow(){
		return type.newCoded(new ValueNode(type.name,new Object[]{System.currentTimeMillis()}),
				null);
	}
}
