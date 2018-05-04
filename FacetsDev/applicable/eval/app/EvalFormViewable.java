package applicable.eval.app;
import facets.core.app.HtmlView.InputView;
import facets.core.app.NodeViewable;
import facets.core.app.AppSpecifier;
import facets.core.app.PathSelection;
import facets.core.app.SView;
import facets.core.app.SViewer;
import facets.core.app.StatefulViewable;
import facets.core.app.TextView;
import facets.core.superficial.app.SSelection;
import facets.core.superficial.app.SelectionView;
import facets.util.Debug;
import facets.util.HtmlFormBuilder;
import facets.util.HtmlFormBuilder.FormInput;
import facets.util.OffsetPath;
import facets.util.Strings;
import facets.util.app.AppValues;
import facets.util.tree.NodePath;
import facets.util.tree.Nodes;
import facets.util.tree.TypedNode;
import applicable.eval.form.EvalForm;
import applicable.eval.view.ViewableForm;
public final class EvalFormViewable extends NodeViewable{
	private static final String STATE_OFFSETS="formOffsets";
	private final AppSpecifier spec;
	private final ViewableForm form;
	public EvalFormViewable(ViewableForm form,AppSpecifier spec){
		super(form.source,null);
		this.form=form;
		this.spec=spec;
		final int[]offsets=spec.state().getInts(STATE_OFFSETS);
		OffsetPath path=offsets.length>0?new NodePath(offsets):NodePath.empty;
		Object definition;
		TypedNode root=form.source;
		try{
			path.members(root);
			definition=offsets.length==0?null:new PathSelection(root,path);
		}catch(Exception e){
			definition=Nodes.descendantTitled(root,"Miscellaneous options");
		}
		if(definition!=null)defineSelection(definition);
	}
	@Override
	protected SSelection newViewerSelection(SViewer viewer){
		SView view=viewer.view();
		if(view instanceof InputView){
			return new SSelection(){
				@Override
				public Object content(){
					return form.newPageContent();
				}
				@Override
				public Object single(){
					throw new RuntimeException("Not implemented in "+this);
				}
				@Override
				public Object[]multiple(){
					throw new RuntimeException("Not implemented in "+this);
				}
			};
		}
		else if(view instanceof TextView){
			return new SSelection(){
				@Override
				public Object single(){
					return form.newPicks();
				}
				@Override
				public Object[]multiple(){
					throw new RuntimeException("Not implemented in "+Debug.info(this));
				}
				@Override
				public Object content(){
					throw new RuntimeException("Not implemented in "+Debug.info(this));
				}
			};
		}
		else return((SelectionView)viewer.view()).newViewerSelection(viewer,selection());
	}
	@Override
	protected void viewerSelectionChanged(SViewer viewer,SSelection selection){
		try{
			super.viewerSelectionChanged(viewer,selection);
			spec.state().put(STATE_OFFSETS,((PathSelection)selection).paths[0].offsets);
		}catch(Exception e){
			trace(".viewerSelectionChanged: e=",e);
		}
	}
	@Override
	protected void viewerSelectionEdited(SViewer viewer,Object edit,boolean interim){
		form.readEdit(edit);
	}
}