package facets.facet.kit;
import facets.core.superficial.Notifiable;
import facets.core.superficial.SFacet;
import facets.core.superficial.STarget;
import facets.util.Titled;
/*
A {@link Facet} that can create, monitor and update widgets exposing
its target to view and control in the surface. 
 */
public interface KitFacet extends SFacet,Titled{
  /*
Convenience method for <code>items()[0]</code> in the usual case
where only a single widget need be returned. 
   */
  KWrap base();
  /*
Return all widgets needed to represent the facet. 
<p>Used directly only for a block of menu items controlled by a single facet.</p> 
   */
  KWrap[]items();
	void targetNotify(Object msg, boolean interim);
	/**
	The current target in the application.
	<p>This will be the last {@link STarget} set with <code>retarget</code>. 
	   */
	STarget target();
}
