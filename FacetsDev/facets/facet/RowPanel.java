package facets.facet;
import facets.core.superficial.SFacet;
import facets.core.superficial.STargeter;
import facets.facet.kit.KMount;
import facets.facet.kit.KWrap;
import facets.facet.kit.KitFacet;
import facets.facet.kit.Toolkit;
import facets.util.Debug;
import facets.util.ItemList;
import facets.util.StringFlags;
class RowPanel extends FacetCore{
	static boolean debug=false;
	static int panels;
	private final int hgap,vgap;
	private final StringFlags hints;
	private SFacet[]contents;
  RowPanel(STargeter t,Toolkit kit,SFacet[]contents,int hgap,int vgap,
  		StringFlags hints){
		super(t.target(),kit);
		t.attachFacet(this);
		this.hgap=hgap;
		this.vgap=vgap;
		this.contents=contents;
		this.hints=hints;
		for(SFacet facet:contents)
			if(facet instanceof MenuRoot)((MenuRoot)facet).forToolbar=true;
	}
  protected KWrap[]lazyParts(){
		return null;
	}
  protected KWrap lazyBase(){
    KMount base=kit.rowMount(this,hgap,vgap,hints);
		ItemList<KWrap>wraps=new ItemList(KWrap.class);
    for(Object set:contents!=null&&contents.length>0&&contents[0]!=null?contents:
      new Object[]{kit.label(this,Debug.info(this),hints)}){
			if(set==null||(wraps.size()==0&&set==FacetFactory.BREAK))continue;
			wraps.addItem(set==FacetFactory.BREAK?KWrap.BREAK:
					set instanceof SFacet?((KitFacet)set).base():(KWrap)set);
		}
		base.setItems(wraps.items());
    return base;
  }
}
