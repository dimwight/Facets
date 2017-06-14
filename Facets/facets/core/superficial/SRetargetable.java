package facets.core.superficial;
import facets.core.superficial.Notifying.Impact;
/**
Application element targetable on a {@link STarget}. 
<p>{@link SRetargetable} captures two distinct types of retargeting: 
<ul>
	<li>of a targeter by its parent in the targeter tree
	<li>of a facet by the targeter or target to which it is attached 
</ul>  
 */
public interface SRetargetable{
  /**
 Ses the target if changed, adjust to latest state.  
 <p>The last {@link STarget} set should be returned by <code>target</code>; 
 the {@link Notifying.Impact} allows for refining the retargeting response. 
 */
	void retarget(STarget target,Impact impact);
}
