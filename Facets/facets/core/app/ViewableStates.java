package facets.core.app;
import facets.core.superficial.app.SSelection;
import facets.core.superficial.app.ViewableFrame;
import facets.util.Debug;
import facets.util.Stateful;
import facets.util.Tracer;
import facets.util.Util;

import java.util.Hashtable;
import javax.swing.undo.StateEdit;
import javax.swing.undo.StateEditable;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
final class ViewableStates extends Tracer implements StateEditable{
	private final static class State{
	  final Stateful content,state;
		State(Stateful content,Stateful state){
			this.content=content;
			this.state=state;
		}
		public String toString(){
			return //"source="+Debug.info(source)+", copy="+
			state +"";
		}
	}
	static final class Change{
		private final Stateful content,beforeState,afterState;
		Change(Stateful content,Stateful beforeState,Stateful afterState){
			this.content=content;
			this.beforeState=beforeState;
			this.afterState=afterState;
			if(false)Util.printOut("Change.Change: ",beforeState+"->"+afterState);
		}
		State newBeforeState(){
			return new State(content,beforeState);
		}
		State newAfterState(){
			return new State(content,afterState);
		}
		public String toString(){
			return Debug.info(this)+" "+Debug.info(beforeState)+">"+Debug.info(afterState);
		}
	}
	private class StateSet{
		private State[]storePairs;
		private SSelection paths;
		StateSet(){
			if(false)trace(".StateSet: ",viewable.selection());
			paths=(SSelection)Util.deserializedCopy(viewable.selection());
		}
		void storeBefores(Change[]changes){
			storePairs=new State[changes.length];
			for(int i=0;i<storePairs.length;i++)
				storePairs[i]=changes[i].newBeforeState();
		}
		void storeAfters(Change[]changes){
			storePairs=new State[changes.length];
			for(int i=0;i<storePairs.length;i++)
				storePairs[i]=changes[i].newAfterState();
		}
		void restore(){
			for(int i=0;i<storePairs.length;i++)
				viewable.restoreState(storePairs[i].content,storePairs[i].state);
			PathSelection reconstructed=new PathSelection(viewable.framed,
					((PathSelection)paths).paths);
			if(false)trace(".restore: ",reconstructed);
			viewable.defineSelection(reconstructed);
		}
		public String toString(){
			return storePairs[0]+"";
		}
		Stateful[]stateCopies(){
			Stateful[]copies=new Stateful[storePairs.length];
			for(int i=0;i<copies.length;i++)copies[i]=storePairs[i].state;
			return copies;
		}
	}
	private final class Edit extends StateEdit{
		private boolean absorbed;
		Edit(StateEditable object){super(object);}
		public boolean addEdit(UndoableEdit edit){
			Edit e=(Edit)edit;
			boolean add=viewable.canMergeEdits(after().stateCopies(),
					e.after().stateCopies());
			if(false)trace(this+"\naddEdit: "+add+" "+e.after());
			if(add)postState.put(object,e.after());
			return e.absorbed=add;
		}
		public String toString(){
			return Debug.info(this)+":\nbefore="+before()+"\nafter="+after();
		}
		StateSet before(){return(StateSet)preState.get(object);}
		StateSet after(){return(StateSet)postState.get(object);}
		public boolean canUndo(){return!absorbed;}
		public boolean canRedo(){return!absorbed;}
	}
  final private UndoManager undoer=new UndoManager();
  final private StatefulViewable viewable;  
	private StateSet stateSet;
	ViewableStates(StatefulViewable viewable){
		this.viewable=viewable;
	}
	public void storeState(Hashtable states){
		states.put(this,stateSet);
		if(false)trace(".store:\n",stateSet);
	}
	public void restoreState(Hashtable states){
		((StateSet)states.get(this)).restore();
	}
	void storeChanges(Change[]changes){  
		if(false)trace(".storeChanges: ",changes);
		stateSet=new StateSet();
		stateSet.storeBefores(changes);
	  StateEdit stateEdit=new Edit(this);
		stateSet=new StateSet();
		stateSet.storeAfters(changes);
	  stateEdit.end();
	  undoer.addEdit(stateEdit);
	}
	boolean canUndo(){return undoer.canUndo();}
	boolean canRedo(){return undoer.canRedo();}
	void undo(){undoer.undo();}
	void redo(){undoer.redo();}
}
