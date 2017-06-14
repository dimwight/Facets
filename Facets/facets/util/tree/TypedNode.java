package facets.util.tree;
import facets.util.Debug;
import facets.util.Identified;
import facets.util.Objects;
import facets.util.Stateful;
import facets.util.StatefulCore;
import facets.util.Util;
import java.util.ArrayList;
import java.util.List;
/**
Defines simple tree node. 
<p>A {@link TypedNode} has 
<ul>
<li>{@link #contents()} comprising any combination of {@link TypedNode} {@link #children()} 
and {@link #values()} of the parameter type.
<li>A {@link #type()} which corresponds to an XML element <code>name</code> and a
	{@link #title()} defining what is in effect a primary attribute. 
</ul>
<p>{@link TypedNode} is declared <code>abstract</code> as several key methods 
are implemented as convenience stubs, some invalid. 
*/
public abstract class TypedNode<V>extends StatefulCore implements Identified{
	final private static long serialVersionUID=-6701419803219131053L;
	final public static TypedNode NULL_NODE=new TypedNode(Object.class,"Null","Empty"){};
	/** For use when no other value is appropriate for return by {@link #title()}. */
	public static final String UNTITLED="...";
	private static int nodes;
	private final int id=nodes++;
	private final Class valueType;
	private String type;
	private TypedNode parent;
	private transient boolean parentSet;
	/**
	Unique constructor. 
	@param valueType enables return from {@link #values()} matching the type parameter; 
		only implemented for {@link Object} and {@link String}; 
		cannot be {@link TypedNode} itself
	@param type passed to {@link #setValidType(String)}
	for return by {@link #type()} 
	@param title passed to {@link StatefulCore}  
	 */
	public TypedNode(Class valueType,String type,String title){
		super(title);
		if(valueType!=Object.class&&valueType!=String.class
				&&valueType!=byte[].class)throw new IllegalArgumentException(
				"Not implemented for " +valueType+" in "+Debug.info(this));
		else if(TypedNode.class.getClass().isAssignableFrom((this.valueType=valueType)))
				throw new IllegalArgumentException("Values may not descend from "+TypedNode.class);
		else setValidType(type);
	}
	@Override
	public String toString(){
		TypedNode[]children=children();
		String title=title();
		return (Debug.natureDebug?(Debug.id(this)+" "):"")+type+" "+(title==UNTITLED?"":title)+
			(this instanceof ValueNode?"":" children="
					+(false?Util.arrayPrintString(children):""+children.length));
	}
	/**
	Validates and sets the string to be returned by {@link #type()}. 
	@param type must be usable as XML <code>name</code>
	 */
	final public void setValidType(String type){
		if(type==null)throw new IllegalArgumentException(
				"Null type in "+Debug.info(this));
		else type=type.trim();
		if(!type.matches("[:\\-\\w]+"))throw new IllegalArgumentException(
				"Illegal type '" +type+"' in "+Debug.info(this));
		else this.type=type;
	}
	/**
	 String suggestive of nature of content tree.
	 @return string passed successfully to {@link #setValidType(String)}
	 */
	final public String type(){
		return type;
	}
	/**
	The node's {@link TypedNode} contents.  
	<p>Complains at <code>null</code> members.
	 */
	final public TypedNode[]children(){
		Object[]contents=nullChecked(contents());
	  List<TypedNode>children=new ArrayList(contents.length);
	  for(Object c:contents)
	    if(c instanceof TypedNode)children.add((TypedNode)c);
	  return children.toArray(new TypedNode[]{});
	}
  /**
	The node's non-{@link TypedNode} contents.  
	@return an array as specified by the type parameter and type passed to 
	the constructor; complains at <code>null</code> members
	 */
	final public V[]values(){
		Object[]contents=nullChecked(contents());
	  List<V>values=new ArrayList(contents.length);
	  List<TypedNode>children=new ArrayList(contents.length);
	  for(Object c:contents)
	    if(!(c instanceof TypedNode))values.add((V)c);
	  return(V[])Objects.newTyped(valueType,values.toArray());
	}
	private Object[]nullChecked(Object[]contents){
		if(contents==null)throw new IllegalStateException(
				"Null contents in "+Debug.info(this));
		else for(int i=0;i<contents.length;i++)
	    if(contents[i]==null)throw new IllegalStateException(
	    		"Null content at " +i+" in "+Debug.info(this));
		return contents;
	}
	/**
	Return the node's children and values.
	<p>Called (via checks for <code>null</code>) by {@link #children()}
	and {@link #values()};
	returned array must not be <code>null</code> nor contain any
	<code>null</code> members; order may be defined in subclasses.
	@return by default an empty array.
	 */
	public Object[]contents(){
		return new Object[]{};
	}
	/**
	Returns the node's parent.
	 <p>If none set, returns <code>null</code>.
	 */
	public TypedNode parent(){
		if(parent==null&&parentSet)throw new IllegalStateException(
				"Lost set parent in "+Debug.info(this));
		return parent;
	}
	/**
	Set the node to be returned by {@link #parent()}. 
	@param parent may be <code>null</code> (signifying instance is root of its tree)
	 */
	public void setParent(TypedNode parent){
		this.parent=parent;
		parentSet=parent!=null;
	}
	/**
	Invalid stub. 
	 */
	@Override
	public Stateful copyState(){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
  /**
	Invalid stub. 
	 */
	@Override
	public void setState(Object src){
		throw new RuntimeException("Not implemented in "+Debug.info(this));		
	}
	@Override
	public Object identity(){
		return id;
	}
	public void setContents(Object[]contents){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	public void setChildren(TypedNode...children){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	public void setValues(Object[]values){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
}
