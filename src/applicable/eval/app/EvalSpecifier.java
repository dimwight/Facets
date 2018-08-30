package applicable.eval.app;
import facets.core.app.ActionAppSurface;
import facets.core.app.AppActions;
import facets.core.app.AppContenter;
import facets.core.app.AreaTargeter;
import facets.core.app.FeatureHost;
import facets.core.app.FeatureHost.LayoutFeatures;
import facets.core.app.SContentAreaTargeter;
import facets.core.app.SContenter;
import facets.core.app.ViewerContenter;
import facets.core.superficial.STargeter;
import facets.facet.FacetFactory;
import facets.facet.app.FacetAppActions;
import facets.facet.app.FacetAppSpecifier;
import facets.facet.app.FacetAppSurface;
import facets.facet.app.FileAppActions;
import facets.util.FileSpecifier;
import facets.util.tree.DataNode;
import facets.util.tree.Nodes;
import facets.util.tree.TypedNode;
import facets.util.tree.ValueNode;
/**
{@link FacetAppSpecifier} for an {@link EvalCoder} application. 
 */
public abstract class EvalSpecifier extends FacetAppSpecifier{
	public static final String ARG_EVALUATE="launchEvaluate";
	private EvalFeatures features;
	private SContentAreaTargeter coder,inputs;
	public EvalSpecifier(Class appClass){
		super(appClass);
	}
	@Override
	protected AppActions newActions(ActionAppSurface app){
		return !isFileApp()?new FacetAppActions(app){
			@Override
			protected boolean contentIsRemovable(String dialogTitle,
					AppContenter content){
				return content instanceof EvalCoder?true:
					(((EvalFormViewer)content).form.isClean());
			}
		}
		:new FileAppActions(app){
			@Override
			protected boolean contentIsRemovable(String dialogTitle,
					AppContenter content){
				if(content instanceof EvalCoder
						||((EvalFormViewer)content).form.isClean())
					return super.contentIsRemovable(dialogTitle,content);
				return false;
			}
		};
	}
	@Override
	protected void addStateDefaults(ValueNode root){
		if(EvalFormViewer.jar)root.setContents(Nodes.decode(new DataNode("Encoded","State",
				new Object[]{"789C6D52CF6B1341147EDD74B541C1263DD4967AD662984D8BD440ECA13F2804D67AD82A420E65924CC396C9EE38F336C41E042F7AF022D2E2D122BD084110FF004F7A12152FBD8A9E0BDE443CE89BCDAA297A19DE30DFF7BEEF7D6FFA47E0261A26EBFE36EF7216C62C103AE432DCE10D29AACF3FE4FBC1E151CD01E82900708C86E92DDE14685882A164A8856037B84CC47ADC12A5B10BEF5F5FFDFECAC2354CFD035CE5C82D6EF2F0D1F5C7CF2E060E8CD461AC19472822340813031B9EE451DBBBD6D8164DACF6FEA7B8715B8996EDF4F4E79BB98F75973A8DD6C0095B3E9C505C533784197F40F32CCDB334EF0FADEAC328D205A138A418A00EA336BDE5BB76A28D14501802AC486E8C757476D851801CC5562257622DAEBC28FC789BF3BE38E0F8E01AE41D45630DB5A8D1AC6DA149C4C510A5B80577E0644F51AE85740316C43250F1F3FEC1B7BBF72B14530DDCD413698FFFC5AD279D86D0F7FA7BE74EED7E7A902D29DF45C831C6A83A4FE239AE54F7186B30E6FED783CA4EF5E572CAA22F90653F8064D93F7C77F3C9B89995BFD73F42A976B841A197E3246A99C5B972B95429971616E64B54CE1B3B8D939EA749BE678B3354CC920F97D6C2D3A76262CF89AC9FE29108940CD16C2E11622D0D76F3D262995DAEFC02527CDAAA9E020000"
				})).contents());
		else super.addStateDefaults(root);
	}
	public Object[][]decorationValues(){
		return joinDecorations(super.decorationValues(),
				new Object[][]{
			{EvalCoder.TITLE_LAUNCH,"","export_wiz.gif","View Configurations"},
		});
	}
	void setFeatureLives(FacetAppSurface app){
		if(coder==null)return;
		boolean live=app.findActiveContent()instanceof EvalCoder;
		coder.content().target().setLive(live);
		coder.viewer().target().setLive(live);
		STargeter[]appElements=app.surfaceTargeter().elements();
		if(!isFileApp())return;
		appElements[FileAppActions.TARGETS_NEW].target().setLive(false);
		appElements[FileAppActions.TARGETS_FILE].target().setLive(live);
		appElements[FileAppActions.TARGETS_RECENT].target().setLive(live);
	}
	LayoutFeatures getDesktopFeatures(FacetAppSurface app,
			SContentAreaTargeter targeter){
		if(targeter.content().target()instanceof EvalFormViewable)inputs=targeter;
		else coder=targeter;
		return new EvalFeatures(app,targeter,targeter==inputs);
	}
	protected abstract String codeRootType();
	@Override
	protected FacetAppSurface newApp(FacetFactory ff,FeatureHost host){
		return new FacetAppSurface(this,ff){
			@Override
			protected
			SContenter newContenter(Object source){
				return source instanceof EvalFormViewer?(ViewerContenter)source
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
				if(!((FacetAppSpecifier)spec).isFileApp()
						||spec.nature().getBoolean(ARG_EVALUATE)
						&&content instanceof EvalCoder)((EvalCoder)content).launchFormViewer();
			}
		};
	}
	protected FileSpecifier[]getAppFileSpecifiers(){
		return new FileSpecifier[]{
				new FileSpecifier("eval.xml","Evaluator logic files"),
				new FileSpecifier("xml","XML files"),
			};
	}
	protected abstract EvalFormViewer newFormViewer(TypedNode code,FacetAppSurface app, 
			EvalCoder coder);
	protected abstract Object getAppInternalContentSource();
}