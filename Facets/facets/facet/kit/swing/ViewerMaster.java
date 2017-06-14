package facets.facet.kit.swing;
import facets.core.superficial.SFacet;
import facets.core.superficial.STarget;
import facets.core.superficial.app.SViewer;
import facets.core.superficial.app.ViewerTarget;
import facets.facet.FacetFactory;
import facets.facet.FacetMaster;
import facets.util.Debug;
import facets.util.Util;
import java.awt.event.FocusEvent;
import java.io.Serializable;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
public abstract class ViewerMaster extends FacetMaster.Viewer{
	ViewerBase base;
	protected abstract JComponent newAvatarPane();
	public JComponent avatarPane(){
		return base().avatarPane;		
	}
	final public ViewerTarget viewerTarget(){
		return(ViewerTarget)base().facet.target();
	}
	public final ViewerBase base(){
		if(base==null)throw new IllegalStateException(
				"No base in (unattached?) "+Debug.info(this));
		else return base;
	}
	public boolean isScrollable(){
		return true;
	}
	final public JScrollPane scrollPane(){
		JScrollPane scroll=base.scroll;
		if(scroll==null)throw new IllegalStateException("Null scroll in "+Debug.info(this));
		else return scroll;
	}
	protected STarget[]targets(){
		return new STarget[]{};
	}
	public SFacet[]getTargetFacets(FacetFactory ff){
		return null;
	}
	protected int defaultCursor(){
		return -1;
	}
	protected void disposeAvatarPane(){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
}
