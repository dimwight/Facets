package applicable.eval.app;
import facets.core.app.FeatureHost.LayoutFeatures;
import facets.core.app.StatefulViewable;
import facets.core.app.ViewerContenter;
import facets.core.superficial.SIndexing;
import facets.core.superficial.STarget;
import facets.core.superficial.app.SAreaTarget;
import facets.core.superficial.app.SContentAreaTargeter;
import facets.core.superficial.app.ViewableFrame;
import facets.facet.app.FacetAppSurface;
import facets.util.Debug;
import facets.util.tree.Nodes;
import facets.util.tree.TypedNode;
import javax.swing.text.View;
import applicable.eval.form.EvalForm;
import applicable.eval.form.EvalFormConsumer;
import applicable.eval.form.EvalRecord;
/**
{@link ViewerContenter} with {@link View}s that embody {@link EvalFormConsumer}s. 
 */
public abstract class EvalViewer extends ViewerContenter{
		protected final FacetAppSurface app;
		protected final EvalSpecifier spec;
		private final EvalCoder coder;
		private final SIndexing chooser;
		protected EvalViewer(EvalForm form,FacetAppSurface app,EvalSpecifier spec, 
				EvalCoder coder){
			super(form);
			this.app=app;
			this.spec=spec;
			this.coder=coder;
			chooser=form.newIndexing();
		}
		@Override
		public STarget[]lazyContentAreaElements(SAreaTarget area){
			return new STarget[]{chooser};
		}
		@Override
		final public void areaRetargeted(SContentAreaTargeter area){
			spec.setFeatureLives(app);
		}
		@Override
		final protected ViewableFrame newContentViewable(Object source){
			return new EvalFormViewable((EvalForm)source);
		}
		@Override
		final public boolean hasChanged(){
			return false;
		}
		@Override
		protected String newContentAreaTitle(ViewableFrame viewable){
			return((EvalForm)viewable.framed).title();
		}
		@Override
		final public LayoutFeatures newContentFeatures(SContentAreaTargeter area){
			return spec.getDesktopFeatures(app,area);
		}
		@Override
		final public void wasRemoved(){
			EvalForm form=(EvalForm)contentFrame().framed;
			if(!form.hasChanged())return;
			TypedNode source=form.activeRecord().source;
			String input=app.dialogs().getTextInput(title(),
					"Edit record title?",source.title()+"+",15);
			if(input!=null)source.setTitle(input);
			coder.updateFromForm((EvalForm)contentFrame().framed);
		}
	}