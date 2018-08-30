package facets.core.app;
import static facets.core.app.ActionViewerTarget.Action.*;
import static facets.core.app.NodePathEdit.*;
import static facets.util.Objects.*;
import static facets.util.tree.Nodes.*;
import static java.util.Arrays.*;
import facets.core.superficial.SFrameTarget;
import facets.core.superficial.SIndexing;
import facets.core.superficial.STarget;
import facets.core.superficial.STextual;
import facets.core.superficial.app.SSelection;
import facets.util.Debug;
import facets.util.ItemList;
import facets.util.Objects;
import facets.util.OffsetPath;
import facets.util.Stateful;
import facets.util.Strings;
import facets.util.Util;
import facets.util.tree.DataConstants;
import facets.util.tree.DataNode;
import facets.util.tree.NodeList;
import facets.util.tree.NodePath;
import facets.util.tree.Nodes;
import facets.util.tree.TypedNode;
import facets.util.tree.ValueNode;
import facets.util.tree.XmlDocRoot;
import facets.util.tree.XmlPolicy;
import facets.util.tree.Nodes.TreeRoot;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;
/**
{@link StatefulViewable} for tree-type content. 
<p>{@link NodeViewable} extends its superclass by 
<ul><li>requiring its {@link #framed} content to be a {@link TypedNode}
<li>providing useful implementations based on {@link TypedNode}
of all superclass methods, enabling its direct use by application code
</ul> 
 */
