package facets.core.app;
import facets.core.app.PagedContentArea.PagedContentAreaTargeter;
import facets.core.superficial.Notifiable;
import facets.core.superficial.Notifying;
import facets.core.superficial.STargeter;
import facets.core.superficial.app.IndexingTargeter;
import facets.util.Debug;
/**
Type of all targeters returned by {@link SAreaTarget}s. 
<p>{@link AreaTargeter} extends its superclass only in its behaviour when
retargeting facets.    
 */
public class AreaTargeter extends IndexingTargeter{
	/**
	Convenience constant denoting level in the {@link AreaTargeter} tree.  
	 */
	final public static int AREA_APP=0,AREA_CONTENT=1,AREA_ACTIVE=AreaTargeter.AREA_LOWEST-1,
		AREA_LOWEST=Integer.MAX_VALUE;
	/**
  Constructs an {@link AreaTargeter} to be retargeted on an 
  {@link SAreaTarget}.     
     */
  public AreaTargeter(Class type){
  	super(type);
  	Class c1=getClass();
  	boolean ok=false;
  	for(Class c2:new Class[]{AreaTargeter.class,
  			SContentAreaTargeter.class,PagedContentAreaTargeter.class})
  		ok|=c2==c1;
  	if(!ok)throw new RuntimeException("Not subclassable in "+Debug.info(this));
  	
  }
  /**
	Extends superclass behaviour. 
	<p>Also calls {@link SAreaTarget#retargetFacets(Notifying.Impact)} in its target. 
	 */
	public void retargetFacets(Impact impact){
	  SAreaTarget areaTarget=(SAreaTarget)target();
	  if(areaTarget.parent()==null)areaTarget.retargetFacets(impact);
	  super.retargetFacets(impact);
	}
	/**
  Returns the {@link AreaTargeter} at the specified depth. 
  @param depth from the root
   */
  public final AreaTargeter areaAt(int depth){
	  AreaTargeter area=this,active=null;
	  Notifiable n;
		while((n=area.notifiable())instanceof AreaTargeter)area=(AreaTargeter)n;
		boolean debug=false&&depth==AREA_ACTIVE;
	  for(int at=0;at<depth;at++){
	  	if(false&&debug)traceDebug(".area: at="+at+" ",area);
	  	if(depth==AREA_ACTIVE&&area instanceof SContentAreaTargeter)active=area;
	  	STargeter below=area.indexedTargeter();
	  	if(below instanceof AreaTargeter)area=(AreaTargeter)below;
	  	else if(depth==AREA_LOWEST)break;
	  	else{
	  		if(active!=null)break;
	  		else throw new IllegalStateException("Bad area depth " +depth+
		  			" in "+Debug.info(this));
	  	}
	  }
	  if(active!=null)area=active;
	  if(debug)traceDebug(".~area: ",area);
	  return area;
	}
	/**
	Convenience method. 
	@return {@link #target()} cast to an {@link SAreaTarget}
	 */
	public SAreaTarget areaTarget(){
		return (SAreaTarget)target();
	}
}