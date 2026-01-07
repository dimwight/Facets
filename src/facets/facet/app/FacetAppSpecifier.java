package facets.facet.app;
import static facets.core.app.AppConstants.*;
import static facets.facet.FacetFactory.*;
import static facets.facet.app.FacetConstants.*;
import static facets.util.app.Events.*;
import static facets.util.tree.Nodes.*;
import facets.core.app.ActionAppSurface;
import facets.core.app.AppActions;
import facets.core.app.AppSpecifier;
import facets.core.app.AppSurface;
import facets.core.app.AppWindowHost;
import facets.core.app.FeatureHost;
import facets.core.app.Headless;
import facets.core.app.HideableHost;
import facets.core.app.HtmlView;
import facets.core.app.PagedActions;
import facets.core.app.PagedContenter;
import facets.core.app.PagedSurface;
import facets.core.app.SContentAreaTargeter;
import facets.core.app.TreeView;
import facets.core.app.ViewerContenter;
import facets.core.app.AppSurface.ContentStyle;
import facets.core.app.Dialogs.Response;
import facets.core.app.Dialogs.Surfacer;
import facets.core.app.avatar.PainterMaster.Textual;
import facets.core.superficial.app.SHost;
import facets.core.superficial.app.SSurface;
import facets.facet.FacetFactory;
import facets.facet.FacetFactory.DialogSurfacer;
import facets.facet.app.FacetPagedSurface.WizardPaged;
import facets.facet.kit.Toolkit;
import facets.facet.kit.avatar.SwingPainterSource;
import facets.facet.kit.swing.KitSwing;
import facets.util.Debug;
import facets.util.Objects;
import facets.util.TextLines;
import facets.util.Tracer;
import facets.util.Util;
import facets.util.app.AppValues;
import facets.util.app.Events;
import facets.util.app.HostBounds;
import facets.util.tree.ValueNode;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import javax.swing.JApplet;
/**
{@link AppSpecifier} that builds and launches a {@link FacetAppSurface}. 
<p>A {@link FacetAppSpecifier} simplifies definition of a desktop application 
 by itself and by a {@link FacetAppSurface},  
{@link ViewerContenter} and {@link FacetFactory}. 
</ul>
<p>These components are assembled into a complete GUI surface as follows:
<ol>
<li>A {@link FacetAppSpecifier} is constructed in <code>main</code> (typically
redefining some of its default values) and {@link #buildAndLaunchApp(String[])}
is called. 
<li>The {@link FacetAppSpecifier} obtains from {@link #newApp(FacetFactory, FeatureHost)}
a concrete {@link FacetAppSurface} which will typically configure itself  
from  the {@link FacetAppSpecifier}. 
<li>The {@link FacetAppSurface} is launched via {@link AppWindowHost#openHostedSurface()}
in the return of {@link FacetAppSurface#host()}. 
<li>Either before or after launch, and as required based on user input,
 the {@link FacetAppSurface} creates {@link ViewerContenter}s in 
 {@link AppSurface#newContenter(Object)}, which in turn create the application content
 and define the user interface.
<li>In {@link ViewerContenter#newContentFeatures(SContentAreaTargeter)} 
a {@link facets.core.app.FeatureHost.LayoutFeatures} (typically a trivial subclass of
{@link FacetFactory}) defines content-specific surface features.
	</ol>
		*/
