package facets.facet;
import facets.core.app.MountFacet;
import facets.core.superficial.SFacet;
import facets.core.superficial.SIndexing;
import facets.core.superficial.STarget;
import facets.core.superficial.SIndexing.Coupler;
import facets.facet.kit.*;
import facets.util.Debug;
import facets.util.StringFlags;
class SwitchMount extends FacetCore implements MountFacet{
	private final String indexingTitle;
  protected KWrap[]items;
  private SIndexing indexing;
	private Coupler external;
  SwitchMount(String indexingTitle,Toolkit kit){
    super(STarget.NONE,kit);
    this.indexingTitle=indexingTitle;
  }
  SIndexing swapReferences(Coupler coupler){
  	external=coupler;
    if(indexing!=null)return indexing;
    return indexing=new SIndexing(indexingTitle,new SIndexing.Coupler(){
      public boolean[]liveStates(SIndexing s){
				return external.liveStates(s);
			}
      public void indexSet(SIndexing s){
				((KMount)base()).setActiveItem((KWrap)s.indexed());
			}
      public Object[]getIndexables(){
      	if(items==null)throw new IllegalStateException("Null items in "+Debug.info(this));
      	return items;
      }
      public String[]newIndexableTitles(SIndexing i){
				return external.newIndexableTitles(i);
			}
    });
  }
	public void setFacets(SFacet...facets){
    items=new KWrap[facets.length];
	  for(int i=0;i<items.length;i++)
	    items[i]=((KitFacet)facets[i]).base();
  }
  KWrap lazyBase(){
		return kit.switchMount(this,items,new StringFlags(FacetFactory.HINT_BARE));
	}
  protected KWrap[]lazyParts(){return null;}
}