package pdft.block;
import static facets.core.app.ActionAppSurface.CachingStyle.*;
import static facets.core.app.AppConstants.*;
import static facets.util.Debug.*;
import static facets.util.Util.*;
import static facets.util.tree.Nodes.*;
import facets.core.app.ActionAppSurface.CachingStyle;
import facets.core.app.AppSurface.ContentCreationException;
import facets.core.app.FeatureHost;
import facets.core.app.AppSurface.ContentStyle;
import facets.core.app.FeatureHost.LayoutFeatures;
import facets.core.app.Dialogs;
import facets.core.app.PagedContenter;
import facets.core.app.ViewerContenter.ContentSource;
import facets.core.superficial.SFacet;
import facets.core.superficial.app.AreaTargeter;
import facets.core.superficial.app.SContentAreaTargeter;
import facets.core.superficial.app.SContenter;
import facets.core.superficial.app.SHost;
import facets.core.superficial.app.SHost.FacetLayout;
import facets.core.superficial.app.SSurface;
import facets.facet.FacetFactory;
import facets.facet.app.FacetAppSpecifier;
import facets.facet.app.FacetAppSurface;
import facets.facet.app.FacetPreferences;
import facets.facet.kit.avatar.SwingPainterSource;
import facets.util.Debug;
import facets.util.FileSpecifier;
import facets.util.Util;
import facets.util.app.ProvidingCache.ItemProvider;
import facets.util.app.ProvidingCache.ItemValuer;
import facets.util.app.HostBounds;
import facets.util.app.WatchableOperation;
import facets.util.app.WatchableOperation.CancelStyle;
import facets.util.tree.ValueNode;
import java.io.File;
import java.io.IOException;
import org.apache.pdfbox.cos.COSDocument;
import pdft.PdfCore;
/**
{@link FacetAppSpecifier} for a {@link PdfContenter}.    
<p>Applications such as {@link pdfInspect} and {@link pdfBlockTexts}
based on {@link PdfApp} demonstrate the use of 
<a href="http://superficial.sourceforge.net/">Superficial</a> to wrap an existing library 
(here Apache PDFBox) in a rich GUI including custom content viewers. 
<h3><a name="args"></a>Arguments</h3>
<dl> 
	<dt><dfn>memMaxMb=X</dfn></dt>
	<dd>Sets the memory limit to <i>X</i>MB. </dd>
	<dt><dfn>traceMem</dfn></dt>
	<dd>Traces memory checking and object caching to <code>System.out</code>.</dd>
	<dt><dfn>renderGraphics</dfn></dt>
	<dd>Sets the default rendering style to Text and Graphics.</dd>
	<dt><dfn>wrapCode</dfn></dt>
	<dd>Sets default line wrapping in the Code pane. </dd>
</dl>
 */
public abstract class PdfApp extends FacetAppSpecifier{
	private static boolean fontSummary=false;
	public final static FileSpecifier[]pdfFiles={
		new FileSpecifier("pdf","PDF Portable Document Format")
	};
	static final String TITLE_DEFAULT="Default";
	protected abstract boolean jarReady();
	PdfApp(Class appClass){
		super(appClass);
	}
	@Override
	protected void traceOutput(String msg){
		System.out.println(msg);
	}
	@Override
	public Object[][]decorationValues(){
		return joinDecorations(super.decorationValues(),new Object[][]{
			{NATURE_APP_ICON_LARGE,"","pdf48.gif"},
			{NATURE_APP_ICON_INTERNAL,"","pdf16.gif"},
		});
	}
	@Override
	protected void addNatureDefaults(ValueNode root){
		super.addNatureDefaults(root);
		boolean jarReady=jarReady();
		mergeContents(root,new Object[]{
			Dialogs.KEYTOP_NATURE_SIZE+"PaneSetLayout_9_1_1=290,90",
			NATURE_ICON_PATH+"=icon",
			NATURE_DOC_PATH+"=.",
			HostBounds.NATURE_SIZE_MIN+"=400,300",
			NATURE_DEBUG+"="+(natureDebug&&!jarReady),
			NATURE_RUN_WATCHED+"="+(false||jarReady)
		});
	}
	@Override
	final public boolean isFileApp(){
		return jarReady();
	}
	@Override
	final public boolean canCreateContent(){
		return !jarReady();
	}
	@Override
	public ContentStyle contentStyle(){
		return false?super.contentStyle():ContentStyle.DESKTOP;
	}
	@Override
	public boolean canEditContent(){
		return false;
	}
	@Override
	final protected FacetAppSurface newApp(FacetFactory ff, FeatureHost host){
		final boolean jarReady=jarReady();
		if(jarReady&natureDebug)Util.printOut("PdfApp.newApp: natureDebug=",natureDebug);
		return new FacetAppSurface(this,ff){
			@Override
			protected CachingStyle cachingStyle(){
				return true||jarReady?checkMemory:noCache;
			}
			@Override
			protected String newTitleBarText(){
				return " ["+areaTitle(AreaTargeter.AREA_CONTENT)+"] - " +title();
			}
			@Override
			protected Object[]getFixedOpeningContentSources(){
				return false?new Object[]{getInternalContentSource(),
						getInternalContentSource()}
					:new Object[]{getInternalContentSource()};
			}
			@Override
			public Object getInternalContentSource(){
				return new ContentSource(){
					@Override
					public Object newContent(){
						return PdfApp.this.newCosDocument(new File(PdfApp.class.getSimpleName()+".pdf"));
					}
				};
			}
			@Override
			protected SContenter newContenter(final Object source){
				final FacetAppSurface app=this;
				WatchableOperation op=new WatchableOperation("PdfApp.newContenter"){
					public CancelStyle cancelStyle(){
						return CancelStyle.Dialog;
					};
					
					protected Object doReturnableOperation(){
						return new PdfContenter(source,app);
					}};
				return(SContenter)(false?watcher.runWatched(op):op.doOperations());
			}
			@Override
			protected void contentNotAdded(ContentCreationException e){
				String msg=e.getMessage();
				dialogs().infoMessage("Content Not Created",msg!=null?msg:e.toString());
			}
			@Override
			public FileSpecifier[]getFileSpecifiers(){
				return pdfFiles;
			}
			@Override
			protected LayoutFeatures newEmptyDesktopFeatures(SContentAreaTargeter area){
				return PdfFeatures.newEmpty(this,area);
			}
			@Override
			protected ItemValuer newCacheItemValuer(ItemProvider p,Object[]itemValues){
				return new ItemValuer(){
					@Override
					protected double mergeValues(double recent,double space,double create){
						return space*10+recent*5+create/10;
					}
				};
			}
			@Override
			protected void appClosing(){
				if(fontSummary)CharFonts.traceSummary();
			}
		};
	}
	final COSDocument newCosDocument(File pdf){
		final File appDir=getAppDir();
		try{
			memCheck=false;
			COSDocument doc=new PdfCore(pdf){
				protected File tmpDir(){
					return appDir;
				};
			}.document.getDocument();
			trace(".newCosDocument: scratch=",doc.getScratchFile());
			memCheck("newCosDocument~: "+Debug.info(doc));
			return doc;
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}
	final File getAppDir(){
		return getDir(userDir(),"pdfApp");
	}
	@Override
	public PagedContenter[]adjustPreferenceContenters(SSurface surface,
			PagedContenter[]contenters){
		return new PagedContenter[]{contenters[FacetPreferences.PREFERENCES_TRACE]};
	}
}