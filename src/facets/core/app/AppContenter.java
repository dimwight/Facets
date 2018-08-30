package facets.core.app;
import facets.core.app.FeatureHost.LayoutFeatures;
import facets.core.superficial.app.SHost;
import facets.util.TypesKey;
/**
{@link SContenter} for use in an {@link AppSurface}. 
<p>{@link AppContenter} defines the extended contract that must be
implemented by any {@link SContenter} supplying content for
an {@link AppSurface}. 
 */
public interface AppContenter extends SContenter{
	/** 
	@param active
	@return always <code>false</code>!
	 */
	boolean useActiveFeatures(SContentAreaTargeter active);
	/**
	Return a layout of facets that expose the content. 
	 * @param area was created by a content root returned by 
	{@link SContenter#newContentArea(boolean)}
	 */
	LayoutFeatures newContentFeatures(SContentAreaTargeter area);
	/**
	Allows the contenter to align two target trees, for instance when 
	'cloning' a window. 
	@param existing containing values to copy
	@param added new root to be aligned with existing
	 */
	void alignContentAreas(SAreaTarget existing,SAreaTarget added);
	/**
	Has the content changed? 
	 */
	boolean hasChanged();
	/**
	Called when the content has been added to the surface.  
	 */
	void wasAdded();
	/**
	Called when one or more content roots have been removed from the surface.  
	 */
	void wasRemoved();
	TypesKey featuresKey(SContentAreaTargeter use);
}