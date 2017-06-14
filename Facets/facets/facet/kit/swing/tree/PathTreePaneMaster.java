package facets.facet.kit.swing.tree;
import static facets.core.app.PathSelection.*;
import static java.util.Arrays.*;
import facets.core.app.PathSelection;
import facets.core.app.TreeView;
import facets.core.superficial.SFacet;
import facets.core.superficial.SIndexing;
import facets.core.superficial.STarget;
import facets.core.superficial.STargeter;
import facets.core.superficial.TargetCore;
import facets.core.superficial.TargeterCore;
import facets.core.superficial.Notifying.Impact;
import facets.core.superficial.app.SSelection;
import facets.core.superficial.app.SViewer;
import facets.core.superficial.app.ViewerTarget;
import facets.facet.FacetFactory;
import facets.facet.kit.swing.KitSwing;
import facets.util.Debug;
import facets.util.Objects;
import facets.util.OffsetPath;
import facets.util.Stateful;
import facets.util.Times;
import facets.util.Util;
import facets.util.ValueProxy;
import facets.util.tree.NodePath;
import facets.util.tree.TypedNode;
import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
public class PathTreePaneMaster extends PathNodePaneMaster{
	@Override
	protected void disposeAvatarPane(){}
	private final DatatreeModel model=false?new DatatreeModel(){
		public Object getRoot(){
			if(contentRoot==null)throw new IllegalStateException(
					"Null contentRoot in "+Debug.info(this));
			return contentRoot;
		};
	}
	:new DatatreeModel(){
		private Object[]lastContents;
		public Object getRoot(){
			if(contentRoot==null)throw new IllegalStateException(
					"Null contentRoot in "+Debug.info(this));
			return contentRoot;
		}
		public boolean isLeaf(Object node){
			return node instanceof StringBuilder 
					||((TypedNode)node).contents().length==0;
		}
		protected Object[]getParentContents(Object parent){
			if(lastParent==parent)return lastContents;
			SViewer viewer=viewerTarget();
			TreeView view=(TreeView)viewer.view();
			Object[]contents=parent instanceof TypedNode?
				view.nodeContents(viewer,lastParent=(TypedNode)parent):new Object[]{};
		  lastContents=new Object[contents.length];
			for(int i=0;i<lastContents.length;i++){
				lastContents[i]=contents[i];
				if(false)trace(".getParentContents: "+lastContents[i].hashCode());
				if(lastContents[i]instanceof TypedNode)continue;
				String check=((String)lastContents[i]).replaceAll("<html>"," <html>");
				if(check.trim().equals(""))check=view.emptyValueText();
				lastContents[i]=new StringBuilder(check);
			}
			return contents;
		}
	};
	private final boolean inGraphPane;
	final boolean debug=false;
	private TypedNode contentRoot,lastParent;
	private boolean updatingDisplay;
	private SFacet[]targetFacets;
	public PathTreePaneMaster(boolean inGraphPane){
		this.inGraphPane=inGraphPane;
	}
	@Override
	protected JComponent newAvatarPane(){
		final SViewer viewer=viewerTarget();
		contentRoot=(TypedNode)viewer.selection().content();
		final MasterPathTree tree=new MasterPathTree(model);
		KitSwing.adjustComponents(false,tree);
		tree.setFocusable(!inGraphPane);
		final TreeView view=(TreeView)viewer.view();
		tree.setEditable(false&&view.isLive());
		tree.setRootVisible(!view.hideRoot());
		tree.restoreExpansionPaths();
		tree.setCellRenderer(new DefaultTreeCellRenderer(){
			public Component getTreeCellRendererComponent(JTree tree,Object value,
					boolean sel,boolean expanded,boolean leaf,int row,boolean hasFocus){
				JLabel label=(JLabel)super.getTreeCellRendererComponent(tree,value,sel,
						expanded,leaf,row,hasFocus);
				PathTreePaneMaster.this.modifyNodeValueRendering(value,label);
				return label;
			}
		});
		tree.addTreeSelectionListener(new TreeSelectionListener(){
			public void valueChanged(TreeSelectionEvent e){
				if(updatingDisplay)return;
				ViewerTarget viewer=PathTreePaneMaster.this.viewerTarget();
				SSelection selection;
				TreePath path=e.getPath();
				String msg=Objects.toString(path.getPath());
				try{
					OffsetPath nodePath=tree.newEventOffsetPath(path);
					msg=nodePath.toString();
					PathSelection treeSelection=new PathSelection(contentRoot,nodePath);
					msg=treeSelection.toString();
					selection=procrust(treeSelection,contentRoot);
				}catch(Exception x){
					selection=viewer.selection();
					if(true||debug)throw new RuntimeException(msg,x);
					else trace(".valueChanged: ",x);
				}
				if(debug)trace(".valueChanged: view="+view+" selection="+selection);
				if(view.canChangeSelection())viewer.selectionChanged(selection);
			}
		});
		tree.addFocusListener(new FocusListener(){
			@Override
			public void focusLost(FocusEvent e){}
			@Override
			public void focusGained(FocusEvent e){
				if(false&&!inGraphPane)viewerTarget().ensureActive(Impact.ACTIVE);
			}
		});
		return tree;
	}
	@Override
	public void refreshAvatars(Impact impact){
		if(impact==Impact.DISPOSE){
			contentRoot.setChildren();
			Debug.memCheck("PathTreePaneMaster.refreshAvatars: ");
			return;
		}
		updatingDisplay=true;
		lastParent=null;
		SViewer viewer=viewerTarget();
		PathSelection contentPaths=(PathSelection)viewer.selection();
		Object contentThen=contentRoot;
		MasterPathTree tree=(MasterPathTree)avatarPane();
		tree.storeExpansionPaths();
		contentRoot=(TypedNode)contentPaths.content();
		if(contentRoot!=contentThen||impact.exceeds(Impact.SELECTION)){
			model.rootUpdated();
			tree.restoreExpansionPaths();
		}
		OffsetPath contentPath=contentPaths.paths[0];
		if(false)trace(".refreshAvatars: contentPath="+contentPath.target(contentRoot));
		if(contentPath==OffsetPath.empty)tree.clearSelection();
		else{
			TreeView view=(TreeView)viewer.view();
			List members=new ArrayList();
			TypedNode parent=null;
			for(Iterator i=asList(contentPath.members(contentRoot)).iterator();
					i.hasNext();){
				Object m=i.next();
				if(parent!=null) 
					while(!asList(view.nodeContents(viewer,parent)).contains(m)){
						parent=(TypedNode)m;
						if(i.hasNext())m=i.next();
						else break;
					}
				members.add(parent=(TypedNode)m);
			}
			if(contentPath==OffsetPath.singleMembered)
				tree.setSelectionPath(new TreePath(contentRoot));
			else tree.setOffsetPath(new NodePath(members.toArray()).valueAtChecked(
					((NodePath)contentPath).valueAt()));
		}
		if(false)expand.setIndex(expand.index());
		updatingDisplay=false;
	}
	@Override
	protected void traceOutput(String msg){
		if(true)Util.printOut(
				PathTreePaneMaster.class.getSimpleName()+"["+viewerTarget().title()+"]"+msg);
	}
	private final SIndexing expand=new SIndexing("E&xpansion",
			new Object[]{"None",1,2,3,4,5},0,new SIndexing.Coupler(){
		public void indexSet(SIndexing i){
			onExpandOrCollapse(i);
		}
		private void onExpandOrCollapse(STarget t){
			PathSelection contentPaths=(PathSelection)viewerTarget().selection();
			TreePath path=new TreePath(contentPaths.paths[0].members(
					contentPaths.content()));
			((MasterPathTree)avatarPane()).expandOrCollapse(path,
					expand.index(),t==expand);
		}
	});
	protected STarget[]targets(){
		return new STarget[]{expand};
	}
	public SFacet[]getTargetFacets(FacetFactory ff){
		if(targetFacets!=null)return targetFacets;
		STargeter[]elements=TargeterCore.newRetargeted(
				new TargetCore("PathTreePaneMaster",targets()),true).elements();
		return targetFacets=new SFacet[]{
				ff.indexingRadioButtonMenu(elements[0],FacetFactory.HINT_INDEXING_SELECT),
		};
	}
}
