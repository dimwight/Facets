package facets.core.app;
import static facets.util.Debug.*;
import static facets.util.Objects.*;
import static facets.util.tree.Nodes.*;
import static java.util.Arrays.*;
import facets.core.superficial.app.SSelection;
import facets.util.Debug;
import facets.util.OffsetPath;
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
abstract class NodePathEdit extends Tracer{
	static final class ValueCarrier extends StatefulCore{
		final Object value;
		ValueCarrier(TypedNode parent,Object value){
			super(parent.title());
			this.value=value;
		}
	}
	private static int ids=0;
	private final int id=++ids;
	protected final NodeViewable viewable;
	protected final Object selectionContent;
	protected final NodePath undonePaths[],parentPath;
	protected final boolean valueEdit;
	protected final Object[]undoneContents;
	protected final Integer[]contentAts;
	protected final int editAt;
	protected Object[]doneContents;
	private NodePath donePaths[];
	private final static boolean traceEdits=false;
	@Override
	protected void traceOutput(String msg){
		if(true)System.out.println("NodeEdit #"+id+ "["+toString()+"]"+msg);
	}
	private NodePathEdit(NodeViewable viewable){
		PathSelection selection=(PathSelection)(this.viewable=viewable).selection();
		selectionContent=selection.content();
		undonePaths=newTyped(NodePath.class,selection.paths);
		ArrayList<Integer>valueAts=new ArrayList();
		for(NodePath path:undonePaths){
			int valueAt=path.valueAt();
			if(valueAt>=0)valueAts.add(valueAt);
		}
		valueEdit=!valueAts.isEmpty();
		Object[]multiple=selection.multiple();
		TypedNode maybe=(TypedNode)multiple[0],
			parent=valueEdit?maybe:maybe.parent();
		undoneContents=parent.contents();
		parentPath=new NodePath(Nodes.ancestry(parent));
		ArrayList<Integer>contents=new ArrayList();
		for(int p=0;p<undonePaths.length;p++)
			contents.add(contentAt(undoneContents,(TypedNode)multiple[p],
					valueEdit?valueAts.get(p):-1));
		if(false&&contents.get(0)>contents.get(contents.size()-1))
			Collections.reverse(contents);
		contentAts=contents.toArray(new Integer[]{});
		editAt=contentAts[0];
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
		throw new IllegalStateException("Not found in "+arrayInfo(content));
	}
	final protected TypedNode parentPathTarget(){
		return(TypedNode)parentPath.target(selectionContent);
	}
	public void doEdit(){
		TypedNode parent=parentPathTarget();
		if(traceEdits)trace(".do: ",parentValue(parent));
		parent.setContents(doneContents!=null?doneContents:(doneContents=newDoneContents()));
		if(traceEdits)trace(".do~: ",parentValue(parent));
		NodePath[]paths=donePaths!=null?donePaths:(donePaths=newDonePaths());
		viewable.defineSelection(new PathSelection(selectionContent,paths));
	}
	public void undoEdit(){
		TypedNode parent=parentPathTarget();
		if(traceEdits)trace(".undo: ",parentValue(parent));
		parent.setContents(undoneContents);
		if(traceEdits)trace(".undo~: ",parentValue(parent));
		viewable.defineSelection(new PathSelection(selectionContent,undonePaths));
	}
	private Object[]parentValue(TypedNode parent){
		Object[]contents=parent.contents();
		return contents;
	}
	protected abstract Object[]newDoneContents();
	protected abstract NodePath[]newDonePaths();
	final protected void checkDoneContents(){
		if(doneContents==null)throw new IllegalStateException(
				"Null doneContents in "+this);
	}
	static NodePathEdit newInsertWithDelete(NodeViewable viewable,Object inserts,
			PathSelection deletePaths){
		return new NodePathEdit(viewable){
			private NodePathEdit delete,insert;
			private final static boolean doDelete=true;
			boolean first=true;
			private NodePath deletePath;
			@Override
			protected Object[]newDoneContents(){
				insert=newInsert(viewable,inserts,false);
				insert.doEdit();
				return insert.doneContents;
			}
			@Override
			public void doEdit(){
				deletePath=(NodePath)deletePaths.paths[0];
				boolean isSibling=deletePath.isSibling(undonePaths[0]);
				Object[]deletes=deletePath.members(selectionContent);
				int deleteAt=deletePath.valueAt();
				super.doEdit();
				PathSelection insertPaths=(PathSelection)viewable.selection();
				NodePath insertPath=(NodePath)insertPaths.paths[0];
				Object[]inserts=insertPath.members(selectionContent);
				int insertAt=insertPath.valueAt();
				if(first){
					if(isSibling){
						if(insertAt<deleteAt&&contentAt(doneContents,null,deleteAt)
								<doneContents.length-1)deleteAt++;
					}
					viewable.defineSelection(new PathSelection(selectionContent,
							new NodePath(deletes).valueAtChecked(deleteAt)));
					if(doDelete)delete=newDelete(viewable,true);
					first=false;
				}
				if(!doDelete)return;
				delete.doEdit();
				viewable.defineSelection(false?insertPaths:new PathSelection(selectionContent,
						new NodePath(inserts).valueAtChecked(insertAt)));
			}
			@Override
			public void undoEdit(){
				if(doDelete)delete.undoEdit();
				super.undoEdit();
			}
			@Override
			protected NodePath[]newDonePaths(){
				return insert.donePaths;
			}
			@Override
			public String toString(){
				return "Move";
			}
		};
	}
	static NodePathEdit newDelete(NodeViewable viewable,boolean asCut){
		return new NodePathEdit(viewable){
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
				checkDoneContents();
				int count=doneContents.length;
				TypedNode parent=parentPathTarget();
				if(count==0)return asPaths(new NodePath(ancestry(parent)));
				int selectAt=editAt<count?editAt:count-1;
				Object select=doneContents[selectAt];
				if(select instanceof TypedNode)
					return asPaths(new NodePath(ancestry((TypedNode)select)));
				int nodes=0;
				for(int at=selectAt;at>-1;at--)
					if(doneContents[at]instanceof TypedNode)nodes++;
				return asPaths(new NodePath(ancestry(parent)).valueAtChecked(selectAt-nodes));
			}
			private NodePath[]asPaths(NodePath single){
				return new NodePath[]{single};
			}
			@Override
			public String toString(){
				return asCut?"Cut":"Delete";
			}
		};
	}
	static NodePathEdit newInsert(NodeViewable viewable,Object insert,boolean into){
		final Object[]inserts=insert instanceof Object[]?(Object[])insert
				:new Object[]{insert},
			values=inserts[0]instanceof ValueCarrier?new Object[inserts.length]:null;
		for(int i=0;values!=null&&i<values.length;i++)
				values[i]=((ValueCarrier)inserts[i]).value;
		return!into?new NodePathEdit(viewable){
			@Override
			protected Object[]newDoneContents(){
				TypedNode parent=parentPathTarget();
				Object[]done=Arrays.copyOf(undoneContents,undoneContents.length);
				for(int i=inserts.length-1;i>=0;i--){
					Object doings[]=new Object[done.length+1],
						insert=inserts[i],value=values==null?null:values[i];
					for(int doneAt=0,nowAt=0;doneAt<doings.length;doneAt++,nowAt++)
						if(doneAt==editAt)nowAt--;
						else doings[doneAt]=done[nowAt];
					doings[editAt]=value!=null?value:insert;
					done=doings;
					if(value==null)((TypedNode)insert).setParent(parent);
				}
				return done;
			}
			@Override
			protected NodePath[]newDonePaths(){
				checkDoneContents();
				int nodes=0;
				for(int at=editAt;at>-1;at--)
					if(doneContents[at]instanceof TypedNode)nodes++;
				NodePath[]paths=new NodePath[inserts.length];
				for(int i=0;i<paths.length;i++){
					Object insert=doneContents[editAt+i];
					paths[i]=insert instanceof TypedNode?new NodePath(
							ancestry((TypedNode)insert))
							:parentPath.valueAtChecked(editAt+i-nodes);
				}
				return paths;
			}
			@Override
			public String toString(){
				return "Paste";
			}
		}
		:new NodePathEdit(viewable){
			@Override
			protected Object[]newDoneContents(){
				Object[]done=new Object[undoneContents.length];
				for(int i=0;i<done.length;i++){
					Object item=undoneContents[i];
					done[i]=item instanceof TypedNode?((TypedNode)item).copyState():item;
				}
				TypedNode editParent=(TypedNode)((Stateful)done[editAt]).copyState();
				done[editAt]=editParent;
				editParent.setContents(values!=null?values:inserts);
				return done;
			}
			@Override
			protected NodePath[]newDonePaths(){
				checkDoneContents();
				NodePath editPath=new NodePath(ancestry((TypedNode)doneContents[editAt])),
					paths[]=new NodePath[inserts.length];
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
	static NodePathEdit newModify(NodeViewable viewable,Object state){
		return new NodePathEdit(viewable){
			@Override
			protected Object[]newDoneContents(){
				TypedNode parent=parentPathTarget(),
						target=valueEdit?parent:(TypedNode)parent.contents()[editAt],
						editTarget=(TypedNode)new NodePath(ancestry(target)).target(state);
					Object[]done=new Object[undoneContents.length];
					for(int i=0;i<done.length;i++){
						Object item=undoneContents[i];
						done[i]=item instanceof TypedNode?((TypedNode)item).copyState():item;
					}
					done[editAt]=target==parent?editTarget.contents()[editAt]:editTarget;
					return done;
				}
			@Override
			protected NodePath[]newDonePaths(){
				return newTyped(NodePath.class,((PathSelection)viewable.selection()).paths);
			}
			@Override
			public String toString(){
				return "Modify";
			}
		};
	}
}
