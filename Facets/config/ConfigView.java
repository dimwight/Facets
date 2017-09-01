package config;
import facets.core.app.ActionViewerTarget;
import facets.core.app.AreaRoot;
import facets.core.app.AppSurface.ContentStyle;
import facets.core.app.HtmlView;
import facets.core.app.PathSelection;
import facets.core.app.TextView;
import facets.core.app.TreeView;
import facets.core.superficial.FacetedTarget;
import facets.core.superficial.app.SSelection;
import facets.core.superficial.app.SView;
import facets.core.superficial.app.SViewer;
import facets.core.superficial.app.ViewableFrame;
import facets.core.superficial.app.ViewerTarget;
import facets.facet.AreaFacets;
import facets.facet.app.FacetAppSurface;
import facets.facet.kit.Toolkit;
import facets.facet.kit.swing.KitSwing;
import facets.util.FileSpecifier;
import java.net.URL;
import applicable.eval.app.EvalCoder;
import applicable.eval.app.EvalSpecifier;
import applicable.eval.app.EvalViewer;
import applicable.eval.form.EvalForm;
import applicable.eval.form.EvalForms;
final public class ConfigView extends EvalViewer{
	private final SView picks=new TextView("Picks"),inputs=new HtmlView.InputView("Form"),
	code=new TreeView("Code"){
		@Override
		public SSelection newViewerSelection(SViewer viewer,SSelection viewable){
			return PathSelection.newMinimal(((EvalForm)viewable.content()).source);
		}
		@Override
		public boolean hideRoot(){
			return false;
		}
		@Override
		public boolean isLive(){
			return false;
		}
	};
	ConfigView(EvalForm form,FacetAppSurface app,EvalSpecifier spec,EvalCoder coder){
		super(form,app,spec,coder);
	}
	@Override
	protected FacetedTarget[]newContentViewers(ViewableFrame viewable){
		return ActionViewerTarget.newViewerAreas(viewable,ViewerTarget.newViewFrames(
			new SView[]{
					inputs,
					picks,
					code,
			}
		));
	}
	@Override
	final protected void attachContentAreaFacets(AreaRoot area){
		app.ff.areas().attachViewerAreaPanes(area,"",AreaFacets.PANE_SPLIT_HORIZONTAL);
	}
	public static void main(final String[]args){
		final URL testSource=ConfigView.class.getClassLoader().getResource("V875.config.xml");
		if(false)EvalForms.testConsumers(testSource,false);
		else new EvalSpecifier(ConfigView.class){
			@Override
			public void adjustValues(){
				super.adjustValues();
			}
			@Override
			public Toolkit newToolkit(){
				return new KitSwing(true,false,true);
			}
			@Override
			final public ContentStyle contentStyle(){
				return ContentStyle.DESKTOP;
			}
			@Override
			protected FileSpecifier[]getAppFileSpecifiers(){
				return new FileSpecifier[]{new FileSpecifier("config.xml","Configurators")};
			}
			@Override
			protected Object getAppInternalContentSource(){
				return testSource;
			};
			@Override
			protected String codeRootType(){
				return ConfigView.class.getSimpleName();
			}
			@Override
			protected EvalViewer newEvalViewer(EvalForm form,FacetAppSurface app, 
					EvalCoder coder){
				return new ConfigView(form,app,this,coder);
			}
		}.buildAndLaunchApp(args);
	}
}