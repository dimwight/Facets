package facets.facet.kit;
import facets.core.superficial.STarget;
import facets.core.superficial.Notifying.Impact;
import facets.core.superficial.STarget.Targeted;
import facets.util.Titled;
public interface KViewer extends KWrap,Targeted,Titled{
	void refresh(Impact impact);
	void setTools(KWrap tools);
	void setPaneControl(KWrap base);
}