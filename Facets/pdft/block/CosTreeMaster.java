package pdft.block;
import facets.core.app.PathSelection;
import facets.core.superficial.Notifying.Impact;
import facets.core.superficial.app.SSelection;
import facets.core.superficial.app.ViewerTarget;
import facets.facet.SwingViewerMaster;
import facets.util.Debug;
import facets.util.Util;
import java.awt.Component;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdfviewer.ArrayEntry;
import org.apache.pdfbox.pdfviewer.MapEntry;
import org.apache.pdfbox.pdfviewer.PDFTreeCellRenderer;
import org.apache.pdfbox.pdfviewer.PDFTreeModel;
import pdft.block.CosTreeView.TreeStyle;
/**
{@link SwingViewerMaster} that displays regions of a {@link COSDocument}
defined by {@link CosTreeView}s. 
<p>Uses a {@link JTree} with a {@link PDFTreeModel} and {@link PDFTreeCellRenderer}. 
 */
final class CosTreeMaster extends SwingViewerMaster{
	private TreePath pathThen=new TreePath("Dummy");
	private TreeSelectionListener treeListener=new TreeSelectionListener(){
		@Override
		public void valueChanged(TreeSelectionEvent e){
			TreePath pathNow=e.getPath();
			if(false)Util.printOut("CosTreeMaster.valueChanged:"+CosTreeMaster.this+
//						" now below then ="+pathThen.isDescendant(pathNow)+
						"\nthen="+Debug.arrayInfo(pathThen.getPath())+
						"\nnow="+Debug.arrayInfo(pathNow.getPath()));
			pathThen=pathNow;
			for(Object node:pathNow.getPath()){
				COSDictionary page=pageValue(node);
				if(page!=null&&page!=CosTreeMaster.this.page)
					viewerTarget().selectionChanged(PathSelection.newMinimal(page));
			}
		}
	};
	private Object page,treeValue;
	private CosTreeView view;
	private Map<COSDictionary,TreePath>pagePaths;
	@Override
	protected JComponent newAvatarPane(){
		JTree tree=new JTree();
		tree.setFont(new JMenu().getFont());
		boolean showRoot=false;
		tree.setRootVisible(showRoot);
		tree.setShowsRootHandles(!showRoot);
		tree.setCellRenderer(new PDFTreeCellRenderer(){
			public Component getTreeCellRendererComponent(JTree tree,Object value,
					boolean sel,boolean expanded,boolean leaf,int row,boolean hasFocus){
				JLabel label=(JLabel)super.getTreeCellRendererComponent(tree,value,sel,
						expanded,leaf,row,hasFocus);
				String info=Debug.info(value);
				if(false)Util.printOut("CosTreeMaster..getTreeCellRendererComponent: "+
						info+Debug.info(treeValue)+
						" "+value.equals(treeValue));
				if(false)label.setText(label.getText()+info);
				return label;
			}
		});
		tree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.addTreeSelectionListener(treeListener);
		return tree;
	}
	@Override
	protected void disposeAvatarPane(){
		pathThen=null;
		if(pagePaths!=null)pagePaths.clear();
		JTree tree=(JTree)avatarPane();
		tree.removeTreeSelectionListener(treeListener);
		tree.setModel(null);
		tree.setUI(null);
		if(false)try{
			refreshAvatars(Impact.DEFAULT);
		}catch(Exception e){}
	}
	@Override
	public void refreshAvatars(Impact impact){
		ViewerTarget viewer=viewerTarget();
		CosTreeView view=(CosTreeView)viewer.view();
		view.master=this;
		final SSelection selection=viewer.selection();
		final JTree tree=(JTree)avatarPane();
		page=selection.single();
		boolean emptyDocView=view.style==TreeStyle.Document
			&&((COSDocument)selection.content()).getObjects().size()>20*1000;
		if(view!=this.view){
			this.view=view;
			tree.removeTreeSelectionListener(treeListener);
			TreeModel model=emptyDocView?new PDFTreeModel(){
						public Object getRoot(){
							return "[Can't display " +((COSDocument)selection.content()).getObjects().size()+" objects]";
						}
						@Override
						public Object getChild(Object parent,int index){
							throw new RuntimeException("Not implemented in "+Debug.info(this));
						}
					}:new PDFTreeModel(){
				public Object getRoot(){
					return selection.content();
				}
				@Override
				public Object getChild(Object parent,int index){
					Object child=treeValue=super.getChild(parent,index);
					if(false)Util.printOut("CosTreeMaster..getChild:"+Debug.info(child));
					return child;
				}
			};
			tree.setModel(model);
			if(emptyDocView){
				tree.setRootVisible(true);
				return;
			}
			PagePaths paths=new PagePaths(model);
			paths.storePaths();
			pagePaths=paths.paths;
			tree.setSelectionPath(new TreePath(model.getRoot()));
			tree.addTreeSelectionListener(treeListener);
		}
		else if(emptyDocView)return;
		final TreePath treePath=tree.getSelectionPath();
		TreePath pagePath=pagePaths.get(page);
		if(false&&pagePath==null)throw new IllegalStateException("Page " +Debug.info(page)+
				" not found in "+this+Debug.arrayInfo(pagePaths.keySet().toArray()));
		tree.setSelectionPath(false&&treePath.getPathCount()>1?treePath:pagePath);
		Runnable mayFail=new Runnable(){public void run(){
			int row=tree.getLeadSelectionRow();
			if(row>=0&&tree.getPathForRow(row).isDescendant(treePath))
				tree.setSelectionPath(treePath);
		}};
		if(false&&Debug.natureDebug)mayFail.run();
		else try{
			mayFail.run();
		}catch(Exception e){
			e.printStackTrace();
		}
		tree.scrollPathToVisible(pagePath);
		if(true)return;
		boolean debug=false;
		if(debug)Util.printOut("CosTreeMaster.refreshAvatars:" +this+
				" "+pagePath.equals(tree.getSelectionPath())+
				"\ntree="+Debug.arrayInfo(treePath.getPath()));
		if(false&&debug)Util.printOut("CosTreeMaster.refreshAvatars:"+this+
				"\npage="+Debug.arrayInfo(pagePath.getPath()));
		if(false)for(TreePath path:Collections.list(tree.getExpandedDescendants(new TreePath(
						pagePath.getPathComponent(0)))
				))Util.printOut("CosTreeMaster.refreshAvatars: "+Debug.arrayInfo(
				path.getPath()));
		if(debug)Util.printOut("CosTreeMaster.refreshAvatars:"+this+
				" "+pagePath.equals(tree.getSelectionPath())+
				"\n~tree="+Debug.arrayInfo(tree.getSelectionPath().getPath())
				);
	}
	@Override
	protected void traceOutput(String msg){
		if(false)super.traceOutput(msg);
	}
	final private class PagePaths{
		final Map<COSDictionary,TreePath>paths=new HashMap();
		private final TreeModel model;
		PagePaths(TreeModel model){
			this.model=model;
		}
		void storePaths(){
			addPagePaths(new TreePath(model.getRoot()),view.style.pagePathMaxDepth);
			if(false)return;
			trace("..storePaths: ",paths.keySet().size());
			if(false)for(TreePath path:paths.values())
				for(Object node:path.getPath()){
					trace("..storePaths: class="+node.getClass());
					if(node instanceof MapEntry)
						trace("..storePaths: key="+Debug.info(((MapEntry)node).getKey()));
				}
		}
		private void addPagePaths(TreePath path,int depthLeft){
			Object node=path.getLastPathComponent();
			Collection<COSName>pagePathKeys=view.style.pagePathNames;
			if(node instanceof MapEntry&&(pagePathKeys==null||!pagePathKeys.contains(
					((MapEntry)node).getKey())))return;
			if(false)traceDebug(".addPagePaths:"+" depthLeft="+depthLeft+" node=",node);
			if(pageValue(node)!=null){
				COSDictionary key=(COSDictionary)
					(node instanceof ArrayEntry?((ArrayEntry)node).getValue():node);
				paths.put(key,path);
				trace("..addPagePaths: paths=" +paths.size()+" " +Debug.info(key)+">"+
						Debug.arrayInfo(path.getPath()));
				return;
			}
			if(--depthLeft==0)return;
			for(int childAt=0;childAt<model.getChildCount(node);childAt++)
				addPagePaths(path.pathByAddingChild(model.getChild(node,childAt)),depthLeft);
		}
	}
	private static COSDictionary pageValue(Object node){
		if(node instanceof ArrayEntry)node=((ArrayEntry)node).getValue();
		if(!(node instanceof COSDictionary))return null;
		COSDictionary maybePage=(COSDictionary)node;
		String type=maybePage.getNameAsString("Type");
		return type!=null&&type.equals("Page")?maybePage:null;
	}
	public String toString(){
		return Debug.info(this)+Debug.info(view);
	}
}