package facets.core.superficial;
import static facets.util.app.Events.*;
import facets.core.app.AreaTargeter;
import facets.core.app.SContentAreaTargeter;
import facets.util.Debug;
import facets.util.ItemList;
import facets.util.ItemList.TreeItems;
import facets.util.Util;
import facets.util.tree.TypedNode;
import java.util.ArrayList;
import java.util.List;
/**
Implements {@link STargeter}. 
<p>{@link TargeterCore} is a public implementation of {@link STargeter} 
  to provide for extension in other packages; instances are generally 
  created by an implementation of {@link facets.core.superficial.TargetCore#newTargeter()}. 
*/
public class TargeterCore extends NotifyingCore implements STargeter{
	public static List<STargeter>targeters=new ArrayList(){
		public boolean add(Object e){
			boolean live=false;
			if(live)Util.printOut("TargeterCore.targeters.add: e=",e);
			return live?super.add(e):false;
		};
	};
	private transient STargeter[]elements;
	private transient ItemList<SFacet>facets=new ItemList(SFacet.class);
	private transient STarget target;  
  private final Class targetType;
  /**
	Construct a {@link TargeterCore} to match <code>target</code>. 
	@param targetType set as {@link #targetType}.
	   */
	public TargeterCore(Class targetType){
		this.targetType=targetType;;
	  targeters.add(this);
	  if(trace)traceEvent("Created " +//"targeter " +targeters+" "+
				this);
	}
	final public String toString(){
		String targetInfo=target==null?"":Debug.info(target);
		return Debug.info(this)+(true?"":" ["+targetInfo+"]");
	}
	public void retargetFacets(Impact impact){
		SFacet[]facets=this.facets.items();
		for(int i=0;i<elements.length;i++)elements[i].retargetFacets(impact);
		if(facets==null)return;
	  for(int i=0;i<facets.length;i++){
			facets[i].retarget(target,impact);
			if(trace)traceEvent("Retargeted facet " +Debug.info(facets[i])+" in "+this);
		}
	}
	final public void attachFacet(SFacet facet){
		if(facet==null)throw new IllegalArgumentException("Null facet in "+Debug.info(this));
		if(!facets.contains(facet))facets.addItem(facet);
		if(trace)traceEvent("Attached facet "+Debug.info(facet)+" to "+Debug.info(this));
	}
	public void retarget(STarget target,Impact impact){
    if(target==null)throw new IllegalArgumentException(
    		"Null target in "+Debug.info(this));
    if(trace)traceEvent("Retargeting "+Debug.info(this)+" on "+Debug.info(target));
    this.target=target;
		STarget[]targets=target.elements();
		if(targets==null)throw new IllegalStateException("No targets in "+Debug.info(this));
		if(elements==null){
      elements=new STargeter[targets.length];
  		for(int i=0;i<elements.length;i++){
  			elements[i]=((TargetCore)targets[i]).newTargeter();
  			elements[i].setNotifiable(this);
  		}
    }
		if(targets.length==elements.length)
			for(int i=0;i<elements.length;i++)elements[i].retarget(targets[i],impact);			
		if(((TargetCore)target).notifiesTargeter())target.setNotifiable(this);
  }  
  final public STargeter[]elements(){
	  if(elements==null)throw new IllegalStateException("No elements in "+Debug.info(this));
	  return elements;
	}
	final public STarget target(){
	  if(target==null)throw new IllegalStateException("No target in "+Debug.info(this));
	  return target;
	}
	final public String title(){
		return target==null?"Untargeted":target.title();
	}
	protected TypedNode[]newDebugChildren(){
    ItemList<TypedNode>items=new ItemList(TypedNode.class);
    items.addItems(super.newDebugChildren());
    items.addItem(newDebugSourcesNode("target",target));
    if(elements.length!=0)
      items.addItem(newDebugSourcesNode("elements",(Object[])elements));
    if(facets.items().length>0)
      items.addItem(newDebugSourcesNode("[facets]",(Object[])facets.items()));
    return items.items();
  }
	public static STargeter[]treeItems(STargeter root){
		STargeter[]tree=new TreeItems<STargeter>(STargeter.class,root){
			@Override
			protected STargeter[]getChildren(STargeter parent){
				ItemList<STargeter>children=new ItemList(STargeter.class);
				children.addItems(parent.elements());
				if(parent instanceof AreaTargeter){
					children.add(((AreaTargeter)parent).indexedTargeter());
					if(parent instanceof SContentAreaTargeter){
						SContentAreaTargeter root=(SContentAreaTargeter)parent;
						children.addItems(new STargeter[]{
								root.indexedTargeter(),
								root.view(),
								root.viewer(),
								root.content(),
								root.selection()
						});
					}
				}
				return children.items();
			}
		}.items();
		return tree;
	}
	public static STargeter newRetargeted(TargetCore target,boolean live){
		target.setLive(live);
		STargeter t=target.newTargeter();
		t.retarget(target,Notifying.Impact.DEFAULT);
		return t;
	}
	@Override
	final public Class targetType(){
		return targetType;
	}
}
