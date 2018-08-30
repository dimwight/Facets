package facets.util.tree;
import facets.util.Util;
import java.util.ArrayList;
import java.util.List;
/**
Facilitates progressive changes in {@link TypedNode} structures. 
 */
public abstract class TreeTransform{
	public final String title;
	public TreeTransform(String title){
		this.title=title;
	}
	public static void applyAll(TypedNode tree,TreeTransform...all){
		List<TreeTransform>apply=new ArrayList();
		for(TreeTransform t:all)
			if(!t.doApply(tree))break;
			else apply.add(0,t);
		for(TreeTransform u:apply){
			Util.printOut("TreeUpdate: ",u.title);
			u.apply(tree);
		}
	}
	protected abstract void apply(TypedNode tree);
	protected boolean doApply(TypedNode tree){
		return true;
	}
}