package pub.view;
import static facets.core.app.ActionViewerTarget.Action.*;
import static facets.core.app.PathSelection.*;
import static facets.facet.FacetFactory.*;
import facets.core.app.ActionViewerTarget;
import facets.core.app.ActionViewerTarget.Action;
import facets.core.app.FeatureHost.LayoutFeatures;
import facets.core.app.HtmlView;
import facets.core.app.HtmlView.InputView;
import facets.core.app.NodeViewable;
import facets.core.app.PathSelection;
import facets.core.app.TreeView;
import facets.core.app.ValueEdit;
import facets.core.app.ViewerContenter;
import facets.core.superficial.FacetedTarget;
import facets.core.superficial.SFacet;
import facets.core.superficial.SFrameTarget;
import facets.core.superficial.app.SAreaTarget;
import facets.core.superficial.app.SContentAreaTargeter;
import facets.core.superficial.app.SSelection;
import facets.core.superficial.app.SView;
import facets.core.superficial.app.SViewer;
import facets.core.superficial.app.ViewableAction;
import facets.core.superficial.app.ViewableFrame;
import facets.core.superficial.app.ViewerTarget;
import facets.facet.FacetFactory;
import facets.facet.ViewerAreaMaster;
import facets.facet.AreaFacets.PaneFacets;
import facets.facet.FacetFactory.EditFacets;
import facets.facet.app.FacetAppSurface;
import facets.util.Debug;
import facets.util.HtmlBuilder.RenderTarget;
import facets.util.HtmlFormBuilder.FormInput;
import facets.util.tree.Nodes;
import facets.util.tree.TypedNode;
import facets.util.tree.ValueNode;
import pub.view.RecordContenter.StateView;
import applicable.field.ValueField;
final class EditViewable extends RecordViewable{
	private static final ViewableAction[]STATE_ACTIONS={MODIFY,COPY,PASTE,PASTE_INTO,DELETE};
	private final FacetAppSurface app;
	EditViewable(RecordContent content,FacetAppSurface app){
		super(content,app,app.ff.statefulClipperSource(false));
		this.app=app;
		content.setUserLock();
	}
	@Override
	public ViewableAction[]viewerActions(SView view){
		return view instanceof StateView?STATE_ACTIONS:new ViewableAction[]{};
	}
	@Override
	public boolean actionIsLive(SViewer viewer,ViewableAction action){
		return content.isDisposed()?false
				:viewer.view()instanceof StateView&&nodeActionLive(viewer,action);
	}
	@Override
	protected void actionTriggered(SViewer viewer,ViewableAction action){
		if(viewer.view()instanceof StateView)nodeActionTriggered(viewer,action);
	}
	@Override
	public boolean editSelection(){
		return new ValueEdit(((PathSelection)selection())){
			protected String getDialogInput(String title,String rubric,
					String proposal){
				return app.dialogs().getTextInput(title,rubric,proposal,0);
			}
		}.dialogEdit();
	}
	@Override
	protected boolean stateIsValid(){
		return content.hasValidStateTree();
	}
	@Override
	protected void viewerSelectionEdited(SViewer viewer,Object edit,boolean interim){
		newDataForm(RenderTarget.Swing,true).readEdit((FormInput)edit);
	}
}