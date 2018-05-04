package facets.facet;
import facets.core.app.SView;
import facets.core.app.ViewerTarget;
import facets.core.superficial.Notice;
import facets.core.superficial.Notifiable;
import facets.core.superficial.Notifying;
import facets.core.superficial.SFacet;
import facets.core.superficial.SFrameTarget;
import facets.core.superficial.STarget;
import facets.core.superficial.STargeter;
import facets.core.superficial.TargetCore;
import facets.core.superficial.TargeterCore;
import facets.core.superficial.Notifying.Impact;
import facets.core.superficial.app.SSelection;
import facets.facet.FacetMaster.Viewer;
import facets.util.Debug;
/**
Builds and manages a form inside a {@link Viewer}. 
 */
public abstract class ViewerPaneForm{
	private Viewer pane;
	private STargeter targeter;
	private Object stateCheck;
	private final Notifying notifier=new Notifying() {
		private Notifiable notifiable;
		final public void notify(Notice notice){
			ViewerTarget viewer=pane.viewerTarget();
			viewer.ensureActive(Impact.ACTIVE);
			Object content=((SFrameTarget)targeter.target()).framed;
			if(stateChecksEqual(stateCheck,newContentStateCheck(content)))
				viewer.selectionChanged(newNotifiedSelection(content,notice));
			else viewer.selectionEdited(null,newNotifiedEdit(notice), false);
		}

		public void setNotifiable(Notifiable n){
			notifiable=n;			
		}
		public void notifyParent(Notifying.Impact impact){
			if(notifiable!=null)notifiable.notify(new Notice(this,impact));			
		}
		public Notifiable notifiable(){
			if(notifiable==null)
				throw new IllegalStateException("Null monitor in "+Debug.info(this));
			return notifiable;
		}
		public Notifying.Impact impact(){
			return Notifying.Impact.DEFAULT;
		}
		@Override
		public Notifying[]elements(){
			throw new RuntimeException("Not implemented in "+Debug.info(this));
		}
		@Override
		public String title(){
			throw new RuntimeException("Not implemented in "+Debug.info(this));
		}
	};
	final public void attachPaneMaster(Viewer pane){
		if(pane==null)
			throw new IllegalArgumentException("Null pane in "+Debug.info(this));
		else if(this.pane!=null)throw new IllegalArgumentException(
				"Change of immutable pane in "+Debug.info(this));
		else this.pane=pane;
		notifier.setNotifiable(pane.viewerTarget());
	}
	final public SFacet newRetargetedFormFacet(){
		if(true)throw new RuntimeException("Not tested in "+this);
		targeter=TargeterCore.newRetargeted(newSelectionFrame(),true);
		targeter.setNotifiable(notifier);
		return newFormFacet(targeter);
	}
	final public void refreshFacets(){
		SFrameTarget frameTarget=newSelectionFrame();
		targeter.retarget(frameTarget,Notifying.Impact.DEFAULT);
		targeter.retargetFacets(Notifying.Impact.DEFAULT);
		stateCheck=newContentStateCheck(frameTarget.framed);
	}
	protected abstract SFrameTarget newSelectionFrame();
	protected boolean elementNeedsSelectionFocus(SSelection selection,
			STarget element){
		return false;
	}
	protected Viewer pane(){
		if(pane==null) throw new IllegalStateException("Null pane in "+Debug.info(this));
		return pane;
	}
	protected abstract STarget[] lazySelectionElements(SFrameTarget frameTarget,
			SView view,SSelection selection);
	protected abstract SFacet newFormFacet(STargeter targeter);
	protected abstract Object newContentStateCheck(Object content);
	protected abstract boolean stateChecksEqual(Object then,Object now);
	protected abstract SSelection newNotifiedSelection(Object content,
			Notice notice);
	protected abstract Object newNotifiedEdit(Notice notice);
}