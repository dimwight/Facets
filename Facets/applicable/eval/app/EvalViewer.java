package applicable.eval.app;
import facets.core.app.FeatureHost.LayoutFeatures;
import facets.core.app.StatefulViewable;
import facets.core.app.ViewerContenter;
import facets.core.superficial.app.SContentAreaTargeter;
import facets.core.superficial.app.ViewableFrame;
import facets.facet.app.FacetAppSurface;
import facets.util.tree.Nodes;
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
		protected EvalViewer(EvalForm form,FacetAppSurface app,EvalSpecifier spec, 
				EvalCoder coder){
			super(form);
			this.app=app;
			this.spec=spec;
			this.coder=coder;
		}
		@Override
		public void areaRetargeted(SContentAreaTargeter area){
			spec.setFeatureLives(app);
		}
		@Override
		final protected ViewableFrame newContentViewable(Object source){
			return new EvalViewable((EvalForm)source);
		}
		@Override
		public boolean hasChanged(){
			return false;
		}
		@Override
		public LayoutFeatures newContentFeatures(SContentAreaTargeter area){
			return spec.getDesktopFeatures(app,null);
		}
		@Override
		public void wasRemoved(){
			coder.updateFromForm((EvalForm)contentFrame().framed);
		}
	}