package facets.core.superficial.app;
/**
Typical surface styles. 
 */
public enum SurfaceStyle{
	/**
	Desktop app with toolbar above content area, status bar below. 
	 */
	DESKTOP,
	/**
	Applet with tool panel below or beside content area. 
	 */
	APPLET,
	/**
	Applet with panel shade to match containing page etc. 
	<p>Default page shade is assumed to be white. 
	 */
	BROWSER
}