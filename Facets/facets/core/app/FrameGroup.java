package facets.core.app;
import facets.core.superficial.SFrameTarget;
import facets.core.superficial.STarget;
import facets.core.superficial.STargeter;
import facets.core.superficial.TargetCore;
import facets.core.superficial.TargeterCore;
import facets.util.ItemList;
import facets.util.Util;
/**
{@link STarget} that enables multiple selection. 
<p>{@link FrameGroup} can bundle together 
multiply-selected frames whose {@link STarget#elements()}...
 */
final public class FrameGroup extends SFrameTarget{
  private static final class FrameTargeter extends TargeterCore{
		FrameTargeter(Class type){
			super(type);
		}
	}
  final public class Proxy extends TargetCore{
    private Proxy(STarget[]elements){
      super(elements[0].title());
      STarget[]children=elements[0].elements();
      setElements(children.length==0?elements:newElementProxies(elements));
    }
  }
	private final SFrameTarget editable,frames[];
  /**
  Unique constructor. 
  @param editable title passed to superclass, prefixed with "Group for ";
  stored for notification by facet
  @param frames to be grouped
   */
  public FrameGroup(SFrameTarget editable,SFrameTarget[]frames){
    super("Group for "+editable.title(),frames[0]);
    this.editable=editable;
    STarget[]proxies=newElementProxies(this.frames=frames);
    setElements(proxies);
  }
  /**
	 Re-implementation to return a private {@link facets.core.app.FrameGroup.FrameTargeter}. 
	 */
	public STargeter newTargeter() {
		return true?new FrameTargeter((Class)framed)
			:new TargeterCore((Class)framed);
	}
  public void facetNotified(boolean interim){
		Object[]framed=new Object[frames.length];
		for(int i=0;i<framed.length;i++)framed[i]=frames[i].framed;
		editable.setFramedState(framed,interim);		
	}
	private STarget[]newElementProxies(STarget[]sources){
    final ItemList<STarget[]> store=new ItemList(STarget[].class);
    for(int g=0;g<sources.length;g++){
      STarget[]elements=sources[g].elements();
      for(int i=0;i<elements.length;i++){
        if(store.size()<i+1)store.addItem(new STarget[sources.length]);
       store.items()[i][g]=elements[i];
      }
    }
    STarget[][]stored=store.items();
    STarget[]proxies=new STarget[stored.length];
    for(int i=0;i<proxies.length;i++)proxies[i]=new Proxy(stored[i]);
    return proxies;
  }
}