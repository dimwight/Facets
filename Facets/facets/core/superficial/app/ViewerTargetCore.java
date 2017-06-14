package facets.core.superficial.app;
import static facets.core.superficial.app.SAreaTarget.*;
import static facets.util.Debug.*;
import static facets.util.app.Events.*;
import facets.core.superficial.FacetedTarget;
import facets.core.superficial.Notifying;
import facets.core.superficial.SFacet;
import facets.core.superficial.SFrameTarget;
import facets.core.superficial.SIndexing;
import facets.core.superficial.SRetargetable;
import facets.core.superficial.STarget;
import facets.core.superficial.TargetCore;
import facets.core.superficial.Notifying.Impact;
import facets.util.Debug;
import facets.util.ItemList;
import facets.util.tree.TypedNode;
class ViewerTargetCore extends TargetCore implements FacetedTarget,SViewer{
	/**Either a {@link SFrameTarget} decorating a {@link SView},  
	 or an {@link SIndexing} of several such. */
	public final STarget views;
	/**Frames content of which some portion is to be exposed by the viewer facet. 
	 */
	public final ViewableFrame viewable;
	SAreaTarget areaParent;
	private SSelection selection;
	private SFacet facet;
	ViewerTargetCore(String title,ViewableFrame viewable,STarget views){
		super(title);
		this.viewable=viewable;
		this.views=views;
		if(views instanceof SIndexing)views.setNotifiable(this);
	}
	final STarget[]facetTargets(){
		return facet!=null&&facet instanceof Targeted?
				((Targeted)facet).targets():new STarget[]{};
	}
	/**
	Implements abstract method. 
	@return the {@link SFrameTarget#framed} returned by {@link #viewFrame()} 
	 */
	@Override
	final public SView view(){
		return(SView)viewFrame().framed;
	}
	/**
	Implements abstract method. 
	<p>Returns a {@link SSelection} for display in its viewer facet,
	calling {@link ViewableFrame#newViewerSelection(SViewer)} 
	to obtain a suitable (sub-)selection within its content. 
	<p>The {@link SSelection} is memoed and returned at any subsequent invocation
	until the next invocation of {@link #retargetFacets(Notifying.Impact)}, when it is destroyed
	before {@link SRetargetable#retarget(STarget,Notifying.Impact)} is called on {@link #attachedFacet()}.
	 */
	@Override
	final public SSelection selection(){
		if(selection!=null)return selection;
	  selection=viewable.newViewerSelection(this);
	  if(trace)traceEvent("Selection "+Debug.info(selection)+" defined for "
	  		+Debug.info(view())+" by "+Debug.info(viewable));
		return selection;
	}
	/**
	Implements interface method. 
	<p>Calls {@link ViewableFrame#viewerSelectionChanged(SViewer,SSelection)} 
	followed by {@link Notifying#notifyParent(Impact)}.
	 */
	@Override
	final public void selectionChanged(SSelection selection){
		if(trace)traceEvent("Selection changed "+Debug.info(selection)+" for "
	  		+Debug.info(viewable)+" in "+Debug.info(this));
	  viewable.viewerSelectionChanged(this,selection);
	  this.notifyParent(Impact.SELECTION);
	}
	/**
  Implements interface method. 
  <p>Calls {@link ViewableFrame#viewerSelectionEdited(SViewer, Object, boolean)} followed by
  {@link Notifying#notifyParent(Impact)}.
   */
	@Override
  final public void selectionEdited(SSelection selection,Object edit,boolean interim){
  	if(trace)traceEvent("Selection edited "+Debug.info(selection)+" for "
	  		+Debug.info(viewable)+" in "+Debug.info(this));
  	if(selection!=null)viewable.viewerSelectionChanged(this,selection);
		viewable.viewerSelectionEdited(this,edit,interim);
	  this.notifyParent((interim?Impact.CONTENT:Impact.DEFAULT));
	}
  final public void clearSelection(){
		selection=null;
	}
	@Override
	final public void attachFacet(SFacet facet){
	  if(!mutableAreaFacets&&this.facet!=null)throw new RuntimeException(
	  		"Facet is immutable in "+Debug.info(this));
	  else this.facet=facet;
  	if(false&&mutableAreaFacets)trace(": attached ",info(facet)+" to\n\t"+info(this));
	}
	@Override
	final public SFacet attachedFacet(){
	  if(facet==null)throw new IllegalStateException("Null facet in "+Debug.info(this));
	  else return facet;
	}
	/**
	Implements interface method. 
	<p>Retargets any attached facet on this {@link ViewerTarget}; 
	also destroys the currently memoed {@link SSelection}.
	 */
	@Override
	public final void retargetFacets(Impact impact){
		selection=null;
	  if(facet==null)return;
	  if(false)traceDebug(".retargetFacets: ",this);
	  facet.retarget(this,impact);
	  if(trace)traceEvent("Retargeted facet " +Debug.info(facet)+" in "+this);
	}
	@Override
	public String toString(){
		return super.toString()+(true?"":(" live="+isLive()));
	}
	@Override
	public boolean isActive(){
		return areaParent().isActive();
	}
	@Override
	final public void ensureActive(Impact notify){
		areaParent().ensureActive(notify);		
	}
	@Override
	final public SAreaTarget areaParent(){
		if(areaParent==null)throw new IllegalStateException(
				"Null areaParent in "+Debug.info(this));
		return areaParent;
	}
	/**
	Re-implementation that ensures the viewable is also set live. 
	 */
	@Override
	final public void setLive(boolean live){
		super.setLive(live);
		viewable.setLive(live|viewable.isLive());
	}
	/**
	Represents the {@link SView} currently returned by {@link #view()}. 
	<p>(Encapsulates the logic for the implementation of {@link #view()}.) 
	@return either the single {@link SFrameTarget} or the <code>indexed</code>
	of the {@link SIndexing} set as {@link #views}.
	 */
	public final SFrameTarget viewFrame(){
		if(views==null)throw new IllegalStateException("No views in "+Debug.info(this));
		return views instanceof SIndexing?
				(SFrameTarget)((SIndexing)views).indexed():(SFrameTarget)views;
	}
	@Override
	protected final boolean notifiesTargeter(){
		return true;
	}
	@Override
	protected TypedNode[]newDebugChildren(){
    ItemList<TypedNode>items=new ItemList(TypedNode.class);
    items.addItems(super.newDebugChildren());
    items.addItem(newDebugSourcesNode("views",views));
    if(true)items.addItem(newDebugSourcesNode("viewable",viewable));
    if(facet!=null)
      items.addItem(newDebugSourcesNode("facet",facet));
    return items.items();
  }
	public void setAreaParent(SAreaTarget parent){
		if(parent==null)throw new IllegalArgumentException("null parent in "+Debug.info(this));
		areaParent=parent;		
	}
}
