package applicable.treetext;
import static applicable.treetext.TreeTextContenter.*;
import static facets.core.app.ActionViewerTarget.Action.*;
import static facets.util.tree.DataConstants.*;
import facets.core.app.ActionViewerTarget;
import facets.core.app.NodeViewable;
import facets.core.app.PathSelection;
import facets.core.app.SView;
import facets.core.app.SViewer;
import facets.core.app.TextView;
import facets.core.app.TreeView;
import facets.core.app.ValueEdit;
import facets.core.app.ViewableAction;
import facets.core.superficial.app.SSelection;
import facets.facet.app.FacetAppSurface;
import facets.util.Debug;
import facets.util.OffsetPath;
import facets.util.tree.NodePath;
import facets.util.tree.TypedNode;
import applicable.treetext.TreeTextContenter.TreeTextView;
public abstract class TreeTextViewable extends NodeViewable{
	private final FacetAppSurface app;
	final TreeView debugView;
	public TreeTextViewable(TypedNode tree,ClipperSource clipperSource,
			FacetAppSurface app){
		super(tree,clipperSource);
		this.app=app;
		readSelectionState(app.spec.state(),STATE_OFFSETS);
		boolean liveViews=app.spec.canEditContent();
		debugView=new TreeView("Debug"){
			@Override
			public boolean allowMultipleSelection(){
				return true;
			}
			@Override
			public boolean hideRoot(){
				return tree.type().endsWith(TYPE_XML);
			}
			@Override
			public boolean isLive(){
				return liveViews;
			}
		};
	}
	@Override
	final protected SSelection newViewerSelection(SViewer viewer){
		if(viewer.view()==debugView)return selection();
		else return newNonTreeViewerSelection(viewer);
	}
	@Override
	final protected void viewerSelectionChanged(SViewer viewer,SSelection selection){
		if(viewer.view() instanceof TreeView)
			super.viewerSelectionChanged(viewer,selection);
		else nonTreeViewerSelectionChanged(viewer,selection);
		putSelectionState(app.spec.state(),STATE_OFFSETS);
	}
	@Override
	final protected void viewerSelectionEdited(SViewer viewer,Object edit,
			boolean interim){
		if(viewer.view() instanceof TreeView)
			textViewerSelectionEdited(viewer,edit,interim);
		else nonTreeViewerSelectionEdited(viewer,edit,interim);
	}
	protected SSelection newNonTreeViewerSelection(SViewer viewer){
		SView view=viewer.view();
		if(view instanceof TreeTextView)
			return((TreeTextView)view).newViewerSelection(viewer,selection());
		else throw new RuntimeException("Not implemented in "+this);			
	}
	protected void nonTreeViewerSelectionChanged(SViewer viewer,
			SSelection selection){
		SView view=viewer.view();
		if(view instanceof TreeTextView)
			super.viewerSelectionChanged(viewer,selection);
		else throw new RuntimeException("Not implemented in "+this);
	}
	protected void nonTreeViewerSelectionEdited(SViewer viewer,Object edit,
			boolean interim){
		SView view=viewer.view();
		if(view instanceof TextView)
			textViewerSelectionEdited(viewer,edit,interim);
		else throw new RuntimeException("Not implemented in "+this);
	}
	protected void textViewerSelectionEdited(SViewer viewer,Object edit,
			boolean interim){
		throw new RuntimeException("Not implemented in "+this);
	}
	@Override
	public ViewableAction[]viewerActions(SView view){
		ViewableAction[]all={
				COPY,
		     CUT,
		     PASTE,
		     DELETE,
		     MODIFY,
		     UNDO,
		     REDO};
		return view.isLive()?all:new ViewableAction[]{COPY};
	}
	@Override
	public boolean actionIsLive(SViewer viewer,ViewableAction action){
		TypedNode tree=tree();
		SSelection selection=selection();
		Object[]selected=selection.multiple();
		int arrayCount=tree.children().length,selectionCount=selected.length;
		boolean empty=arrayCount==0;
		TypedNode selectedNode=(TypedNode)selected[0];
		OffsetPath firstPath=((PathSelection)selection).paths[0];
		boolean valueSelected=firstPath instanceof NodePath
					&&((NodePath)firstPath).valueAt()>=0,
				belowRoot=selectedNode.parent()!=tree,nodeSelected=!valueSelected;
		return action==COPY?nodeSelected||valueSelected
				:action==MODIFY?valueSelected&&selectionCount==1
				:action==CUT||action==DELETE?belowRoot&&nodeSelected
	 			:action==PASTE?canPaste()&&!valueSelected
	 			:action==SELECT_ALL?false
				:super.actionIsLive(viewer,action);
	}
	@Override
	public String title(){
		String title=super.title();
		int dotAt=title.indexOf(".");
		return dotAt<0?title:title.substring(0,dotAt);
	}
	@Override
	public String toString(){
		return Debug.info(this);
	}
	@Override
	public boolean editSelection(){
		return new ValueEdit(((PathSelection)selection())){
			protected String getDialogInput(String title,String rubric,
					String proposal){
				return app.dialogs().getTextInput(title,rubric,proposal, 0);
			}
		}.dialogEdit();
	}
}
