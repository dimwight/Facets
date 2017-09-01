package facets.facet.kit;
import facets.util.StringFlags;
public interface KMount extends KWrap{
	void setItem(KWrap item);	
	void setItems(KWrap... items);
	void setActiveItem(KWrap item);	
	void setHidden(boolean hidden);
}