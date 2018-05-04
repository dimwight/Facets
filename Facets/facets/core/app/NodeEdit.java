package facets.core.app;
import static facets.util.Objects.*;
import static facets.util.tree.Nodes.*;
import static java.util.Arrays.*;
import facets.core.superficial.app.SSelection;
import facets.util.Debug;
import facets.util.Stateful;
import facets.util.StatefulCore;
import facets.util.Tracer;
import facets.util.Util;
import facets.util.tree.NodePath;
import facets.util.tree.Nodes;
import facets.util.tree.TypedNode;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
abstract class NodeEdit extends Tracer{
	static final class ValueCarrier extends StatefulCore{
		final Object value;
		ValueCarrier(TypedNode parent,Object value){
			super(parent.title());
			this.value=value;
		}
	}
	protected final int startAt;
	protected final Integer[]valueAts,contentAts;
	protected final NodeViewable viewable;
	private final Object selectionContent;
	protected final NodePath undonePaths[],parentPath;
	private NodePath donePaths[];
	protected Object[]undoneContents,doneContents;
	private final boolean traceDebug=false;
	@Override
	protected void traceOutput(String msg){
		if(true)super.traceOutput(msg);
	}
	NodeEdit(NodeViewable viewable){
		PathSelection selection=(PathSelection)(this.viewable=viewable).selection();
		selectionContent=selection.content();
		undonePaths=newTyped(NodePath.class,selection.paths);
		ArrayList<Integer>values=new ArrayList();
		for(NodePath path:undonePaths){
			int valueAt=path.valueAt();
			if(valueAt>=0)values.add(valueAt);
		}
		valueAts=values.isEmpty()?null:values.toArray(new Integer[]{});
		Object[]multiple=selection.multiple();
		TypedNode maybe=(TypedNode)multiple[0],
			parent=valueAts==null?maybe.parent():maybe;
		undoneContents=parent.contents();
		parentPath=new NodePath(Nodes.ancestry(parent));
		ArrayList<Integer>contents=new ArrayList();
		for(int p=0;p<undonePaths.length;p++)
			contents.add(contentAt(undoneContents,(TypedNode)multiple[p],
					valueAts!=null?valueAts[p]:-1));
		if(contents.get(0)>contents.get(contents.size()-1))
			Collections.reverse(contents);
		contentAts=contents.toArray(new Integer[]{});
		if(false)trace(".: contentAts=",contentAts);
		startAt=contentAts[0];
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
	static NodePath[]asPaths(NodePath single){
		return new NodePath[]{single};
	}
	TypedNode parentFromPath(){
		return (TypedNode)parentPath.target(selectionContent);
	}
	final void doEdit(){
		TypedNode parent=parentFromPath();
		if(traceDebug)traceDebug(".doEdit: contents=",parent.contents());
		parent.setContents(doneContents=newDoneContents());
		if(traceDebug)traceDebug(".doEdit~: contents=",parent.contents());
		NodePath[]paths=donePaths!=null?donePaths:(donePaths=newDonePaths());
		viewable.defineSelection(new PathSelection(selectionContent,paths));
	}
	final void undoEdit(){
		TypedNode parent=parentFromPath();
		if(traceDebug)traceDebug(".undoEdit: contents=",parent.contents());
		parent.setContents(undoneContents);
		if(traceDebug)traceDebug(".undoEdit~: contents=",parent.contents());
		viewable.defineSelection(new PathSelection(selectionContent,undonePaths));
	}
	protected void testDoneContents(){
		if(doneContents==null)throw new IllegalStateException(
				"Null doneContents in "+this);
	}
	protected abstract Object[]newDoneContents();
	protected abstract NodePath[]newDonePaths();
	static NodeEdit newInsert(NodeViewable viewable,Object insert,
			boolean into){
		final Object[]inserts=insert instanceof Object[]?(Object[])insert
				:new Object[]{insert},
			values=inserts[0]instanceof ValueCarrier?new Object[inserts.length]
				:null;
		for(int i=0;values!=null&&i<values.length;i++)
				values[i]=((ValueCarrier)inserts[i]).value;
		return!into?new NodeEdit(viewable){
			@Override
			protected Object[]newDoneContents(){
				TypedNode parent=parentFromPath();
				Object[]done=Arrays.copyOf(undoneContents,undoneContents.length);
				for(int i=inserts.length-1;i>=0;i--){
					Object doings[]=new Object[done.length+1],
						insert=inserts[i],value=values==null?null:values[i];
					for(int doneAt=0,nowAt=0;doneAt<doings.length;doneAt++,nowAt++)
						if(doneAt==startAt)nowAt--;
						else doings[doneAt]=done[nowAt];
					doings[startAt]=value!=null?value:insert;
					done=doings;
					if(value==null)((TypedNode)insert).setParent(parent);
				}
				return done;
			}
			@Override
			protected NodePath[]newDonePaths(){
				testDoneContents();
				int nodes=0;
				for(int at=startAt;at>-1;at--)
					if(doneContents[at]instanceof TypedNode)nodes++;
				NodePath[]paths=new NodePath[inserts.length];
				for(int i=0;i<paths.length;i++){
					Object insert=doneContents[startAt+i];
					paths[i]=insert instanceof TypedNode?new NodePath(
							ancestry((TypedNode)insert))
							:parentPath.valueAtChecked(startAt+i-nodes);
				}
				return paths;
			}
			@Override
			public String toString(){
				return "Paste";
			}
		}
		:new NodeEdit(viewable){
			@Override
			protected Object[]newDoneContents(){
				Object[]done=new Object[undoneContents.length];
				for(int i=0;i<done.length;i++){
					Object item=undoneContents[i];
					done[i]=item instanceof TypedNode?((TypedNode)item).copyState():item;
				}
				TypedNode editParent=(TypedNode)((Stateful)done[startAt]).copyState();
				done[startAt]=editParent;
				editParent.setContents(values!=null?values:inserts);
				return done;
			}
			@Override
			protected NodePath[]newDonePaths(){
				testDoneContents();
				NodePath editPath=new NodePath(ancestry((TypedNode)doneContents[startAt]));
				NodePath[]paths=new NodePath[inserts.length];
				for(int i=0;i<paths.length;i++){
					Object insert=inserts[i];
					paths[i]=insert instanceof TypedNode?
							new NodePath(ancestry((TypedNode)insert)):editPath.valueAtChecked(i);
				}
				return paths;
			}
			@Override
			public String toString(){
				return "Paste Into";
			}
		};
	}
	static NodeEdit newDelete(NodeViewable viewable,boolean asCut){
		return new NodeEdit(viewable){
			@Override
			protected Object[]newDoneContents(){
				Object[]done=Arrays.copyOf(undoneContents,undoneContents.length);
				for(int i=contentAts.length-1;i>=0;i--){
					Object[]doings=new Object[done.length-1];
					for(int doneAt=0,nowAt=0;doneAt<doings.length;doneAt++,nowAt++){
						if(doneAt==contentAts[i])nowAt++;
						doings[doneAt]=done[nowAt];
					}
					done=doings;
				}
				return done;
			}
			@Override
			protected NodePath[]newDonePaths(){
				testDoneContents();
				int count=doneContents.length;
				TypedNode parent=parentFromPath();
				if(count==0)return asPaths(new NodePath(ancestry(parent)));
				int selectAt=startAt<count?startAt:count-1;
				Object select=doneContents[selectAt];
				if(select instanceof TypedNode)
					return asPaths(new NodePath(ancestry((TypedNode)select)));
				int nodes=0;
				for(int at=selectAt;at>-1;at--)
					if(doneContents[at]instanceof TypedNode)nodes++;
				return asPaths(new NodePath(ancestry(parent)).valueAtChecked(selectAt-nodes));
			}
			@Override
			public String toString(){
				return (asCut?"Cut":"Delete")+" Multiple";
			}
		};
	}
	static NodeEdit newModify(NodeViewable viewable,Object state){
		return new NodeEdit(viewable){
			@Override
			protected Object[]newDoneContents(){
				TypedNode parent=parentFromPath(),
						target=valueAts!=null&&valueAts.length>0?parent
								:(TypedNode)parent.contents()[startAt],
						stateTarget=(TypedNode)new NodePath(ancestry(target)).target(state);
					Object[]done=new Object[undoneContents.length];
					for(int i=0;i<done.length;i++){
						Object item=undoneContents[i];
						done[i]=item instanceof TypedNode?((TypedNode)item).copyState():item;
					}
					done[startAt]=target==parent?stateTarget.contents()[startAt]:stateTarget;
					return done;
				}
			@Override
			protected NodePath[]newDonePaths(){
				return newTyped(NodePath.class,((PathSelection)viewable.selection()).paths);
			}
		};
	}
}
