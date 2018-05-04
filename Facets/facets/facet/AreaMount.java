package facets.facet;
import facets.core.app.SAreaTarget;
import facets.core.superficial.Notifying;
import facets.core.superficial.SFacet;
import facets.core.superficial.STarget;
import facets.core.superficial.Notifying.Impact;
import facets.facet.kit.*;
import facets.util.Debug;
import facets.util.Objects;
import facets.util.Util;
abstract class AreaMount extends AreaMountCore{
  AreaMount(SAreaTarget area,Toolkit kit){
		super(area,kit);
	}
  protected void retargeted(SAreaTarget area,Impact impact){
    super.retargeted(area,impact);
  	if(area.indexableTargets().length==0)return;
    STarget indexed=area.indexedTarget();
    if(!(indexed instanceof SAreaTarget))return;
    KWrap active=((KitFacet)((SAreaTarget)indexed).attachedFacet()).base();
    ((KMount)base()).setActiveItem(active);
  }
  final public void setFacets(SFacet...facets){
		KWrap[]bases=new KWrap[facets.length];
		for(int i=0;i<bases.length;i++)bases[i]=((KitFacet)facets[i]).base();
		((KMount)base()).setItems(bases);
  }
}