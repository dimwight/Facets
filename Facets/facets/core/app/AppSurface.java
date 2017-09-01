package facets.core.app;
import static facets.core.app.AppAreas.*;
import static facets.core.app.AppSurface.ContentStyle.*;
import static facets.util.Debug.*;
import static facets.util.app.Events.*;
import facets.core.app.FeatureHost.LayoutFeatures;
import facets.core.superficial.Notice;
import facets.core.superficial.SFrameTarget;
import facets.core.superficial.STarget;
import facets.core.superficial.STargeter;
import facets.core.superficial.Notifying.Impact;
import facets.core.superficial.app.AreaTargeter;
import facets.core.superficial.app.SAreaTarget;
import facets.core.superficial.app.SContentAreaTargeter;
import facets.core.superficial.app.SContenter;
import facets.core.superficial.app.SHost;
import facets.core.superficial.app.SSurface;
import facets.core.superficial.app.SContentAreaTargeter.ContentArea;
import facets.core.superficial.app.SHost.FacetLayout;
import facets.core.superficial.app.SSurface.WindowAppSurface;
import facets.util.Tracer;
import facets.util.TypesKey;
import facets.util.app.AppValues;
import facets.util.app.AppWatcher;
import facets.util.app.WatchableOperation;
import java.util.ArrayList;
import java.util.List;
/**
Implements {@link SSurface} for single- or multi-content applications. 
<p>{@link AppSurface} maintains a set of {@link SContenter} members 
	and an internal surface area root containing content area roots exposing 
	these members; a wide range of methods enable addition and removal of 
	both roots and content. 
<p>Depending on {@link AppSpecifier#contentStyle()} in the {@link AppSpecifier} 
passed to the core constructor, {@link AppSurface} 
	can build an application whose content is exposed three ways:</p>
<ul>
	<li><code>SINGLE</code> - single content and content root
	<li><code>TABBED</code> - multiple content exposed by content 
		roots in folder tabs
	<li><code>DESKTOP</code> - multiple content (potentially of multiple types)
	roots exposed by windows within internal desktop
</ul>
<p>Once the surface is built, the <code>add/removeXXX</code> methods can 
	be called to perform operations on both the content roots and their underlying 
	{@link SContenter}s. 
 */
