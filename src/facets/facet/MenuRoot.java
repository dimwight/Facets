package facets.facet;
import static facets.facet.FacetFactory.*;
import facets.core.app.MenuFacets;
import facets.core.superficial.SFacet;
import facets.core.superficial.STarget;
import facets.core.superficial.STargeter;
import facets.facet.kit.*;
import facets.facet.kit.KWrap.ItemSource;
import facets.util.Debug;
import facets.util.ItemList;
import facets.util.StringFlags;
final class MenuRoot extends FacetCore implements ItemSource{
  final MenuFacets facets;
  boolean forToolbar;
	private KWrap[]wraps;
	MenuRoot(STargeter targeter,Toolkit kit,MenuFacets facets){
    super(targeter.target(),kit);
    if(targeter==null)throw new IllegalArgumentException(
    		"Null targeter in "+Debug.info(this));
    this.facets=facets;
    targeter.attachFacet(this);
  }
	public String title(){
		return facets==null?super.title():facets.title();
	}
	public KWrap[]getItems(){
		if(false&&wraps!=null)return wraps;
		ItemList<KWrap>list=new ItemList(KWrap.class);
		SFacet[]facets=this.facets.getFacets();
		if(facets==null)throw new IllegalStateException(
				"No item facets in "+Debug.info(this));
		for(SFacet f:facets){
			if(f==null)continue;
			KWrap[]items=((KitFacet)f).items();
			for(KWrap item:items)if(item!=null)list.addItem(item);
		}
		wraps=list.items();
		kit.adjustMenuMnemonics(wraps);
		return wraps; 
	}
  protected KWrap lazyBase(){
		return kit.menu(this,title(),this,
				new StringFlags(forToolbar?HINT_USAGE_PANEL:false?HINT_DEBUG:HINT_NONE));
	}
	protected void retargetedSingle(STarget target){
		wraps=null;
	}
	protected KWrap[]lazyParts(){return null;}
}
