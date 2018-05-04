package facets.core.app;
import facets.core.superficial.Notifying;
import facets.core.superficial.SFrameTarget;
import facets.core.superficial.SIndexing;
import facets.core.superficial.STarget;
import facets.core.superficial.STargeter;
import facets.core.superficial.STrigger;
import facets.core.superficial.TargetCore;
import facets.core.superficial.TargeterCore;
import facets.core.superficial.Notifying.Impact;
import facets.core.superficial.app.FacetedTarget;
import facets.util.StatefulCore;
/**
Connects a facet to the viewer and retargeting architectures. 
<p>{@link ViewerTarget} connects a viewer facet 
to the surface targeter tree in three distinct ways.
<p>Firstly, it implements {@link SViewer} to supply a viewer facet with 
	view policy and content based on two <code>final</code> {@link STarget} 
	members passed to the constructor:
<ul>
	<li><code>views</code> is either a {@link SFrameTarget} whose {@link SFrameTarget#framed}
		is a {@link SView}, or an {@link SIndexing} of several such.
	<li><code>viewable</code> is the {@link ViewableFrame} 
		whose <code>framed</code> is to be exposed in whole or part by the 
		viewer facet attached to the {@link ViewerTarget}</li>
</ul>
<p>Both <code>views</code> and (most commonly) <code>viewable</code> can 
	be shared with other {@link ViewerTarget}s. 
<p>Secondly, {@link ViewerTarget} implements {@link FacetedTarget} to represent 
	its facet in turn to the retargeting architecture via the {@link 
	SAreaTarget} content area tree. 
<p>Finally, the {@link ViewableAction}s passed to the constructor are
coupled to {@link STrigger} targets that can be
<ul>
	<li>queried by the viewer facet to set/adjust its key mappings
	<li>exposed in the surface
	<li>used to trigger actions on viewable content 
</ul>
<p>The {@link STargeter} returned by {@link #newTargeter()} ensures that 
	trigger live states are updated at each retargeting. 
 */
public abstract class ViewerTarget extends ViewerTargetCore{
	private static final class Targeter extends TargeterCore{
		private Targeter(Class target){
			super(target);
		}
		public void retarget(STarget target,Notifying.Impact impact){
			super.retarget(target,impact);
			((ViewerTarget)target).updateActionLiveStates();
		}
	}
	/**
	Final re-implementation. 
	@return a targeter 	that
	<ul>
	<li> updates trigger live states during
		 {@link STargeter#retarget(STarget, Notifying.Impact)}; 
	<li>returns from {@link STargeter#targetType()} the type of {@link ViewerTarget#view()}
	</ul>
	 */
	public final STargeter newTargeter(){
		return new Targeter(view().getClass());
	}
	public static final int TARGETS_VIEWABLE=0,TARGETS_FACET=1;
	/**Coupled to the {@link ViewableAction}s passed to the constructor. */
	public final STrigger[]actionTriggers;
	private final ViewableAction[]actions;
	/**
	 Unique constructor. 
	<p>The <code>views</code> passed can be either:   
	<ul>
	  <li>a single {@link facets.core.superficial.SFrameTarget} wrapping 
	    a {@link SView}</li>
	  <li>an {@link facets.core.superficial.SIndexing} of several such</li>
	</ul>
	 @param title passed to superclass 
	 @param viewable stored as immutable member
	 @param views stored as immutable member; if an {@link SIndexing}
	 the {@link ViewerTarget} is set as its notification monitor
	 */
	protected ViewerTarget(String title,ViewableFrame viewable,STarget views){
		super(title,viewable,views);
		SView view=view();
		actions=viewable.viewerActions(view);
		if(false&&view instanceof TreeView)trace(".ViewerTarget: view="+view+" actions=",actions);
		actionTriggers=newActionTriggers(actions);
		for(STrigger t:actionTriggers)t.setLive(false);
	}
	/**
	Create triggers coupled to the actions passed. 
	<p>Called from the constructor (so should not rely on instance variables
	in subclasses)
	@param actions were passed to the constructor
	@return a {@link STrigger}[] for storage as {@link #actionTriggers} 
	 */
	protected abstract STrigger[]newActionTriggers(ViewableAction[]actions);
	/**
	Re-implementation. 
	Returns groups of {@link STrigger}s created by {@link #newActionTriggerGroups(ViewableAction[])};	 
	 */
	protected STarget[]lazyElements(){
		return new STarget[]{
			new TargetCore("Viewable targets",newActionTriggerGroups(actions)),
			new TargetCore("Facet targets",facetTargets())
		};
	}
	/**
	Compose groups of triggers for use in the targeter tree. 
	<p>Groups must contain only members of {@link #actionTriggers}. 
	@param actions 
	@return a {@link STarget}[] for return by {@link ViewerTarget#lazyElements()};
	default returns a {@link STarget}[] with a single member grouping
	all {@link #actionTriggers} 
	 */
	protected STarget[]newActionTriggerGroups(ViewableAction[]actions){
		return new STarget[]{new TargetCore("All Actions",actionTriggers)};
	}
	/**
	Retrieve the action coupled to a trigger.
	<p>Called during retargeting; the trigger live state is set to the value 
	returned by {@link ViewableFrame#actionIsLive(SViewer, ViewableAction)},
	@param trigger was coupled in {@link #newActionTriggers(ViewableAction[])}
	to the action to be checked	
	 */
	protected abstract ViewableAction getTriggerAction(STrigger trigger);
	/**
	Responds to firing of a trigger coupled to the action. 
	<p>Calls {@link ViewableFrame#actionTriggered(SViewer, ViewableAction)}
	with <code>action</code>.
	@param action was coupled in {@link #newActionTriggers(ViewableAction[])}	
	to the trigger fired 	
	 */
	final protected void actionTriggerFired(ViewableAction action){
		viewable.actionTriggered(this,action);
	}
	final void updateActionLiveStates(){
		for(STrigger t:actionTriggers)
			t.setLive(viewable.actionIsLive(this,getTriggerAction(t)));		
	}
	/**
	Convenience method that wraps {@link SView}s in {@link SFrameTarget}s. 
	@param views to be wrapped
	 */
	public final static SFrameTarget[]newViewFrames(SView...views){
		SFrameTarget[]frames=new SFrameTarget[views.length];
	  for(int i=0;i<frames.length;i++)frames[i]=new SFrameTarget(views[i]){};
	  return frames;		
	}
}
