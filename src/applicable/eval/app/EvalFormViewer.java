package applicable.eval.app;
import facets.core.app.FeatureHost.LayoutFeatures;
import facets.core.app.SAreaTarget;
import facets.core.app.SContentAreaTargeter;
import facets.core.app.ViewableFrame;
import facets.core.app.ViewerContenter;
import facets.core.superficial.STarget;
import facets.core.superficial.TargetCore;
import facets.facet.app.FacetAppSurface;
import facets.facet.app.FileAppActions;
import facets.util.FileSpecifier;
import facets.util.Titled;
import facets.util.tree.DataNode;
import facets.util.tree.TypedNode;
import java.io.File;
import java.io.IOException;
import javax.swing.text.View;
import applicable.eval.form.EvalFormConsumer;
import applicable.eval.form.EvalForm;
import applicable.eval.view.ViewableForm;
public class EvalFormViewer extends ViewerContenter{
		final public static boolean jar=System.getProperty("cfjar")!=null;
		protected final FacetAppSurface app;
		protected final EvalSpecifier spec;
		final ViewableForm form;
		private final EvalCoder coder;
		protected EvalFormViewer(TypedNode code,FacetAppSurface app,EvalCoder coder, 
				EvalSpecifier spec){
			super(code);
			form=new ViewableForm(code,app.dialogs(),coder);
			this.app=app;
			this.spec=spec;
			this.coder=coder;
			setSink(coder.sink());
			FileAppActions.dummy=false;
		}
		@Override
		public void saveToSink(Object sink)throws IOException{
			DataNode root=(DataNode)contentFrame().framed;
			String name=((File)sink).getName().replaceAll("\\..*",""),
					then=root.title();
			root.setTitle(then.replaceAll(": .*",": "+name));
			coder.saveToSink(sink);
		}
		@Override
		final protected ViewableFrame newContentViewable(Object source){
			return new EvalFormViewable(form,spec);
		}
		@Override
		public STarget[]lazyContentAreaElements(SAreaTarget area){
			return new STarget[]{form.indexing,
					new TargetCore("Active",form.storer,form.remover),
					form.codesToggling};
		}
		@Override
		final public void areaRetargeted(SContentAreaTargeter area){
			form.remover.setLive(form.canRemoveActive());
			spec.setFeatureLives(app);
		}
		@Override
		protected String newContentAreaTitle(ViewableFrame viewable){
			return((Titled)viewable.framed).title();
		}
		@Override
		final public LayoutFeatures newContentFeatures(SContentAreaTargeter area){
			return spec.getDesktopFeatures(app,area);
		}
		@Override
		public FileSpecifier[]sinkFileSpecifiers(){
			return spec.getAppFileSpecifiers();
		}
		@Override
		final public boolean hasChanged(){
			return coder.hasChanged();
		}
	}