package facets.core.app;
import static facets.util.tree.Nodes.*;
import facets.util.OffsetPath;
import facets.util.Stateful;
import facets.util.StatefulCore;
import facets.util.Tracer;
import facets.util.tree.DataNode;
import facets.util.tree.NodePath;
import facets.util.tree.TypedNode;
import java.util.Arrays;
abstract class NodeUndoableEdit extends Tracer{
	static final class ValueCarrier extends StatefulCore{
		final Object value;
		ValueCarrier(TypedNode parent,Object value){
			super(parent.title());
			this.value=value;
		}
	}
	protected final int doAt;
	protected final int valueAt;
	protected final TypedNode parent;
	private final NodeViewable viewable;
	private final Object selectionContent;
	private final OffsetPath openingPath;
	protected final Object[]undoneContents,doneContents;
	NodeUndoableEdit(NodeViewable viewable){
		PathSelection selection=(PathSelection)(this.viewable=viewable).selection();
		selectionContent=selection.content();
		openingPath=selection.paths[0];
		TypedNode target=(TypedNode)openingPath.target(selectionContent);
		valueAt=((NodePath)openingPath).valueAt();
		parent=valueAt<0?target.parent():target;
		doAt=contentAt(parent.contents(),target,valueAt);
		undoneContents=parent.contents();
		doneContents=newDoneContent();
	}
	static int contentAt(Object[]content,TypedNode target,int valueAt){
		int contentAt=-1;
		for(int at=0,nodes=0;at<content.length;at++){
			if(content[at]instanceof TypedNode){
				nodes++;
				if(content[at]==target)contentAt=at;
			}
			else if(valueAt>=0&&at-nodes==valueAt)contentAt=at;
			if(contentAt>=0)return contentAt;
		}
		throw new IllegalStateException("Not found in "+content);
	}
	protected abstract Object[]newDoneContent();
	protected abstract OffsetPath newDonePath();
	final void doEdit(){
		traceDebug(".doEdit: parent=",parent.contents());
		parent.setContents(doneContents);
		traceDebug(".doEdit~: parent=",parent.contents());
		viewable.defineSelection(new PathSelection(selectionContent,newDonePath()));
	}
	final void undoEdit(){
		traceDebug(".undoEdit: parent=",parent.contents());
		parent.setContents(undoneContents);
		traceDebug(".undoEdit~: parent=",parent.contents());
		viewable.defineSelection(new PathSelection(selectionContent,openingPath));
	}
	static NodeUndoableEdit newModify(NodeViewable viewable,Object state){
		return new NodeUndoableEdit(viewable){
			@Override
			protected Object[]newDoneContent(){
				TypedNode target=valueAt>=0?parent:(TypedNode)parent.contents()[doAt],
					stateTarget=(TypedNode)new NodePath(ancestry(target)).target(state);
				Object[]done=Arrays.copyOf(undoneContents,undoneContents.length);
				done[doAt]=target==parent?stateTarget.contents()[doAt]:stateTarget;
				return done;
			}
			@Override
			protected OffsetPath newDonePath(){
				return((PathSelection)viewable.selection()).paths[0];
			}
		};
	}
	static NodeUndoableEdit newDelete(NodeViewable viewable,boolean asCut){
		return new NodeUndoableEdit(viewable){
			@Override
			protected Object[]newDoneContent(){
				Object[]done=new Object[undoneContents.length-1];
				for(int doneAt=0,nowAt=0;doneAt<done.length;doneAt++,nowAt++){
					if(doneAt==doAt)nowAt++;
					done[doneAt]=undoneContents[nowAt];
				}
				return done;
			}
			@Override
			protected NodePath newDonePath(){
				int selectAt=doAt<doneContents.length?doAt:doAt-1;
				if(doneContents.length==0)return new NodePath(ancestry(parent));
				Object select=doneContents[selectAt];
				if(select instanceof TypedNode)
					return new NodePath(ancestry((TypedNode)select));
				int nodes=0;
				for(int at=selectAt;at>-1;at--)
					if(doneContents[at]instanceof TypedNode)nodes++;
				return new NodePath(ancestry(parent)).valueAtChecked(selectAt-nodes);
			}
			@Override
			public String toString(){
				return asCut?"Cut":"Delete";
			}
		};
	}
	static NodeUndoableEdit newInsert(NodeViewable viewable,Stateful insert,
			boolean into){
		final Object insertValue=
				insert instanceof ValueCarrier?((ValueCarrier)insert).value:null;
		return!into?new NodeUndoableEdit(viewable){
			@Override
			protected Object[] newDoneContent(){
				Object[]done=new Object[undoneContents.length+1];
				for(int doneAt=0,nowAt=0;doneAt<done.length;doneAt++,nowAt++)
					if(doneAt==doAt)nowAt--;
					else done[doneAt]=undoneContents[nowAt];
				done[doAt]=insertValue!=null?insertValue:insert;
				if(insertValue==null)((TypedNode)insert).setParent(parent);
				return done;
			}
			@Override
			protected NodePath newDonePath(){
				Object select=doneContents[doAt];
				if(select instanceof TypedNode)
					return new NodePath(ancestry((TypedNode)select));
				int nodes=0;
				for(int at=doAt;at>-1;at--)
					if(doneContents[at]instanceof TypedNode)nodes++;
				return new NodePath(ancestry(parent)).valueAtChecked(doAt-nodes);
			}
			@Override
			public String toString(){
				return "Paste";
			}
		}
		:new NodeUndoableEdit(viewable){
			@Override
			protected Object[]newDoneContent(){
				TypedNode newParent=(TypedNode)((Stateful)undoneContents[doAt]).copyState();
				undoneContents[doAt]=newParent;
				if(insertValue!=null)
					newParent.setContents(new Object[]{insertValue});
				else appendChild((DataNode)newParent,(TypedNode)insert);
				return undoneContents;
			}
			@Override
			protected OffsetPath newDonePath(){
				TypedNode newParent=(TypedNode)doneContents[doAt];
				if(insertValue==null){
					TypedNode[]children=newParent.children();
					return new NodePath(ancestry(children[children.length-1]));
				}
				else return(new NodePath(ancestry(newParent)).valueAtChecked(0));
			}
			@Override
			public String toString(){
				return "Paste Into";
			}
		};
	}
}
