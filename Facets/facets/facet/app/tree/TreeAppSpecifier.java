package facets.facet.app.tree;
import static facets.facet.app.FacetPreferences.*;
import static facets.facet.app.tree.TreeTargets.*;
import facets.XmlView;
import facets.core.app.ActionAppSurface;
import facets.core.app.AppActions;
import facets.core.app.FeatureHost;
import facets.core.app.ListView;
import facets.core.app.MenuFacets;
import facets.core.app.NodeViewable;
import facets.core.app.PagedContenter;
import facets.core.app.TableView;
import facets.core.app.TreeView;
import facets.core.app.ViewerContenter;
import facets.core.app.AppSurface.ContentStyle;
import facets.core.app.FeatureHost.LayoutFeatures;
import facets.core.superficial.SFacet;
import facets.core.superficial.SFrameTarget;
import facets.core.superficial.SIndexing;
import facets.core.superficial.STarget;
import facets.core.superficial.STargeter;
import facets.core.superficial.app.SAreaTarget;
import facets.core.superficial.app.SContentAreaTargeter;
import facets.core.superficial.app.SContenter;
import facets.core.superficial.app.SHost;
import facets.core.superficial.app.SSurface;
import facets.core.superficial.app.SView;
import facets.core.superficial.app.ViewableFrame;
import facets.core.superficial.app.SHost.FacetLayout;
import facets.facet.FacetFactory;
import facets.facet.app.FacetAppSpecifier;
import facets.facet.app.FacetAppSurface;
import facets.facet.app.FacetPreferences;
import facets.util.Debug;
import facets.util.FileSpecifier;
import facets.util.tree.DataConstants;
import facets.util.tree.DataNode;
import facets.util.tree.Nodes;
import facets.util.tree.TypedNode;
import facets.util.tree.XmlPolicy;
import facets.util.tree.XmlSpecifier;
/**
{@link FacetAppSpecifier} for simple applications with {@link DataNode} content. 
<p>{@link TreeAppSpecifier} exemplifies practical use of the superclass with
a {@link ViewerContenter} (here {@link TreeAppContenter}) that provides the detail functionality.  
It also demonstrates with {@link #newContentRootTargets(FacetAppSurface)} and
{@link #newTreeMenuItems(FacetFactory, STargeter[], STargeter[])} the key  
<a href="http://superficial.sourceforge.net/">Superficial</a>  mechanism for binding 
data abstracted as {@link STarget}s to widgets abstracted as {@link SFacet}s 
via an automatically-generated {@link STargeter} tree.  
<p>Concrete applications can be readily created by extending {@link TreeAppSpecifier} 
and types such as: 
<ul>
<li>{@link XmlPolicy} (especially {@link XmlPolicy#getTitleAttributeNames()})
and {@link XmlSpecifier}
<li>{@link SView}s such as {@link TreeView}, {@link ListView}, {@link TableView} 
for appropriate content 
</ul>
@see XmlView
 */
