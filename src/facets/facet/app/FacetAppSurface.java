package facets.facet.app;
import static facets.core.app.AppConstants.*;
import static facets.core.app.AppSurface.ContentStyle.*;
import static facets.core.app.Dialogs.Response.*;
import static facets.facet.FacetFactory.*;
import static facets.facet.app.FacetAppActions.*;
import static facets.util.Debug.*;
import static facets.util.app.AppValues.*;
import static java.lang.System.*;
import static javax.swing.SwingUtilities.*;
import facets.core.app.ActionAppSurface;
import facets.core.app.AppSpecifier;
import facets.core.app.AppSurface;
import facets.core.app.AppWindowHost;
import facets.core.app.AreaTargeter;
import facets.core.app.Dialogs;
import facets.core.app.FeatureHost;
import facets.core.app.MenuFacets;
import facets.core.app.MountFacet;
import facets.core.app.PagedActionDefaults;
import facets.core.app.PagedActions;
import facets.core.app.PagedContenter;
import facets.core.app.SAreaTarget;
import facets.core.app.SContentAreaTargeter;
import facets.core.app.SurfaceServices;
import facets.core.app.ViewerContenter;
import facets.core.app.Dialogs.Surfacer;
import facets.core.app.FeatureHost.LayoutFeatures;
import facets.core.superficial.Notice;
import facets.core.superficial.Notifying;
import facets.core.superficial.SFacet;
import facets.core.superficial.SIndexing;
import facets.core.superficial.STarget;
import facets.core.superficial.STargeter;
import facets.core.superficial.STextual;
import facets.core.superficial.TargeterCore;
import facets.core.superficial.Notifying.Impact;
import facets.core.superficial.STextual.Coupler;
import facets.core.superficial.app.FacetedTarget;
import facets.core.superficial.app.SHost;
import facets.core.superficial.app.SSurface;
import facets.core.superficial.app.SHost.FacetLayout;
import facets.facet.AppFacetsBuilder;
import facets.facet.FacetFactory;
import facets.facet.FacetFactory.SimpleServices;
import facets.facet.app.FacetAppActions.BarHide;
import facets.facet.kit.Toolkit;
import facets.facet.kit.swing.KitSwing;
import facets.util.Debug;
import facets.util.FileSpecifier;
import facets.util.Times;
import facets.util.TypesKey;
import facets.util.Util;
import facets.util.app.AppFileValues;
import facets.util.app.AppServices;
import facets.util.app.AppWatcher;
import facets.util.app.Events;
import facets.util.app.HostBounds;
import facets.util.app.WatchableOperation;
import facets.util.app.ProvidingCache.ItemProvider;
import facets.util.tree.ValueNode;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
/** 
{@link ActionAppSurface} with a built-in {@link FacetFactory}. 
<p>{@link FacetAppSurface} extends its superclass primarily by 
	passing an {@link FacetAppSpecifier} to be set as {@link AppSurface#spec};
	this in turn supplies the {@link FacetFactory} set as {@link #ff}. 
	<p>Further general-purpose functionality is supplied by the  
	{@link FacetAppActions} or {@link FileAppActions}
	 set as {@link ActionAppSurface#actions}.  
 */
