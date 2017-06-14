package facets.core.app;
import static facets.core.app.ActionAppSurface.CachingStyle.*;
import static facets.core.app.AppConstants.*;
import static facets.core.app.AppSurface.ContentStyle.*;
import static facets.core.app.Dialogs.Response.*;
import static facets.util.app.AppValues.*;
import facets.core.app.Dialogs.ExceptionTexts;
import facets.core.app.Dialogs.Surfacer;
import facets.core.superficial.Notice;
import facets.core.superficial.STarget;
import facets.core.superficial.TargetCore;
import facets.core.superficial.Notifying.Impact;
import facets.core.superficial.app.AreaTargeter;
import facets.core.superficial.app.SSurface;
import facets.core.superficial.app.SSurface.WindowAppSurface;
import facets.util.Debug;
import facets.util.Objects;
import facets.util.app.AppServices;
import facets.util.app.AppWatcher;
import facets.util.app.MemoryChecks;
import facets.util.app.ProvidingCache;
import facets.util.app.WatchableOperation;
import facets.util.app.WatcherCoupler;
import facets.util.app.ProvidingCache.ItemProvider;
import facets.util.app.ProvidingCache.ItemValuer;
import facets.util.app.WatchableOperation.CancelStyle;
import facets.util.tree.ValueNode;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
/**
{@link AppSurface} defining application actions and management. 
<p>{@link ActionAppSurface} extends its superclass by composition with  
<ul>
<li>an {@link AppActions} that creates root target elements and provides
an independently extensible contract. 
<li>an {@link AppWatcher} supplied by the {@link AppServices} passed to the constructor
and coupled to a private {@link WatcherCoupler}
<li>a private {@link ProvidingCache} initialised with a {@link MemoryChecks}
</ul>
<p>{@link ActionAppSurface} also provides mechanism for management of slave
{@link ActionAppSurface}s hosted by their own top-level windows.  
 */