public abstract class TreeAppSpecifier extends FacetAppSpecifier{
	/** Argument keys */
	public static final String ARG_TREE_SIZE="treeSize";
	/**
	Unique constructor. 
	@param appClass passed to superclass
	 */
	public TreeAppSpecifier(Class appClass){
		super(appClass);
	}
	@Override
	public PagedContenter[]adjustPreferenceContenters(SSurface surface,
			PagedContenter[]contenters){
		return false?contenters:new PagedContenter[]{
			contenters[PREFERENCES_TRACE],
			contenters[PREFERENCES_GRAPH],
			contenters[PREFERENCES_VALUES],
			contenters[PREFERENCES_VIEW],
		};
	}
	/**
	Locks superclass implementation. 
	 */
	@Override
	protected
	final AppActions newActions(ActionAppSurface app){
		return super.newActions(app);
	}
	@Override
	public boolean headerIsRibbon(){
		return args().getOrPutBoolean(ARG_RIBBON,false);
	}
	/**
	Final implementation. 
	<p>The {@link FacetAppSurface} returned calls out to {@link #getInternalContentSource()} 
	and to {@link XmlPolicy#fileSpecifiers()} via {@link #xmlPolicy()}; it creates
	 {@link TreeAppContenter}s. 
		 */
	@Override
	final protected FacetAppSurface newApp(FacetFactory ff,FeatureHost host){
		return new FacetAppSurface(this,ff){
			@Override
			public FileSpecifier[]getFileSpecifiers(){
				return((TreeAppSpecifier)spec).xmlPolicy().fileSpecifiers();
			}
			@Override
			protected Object getInternalContentSource(){
				return((TreeAppSpecifier)spec).getInternalContentSource();
			}
			@Override
			protected SContenter newContenter(Object source){
				return new TreeAppContenter(source,this);
			}
		};
	}
	/**
	Defines an {@link XmlPolicy} for the application content. 
	<p>Called by the return of {@link #newApp(FacetFactory, FeatureHost)} 
	and by {@link TreeAppContenter#saveToSink(Object)}.  
	@return by default a plain {@link XmlPolicy}
	 */
	protected XmlPolicy xmlPolicy(){
		return new XmlPolicy(){
			@Override
			protected boolean treeAsXmlRoot(){
				return false?true:super.treeAsXmlRoot();
			}
			@Override
			protected boolean dataUsesAttributes(){
				return false?false:super.dataUsesAttributes();
			}
			public XmlSpecifier[]fileSpecifiers(){
				return false?new XmlSpecifier[]{
					new XmlSpecifier("state.xml","Facets state files",this),
					super.fileSpecifiers()[0],
					super.fileSpecifiers()[1],
				}
				:super.fileSpecifiers();
			};
		};
	}
	/**
	Fulfils {@link FacetAppSurface#getInternalContentSource()} in the
	return of {@link #newApp(FacetFactory, FeatureHost)}.
	@return by default {@link Nodes#newTestTree(String, int, double)} with <code>broad</code>=5 which
	may be changed by passing {@link #ARG_TREE_SIZE}=<code>broad</code> 
	 */
	protected Object getInternalContentSource(){
		return Nodes.newTestTree("Test",nature().getOrPutInt(ARG_TREE_SIZE,4),0.75);
	}
	/**
	Enables redefinition of {@link SView}s supplying viewer policy. 
	<p>Called from {@link TreeAppContenter#newContentViewers(ViewableFrame)}. 
	@param viewable has the content as its {@link SFrameTarget#framed}
	@return by default a single {@link TreeView}
	 */
	protected SView[]newContentViews(NodeViewable viewable){
		final String rootTitle=((TypedNode)viewable.framed).title();
		final boolean liveViews=canEditContent();
		SView view=new TreeView("View"){
			public boolean hideRoot(){
				return true||rootTitle.endsWith(DataConstants.TYPE_XML);
			}
			public boolean isLive(){
				return liveViews;
			}
		},
		basic=new TreeView("Basic"){
			@Override
			public String contentIconKey(Object content){
				return null;
			}
			public boolean isLive(){
				return liveViews;
			}
		};
		return true&&System.getProperty("XmlViewDebug")!=null?new SView[]{basic,view}
			:new SView[]{view};
	}
	/**
	Enables definition of additional {@link STarget}s to be exposed in the
	{@link TreeTargets#TITLE_MENU} menu. 
	<p>Called from {@link TreeAppContenter#lazyContentAreaElements(SAreaTarget)}. 
	@return targets whose {@link STargeter}s will be  
	passed to {@link #newTreeMenuItems(FacetFactory, STargeter[], STargeter[])};
	by default an empty {@link STarget}<code>[]</code>
	 */
	protected STarget[]newContentRootTargets(FacetAppSurface app){
		return new STarget[]{};
	}
	/**
	Defines {@link SFacet} items for the {@link TreeTargets#TITLE_MENU} menu. 
	<p>Called from {@link TreeAppFeatures#adjustMenuRoots(MenuFacets[])} 
	@param ff the calling {@link TreeAppFeatures} 
	@param treeLinks exposing {@link TreeTargets#appTargets()} 
	@param contentLinks targeting returns (if any) of {@link #newContentRootTargets(FacetAppSurface)} 
	@return by default <code>treeAppFacets</code> indexed with 
	{@link TreeTargets#TARGET_SEARCH} and with 
		{@link TreeTargets#TARGET_ENCODE},{@link TreeTargets#TARGET_TYPE} where
		{@link #canCreateContent()} returns <code>true</code>.
	 */
	protected SFacet[]newTreeMenuItems(FacetFactory ff,STargeter[]treeLinks,
			STargeter[]contentLinks){
		String hint=FacetFactory.HINT_NONE;
		return canCreateContent()?new SFacet[]{
			ff.triggerMenuItems(treeLinks[TARGET_SEARCH],hint),
			ff.triggerMenuItems(treeLinks[TARGET_TYPE],hint),
			ff.triggerMenu(treeLinks[TARGET_ENCODE],hint),
		}
		:new SFacet[]{
				ff.triggerMenuItems(treeLinks[TARGET_SEARCH],hint),
			};
	}
}