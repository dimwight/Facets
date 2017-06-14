package facets.facet;
import facets.core.superficial.Notifying.Impact;
import facets.core.superficial.app.ViewerTarget;
import facets.facet.kit.KitFacet;
import facets.facet.kit.swing.ViewerMaster;

import javax.swing.JComponent;
/**
Builds and manages Swing pane for a viewer facet. 
<p>{@link SwingViewerMaster} descends from {@link facets.facet.FacetMaster.Viewer}
via a superclass known by the {@link AreaFacets} viewers builder. 
<p>A custom viewer facet can be defined by returning a concrete subclass from
{@link facets.facet.ViewerAreaMaster#viewerMaster()}. 
 */
abstract public class SwingViewerMaster extends ViewerMaster{	
	/**
	{@link SwingViewerMaster} defined by a {@link ViewerPaneForm}.
	 */
	public static class Form extends SwingViewerMaster{
		private final ViewerPaneForm form;
		public Form(ViewerPaneForm form){
			this.form=form;
		}
		@Override
		protected JComponent newAvatarPane(){
			form.attachPaneMaster(this);
			return (JComponent)((KitFacet)form.newRetargetedFormFacet()).base().wrapped();
		}
		@Override
		public void refreshAvatars(Impact impact){
			form.refreshFacets();
		}
	}
	/**
	Create the avatar pane to be used in the viewer facet.	
	<p>Called during viewer construction; the pane returned can be accessed
	with {@link #avatarPane()}. 
	 */
	protected abstract JComponent newAvatarPane();
	/**
	The Swing avatar pane. 
	@return the pane created in {@link #newAvatarPane()}
	 */
	final public JComponent avatarPane(){
		return super.avatarPane();
	}
}
