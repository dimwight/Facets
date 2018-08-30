package facets.core.app;
/**
{@link TreeView} that displays only the direct children of its root. 
 */
public class ListView extends TreeView{
	public ListView(String title){
		super(title);
	}
	@Override
	final public boolean hideRoot(){
		return true;
	}
	public boolean isHorizontal(){
		return false;
	}
	public boolean useTextFont(){
		return false;
	}
}
