package applicable.eval.app;
import facets.core.app.AppContenter;
import facets.core.app.FeatureHost;
import facets.core.app.FeatureHost.LayoutFeatures;
import facets.core.app.ViewerContenter;
import facets.core.superficial.STargeter;
import facets.core.superficial.app.AreaTargeter;
import facets.core.superficial.app.SContentAreaTargeter;
import facets.core.superficial.app.SContenter;
import facets.core.superficial.app.ViewableFrame;
import facets.facet.FacetFactory;
import facets.facet.app.FacetAppSpecifier;
import facets.facet.app.FacetAppSurface;
import facets.facet.app.FileAppActions;
import facets.util.FileSpecifier;
import facets.util.tree.ValueNode;
import applicable.eval.form.EvalForm;
/**
{@link FacetAppSpecifier} for an {@link EvalCoder} application. 
 */
public abstract class EvalSpecifier extends FacetAppSpecifier{
	public static final String ARG_EVALUATE="launchEvaluate";
	private EvalFeatures features;
	private SContentAreaTargeter code,form;
	public EvalSpecifier(Class appClass){
		super(appClass);
	}
	public Object[][]decorationValues(){
		return joinDecorations(super.decorationValues(),
				new Object[][]{
			{EvalCoder.TITLE_LAUNCH,"","export_wiz.gif","New configurator"},
		});
	}
	void setFeatureLives(FacetAppSurface app){
		if(code==null)return;
		boolean live=app.findActiveContent()instanceof EvalCoder;
		code.content().target().setLive(live);
		code.viewer().target().setLive(live);
		STargeter[]appElements=app.surfaceTargeter().elements();
		if(!isFileApp())return;
		appElements[FileAppActions.TARGETS_NEW].target().setLive(false);
		appElements[FileAppActions.TARGETS_FILE].target().setLive(live);
		appElements[FileAppActions.TARGETS_RECENT].target().setLive(live);
	}
	LayoutFeatures getDesktopFeatures(FacetAppSurface app,
			SContentAreaTargeter targeter){
		if(targeter.content().target()instanceof EvalFormViewable)form=targeter;
		else code=targeter;
		return new EvalFeatures(app,targeter,targeter==form);
	}
	protected abstract String codeRootType();
	@Override
	final protected FacetAppSurface newApp(FacetFactory ff,FeatureHost host){
		return new FacetAppSurface(this,ff){
			@Override
			protected
			SContenter newContenter(Object source){
				return source instanceof EvalViewer?(ViewerContenter)source
						:new EvalCoder(source,this,EvalSpecifier.this);
			}
			@Override
			protected String newTitleBarText(){
				return areaTitle(AreaTargeter.AREA_CONTENT)+
					(findActiveContent().hasChanged()?"*":"")+" - "+title();
			}
			@Override
			protected Object getInternalContentSource(){
				return getAppInternalContentSource();
			}
			@Override
			public FileSpecifier[]getFileSpecifiers(){
				return getAppFileSpecifiers();
			}
			@Override
			protected void appOpened(){//${string_prompt:mode:noFiles}
				AppContenter content=findActiveContent();
				if(false&&spec.nature().getBoolean(ARG_NO_FILES)
						&&content instanceof EvalCoder)((EvalCoder)content).launchForm();
			}
		};
	}
	protected FileSpecifier[]getAppFileSpecifiers(){
		return new FileSpecifier[]{
				new FileSpecifier("eval.xml","Evaluator logic files"),
				new FileSpecifier("xml","XML files"),
			};
	}
	protected abstract EvalViewer newEvalViewer(EvalForm form,FacetAppSurface app, 
			EvalCoder coder);
	protected abstract Object getAppInternalContentSource();
}