public abstract class FacetAppSpecifier extends AppSpecifier{
	/** Debug flag. */
	public static final String ARG_NO_FILES="noFiles",ARG_NO_CREATE="noCreate",
		ARG_PREFERENCES="prefs",ARG_RIBBON="ribbon";
	final public static int DRAG_WAIT_MIN=10;
	final Surfacer preferences=FacetPagedSurface.newDefaultTabbedSurfacer();
	private int graphWhereArg=-1;
	private PrintStream log;
	private File logFile;
	private void adjustLaunchPreferences(){
		FacetPreferences.openArgs(this);
		ValueNode args=args();
		if(args.getBoolean(ARG_PREFERENCES)){
			adjustValues();
			FacetFactory ff=newDesktopCore(newToolkit(),this);
			SSurface headless=Headless.newHeadlessSurface(appName);
			FacetPreferences prefs=newArgPreferences(headless,ff);
			PagedContenter[]pages=Objects.join(PagedContenter.class,
					prefs.newArgContenters(),
					adjustPreferenceContenters(headless,prefs.newContenters()));
			boolean abort=ff.launchOrphanDialog(new DialogSurfacer(){
					@Override
					public PagedSurface newSurface(String title,HideableHost host,PagedActions actions,
							PagedContenter[]contents,FacetFactory ff,AppSpecifier spec){
						return new WizardPaged(title,host,actions,contents,ff,spec);
					}
				},
				appName+" - |"+PATH_ARGS,WizardPaged.newActions(),pages
			)==Response.Cancel;
			if(abort){
				tryWriteValues(true?"":(ARG_PREFERENCES+" "+Debug.info(this)+" abort="+abort+": "));
				System.exit(0);
			}
			args.put(ARG_PREFERENCES,false);
			prefs.storeWizardArgs();
			nature().put(NATURE_PREFERENCES,true);
		}
		FacetPreferences.applyArgs(this);
		adjustValues();
	}
	/**
	Overrides superclass method to set class values in {@link FacetFactory} and elsewhere. 
	 */
	@Override
	public void adjustValues(){
		if(!hasSystemAccess())return;
		ValueNode args=args();
		graphBuild=args.getBoolean(ARG_GRAPH_BUILD);
		if(graphBuild)nature().put(NATURE_PREFERENCES,true);
		ValueNode debug=state(PATH_DEBUG);
		if(graphWhereArg<0){
			graphWhereArg=args.getInt(KEY_GRAPH_WHERE);
			if(graphWhereArg!=ValueNode.NO_INT)debug.put(KEY_GRAPH_WHERE,graphWhereArg);
		}
		graphShowWhere=debug.getOrPutInt(KEY_GRAPH_WHERE,graphShowWhere);
		dragNotifyInterim=debug.getOrPutBoolean(KEY_DRAG_NOTIFY,dragNotifyInterim);
		dragNotifyPause=debug.getOrPutInt(KEY_DRAG_PAUSE,dragNotifyPause);	
		TreeView.debug=debug.getOrPutBoolean(TreeView.KEY_DEBUG,TreeView.debug);
		HtmlView.showAllSources=debug.getOrPutBoolean(HtmlView.KEY_SOURCE,
				HtmlView.showAllSources);
		super.adjustValues();
		File userDir=!hasSystemAccess()?null:userDir();
		boolean userLog=userLog();
		if(userLog&&userDir!=null&&!userDir.getName().equals(DIR_DEV))try{
			logFile=new File(userDir,getClass().getSimpleName()+".log");
			log=new PrintStream(logFile);
			if(logFile.exists()){
				trace(": deleting logFile=",logFile.length());
				logFile.delete();
			}
			System.setOut(log);
			System.setErr(log);
			trace(": logFile=",logFile);
		}
		catch(FileNotFoundException e){
			throw new RuntimeException(e);
		}
		else if(false)trace(".userLog: userLog=",userLog);
	}
	protected boolean userLog(){
		return true;
	}
	final void closeLog(){
		if(log==null)return;
		log.close();
		try{
			trace(".closeLog: logFile="+logFile.length()+"\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n",
					new TextLines(logFile).readLinesString()+"\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		}
		catch(IOException e){
			throw new RuntimeException(e);
		}
	}
	/**
	Creates the {@link FacetPreferences} required if {@link #ARG_PREFERENCES} is passed to
	{@link #readValues(String...)}. 
	@param headless stands in for the non-existent parent app
	@param ff created using {@link #newToolkit()} with LaF as set by 
	{@link FacetConstants#NATURE_SWING_SYSTEM}
	@return by default a simple {@link FacetPreferences}
	 */
	protected FacetPreferences newArgPreferences(SSurface headless,FacetFactory ff){
		if(forSlave()||!hasSystemAccess())throw new IllegalStateException(
				"Not permitted for "+Debug.info(this));
		else return new FacetPreferences(this,headless,ff);
	}
	public boolean headerIsRibbon(){
		return false;
	}
	final PagedContenter[]newPreferenceContenters(ActionAppSurface app){
		return adjustPreferenceContenters(app,
				new FacetPreferences(this,app,((FacetAppSurface)app).ff).newContenters());
	}
	public PagedContenter[]adjustPreferenceContenters(
			SSurface surface,PagedContenter[]contenters){
		return contenters;
	}
	public FacetAppSpecifier(Class nameClass){
		super(nameClass);
	}
	public FacetAppSpecifier(AppSpecifier master){
		super(master);
	}
	/**
	Defines default returns of {@link #newActions(ActionAppSurface)} 
	and {@link #contentStyle()}. 
	@return by default <code>true</code> unless contradicted using {@link #ARG_NO_FILES}  
	 */
	public boolean isFileApp(){
		return hasSystemAccess()&&!args().getBoolean(ARG_NO_FILES);
	}
	/**
	Re-implementation setting positive default. 
	@return by default <code>true</code> unless contradicted using {@link #ARG_NO_CREATE}  
	 */
	@Override
	public boolean canCreateContent(){
		return !args().getBoolean(ARG_NO_CREATE);
	}
	/**
	Re-implementation making instance checks. 
	@return by default <code>true</code> where {@link #canEditContent()} and {@link #isFileApp()} 
	both return <code>true</code>
	 */
	@Override
	public boolean canSaveContent(){
		return canEditContent()&&isFileApp();
	}
	public String[]argumentKeys(){
		return new String[]{ARG_GRAPH_BUILD,ARG_NO_CREATE,ARG_NO_FILES,ARG_PREFERENCES,
				ContentStyle.NATURE_KEY,NATURE_SWING_SYSTEM};
	}
	@Override
	protected void addNatureDefaults(ValueNode root){
		super.addNatureDefaults(root);
		mergeContents(root,Objects.join(Object.class,
				true?new Object[]{}:new Object[]{
			NATURE_WRITABLE+"="+false,
			NATURE_RECORD_RUNS+"="+false,
			NATURE_CONFIRM_CLOSE+"="+false,
			NATURE_OPEN_EMPTY+"="+false,
			NATURE_ENSURE_FIRST_ROOT_ACTIVE+"="+false,
			NATURE_DEBUG+"="+true,
			NATURE_RUN_WATCHED+"="+!Debug.natureDebug,
		},new Object[]{
			NATURE_PREFERENCES+"=true",
			NATURE_ICON_STORE_PATH+"=_image/icon_",
			NATURE_DEBUG+"="+true,
			HostBounds.NATURE_SIZE_MIN+"=400,200",
			HostBounds.NATURE_SIZER_DEFAULT+"=0.4,0.6",
			NATURE_ICON_PATH+"=_image/icon",
			NATURE_DOC_PATH+"=_doc",
		}));
	}
	/**
	Overrides superclass method.
	<p>Fills {@link AppValues#stateDebug} with defaults for {@link Events}. 
	 */
	@Override
	protected void addStateDefaults(ValueNode root){
		if(!hasSystemAccess())return;
		stateDebug.setContents(new Object[]{
				KEY_TRACE+"="+trace,
				KEY_EVENTS+"="+true,
				KEY_TIMES+"="+false,
				KEY_MEM+"="+false,
				KEY_ESTIMATE+"="+5000,
				KEY_TIMES_RESET+"="+1000,
				DEFAULT_FILTERS
			});
		mergeContents((ValueNode)descendantTyped(root,PATH_APP),new Object[]{
				FacetAppSurface.KEY_SPLASH+"="+false,
				FacetAppSurface.KEY_DEBUG_STATUS+"="+false
			}
		);
	}
	/**
	Valid reimplementation.  
	@return by default {@link FacetConstants#DECORATION_VALUES}
	 */
	@Override
	public Object[][]decorationValues(){
		return DECORATION_VALUES;
	}
	/**
	Implements abstract method. 
	@return by default either a {@link FacetAppActions} or a {@link FileAppActions}
	depending on {@link #isFileApp()}
	 */
	@Override
	protected AppActions newActions(ActionAppSurface app){
		return isFileApp()?new FileAppActions(app):new FacetAppActions(app);
	}
	/**
	Re-implementation looking at {@link ContentStyle#NATURE_KEY}. 
	@return by default {@link ContentStyle#SINGLE}
	 */
	@Override
	public ContentStyle contentStyle(){
		return ContentStyle.values()[args().getOrPutInt(ContentStyle.NATURE_KEY,0)];
	}
	/**
	Creates the GUI toolkit required for the application {@link FacetFactory}. 
	<p>Called from {@link #buildAndLaunchApp(String[])}
	@return a {@link Toolkit} for passing to 
	{@link FacetFactory#newDesktopCore(Toolkit, AppSpecifier)} 
	 */
	public Toolkit newToolkit(){
		return new KitSwing(true,false,args().getOrPutBoolean(NATURE_SWING_SYSTEM,true));
	}
	/**
	Calls {@link #newToolkit()}, {@link #newApp(FacetFactory, FeatureHost)} 
	and {@link FeatureHost#openHostedSurface()}.
	<p>If <code>args</code> include {@value #PATH_ARGS} a configuration dialog is
	launched enabling nature and state values to be overridden before opening;
	dialog pages are created by the return of {@link #newArgPreferences(SSurface, FacetFactory)}  
	@param args passed to {@link AppValues#readValues(String...)} 
	before launching dialog or opening app
	 */
	final public void buildAndLaunchApp(String[]args){
		readValues(args);
		adjustLaunchPreferences();
		FacetFactory ff=FacetFactory.newDesktopCore(newToolkit(),this);
		AppSurface app=newApp(ff,null);
		FeatureHost host=(FeatureHost)app.host();
		if(host==null)throw new IllegalStateException("Null host in "+Debug.info(this));
		else host.openHostedSurface();
	}
	final public void buildAppletHosted(JApplet applet,String[]args){
		readValues(args);
		KitSwing kit=(KitSwing)newToolkit();
		FacetFactory ff=FacetFactory.newDesktopCore(kit,this);
		newApp(ff,kit.newSwingAppletHost(applet)).openApp();
	}
	/**
	Create a {@link FacetAppSurface}. 
	<p>Parameters match {@link FacetAppSurface#FacetAppSurface(FacetAppSpecifier, FacetFactory)}
	and {@link AppSurface#host()}
	@param ff as created or passed from subclass  
	@param host as created or passed from subclass, or <code>null</code> 
	if {@link AppSurface} can create its own {@link SHost}
	@return a {@link FacetAppSurface} that will create content and the surface
	to expose it
	 */
	protected abstract FacetAppSurface newApp(FacetFactory ff, FeatureHost host);
}