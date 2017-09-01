package facets.core.app;
import facets.core.app.ViewableStates.Change;
import facets.core.superficial.app.SSelection;
import facets.core.superficial.app.ViewableFrame;
import facets.util.Objects;
import facets.util.Stateful;
import facets.util.tree.NodePath;
import facets.util.tree.TypedNode;
import javax.swing.undo.UndoManager;
abstract class StatefulViewableCore extends ViewableFrame{
	private static final boolean noUndo=false;
	final UndoManager states=//new ViewableStates((StatefulViewable)this)
			new UndoManager();
	private Stateful[]statesThen;
	protected StatefulViewableCore(String title,Stateful content){
		super(title,content);
	}
	/**
	 Overrides method from {@link ViewableFrame}. 
	 <p>Having converted both <code>stateSpec</code> and
	 the {@link PathSelection#multiple()} of {@link #selection()} to {@link Stateful}[],
	<ol>
	<li>matches edits to their targets by calling 
	{@link #matchSelectionEdits(Stateful[], Stateful[])}
	<li>compares array members using {@link #checkStateChange(Stateful, Stateful)}
	<li>calls {@link Stateful#setState(Object)} in each path target with 
	the appropriate edit
	<li>stores non-interim changes in a private state management sequence
	</ol>
	 */
	public void setFramedState(final Object stateSpec,boolean interim){
		Stateful[]selections=newSelectionArray(selection()),
			edits=newSelectionArray(new SSelection(){
				@Override
				public Object[] multiple(){
					return (Object[])stateSpec;
				}
				@Override
				public Object single(){
					throw new RuntimeException("Not implemented in "+this);
				}
				@Override
				public Object content(){
					throw new RuntimeException("Not implemented in "+this);
				}
			}),
			matches=matchSelectionEdits(selections,edits);
		if(matches==null)throw new IllegalStateException(
				"No matches for edits\n"+Objects.toStringWithHeader(edits));
		if(statesThen==null)statesThen=new Stateful[selections.length];
		Change[]changes=new Change[matches.length];
		boolean changed=false;
		for(int i=0;i<changes.length;i++){
			if(statesThen[i]==null)statesThen[i]=selections[i].copyState();
			Stateful stateNow=matches[i];
			selections[i].setState(stateNow);
			if(noUndo||interim)continue;
			Stateful stateThen=statesThen[i];
			changed|=checkStateChange(stateThen,stateNow);
			changes[i]=new Change(selections[i],stateThen,stateNow);
			if(false)trace(".setFramedState [" ,changed+"]:\n"+stateThen+"\n"+stateNow);
		}
//		if(changed)states.storeViewableChanges(changes);
		if(!interim)statesThen=null;
	}
	/**
	Ensure that the list of edits matches that of the content passed. 
	<p>Called from {@link #setFramedState(Object, boolean)}; 
	default returns the edits passed.
	@param selections from the current selection
	@param edits were passed to {@link #setFramedState(Object, boolean)}
	 */
	protected Stateful[]matchSelectionEdits(Stateful[]selections,Stateful[]edits){
		return edits;
	}
	/**
	Check for difference between two states of a content member. 
	<p>Called from {@link #setFramedState(Object, boolean)}; 
	default returns <code>false</code>.
	@param stateThen a copy of the previous state
	@param stateNow the current state
	 */
	protected boolean checkStateChange(Stateful stateThen,Stateful stateNow){
		return false;
	}
	protected void restoreState(Stateful content,Stateful state){
		content.setState(state);
	}
	/**
	Can one edit state subsume the other? 
	<p>Called (indirectly) from {@link #setFramedState(Object, boolean)}; default is <code>false</code>
	@param earlier the previous edit 
	@param later the new edit which may subsume <code>earlier</code>
	 */
	protected boolean canMergeEdits(Stateful[]earlier,Stateful[]later){
		return false;
	}
	final public static Stateful[]newSelectionArray(SSelection selection){
		Object[]multiple=selection.multiple();
		if(multiple.length>1)throw new RuntimeException("Not implemented for "+selection);
		NodePath path=(NodePath)((PathSelection)selection).paths[0];
		int valueAt=path.valueAt();
		if(valueAt>=0){
			TypedNode parent=(TypedNode)((Stateful)multiple[0]);
			Object[]content=parent.contents();
			multiple[0]=new NodeUndoableEdit.ValueCarrier(parent,
					content[NodeUndoableEdit.contentAt(content,parent,valueAt)]);
		}
		return Objects.newTyped(Stateful.class,multiple);
	}
}
