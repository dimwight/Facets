package facets.facet.kit.swing.tree;
import static facets.core.app.PathSelection.*;
import static java.util.Arrays.*;
import facets.core.app.PathSelection;
import facets.core.app.SViewer;
import facets.core.app.TreeView;
import facets.core.app.ViewerTarget;
import facets.core.superficial.Notifying.Impact;
import facets.core.superficial.SFacet;
import facets.core.superficial.SIndexing;
import facets.core.superficial.STarget;
import facets.core.superficial.STargeter;
import facets.core.superficial.TargetCore;
import facets.core.superficial.TargeterCore;
import facets.facet.FacetFactory;
import facets.facet.kit.swing.KitSwing;
import facets.util.Debug;
import facets.util.Objects;
import facets.util.OffsetPath;
import facets.util.Util;
import facets.util.tree.NodePath;
import facets.util.tree.TypedNode;
import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
public class PathTreePaneMaster extends PathNodePaneMaster{
	@Override
	protected void disposeAvatarPane(){}
	final DatatreeModel model=false?new DatatreeModel(){
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
		final TreeView view=(TreeView)viewer.view();
		boolean multiples=view.allowMultipleSelection();
		final MasterPathTree tree=new MasterPathTree(this,multiples);
		KitSwing.adjustComponents(false,tree);
		tree.setFocusable(!inGraphPane);
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
		tree.addTreeSelectionListener(false&&!multiples?new TreeSelectionListener(){
			public void valueChanged(TreeSelectionEvent e){
				if(updatingDisplay)return;
				ViewerTarget viewer=PathTreePaneMaster.this.viewerTarget();
				PathSelection selection;
				TreePath path=e.getPath();
				String detail=Objects.toString(path.getPath());
				try{
					OffsetPath nodePath=tree.newOffsetPath(path);
					detail=nodePath.toString();
					trace(".valueChanged: target=",nodePath.target(contentRoot));
					PathSelection treeSelection=new PathSelection(contentRoot,nodePath);
					detail=treeSelection.toString();
					selection=procrust(treeSelection,contentRoot);
				}catch(Exception x){
					selection=(PathSelection)viewer.selection();
					if(true)throw new RuntimeException(detail,x);
					else trace(".valueChanged: ",x);
				}
				traceDebug(".valueChanged: selection=",selection.paths[0].target(contentRoot));
				if(view.canChangeSelection())viewer.selectionChanged(selection);
			}
		}
		:new TreeSelectionListener(){
			List<TreePath>treePaths=new ArrayList();
			public void valueChanged(TreeSelectionEvent e){
				for(TreePath path:e.getPaths())
					if(treePaths.contains(path))treePaths.remove(path);
					else treePaths.add(path);
				if(updatingDisplay)return;
				ViewerTarget viewer=PathTreePaneMaster.this.viewerTarget();
				List<OffsetPath>offsets=new ArrayList();
				PathSelection selection=(PathSelection)viewer.selection();
				Object[]parentsThen=null;
				Class classThen=null;
				for(TreePath path:treePaths){
					TreePath parentPath=path.getParentPath();
					if(false&&parentPath==null)continue;
					Object[]parents=parentPath==null?new Object[]{}:parentPath.getPath();
					Class classNow=path.getLastPathComponent().getClass();
					if(false)trace(".valueChanged: parents=",parents);
					if(parentsThen!=null&&
							(!Arrays.equals(parentsThen,parents)||classNow!=classThen))break;
					parentsThen=parents;
					classThen=classNow;
					String detail=Objects.toString(parents);
					try{
						OffsetPath nodePath=tree.newOffsetPath(path);
						detail=nodePath.toString();
						trace(".valueChanged: target=",nodePath.target(contentRoot));
						PathSelection treeSelection=new PathSelection(contentRoot,nodePath);
						detail=treeSelection.toString();
						offsets.add(procrust(treeSelection,contentRoot).paths[0]);
					}catch(Exception x){
						offsets.add(selection.paths[0]);
						if(true)throw new RuntimeException(detail,x);
						else trace(".valueChanged: ",x);
						break;
					}
				}
				if(false)trace(".valueChanged: offsets=",offsets.size());
				if(!offsets.isEmpty())
					selection=new PathSelection(contentRoot,offsets.toArray(new OffsetPath[]{}));
				if(false)traceDebug(".valueChanged: selection=",selection.paths[0].target(contentRoot));
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
		TreeView view=(TreeView)viewer.view();
		PathSelection contentPaths=(PathSelection)viewer.selection();
		Object contentThen=contentRoot;
		MasterPathTree tree=(MasterPathTree)avatarPane();
		tree.storeAndClearExpansionPaths();
		contentRoot=(TypedNode)contentPaths.content();
		if(contentRoot!=contentThen||impact.exceeds(Impact.SELECTION)){
			model.rootUpdated();
			tree.restoreExpansionPaths();
		}
		traceDebug(".refreshAvatars: contentPaths=",contentPaths.paths[0]);
		for(OffsetPath contentPath:contentPaths.paths){
			if(contentPath==OffsetPath.empty){
				tree.clearSelection();
				continue;
			}
			trace(".refreshAvatars: target="+contentPath.target(contentRoot));
			List members=new ArrayList();
			TypedNode parent=null;
			for(Iterator i=asList(contentPath.members(contentRoot)).iterator();
					i.hasNext();){
				Object member=i.next();
				if(parent!=null) 
					while(!asList(view.nodeContents(viewer,parent)).contains(member)){
						parent=(TypedNode)member;
						if(i.hasNext())member=i.next();
						else break;
					}
				members.add(parent=(TypedNode)member);
			}
			if(contentPath==OffsetPath.singleMembered)
				tree.setSelectionPath(new TreePath(contentRoot));
			else tree.addOffsetPath(new NodePath(members.toArray()).valueAtChecked(
					((NodePath)contentPath).valueAt()));
		}
		if(false)trace(".refreshAvatars~: tree=",tree.getSelectionPaths().length);
		if(false)expand.setIndex(expand.index());
		updatingDisplay=false;
	}
	@Override
	protected void traceOutput(String msg){
		if(false&&msg.contains("valueChanged"))Util.printOut(getClass().getSimpleName()
				+"["+viewerTarget().title()+"]"+msg);
	}
	private final SIndexing expand=new SIndexing("E&xpansion",
			new Object[]{"None",2,4,8,16,32},0,new SIndexing.Coupler(){
		public void indexSet(SIndexing i){
			onExpandOrCollapse(i);
		}
		private void onExpandOrCollapse(STarget t){
			PathSelection contentPaths=(PathSelection)viewerTarget().selection();
			TreePath path=new TreePath(contentPaths.paths[0].members(
					contentPaths.content()));
			((MasterPathTree)avatarPane()).expandOrCollapse(path,
					expand.index()==0?0:(Integer)expand.indexed(),
							t==expand);
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
