package facets.core.app;
import facets.core.app.Dialogs.Response;
import facets.core.superficial.Notifying;
import facets.core.superficial.SFacet;
import facets.core.superficial.STargeter;
import java.awt.Dimension;
/**
{@link SContenter} for use in a {@link PagedSurface}. 
<p>{@link PagedContenter} defines an extended contract that must be
implemented by any {@link SContenter} supplying content for
a {@link PagedSurface}. 
 */
public interface PagedContenter extends SContenter{
	/**
	Return a panel containing facets exposing the content. 
	<p>Called from {@link STargeter#retargetFacets(Notifying.Impact)} in the (special)
	{@link SContentAreaTargeter} created by {@link PagedContentArea}
	@param t heads the targeter tree for the content
	 */
	SFacet newContentPanel(SContentAreaTargeter t);
	/**
	Enables the contenter to interact with its host dialog. 
	<p>Can be implemented empty where no interaction is required. 
	 */
	void setSurface(PagedSurface surface);
	/**
	Return the panel size needed for the content surface elements. 
	 */
	Dimension contentAreaSize();
	/**
	Return a title for the containing page. 
	 */
	String title();
	/**
	Change content state to match surface state. 
	<p>This will generally mean setting content state to that of a buffer
	storing surface state. 
	 */
	void applyChanges();
	/**
	Restore content to its original state. 
	 */
	void reverseChanges();
	/**
	Called once the host is hidden with {@link PagedSurface#hideHost(Response)}. 
	 */
	void hostHidden();
}