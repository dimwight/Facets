package facets.core.superficial;
import facets.util.Titled;
/** 
Superficial targeter. 
*<p>A {@link STargeter} mediates between a target, its exposing facet(s) and 
the surface by extending three significant interfaces: 
 <ul> 
 <li>{@link SRetargetable} enables to to have a target.
 <li>{@link Notifying} enables it to form part of the notification tree.
 <li>{@link Facetable} enables it to attach facet which it 
 applies with its own target; note that some {@link STargeter}s have
 no facet, functioning only as organising elements of the targeter tree.
</ul> 
 
 */
public interface STargeter extends SRetargetable,Notifying,Facetable{
  /**
	Sets the <code>target</code> of the {@link STargeter} 
and those of any <code>elements</code> or other {@link STargeter} members. 
<p>Also sets the {@link STargeter} as notification monitor of 
its {@link STargeter} members and of its target.
@see facets.core.superficial.Notifying
   */
  void retarget(STarget target,Impact impact);
	/**
	Dynamically-defined children.   
<p>Return any children created dynamically during initial retargeting to
correspond with the child <code>elements</code> of <code>target</code>.    
	 */
	STargeter[]elements();
  /**Adds <code>facet</code> to a list of facets to be updated during <code>retargetFacets</code>. 
   */
  void attachFacet(SFacet facet);
  /**Updates all facets attached with {@link #attachFacet(SFacet)} and those of any child
{@link #elements()}. 
<p>Application code need never call this method directly; it is called 
on the surface root targeter after retargeting following notification,
 and then recursively through the targeter tree. 
   */
  void retargetFacets(Impact impact);
	/**
	The current target in the application.
	<p>This will be the last {@link STarget} set with <code>retarget</code>. 
	   */
	STarget target();
	Class targetType();
}
