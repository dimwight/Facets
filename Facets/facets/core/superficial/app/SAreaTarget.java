package facets.core.superficial.app;
import static facets.util.Debug.*;
import static facets.util.app.Events.*;
import facets.core.app.NestedView;
import facets.core.superficial.Facetable;
import facets.core.superficial.FacetedTarget;
import facets.core.superficial.SFacet;
import facets.core.superficial.SFrameTarget;
import facets.core.superficial.SIndexing;
import facets.core.superficial.STarget;
import facets.core.superficial.STargeter;
import facets.core.superficial.Notifying.Impact;
import facets.core.superficial.app.SContentAreaTargeter.ContentArea;
import facets.util.Debug;
import facets.util.ItemList;
import facets.util.tree.TypedNode;
import java.util.Arrays;
/**
Represents viewers and other surface areas to the targeter tree. 
<p>{@link SAreaTarget} manages updating and focus for trees of viewers 
	and other facets, being the target of a directly attached facet 
	which it represents within an <a href="#AreaTrees">area target tree</a>. 
<p>Much key functionality is defined in the {@link IndexingTarget} superclass. </p>
<h3><a name="AreaTrees"></a>Area target trees</h3>
<p>The {@link IndexingTarget#indexableTargets()} of an {@link SAreaTarget} must 
	comprise one of</p>
<ul>
	<li>a single {@link SFrameTarget} or {@link ViewerTarget} 
	<li> one or more child {@link SAreaTarget}s 
</ul>
<p>A tree of {@link SAreaTarget}s therefore terminates in {@link SFrameTarget} 
	or {@link ViewerTarget} leaves. </p>
<p>The facets attached to and represented in the application by 
	{@link FacetedTarget} ({@link SAreaTarget} 
	and {@link ViewerTarget}) members of a tree generally form a containment hierarchy 
	of tabs/windows and panes. 
 */
