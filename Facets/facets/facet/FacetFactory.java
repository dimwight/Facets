package facets.facet;
import static facets.core.app.AppConstants.*;
import static facets.core.app.TextView.*;
import static facets.core.superficial.app.ViewerTarget.*;
import static facets.facet.FacetFactory.*;
import static facets.util.StringFlags.*;
import static facets.util.app.AppValues.*;
import facets.core.app.AppActions;
import facets.core.app.AppConstants;
import facets.core.app.AppSpecifier;
import facets.core.app.FacetHostable;
import facets.core.app.FeatureHost;
import facets.core.app.HideableHost;
import facets.core.app.MenuFacets;
import facets.core.app.PagedActions;
import facets.core.app.PagedContenter;
import facets.core.app.PagedSurface;
import facets.core.app.SurfaceServices;
import facets.core.app.TypeKeyable;
import facets.core.app.AppSurface.ContentStyle;
import facets.core.app.Dialogs.ExceptionTexts;
import facets.core.app.Dialogs.Response;
import facets.core.app.FeatureHost.LayoutFeatures;
import facets.core.app.StatefulViewable.ClipperSource;
import facets.core.superficial.Notice;
import facets.core.superficial.Notifying;
import facets.core.superficial.SFacet;
import facets.core.superficial.SIndexing;
import facets.core.superficial.STarget;
import facets.core.superficial.STargeter;
import facets.core.superficial.STextual;
import facets.core.superficial.SToggling;
import facets.core.superficial.STrigger;
import facets.core.superficial.TargetCore;
import facets.core.superficial.TargeterCore;
import facets.core.superficial.Notifying.Impact;
import facets.core.superficial.SIndexing.Coupler;
import facets.core.superficial.app.AreaTargeter;
import facets.core.superficial.app.IndexingTargeter;
import facets.core.superficial.app.SAreaTarget;
import facets.core.superficial.app.SContentAreaTargeter;
import facets.core.superficial.app.SurfaceStyle;
import facets.core.superficial.app.ViewerTarget;
import facets.facet.AreaFacets.PaneDialogStyle;
import facets.facet.HostingFacetSwing.Paged;
import facets.facet.Indexings.Iterator;
import facets.facet.app.FacetAppActions;
import facets.facet.app.FacetAppSpecifier;
import facets.facet.app.FacetAppSurface;
import facets.facet.kit.DialogHost;
import facets.facet.kit.KWrap;
import facets.facet.kit.KitCore;
import facets.facet.kit.KitFacet;
import facets.facet.kit.Toolkit;
import facets.facet.kit.swing.KitSwing;
import facets.util.Debug;
import facets.util.ItemList;
import facets.util.StringFlags;
import facets.util.Util;
import facets.util.app.AppServices;
import facets.util.app.AppValues;
import facets.util.app.Events;
import facets.util.app.ProvidingCache;
import facets.util.app.WatcherCoupler;
import facets.util.shade.Shade;
import facets.util.shade.Shades;
import facets.util.tree.Nodes;
import facets.util.tree.ValueNode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
/**
Superficial surface builder that creates and lays out {@link SFacet}s 
exposing application content and logic. 
<p>{@link FacetFactory} provides a convenient API for creating {@link SFacet}s, 
  attaching them to members of a {@link STargeter} tree and assembling them into a 
  complete application layout; it facilitates
  semi-declarative definition of menus, toolbars etc by implementing {@link LayoutFeatures}. 
<p>Most methods of {@link FacetFactory} comply with the following general contract: 
<ul>
  <li>The method name denotes the type of facet that will be returned and 
  often includes the type of target that it exposes.
  <li>The facet returned will be attached to the targeter passed, which
  must have a target of the right type.
  <li><a href="#Hints">Hints</a> passed will be honoured on a 
    best efforts basis.
</ul>
<p>A core {@link FacetFactory} must be created using one of  
	class methods {@link #newDesktopCore(Toolkit, AppSpecifier)}  
	or {@link #newAppletCore(Toolkit, SurfaceStyle)}.
	It	may then be passed to the constructor of concrete subclasses 
which will thus use the same toolkit and other resources. 
<h3><a name="Hints"></a>Hints</h3>
<p>Hints use the {@link StringFlags} API to add presentation suggestions to 
facet creation methods in a way that is compact, flexible and intuitive; 
they can be any concatenation of <code>HINT_XXX</code> class constants.
 */
