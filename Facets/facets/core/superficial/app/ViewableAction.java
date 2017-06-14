package facets.core.superficial.app;
/**
Action on viewable content. 
<p>A {@link ViewableAction} denotes a non-gesture defined action on 
viewable content; typical actions could be
<ul>
<li>copying, cutting, pasting, deleting
<li>launching a renaming interaction
<li>selecting by rule eg select/deselect all 
</ul>
<p>The {@link Object#toString()} implementation should return a suitable title for 
the action. 
 */
public interface ViewableAction{
	ViewableAction[]NO_ACTIONS=new ViewableAction[]{};
}