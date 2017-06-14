package facets.facet;
import static facets.facet.kit.KButton.*;
import facets.core.app.AppConstants;
import facets.core.superficial.SIndexing;
import facets.core.superficial.STarget;
import facets.core.superficial.STrigger;
import facets.core.superficial.Notifying.Impact;
import facets.facet.kit.KButton;
import facets.facet.kit.KWrap;
import facets.facet.kit.KButton.Type;
import facets.facet.kit.swing.KitSwing;
import facets.util.Debug;
import facets.util.StringFlags;
class TriggerButton extends SimpleMaster{
	protected KButton button;
	protected final int usage;
	TriggerButton(int usage,StringFlags hints){
    super(hints);
    this.usage=usage;
  }
  public void attachedToFacet(){
  	STrigger trigger=(STrigger)core.target();
  	button=core.newRegisteredButtons(Type.Fire,usage,new String[]{title()},hints)[0];
  }
	protected void notifyingSingle(STarget t,Object msg){
	  if(true||t instanceof STrigger)((STrigger)t).fire();
	}
	void setEnables(STarget target){
		if(false)trace(".setEnables: ",Debug.info(target)+": "+target.isLive());
    button.setEnabled(target.isLive());
  }
	KWrap lazyBaseWrap(){
		return button;
	}
	KWrap[]lazyPartWraps(){
		return null;
	}
	void flashFire(){
		KitSwing.flashButton(button);
  	STrigger trigger=(STrigger)core.target();
  	trigger.fire();
  	trigger.notifyParent(Impact.DEFAULT);
	}
}
