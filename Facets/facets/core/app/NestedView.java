package facets.core.app;
import facets.core.superficial.SIndexing;
import facets.core.superficial.STarget;
import facets.core.superficial.Notifying.Impact;
import facets.core.superficial.app.SAreaTarget;
import facets.core.superficial.app.SView;
import facets.core.superficial.app.SelectingFrame;
import facets.core.superficial.app.ViewerTarget;
import facets.core.superficial.app.SContentAreaTargeter.ContentArea;
public abstract class NestedView extends FacetHostable implements SView{
	private final String title;
	public NestedView(String title){
		this.title=title;
	}
	@Override
	final public void facetRetargeted(Hosting host,STarget target,Impact impact){
		host.refreshViewer(target);
	}
	@Override
	final public String title(){
		return title;
	}
	@Override
	final public boolean allowMultipleSelection(){
		return false;
	}
	@Override
	final public boolean isLive(){
		return false;
	}
	final public void expandViewer(ViewerTarget viewer){
		SAreaTarget area=viewer.areaParent();
		SelectingFrame sourceFrame=(SelectingFrame)
				((ContentArea)area.areaParent()).contenter.contentFrame();
		Object content=getSourceSelectionContent(sourceFrame.selection());
		AppContenter contenter=newViewerContenter(content);
		SAreaTarget root=contenter.newContentArea(false);
		area.setIndexing(SIndexing.newDefault(title(),new STarget[]{root}));
	}
}