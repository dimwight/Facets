package facets.core.app;
import facets.core.superficial.FacetedTarget;
import facets.core.superficial.SFrameTarget;
import facets.core.superficial.SIndexing;
import facets.core.superficial.STarget;
import facets.core.superficial.STrigger;
import facets.core.superficial.TargetCore;
import facets.core.superficial.app.SAreaTarget;
import facets.core.superficial.app.SView;
import facets.core.superficial.app.SelectionView;
import facets.core.superficial.app.ViewableAction;
import facets.core.superficial.app.ViewableFrame;
import facets.core.superficial.app.ViewerTarget;
import facets.util.Debug;
import java.util.HashMap;
import java.util.Map;
/**
{@link ViewerTarget} that couples {@link ViewableAction}s to {@link STrigger}s. 
<p>{@link ActionViewerTarget} extends its superclass by defining
<ul><li>matching implementations of {@link ViewerTarget#newActionTriggers}
and {@link ViewerTarget#getTriggerAction}
<li>{@link #newActionTriggerGroup(String, ViewableAction[])} which can
create trigger groups for applications to return from {@link #newActionTriggerGroups(ViewableAction[])}
</ul> 
<p>The {@link Action} enumeration defines a range of {@link ViewableAction}s. 
<p>{@link ActionViewerTarget} is defined <code>abstract</code> to ensure that
the static class of application instances marks 
the dynamic type defined by the {@link ViewerTarget#actionTriggers} created
during construction.
 */
public abstract class ActionViewerTarget extends ViewerTarget{
	private static final class ViewersTarget extends ActionViewerTarget{
		ViewersTarget(String title,ViewableFrame viewable,STarget views){
			super(title,viewable,views);
		}
	}
	private final class ActionCoupler extends STrigger.Coupler{
		final ViewableAction action;
		ActionCoupler(ViewableAction action){
			this.action=action;
		}
		public void fired(STrigger t){
			ActionViewerTarget.this.actionTriggerFired(action);
		}
	}
	/**
	Defines a range of {@link ViewableAction}s. 
	 */
	public enum Action implements ViewableAction{
		COPY("C&opy"),CUT("Cu&t"),PASTE("P&aste"),PASTE_INTO("Paste &Child"),
		DELETE("De&lete"),EDIT("Mo&dify|Re&name"),
		ITERATE_FORWARD("Next|"+AppConstants.ARROW_RIGHT),
		ITERATE_BACK("Previous|"+AppConstants.ARROW_LEFT),
		SELECT_ALL("Select &All"),
		FOLDER_UP("Folder Up|Up"),FOLDER_OPEN("Folder Open|Open"),
		UNDO("Undo|Undo Edit"),REDO("Redo|Redo Edit");
		private final String title;
		Action(String title){this.title=title;}
		public String toString(){return title;}
		public static ViewableAction newTitled(final String title){
			return new ViewableAction(){
				@Override
				public String toString(){
					return title;
				}
			};
		}
	}
	private final Map<ViewableAction,STrigger>triggerMap=new HashMap();
	/**
	Unique constructor. 
	<p>All parameters are passed to the superclass. 
	 */
	public ActionViewerTarget(String title,ViewableFrame viewable,STarget views){
		super(title,viewable,views);
	}
	/**
	Implements abstract method. 
	<p>Returns triggers coupled via an internal subclass of 
	{@link facets.core.superficial.STrigger.Coupler}
	to <code>actions</code> and suitable for passing to {@link #getTriggerAction(STrigger)}. 
	 */
	final protected STrigger[]newActionTriggers(ViewableAction[]actions){
		STrigger[]triggers=new STrigger[actions.length];
		for(int i=0;i<triggers.length;i++)
			triggers[i]=new STrigger(actions[i].toString(),
					new ActionCoupler(actions[i]));
		return triggers;
	}
	/**
	Implements abstract method. 
	<p>Returns the {@link ViewableAction} coupled to <code>trigger</code> by 
	{@link #newActionTriggers(ViewableAction[])}  
	 */
	final protected ViewableAction getTriggerAction(STrigger trigger){
		return((ActionCoupler)trigger.coupler).action;
	}
	/**
	Creates a target group containing action triggers. 
	<p>Groups created are suitable for return by {@link #newActionTriggerGroups(ViewableAction[])}. 
	@param title for the group
	@param actions a subset of those passed to the constructor
	@return a group containing the members of {@link #actionTriggers}
	 coupled to <code>actions</code> in {@link #newActionTriggers(ViewableAction[])}
	 */
	protected final STarget newActionTriggerGroup(String title,ViewableAction[]actions){
		if(triggerMap.size()==0){
			for(int i=0;i<actionTriggers.length;i++)
				triggerMap.put(((ActionCoupler)actionTriggers[i].coupler).action,
						actionTriggers[i]);			
		}
		STarget[]triggers=new STarget[actions.length];
		for(int i=0;i<triggers.length;i++)
			if((triggers[i]=triggerMap.get(actions[i]))==null)
					throw new IllegalStateException("Null trigger in "+Debug.info(this));
		return new TargetCore(title,triggers);
	}
	/**
	Convenience method for creating viewer arrangements. 
	<p>Creates {@link SAreaTarget}s each wrapping an instance of <i>the same</i> 
	trivial subclass of {@link ActionViewerTarget}; this method cannot therefore be 
	used where the {@link ViewableFrame} will define different {@link ViewableAction}s 
	for the underlying {@link SView}s.   
	@param viewable will be shared by all viewers created
	@param views each wrapping a {@link SView} or {@link SIndexing}
	 */
	public static SAreaTarget[]newViewerAreas(ViewableFrame viewable,
			STarget[]views){
		ViewerTarget[]viewers=new ViewerTarget[views.length];
		for(int i=0;i<viewers.length;i++){
			STarget frame=views[i];
			viewers[i]=new ViewersTarget(frame.title(),viewable,frame);
		}
		SAreaTarget[]areas=new SAreaTarget[viewers.length];
		for(int i=0;i<areas.length;i++)
			areas[i]=SAreaTarget.newArea(views[i].title(),viewers[i]);
		return areas;
	}
}
