package facets.core.superficial.app;
import facets.core.superficial.SFrameTarget;
import facets.core.superficial.STarget;
import facets.util.Titled;
/**
Builds a targeter tree and facets that can expose its content. 
<p>{@link SContenter} encapsulates creation of a targeter tree that  
exposes its content to user view and control, defining  
methods that can be called by a surface during (re)building.
<ul>
	<li>{@link #newContentArea(boolean)} will return a content area root which 
		in turn creates the {@link facets.core.superficial.app.SContentAreaTargeter} 
		to be used by the surface for the content type. 
  <li>{@link #areaRetargeted(SContentAreaTargeter)} can be called after the initial 
  build and at each retargeting
</ul>
 */
public interface SContenter extends Titled{
	/**
	Construct a content area tree. 
	<p>For use by {@link SSurface}s to create content area trees for incorporation
	into their surface area trees.  
	@param faceted should the area tree have facets attached?
	@return the {@link SAreaTarget} root of an area tree containing either
	the content or viewers for the content; the root should in turn return with
	{@link SAreaTarget#newTargeter()} a {@link SContentAreaTargeter} for the content
	wrapped by the {@link SContenter}.
	 */
	SAreaTarget newContentArea(boolean faceted);
	/**
	Notify the contenter of retargeting. 
	<p>Should be called by the surface whenever its root {@link SContentAreaTargeter}
	has been retargeted on a content root returned by {@link #newContentArea(boolean)} 
	in this {@link SContenter}, with the following guarantees.
	<ul><li>Before the first invocation, all targeter tree elements exposing this content 
	will have been attached to <code>rootTargeter</code> - allows setting of
	references to other elements.
	<li>All attached facets will be retargeted after any invocation - allows
	setting of live states etc.
	</ul>
	@param area will be retargeted on a content root
	created by this {@link SContenter} type (not generally by this 
	instance). 
	 */
	void areaRetargeted(SContentAreaTargeter area);
	/**
	Frame for content exposed by this contenter. 
	 */
	SFrameTarget contentFrame();
	/**
	Return a suitable title for the content. 
	<p>This will generally be either the name of an external content source
	or a suitable dialog area title. 
	 */
	String title();
	Class targetType();
	/**
	Return elements for the content area. 
	<p>The default implementation returns an empty <code>Target[]</code> to
	meet the contract of <code>TargetCore.lazyElements</code>. 
	@param area the content area
	 */
	STarget[]lazyContentAreaElements(SAreaTarget area);
}
