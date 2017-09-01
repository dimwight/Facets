package pdft.inspect;
import facets.core.app.PathSelection;
import facets.core.superficial.Notifying.Impact;
import facets.core.superficial.app.SSelection;
import facets.core.superficial.app.ViewerTarget;
import facets.facet.SwingViewerMaster;
import facets.util.Debug;
import facets.util.Titled;
import facets.util.Util;
import facets.util.app.AppValues;
import java.awt.Component;
import java.util.Collections;
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
import org.apache.pdfbox.pdfviewer.ArrayEntry;
import org.apache.pdfbox.pdfviewer.PDFTreeCellRenderer;
import org.apache.pdfbox.pdfviewer.PDFTreeModel;
final class CosTreeMaster extends SwingViewerMaster{
	private final TreeSelectionListener treeListener=new TreeSelectionListener(){
		private TreePath pathThen=new TreePath("Dummy");
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
	private Titled view;
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
	public void refreshAvatars(Impact impact){
		ViewerTarget viewer=viewerTarget();
		CosTreeView view=(CosTreeView)viewer.view();
		final SSelection selection=viewer.selection();
		final JTree tree=(JTree)avatarPane();
		page=selection.multiple()[0];
		if(view!=this.view){
			this.view=view;
			tree.removeTreeSelectionListener(treeListener);
			TreeModel model=new PDFTreeModel(){
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
			pagePaths=view.newPagePaths(model);
			tree.setModel(model);
			tree.setSelectionPath(new TreePath(model.getRoot()));
			tree.addTreeSelectionListener(treeListener);
		}
		final TreePath treePath=tree.getSelectionPath();
		TreePath pagePath=pagePaths.get(page);
		if(pagePath==null)throw new IllegalStateException("Page" +Debug.info(page)+
				" not found in "+this+Debug.arrayInfo(pagePaths.keySet().toArray()));
		tree.setSelectionPath(false&&treePath.getPathCount()>1?treePath:pagePath);
		Runnable mayFail=new Runnable(){public void run(){
			if(tree.getPathForRow(tree.getLeadSelectionRow()).isDescendant(treePath))
			tree.setSelectionPath(treePath);
		}};
		if(Debug.natureDebug)mayFail.run();
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
	public String toString(){
		return Debug.info(this)+Debug.info(view);
	}
	final static COSDictionary pageValue(Object node){
		if(node instanceof ArrayEntry)node=((ArrayEntry)node).getValue();
		if(!(node instanceof COSDictionary))return null;
		COSDictionary maybePage=(COSDictionary)node;
		String type=maybePage.getNameAsString("Type");
		return type!=null&&type.equals("Page")?maybePage:null;
	}
}/*
This file forms part of Version 0.3.62 of pdfInspect 
http://pdfinspector.sourceforge.net
Copyright (C) 2011  David M Wright 
This library is free software; you can redistribute it and/or modify it under 
the terms of the GNU Lesser General Public License as published by the 
Free Software Foundation; either release 3 of the License, or (at your 
option) any later release.
This library is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR 
A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more 
details.
You should have received a copy of the GNU Lesser General Public License along 
with this library; if not, write to the Free Software Foundation, Inc., 
59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
*/