public abstract class FacetAppSurface extends ActionAppSurface implements SurfaceServices{
	/** Key for debug value. */
	final public static String KEY_SPLASH="splashScreen",KEY_DEBUG_STATUS="debugStatus";
	/**
	For use by subclasses and {@link facets.core.app.AppContenter}s.
	<p>Can be used as-is or provide a core for content-specific subclasses
	to be used as {@link LayoutFeatures}. 
	 */
	public final FacetFactory ff;
	private final Runnable dialogLaunchOnOpen=new Runnable(){
		private boolean launchPreferences=false;
		{
			if(launchPreferences)spec.nature().put(NATURE_PREFERENCES,true);
		}
		public void run(){
			if(launchPreferences)launchPreferences();
			else if(false)actions.app.launchAbout();
	}};
	private final List<SIndexing>debugIndexings=new ArrayList();
	private final STargeter debugText;
	private SHost host;
	private Dialogs dialogs;
	/**
	Unique constructor. 
	 @param ff set as {@link #ff} after being passed to 
	 constructor as {@link AppServices}
	 */
	public FacetAppSurface(FacetAppSpecifier spec,FacetFactory ff){
		super(spec,ff);
		this.ff=ff;
		if(false)throw new RuntimeException("Not tested in "+this);
		debugText=TargeterCore.newRetargeted(
				new STextual(title()+".debugText","",new Coupler()),true);
	}
	@Override
	final protected void traceOutput(String msg){
		msg=FacetAppSurface.class.getSimpleName()+msg;
		if(true)Util.printOut(msg);
		else if(false)Times.printElapsed(msg);
	}
	final public boolean hideLayoutBar(BarHide bar){
		return bar.readHide(spec.state(PATH_APP),activeFeatures());
	}
	@Override
	final public void notify(Notice notice){
		BarHide.updateTogglings(this,activeFeatures());
		if(notice==null)throw new IllegalArgumentException("Null notice in "+Debug.info(this));
		else if(notice.impact==Impact.MINI){
			notifiedFlash(notice);
			return;
		}
		else super.notify(notice);
	}
	@Override
	final protected MountFacet newMultiContentFacet(SAreaTarget appArea){
		return ff.areas().appMultiContentFacet(appArea,this);
	}
	@Override
	final protected void appRetargeted(){
		super.appRetargeted();
		int switchAt=spec.state(PATH_APP).getBoolean(KEY_DEBUG_STATUS)?1:0;
		for(SIndexing each:debugIndexings)each.setIndex(switchAt);
		((STextual)debugText.target()).setText(newDebugText());
		debugText.retargetFacets(Impact.DEFAULT);
		SHost host=host();
		host.setTitle(newTitleBarText());
		((FeatureHost)host).showExtras(actions.showHelp.isSet()||graphBuild&&graphShowWhere==GRAPH_APP);
	}
	/**
	Overrides superclass implementation. 
	<ol>
	<li>Creates or gets the {@link FeatureHost} and obtains from it a 
	suitable {@link Dialogs}. 
	<li>Issues any {@link AppWatcher} warnings. 
	<li>Possibly puts up a splash panel and calls the super-implementation.
	<li>If applet-hosted, calls {@link #appOpened()} and returns
	<li>Otherwise opens app window, attempts to add content to an empty desktop,
	pulls down any splash panel, calls {@link #appOpened()}. 
	</ol>	
	 */
	@Override
	final public void openApp(){
		Events.traceEvent(">Getting host for "+info(this));
		if(watcher!=null&&contentStyle!=DESKTOP)checkTimeouts();
		final SHost host=host();
		boolean windowed=host instanceof AppWindowHost;
		if(windowed&&(dialogs=((AppWindowHost)host).newDialogs(this))==null)
			throw new IllegalStateException("Null dialogs in "+Debug.info(this));
		Events.traceEvent(">Got host "+info(host));
		final ValueNode nature=spec.nature(),stateApp=spec.state(PATH_APP);
		if(windowed){
			if(stateApp.getBoolean(KEY_SPLASH))
				((AppWindowHost)host).splashUp("Opening "+title()+" - Facets "+
						FacetFactory.version()+"...");
		}
		super.openApp();
	  if(nature.getBoolean(NATURE_ENSURE_FIRST_ROOT_ACTIVE)){
			((FacetedTarget)((SAreaTarget)surfaceTargeter().areaAt(AreaTargeter.AREA_APP).target()
  			).indexableTargets()[0]).ensureActive(Impact.ACTIVE);
			notify(null);
		}
		if(!windowed){
			appOpened();
	  	return;
	  }
		host.setTitle(newTitleBarText());
		((AppWindowHost)host).openWindow();
		final boolean inSwt=!(ff.kit instanceof KitSwing);
		WatchableOperation finishOpen=new WatchableOperation("FacetedAppSurface.finishOpen"){
		public void doSimpleOperation(){
			if(findActiveContent()==emptyContent&&!nature.getBoolean(NATURE_OPEN_EMPTY)){
				if(actions instanceof FileAppActions){
					beforeContent();
					File file=null;
					if(((FileAppActions)actions).values().recentFiles().length==0)
						actions.app.launchAbout();
					else file=actions.getOpeningContentSourceFile();
					if(file!=null&&file.isFile())addContent(file);
				}
				else{
					beforeContent();
					for(Object s:getFixedOpeningContentSources())addContent(s);
				}
			}
			else beforeContent();
			if(inSwt)dialogLaunchOnOpen.run();
			else invokeLater(dialogLaunchOnOpen);
			appOpened();
		}
		private void beforeContent(){
			((AppWindowHost)host).splashDown();
			if(watcher!=null)checkTimeouts();
		}};
		if(inSwt)finishOpen.doOperations();
		else runWatchedLater(finishOpen);
	}
	public final String areaTitle(int depth){
		String area=surfaceTargeter().areaAt(depth).title();
		return area.equals(TITLE_EMPTY)?"No Content":area;
	}
	/**
	Launches a preferences dialog exposing session state values. 
	<p>Calls {@link Dialogs#launchSurfaced(Surfacer, String, PagedActions,PagedContenter...)}
	with parameters returned by {@link FacetAppSpecifier} 
	 */
	@Override
	protected final void launchPreferences(){
	  FacetAppSpecifier facetSpec=(FacetAppSpecifier)spec;
		dialogs().launchSurfaced(facetSpec.preferences,TITLE_APP_PREFERENCES,
				PagedActionDefaults.newApplyOkCancel(),facetSpec.newPreferenceContenters(this));
	}
	@Override
	public final boolean attemptClose(){
		ValueNode nature=spec.nature();
		ActionAppSurface master=slaveMaster();
		boolean isSlave=master!=null,canClose=actions.appCloseAcceptable();
		if(!isSlave)canClose&=!nature.getBoolean(NATURE_CONFIRM_CLOSE)
				||dialogs().confirmOKCancel(TITLE_CLOSE_REQUESTED,RUBRIC_CONFIRM_CLOSE)==Ok;
		if(!canClose)return false;
		AppWindowHost host=(AppWindowHost)host();
		HostBounds appBounds=host.newBounds();
		if(isSlave){
			appBounds.putBounds();
			host.closeWindow();
			master.slaveClosed(this);
			return true;
		}
		else if(!closeSlaves())return false;
		appBounds.putBounds();
		String stateSave=NATURE_CONFIRM_STATE_SAVE;
		if(!nature.getBoolean(stateSave)||dialogs().confirmYesNo(stateSave,stateSave)!=Yes)
			spec.tryWriteValues("");
		removeAllContent(true);
		appClosing();
		host.closeWindow();
		((FacetAppSpecifier)spec).closeLog();
		System.exit(0);
		return true;
	}
	final public void debugWatch(final String callName,final boolean providing,
			final boolean exception){
		if(!Debug.natureDebug)return;
		final ItemProvider op=new ItemProvider(ff.providingCache(),this,
				"debugWatch."+(providing?"providing.":"")+callName){
			@Override
			public CancelStyle cancelStyle(){
				return providing?CancelStyle.Timeout:CancelStyle.None;
			}
			@Override
			protected Object newItem(){
				boolean runOn=!exception;
				long startAt=currentTimeMillis(),timeoutBlock=3*1000,
					breakAt=startAt+(runOn?timeoutBlock*200:timeoutBlock/2),thenAt=startAt,nowAt;
				while(true)if((nowAt=currentTimeMillis())-thenAt>(runOn?2000:500)){
					if(false)Util.printOut(this+" " +(nowAt-startAt)/1000+
							" providing="+providing+" throwException="+exception);
					thenAt=nowAt;
					if(nowAt<breakAt)continue;
					else if(exception)throw new RuntimeException(callName+" "+this);
					else break;
				}
				return null;
			}
			@Override
			protected long buildByteCount(){
				return 0;
			}
		};
		if(providing)op.getForValues(this);
		else runWatched(op);
	}
	@Override
	final public Object runWatched(WatchableOperation r){
		return watcher!=null?watcher.runWatched(r):r.doOperations();
	}
	final public void runWatchedLater(final WatchableOperation r){
		invokeLater(new Runnable(){public void run(){runWatched(r);}});
	}
	protected CachingStyle cachingStyle(){
		return CachingStyle.checkItemCount;
	}
	@Override
	protected LayoutFeatures newEmptyDesktopFeatures(final SContentAreaTargeter area){
		return new FacetFactory(ff){
			public SFacet[]header(){
				return new SFacet[]{
					menuRoot(new AppFacetsBuilder(this,area).newMenuFacets()),
					menuRoot(helpMenuFacets(area)),
				};
			}
			@Override
			public SFacet extras(){
				return appExtras(FacetAppSurface.this);
			}
		};
	}
	/**
	Called from {@link #notify(Notice)} when the {@link Notice} passed
	has {@link Impact#MINI}. 
	<p>No other response is made to such notifications. 
	<p>Default...   
	 */
	protected void notifiedFlash(Notice notice){
		if(true||findActiveContent()==emptyContent)return;
		STargeter selection=activeContentTargeter().selection();
		((STextual)selection.elements()[0].target()).setText(notice.flashText());
		selection.retargetFacets(notice.impact);
	}
	final public SFacet newDebugSwitchLabel(SFacet client){
		MountFacet mount=ff.switchMount(title()+".debugSwitchLabel");
		mount.setFacets(client,ff.textualLabel(debugText,HINT_NONE));
		debugIndexings.add(FacetFactory.switchMountIndexing(mount,new SIndexing.Coupler()));
		return mount;
	}
	/**
	Implements interface method. 
	<p>To ensure the request is met by the active {@link FacetLayout},
	delegates to the {@link SurfaceServices} returned by 
	{@link FeatureHost#activeServices()} in {@link #host()}.  
	 */
	@Override
	final public MenuFacets getContextMenuFacets(){
		SurfaceServices services=((FeatureHost)host()).activeServices();
		return services==null?null:services.getContextMenuFacets();
	}
	/**
	Implements interface method. 
	<p>To ensure the request is met by the active {@link FacetLayout},
	delegates to the {@link SurfaceServices} returned by 
	{@link FeatureHost#activeServices()} in {@link #host()}.  
	 */
	@Override
	final public void handleInvalidInput(STarget target,Object input){
		SurfaceServices services=((FeatureHost)host()).activeServices();
		if(services==null)throw new IllegalStateException(
				"Null services in "+Debug.info(this));
		else services.handleInvalidInput(target,input);
	}
	/**
	Supplies a full implementation of {@link SurfaceServices}. 
	<p>Enables a {@link LayoutFeatures}to provide
	 a valid implementation of {@link SurfaceServices#handleInvalidInput(STarget, Object)}
	 (by contrast with {@link SimpleServices}.
	@param contextFacets will ultimately be returned by 
	{@link #getContextMenuFacets()}
	 */
	final public SurfaceServices newFullServices(final MenuFacets contextFacets){
		return new SurfaceServices(){
			public MenuFacets getContextMenuFacets(){
				return contextFacets;
			}
			final public void handleInvalidInput(STarget target,Object input){
				String text="Invalid input " +input+" for "+target.title();
				STargeter status=null;
				if(true)throw new RuntimeException("Not tested in "+Debug.info(this));
				if(status==null||BarHide.Status.readHide(spec.state(PATH_APP),TypesKey.EMPTY))
					dialogs().errorMessage(title(),text);
				else{
					((STextual)status.target()).setText(text);
					status.retargetFacets(Notifying.Impact.DEFAULT);
				}
			}
		};
	}
	/**
	The {@link Dialogs} created during {@link #openApp()}. 
	 */
	@Override
	public final Dialogs dialogs(){
		if(dialogs==null)throw new IllegalStateException("Null dialogs in "+info(this));
		return dialogs;
	}
	/**
	Returns the app's {@link SHost}. 
	@return memo of
<ul>
	<li>if {@link #isHeadless()} returns <code>true</code>: {@link #newHeadlessHost()}
	<li>if return of {@link AppSpecifier#hasSystemAccess()} is <code>false</code>
		: {@link #getPassedHost()}
	<li>otherwise: {@link Toolkit#newWindowHost(SSurface.WindowAppSurface,AppSpecifier)}
	</ul>
		 */
	@Override
	final public SHost host(){
		return host!=null?host:(host=isHeadless()?newHeadlessHost()
				:!spec.hasSystemAccess()&&!spec.forSlave()?getPassedHost()
						:ff.newWindowHost(this));
	}
	protected FeatureHost getPassedHost(){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	final public void setLayoutTargetsLive(boolean live){
	  if(false)surfaceTargeter().elements()[TARGETS_LAYOUT].target().setLive(live);
	}
	/**
	Implements abstract method. 
	<p>Default is invalid stub. 
	 */
	@Override
	protected Object getInternalContentSource(){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	/**
	What files can the application open?
	<p>Called from {@link AppFileValues#getOpenSpecifiers()} in {@link FileAppActions} 
	and typically from {@link ViewerContenter#sinkFileSpecifiers()};
	default returns {@link FileSpecifier#ALL}. 
	 */
	public FileSpecifier[]getFileSpecifiers(){
		return new FileSpecifier[]{FileSpecifier.ALL};
	}
	/**
	Implements abstract method. 
	<p>Default adjusts title bar to shown save warning. 
	 */
	protected String newTitleBarText(){
		return (((FacetAppSpecifier)spec).isFileApp()
			&&spec.canEditContent()&&findActiveContent().hasChanged()?"*":"")
				+areaTitle(AreaTargeter.AREA_CONTENT)+" - "+title();
	}
	final protected String newDebugText(){
		return newCountsText();
	}
	/**
	Called from {@link #openApp()}. 
	<p>Default is empty stub. 
	 */
	protected void appOpened(){}
	/**
	Called before a <code>true</code> return from {@link #attemptClose()}. 
	<p>Default is empty stub. 
	 */
	protected void appClosing(){}
}
