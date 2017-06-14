package facets.core.superficial.app;
import facets.core.superficial.Notifiable;
import facets.core.superficial.SFacet;
import facets.core.superficial.STargeter;
import facets.core.superficial.app.SHost.FacetLayout;
import facets.util.Titled;
import java.awt.Rectangle;
/**
Exposes application content and logic via one or more {@link SContentAreaTargeter}s 
in an {@link AreaTargeter} tree. 
<p>A {@link SSurface} should build a surface by 
<ol>
	<li>creating a {@link STargeter} tree containing at least one 
	{@link SContentAreaTargeter}
	<li>attaching {@link SFacet}s to the tree 
	<li>passing a {@link FacetLayout} containing the root {@link SFacet}(s) for the tree
	to a {@link SHost} GUI context.
	<li>retargeting the tree and its attached {@link SFacet}s as required
</ol>
<p>This general contract can be implemented to build {@link SSurface}s for 
<ul>
	<li> simple applications or applets exposing a single content object
	<li> complex applications with multiple-content interfaces
	<li> top-level 'slave' application windows 
	<li> modal dialogs with single or multiple content pages
	<li> dialog-type surfaces embedded in application surfaces as eg sidebars
</ul>
 */
public interface SSurface extends Notifiable{
	/**
	{@link SSurface} that can open in a top-level application window. 
	 */
	public static interface WindowAppSurface extends SSurface{
		void openApp();
		/**
		Close the surface after any checks. 
		@return false if close not allowed
		 */
		boolean attemptClose();
		/**
		Is the {@link WindowAppSurface} slave to another?
		<p>(May affect default bounds.)  
		 */
		boolean isSlave();
	}
	/**
	{@link SSurface} that can be hosted in a dialog. 
	 */
	public interface DialogSurface extends SSurface{
		int AT_NOT_SET=Integer.MIN_VALUE;
		/**
		Allows a dialog to calculate/retrieve its bounds before launch.
		 */
		Rectangle getLaunchBounds();
		/**
		Allows the surface to tidy up upon dismissal of dialog.
		@param bounds latest of host dialog
		 */
		void dialogDismissed(Rectangle bounds);
		boolean isResizable();
	}
	/**
  Build a targeter tree and pass its attached facets to a 
  GUI host.
	 */
	void buildRetargeted();
	/**
	The root of the surface targeter tree. 
	@return the root of the targeter tree created in {@link #buildRetargeted()}
	 */
	AreaTargeter surfaceTargeter();
	/**
	Return the host.
	<p>In an application lazily create a suitable host if required; 
	otherwise return host passed during construction.
	 */
	SHost host();
	/**
	Allows a check that the surface has been built. 
	 */
	boolean isBuilt();
}