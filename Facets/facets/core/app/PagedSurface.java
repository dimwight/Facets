package facets.core.app;
import static facets.core.app.AppConstants.*;
import static facets.core.app.Dialogs.*;
import static facets.util.Debug.*;
import static facets.util.app.Events.*;
import facets.core.app.Dialogs.Response;
import facets.core.superficial.Notice;
import facets.core.superficial.Notifiable;
import facets.core.superficial.Notifying;
import facets.core.superficial.STarget;
import facets.core.superficial.STargeter;
import facets.core.superficial.TargetCore;
import facets.core.superficial.Notifying.Impact;
import facets.core.superficial.app.AreaTargeter;
import facets.core.superficial.app.IndexingTarget;
import facets.core.superficial.app.SAreaTarget;
import facets.core.superficial.app.SContentAreaTargeter;
import facets.core.superficial.app.SContenter;
import facets.core.superficial.app.SSurface;
import facets.core.superficial.app.SSurface.DialogSurface;
import facets.facet.kit.DialogHost;
import facets.util.Debug;
import facets.util.Titled;
import facets.util.Util;
import facets.util.app.AppFileValues;
import facets.util.app.AppValues;
import facets.util.tree.TypedNode;
import facets.util.tree.ValueNode;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
/**
Implements {@link SSurface} for fixed content laid out as one more pages,
and {@link facets.core.superficial.app.SSurface.DialogSurface} to communicate with {@link DialogHost}. 
<p>{@link PagedSurface} can build different paging styles 
  depending on the {@link PagedContenter}s passed to its constructor 
  and the methods overriden in the instance subclass.
<ul>
  <li><b>simple </b> - pass a single {@link PagedContenter}, 
    from which a content root is created and added as the single 
    page. 
  <li><b>tabbed </b> - pass multiple {@link PagedContenter}s, 
    from which the content roots are added as children of a single 
    page. 
  <li><b>multi-page</b> - create tree members whose {@link SAreaTarget}
    values are added as pages. 
</ul>
 */
