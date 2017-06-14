package facets.core.superficial.app;
import facets.util.Debug;
import facets.util.Titled;
/**
Defines display policy for a viewer facet. 
<p>The core interface defines only very general properties.
 */
public interface SView extends Titled{
	/**
	Can more than one object be selected at a time in the view? 
	 */
	boolean allowMultipleSelection();
	/**
	Does the view allow user interaction/editing? 
	 */
	boolean isLive();
}
