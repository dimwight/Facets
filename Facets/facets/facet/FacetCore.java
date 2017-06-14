package facets.facet;
import static facets.util.app.Events.*;
import facets.core.superficial.Notifying;
import facets.core.superficial.STarget;
import facets.core.superficial.Notifying.Impact;
import facets.core.superficial.STarget.Targeted;
import facets.facet.kit.*;
import facets.util.Debug;
import facets.util.ItemList;
import facets.util.Objects;
import facets.util.StringFlags;
import facets.util.Tracer;
import facets.util.app.Events;
import java.util.HashSet;
import java.util.Set;
abstract class FacetCore extends Tracer implements KitFacet,STarget.Targeted{
	final Toolkit kit;
  protected STarget target;
	private KWrap[]parts;
  private Set<KWrap>registered=new HashSet();
	@Override
  public void dispose(){};
  FacetCore(STarget target,Toolkit kit){
  	this.kit=kit;
  	FacetFactory.facets++;
		if((this.target=target)==null)
			throw new IllegalArgumentException("Null target in "+Debug.info(this));
	}
  final public KWrap base(){return parts()[0];}
  final public KWrap[]items(){return parts();}
  public void targetNotify(Object msg,boolean interim){
		if(msg==null)throw new IllegalArgumentException("Null msg in "+Debug.info(this));
		String targetInfo=Debug.id(target)+" "+target.title();
    if(trace)traceEvent("Notified interim=" +interim+
    		" by " +Debug.info(msg)+
    		" in " +Debug.info(this)+" retargeted on "+targetInfo);
  }
  public void retarget(STarget target,Impact impact){
		if((this.target=target)==null)
			throw new IllegalArgumentException("Null target in "+Debug.info(this));
		else setEnablesToTarget();
	}
  public final STarget target(){
    if(target==null)throw new IllegalStateException("No target in "+Debug.info(this));
    return target;
  }
	public String title(){
		return target.title();
	}
	public String toString(){
		return Debug.info(this)+" target="+target;
	}
  final protected void setEnables(boolean live){
		if(registered.isEmpty())return;
		else for(KWrap item:registered)
			if(item instanceof KWidget&&item.facet()!=FacetFactory.BREAK)
				((KWidget)item).setEnabled(live&&item.facet().target().isLive());
	}
  protected void setEnablesToTarget(){setEnables(target.isLive());}
  protected void deleteParts(){
		parts=null;
		registered=new HashSet();
  }
  abstract KWrap lazyBase();
  abstract KWrap[]lazyParts();
  KWrap newRegisteredLabel(String title,StringFlags hints){
    KWrap label=kit.label(this,title,hints);
    registerPart(label);
    return label;
  }
  final KWrap[]parts(){
		if(parts!=null)return parts;
		KWrap base=lazyBase();
		parts=base!=null?new KWrap[]{base}:lazyParts();
		if(parts==null)throw new IllegalStateException("Null parts in "+Debug.info(this));
		for(int i=0;i<parts.length;i++)registerPart(parts[i]);
		setEnablesToTarget();
		if(false)trace(".parts: ",parts);
		return parts;		
  }
	final void registerPart(KWrap part){
		registered.add(part);
	}
	final void registerParts(FacetCore facet){
		for(KWrap part:facet.parts())registerPart(part);
  }
	public STarget[]targets(){
		KWrap base=base();
		return base instanceof Targeted?((Targeted)base).targets():new STarget[]{};
	}
	public KWrap[]treeWidgets(){
		return new KWrap[]{};
	}
}

