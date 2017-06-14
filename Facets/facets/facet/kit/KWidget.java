package facets.facet.kit;
import facets.util.shade.Shade;

import java.util.Map;
public interface KWidget extends KWrap{
	void setEnabled(boolean enabled);
  void setIndeterminate(boolean on);
	Map<String,?>components();
}