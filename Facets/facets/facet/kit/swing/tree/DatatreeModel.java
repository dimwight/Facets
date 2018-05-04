package facets.facet.kit.swing.tree;
import static facets.util.tree.TypedNode.*;
import facets.core.app.SViewer;
import facets.core.app.TreeView;
import facets.util.Debug;
import facets.util.Tracer;
import facets.util.tree.DataNode;
import facets.util.tree.TypedNode;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
/** {@link TreeModel} that knows about {@link DataNode}s. 
 */
public abstract class DatatreeModel extends Tracer implements TreeModel{
	protected final Set<TreeModelListener>listeners=new HashSet();
	public Object getRoot(){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	final public int getChildCount(Object parent){
		return getParentContents(parent).length;
	}
	final public Object getChild(Object parent,int at){
		return getParentContents(parent)[at];
	}
	final public int getIndexOfChild(Object parent,Object child){
		Object[]contents=getParentContents(parent);
		for(int i=0;i<contents.length;i++)
			if(contents[i]==child)return i;
		return -1;
	}
	public boolean isLeaf(Object node){
		return !(node instanceof DataNode);
	}
	final public void addTreeModelListener(TreeModelListener listener){
		listeners.add(listener);
	}
	final public void removeTreeModelListener(TreeModelListener listener){
		listeners.remove(listener);
	}
	public void valueForPathChanged(TreePath path,Object value){
		String textNow=(String)value;
		Object target=path.getLastPathComponent();
		if(target instanceof DataNode){
			DataNode node=(DataNode)target;
			node.setTitle(textNow.trim().equals("")?UNTITLED:textNow);
		}
		else{
			DataNode node=(DataNode)path.getParentPath().getLastPathComponent();
			Object[]contents=node.contents();
			int contentAt=0;
			for(Object content:contents)
				if(content==target)contents[contentAt]=textNow;
				else contentAt++;
			if(contentAt>contents.length-1)throw new IllegalStateException(
					"Unmatched value for target="+target+" in "+node);
			else node.setContents(contents);
			if(false)trace(".~valueForPathChanged: contents=",node.contents());
		}
		for(TreeModelListener l:listeners)
			l.treeNodesChanged(new TreeModelEvent(this,path.getParentPath(),
					new int[]{0},
					new Object[]{target}));
	}
	protected Object[]getParentContents(Object parent){
		return parent==null?new Object[]{}
			:stringCheckedContents(((TypedNode)parent).contents());
	}
	protected static Object[]stringCheckedContents(Object[]contents){
		for(int i=0;i<contents.length;i++){
			if(contents[i]instanceof TypedNode)continue;
			String check=contents[i].toString();
			contents[i]=true?check:new StringBuilder(check);
		}
		return contents;
	}
	final void rootUpdated(){
		Object root=getRoot();
		for(TreeModelListener l:listeners){
			l.treeStructureChanged(new TreeModelEvent(this,new TreePath(root)));
		}
	}}
