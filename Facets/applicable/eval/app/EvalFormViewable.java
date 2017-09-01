package applicable.eval.app;
import facets.core.app.HtmlView.InputView;
import facets.core.app.StatefulViewable;
import facets.core.app.TextView;
import facets.core.superficial.app.SSelection;
import facets.core.superficial.app.SView;
import facets.core.superficial.app.SViewer;
import facets.core.superficial.app.SelectionView;
import facets.util.Debug;
import facets.util.HtmlFormBuilder;
import facets.util.HtmlFormBuilder.FormInput;
import applicable.eval.form.EvalForm;
/**
{@link StatefulViewable} wrapping a {@link EvalForm}. 
<p>{@link EvalFormViewable} maintains {@link SSelection}s as appropriate for its {@link SViewer}s
using {@link EvalForm#newInputsBuilder()} and {@link EvalForm#newPicksBuilder()}. 
 */
public final class EvalFormViewable extends StatefulViewable{
	HtmlFormBuilder inputs;
	public EvalFormViewable(final EvalForm framed){
		super(framed.title(),framed,null);
		setSelection(new SSelection(){
			@Override
			public Object single(){
				return framed.source;
			}
			@Override
			public Object content(){
				return framed;
			}
			@Override
			public Object[]multiple(){
				return new Object[]{single()};
			}
		});
	}
	@Override
	protected void viewerSelectionEdited(SViewer viewer,Object edit,boolean interim){
		inputs.readEdit((FormInput)edit);
		inputs=null;
	}
	@Override
	protected SSelection newViewerSelection(SViewer viewer){
		final EvalForm form=(EvalForm)framed;
		SView view=viewer.view();
		if(view instanceof InputView){
			if(true||inputs==null)inputs=form.newInputsBuilder();
			return new SSelection(){
				@Override
				public Object content(){
					return inputs.newPageContent();
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
					return form.newPicksBuilder().toString();
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
		}catch(Exception e){
			trace(".viewerSelectionChanged: e=",e);
		}
	}
}