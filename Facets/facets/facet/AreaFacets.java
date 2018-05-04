package facets.facet;
import static facets.facet.AreaFacets.PaneDialogStyle.*;
import static facets.facet.FacetFactory.*;
import static facets.util.StringFlags.*;
import facets.core.app.ActionAppSurface;
import facets.core.app.FacetHostable;
import facets.core.app.MenuFacets;
import facets.core.app.MountFacet;
import facets.core.app.NestedView;
import facets.core.app.SAreaTarget;
import facets.core.app.SView;
import facets.core.app.ViewerContenter;
import facets.core.app.ViewerTarget;
import facets.core.superficial.SFacet;
import facets.core.superficial.STarget;
import facets.core.superficial.STargeter;
import facets.core.superficial.TargetCore;
import facets.core.superficial.Notifying.Impact;
import facets.core.superficial.app.FacetedTarget;
import facets.facet.AreaFacets.PaneDialogStyle;
import facets.facet.HostingFacetSwing.Nested;
import facets.facet.app.FacetAppSurface;
import facets.facet.kit.KWrap;
import facets.facet.kit.KitFacet;
import facets.facet.kit.Toolkit;
import facets.facet.kit.swing.ViewerBase;
import facets.util.Debug;
import facets.util.NumberPolicy;
import facets.util.StringFlags;
import facets.util.Tracer;
import facets.util.app.AppValues;
import facets.util.shade.Shade;
import facets.util.shade.Shades;
import facets.util.tree.ValueNode;
import java.util.Arrays;
import javax.swing.JComponent;
/**
Sub-factory for viewer, area and surface facets.
<p>{@link AreaFacets} provides a range of methods to create and attach viewer 
facets and their containing area facets to members of the area target tree.
 */