public abstract class AppSurface extends Tracer implements WindowAppSurface{
	/** Possible styles for the content area of an {@link AppSurface}. 
	<p>Styles are defined in terms of content presentation: 
	<ul><li>{@link #SINGLE} - single (possibly replaceable) content
	<li>{@link #TABBED} - multiple content in tabs
	<li>{@link #DESKTOP} - multiple content in windows of internal desktop
	</ul>
	 */
	public static enum ContentStyle{
		SINGLE,TABBED,DESKTOP;
		/**
		Key for configuration. 
		 */
		public static final String NATURE_KEY="contentStyle";
	}
	/**Title of {@link #emptyContent}.*/
	public static final String TITLE_EMPTY="Empty Area";
	/**
  Non-existent content of an "empty" {@link ContentStyle#DESKTOP} 
  application. 
  <p>When returned by {@link AppSurface#findActiveContent()} serves as a flag.   
   */
  public final AppContenter emptyContent=new EmptyContenter(this);
	/** Immutable {@link AppSpecifier} for the application. */
	final public AppSpecifier spec;
	/** Immutable {@link ContentStyle} for the application.*/
	public final ContentStyle contentStyle;
	final AppAreas areas;
	private final List<AppContenter>contents=new ArrayList();
	/**
  Constructs an application surface. 
  <p>The surface may be empty if {@link #contentStyle} is <code>DESKTOP</code>
  	otherwise will be initialised with content constructed from return of 
  	{@link #getOpeningContentSources()}). 
	<p>The surface can form the basis of either a free-standing application that 
	  creates its own window and thus its own 
	  {@link facets.core.superficial.app.SHost.FacetLayout}, 
	  or one created within and by its {@link SHost} such as 
	  an applet. 
	<p>Application build sequence is as follows:</p>
	<ol>
	  <li>Create a suitable {@link AppSurface} in <code>main</code> 
	    if free-standing, in <code>init</code> if applet-hosted.</li>
	  <li>Call {@link #openApp()} on the {@link AppSurface}. </li>
	  <li>The {@link AppSurface} calls {@link SSurface#buildRetargeted()} to build facet 
	    layouts which it passes the host. </li>
	  </ol>
   */
  protected AppSurface(AppSpecifier spec){
  	if((this.spec=spec)==null)throw new IllegalArgumentException(
  			"Null values in "+info(this));
		else contentStyle=spec.contentStyle();
		traceEvent(">Created surface "+info(this)+" contentStyle="+contentStyle);
  	areas=new AppAreas(this);
  }
	@Override
	public void notify(Notice notice){
		if(trace)traceEvent(">Surface "+(isSlave()?"[slave] ":"")
				+info(this)+" notified with "+notice);
	  AreaTargeter targeter=surfaceTargeter();
	  SContentAreaTargeter activeThen=activeContentTargeter(),activeNow;
	  boolean empty=contents.contains(emptyContent);
	  STargeter viewerThen=empty?null:activeThen.viewer();
	  Impact impact=notice.impact;
	  targeter.retarget(targeter.target(),impact);
	  activeNow=activeContentTargeter();
	  String retargeted=" retargeted in "+info(this)+" for "+info(notice);
		if(trace)traceEvent(">Targeters" +retargeted);
		if(!empty){
			for(AppContenter content:contents)
				if(((ViewerContenter)content).contentFrame()==activeNow.content().target())
					content.areaRetargeted(activeNow);
			if(activeThen!=activeNow||viewerThen!=activeNow.viewer())areas.updateLayout(contents);
	  }
	  appRetargeted();
	  if(contentStyle==TABBED){
	  	SAreaTarget appArea=areas.appArea();
	  	STarget indexed=appArea.indexedTarget();
	  	for(STarget area:appArea.indexableTargets())area.setLive(area==indexed);
	  }
	  targeter.retargetFacets(impact);
	  if(trace)traceEvent(">Facets"+retargeted);
	}
	/**
  Create targets to be returned as the <code>elements</code> of the surface root. 
  @return a non-<code>null</code> (though possibly empty) {@link STarget}[] 
   */
  protected STarget[]lazyAppAreaElements() {
		return new STarget[]{};
	}
	/**
  Defines an application and builds its surface. 
    <p>Calls the final implementation of {@link SSurface#buildRetargeted()}
   */
  public void openApp(){
  	traceEvent(">Opening surface "+info(this));
  	buildRetargeted();
  	traceEvent(">Opened surface "+info(this)+"\n");
  }
  /**
  Builds an appropriate surface for the type passed to the 
  constructor. 
  <p>Opening content is created from sources returned by {@link #getOpeningContentSources()}.
  <p>Surfaces have the following features:
  <ul>
    <li><code>SINGLE</code> - content root facet from 
      the single {@link SContenter} added directly to the 
      host GUI; menu, tool and status facet from the content 
      can be merged with roots defined by the {@link AppSurface}. 
    <li><code>TABBED</code> - one or more tabs containing 
      content root facets obtained from multiple {@link SContenter}s, 
      managed by the {@link AppSurface} which sets new 
      merged menu, tool and status roots as required for the 
      active tab. 
    <li><code>DESKTOP</code> - content root facet 
      in internal windows; <code>DESKTOP</code> as a singleton source
		signals that an empty desktop be created with suitable menus etc. 
  </ul>
   */
  final public void buildRetargeted(){
  	Object[]sources=getOpeningContentSources();
  	if(sources==null||sources.length==0)throw new IllegalStateException(
  			"Null or empty sources in "+info(this));
  	else if(contentStyle==DESKTOP&&sources[0]==DESKTOP)
  		contents.add(emptyContent);
		else for(int i=0;i<sources.length;i++)
			contents.add((AppContenter)newContenter(sources[i]));
  	for(AppContenter each:contents){
  		areas.newUpdate().addFreshArea(each);
  		each.wasAdded();
  	}
  	areas.updateLayout(contents);
  }
	/**
	Return sources for content to be exposed when the surface is first created.
	<p>The objects returned must be suitable for passing to {@link #newContenter(Object)}.  
	@return suitable source[es] for opening content. 
	 */
	protected abstract Object[]getOpeningContentSources();
	/**
  Return a {@link SContenter} wrapping content created from 
  <code>source</code>. 
  <p>The content must be an implementor of {@link AppContenter} such as 
  {@link ViewerContenter}; it will appear in the surface as follows: 
  	<ul>
  		<li><code>SINGLE</code> - replacing content in any existing surface</li>
  		<li><code>TABBED</code>, <code>DESKTOP</code> - in new window/tab</li>
  	</ul>
   @param source for the content
   */
  abstract protected SContenter newContenter(Object source);
  private static final class EmptyContenter implements AppContenter{
		private final AppSurface app;
		EmptyContenter(AppSurface app){
			this.app=app;
		}
		@Override
		public final SAreaTarget newContentArea(boolean faceted){
			return new ContentArea(title(),this,SAreaTarget.EMPTY_AREA_CHILDREN){};
		}
		@Override
		public STarget[]lazyContentAreaElements(SAreaTarget area){
			return new STarget[]{};
		}
		@Override
		public TypesKey featuresKey(SContentAreaTargeter use){
			return TypesKey.EMPTY;
		}
		@Override
		public LayoutFeatures newContentFeatures(SContentAreaTargeter area){
			return app.newEmptyDesktopFeatures(area);
		}
		@Override
		public SFrameTarget contentFrame(){
			return new SFrameTarget("No content frame");
		}
		@Override
		public boolean hasChanged(){
			return false;
		}
		@Override
		public Class targetType(){
			return getClass();
		}
		@Override
		public boolean useActiveFeatures(SContentAreaTargeter active){
			return false;
		}
		@Override
		final public String title(){
			return TITLE_EMPTY;
		}
		@Override
		public String toString(){
			return info(this);
		}
		@Override
		public void areaRetargeted(SContentAreaTargeter area){}
		@Override
		public void wasAdded(){}
		@Override
		public void wasRemoved(){}
		@Override
		public void alignContentAreas(SAreaTarget existing,SAreaTarget added){
			throw new RuntimeException("Not implemented in "+info(this));
		}
	}
	/**
  Provide a {@link FacetLayout} for empty desktop content.
  <p>Implements {@link AppContenter#newContentFeatures(SContentAreaTargeter)}
  on behalf of {@link #emptyContent}; default is invalid stub 
   */
  protected LayoutFeatures newEmptyDesktopFeatures(SContentAreaTargeter root){
		throw new RuntimeException("Not implemented in "+info(this));
	}
	/**
	May return a facet to be attached to the surface root. 
	<p>Defines a tab folder or internal desktop for a multi-content
	surface - not called for single content as the content root facet is used instead.
	@param appArea was (re)created in {@link SSurface#buildRetargeted()}
	 */
	protected MountFacet newMultiContentFacet(SAreaTarget appArea){
		throw new RuntimeException("Not implemented in "+info(this));
	}
	/**
	Opens or activates a window/tab on <code>source</code>. 
	<p>Where type is <code>TABBED</code> or <code>DESKTOP</code> 
		and <code>source</code> is different from that of any existing content, 
		creates a new {@link SContenter} and from that a new content root; 
		where <code>source</code> is already in use, 
		activates an existing content root exposing the {@link AppContenter} 
		for that source. 
	<p>If client code throws a {@link ContentCreationException} the method returns without
	effect after passing the exception to {@link #contentNotAdded(ContentCreationException)}.  
	 @param source for added content
	 */
	final public void addContent(Object source){
	  AppContenter content=(AppContenter)newContenter(source);
	  try{
			areas.newUpdate().addFreshArea(content);
		}catch(ContentCreationException e){
			contentNotAdded(e);
			return;
		}
		contents.add(content);
		contents.remove(emptyContent);
		areas.updateLayout(contents);
		content.wasAdded();
	}
	/**
	Enables a client to abandon creation of content. 
	 */
	public static final class ContentCreationException extends RuntimeException{
		public ContentCreationException(String message){
			super(message);
		}
	}
  protected void contentNotAdded(ContentCreationException e){
		throw new RuntimeException(e);
	}
	/**
	Replaces content in single-content surface. 
	<p>Where type is {@link ContentStyle#SINGLE}, replaces content in existing surface 
	with new {@link AppContenter} created from <code>source</code>.
  @param source for replacement content
	 */
	final public void replaceSingleContent(Object source){
		if(contentStyle!=SINGLE)throw new IllegalStateException(
				"Bad contentStyle in "+info(this));
		AppContenter then=findActiveContent(),now=(AppContenter)newContenter(source);
	  contents.clear();
	  contents.add(now);
	  areas.newUpdate().replaceActiveArea(now);
	  areas.updateLayout(contents);
  	now.wasAdded();
  	then.wasRemoved();
	}
	/**
	Restores content of active root. 
	<p>?Any other roots for the same content are removed.  
	 */
	final public void revertActiveContent(){
	  AppContenter then=findActiveContent(),
	  	now=(AppContenter)newContenter(((ViewerContenter)then).sink());
	  contents.remove(then);
	  contents.add(now);
	  areas.newUpdate().replaceActiveArea(now);
	  areas.updateLayout(contents);
	  now.wasAdded();
	  then.wasRemoved();
	}
	/**
	Attempts to close all tabs/windows exposing content of the active viewer. 
	<p>Providing {@link #contentIsRemovable(AppContenter)} returns <code>true</code> for 
		the content of the active viewer, closes all tabs/windows exposing that content.
	*/
	final public boolean removeActiveContent(){
	  SFrameTarget viewable=areas.activeContentFrame();
	  AppContenter remove=null;
	  for(AppContenter content:contents)
			if(content instanceof ViewerContenter
					&&((ViewerContenter)content).contentFrame()==viewable){
				if(!contentIsRemovable(content))return false;
				else{
					areas.newUpdate().removeContentAreas(content);
					remove=content;
				}
			}
	  if(remove==null)throw new IllegalStateException("Null remove in "+this);
	  contents.remove(remove);
	  ((ViewerContenter)remove).wasRemoved();
		if(contents.isEmpty())contents.add(emptyContent);
		areas.updateLayout(contents);
		return true;
	}
	/**
	Attempts to close all tabs/windows. 
	<p>Removes content roots for all content for which 
	{@link #contentIsRemovable(AppContenter)} returns <code>true</code>; 
	@param appClosing 
	 */
	final public void removeAllContent(boolean appClosing){
		boolean desktop=contentStyle==DESKTOP;
	  for(AppContenter content:contents){
			if(content==emptyContent)continue;
	  	if(appClosing||contentIsRemovable(content)){
				if(desktop)areas.newUpdate().removeContentAreas(content);
				content.wasRemoved();
			}
			else if(!appClosing)return;
	  }
	  if(!desktop)return;
		contents.clear();
		if(contents.isEmpty())contents.add(emptyContent);
		areas.updateLayout(contents);
	}
	/**
	Remove content after closing all exposing areas. 
	 */
	final public void removeContent(AppContenter content){
		areas.newUpdate().removeContentAreas(content);
		content.wasRemoved();
		contents.remove(content);
		if(contents.isEmpty())contents.add(emptyContent);
		areas.updateLayout(contents);
	}
	/**
	Attempts to close tab/window containing active viewer. 
	<p>Where there is no other root for the content, calls
		{@link #removeActiveContent()} and returns the result.
	 */
	final public boolean removeActiveArea(){
		SAreaTarget appArea=areas.appArea();
		ContentArea activeArea=(ContentArea)appArea.indexedTarget();
	  SFrameTarget frame=activeArea.contenter.contentFrame();
	  boolean isLastArea=true;
	  for(ContentArea area:viewerAreas(appArea.indexableTargets()))
	  	if(area!=activeArea)isLastArea&=area.contenter.contentFrame()!=frame;
		if(isLastArea)return removeActiveContent();
	  else{
	  	areas.newUpdate().removeArea(activeArea);
	  	areas.updateLayout(contents);
		}
		return true;
	}
	/**
	Check if  content passed may be removed. 
	<p>Default is invalid stub. 
	@param content to be checked
	 */
	public boolean contentIsRemovable(AppContenter content){
		throw new RuntimeException("Not implemented in "+info(this));
	}
	/**
	Returns the content that created the active {@link SAreaTarget} tree. 
	@return the current content or {@link #emptyContent}
	 */
	final public AppContenter findActiveContent(){
		boolean debug=false;
	  SFrameTarget active=areas.activeContentFrame();
		if(debug)traceDebug(".findActiveContent: ",active);
		for(AppContenter content:contents){
			SFrameTarget frame=content.contentFrame();
			if(debug)traceDebug(".findActiveContent: ",frame);
			if(frame==active)return content;
		}
		return emptyContent;
	}
	/**
	Returns the first {@link SAreaTarget} tree exposing the source passed. 
	@param source of content to be found (typically a file) 
	 */
	final public SAreaTarget firstContentArea(Object source){
		AppContenter found=null;
		for(AppContenter content:contents)
			if(content instanceof ViewerContenter && 
	  			((ViewerContenter)content).sink().equals(source))
	  		found=content;
		if(found==null)return null;
		for(ContentArea area:viewerAreas(areas.appArea().indexableTargets()))
			if(area.contenter.contentFrame()==found.contentFrame())return area;
		return null;
	}
	/**
	Opens a new tab/window on existing content. 
	<p>Where type is <code>TABBED</code> or <code>DESKTOP</code>, 
		adds a new content root exposing the content of the active content root.
	 */
	final public void cloneActiveArea(){
	  areas.newUpdate().addAlignedArea(findActiveContent());
	  areas.updateLayout(contents);
	}
	final void moveActiveArea(int to){
	  areas.newUpdate().moveActiveArea(to);
	  areas.updateLayout(contents);
	}
	/**
  Called whenever the surface targeters have been retargeted. 
   */
  protected void appRetargeted(){}
  /**
	Returns the targeter of the currently active content root. 
	 */
	final public SContentAreaTargeter activeContentTargeter(){
  	return(SContentAreaTargeter)surfaceTargeter().areaAt(AreaTargeter.AREA_ACTIVE);
  }
	public final AreaTargeter surfaceTargeter(){
		if(areas.targeter==null)throw new IllegalStateException(
				"No targeter in "+info(this));
		else return areas.targeter;
	}
	/**
	Implements interface method. 
	@return {@link AppValues#appName} from {@link #spec}. 
	 */
	final public String title(){
		return spec.appName;
	}
	/**
	Returns all {@link ViewerContenter}s currently active in the application. 
	 */
	public ViewerContenter[]findViewerContents(){
		List<ViewerContenter>list=new ArrayList();
		for(AppContenter each:contents)
			if(each instanceof ViewerContenter)list.add((ViewerContenter)each);
			else if(false)throw new RuntimeException("Surprising in "+info(this));
		return list.toArray(new ViewerContenter[]{});
	}
	/**
	Provides for an application to run headless. 
	@return <code>false</code> by default
	 */
	protected boolean isHeadless(){
		return false;
	}
	/**
	Provides the host for a headless application. 
	@return by default a {@link HeadlessHost}
	 */
	protected FeatureHost newHeadlessHost(){
		return new HeadlessHost(this);
	}
	/**
	Implements interface method. 
	@return <code>true</code> once {@link #surfaceTargeter()} will not
	throw an {@link IllegalStateException}
	 */
	final public boolean isBuilt(){
		return areas.targeter!=null;
	}
	@Override
	final public boolean isSlave(){
		return spec.forSlave();
	}
	/**
	Allows runtime exceptions in framework and client code to be handled gracefully, 
	by {@link AppWatcher}.
	<p>Default simply runs <code>op</code> 
	 */
	public Object runWatched(WatchableOperation op){
		return op.doOperations();
	}
	final public TypesKey activeFeatures(){
		if(areas.featuresKey==null)return TypesKey.EMPTY;
		else return areas.featuresKey;
	}
}