public class FacetFactory extends FacetsCore implements LayoutFeatures,
		AppServices{
  final public static int GRAPH_NONE=0,GRAPH_APP=1,GRAPH_DIALOGS=2,
		MENU_APP=0,MENU_EDIT=1,MENU_PANE=2,MENU_WINDOW=3,MENU_HELP=4;
	public static boolean graphBuild,graphFindNow;
	public static final String GRAPH_FIND_NONE="[Enter text to match in title]";
	public static String findGraphValue=GRAPH_FIND_NONE;
	public static int graphShowWhere=GRAPH_NONE,graphFindAt=0;
 /**Shade to be used for surface panels and menus.
 	<p>May be <code>null</code> to signify use of toolkit defaults. */
	public static Shade panelShade;
	/** App surface style. */
	public static SurfaceStyle surfaceStyle;
	public static int facets;
	/** Time delay for drag input notifications, in milliseconds. */
	public static int dragNotifyPause=50;
	/**Should drag notification only take place when drag is paused? */
	public static boolean dragNotifyInterim=false;
	/** Key for debug value. */
	final public static String 
		ARG_GRAPH_BUILD="graphPaneBuild",
		KEY_GRAPH_WHERE="graphPaneWhere",
		KEY_GRAPH_FIND="graphPaneFind",
		KEY_GRAPH_SPLIT="graphPaneSplit_",
		KEY_GRAPH_OFFSETS="graphPaneOffsets_",
		KEY_DRAG_NOTIFY="dragNotifyInterim",
		KEY_DRAG_PAUSE="dragNotifyPause";
	/**
	Alternative name for null facet that can be used as divider for facet layouts. 
	<p>Interpreted as follows:
	<ul>
	 <li>in <code>rowPanel</code> methods, as a row break</li> 
	 <li>in (menu)<code>items</code> methods, as a menu divider</li> 
	</ul> 
	 */
	final public static KitFacet BREAK=new KitFacet(){
    public KWrap base(){return KWrap.BREAK;}
    public KWrap[]items(){return new KWrap[]{base()};}
    public void targetNotify(Object notice, boolean interim){}
    public void retarget(STarget target,Notifying.Impact impact){}
    public STarget target(){return STarget.NONE;}
    public String title(){return "BREAK/NO_FACET";}
    public void dispose(){}
    public String toString(){return title();}
  },
  NO_FACET=BREAK;
  public final static Integer[]fontSizes=new Integer[5];
  private static final int fontSizeMidAt=fontSizes.length/2;
	public static int fontSizeAt=fontSizeMidAt;
	public static final SIndexing fontIndexing=new SIndexing("Font Size",fontSizes,
			fontSizeAt,new SIndexing.Coupler(){
		@Override
		public void indexSet(SIndexing i){
			AppValues.stateDebug.put(FONT_OFFSET,(fontSizeAt=i.index())-fontSizeMidAt);
		}
		public String[]iterationTitles(SIndexing i){
			return new String[]{FONT_SMALLER,FONT_LARGER};
		}
	});
	/**Flag constant to be passed to facet factory methods; may be concatenated
	 with others. */
	final public static String TREE_EXPAND="E&xpand",TREE_COLLAPSE="Coll&apse",
	HINT_NONE=FLAG_NONE,
	HINT_EXTRAS_PANE=newFlag("In graph pane"),  
	HINT_NO_FLASH=newFlag("No flash notifications"),  
	HINT_BARE=newFlag("Bare"),  
	HINT_TALL=newFlag("Tall"), 
	HINT_TEXT_FONT=newFlag("Text font"), 
	HINT_LABEL_AUTOCLEAR=newFlag("Auto-paint label on change"), 
	HINT_TITLE1=newFlag("Title 2"),
	HINT_TITLE2=newFlag("Title 3"),
	HINT_SQUARE=newFlag("Square"),
	HINT_SPREAD=newFlag("Spread"),
	HINT_HEADED=newFlag("Slack"),
	HINT_GRID=newFlag("Grid"),
	HINT_DEBUG=newFlag("Debug"),
	HINT_USAGE_PANEL=newFlag("Panel usage"),
	HINT_USAGE_FORM=newFlag("Form usage"),
	HINT_USAGE_ICON=newFlag("Icon usage"),
	HINT_NO_FOCUS=newFlag("No focus"),
	HINT_PANEL_CENTER=newFlag("Center-align panel"),
	HINT_PANEL_MIDDLE=newFlag("Middle-align panel"),
	HINT_PANEL_RIGHT=newFlag("Right-align panel or place to right of panel"),
	HINT_PANEL_BELOW=newFlag("Place below panel"),
	HINT_PANEL_ABOVE=newFlag("Place above panel"),
	HINT_PANEL_INSET=newFlag("Inset panel"),
	HINT_PANEL_BORDER=newFlag("Panel with border or header"),
	HINT_NUMERIC_NUDGERS_FIRST=newFlag("Numeric nudgers first"),
	HINT_NUMERIC_UNGROUPED=newFlag("Numeric ungrouped"),
	HINT_NUMERIC_FIELDS=newFlag("Slider fields"),
	HINT_SLIDER_TICKS=newFlag("Slider ticks"),
	HINT_SLIDER_LABELS=newFlag("Slider labels"),
	HINT_SLIDER_LOCAL=newFlag("Slider local"), 
	HINT_SLIDER_FIELDS_TICKS_LABELS=
	 	HINT_NUMERIC_FIELDS+HINT_SLIDER_TICKS+HINT_SLIDER_LABELS,
	HINT_NO_MNEMONICS=newFlag("Menu with no mnemonics"),
	HINT_MENU_PRELOAD=newFlag("Preload menu to get key bindings"),
	HINT_INDEXING_SELECT=newFlag("Indexing with no selection"),
	HINT_TOOLTIPS=newFlag("Tooltips");
		final AppSpecifier spec;
	private final HelpPages helpPages;
	private AreaFacets areas;
	private ProvidingCache providingCache;
	/**
	 Constructor for use by (possibly trivial) subclasses to be passed as {@link LayoutFeatures}. 
	 @param src the instance whose members will be shared
	 */
	public FacetFactory(FacetFactory src){
		this(src.kit,surfaceStyle,src.spec,false);
		providingCache=src.providingCache;
	}
	private FacetFactory(Toolkit kit,SurfaceStyle surfaceStyle,AppSpecifier values,
			boolean isCore){
		super(kit);
		FacetFactory.surfaceStyle=surfaceStyle;
		FacetFactory.panelShade=surfaceStyle!=SurfaceStyle.BROWSER?null:Shades.white;
		this.spec=values;
		FacetAppSpecifier spec=values instanceof FacetAppSpecifier?((FacetAppSpecifier)values)
				:null;
		helpPages=spec==null?null:new HelpPages(spec);
		if(!isCore)return;
		kit.readDecorationValues(values);
		if(values==null){
			Util.printOut("FacetFactory.FacetFactory: No app values");
			return;
		}
		else Events.traceEvent(">Read surface builder values");
		int offset=!values.hasSystemAccess()?0:AppValues.stateDebug.getOrPutInt(FONT_OFFSET,0);
		fontSizeAt=fontSizes.length/2+offset;
		Events.traceEvent(">Got surface builder with toolkit "+Debug.info(kit));
	}
	protected boolean implementsRibbon(){
		return((FacetAppSpecifier)spec).headerIsRibbon();
	}
	/**
	Creates a basic {@link FacetFactory} for use in a desktop application. 
	<p>Widgets will be constructed using the toolkit supplied; 
	applications can use the builder returned as the core of a custom builder.    
	 @param kit widget toolkit with look-and-feel set
	 @param spec defines app features 
	 */
	public static FacetFactory newDesktopCore(Toolkit kit,AppSpecifier spec){
		Events.traceEvent(">Creating core surface builder");
		FacetFactory ff=new FacetFactory(kit,SurfaceStyle.DESKTOP,spec,true);
		ff.coupleAppWatcher(new WatcherCoupler());
		return ff;
	}
	/**
	Creates a basic {@link FacetFactory} for use in an applet.  
	<p>Widgets will be constructed using the toolkit supplied; 
	applications can use the builder returned as the core of a custom builder.    
	 @param kit widget toolkit with look-and-feel set
	@param style defines L&F, widget background colour
	 */
	public static FacetFactory newAppletCore(Toolkit kit,SurfaceStyle style){
		Events.traceEvent(">Creating core surface builder");
		return new FacetFactory(kit,style,null,true);
	}
	final public void iterateIndexingButtons(SFacet buttons,boolean forward){
		((Iterator)((SimpleCore)buttons).master).flashIterate(forward);
	}
	final public void fireTriggerButton(SFacet buttons){
		((TriggerButton)((SimpleCore)((LinkMount)buttons).facets[0]).master).flashFire();
	}
	public static void fillFontSizes(int kitMidSize){
		for(int i=0;i<fontSizes.length;i++)fontSizes[i]=kitMidSize-fontSizeMidAt+i;
	}
	public static double fontFactor(){
		return(double)fontSizes[fontSizeAt]/fontSizes[fontSizeMidAt];
	}
	final public MenuFacets windowMenuFacets(SContentAreaTargeter area,boolean withLayout){
		return new WindowFacetBuilder(this,area).newMenuFacets(withLayout);
	}
	/**
	Creates facets for an application edit menu. 
	<p>{@link #getFacets()} returns facets 
	retargeted on elements of {@link SContentAreaTargeter#viewer()} 
	and indexed by {@link ViewerTarget#TARGETS_VIEWABLE}.   
	 */
	public final class EditFacets extends MenuFacets{
		private final STargeter[]elements;
		public EditFacets(SContentAreaTargeter area){
			this(area,TITLE_EDIT_MENU);
		}
		public EditFacets(SContentAreaTargeter area,String title){
			super(area.viewer(),title);
			elements=findEditElements(area);
		}
		public SFacet[]getFacets(){
			int count=elements.length;
			final SFacet[]facets=new SFacet[count];
			for(int i=0;i<facets.length;i++)facets[i]=triggerMenuItems(elements[i],HINT_NONE);
			return facets;
		}
	}
	public static class SuggestionsCoupler extends STextual.Coupler implements TypeKeyable{
		final private ValueNode state;
		public static final String TYPE="suggest";
		public SuggestionsCoupler(AppValues spec){
			ValueNode suggestionsRoot=spec.state(TYPE);
			if(!suggestionsRoot.type().equals(TYPE))throw new IllegalArgumentException(
					suggestionsRoot+" not a suggestions root in "+Debug.info(this));
			else state=Nodes.guaranteedChild(suggestionsRoot,TYPE,typeKey());
		}
		protected STrigger commitTrigger(){
			throw new RuntimeException("Not implemented in "+Debug.info(this));
		}
		final public Collection<String>suggestions(){
			return Collections.unmodifiableCollection(newStateList());
		}
		final public void updateSuggestions(String text,boolean remove){
			text=text.trim();
			if(text.equals(""))return;
			List suggestions=newStateList();
			if(remove)suggestions.remove(text);
			else{
				if(suggestions.contains(text))suggestions.remove(text);
				suggestions.add(0,text);
			}
			if(false)Util.printOut("TextualCoupler:" +Debug.id(this)+" suggestions=",suggestions);
			state.setContents(suggestions.toArray());
		}
		private List<String>newStateList(){
			return new ArrayList(Arrays.asList(state.values()));
		}
		@Override
		public String typeKey(){
			return Util.shortTypeNameKey(this);
		}
	}
	/**
	Coupler for any combination of indexed list and text box. 
	<p>Implements the coupler interfaces of both 
	{@link facets.core.superficial.SIndexing} and {@link facets.core.superficial.STextual}. 
	 */
	public abstract static class ComboCoupler extends SIndexing.Coupler{
	   public boolean indexedTitleEditable(SIndexing i){return true;}
	   public abstract void indexedTitleEdited(String edit);
	   final public void _textSet(STextual t){indexedTitleEdited(t.text());}
	 }
	public static abstract class TriggerCodeCoupler extends STrigger.Coupler{
		private String active="Active code not set";
		public abstract String[]codes();
		@Override
		final public void fired(STrigger t){
			firedCode(active);
		}
		protected abstract void firedCode(String code);
		public void setActiveCode(String code){
			active=null;
			for(String each:codes())if(each.equals(code))active=code;
			if(active==null)active=codes()[0];
		}
		public String activeCode(){
			return active;
		}
	}
	@Override
	public SFacet extras(){
		return null;
	}
	final public FeatureHost newWindowHost(FacetAppSurface app){
		return kit.newWindowHost(app,app.spec);
	}
	final public SFacet appExtras(FacetAppSurface app){
		return extras(app.surfaceTargeter(),
				spec.forSlave()?null:app.actions.showHelp,true);
	}
	final public SFacet extras(final AreaTargeter targeter,final SToggling showHelp, 
			final boolean forApp){
		final ValueNode stateApp=spec.state(PATH_APP);
		final SFacet graph=!spec.nature().getBoolean(ARG_GRAPH_BUILD)?null
				:ExtrasGraph.newFaceted(targeter,stateApp,kit,this),
			help=showHelp==null?null:new ExtrasHelp(helpPages,this);
		if(graph==null&&help==null)return null;
		final class ExtrasMount extends SwitchMount{
			final SIndexing indexing=swapReferences(new Coupler());
			ExtrasMount(Toolkit kit){
				super("Extras",kit);
			}
			public void retarget(STarget target,Impact impact){
				if(!impact.exceeds(Impact.MINI))throw new IllegalArgumentException(
						"Bad impact=" +impact+" in "+Debug.info(this));
				else super.retarget(target,impact);
				SAreaTarget area=(SAreaTarget)target;
				if(false&&forApp&&((AreaTargeter)area.areaParent().notifiable()
					).indexedTargeter()!=targeter)return;
				boolean showGraph=graph!=null&&graphShowWhere==(forApp?GRAPH_APP:GRAPH_DIALOGS);
				if(items!=null&&items.length>1)indexing.setIndex(showGraph?0:1);
				if(showGraph)graph.retarget(target,impact);
				else if(help!=null&&showHelp.isSet())help.retarget(target,impact);
			}
		}
		SwitchMount mount=new ExtrasMount(kit);
		STarget target=targeter.target();
		if(false&&spec.nature().getBoolean(ARG_GRAPH_BUILD))Util.printOut(
				"FacetFactory.extras: " +Debug.info(target)+" "+showHelp);
		mount.retarget(target,Impact.DEFAULT);
		targeter.attachFacet(mount);
		if(help==null)mount.setFacets(graph);
		else if(graph==null)mount.setFacets(help);
		else mount.setFacets(graph,help);
		return mount;
	}
	/**
	Creates a hosted {@link PagedSurface} in 
		{@link FacetFactory#launchOrphanDialog(DialogSurfacer, String, PagedActions, PagedContenter...)}. 
	 */
	public interface DialogSurfacer{
		/**
		Create a {@link PagedSurface} to expose any {@link PagedContenter}s type-identical to
		those passed. 
		 */
		PagedSurface newSurface(String title,HideableHost host,PagedActions actions,
				PagedContenter[]contents,FacetFactory ff,AppSpecifier spec);
	}
	public final Response launchOrphanDialog(DialogSurfacer surfacer,String title, 
			PagedActions actions,PagedContenter...contents){
		HideableHost host=kit.newOrphanDialogHost(spec);
		PagedSurface surface=surfacer.newSurface(title,host,actions,contents,this,spec);
		surface.buildRetargeted();
		Response response=((DialogHost)host).launchWindowedSurface(surface,null);
		if(false)trace(".launchOrphanDialog: response=",response);
		return response;
	}
	/**
	Creates a facet defining a pushbutton with a selectable action code. 
	@param t must be retargeted on a trigger whose coupler is a {@link TriggerCodeCoupler} 
	 */
	final public SFacet triggerCodeButton(STargeter t){
		KitSwing swing=(KitSwing)kit;
		return new SimpleCore(t,new TriggerCodeButtonSwing(swing),kit);
	}
	public final SFacet sidebarHost(STargeter toggling,FacetHostable hostable){
		HostingFacetSwing host=new Paged(hostable,kit);
		toggling.attachFacet(host);
		host.retarget(toggling.target(),Impact.DEFAULT);
		return host;
	}
	/**
	Returns a sub-factory for viewer, area and surface facets. 
	 */
	final public AreaFacets areas(){
		return areas==null?areas=new AreaFacets(this,spec):areas;
	}
	/**
	Implements interface method. 
	@return <code>null</code> by default
	 */
	public SFacet[]header(){
		return null;
	}
	/**
	Implements interface method. 
	@return <code>null</code> by default
	 */
	public SFacet toolbar(){
		return null;
	}
	/**
	Implements interface method. 
	@return <code>null</code> by default
	 */
	public SFacet sidebar(){
		return null;
	}
	/**
	Implements interface method. 
	@return <code>null</code> by default
	 */
	public SFacet status(){
		return null;
	}
	/**
	Implements interface method. 
	@return by default a private {@link SimpleServices} calling out to
	{@link #getServicesContextMenuFacets()}
	 */
	public SurfaceServices services(){
		return new SimpleServices(){
			@Override
			final public MenuFacets getContextMenuFacets(){
				return getServicesContextMenuFacets();
			}
		};
	}
	/**
	Called from default implementation of #{@link SimpleServices}. 
	@return <code>null</code> by default
	 */
	protected MenuFacets getServicesContextMenuFacets(){
		return null;
	}
	public class SimpleServices implements SurfaceServices{
		@Override
		public MenuFacets getContextMenuFacets(){
			return null;
		}
		public boolean isBlocking(){
			return false;
		}
		public void handleBlockedKey(int code){
			throw new RuntimeException("Not implemented in "+Debug.info(this));
		}
		@Override
		final public void handleInvalidInput(STarget target,Object input){
			throw new RuntimeException("Not implemented in "+Debug.info(this));
		}
	}
	/**
	Creates standard menus to match an {@link AppSpecifier}. 
	@return menus possibly modified by {@link #adjustMenuRoots(MenuFacets[])}
	 */
	final protected SFacet[]newAdjustedMenus(FacetAppSurface app,SContentAreaTargeter area){
		STargeter pane=findPaneTargeter(area);
		MenuFacets appMenu=new AppFacetsBuilder(this,area).newMenuFacets(),
			editMenu=!app.spec.canEditContent()||findEditElements(area).length==0?null
					:new EditFacets(area),
			paneMenu=pane==null||pane.elements().length==0?null
					:areas().new PaneFacets("Pane",pane),
			windowMenu=app.contentStyle==ContentStyle.SINGLE?null:windowMenuFacets(area,false),
			helpMenu=true?null:helpMenuFacets(area);
		List<SFacet>menus=new ArrayList();
		for(MenuFacets possible:adjustMenuRoots(new MenuFacets[]{
				appMenu,editMenu,paneMenu,windowMenu,helpMenu
		}))
			if(possible!=null)menus.add(menuRoot(possible));
		if(menus==null||menus.size()<1)throw new IllegalStateException(
				"Null or empty menus in "+Debug.info(this));
		else return menus.toArray(new SFacet[]{});
	}
	/**
	Retrieve a {@link STargeter} targeting the return of 
	{@link AreaFacets#panesGetTarget(SAreaTarget)}. 
	@return the appropriate {@link STargeter} or <code>null</code>
	 */
	protected STargeter findPaneTargeter(SContentAreaTargeter area){
		return null;
	}
	/**
	Enables modification of standard menus. 
	@param menus created in {@link #newAdjustedMenus(FacetAppSurface, SContentAreaTargeter)}
	@return by default <code>menus</code>
	 */
	protected MenuFacets[]adjustMenuRoots(MenuFacets[]menus){
		return menus;
	}
	protected STargeter[]findEditElements(SContentAreaTargeter area){
		STargeter[]elements=area.viewer().elements()[ViewerTarget.TARGETS_VIEWABLE].elements();
		int count=elements[0].elements().length==0?0:elements.length;
		return count>0?elements:new STargeter[]{};
	}
	public static final class AppletFeatures implements LayoutFeatures{
		public final boolean panelToSide;
		private final SFacet toolPanel;
		private final SFacet[]menus;
		public AppletFeatures(SFacet toolPanel,SFacet[]menus,boolean panelToSide){
			this.toolPanel=toolPanel;
			this.menus=menus;
			this.panelToSide=panelToSide;
		}
		@Override
		public SFacet toolbar(){
			return toolPanel;
		}
		@Override
		public SFacet[]header(){
			return menus;
		}
		@Override
		public SFacet status(){
			return null;
		}
		@Override
		public SFacet sidebar(){
			return null;
		}
		@Override
		public SurfaceServices services(){
			return null;
		}
		@Override
		public SFacet extras(){
			return null;
		}
	}
	/**
	Creates facets for an application help menu. 
	<p>The {@link MenuFacets} created by this method 
	returns menu facets retargeted on elements defined  
	in {@link AppActions}.   
	@param rootTargeter is passed to the {@link MenuFacets} created. 
	 */
	final public MenuFacets helpMenuFacets(SContentAreaTargeter rootTargeter){
		final STargeter appTargeter=(AreaTargeter)rootTargeter.notifiable(), 
			elements[]=appTargeter.elements(
					)[AppActions.TARGETS_HELP].elements();
		final SFacet[]facets=new SFacet[elements.length];
		for(int i=0;i<facets.length;i++)
			facets[i]=elements[i].title().equals(AppConstants.TITLE_APP_HELP)?
				togglingCheckboxMenuItems(elements[i],HINT_NONE)
				:triggerMenuItems(elements[i],HINT_NONE);
		return new MenuFacets(appTargeter,TITLE_HELP_MENU){
			public SFacet[]getFacets(){
				return facets;
			}
		};
	}
	/**
	Creates facets for an application edit toolbar. 
	<p>Calls {@link #toolGroups(STargeter, String, SFacet...)} with facets
	retargeted on elements provided by {@link SContentAreaTargeter#viewer()} 
	and indexed by {@link ViewerTarget#TARGETS_VIEWABLE}.   
	 */
	final public SFacet[]editTools(STargeter viewer){
		STarget target=viewer.target();
		if(!(target instanceof ViewerTarget))throw new IllegalArgumentException(
				"Bad target="+Debug.info(target));
		STargeter[]elements=viewer.elements(),viewables;
		if(elements.length-1<TARGETS_VIEWABLE)throw new IllegalStateException(
				"No viewables in "+target);
		else viewables=elements[TARGETS_VIEWABLE].elements();
		SFacet[]facets=new SFacet[viewables.length];
		for(int i=0;i<facets.length;i++){
			STargeter link=viewables[i];
			if(link.elements().length==0)throw new IllegalStateException(
					"Empty elements in "+link);
			else facets[i]=triggerButtons(link,HINT_BARE);
		}
		return facets;
	}
	final public ClipperSource statefulClipperSource(boolean useSystemClipboard){
		return kit.statefulClipperSource(useSystemClipboard);
	}
	final public void warningCritical(String appTitle,Exception e,boolean inOpen){
		kit.warningCritical(new ExceptionTexts(appTitle,e),e,inOpen);		
	}
	public final void setAppProvidingCache(ProvidingCache cache){
		kit.setCache(providingCache=cache);
	}
	final public ProvidingCache providingCache(){
		if(providingCache==null)throw new IllegalStateException(
				"Null providingCache in "+Debug.info(this));
		return providingCache;
	}
	public static String version(){
		return "0.8.80";
	}
	public static void resetCounts() {
		KitCore.widgets=facets=TargetCore.targets=0;
		TargeterCore.targeters.clear();
	}
	public static String newCountsText(){
		return "[Created widgets "+KitCore.widgets+
		", facets "+FacetFactory.facets+
		", targeters "+TargeterCore.targeters+
		", targets "+TargetCore.targets+
		"]";
	}}
