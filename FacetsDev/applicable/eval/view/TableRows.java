package applicable.eval.view;
import facets.util.tree.TypedNode;
import facets.util.tree.TypedNode;
import applicable.treecode.TreeCodeContext;
import applicable.treecode.TreeCodeType;
import applicable.treecode.TreeCoded;
public final class TableRows extends TreeCoded{
	final public static TreeCodeType<TableRows>type=new TreeCodeType(
			TableRows.class.getSimpleName()){
		@Override
		public TableRows newCoded(TypedNode source,TreeCodeContext context){
			return new TableRows(source);
		};
	};
	private TableRows(TypedNode source){
		super(source,type);
	}
}
