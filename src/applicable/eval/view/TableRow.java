package applicable.eval.view;
import facets.util.tree.TypedNode;
import applicable.treecode.TreeCodeContext;
import applicable.treecode.TreeCodeType;
import applicable.treecode.TreeCoded;
public final class TableRow extends TreeCoded{
	final public static TreeCodeType<TableRow>type=new TreeCodeType(
			TableRow.class.getSimpleName()){
		@Override
		public TableRow newCoded(TypedNode source,TreeCodeContext context){
			return new TableRow(source);
		};
	};
	private TableRow(TypedNode source){
		super(source,type);
	}
}
