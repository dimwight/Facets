package applicable.treecode;
import facets.util.Debug;
import facets.util.Titled;
import facets.util.tree.TypedNode;
/**
Creates {@link TreeCoded}s of the type of its {@link #name}. 
 */
public abstract class TreeCodeType<T extends TreeCoded>implements Titled{
	public final String name;
	public TreeCodeType(String name){
		if(name==null||name.trim().equals(""))
			throw new IllegalStateException("Null name in "+Debug.info(this));
		else this.name=name.trim();
	}
	public abstract T newCoded(TypedNode code,TreeCodeContext context);
	@Override
	public String toString(){
		return name;
	}
	@Override
	public String title(){
		return name;
	}
}
