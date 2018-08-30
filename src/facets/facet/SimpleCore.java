package facets.facet;
import facets.core.app.FrameGroup;
import facets.core.superficial.Notifying;
import facets.core.superficial.SIndexing;
import facets.core.superficial.SNumeric;
import facets.core.superficial.STarget;
import facets.core.superficial.STargeter;
import facets.core.superficial.STextual;
import facets.core.superficial.SToggling;
import facets.core.superficial.STrigger;
import facets.core.superficial.TargetCore;
import facets.core.superficial.Notifying.Impact;
import facets.facet.kit.*;
import facets.util.Debug;
import facets.util.StringFlags;
import facets.util.Util;
import java.util.Map;
import javax.swing.JComponent;
final class SimpleCore extends FacetCore implements SwingPanelFacet{
	final public Map<String,JComponent>components(){
		return(Map<String,JComponent>)((KWidget)parts()[0]).components();
	}
	final Class singleTargetClass;
	final SimpleMaster master;
	@Override
	final protected void traceOutput(String msg){
		super.traceOutput(msg);
	}
	SimpleCore(STargeter link,SimpleMaster master,Toolkit toolkit){
		super(findContentTarget(link.target()),toolkit);
		singleTargetClass=target.getClass();
		(this.master=master).core=this;
		master.attachedToFacet();
		retarget(target,Notifying.Impact.DEFAULT);
		link.attachFacet(this);
	}
	private static STarget findContentTarget(STarget target){
		return target instanceof SIndexing
				||target instanceof SToggling
				||target instanceof SNumeric
				||target instanceof STrigger
				||target instanceof STextual
				||target.elements().length==0?
			target:target.elements()[0];
	}
	public void targetNotify(Object msg,boolean interim){
		super.targetNotify(msg,interim);
		boolean isSingle=singleTargetClass.isInstance(target);
		if(isSingle)master.notifyingSingle(target,msg);
		else{
			if(false)traceDebug(".targetNotify: singleTargetClass="
					+singleTargetClass.getSimpleName()+" target=",target);
			master.notifyingMultiple(target.elements(),msg);
			Notifying check=target;
			while(!((check=(Notifying)check.notifiable())instanceof FrameGroup));
			((FrameGroup)check).facetNotified(interim);
		}
		target.notifyParent(target.impact());
	}
	final public void retarget(STarget target,Notifying.Impact impact){
		super.retarget(target,impact);
		boolean minimal=impact==Notifying.Impact.MINI;
		boolean isSingle=singleTargetClass.isInstance(target);
		if(false)traceDebug(".retarget: isSingle="+isSingle+" target=",target);
		if(isSingle)
			master.retargetedSingle(target,impact);
		else if(!minimal)
			master.retargetedMultiple(target.elements(), impact);
		if(!minimal&&target.wantsFocus())master.respondTargetWantsFocus();
	}
	KButton[]newRegisteredButtons(KButton.Type type,int usage,String[]titles,
			StringFlags hints){
		if(titles==null||titles.length==0)throw new IllegalArgumentException(
				"Null or empty titles in "+Debug.info(this));
		KButton[]buttons=new KButton[titles.length];
		for(int i=0;i<buttons.length;i++)
			registerPart(buttons[i]=kit.button(this,type,usage,titles[i],hints));
		return buttons;
	}
	protected void setEnablesToTarget(){
		super.setEnablesToTarget();
		master.setEnables(target());
	}
  public String toString(){
  	return super.toString()+" "+master;
  }
	KWrap lazyBase(){
		return master.lazyBaseWrap();
	}
	KWrap[]lazyParts(){
		return master.lazyPartWraps();
	}
}