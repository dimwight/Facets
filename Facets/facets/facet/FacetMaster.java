package facets.facet;
import facets.core.app.MenuFacets;
import facets.core.app.ViewerContenter;
import facets.core.superficial.Notifying;
import facets.core.superficial.STarget;
import facets.core.superficial.STargeter;
import facets.core.superficial.TargetCore;
import facets.core.superficial.Notifying.Impact;
import facets.core.superficial.app.ViewerTarget;
import facets.util.Tracer;
/**
Integrates a custom widget assembly with the facet builder API. 
<p>{@link FacetMaster} allows custom widget assemblies 
	to be built and managed within an abstract facet builder API. 
<p>A facet master must</p>
<ul>
	<li>create a widget assembly on behalf of a facet for use in the GUI surface</li>
	<li>maintain the state of this assembly to match the facet target as 
		the facet is applied</li>
	<li> respond to input events on the assembly 
		by updating the current content or target and triggering a retargeting</li>
</ul>
<p>A master should be used by a facet builder
to extend by composition an appropriate generic facet; 
{@link FacetMaster} itself defines {@link #attachedToFacet()}
to be called by the facet during construction. 
<p>{@link FacetMaster} is extended for viewer and simple facet by its internal
abstract subclasses;  
methods to create and reference the custom assembly can be
 defined in toolkit-specific subclasses.
 */
public abstract class FacetMaster extends Tracer{
	/**
	Builds and manages avatar pane for a viewer facet. 
	 */
	public static abstract class Viewer extends FacetMaster{
		/**
		Update the avatar pane to match viewer content. 
		<p>Called during each retargeting. 
		 */
		public abstract void refreshAvatars(Impact impact);
		/**
		The {@link ViewerTarget} representing the facet in the area target tree. 
		@return the {@link ViewerTarget} to which the facet is attached. 
		 */
		public abstract ViewerTarget viewerTarget();
		/**
		Should the viewer facet scroll the avatar pane? 
		 */
		public abstract boolean isScrollable();
	}
	/**
	Builds and manages widget assembly for a simple facet. 
	 */
	public static abstract class Simple extends FacetMaster{
		/**
		The current facet target. 
		 */
		public abstract STarget target();
		/**
		Update GUI components to the state of the target passed. 
		<p>Called during each retargeting with a single target (ultimately by 
		{@link STargeter#retargetFacets(Notifying.Impact)}, and also 
		during facet construction immediately after assembly creation. 
		 */
		public abstract void retargetedSingle(STarget target,Notifying.Impact impact);
		/**
		Update GUI components to match the states of the targets passed. 
		<p>Called during each retargeting with multiple targets (ultimately by 
		{@link STargeter#retargetFacets(Notifying.Impact)}. 
		 */
		public abstract void retargetedMultiple(STarget[]targets, Notifying.Impact impact);
		public void respondTargetWantsFocus(){}
	}
	/**
	Called when a facet is constructed from the instance. 
	<p>Can be used to perform facet-dependent construction:
	up to this call, methods depending on the facet will fail.
		<p>Default implementation is empty. 
	 */
	public void attachedToFacet(){}
}
