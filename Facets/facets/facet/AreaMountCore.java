package facets.facet;
import facets.core.app.MountFacet;
import facets.core.superficial.Notifying;
import facets.core.superficial.STarget;
import facets.core.superficial.Notifying.Impact;
import facets.core.superficial.app.SAreaTarget;
import facets.facet.kit.*;
import facets.util.Debug;
abstract class AreaMountCore extends FacetCore implements MountFacet{
  AreaMountCore(SAreaTarget area,Toolkit kit){
		super(area,kit);
		area.attachFacet(this);
	}
  @Override
  final public void targetNotify(Object msg,boolean interim){
    SAreaTarget area=(SAreaTarget)target;
		area.ensureActive(Impact.MINI);
		area.notifyParent(interim?Impact.CONTENT:Impact.DEFAULT);
	}
  @Override
  final public void retarget(STarget target,Impact impact){
		if(impact==Impact.MINI)return;
		else if(target==null)throw new IllegalArgumentException("Null target in "+Debug.info(this));
    else if(target instanceof SAreaTarget)retargeted((SAreaTarget)target,impact);
    if(true)setEnablesToTarget();
  }
  protected void retargeted(SAreaTarget area,Notifying.Impact impact){
		target=area;
		if(true)area.attachFacet(this);
	}
  @Override
	protected KWrap[]lazyParts(){
		return null;
	}
}