public abstract class PagedSurface extends PagedSurfaceCore implements DialogSurface{
	protected enum PagingStyle{Tree,Wizard};
	private static final int MARGIN=false?0:40,
		TRIM_BUTTONS=35,//50
		TRIM_TAB_H=25,//30
		TRIM_TAB_W=6,
		TRIM_TREE=100,
		TRIM_WIZARD=50;//60
	private final String tailKey=Util.shortTypeNameKey(this),boundsKey=KEYTOP_BOUNDS+tailKey,
		sizeDefaultKey=KEYTOP_NATURE_SIZE+tailKey;
	public final Dimension trim=new Dimension(0,TRIM_BUTTONS);
	protected final AppSpecifier spec;
	private final PagedActions actions;
	private int widthMin,heightMin;
	private Rectangle boundsDefault,boundsState;
	private PagedContenter[]contents;
	/**
	Unique constructor. 
	 @param title passed to the host dialog window
	 @param host 
	 @param spec 
	 @param actions the {@link PagedActions} defining a set of control actions for the dialog
	 @param contents the {@link PagedContenter}s defining the content to be exposed in the dialog 
	 */
	public PagedSurface(String title,HideableHost host,AppSpecifier spec,PagedActions actions,
			PagedContenter[]contents){
		super(title,host);
		this.spec=spec;
		this.contents=contents;
		if((this.actions=actions)==null)throw new IllegalArgumentException(
				"Null actions in "+info(this));
		else actions.attachSurface(this);
	}
	/**
	Implements interface method. 
	<p>Default size may be retrieved from {@link AppValues#nature(String...)} using
	keys headed with {@link Dialogs#KEYTOP_NATURE_SIZE}; and 
	session bounds from {@link AppValues#state(String...)} using
	{@link Dialogs#KEYTOP_BOUNDS}.  
	 */
	@Override
	final public Rectangle getLaunchBounds(){
		SAreaTarget areaRoot=(SAreaTarget)surfaceTargeter().target();
		if(boundsDefault==null){
			for(PagedContenter content:contents){
				if(widthMin>0)break;
				Dimension size=content.contentAreaSize();
				if(false)trace(".getLaunchBounds: content="+content.title()+" size="+size.height);
				widthMin=Math.max(widthMin,size.width);
				heightMin=Math.max(heightMin,size.height);
			}
			PagingStyle paging=pagingStyle();
			boolean singleContent=contents.length==1,
				multiPage=paging!=null,wizard=multiPage&&paging==PagingStyle.Wizard;
			trim.width=singleContent?0:!multiPage?TRIM_TAB_W:wizard?0:TRIM_TREE;
			trim.height+=singleContent?0:!multiPage?TRIM_TAB_H:wizard?TRIM_WIZARD:0;
			STarget[]rootChildren=areaRoot.indexableTargets(),
				panelRoots=!multiPage?rootChildren:
					((IndexingTarget)rootChildren[MULTI_AREA_PAGES]).indexableTargets();
			for(STarget panelRoot:panelRoots)
				if(!(panelRoot instanceof SAreaTarget)){
					if(true)throw new RuntimeException("Not implemented in "+info(this));
					trim.height+=TRIM_TAB_H;
					break;
				}
			widthMin+=trim.width-MARGIN;
			heightMin+=trim.height-MARGIN;
			int[]natureSize=dialogValues(true).getInts(sizeDefaultKey);
			Dimension sizeDefault=natureSize.length<2?
					new Dimension(widthMin+MARGIN,heightMin+MARGIN)
				:new Dimension(natureSize[0],natureSize[1]);
			boundsDefault=new Rectangle(new Point(AT_NOT_SET,0),sizeDefault);
			trace(".getLaunchBounds: natureSize="+natureSize.length+
					" boundsDefault="+boundsDefault.height);
		}
		if(boundsState==null){
			int[]ints=dialogValues(false).getInts(boundsKey);
			if(ints.length>3)boundsState=new Rectangle(ints[0],ints[1],ints[2],ints[3]);
			trace(".getLaunchBounds: boundsKey="+boundsKey+
					" boundsState="+(boundsState==null?null:boundsState.height));
		}
		Rectangle launch=boundsState=boundsState==null?boundsDefault
			:new Rectangle(boundsState.getLocation(),
					new Dimension(boundsState.width<widthMin?
							boundsDefault.width:boundsState.width,
						boundsState.height<heightMin?
							boundsDefault.height:boundsState.height));
		trace(".getLaunchBounds: launch=",launch.height);
		return launch;
	}
	/**
	Implements interface method. 
	 */
	@Override
	public final void dialogDismissed(Rectangle bounds){
		for(PagedContenter content:contents())content.hostHidden();
		trace(".dialogDismissed: bounds="+bounds.getSize().height);
		if(bounds.x!=AT_NOT_SET)boundsState=new Rectangle(bounds);
		if(!persistBoundsState())return;
		trace(".dialogDismissed: boundsState="+boundsState.height);
		int[]ints={bounds.x,bounds.y,bounds.width,bounds.height};
		dialogValues(false).put(boundsKey,ints);
	}
	/**
	Implements interface method. 
	<p> All surface variants are built by the same code, with execution path 
	determined primarily by whether it builds a single
	or multiple page layout. </p>
	<ol>
		<li>Content roots are constructed and their facet trees built for each of 
		the {@link PagedContenter}s passed to the constructor, 
		and passed to {@link #newContentPages(PagedContenter[], SAreaTarget[])}.</li>
		<li> Facets are attached using {@link #attachPageFacet(SAreaTarget)} to 
		any pages returned that are not content roots 
		(and thus already have facet attached). </li>
		  <li>A single page is contained directly by the root area 
	      passed to {@link #attachContentAreas(SAreaTarget)}.</li>
		  <li>Multiple pages are passed to {@link #newPageTreeNodes(SAreaTarget[])} 
	      which defines nodes for a page selection tree, and these 
	      nodes passed to {@link #newPagesTreeView(TypedNode[])}; 
	      from this a viewer area is created which is one of two 
	      children of the root area, the other being an area containing 
	      the pages.</li>
	</ol>
	 */
	@Override
	final public void buildRetargeted(){
		rebuild(contents);
	}
	/**
	Replaces all content in the surface. 
	<p>Existing content roots are replaced by roots created by 
		each member of <code>contents</code>, whose types must match 
		exactly those of the existing contents. 
		@param contents replace existing contents 
	 */
	final public void replaceContents(PagedContenter[]contents){
		if(this.contents==null||contents==null)
			throw new IllegalArgumentException("Null contents in "+info(this));
		else if(!contentsMatch(this.contents,contents))
			throw new IllegalArgumentException("Contents do not match existing in "+info(this));
		else if(false)for(PagedContenter c:this.contents)
			traceDebug("DialogSurface.replaceContents: ",c.contentFrame());
		else rebuild(this.contents=contents);
	}
	final void rebuild(PagedContenter[]contents){
		for(PagedContenter contenter:contents)contenter.setSurface(this);
		super.rebuild(contents);
	}
	/**
	Implements interface method. 
	 */
	@Override
	public void notify(Notice notice){
		if(trace)traceEvent(">Surface "+info(this)+" notified with "+notice);
		Impact impact=notice.impact;
		Notifying latest=notice.sources.get(notice.sources.size()-1);
		if(true&&((Titled)latest).title().equals(TYPE_PAGES_TREE))return;
		AreaTargeter targeter=surfaceTargeter();
		targeter.retarget(targeter.target(),impact);
	  if(trace)traceEvent((">Targeters retargeted in "+info(this)));
	  AreaTargeter moveUp=targeter.areaAt(AreaTargeter.AREA_LOWEST);
	  if(moveUp.title().equals(TYPE_PAGES_TREE))
	  	moveUp=(AreaTargeter)((SAreaTarget)((TypedNode)
	  			((NodeViewable)((SAreaTarget)moveUp.target()).contenterFrame()
	  					).selection().single()).values()[0]).notifiable();
	  while(!(moveUp instanceof SContentAreaTargeter))
			moveUp=(AreaTargeter)moveUp.notifiable();
	  SContentAreaTargeter contentTargeter=(SContentAreaTargeter)moveUp;
		activeContentType=contentTargeter.targetType();
		if(false)trace(".notify: ",activeContentType);
	  SAreaTarget activeRoot=(SAreaTarget)moveUp.target();
		activeRoot.setLive(true);
		for(PagedContenter c:contents){
			if(c.getClass()==activeContentType){
				if(false)trace(".notify: "+"c="+c.title());
				c.areaRetargeted(contentTargeter);
				if(false)host().setTitle(c.title());
			}
		}
	  targeter.retargetFacets(impact);
	  if(trace)traceEvent(">Facets retargeted in "+info(this));
	}
	/**
	Implements abstract/interface method. 
	 */
	@Override
	final STarget lazyTriggerGroup(){
		STarget[]triggers=actions.newTriggers();
		if(triggers==null)throw new IllegalStateException("Null triggers in "+info(this));
		return new TargetCore("Actions",triggers);
	}
	/**
	Enables top-level buttons to hide the dialog or other host. 
	 */
	public final void hideHost(Response response){
		((HideableHost)host()).hide(response);
	}
	/**
	Do the types of the two arrays match? 
	<p>Called by {@link #replaceContents(PagedContenter[])}; 
	implemented with {@link #contentsKey(PagedContenter[])}. 
	@param existing set during initial build or last launch
	@param proposed for this launch
	 */
	final protected boolean contentsMatch(PagedContenter[]existing,
			PagedContenter[]proposed){
		return contentsKey(existing).equals(contentsKey(proposed));
	}
	/**
	The contenters for the surface. 
	@return contenters passed during construction or to <code>replaceContents</code>
	 */
	public final PagedContenter[]contents(){
		if(contents==null)throw new IllegalStateException("No contents in "+info(this));
		return contents;
	}
	/**
	Creates a key representing the exact types of the {@link SContenter}s
	passed. 
	<p>Used to implement {@link #replaceContents(PagedContenter[])}; 
	available for use by
	any class requiring a definition of the composite type of a {@link SContenter}[]
	@param contents define the key returned by their types
	 */
	public final static Object contentsKey(PagedContenter[]contents){
		StringBuffer b=new StringBuffer();
		for(int i=0;i<contents.length;i++)b.append(contents[i].getClass().getName());
		return b.toString();
	}
	@Override
	public boolean isResizable(){
		return persistBoundsState()||AppValues.stateDebug.getBoolean(Dialogs.DEBUG_RESIZABLE);
	}
	/**
	Required to ensure correct calculation of default dialog size. 
	@return <code>null</code> by default to signal single page
	 */
	protected PagingStyle pagingStyle(){
		return null;
	}
	private ValueNode dialogValues(boolean nature){
		return nature?spec.nature():spec.state(AppFileValues.STATE_TYPE_DIALOGS);
	}
	/**
	Should host bounds be persisted? 
	@return <code>true</code> by default
	 */
	protected final boolean persistBoundsState(){
		return true;
	}
	public static STarget findDialogTrigger(SContentAreaTargeter content,String title){
		for(STarget t:findRootArea(content).elements()[0].elements())
			if(t.title().equals(title))return t;
		return STarget.NONE;
	}
	/**
	Convenience method for accessing the surface area root. 
	@param content within the area targeter tree
	 */
	public static final SAreaTarget findRootArea(SContentAreaTargeter content){
		STargeter maybeRoot=(STargeter)content.notifiable();
		Notifiable check=maybeRoot;
		while((check=maybeRoot.notifiable())instanceof STargeter)
			maybeRoot=(STargeter)check;
		return(SAreaTarget)maybeRoot.target();
	}
}