final public class AreaFacets extends Tracer{
	final public static Shade COLOR_DESKTOP=Shades.gray;
	final public static String 
		HINT_PANE_STACK=newFlag("Stacked pane only"),  
		HINT_PANE_TABS=newFlag("Unstacked panes in tabs"),  
		PANE_TITLE_FLIP="Flip",PANE_TITLE_RESET="Reset|Reset Tabs",
		PANE_TITLE_TABS="Reset Pane|Reset Tabs",
		PANE_TITLE_MAXIMISE="Ma&ximise Active",
		PANE_TITLE_RESTORE="Restore All",
		PANE_TITLE_ASK="PaneAsk",PANE_TITLE_DEFAULT="PaneDefaults",
		PAGES_SINGLE="Single page",
		PAGES_SHARED="Shared page",
		TITLE_DESKTOP_TILE="Tile Windows",
		TITLE_DESKTOP_SCALE="Scale to Desktop",
		KEY_DESKTOP_NO_SCALE="desktopNoScale",
		KEY_DESKTOP_MAXIMUM="desktopMaximum";
	final public static int TARGET_DESKTOP_TILE=0,TARGET_DESKTOP_SCALE=1;
	/**Pane layout constant*/
	final public static int PANE_LEFT=0,PANE_UPPER=PANE_LEFT,PANE_RIGHT=1,
		PANE_LOWER=PANE_RIGHT,PANE_SPLIT_VERTICAL=0,PANE_SPLIT_HORIZONTAL=1,PANE_STACK=-1,
		PANE_LAYOUT=0,PANE_SHOW=1,PANE_ACTIVE=2,PANE_SELECT=3,PANE_SELECT_OPTIONS=4,
		PANE_LAST=PANE_SELECT_OPTIONS,
		PANE_ACTIVE_MAXIMISE=0,PANE_ACTIVE_RESTORE=1;
	/**State storage key element*/
	final public static String 
		PANE_KEY_FLIP="paneFlip_",PANE_KEY_SPLITS="paneSplits_",
		PANE_KEY_TABS_AND_HIDES="paneTabsAndHides_",
		PANE_KEY_SWAPS="paneSwaps_",
		TABLE_KEY_COLUMNS="tableColumns_",TABLE_KEY_SORT_COL="tableSortCol_",
		TABLE_KEY_SORT_UP="tableSortUp_",TYPE="areas";
	final public static NumberPolicy SASH_SPLIT_POLICY=new NumberPolicy(10,90){
		public int format(){
			return 0;
		}
	};
	public interface PaneLinking extends KitFacet{
		void linkDefined(KWrap from,KWrap to);
		boolean canLink(KWrap tab);
	}
	public enum PaneDialogStyle{None,Simple,Options}
	public class PaneFacets extends MenuFacets{
		private final STargeter shows;
		private final SFacet[]facets;
		public PaneFacets(String title,STargeter t){
			super(t,title);
			STargeter elements[]=targeter.elements();
			shows=elements[PANE_SHOW];
			String hints=HINT_NONE;
			PaneDialogStyle dialog=dialogStyle();
			facets=new SFacet[]{
				dialog!=None?core.triggerMenuItems(
						elements[dialog==Simple?PANE_SELECT:PANE_SELECT_OPTIONS],hints)
						:core.togglingCheckboxMenu(shows,hints),
						core.triggerMenu(elements[PANE_LAYOUT],hints),
				core.triggerMenuItems(elements[PANE_ACTIVE],hints),
			};
		}
		protected PaneDialogStyle dialogStyle(){
			return shows.elements().length<3?None:Simple;
		}
		@Override
		public SFacet[]getFacets(){
			return facets;
		}
	}
	private final FacetFactory core;
	private final ValueNode areaState;
	public AreaFacets(FacetFactory core,AppValues values){
		this.core=core;
		areaState=values==null?new ValueNode(TYPE,"Area dummy"):values.state(TYPE);
	}
	/**
	Constructs the root facet of a multi-content application surface. 
	 <p>If the widget kit has the capacity to do so, may construct this root as 
	 <ul>
	  <li>a tab folder with tabs containing content root core</li> 
	 <li>an internal desktop whose windows contain these core</li>
	</ul>
	 @param appArea the surface root
	 @param app the app
	 */
	public MountFacet appMultiContentFacet(final SAreaTarget appArea,
			final FacetAppSurface app){
		final class MultiContentFacet extends AreaMount{
			MultiContentFacet(Toolkit kit){
				super(appArea,kit);
			}
			protected KWrap lazyBase(){
				return kit.appMultiMount(this,app);
			}
		}
		return new MultiContentFacet(core.kit);
	}
	/**
	Creates a viewer facet for the viewer frame contained by an area. 
	 <p>The facet, with an avatar pane defined by <code>vam</code>,
	  is attached to the {@link ViewerTarget} 
	 returned by {@link SAreaTarget#activeFaceted()} in <code>area</code>. 
	 <p>The facet returned <i>contains</i> the viewer facet and is 
	 accessible as {@link SAreaTarget#attachedFacet()}in <code>area</code>; 
	 it manages the toolkit container of the avatar pane. 
	 */
	public SFacet viewerArea(SAreaTarget area,final ViewerAreaMaster vam){
		final ViewerTarget viewer=(ViewerTarget)area.activeFaceted();
		final SView view=viewer.view();
		return!(view instanceof NestedView)?
			new ViewerFacet(viewer,core,vam,areaState).newAreaFacet(area)
				:new AreaMountCore(area,core.kit){
			final HostingFacetSwing hosting=new Nested((NestedView)view,core.kit);
			@Override
			protected void retargeted(SAreaTarget area,Impact impact){
				hosting.retarget(area,impact);
			}
			@Override
			KWrap lazyBase(){
				return hosting.base();
			}
			@Override
			public void setFacets(SFacet...facets){
				throw new RuntimeException("Not implemented in "+Debug.info(this));
			}
		};
	}
	/**
	Attaches a {@link MountFacet} to the area passed. 
	@return the mount attached
	 */
	public MountFacet mount(SAreaTarget area,final boolean inset){
		AreaMount mount=new AreaMount(area,core.kit){
			KWrap lazyBase(){
				return kit.spreadMount(this,inset);
			}
		};
		return mount;		
	}
	/**
	Convenience method that creates and lays out viewer areas in sash panes or tabs. 
	@param area containing at least one viewer area meeting the contract of 
	{@link ViewerContenter}<code>.newContentViewers</code>
	 @param keyable may be a {@link ViewerAreaMaster}; if not a default is constructed
	 @param layoutHint passed where required 
	to {@link #attachPanes(SAreaTarget,SFacet[], int)}	
	 */
	public void attachViewerAreaPanes(final SAreaTarget area,final Object keyable,
			int layoutHint){
		ViewerAreaMaster vam=keyable instanceof ViewerAreaMaster?(ViewerAreaMaster)keyable
				:new ViewerAreaMaster(){
			protected String hintString(){
				return area.indexableTargets().length==1?HINT_BARE:HINT_NONE;
			}
			@Override
			public String typeKey(){
				return newTypeKey(keyable);
			}
		};
		if(area.indexedTarget()instanceof SAreaTarget){
			SFacet[]childAreas=viewerAreaChildren(area,vam);
			if(childAreas.length>1)attachPanes(area,childAreas,layoutHint);
			else mount(area,false).setFacets(childAreas[0]);
		}
		else viewerArea(area,vam);
	}
	/**
	Convenience method to define a simple pane set laid out in either sashes or tabs.
	<p>Duplicates {@link #attachPanes(SAreaTarget,SFacet[], int[][], double[], int[], String[])}
	with constructed parameters.   
	@param layoutHint one of {@link #PANE_SPLIT_HORIZONTAL}, {@link #PANE_SPLIT_HORIZONTAL}, 
	{@link #PANE_STACK}
	 */
	public void attachPanes(SAreaTarget area,SFacet[]viewers,
  		int layoutHint){
		final int content=area.indexableTargets().length,
				codes[]=new int[content==1?0:content*2-3];
		Arrays.fill(codes,layoutHint);
		new PaneSet(area,core.kit,new PaneSetLayout(area,areaState,
				new int[][]{codes},new double[]{},new int[]{},new String[]{}));
	}
	/**
	Convenience method to define a pane set with a single root.
	<p>Duplicates {@link #attachPanes(SAreaTarget,SFacet[], int[][], double[], int[], String[])}
	with a single-member <code>int[][]</code>; unspecified parameters are constructed. 
   */
  public void attachPanes(SAreaTarget area,SFacet[]viewers,
  		int[]codes,double[]splitDefaults){
			new PaneSet(area,core.kit,new PaneSetLayout(area,areaState,
					new int[][]{codes},splitDefaults,new int[]{},new String[]{}));
	}
	/**
	Builds pane set based on sashed mounts which can also appear as tabs.
	<p>The root pane is attached to <code>area</code>; the members of  
	<code>contents</code> must be be attached to its child areas. 
	<p>The <code>int[]</code> members of <code>codes</code> 
	each define a layout as described below. 
	Any <code>int[]</code> member after the first defines a layout
	within the next available child as defined by the first. 
	<p>Layouts are defined as follows using <code>PANE_</code> and <code>SPLIT_</code> constants:   
<ul>
	<li>The first constant specifies how the current pane is to be split ie 
	{@link #PANE_SPLIT_HORIZONTAL}
	or {@link #PANE_SPLIT_VERTICAL}
	<li>The second specifies which of the panes thus created is to be split further ie
	{@link #PANE_LEFT}/{@link #PANE_UPPER} or {@link #PANE_RIGHT}/{@link #PANE_LOWER}
	</ul>	
	@param area holds the children
	@param viewers 
	 @param codes define a tree of sash splits using <code>PANE_</code> 
	constants as described above
	 @param splitDefaults defines default values for each pane split
	 @param stackAndHideDefaults panes to be hidden initially, preceded by root areas
	to set children in stacked tabs
	 @param groups titles for root areas 
   */
  public void attachPanes(SAreaTarget area,SFacet[]viewers,
  		int[][]codes,double[]splitDefaults,
  		int[]stackAndHideDefaults,String[]groups){
			new PaneSet(area,core.kit,
			new PaneSetLayout(area,areaState,codes,splitDefaults,stackAndHideDefaults,groups));
	}
	public STarget panesGetTarget(SAreaTarget area){
		if(area.indexableTargets().length==1)return new TargetCore("Single Pane");
		area.retargetFacets(Impact.DEFAULT);
		return((PaneSet)area.attachedFacet()).targets;
	}
	public void panesValidateLayout(SAreaTarget area){
		((PaneSet)area.attachedFacet()).layout.validate();
	}
	/**
	Convenience method that creates viewers for an array of area children. 
	 */
	public SFacet[]viewerAreaChildren(SAreaTarget area,ViewerAreaMaster vam){
	  ViewerAreaMaster[]childMasters=vam.childMasters(area);
	  STarget[]childAreas=area.indexableTargets();
	  SFacet[]viewers=new SFacet[childMasters.length];
	  for(int i=0;i<childMasters.length;i++)
			viewers[i]=viewerArea((SAreaTarget)childAreas[i],childMasters[i]);
		return viewers; 
	}
	public void attachFacetArea(SFacet facet,SAreaTarget area){
		SAreaTarget then=(SAreaTarget)((FacetCore)facet).target();
		if(then.contenterFrame().framed==area.contenterFrame().framed)return;
		area.attachFacet(then.attachedFacet());
		area.activeFaceted().attachFacet(then.activeFaceted().attachedFacet());
	}
	public SFacet viewerGrid(SAreaTarget area,ViewerAreaMaster vam){
		final KWrap[]contents=FacetsCore.newBaseWraps(viewerAreaChildren(area, vam));
	  final StringFlags hints=new StringFlags(vam.hintString()+HINT_BARE+HINT_GRID);
	  return new AreaMountCore(area,core.kit){
			protected KWrap lazyBase(){
			  for(int i=0;i<contents.length;i++)registerPart(contents[i]);
	      return kit.wrapMount(this,contents,0,0,hints);
			}
		  public void setFacets(SFacet...facets){
				throw new RuntimeException("Not implemented in "+Debug.info(this));
			}
	  };
	}
	public SFacet viewerTabs(SAreaTarget area,ViewerAreaMaster vam){
		final SFacet[]viewers=viewerAreaChildren(area, vam);
		return new AreaMountCore(area,core.kit){
			final protected KWrap lazyBase(){return kit.viewerTabs(viewers);}
		  public void setFacets(SFacet...facets){
				throw new RuntimeException("Not implemented in "+Debug.info(this));
			}
		};
	}
	public MountFacet tabs(SAreaTarget area,final String hints){
		STarget[]children=area.indexableTargets();
		final SFacet[]facets=new SFacet[children.length];
		for(int i=0;i<facets.length;i++)
			facets[i]=((SAreaTarget)children[i]).attachedFacet();
		if(false)area.trace("tabs: " +area,children);
		return new AreaMount(area,core.kit){
			final protected KWrap lazyBase(){
				return kit.areaTabs(facets,new StringFlags(hints));
			}
		};
	}
	/**
	Create a programmatically switchable container for facets in an area tree. 
	<p>The container will be switchable programmatically but not within the GUI eg 
	a card layout. 
	@param area will have attached a {@link MountFacet} managing a container
	for those in turn managed by the {@link SFacet}s attached to the {@link FacetedTarget}
	members of its {@link SAreaTarget#indexableTargets()}. 
	 */
	public MountFacet switchMount(SAreaTarget area){
		STarget[]children=area.indexableTargets();
		final KWrap[]bases=new KWrap[children.length];
		for(int i=0;i<bases.length;i++)bases[i]=
			((KitFacet)((SAreaTarget)children[i]).attachedFacet()).base();
		if(false)area.trace(".switchMount: "+area,children);
		return new AreaMount(area,core.kit){
			KWrap lazyBase(){
				return kit.switchMount(this,bases,StringFlags.EMPTY);
			}
		};
	}
	public void disposeViewer(ViewerTarget viewer){
		((ViewerBase)((KitFacet)viewer.attachedFacet()).base()).disposeWrapped();
	}
	static ViewerAreaMaster activeMaster(SAreaTarget area){
		SFacet facet=area.activeFaceted().attachedFacet();
		return facet instanceof ViewerFacet?((ViewerFacet)facet).vam:null;
	}
}
