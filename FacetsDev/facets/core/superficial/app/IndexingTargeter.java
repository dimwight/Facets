package facets.core.superficial.app;
import static facets.util.app.Events.*;
import facets.core.superficial.Notifying;
import facets.core.superficial.STarget;
import facets.core.superficial.STargeter;
import facets.core.superficial.TargetCore;
import facets.core.superficial.TargeterCore;
import facets.core.superficial.Notifying.Impact;
import facets.util.Debug;
import facets.util.ItemList;
import facets.util.Util;
import facets.util.app.Events;
import facets.util.tree.TypedNode;
import java.util.HashMap;
import java.util.Map;
/**
FrameTargeter for an {@link IndexingTarget}.   
<p>{@link IndexingTargeter} defines much important behaviour used by 
	{@link facets.core.app.AreaTargeter}. In particular, it ensures 
	that at each retargeting an appropriate targeter is either created or 
	(more importantly) retrieved for retargeting to its target's indexed child; 
	the criterion used being the {@link Class} of the targeter returned by the 
	child. 
<p>Construction is package-private; this class is defined <code>public</code> 
for documentation purposes only. 
 */
public class IndexingTargeter extends TargeterCore{
  final private Map<Class,STargeter>targeters=new HashMap();
  final public STargeter targeter(Class targetType){
  	if(false)trace(".targeter: targeters=",targeters.keySet());
		return targeters.get(targetType);
	}
	private STargeter indexedTargeter,indexing;
	protected IndexingTargeter(Class targetType){
		super(targetType);
	}
  /**
  Retargeted to {@link IndexingTarget#indexedTarget()} in the current target.  
   */
  public final STargeter indexedTargeter(){
    if(indexedTargeter==null)throw new IllegalStateException("No childTargeter in "+Debug.info(this));
    return indexedTargeter;
  }
  /**
  Retargeted to {@link IndexingTarget#indexing()} in the current target. 
   */
  public final STargeter indexing(){
    if(indexing==null)throw new IllegalStateException(
    		"No indexingLink in "+Debug.info(this));
    return indexing;
  }
  /**
  Overrides superclass method. 
  <p>Creates and applies a targeter tree on the target tree headed
  by its {@link IndexingTarget} target. 
   */
  public void retarget(STarget target,Impact impact){
    super.retarget(target,impact);
    IndexingTarget it=(IndexingTarget)target;
    STarget ix=it.indexing(),
    	targets[]=it.indexableTargets(),
			indexed=it.indexedTarget();
    if(indexing==null)indexing=((TargetCore)ix).newTargeter();
    indexing.retarget(ix,impact);
    for(STarget t:targets)
      if(t!=indexed&&t instanceof IndexingTarget)
        retargetedTargeter(t,impact);
    indexedTargeter=retargetedTargeter(indexed,impact);
    it.indexing().setNotifiable(it);
  }
  /**
  Overrides superclass method. 
  <p>Also calls {@link #retargetFacets(Notifying.Impact)} in {@link #indexing()}
  and {@link #indexedTargeter}. 
   */
  public void retargetFacets(Impact impact){
    super.retargetFacets(impact);
    STargeter indexed=indexedTargeter();
		indexed.retargetFacets(impact);
    indexing().retargetFacets(impact);
    if(true)for(STargeter t:targeters.values())if(t!=indexed)t.retargetFacets(impact);
  }
  /**
	Returns a targeter retargeted on the target passed. 
	<p>The key method in managing variation in target type.  
	@param target for targeter
   * @param impact 
	@return a targeter whose target type matches that of 
	{@link TargetCore#newTargeter()} in <code>target</code>; this will generally 
	have been stored during a previous retargeting. 
	 */
	protected final STargeter retargetedTargeter(STarget target,Impact impact){
		boolean debug=false||(Events.trace);
	  STargeter checkTargeter=((TargetCore)target).newTargeter();
	  TargeterCore.targeters.remove(checkTargeter);
	  Class targetType=checkTargeter.targetType();
	  STargeter targeter=targeters.get(targetType);
	  if(targeter==null){
	  	targeters.put(targetType,targeter=checkTargeter);
	  	TargeterCore.targeters.add(targeter);
	    if(trace)traceEvent("Storing targeter for "+targetType+" in "+Debug.info(this));
	  }
	  else if(trace)Events.traceEvent("Retrieved targeter for "+targetType+" in "+Debug.info(this));
	  targeter.setNotifiable(null);
	  if(false)traceDebug(".retargetedTargeter: ",target);
	  targeter.retarget(target,impact);
	  targeter.setNotifiable(this);
	  return targeter;
	}
	protected TypedNode[]newDebugChildren(){
    ItemList<TypedNode>items=new ItemList(TypedNode.class);
    items.addItems(super.newDebugChildren());
    items.addItem(newDebugSourcesNode("[targeters]",targeters.values().toArray()));
    if(indexedTargeter!=null)
      items.addItem(newDebugSourcesNode("indexedTargeter",indexedTargeter));
    if(indexing!=null)
      items.addItem(newDebugSourcesNode("indexingLink",indexing));
    return items.items();
  }
}
