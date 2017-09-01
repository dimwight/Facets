package pdft.inspect;
import static facets.core.app.ActionAppSurface.CachingStyle.*;
import static facets.core.app.AppConstants.*;
import static facets.core.app.AppSurface.ContentStyle.*;
import static facets.core.app.TextView.*;
import static facets.util.Debug.*;
import static facets.util.tree.Nodes.*;
import facets.core.app.AppSurface.ContentStyle;
import facets.core.app.Dialogs;
import facets.core.app.FeatureHost;
import facets.core.app.FeatureHost.LayoutFeatures;
import facets.core.superficial.app.AreaTargeter;
import facets.core.superficial.app.SContentAreaTargeter;
import facets.core.superficial.app.SContenter;
import facets.facet.FacetFactory;
import facets.facet.app.FacetAppSpecifier;
import facets.facet.app.FacetAppSurface;
import facets.util.FileSpecifier;
import facets.util.app.AppValues;
import facets.util.app.HostBounds;
import facets.util.app.WatchableOperation;
import facets.util.tree.ValueNode;
import java.io.File;
import pdft.block.PdfApp;
public final class pdfInspect extends FacetAppSpecifier{
	private final boolean jarReady=false;
	pdfInspect(){
		super(pdfInspect.class);
	}
	@Override
	protected void traceOutput(String msg){
		if(false)System.out.println(msg);
	}
	@Override
	public Object[][]decorationValues(){
		return joinDecorations(super.decorationValues(),new Object[][]{
			{NATURE_APP_ICON_LARGE,"","pdf48.gif"},
			{NATURE_APP_ICON_INTERNAL,"","pdf16.gif"},
			{PdfContenter.TITLE_REOPEN,"","","The file will be closed. " +
					"Re-open without content viewers?"}
		});
	}
	@Override
	protected void addNatureDefaults(ValueNode root){
		super.addNatureDefaults(root);
		mergeContents(root,new Object[]{
			Dialogs.KEYTOP_NATURE_SIZE+"PaneSetLayout_9_1_1=290,90",
			NATURE_ICON_PATH+"=icon",
			NATURE_DOC_PATH+"=.",
			NATURE_DEBUG+"="+(natureDebug&&!jarReady),
			NATURE_RUN_WATCHED+"="+(true&&jarReady)
		});
	}
	@Override
	public boolean isFileApp(){
		return jarReady;
	}
	@Override
	public boolean offersHelp(){
		return true;
	}
	@Override
	public boolean canCreateContent(){
		return !jarReady;
	}
	@Override
	public ContentStyle contentStyle(){
		return DESKTOP;
	}
	@Override
	public boolean canEditContent(){
		return false;
	}
	@Override
	protected FacetAppSurface newApp(FacetFactory ff,FeatureHost host){
		if(jarReady&natureDebug)trace(".newApp: natureDebug=",natureDebug);
		return new FacetAppSurface(this,ff){
			@Override
			protected CachingStyle cachingStyle(){
				return false||jarReady?checkMemory:checkItemCount;
			}
			@Override
			protected String newTitleBarText(){
				return " ["+areaTitle(AreaTargeter.AREA_CONTENT)+"] - " +title();
			}
			@Override
			protected Object[]getFixedOpeningContentSources(){
				return false?new Object[]{getInternalContentSource(),getInternalContentSource()}
					:new Object[]{getInternalContentSource()};
			};
			@Override
			public Object getInternalContentSource(){
				return PdfContenter.newCosDocument(new File(PdfApp.class.getSimpleName()+".pdf"));
			}
			@Override
			protected SContenter newContenter(final Object source){
				final FacetAppSurface app=this;
				WatchableOperation op=new WatchableOperation("pdfInspect.newContenter"){
					public CancelStyle cancelStyle(){
						return CancelStyle.Dialog;
					};
					public Object doReturnableOperation(){
						return new PdfContenter(source,app,new PageAvatarPolicies(spec));
					}};
				return(SContenter)(false?watcher.runWatched(op):op.doOperations());
			}
			@Override
			protected void contentNotAdded(ContentCreationException e){
				String msg=e.getMessage();
				dialogs().infoMessage("Content Not Created",msg!=null?msg:e.toString());
			};
			@Override
			public FileSpecifier[]getFileSpecifiers(){
				return new FileSpecifier[]{PdfContenter.pdfFiles};
			}
			@Override
			protected LayoutFeatures newEmptyDesktopFeatures(SContentAreaTargeter area){
				return PdfFeatures.newEmpty(this,area);
			};
		};
	}
	public static void main(String[]args){
		(false?new pdft.block.pdfInspect():new pdfInspect()).buildAndLaunchApp(args);
	}
}