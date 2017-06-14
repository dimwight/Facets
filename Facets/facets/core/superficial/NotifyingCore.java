package facets.core.superficial;
import static facets.util.app.Events.*;
import facets.core.superficial.Notifying.Impact;
import facets.util.Debug;
import facets.util.Identified;
import facets.util.Stateful;
import facets.util.Tracer;
import facets.util.Util;
import facets.util.app.Events;
import facets.util.tree.TypedNode;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
/**
Core implementation of key interfaces. 
<p>{@link NotifyingCore} is the shared superclass of both the {@link STarget} and
{@link STargeter} class hierarchies. 
<p>Declared <code>public</code> for documentation purposes only; client code should 
use the concrete subclass hierarchies. 
 */
abstract class NotifyingCore extends Tracer implements Notifying,Identified{
	private TypedNode newContentNode(String title,final Object...passed){
		return new TypedNode(Object.class,"Graph",title
				.substring(0,Math.min(title.length(),50)).replaceAll("\\s+"," ")){
			private Object[]contents=passed;
			public Object[]contents(){
				if(contents.length>0)return contents;
			  final TypedNode children[]=newDebugChildren();
			  for(int i=0;i<children.length;i++)children[i].setParent(this);
				return contents=children;
			}
			@Override
			public boolean stateEquals(Stateful s){
				return true;
			}
		};
	}
  final protected TypedNode newDebugSourcesNode(String title,final Object...sources){
	  final Object[]contents=new Object[sources.length];
	  for(int i=0;i<contents.length;i++){
			Object source=sources[i];
			String classTitle=Util.helpfulClassName(source);
			contents[i]=source instanceof NotifyingCore?
			  		((NotifyingCore)source).newGraphNode()
			  :source instanceof Integer||source instanceof Double
			  	||source instanceof Boolean||source instanceof String
			    		||source instanceof SFacet?
			    	newDebugLeafNode(classTitle,validTitleString(source))
			  :newDebugLeafNode(classTitle,Debug.info(source));
		}
	  TypedNode node=newContentNode(title+(title.startsWith("[")?"":true?"":"()"),
	  		contents);
    for(int i=0;i<contents.length;i++)
    	if(contents[i]instanceof TypedNode)((TypedNode)contents[i]).setParent(node);
	  return node;
	}
	private static String validTitleString(Object source){
		String text=source.toString().replaceAll("\\s+"," ").trim();
		return text.equals("")?"[Empty Title]":text;
	}
	final protected TypedNode newDebugLeafNode(String title,Object o){
	  return newContentNode("[" +title+"]",Debug.info(o));
	}
	/**
	Create a <code>TypedNode[]</code> representation of this {@link Notifying} 
	for debug purposes.  
	 */
	protected TypedNode[]newDebugChildren(){
	  return notifiable==null?new TypedNode[]{}:
	    new TypedNode[]{newDebugLeafNode("monitor",notifiable)};
	}
	/**
	Create root node of debug object graph. 
	 */
	final public TypedNode newGraphNode(){
	  return newContentNode(Debug.info(this),
	  		(Object[])(false?newDebugChildren():new TypedNode[]{}));
	}
	transient Notifiable notifiable;
	private static int identities;
	private final int identity=identities++;
	@Override
	public Impact impact(){
		return Impact.DEFAULT;
	}
	@Override
	public Object identity(){
		return identity;
	}
	@Override
  final public Notifiable notifiable(){
  	if(false&&notifiable==null)throw new IllegalStateException("No monitor in "+Debug.info(this));
  	return notifiable;
  }
	@Override
	public void notify(Notice notice){
		if(trace)traceEvent("Notified in "+this+" with "+notice);
		if(notifiable==null)return;
		if(!blockNotification())notifiable.notify(notice.addSource(this));
		else if(trace)traceEvent("Notification blocked in "+this);
	}
	@Override
  final public void notifyParent(Impact impact){
    if(notifiable==null)return;
    notifiable.notify(new Notice(this,impact));
  }
  /**
  Enables notification to be restricted to this member of the tree. 
  <p>Checked by {@link #notify(Notice)}; default returns <code>false</code>.
   */
  protected boolean blockNotification(){return false;}
	@Override
	public final void setNotifiable(Notifiable n){
  	if(false)traceDebug("NotifyingCore: ",this);
		this.notifiable=n;
	}
	@Override
  public String toString(){return Debug.info(this);}
}
