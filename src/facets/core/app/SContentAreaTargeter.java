package facets.core.app;
import facets.core.superficial.Notifiable;
import facets.core.superficial.Notifying;
import facets.core.superficial.SFrameTarget;
import facets.core.superficial.STarget;
import facets.core.superficial.STargeter;
import facets.core.superficial.TargetCore;
import facets.core.superficial.app.FacetedTarget;
import facets.core.superficial.app.SelectingFrame;
import facets.util.Debug;
import facets.util.ItemList;
import facets.util.Util;
import facets.util.tree.TypedNode;
/**
Heads the targeter tree for a content area. 
<p>The {@link SAreaTarget} root of each content area tree in a surface 
	must return from {@link SAreaTarget#newTargeter()} an appropriate 
	{@link SContentAreaTargeter} to create and manage the targeter tree for all content 
	areas of that type. 
<p>Content areas for applications will generally, and those for dialogs 
	frequently, include one or more viewers exposing a {@link ViewableFrame}. 
<p>In addition to creating and retargeting its own <code>elements</code> on 
	those of its {@link SAreaTarget} target, {@link SContentAreaTargeter} 
	applies specialised children on target trees representing (where present) 
<ul>
	<li>the content exposed by the area tree
	<li>the current selection within this content
	<li>the currently active viewer for the content
	<li>both the currently active view and all views used by the viewer
</ul>
<p>These child targeter trees provide attachment points for simple facets 
	forming panels, menus etc exposing their targets; viewer and area facets 
	for the surface should already be attached to the area target tree.
 */
public class SContentAreaTargeter extends AreaTargeter{
  abstract public static class ContentArea extends AreaRoot{
		final public SContenter contenter;
		protected ContentArea(String title,SContenter contenter,STarget[]children){
			super(title,children);
			this.contenter=contenter;
		}
		final protected STarget[]lazyElements(){
			return contenter.lazyContentAreaElements(this);
		}
	  public STargeter newTargeter(){
	    return new SContentAreaTargeter(contenter.targetType());
	  }
	}
	private STargeter viewer,content,view,views,selection,stateLinks[];
  public SContentAreaTargeter(Class type){
		super(type);
	}
  public void retarget(STarget target,Impact impact){
  	super.retarget(target,impact);
  	if(!(target instanceof ContentArea))
  		throw new RuntimeException("Not implemented in "+this);
  	ContentArea area=(ContentArea)target;
    SFrameTarget frame=area.contenter.contentFrame();
		content=retargetedTargeter(frame,impact);
		if(frame instanceof SelectingFrame)selection=
			retargetedTargeter(((SelectingFrame)frame).selectionFrame(),impact);	
		FacetedTarget faceted=area.activeFaceted();
  	if(!(faceted instanceof ViewerTarget))return;
  	ViewerTarget viewerTarget=(ViewerTarget)faceted;
    viewer=retargetedTargeter(viewerTarget,impact);
    STarget viewFrameTarget=viewerTarget.viewFrame();
    view=retargetedTargeter(viewFrameTarget,impact);
  	STarget viewsTarget=viewerTarget.views;
  	if(viewsTarget!=viewFrameTarget){
      if(views==null)views=((TargetCore)viewsTarget).newTargeter();
      views.retarget(viewsTarget,impact);
  	}
  }
  /**
  Extends superclass behaviour by calling {@link #retargetFacets(Notifying.Impact)} in 
  its specialised targeter members.  
   */
  public void retargetFacets(Impact impact){
    super.retargetFacets(impact);
	  if(content==null){
	  	if(false)throw new IllegalStateException("No content in "+Debug.info(this));
	  	else Util.printOut("SContentAreaTargeter.retargetFacets: No content");
	  }
	  else if(false)content.retargetFacets(impact);
    if(false&&selection!=null)selection.retargetFacets(impact);
    if(viewer==null)return;
    if(viewer.target()!=((SAreaTarget)target()).activeFaceted())
    	viewer.retargetFacets(impact);
    view.retargetFacets(impact);
    if(views!=null)views.retargetFacets(impact);
  }
  /**
	{@link STargeter} for the content exposed by the target {@link SAreaTarget}. 
	<p>The target of <code>content</code> is the {@link SFrameTarget} 
	returned as {@link SAreaTarget#contenterFrame()} 
	by the target of this {@link SContentAreaTargeter}.
	 */
	final public STargeter content(){
	  if(content==null)throw new IllegalStateException("No content in "+Debug.info(this));
	  else return content;
	}
	/**
	{@link STargeter} for the currently active view. 
	<p>The target of <code>view</code> is the {@link facets.core.superficial.SFrameTarget} 
    returned as <code>activeView</code> by the target of <code>viewer</code>.
   */
  final public STargeter view(){
    if(view==null)throw new IllegalStateException("No view in "+Debug.info(this));
    else return view;
  }
  /**
	{@link STargeter} for the currently active viewer. 
	<p>The target of <code>viewer</code> is the {@link facets.core.app.ViewerTarget} 
    returned as <code>activeViewer</code> by the target of the 
    {@link SContentAreaTargeter}.
   */
  final public STargeter viewer(){
    if(viewer==null)throw new IllegalStateException("No viewer in "+Debug.info(this));
    else return viewer;
  }
  /**
	{@link STargeter} for the currently active views target. 
	<p>The target of <code>views</code> is the {@link facets.core.superficial.STarget} 
	    returned as <code>views</code> by the target of <code>viewer</code>.
	   */
  final public STargeter views(){
    return views!=null?views:view();
  }
	/**
	{@link STargeter} for the current selection. 
	<p>The target of <code>selection</code> is the {@link SFrameTarget} 
	    returned as {@link facets.core.superficial.app.SelectingFrame#selectionFrame()} by 
	    the target of {@link #content()}.
	   */
  final public STargeter selection(){
	  if(selection==null)throw new IllegalStateException("No selection in "+Debug.info(this));
	  else return selection;
	}
	protected TypedNode[]newDebugChildren(){
    ItemList<TypedNode>items=new ItemList(TypedNode.class);
    items.addItems(super.newDebugChildren());
    if(viewer!=null)
      items.addItem(newDebugSourcesNode("viewer",viewer));
    if(views!=null)
      items.addItem(newDebugSourcesNode("views",views));
    if(view!=null)
      items.addItem(newDebugSourcesNode("view",view));
    if(content!=null)
      items.addItem(newDebugSourcesNode("content",content));
    if(selection!=null)
      items.addItem(newDebugSourcesNode("selection",selection));
    return items.items();
  }
	/**
	Returns the first {@link SContentAreaTargeter} of the specified type found in 
	the instance and its notifying tree.
	<p>Complains if none found.  
	@see Notifying
	 */
	public final SContentAreaTargeter contentAncestor(Class type){
	  Notifiable n;
	  AreaTargeter area=this;
	  SContentAreaTargeter contentArea=null;
		while(true){
			if(area instanceof SContentAreaTargeter){
				contentArea=(SContentAreaTargeter)area;
				Class targetType=contentArea.targetType();
				if(false)traceDebug(".contentTyped: contentArea="+targetType.getSimpleName()+
						" in ",this);
				if(type!=null&&type.isAssignableFrom(targetType))return contentArea;
			}
			n=area.notifiable();
			if(n instanceof AreaTargeter)area=(AreaTargeter)n;
			else return contentArea;
		}
	}
}
