package facets.facet.kit.swing.tree;
import facets.util.Debug;
import facets.util.OffsetPath;
import facets.util.Util;
import facets.util.tree.TypedNode;
import java.awt.Insets;
import java.util.Collections;
import java.util.Enumeration;
import javax.swing.JMenuBar;
import javax.swing.JTree;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
public class OffsetPathTree extends JTree{
	private static class ModelPath extends OffsetPath{
		private final TreeModel model;
		ModelPath(TreeModel model,Object[]members){
			super(newModelOffsets(model,members));
			this.model=model;
		}
		private static int[]newModelOffsets(TreeModel model,Object[]members){
			int[]offsets=new int[members.length];
			offsets[0]=0;
			for(int o=1;o<offsets.length;o++)
				offsets[o]=model.getIndexOfChild(members[o-1],members[o]);
			return offsets;
		}
		ModelPath(TreeModel model,int[]offsets){
			super(offsets);
			this.model=model;
		}
		@Override
		protected Object[]newMembers(Object root,int[]offsets){
			Object[]members=new Object[offsets.length];
			members[0]=root;
			for(int i=1;i<members.length;i++)
				members[i]=model.getChild(members[i-1],offsets[i]);
			return members;
		}
		@Override
		protected int[]newOffsets(Object[]members){
			throw new RuntimeException("Not implemented in "+Debug.info(this));
		}
		@Override
		public OffsetPath procrusted(Object root,Object to){
			throw new RuntimeException("Not implemented in "+Debug.info(this));
		}
	}
	final public OffsetPath newTreeOffsetPath(int[]offsets){
		return new ModelPath(getModel(),offsets);
	}
	public OffsetPath newEventOffsetPath(TreePath path){
		return new ModelPath(getModel(),path.getPath());
	}
	private OffsetPath[]newOffsetPaths(TreePath[]trees){
		OffsetPath[]offsets=new OffsetPath[trees.length];
		for(int i=0;i<offsets.length;i++){
			TreePath path=trees[i];
			try{
				offsets[i]=new ModelPath(getModel(),path.getPath());
			}catch(Exception e){
				if(true)throw new RuntimeException(e);
				path=true?path:new TreePath(path.getParentPath());
			}
		}
		return offsets;
	}
	private OffsetPath[]offsets;
	public OffsetPathTree(TreeModel model){
		super(model);
		setFont(new JMenuBar().getFont());
	}
	final void storeExpansionPaths(){
		Enumeration expanded=getExpandedDescendants(new TreePath(getModel().getRoot()));
		final TreePath[]trees=expanded==null?new TreePath[]{}:
	    (TreePath[])Collections.list(expanded).toArray(new TreePath[]{});
		OffsetPath[]offsets=trees==null?null:newOffsetPaths(trees);
		if(false)Util.printOut("OffsetPathTree.storeExpansionPaths: offsets=",offsets);
		putOffsets(offsets);
	}
	final void restoreExpansionPaths(){
		OffsetPath[]offsets=getOffsets();
		if(false)Util.printOut("OffsetPathTree.restoreExpansionPaths: offsets=",offsets);
	  if(offsets!=null)for(OffsetPath o:offsets)try{
		  	expandPath(new TreePath(o.members(getModel().getRoot())));
		  }
		  catch(Exception e){
		  	if(true)Util.printOut("PathTree.restoreExpansionPaths: ",Debug.info(this));
		  	return;
		  }
	}
	@Override
	final public Insets getInsets(){
		return new Insets(5,5,5,5);
	}
	public void setOffsetPath(OffsetPath offsets){
		setSelectionPath(new TreePath(offsets.members(getModel().getRoot())));
	}
	protected void putOffsets(OffsetPath[]offsets){
		this.offsets=offsets;
	}
	protected OffsetPath[]getOffsets(){
		return offsets;
	}
	final void expandOrCollapse(TreePath path,int level,boolean expand){
		Object last=path.getLastPathComponent();
		if(!(last instanceof TypedNode))return;
		expand&=level-->0;
		TypedNode node=(TypedNode)last;
		for(TypedNode child:node.children())
			expandOrCollapse(path.pathByAddingChild(child),level,expand);
		if(expand)expandPath(path);
		else collapsePath(path);
	}
}