public abstract class ActionAppSurface extends AppSurface{
	/** Key for debug value. */
	final public static String KEY_TIMEOUTS="timeoutChecks",KEY_TIMED_OUT="timedOut",
		KEY_EXTRAS_SPLIT="extrasSplit",KEY_SHOW_EXTRAS="showExtras";
	public static final int BLOCKING_TITLE_0=0,BLOCKING_RUBRIC_1=1,
		CANCEL_TITLE_2=2,CANCEL_RUBRIC_3=3;
	/** 
	Defines management style for {@link ProvidingCache}. 
	 */
	public enum CachingStyle{noCache,passThrough,checkItemCount,checkMemory}
	final public AppActions actions;
	public final AppWatcher watcher;
	private final boolean debugWatch;
	private final Set<ActionAppSurface>slaves=new HashSet();
	private ActionAppSurface master;
	final protected ActionAppSurface slaveMaster(){
		return master;
	}
	public final void openSlave(ActionAppSurface slave){
		if(contentStyle==DESKTOP)throw new IllegalStateException(
				"Can't add slaves: contentStyle="+contentStyle);
		else if(slave.contentStyle==DESKTOP)throw new IllegalStateException(
				"Can't add slave: contentStyle="+slave.contentStyle);
		else slave.master=this;
		slaves.add(slave);
		FeatureHost host=(FeatureHost)slave.host();
		if(host==null)throw new IllegalStateException("Null host in "+Debug.info(slave));
		else host.openHostedSurface();
	}
	final public void slaveClosed(ActionAppSurface slave){
		if(!slaves.remove(slave))throw new IllegalStateException(
				"Not a slave: "+Debug.info(slave));
	}
	final protected boolean closeSlaves(){
		if(slaves.size()==0)return true;
		Set<ActionAppSurface>slaves=new HashSet(this.slaves);
		for(ActionAppSurface slave:slaves)if(!slave.attemptClose())return false;
		return true;
	}
	/**
	Unique constructor. 
	@param spec set as final field
	@param services supplies an {@link AppWatcher} and application {@link ProvidingCache} 
	 */
	protected ActionAppSurface(AppSpecifier spec,AppServices services){
		super(spec);
		if((this.actions=spec.newActions(this))==null)throw new IllegalArgumentException(
				"Null actions in "+Debug.info(this));
	  final ValueNode stateApp=spec.state(PATH_APP),nature=spec.nature();
		boolean watchable=spec.hasSystemAccess(),
			runWatched=watchable&&nature.getBoolean(NATURE_RUN_WATCHED);
		debugWatch=runWatched&&nature.getBoolean(NATURE_RUN_WATCHED_DEBUG);
		if(debugWatch){
			if(!Debug.natureDebug)throw new IllegalStateException(
					"debugWatch=true in "+Debug.info(this));
			stateApp.put(KEY_TIMED_OUT,false);
			stateApp.put(KEY_TIMEOUTS,false);
		}
		watcher=!runWatched?null
			:services.coupleAppWatcher(constructWatcherCoupler(stateApp,services));
		final CachingStyle caching=cachingStyle();
		if(caching!=noCache)services.setAppProvidingCache(caching==checkMemory?
			new ProvidingCache(new MemoryChecks(spec){
				@Override
				protected int headroomMb(int maxMemMb){
					return cacheHeadroomMb(maxMemMb);
				}
			},watcher){
				@Override
				protected ItemValuer getItemValuer(ItemProvider p,Object[]itemValues){
					return newCacheItemValuer(p,itemValues);
				}
			}
			:new ProvidingCache(caching==passThrough?ProvidingCache.PASS_THROUGH
					:cacheCountMax(),watcher){
				@Override
				protected boolean doTrace(){
					return false;
				}
			}
		);
	}
	/**
	Supplies the {@link Dialogs} which will launch dialogs from this application surface. 
	 */
	public abstract Dialogs dialogs();
	/**
	Launch a preferences dialog exposing session state values. 
	 */
	protected abstract void launchPreferences();
	private WatcherCoupler constructWatcherCoupler(final ValueNode stateApp,
			final AppServices services){
		return new WatcherCoupler(){
			private WatchableOperation opThen;
			@Override
			protected void handleSystemTimeout(WatchableOperation watched,Thread worker,
					boolean wait){
				stateApp.put(KEY_TIMED_OUT,true);
				spec.tryWriteValues("Timeout flag set. ");
				super.handleSystemTimeout(watched,worker,wait);
			}
			@Override
			protected boolean checkTimeouts(){
				return stateApp.getBoolean(KEY_TIMEOUTS);
			}
			@Override
			protected int systemSec(){
				return 60;
			}
			@Override
			protected void handleException(List<WatchableOperation>ops,Exception e){
				if(false&&debugWatch)e.printStackTrace();
				else try{
					opThen=ops.get(0);
					handleWatchCall(actions, ops,e!=null?e
							:new Exception("Cause unknown"),false,!isBuilt());
				}catch(Exception ex){
					if(debugWatch)throw new RuntimeException(ex); 
					else lastWarning(e);
				}
			}
			@Override
			protected boolean retryCancel(List<WatchableOperation>ops){
				WatchableOperation op=ops.get(0);
				boolean firstRetry=opThen!=op;
				opThen=op;
				try{
					return handleWatchCall(actions, ops,null,firstRetry,!isBuilt());
				}catch(Exception e){
					if(debugWatch)throw new RuntimeException(e); 
					else lastWarning(e);
				}
				return false;
			}
			private boolean handleWatchCall(AppActions actions, List<WatchableOperation> ops, 
					Exception e, boolean firstRetry, boolean inOpen){
				String title=title();
				Dialogs dialogs=dialogs();
				WatchableOperation op=ops.get(0);
				String[]texts=op.getBlockingCancelTexts();
				boolean error=e!=null,detail=true&&Debug.natureDebug,
					cancel=op.cancelStyle()==CancelStyle.Dialog;
				if(!error){
					return cancel?dialogs.confirmYesNo(texts[CANCEL_TITLE_2],
							texts[CANCEL_RUBRIC_3])!=No 
					:dialogs.warningYesNo("Operation Timed Out",
						(detail?("Operation " +op):(firstRetry?"An operation":"The operation"))
								+(firstRetry?(" has timed out. If you do not wait for it to complete,<br>" +
								" $appTitle may slow down or close without warning.<br>&nbsp;<br>" +
								"Wait for the operation to complete?")
										:" has still not completed. <br>Carry on waiting?")
						)!=No;
				}
				ExceptionTexts tt=new ExceptionTexts("Operation Failed",
					("An operation has failed" +(detail?" as shown below. <br>":". <br>")), 
					!detail?"":("Operation failed in\n" +Objects.toString(ops.toArray(),"\n")+
							"\ndue to: \n"+e+"\n"),
					(detail?"":("If you do not exit now, "+title+" may slow down or close without warning.<br>")) +
							"Exit "+title+"?");
				if(dialogs.warningException(tt,e,inOpen)==Yes)attemptClose();
				return false;
			}
			private void lastWarning(Exception e){
				services.warningCritical(title(),e,true);
			}
		};
	}
	/**
	Encapsulates checking and updating timeout flags.
	<p>Not called within {@link ActionAppSurface} but provides for checking by subclasses.  
	 */
	final protected void checkTimeouts(){
		ValueNode stateApp=spec.state(PATH_APP);
		final Dialogs dialogs=dialogs();
		if(false||stateApp.getBoolean(KEY_TIMED_OUT)){
			dialogs.infoMessage(KEY_TIMED_OUT,KEY_TIMED_OUT);
			stateApp.put(KEY_TIMEOUTS,true);
		}
		stateApp.put(KEY_TIMED_OUT,false);
		if(stateApp.getBoolean(KEY_TIMEOUTS)
				&&dialogs.warningYesNo(KEY_TIMEOUTS,KEY_TIMEOUTS)==Dialogs.Response.No)
			stateApp.put(KEY_TIMEOUTS,false);
	}
	protected int cacheHeadroomMb(int maxMemMb){
		return 20;
	}
	protected int cacheCountMax(){
		return 10;
	}
	protected abstract CachingStyle cachingStyle();
	protected ItemValuer newCacheItemValuer(ItemProvider p, Object[] itemValues){
		return new ItemValuer();
	}
	final TargetCore slaveOrMaster=new TargetCore("Slave or master");
	@Override
	public void notify(final Notice notice){
		if(notice.sources.contains(slaveOrMaster))return;
		actions.updateWindowTops();
		WatchableOperation op=new WatchableOperation("ActionAppSurface.notify"){
			@Override
			public void doSimpleOperation(){
				ActionAppSurface.super.notify(notice);
				if(master!=null)master.notify(notice.addSource(slaveOrMaster));
				if(false)for(ActionAppSurface slave:slaves)slave.notify(
						notice.addSource(slaveOrMaster));
			}};
			if(true)runWatched(op);
			else op.doOperations();
	}
	/**
	Re-implementation calling {@link AppActions#appRetargeted()}. 
	 */
	@Override
	protected void appRetargeted(){
		actions.appRetargeted();
	}
	/**
	Implements abstract method. 
	<p>Returns targets created by {@link AppActions#newAppAreaElements()}. 
	 */
	@Override
	protected STarget[]lazyAppAreaElements(){
		STarget[]targets=actions.newAppAreaElements();
		if(targets==null)throw new IllegalStateException(
				"Null targets in "+Debug.info(this));
		return targets;
	}
	/**
	Implements abstract method. 
	@return response of {@link AppActions#contentIsRemovable(String, AppContenter)}
	 */
	@Override
	final public boolean contentIsRemovable(AppContenter content){
		return actions.contentIsRemovable(AppConstants.TITLE_CLOSE_CONTENT,content);
	}
	/**
	Implements interface method. 
	 */
	@Override
	final protected Object[]getOpeningContentSources(){
		if(contentStyle==DESKTOP)return new Object[]{DESKTOP};
		else if(!spec.hasSystemAccess())return getFixedOpeningContentSources();
		File file=actions.getOpeningContentSourceFile();
		return file!=null&&file.isFile()?new Object[]{file}
			:getFixedOpeningContentSources();
	}
	protected Object[]getFixedOpeningContentSources(){
		return new Object[]{getInternalContentSource()};
	}
	final public void addInternalContent(){
		Object source=getInternalContentSource();
		if(contentStyle!=SINGLE)addContent(source);
		else if(contentIsRemovable(findActiveContent()))
			replaceSingleContent(source);
		else return;
		if(false)notify(new Notice(surfaceTargeter(),Impact.DEFAULT));
	}
	/**
	Return a suitable source for default content, or the content itself.  
	<p>Called from {@link AppActions} and from default {@link #getFixedOpeningContentSources()}
	 */
	protected abstract Object getInternalContentSource();
	/**
	Cover for {@link AppSurface#removeActiveArea()}. 
	 */
	final public boolean tryCloseContent(){
		return removeActiveArea();
	}
	public boolean canCloseContent(){
		return true;
	}
	/**
	Encapsulates reverting the current content. 
	 */
	final public void revertContent(){
		AppContenter content=findActiveContent();
		if(true&&content.hasChanged()&&dialogs().confirmYesNo("Reload content?",
				"Reload " +content.title()+"?")!=Yes)
			return;
		revertActiveContent();
	}
	/**
	Launch an 'about' pane.
	 */
	final public void launchAbout(){
		String name=spec.appName;
		dialogs().htmlPane(TITLE_APP_ABOUT+" "+name,spec.nature().getString(NATURE_DOC_PATH)
				+"/"+name+".html");
	}
	/**
	Convenience method calling {@link FeatureHost#updateLayout(SSurface)}. 
	 */
	final public void updateLayout(){
		((FeatureHost)host()).updateLayout(this);
	}
}
