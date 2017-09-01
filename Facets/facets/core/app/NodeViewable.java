package facets.core.app;
import static facets.core.app.ActionViewerTarget.Action.*;
import static facets.util.Objects.*;
import static facets.util.tree.Nodes.*;
import static java.util.Arrays.*;
import facets.core.superficial.SFrameTarget;
import facets.core.superficial.SIndexing;
import facets.core.superficial.STarget;
import facets.core.superficial.STextual;
import facets.core.superficial.app.SSelection;
import facets.core.superficial.app.SView;
import facets.core.superficial.app.SViewer;
import facets.core.superficial.app.ViewableAction;
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
import java.util.List;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
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
	public final SIndexing indexing;
	private int selections;
  private static String newUseTitle(TypedNode node){
    String title=node.title();
    return title!=null&&!title.equals("")?title:"Untitled "+node.type();
  }
	public NodeViewable(TypedNode tree){
		this(tree,null);
	}
	public NodeViewable(final TypedNode tree,ClipperSource clipperSource){
	  super(newUseTitle(tree),tree,clipperSource);
	  indexing=new SIndexing(title(),new SIndexing.Coupler(){
	  	@Override
	  	public Object[]getIndexables(){
	  		return tree.children();
	  	}
	  	@Override
	  	public void indexSet(SIndexing i){
	  		defineSelection(i.indexed());
	  	}
	  	@Override
	  	public boolean canCycle(SIndexing i){
	  		return true;
	  	}
	  });
	  if(true||clipperSource!=null)copyFramedState();
	  setSelection(PathSelection.newMinimal(tree));
	}
	final public TypedNode tree(){
		return(TypedNode)framed;
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
	protected void viewerSelectionChanged(SViewer viewer,SSelection selection){
		SSelection rootPaths=selection.content()instanceof SView?
				PathSelection.newMinimal(framed)
			:pathsFromRoot((PathSelection)selection);
		setSelection(rootPaths);
	}
	/**
	Converts viewer selections to full paths. 
	<p>Traces the path from the <code>indexed</code> of each path in 
	<code>selection</code> to the content root.  
	@param selection to be converted
	 */
	final protected PathSelection pathsFromRoot(PathSelection selection){
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
		return action==COPY||action==CUT?belowRoot&&nodeSelected
			:action==MODIFY||action==DELETE?belowRoot||!nodeSelected
 			:action==PASTE?canPaste()
 			:action==PASTE_INTO?canPaste()&&valueAt<0
 					&&selectedNode.contents().length==0
			:action==SELECT_ALL?!empty&&!allSelected
			:super.actionIsLive(viewer,action);
	}
	@Override
	public void iterateSelection(boolean forward){
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
	@Override
	public void insertStatefuls(boolean insertInto,Stateful...statefuls){
		if(undoableEdits){
			doUndoableEdit(NodeUndoableEdit.newInsert(this,statefuls[0],insertInto));
			return;
		}
		PathSelection paths=(PathSelection)selection();
		NodePath firstPath=(NodePath)paths.paths[0];
		int valueAt=firstPath.valueAt();
		TypedNode inserts[]=newTyped(TypedNode.class,statefuls),
			first=(TypedNode)selection().multiple()[0],root=tree();
		NodeList into=new NodeList(valueAt>=0||first==root||insertInto?first
				:first.parent(),false);
		int firstAt=valueAt>=0?valueAt:into.indexOf(first);
		into.addAll(firstAt>=0?firstAt:0,inserts);
		if(!hasMixedContents(into.parent))into.updateParent();
		else into.updateMixedParent(first);
		setSelection(newNodeChangeSelection(root,inserts,false));
	}
	@Override
	protected void setModifyState(Stateful state){
		if(!undoableEdits)super.setModifyState(state);
		else doUndoableEdit(NodeUndoableEdit.newModify(this,state));
	}
	@Override
	public void deleteSelection(boolean asCut){
		if(undoableEdits){
			doUndoableEdit(NodeUndoableEdit.newDelete(this,asCut));
			return;
		}
		PathSelection paths=(PathSelection)selection();
		NodePath firstPath=(NodePath)paths.paths[0];
		int valueAt=firstPath.valueAt();
		if(valueAt>=0){
			ValueNode node=(ValueNode)firstPath.target(framed);
			int valueCount=node.values().length;
			node.deleteValueAt(valueAt);
			paths.paths[0]=firstPath.valueAtChecked(
					valueCount==1?-1:valueAt==valueCount-1?valueAt-1:valueAt);
			return;
		}
		TypedNode[]deletes=newTyped(TypedNode.class,paths.multiple());
		List<TypedNode>reselects=new ArrayList();
		for(TypedNode delete:deletes){
			TypedNode parent=delete.parent();
			NodeList keeps=new NodeList(parent,false);
			keeps.clear();
			TypedNode[]thenChildren=parent.children();
			int thenAt=-1,nowAt=-1,thenLastAt=thenChildren.length-1;
			for(TypedNode thenChild:thenChildren){
				nowAt=++thenAt==thenLastAt?thenAt-1:thenAt+1;
				boolean keep=true;
				for(TypedNode check:deletes)keep&=(check!=thenChild);
				if(keep)keeps.add(thenChild);
				else reselects.add(thenChildren.length==1?parent:thenChildren[nowAt]);
			}
			if(!hasMixedContents(parent))keeps.updateParent();
			else keeps.updateMixedParent(null);
		}
		if(reselects.size()!=deletes.length)throw new IllegalStateException(
				"Selection lengths don't match "+Debug.info(this));
		setSelection(newNodeChangeSelection(tree(),
				reselects.toArray(new TypedNode[]{}),true));
	}
	private void doUndoableEdit(final NodeUndoableEdit edit){
		edit.doEdit();
		states.addEdit(new UndoableEdit(){
			@Override
			public void undo()throws CannotUndoException{
				edit.undoEdit();
			}
			@Override
			public boolean replaceEdit(UndoableEdit anEdit){
				return false;
			}
			@Override
			public void redo() throws CannotRedoException{
				edit.doEdit();
			}
			@Override
			public boolean isSignificant(){
				return true;
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
			public String getPresentationName(){
				return edit.toString();
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
		});
	}
	final protected PathSelection newNodeChangeSelection(TypedNode root, 
			TypedNode[]nodes,boolean removing){
		OffsetPath[]paths;
		if(false&&removing)
			paths=new OffsetPath[]{pathFromRoot(root)};
		else{
			paths=new OffsetPath[nodes.length];
			for(int i=0;i<paths.length;i++)paths[i]=pathFromRoot(nodes[i]);
		}
		return new PathSelection(root,paths);
	}
	@Override
	public boolean textSeemsPastable(String text){
		return text.startsWith(XmlDocRoot.SHEBANG);
	}
	protected TypedNode[]newPasteTextStatefuls(String text){
		TreeRoot copyWrap=new XmlDocRoot(new DataNode(DataConstants.TYPE_XML,title(),
						new TypedNode[]{new ValueNode("Paste",title())}),new XmlPolicy());  
		try{
			copyWrap.readFromSource(Strings.stringLines(text));
		}catch(IOException e){
			throw new RuntimeException(e);
		}
		return copyWrap.tree.children()[0].children();
	}
	/**
	Re-implementation. 
	<p>Matches based on {@link TypedNode#type()} and {@link TypedNode#title()}.	
	 */
	@Override
	protected Stateful[]matchSelectionEdits(Stateful[]targets,Stateful[]edits){
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
	private OffsetPath pathFromRoot(TypedNode node){
		return new NodePath(ancestry(node));
	}
	final public void readSelectionState(ValueNode state,String key){
		setSelection(PathSelection.getOffsetSelection(framed,state,key));
	}
	final public void putSelectionState(final ValueNode state,String key){
		PathSelection.putSelectionOffsets((PathSelection)selection(),state,key);
	}
	@Override
	protected void traceOutput(String msg){
		Util.printOut((true?title():(Debug.info(this)))+msg);
	}
	@Override
	protected SSelection newViewerSelection(SViewer viewer){
		SView view=viewer.view();
		if(view instanceof TreeView)return super.newViewerSelection(viewer);
		else throw new RuntimeException("Not implemented for "+Debug.info(view));
	}
}
