package facets.core.app;
import static facets.core.app.AppConstants.*;
import facets.core.app.Dialogs.Response;
import facets.core.superficial.STrigger;
import facets.core.superficial.STrigger.Coupler;
import facets.core.superficial.app.SContenter;
/**
Convenience extension of {@link PagedActions}. 
<p>{@link PagedActionDefaults} extends its superclass by defining a range
of {@link STrigger}s representing commonly required top-level dialog 
actions; class methods return instances defining convenience combinations.  
 */
public abstract class PagedActionDefaults extends PagedActions{
	private enum Trigger{OK,CANCEL,APPLY,CLOSE,CONFIRM}
	final public static PagedActions newConfirmCancel() {
		return new PagedActionDefaults(){
			public STrigger[]newTriggers(){
				return new STrigger[]{
						newTrigger(Trigger.CONFIRM),
						newTrigger(Trigger.CANCEL),
				};
			}
		};
	}
	final public static PagedActions newOkCancel() {
		return new PagedActionDefaults(){
			public STrigger[]newTriggers(){
				return new STrigger[]{
						newTrigger(Trigger.OK),
						newTrigger(Trigger.CANCEL),
				};
			}
		};
	}
	final public static PagedActions newCancel() {
		return new PagedActionDefaults(){
			public STrigger[]newTriggers(){
				return new STrigger[]{
						newTrigger(Trigger.CANCEL),
				};
			}
		};
	}
	final public static PagedActions newOk() {
		return new PagedActionDefaults(){
			public STrigger[]newTriggers(){
				return new STrigger[]{
						newTrigger(Trigger.OK),
				};
			}
		};
	}
	final public static PagedActions newApplyOkCancel() {
		return new PagedActionDefaults(){
			public STrigger[]newTriggers(){
				return new STrigger[]{
						newTrigger(Trigger.APPLY),
						newTrigger(Trigger.OK),
						newTrigger(Trigger.CANCEL),
				};
			}
		};
	}
	/**
	Returns triggers for various actions. 
	<p>The method can be overridden to implement further action types. 
	@param type constant determining the action to be performed
	 */
	protected STrigger newTrigger(Trigger type){
		final PagedSurface surface=surface();
		final PagedContenter[]contents=surface.contents();
		return type==Trigger.OK?new STrigger(TITLE_OK,new Coupler(){
				@Override
				public boolean makeDefault(STrigger t){
					return true;
				}
				public void fired(STrigger t){
					for(PagedContenter content:contents)
						content.applyChanges();
					surface.hideHost(Response.Ok); 
				}
			})
			:type==Trigger.CANCEL?new STrigger(TITLE_CANCEL,new Coupler(){
				public void fired(STrigger t){
					for(PagedContenter content:contents)
						content.reverseChanges();
					surface.hideHost(Response.Cancel); 
				}
			})
			:type==Trigger.APPLY||type==Trigger.CONFIRM?
					new STrigger(type==Trigger.APPLY?TITLE_APPLY:TITLE_CONFIRM,new Coupler(){
				public void fired(STrigger t){
					for(PagedContenter content:contents)
						content.applyChanges();
				}
			})
			:new STrigger(TITLE_CLOSE,new Coupler(){
				public void fired(STrigger t){
					surface.hideHost(Response.Close); 
				}
			});
	}
}