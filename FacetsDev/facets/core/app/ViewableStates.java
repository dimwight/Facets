package facets.core.app;
import facets.core.superficial.app.SSelection;
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
	private final static class ContentState{
	  final Stateful content,state;
		ContentState(Stateful content,Stateful state){
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
		ContentState newBeforeState(){
			return new ContentState(content,beforeState);
		}
		ContentState newAfterState(){
			return new ContentState(content,afterState);
		}
		public String toString(){
			return Debug.info(this)+" "+Debug.info(beforeState)+">"+Debug.info(afterState);
		}
	}
	private class StateSet{
		private ContentState[]storePairs;
		private SSelection paths;
		StateSet(){
			if(false)trace(".StateSet: ",viewable.selection());
			paths=(SSelection)Util.deserializedCopy(viewable.selection());
		}
		void storeBefores(Change[]changes){
			storePairs=new ContentState[changes.length];
			for(int i=0;i<storePairs.length;i++)
				storePairs[i]=changes[i].newBeforeState();
		}
		void storeAfters(Change[]changes){
			storePairs=new ContentState[changes.length];
			for(int i=0;i<storePairs.length;i++)
				storePairs[i]=changes[i].newAfterState();
		}
		void restoreViewableStateAndSelection(){
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
		private boolean added;
		Edit(StateEditable object){super(object);}
		public boolean addEdit(UndoableEdit edit){
			Edit e=(Edit)edit;
			StateSet eAfter=e.after();
			boolean eAddable=viewable.canMergeEdits(this.after().stateCopies(),
					eAfter.stateCopies());
			if(false)trace(this+"\naddEdit: "+eAddable+" "+eAfter);
			if(eAddable)postState.put(object,eAfter);
			return e.added=eAddable;
		}
		public String toString(){
			StateSet before=(StateSet)preState.get(object);
			return Debug.info(this)+":\nbefore="+before+"\nafter="+after();
		}
		StateSet after(){return(StateSet)postState.get(object);}
		public boolean canUndo(){return!added;}
		public boolean canRedo(){return!added;}
	}
  final private UndoManager undoer=new UndoManager();
  final private StatefulViewable viewable;  
	private StateSet stateSet;
	ViewableStates(StatefulViewable viewable){
		this.viewable=viewable;
	}
	public void storeState(Hashtable store){
		store.put(this,stateSet);
		if(false)trace(".store:\n",stateSet);
	}
	public void restoreState(Hashtable store){
		((StateSet)store.get(this)).restoreViewableStateAndSelection();
	}
	void storeViewableChanges(Change[]changes){  
		if(true)trace(".storeChanges: ",changes);
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
