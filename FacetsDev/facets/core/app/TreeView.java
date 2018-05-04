package facets.core.app;
import static facets.util.tree.Nodes.*;
import static facets.util.tree.TypedNode.*;
import facets.core.superficial.app.SelectionView;
import facets.util.Debug;
import facets.util.ItemList;
import facets.util.OffsetPath;
import facets.util.Util;
import facets.util.tree.DataConstants;
import facets.util.tree.DataNode;
import facets.util.tree.Nodes;
import facets.util.tree.TypedNode;
import java.util.ArrayList;
import java.util.List;
/**
{@link SelectionView} for {@link TypedNode} content.
 */
public class TreeView extends SelectionView{
	public static final String KEY_DEBUG="treeDebug",KEY_EXPAND="treeExpand",
		NO_VALUE=" ";
	public static final int FACET_EXPAND=0,FACET_COLLAPSE=1;
	public static boolean debug;
	public TreeView(String title){
		super(title);
	}
	public boolean allowMultipleSelection(){
		return false;
	}
	/**
	Key defining icon for node or value. 
	 * @param content to be rendered
	 */
	public String contentIconKey(Object content){
		return (!(content instanceof TypedNode))?DataConstants.VALUE
				:((TypedNode)content).type();
	}
	/**
	Return contents to be rendered. 
	<ul>
	<li>If non-<code>null node</code> is {@link TypedNode#NULL_NODE}, 
	 returns an empty <code>Object[]</code> array.
	 <li>If {@link #filterNodeContents()} returns 
	  output of {@link #filteredNodeContents(TypedNode)}
	 <li>Otherwise returns the contents unchecked
	</ul>
	<p>The tree facet avatar may need to optimise calls to this method to ensure
	good rendering performance.  
	@param viewer rendering the content
	@param node contains content to be rendered
	 */
	public Object[]nodeContents(SViewer viewer,TypedNode node){
    if(node==null)throw new IllegalArgumentException("Null node in "+Debug.info(this));
		return node==NULL_NODE?new Object[]{}
			:filterNodeContents()?filteredNodeContents(node)
				:node.contents();
	}
	/**
	Checks  node<code>contents</code> against either 
	 {@link #includeNode(TypedNode,TypedNode)} or
	 {@link #includeValue(TypedNode,Object)}.  
	@param node
	 */
	protected final Object[]filteredNodeContents(TypedNode node){
		List list=new ArrayList();
		for(Object item:node.contents())
			if((item instanceof TypedNode?includeNode(node,
					(TypedNode)item):includeValue(node,item)))list.add(item);
		return list.toArray();
	}
	protected boolean filterNodeContents(){
		return false;
	}
	/**
	Should <code>node</code> be included in the displayed contents of <code>parent</code>?
	 <p>Default returns <code>true</code>.
	 @param parent is that passed to {@link #nodeContents(SViewer,TypedNode)}
	 */
	protected boolean includeNode(TypedNode parent,TypedNode node){
		return true;
	}
	/**
	Should <code>value</code> be included in the displayed contents of <code>parent</code>?
	 <p>Default returns <code>true</code>.
	 @param parent is that passed to {@link #nodeContents(SViewer,TypedNode)}
	 */
	protected boolean includeValue(TypedNode parent,Object value){
		return true;
	}
	/**
	The text to be rendered by the viewer for a node. 
	<p>Default returns node type and title. 
	 * @param node to be rendered
	 */
	public String nodeRenderText(TypedNode node){
		String title=node.title();
		Object[]values=node.values();
		TypedNode[]children=node.children();
		return node.type()+(!title.equals(UNTITLED)?(" "+title)
				:children.length==0&&values.length==1?(" ["+values[0]+"]"):"");
	}
	/**
	Should the viewer hide the root node of the tree? 
	@return by default <code>false</code>.
	 */
	public boolean hideRoot(){
		return false;
	}
	public boolean canChangeSelection(){
		return true;
	}
	public String emptyValueText(){
		return NO_VALUE;
	}
	public static TreeView newNonSelecting(String title){
		return new TreeView(title) {
			@Override
			public boolean canChangeSelection(){
				return false;
			}
		};
	}
}
