package facets.util.tree;
import static facets.util.tree.Nodes.*;
import facets.util.Debug;
import facets.util.TitledList;
import facets.util.Util;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
/**
{@link List} wrapping a {@link DataNode} and its {@link TypedNode#children()}. 
 */
public final class NodeList extends TitledList<TypedNode>{
	/** The node whose children the {@link NodeList} is manipulating. */
	public final DataNode parent;
	private final HashMap<String,TypedNode>titled=new HashMap();
	private final boolean autoUpdate;
	/**
	Unique constructor. 
	@param parent must be a {@link DataNode} whose children the {@link NodeList} will 
	manipulate
	@param autoUpdate should each operation of the {@link NodeList} 
	be reflected immediately in  {@link #parent}? 
	 */
	public NodeList(TypedNode parent,boolean autoUpdate){
		super(TypedNode.class);
		this.parent=(DataNode)parent;
		this.autoUpdate=autoUpdate;
		addAll(Arrays.asList(parent.children()));
	}
	@Override
	final protected HashMap<String,TypedNode>getTitled(){
		return titled;
	}
	/**
	Update {@link #parent} with the contents of the {@link NodeList}. 
	<p>Called internally on each operation unless <code>false</code> was passed
	to {@link NodeList#NodeList(TypedNode, boolean)}, in which case client code
	must call as appropriate. 
	 */
	public void updateParent(){
		if(false&&hasMixedContents(parent))throw new IllegalArgumentException(
				"Mixed contents in parent="+parent);
		parent.setChildren(toArray(new TypedNode[]{}));
		titled.clear();
		for(TypedNode child:parent.children())titled.put(child.title(),child);
	}
	public boolean add(TypedNode n){
		boolean added=super.add(n);
		if(autoUpdate)updateParent();
		return added;
	}
	public TypedNode set(int at,TypedNode n){
		TypedNode set=super.set(at,n);
		if(autoUpdate)updateParent();
		return set;
	}
	public void add(int at,TypedNode n){
		super.add(at,n);
		if(autoUpdate)updateParent();
	}
	public boolean addAll(Collection<?extends TypedNode>c){
		boolean all=super.addAll(c);
		if(autoUpdate)updateParent();
		return all;
	}
	public boolean remove(Object o){
		boolean removed=super.remove(o);
		if(!removed)Util.printOut(Debug.info(this)+": Not found remove="+o);
		if(o==null)return removed;
		((TypedNode)o).setParent(null);
		if(autoUpdate)updateParent();
		return removed;
	}
	public TypedNode remove(int at){
		TypedNode remove=super.remove(at);
		if(autoUpdate)updateParent();
		return remove;
	}
	public boolean removeAll(Collection<?>c){
		boolean all=super.removeAll(c);
		if(autoUpdate)updateParent();
		return all;
	}
	public void clear(){
		super.clear();
		if(autoUpdate)updateParent();
	}
	public void addAll(TypedNode...nodes){
		addAll(Arrays.asList(nodes));
		if(autoUpdate)updateParent();
	}
	public void addAll(int at,TypedNode...nodes){
		addAll(at,Arrays.asList(nodes));
		if(autoUpdate)updateParent();
	}
	public void removeAll(TypedNode[]nodes){
		removeAll(Arrays.asList(nodes));
	}
	public TypedNode[]copyChildren(){
		updateParent();
		return((TypedNode)parent.copyState()).children();
	}
	public void updateMixedParent(Object insertMark){
		ArrayList update=new ArrayList();
		List contents=Arrays.asList(parent.contents());
		for(Object c:contents)
			if(c instanceof TypedNode){
				if(contains(c)){
					update.add(c);
					remove(c);
				}
			}
			else update.add(c);
		if(insertMark==null)update.addAll(this);
		else update.addAll(contents.indexOf(insertMark),this);
		if(false){
			Util.printOut("NodeList.updateMixedParent: parent=",parent.contents());
			Util.printOut("this=",toArray());
			Util.printOut("update=",update);
		}
		else parent.setContents(update.toArray());
	}
}