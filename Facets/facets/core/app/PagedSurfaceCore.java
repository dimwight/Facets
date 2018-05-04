package facets.core.app;
import facets.core.app.SContentAreaTargeter.ContentArea;
import facets.core.superficial.Notice;
import facets.core.superficial.Notifying;
import facets.core.superficial.SFacet;
import facets.core.superficial.SFrameTarget;
import facets.core.superficial.SIndexing;
import facets.core.superficial.STarget;
import facets.core.superficial.STargeter;
import facets.core.superficial.TargetCore;
import facets.core.superficial.TargeterCore;
import facets.core.superficial.Notifying.Impact;
import facets.core.superficial.app.SHost;
import facets.core.superficial.app.SSelection;
import facets.core.superficial.app.SSurface;
import facets.util.Debug;
import facets.util.Objects;
import facets.util.Tracer;
import facets.util.tree.DataNode;
import facets.util.tree.Nodes;
import facets.util.tree.TypedNode;
abstract class PagedSurfaceCore extends Tracer implements SSurface{
	static final String SIMPLE_NAME=PagedSurfaceCore.class.getSimpleName(),
		TYPE_PAGES_TREE=SIMPLE_NAME+"_PagesTree",TYPE_PAGE=SIMPLE_NAME+"_Page";
	public static final int MULTI_AREA_TREE=1,MULTI_AREA_PAGES=0;
	Class<PagedContenter>activeContentType;
  private final String title;
  private AreaTargeter targeter;
	private HideableHost host;
  protected PagedSurfaceCore(String title,HideableHost host){
  	this.title=title;
  	this.host=host;
	}
	@Override
	public SHost host(){
		if(host==null)throw new IllegalStateException(
				"Null host in "+Debug.info(this));
		else return host;
	}
	void rebuild(PagedContenter[]contents){
  	SAreaTarget[]contentRoots=new SAreaTarget[contents.length];
  	for(int i=0;i<contentRoots.length;i++)
  		contentRoots[i]=contents[i].newContentArea(targeter==null);
  	SAreaTarget[]pages=newContentPages(contents,contentRoots);
  	if(pages==null||pages.length==0)
  		throw new IllegalStateException("Null or empty panels in "+Debug.info(this));
    SAreaTarget treeArea=pages.length==1?null:newMultiplePageArea(pages);
    STarget[]rootChildren=treeArea==null?pages:new SAreaTarget[]{
    		SAreaTarget.newArea(title,pages),
		  	treeArea
		  };
	  AreaRoot surfaceRoot=new AreaRoot(title,rootChildren){
	  	protected STarget[]lazyElements(){
	  		STarget group=lazyTriggerGroup();
	  		if(group==null)throw new IllegalStateException(
	  				"Null group in "+Debug.info(this));
				return new STarget[]{group};
	  	}
  		protected boolean blockNotification(){
				return false;
			}
	  },
  	thenRoot=targeter==null?null:(AreaRoot)targeter.target();
	  if(targeter==null&&(targeter=(AreaTargeter)surfaceRoot.newTargeter())==null)
	    throw new IllegalStateException("No targeter in "+Debug.info(this));
	  if(thenRoot==null){
	  	for(SAreaTarget page:pages)
		  	if(!(page instanceof ContentArea))attachPageFacet(page);
			attachContentAreas(surfaceRoot);
			targeter.setNotifiable(this);
	  	targeter.retarget(surfaceRoot,Notifying.Impact.DEFAULT);
  		SFacet buttons=newControlButtons(targeter.elements()[0]);
  		if(buttons==null)throw new IllegalStateException("Null buttons in "+Debug.info(this));
  		HideableHost host=(HideableHost)host();
  		host.setTitle(title);
  		host.setLayout(host.newLayout(surfaceRoot.attachedFacet(),buttons,
  				newExtras(targeter)));
  	}
	  else{
  		for(STarget target:thenRoot.descendants())
  		if(areaContentType(target)==activeContentType)
  			((SAreaTarget)target).ensureActive(Impact.MINI);
			surfaceRoot.attachThenFacets(thenRoot);
			targeter.retarget(surfaceRoot,Impact.DEFAULT);
		}
	  targeter.retargetFacets(Impact.DEFAULT);
  	notify(new Notice(targeter,Impact.DEFAULT));
		Debug.memCheck("PagedSurfaceCore.rebuild: ");
	}
	protected SFacet newExtras(AreaTargeter targeter){
		return null;
	}
	private SAreaTarget newMultiplePageArea(SAreaTarget[]pages){
	  final TypedNode[]treeNodes=newPageTreeNodes(pages);
	  if(treeNodes==null||treeNodes.length==0)
	  	throw new IllegalStateException("Null or empty tree nodes in "+Debug.info(this));
	  SView treeView=newPagesTreeView(treeNodes);
	  if(treeView==null&&treeNodes.length>1)
	  	throw new IllegalStateException("No tree view in "+Debug.info(this));
		TypedNode tree=new DataNode(TYPE_PAGES_TREE,TYPE_PAGES_TREE,treeNodes);
		ViewableFrame viewable=new NodeViewable(tree){
			protected void viewerSelectionChanged(SViewer viewer,SSelection selection){
				TypedNode node=(TypedNode)selection.single();
				if(!node.type().equals(TYPE_PAGE))return;
				defineSelection(selection);
				SAreaTarget page=(SAreaTarget)node.values()[0];
				page.ensureActive(Impact.ACTIVE);
			}
		};
		viewable.defineSelection(treeNodes[0]);
		for(TypedNode node:Nodes.descendants(tree))
			if(node==tree)continue;
			else if(!node.type().equals(TYPE_PAGE))throw new IllegalStateException(
					"Invalid page node="+node);
			else if(areaContentType((STarget)node.values()[0])==activeContentType)
				viewable.defineSelection(node);
		return SAreaTarget.newSingleViewerArea(
				new ActionViewerTarget(viewable.title(),viewable,new SFrameTarget(treeView)){});
	}
	private Class areaContentType(STarget target){
		return((TargeterCore)((TargetCore)target).newTargeter()).targetType();
	}
	/**
	Create at least one dialog page. 
	<p>Where the implementation returns a multi-member array,
	<ul>
	<li>a valid implementation will be required of 
	{@link #newPagesTreeView(TypedNode[])} to define a tree/list viewer
	to select between the pages; 
	for a full tree viewer {@link #newPageTreeNodes(SAreaTarget[])} will also
	need to be re-implemented</li> 
	<li>the area passed to {@link #attachContentAreas(SAreaTarget)} will 
	contain the tree viewer area and an area containing the pages</li>
	</ul>
	<p>The default implementation defines a single-page dialog by returning 
	a single-member array containing: 
	<ul>
	  <li>if <code>contentRoots</code> is single-membered, that member
	  <li>otherwise an area containing <code>contentRoots</code>
	  (whose root facet can be displayed as tab children of the facet
	  attached to the area), titled from {{@link #title()}
	</ul>
	@return an {@link SAreaTarget}[] with at least one member
	@param contents passed during construction or to 
	{@link PagedSurface#replaceContents(PagedContenter[])};
	may be queried for additional information such as page titles
	@param contentRoots were created from <code>contents</code>
	 */
	protected SAreaTarget[]newContentPages(PagedContenter[] contents,
			SAreaTarget[]contentRoots){
		return contentRoots.length==1?contentRoots:
			new SAreaTarget[]{SAreaTarget.newArea("Single page for "+title(),contentRoots)};
	}
	/**
	Creates the top-level children of a page selection tree. 
  <p>Children created will be passed to {@link #newPagesTreeView(TypedNode[])} 
    which should be reimplemented to define a viewer for the 
    tree. 
  <p>The default implementation returns an array constructed from 
  <code>panelAreas</code> using {@link #newPageTreeNode(SAreaTarget,TypedNode[])}
  and suitable for a list viewer; this method can also be used to created
  more complex trees.
    @param pages were returned from {@link #newContentPages(PagedContenter[], SAreaTarget[])} 
 */
  protected TypedNode[]newPageTreeNodes(SAreaTarget[]pages){
  	TypedNode[]nodes=new TypedNode[pages.length];
  	for(int i=0;i<nodes.length;i++)
  		nodes[i]=newPageTreeNode(pages[i],new TypedNode[0]);
  	return nodes;
  }
  /**
	Create a view for the page tree. 
	<p>The {@link SView} returned should define a suitable 
	viewer for the tree defined by <code>treeNodes</code>, which 
	were returned by {@link #newPageTreeNodes(SAreaTarget[])}. 
	<p>Default implementation is an invalid stub. 
	@param treeNodes will have at least two members
   */
  protected SView newPagesTreeView(TypedNode[]treeNodes){
  	throw new RuntimeException("Not implemented in "+Debug.info(this));
  }
  /**
  Attach a suitable facet to this page area. 
  @param page was returned by {@link #newContentPages(PagedContenter[], SAreaTarget[])} and
  will not itself be a content root. 
   */
  protected abstract void attachPageFacet(SAreaTarget page);
  /**
  Attach suitable facets to the dialog area root and its children. 
  @param surfaceRoot contains either a single page area returned by 
  {@link #newContentPages(PagedContenter[], SAreaTarget[])} or viewer and switching
  areas managing multiple page areas   
   */
  protected abstract void attachContentAreas(SAreaTarget surfaceRoot);
  /**
  Create targets defining the top-level dialog control buttons. 
  @return a {@link STarget}[] with at least one member
   */
  abstract STarget lazyTriggerGroup();
  /**
  Create facet managing the top-level dialog control buttons. 
  @return a {@link SFacet}[] with at least one member
  @param link will match the elements returned by {@link #lazyTriggerGroup()} 
   */
  protected abstract SFacet newControlButtons(STargeter link);
	final public AreaTargeter surfaceTargeter(){
		if(targeter==null)throw new IllegalStateException(
				"Null targeter in "+Debug.info(this));
		return targeter;
	}
	@Override
	final public boolean isBuilt(){
		return targeter!=null;
	}
	public final String title(){
		return title;
	}
	/**
	Creates a node for a page selection tree. 
	<p>The node returned will 
	<ul>
		<li>be of <code>type</code> {@link #TYPE_PAGE} 
		<li> return as <code>title</code> the title of <code>panelRoot</code> 
		<li>contain <code>panelRoot</code> as its single value 
		<li>contain <code>children</code> as its children 
	</ul>
	@param page should be an area tree defining the layout 
	of a dialog page 
	@param children to appear in a tree viewer 
	below the node created; themselves created using this method 
   */
  final protected static TypedNode newPageTreeNode(final SAreaTarget page,
      final TypedNode...children){
  	return new TypedNode(Object.class,TYPE_PAGE,page.title()){
  		@Override
  		public Object[]contents(){
  			return Objects.join(Object.class,new Object[]{page},children);
  		}
  	};
  }
}