public class SAreaTarget extends IndexingTarget implements FacetedTarget{
	public static final boolean mutableAreaFacets=false;
  public static final STarget[]EMPTY_AREA_CHILDREN={new SFrameTarget("Empty area")};
  private SFacet facet;
	/**
	Core constructor which sets indexing. 
	<p>Only used in framework subclasses. 
	 @param title passed to superclass
	  @param children passed to superclass
	 */
	protected SAreaTarget(String title,SIndexing children){
	  super(title,children);
	}
	/**
	Convenience method passing indexable children. 
	@param title passed to superclass 
	@param children passed to superclass in an 
	{@link SIndexing} with a default coupler. 
	 */
	public static SAreaTarget newArea(String title,STarget...children){
		return new SAreaTarget(title,SIndexing.newDefault(title,children));
	}
	/**
	Convenience method to a wrap single viewer in area. 
	<p>Passes to core constructor the <code>title</code> of <code>viewerFrame</code>,
	which is wrapped in an {@link SIndexing} of the same title. 
	 */
	final public static SAreaTarget newSingleViewerArea(ViewerTarget viewer){
	  String title=viewer.title();
		return new SAreaTarget(title,SIndexing.newDefault(title,new STarget[]{viewer}));
	}
	/**
	Implements interface method. 
	<p>In addition to to retargeting any {@link #attachedFacet()}, calls
	{@link Facetable#retargetFacets(Impact)} in all {@link #indexableTargets()},
	starting with {@link #indexedTarget()} to minimise apparent latency. 
	 */
	final public void retargetFacets(Impact impact){
		if(facet!=null){
			facet.retarget(this,impact);
			if(trace)traceEvent("Retargeted facet in "+this);
		}
	  STarget children[]=indexableTargets(),indexed=indexedTarget();
	  if(indexed instanceof Facetable)((Facetable)indexed).retargetFacets(impact);
	  for(STarget child:children)
	  	if(child!=indexed)((SAreaTarget)child).retargetFacets(impact);
	}
	final public void attachFacet(SFacet facet){
	  if(!mutableAreaFacets&&this.facet!=null&&this.facet!=facet)throw new RuntimeException(
	      "Facet already set in "+info(this)+", can't replace \n"+
	      info(this.facet)+" with "+info(facet)
	    );
	  else this.facet=facet;
	  if(false&&mutableAreaFacets)trace("\nSAreaTarget: attached ",info(facet)+" to\n\t"+info(this));
	}
	/**
	Overrides superclass method. 
	@param children must return 
	as its <code>indexables</code> a non-empty {@link STarget}[] 
	which is either 
	<ul>
		<li>single-member {@link STarget}[] containing either 
		a {@link ViewerTarget} or a {@link SFrameTarget}</li>
		<li>an <code>AreaTarget[]</code> </li>
	</ul>
	 */
	final public void setIndexing(SIndexing children){
		super.setIndexing(children);
		STarget[]targets=indexableTargets();
		if(targets.length>1){
			if(!(targets instanceof SAreaTarget[]))
				throw new IllegalArgumentException("Multiple non-area children in "+info(this));
		}
		else{
			STarget singleChild=targets[0];
			if(singleChild instanceof ViewerTarget)
				((ViewerTarget)singleChild).setAreaParent(this);
			else if(!(singleChild instanceof SFrameTarget
					||singleChild instanceof SAreaTarget))
				throw new IllegalArgumentException("Bad single child in "+info(this));
		}
	}
	/**
	Must return an {@link AreaTargeter}. 
	 */
	public STargeter newTargeter(){
		return new AreaTargeter(getClass());
	}
	final public SFacet attachedFacet(){
		if(facet==null)throw new IllegalStateException("Null facet in "+info(this)+" "+title()
				+(false?"":"\nparent="+parent()+"\ndescendants="+info(descendants())));
		else return facet;
	}
	final public boolean isActive(){
	  boolean inPath=true;
	  IndexingTarget parent=this.parent(),child=this;
	  while(inPath&&parent!=null){
	    inPath&=child==parent.indexing().indexed();
	    child=parent;
	    parent=parent.parent();
	  }
	  return inPath;
	}
	final public void ensureActive(Impact impact){
		IndexingTarget parent=this.parent();
		if(parent==null||isActive())return;
		((SAreaTarget)parent).ensureActive(impact);
		SIndexing indexing=parent.indexing();
		if(this!=indexing.indexed()&&Arrays.asList(indexing.indexables()).contains(this)){
			indexing.setIndexed(this);
			if(impact.exceeds(Impact.MINI))notifyParent(impact);
		}
	}
	@Override
	public String toString(){
		return super.toString()+" active="+isActive();
	}
	/**
	The {@link FacetedTarget} representing the contained viewer or area facet 
	that should have the focus. 
	<p>Checks the {@link #indexedTarget()} of this {@link SAreaTarget} 
	and its descendents until it finds the last that is a {@link FacetedTarget}.
	 */
	final public FacetedTarget activeFaceted(){
		FacetedTarget facet=this;
		STarget child;
		while(facet instanceof SAreaTarget){
			child=((SAreaTarget)facet).indexedTarget();
			if(!(child instanceof FacetedTarget))break;
			facet=(FacetedTarget)child;
		}
	  return facet;
	}
	/**
	Finds the {@link SFrameTarget} framing the current content exposed by the area tree. 
	<p>Checks the ancestors of this {@link SAreaTarget} 
	and until it finds one that is a {@link ContentArea},
	returns its {@link SContenter#contentFrame()}
	 */
	final public SFrameTarget contenterFrame(){
		if(true)throw new RuntimeException("Not tested in "+Debug.info(this));
		SAreaTarget check=this;
	  while(!(check instanceof ContentArea))check=(SAreaTarget)check.parent();
	  if(check==null)throw new IllegalStateException(
				"Null check in "+Debug.info(this));
	  else return ((ContentArea)check).contenter.contentFrame();
	}
	/**
	Depth-first traversal of this area tree, including the root. 
	 */
	public final STarget[]descendants(){
		ItemList<STarget>descendents=new ItemList(STarget.class);
		descendents.addItem(this);
		STarget[]children=indexableTargets();
		for(int i=0;i<children.length;i++)
			if(children[i]instanceof SAreaTarget)
				descendents.addItems(((SAreaTarget)children[i]).descendants());
			else descendents.addItem(children[i]);
		return descendents.items();
	}
	final public SAreaTarget areaParent(){
		return (SAreaTarget)parent();
	}
	final protected TypedNode[]newDebugChildren(){
    ItemList<TypedNode>items=new ItemList(TypedNode.class);
    items.addItems(super.newDebugChildren());
    if(parent()!=null)items.addItem(newDebugLeafNode("parent",parent()));
    if(facet!=null)items.addItem(newDebugLeafNode("facet",facet));
    if(false)traceDebug(".newDebugChildren: ",this);
    return items.items();
  }
}
