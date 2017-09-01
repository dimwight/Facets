package facets.core.app;
import static facets.core.app.ActionViewerTarget.Action.*;
import facets.core.superficial.app.SViewer;
import facets.core.superficial.app.ViewableAction;
import facets.core.superficial.app.ViewableFrame;
import facets.util.Debug;
import facets.util.Stateful;
import facets.util.tree.TypedNode;
/**
{@link ViewableFrame} with {@link Stateful} content. 
<p>{@link StatefulViewable} extends its superclass by 
requiring both its {@link #framed} content and all members of its {@link #selection()} 
to be {@link Stateful}, enabling the provision of 
<ul>
<li>data transfer using a {@link Clipper} connection to the system clipboard
and drag and drop implementation 
<li>state management (undo/redo)
</ul> 
 */
public abstract class StatefulViewable<S extends Stateful> 
		extends StatefulViewableCore{
	/**
	final static boolean selectionEdits=System.getProperty("NodeSelectionEdit")!=null;
	Can supply a {@link StatefulViewable} with a suitable {@link Clipper}. 
	 */
	public interface ClipperSource{
		Clipper newClipper(StatefulViewable viewable);
	}
	/**
	Can copy and paste the content of a {@link StatefulViewable}. 
	 */
	public interface Clipper{
		/**
		Place the currently selected content on the clipboard. 
		 */
		void copySelection();
		/**
		 Are the current clipboard contents suitable for pasting? 
		 */
		boolean canPaste();
		/**
		 Attempt to create content from the current clipboard contents. 
		 @return the content, or <code>null</code> on failure
		 */
		Stateful[]newStatefuls();
	}
	final static boolean undoableEdits=System.getProperty("undoableEdits")!=null;
	protected final Clipper clipper;
	private S copyFramed;
	protected final S copyFramed(){
		if(copyFramed==null)throw new IllegalStateException(
				"Null copyFramed in "+Debug.info(this));
		else return copyFramed;
	}
	/**
	Unique constructor. 
	@param title passed to superclass
	@param content passed to superclass
	@param clipperSource if non-<code>null</code> will be used for data transfer
	 */
	protected StatefulViewable(String title,S content,ClipperSource clipperSource){
		super(title,content);
		clipper=clipperSource==null?null:clipperSource.newClipper(this);
	}
	public final void copyStatefulSelection(){
		if(clipper==null)throw new IllegalStateException("Null clipper in "+Debug.info(this));
		clipper.copySelection();
	}
	final protected boolean canPaste(){
		return clipper==null?false:clipper.canPaste();
	}
	final protected Stateful[]newPasteStatefuls(){
		if(clipper==null)throw new IllegalStateException("Null clipper in "+Debug.info(this));
		return clipper.newStatefuls();
	}
	/**
	Create a textual representation of one or more {@link Stateful}s for use in 
	data transfer.
	<p>May be called (indirectly) from {@link #copyStatefulSelection()}; 
	default returns a debug string. 
	@param selected are the currently selected content 
	 */
	public String newStatefulsText(S[]selected){
		return Debug.info(selected);
	}
	/**
	Does the text passed seem to represent suitable content? 
	<p>Used to determine the live state of the viewer 'Paste' target; default
	return <code>false</code>.
	@param text is currently on the system clipboard
	 */
	public boolean textSeemsPastable(String text){
		return false;
	}
	/**
	Re-implementation that understands {@link ActionViewerTarget.Action}s. 
	 */
	public boolean actionIsLive(SViewer viewer,ViewableAction action){
		return action==UNDO?states.canUndo() 
			:action==REDO?states.canRedo() 
			:super.actionIsLive(viewer,action);
	}
	/**
	Valid implementation that understands {@link ActionViewerTarget.Action}s. 
	 */
	protected void actionTriggered(SViewer viewer,ViewableAction action){
		if(action==SELECT_ALL)defineSelection(SELECT_ALL);
		else if(action==ITERATE_FORWARD||action==ITERATE_BACK)
			iterateSelection(action==ITERATE_FORWARD);
		else if(action==CUT||action==COPY)copyStatefulSelection();
		boolean editing=action==PASTE||action==PASTE_INTO||action==MODIFY
				||action==CUT||action==DELETE||action==UNDO||action==REDO;
		if(!editing)return;
		copyFramedState();
		if(action==PASTE||action==PASTE_INTO)
			insertStatefuls(action==PASTE_INTO,newPasteStatefuls());
		else if(action==CUT||action==DELETE)deleteSelection(action==CUT);
		else if(action==MODIFY){
			editing&=editSelection();//modifySelection?
			if(undoableEdits){
				Stateful modifiedState=((Stateful)framed).copyState();
				restoreAfterEditAction();
				setModifyState(modifiedState);
				updateAfterEditAction();
			}
		}
		else if(action==UNDO)states.undo();
		else if(action==REDO)states.redo();
		if(!editing)return;
		if(stateIsValid())updateAfterEditAction();
		else restoreAfterEditAction();
	}
	protected void setModifyState(Stateful state){
		((Stateful)framed).setState(state);
	}
	final public void updateAfterEditAction(){
		copyFramedState();
		((Stateful)framed).updateStateStamp();
	}
	protected final void copyFramedState(){
		S src=(S)framed;
		copyFramed=clipper==null?src:(S)src.copyState();
	}
	protected void restoreAfterEditAction(){
		if(!undoableEdits)trace(".restoreAfterEditAction");
		((S)framed).setState(copyFramed);
	}
	protected boolean stateIsValid(){
		return true;
	}
	public void iterateSelection(boolean forward){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	public void insertStatefuls(boolean into, Stateful...stateful){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	public void deleteSelection(boolean asCut){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	public boolean editSelection(){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
}