public class NodeViewable extends StatefulViewable<TypedNode>{
	private int selections;
  public NodeViewable(TypedNode tree){
		this(tree,null);
	}
	public NodeViewable(final TypedNode tree,ClipperSource clipperSource){
	  super(newUseTitle(tree),tree,clipperSource);
	  if(tree.parent()!=null)throw new IllegalStateException(
				"Not a root in "+this);
	  if(true||clipperSource!=null)copyFramedState();
	  setSelection(PathSelection.newMinimal(tree));
	}
	private static String newUseTitle(TypedNode node){
	  String title=node.title();
	  return title!=null&&!title.equals("")?title:"Untitled "+node.type();
	}
	@Override
	protected void traceOutput(String msg){
		Util.printOut((true?title():(Debug.info(this)))+msg);
	}
	/**
	Default implementation for {@link TypedNode} tree. 
	 */
	@Override
	public SFrameTarget selectionFrame(){
	  final Object selection=selection().multiple()[0];
	  return false&&selection instanceof TypedNode?new NodeViewable((TypedNode)selection)
      :new SFrameTarget(title()+" #"+selections++,selection){
	  		@Override
  			protected STarget[]lazyElements(){
  				STarget valueTextual=new STextual("Value",selection.toString(),
  						new STextual.Coupler());
					return new STarget[]{valueTextual};
  			}
  		};
	}
	@Override
	protected STarget[]lazyElements(){
		TypedNode tree=tree();
		STarget valueTextual=new STextual("Node",tree
				+(true?"":(" descendants="+(Nodes.descendants(tree).length-1))),
			new STextual.Coupler());
		return new STarget[]{valueTextual};
	}
	/**
	Re-implements abstract method. 
	@param definition is cast to a {@link TypedNode}, a path to the content
	root constructed and from this a {@link PathSelection} which
	 is passed to {@link #setSelection(SSelection)} PLUS...
	 */
	@Override
	public SSelection defineSelection(Object definition){
		TypedNode tree=tree();
		if(definition instanceof PathSelection){
			OffsetPath[]srcPaths=((PathSelection)definition).paths,
				nodePaths=new OffsetPath[srcPaths.length];
			for(int i=0;i<nodePaths.length;i++)
				nodePaths[i]=new NodePath(srcPaths[i].offsets);
			return setSelection(new PathSelection(tree,nodePaths));
		}
		else if(definition==SELECT_ALL){
			TypedNode[]descendants=Nodes.descendants(tree);
			OffsetPath[]paths=new OffsetPath[descendants.length-1];
			for(int i=0;i<paths.length;i++)
				paths[i]=pathFromRoot(descendants[i+1]);
			return setSelection(new PathSelection(tree,paths));
		}
		TypedNode find=definition instanceof String?
				Nodes.descendantTitled(tree,(String)definition)
				:definition instanceof Integer?tree.children()[(Integer)definition]
				:(TypedNode)definition;
		if(false)trace(".defineSelection: ",find);
		List<TypedNode>descendants=Arrays.asList(Nodes.descendants(tree));
		if(find==null)find=tree;
		if(!descendants.contains(find))
			throw new IllegalStateException(Debug.info(definition)
					+" not selectable in "+Debug.info(this));
		for(TypedNode d:descendants)
			if(d==find){
				Object[]ancestry=Nodes.ancestry(find);
				setSelection(new PathSelection(ancestry[0],
						new OffsetPath[]{new NodePath(ancestry)}));
				break;
			}
		if(false)trace("~.defineSelection: ",selection());
		return selection();
	}
	@Override
	final public void iterateSelection(boolean forward){
		TypedNode tree=tree();
		TypedNode[]descendants=Nodes.descendants(tree);
		int markAt=-1,lastAt=descendants.length-1;
		Object selected[]=selection().multiple(),
			mark=selected[forward?selected.length-1:0];
		if(mark!=tree)for(int i=0;i<descendants.length;i++)
			if(descendants[i]==mark)markAt=i;
		markAt=markAt<1?forward?1:lastAt
				:forward?markAt==lastAt?1:markAt+1
				:markAt==1?lastAt:markAt-1;
		setSelection(new PathSelection(tree,pathFromRoot(descendants[markAt])));
	}
	private OffsetPath pathFromRoot(TypedNode node){
		return new NodePath(ancestry(node));
	}
	@Override
	protected SSelection newViewerSelection(SViewer viewer){
		SView view=viewer.view();
		if(view instanceof TreeView)return super.newViewerSelection(viewer);
		else throw new RuntimeException("Not implemented for "+Debug.info(view));
	}
	@Override
	protected void viewerSelectionChanged(SViewer viewer,SSelection selection){
		SSelection rootPaths=selection.content()instanceof SView?
				PathSelection.newMinimal(framed)
			:pathsFromRoot((PathSelection)selection);
		setSelection(rootPaths);
	}
	private PathSelection pathsFromRoot(PathSelection selection){
		OffsetPath[]rootPaths=new OffsetPath[selection.paths.length];
		Object content=selection.content();
		for(int i=0;i<rootPaths.length;i++){
			OffsetPath selectionPath=selection.paths[i];
			if(selectionPath==OffsetPath.empty){
				rootPaths[i]=selectionPath;
				continue;
			}
			NodePath nodePath=new NodePath(ancestry((TypedNode)selectionPath.target(content)));
			if(selectionPath instanceof NodePath)
				nodePath=nodePath.valueAtChecked(((NodePath)selectionPath).valueAt());
			rootPaths[i]=nodePath;
		}
		return new PathSelection(framed,rootPaths);
	}
	@Override
	public boolean actionIsLive(SViewer viewer,ViewableAction action){
		TypedNode tree=tree();
		Object[]selected=selection().multiple();
		int arrayCount=tree.children().length,selectionCount=selected.length;
		boolean empty=arrayCount==0,allSelected=arrayCount==selectionCount,
			iterate=action==ITERATE_FORWARD||action==ITERATE_BACK;
		if(iterate)return!empty;
		TypedNode selectedNode=(TypedNode)selected[0];
		OffsetPath firstPath=((PathSelection)selection()).paths[0];
		if(!(firstPath instanceof NodePath))return iterate;
		int valueAt=((NodePath)firstPath).valueAt();
		boolean belowRoot=tree!=selectedNode,nodeSelected=true||valueAt<0;
		return action==COPY?nodeSelected
			:action==MODIFY?belowRoot&&nodeSelected&&selectionCount==1
			:action==CUT||action==DELETE?belowRoot&&nodeSelected
 			:action==PASTE?canPaste()
 			:action==PASTE_INTO?canPaste()&&valueAt<0
 					&&selectedNode.contents().length==0&&selectionCount==1
			:action==SELECT_ALL?!empty&&!allSelected
			:super.actionIsLive(viewer,action);
	}
	@Override
	public void copyStatefulSelection(){
		PathSelection selection=(PathSelection)selection();
		Object content=selection.content();
		NodePath[]paths=newTyped(NodePath.class,selection.paths);
		if(false)trace(".: undonePaths=",paths);
		Arrays.sort(paths,new Comparator<NodePath>(){
			@Override
			public int compare(NodePath p1,NodePath p2){
				int value1=p1.valueAt();
				if(value1>=0)return value1-p2.valueAt();
				TypedNode node1=(TypedNode)p1.target(content),
					node2=(TypedNode)p2.target(content);
				boolean found1=false,found2=false;
				for(TypedNode child:node1.parent().children()){
					found1|=child==node1;
					found2|=child==node2;
					if(found1&&!found2)return -1;
				}
				return 1;
			}
		});
		if(false)trace(".: ~undonePaths=",paths);
		setSelection(new PathSelection(content,paths));
		super.copyStatefulSelection();
	}
	final public void insertWithDelete(Stateful[]inserts,SSelection deleteAt){
		doEdit(newInsertWithDelete(this,inserts,(PathSelection)deleteAt),true);
		updateAfterEditAction();
	}
	@Override
	final public void insertStatefuls(boolean into,Stateful...statefuls){
		doEdit(newInsert(this,statefuls,into),true);
	}
	@Override
	final public void deleteSelection(boolean asCut){
		doEdit(newDelete(this,asCut),true);
	}
	final protected boolean maybeModify(){
		boolean edited=editSelection();
		if(!edited)return false;
		Stateful state=((Stateful)framed).copyState();
		restoreAfterEditAction();
		doEdit(newModify(this,state),true);
		if(false)updateAfterEditAction();
		return true;
	}
	private void doEdit(NodePathEdit edit, boolean significant){
		edit.doEdit();
		states.addEdit(new AddEdit(edit,significant){});
	}
	private class AddEdit implements UndoableEdit{
		private final boolean significant;
		private final NodePathEdit edit;
		AddEdit(NodePathEdit edit, boolean significant){
			this.edit=edit;
			this.significant=significant;
		}
		@Override
		public void undo()throws CannotUndoException{
			edit.undoEdit();
			NodeViewable.this.editUndoneOrRedone();
		}
		@Override
		public void redo()throws CannotRedoException{
			edit.doEdit();
			NodeViewable.this.editUndoneOrRedone();
		}
		@Override
		public String getPresentationName(){
			return edit.toString();
		}
		@Override
		public boolean replaceEdit(UndoableEdit anEdit){
			return false;
		}
		@Override
		public boolean isSignificant(){
			return significant;
		}
		@Override
		public String getUndoPresentationName(){
			return "Undo "+getPresentationName();
			}
		@Override
		public String getRedoPresentationName(){
			return "Redo "+getPresentationName();
		}
		@Override
		public void die(){}
		@Override
		public boolean canUndo(){
			return true;
		}
		@Override
		public boolean canRedo(){
			return true;
		}
		@Override
		public boolean addEdit(UndoableEdit anEdit){
			return false;
		}
	}
	final public TypedNode tree(){
		return(TypedNode)framed;
	}
	protected void editUndoneOrRedone(){}
	final public void readSelectionState(ValueNode state,String key){
		setSelection(PathSelection.getOffsetSelection(framed,state,key));
	}
	final public void putSelectionState(final ValueNode state,String key){
		PathSelection.putSelectionOffsets((PathSelection)selection(),state,key);
	}
	@Override
	final public boolean textSeemsPastable(String text){
		return text.startsWith(XmlDocRoot.SHEBANG);
	}
	/**
	Re-implementation. 
	<p>Matches based on {@link TypedNode#type()} and {@link TypedNode#title()}.	
	 */
	@Override
	final protected Stateful[]matchSelectionEdits(Stateful[]targets,Stateful[]edits){
		Stateful[]matches=new Stateful[targets.length];
		for(int m=0;m<matches.length;m++){
			for(int i=0;i<edits.length;i++){
				TypedNode edit=(TypedNode)edits[i],target=((TypedNode)targets[m]);
				if(edit.type().equals(target.type())
						&&edit.title().equals(target.title())) 
								matches[m]=edits[i];
			}
			if(matches[m]==null)return null;
		}
		if(false)trace(".matchSelectionEdits: ",edits);
		return matches;
	}
}
