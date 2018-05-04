package facets.util.tree;
import static facets.util.tree.Nodes.*;
import facets.util.Debug;
import facets.util.IndexingIterator;
import facets.util.Objects;
import facets.util.Stateful;
import facets.util.Strings;
import facets.util.Util;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
/**
Mutable, persistable {@link TypedNode}. 
<p>{@link DataNode} extends its superclass by
<ul>
<li>defining methods to set {@link #contents()}, 
{@link #children()} and {@link #values()}
 <li>providing a valid implementation of {@link #contents()}
 and of methods from {@link Stateful}
 <li>parameterising {@link TypedNode#values()} with {@link String}, so allowing 
 {@link DataNode} trees to be persisted textually. 
</ul>
*/
public class DataNode extends TypedNode<String>{
	private Object[]contents={};
	public DataNode(String type,String title){
		super(String.class,type,title);
	}
	public DataNode(String type,String title,Object[]contents){
		this(type,title);
		setContents(contents);
	}
	public String toString(){
		return super.toString()+(true?"":(" string="+Util.sf(Nodes.treeString(this).length())));
	}
	/**
	Replaces any existing contents.
	<p><code>contents</code> must conform to the contract for {@link #contents()}. 
			 */
	public void setContents(Object[]contents){
		if(contents==null)throw new IllegalArgumentException(
				"Null contents in "+Debug.info(this));
		final List values=false?null:new ArrayList();
		if(values==null)for(int i=0;i<contents.length;i++){
			Object item=contents[i];
			if(item==null)throw new IllegalArgumentException(
					"Null content at "+i+" in "+Debug.info(this));
			else if(item instanceof TypedNode)((TypedNode)item).setParent(DataNode.this);
			else item=valueString(item);
			contents[i]=item;
		}
		else new IndexingIterator(contents){
			@Override
			protected void itemIterated(Object item,int i){
				if(item==null)throw new IllegalArgumentException(
						"Null content at "+i+" in "+Debug.info(this));
				else if(item instanceof TypedNode)
					((TypedNode)item).setParent(DataNode.this);
				else item=valueString(item);
				values.add(item);
			}
		}.iterate();
		this.contents=values==null?contents:values.toArray();
	}
	/**
	Removes any existing children, appends those passed to any  
	non-{@link TypedNode} contents.
	<p><code>children</code> must conform to the contract for {@link #children()}. 
			 */
	final public void setChildren(TypedNode...children){
		setContents(Objects.join(Object.class,values(),children));
	}
	/**
	Removes any existing values, prepending those passed to any  
	{@link TypedNode} contents. 
	@param values must  
	<ul>
		<li>contain only members stringifiable by {@link #valueString(Object)}
		<li>contain no strings with structural whitespace
		<li>otherwise conform to the contract for {@link #values()}
	</ul>
	<p>String values may be key pairs in the format <i>key=value</i> where
	<i>value</i> is stringifiable by the implementation
		 */
	final public void setValues(Object[]values){
		if(values==null)throw new IllegalArgumentException("Null values in "+Debug.info(this));
		String[]strings;
		if(values.length<2)strings=values.length==0?new String[]{}
			:new String[]{valueString(values[0])};
		else{
			final List<String>dst=new ArrayList();
			new IndexingIterator<Object>(values){
				@Override
				protected void itemIterated(Object value,int at){
					if(value instanceof TypedNode)
						throw new IllegalArgumentException("Can't add node as value "+Debug.info(value));
					else if(value instanceof String){
						String text=(String)value;
						if(text.indexOf('\n')<0&&text.indexOf('\r')<0
								&&text.indexOf('\t')<0)dst.add(text);
						else throw new IllegalArgumentException("Structural whitespace in:\n "+text);
					}
					else dst.add(DataNode.this.valueString(value));
				}
			}.iterate();
			strings=dst.toArray(new String[]{});
		}
		setContents(Objects.join(Object.class,strings,children()));
	}
	/**
	Stringifies values to comply with {@link #values()}. 
	@param value passed from {@link #setValues(Object...)}
	@return a {@link String} for return by {@link #values()}
	 */
	protected String valueString(Object value){
		String str=value instanceof Byte||value instanceof Short
				||value instanceof Integer||value instanceof Long
				||value instanceof Boolean||value instanceof String?
						value.toString()
				:value instanceof Double?Util.sf((Double)value)+""
				:value instanceof int[]?Strings.intsString((int[])value)
				:value instanceof double[]?Strings.fxString((double[])value)
				:value instanceof File?((File)value).getAbsolutePath()
				:null;
		if(str==null)throw new IllegalArgumentException(
				"Bad type value="+Debug.info(value));
		return str;
	}
	/**
	Useful re-implementation. 
	@return array set (possibly indirectly) by {@link #setContents(Object...)}
	 */
	public Object[]contents(){
		return contents;
	}
	/**
	Implements interface method. 
	<p>Returns a deep copy of the node and its {@link #contents()} with
	<ul>
	<li>the same {@link #type()} and {@link #title()}
	<li> {@link String}s returned by {@link #values()}
	<li>children created with {@link #copyState()}
	</ul>	
	 */
	@Override
	public Stateful copyState(){
		if(false)return (Stateful)Util.deserializedCopy(this);
		Object[]copy=new Object[contents.length],values=values();
		for(int i=0,valueAt=0;i<copy.length;i++)
			copy[i]=contents[i]instanceof TypedNode?((TypedNode)contents[i]).copyState()
					:values[valueAt++];
		return new ValueNode(type(),title(),copy);
	}
	/**
	Valid re-implementation. 
	@param src must be a {@link TypedNode} of the same {@link #type()}
	 */
	public void setState(Object src){
		TypedNode state=((TypedNode)src);
		if(false&&!type().equals(state.type()))
			throw new IllegalArgumentException("Bad state source type in "
					+Debug.info(this));
		setTitle(state.title());
		setContents(((TypedNode)state.copyState()).contents());
	}
	/**
	Valid re-implementation. 
	@param other must be {@link DataNode}
	@return <code>true</code> if the two trees are byte-identical
	apart from {@link #identity()} 
	 */
	final public boolean stateEquals(Stateful other){
		boolean debug=false;
		if(other==null||!(other instanceof DataNode)){
			if(debug)
				trace(".stateEquals: Can't compare\nthis "+this+"\nother "+other);
			return false;
		}
		DataNode that=(DataNode)other;
		if(!type().equals(that.type())||!title().equals(that.title())){
			if(debug) trace(".stateEquals: Type and title don't match");
			return false;
		}
		TypedNode[]children=children(),thoseChildren=that.children();
		String[]values=values(),thoseValues=that.values();
		if(children.length!=thoseChildren.length){
			if(debug){
				trace(".stateEquals: Children unequal lengths",children);
				Util.printOut("",thoseChildren);
			}
			return false;
		}
		if(values.length!=thoseValues.length){
			if(debug) trace(".stateEquals: Values unequal lengths");
			return false;
		}
		for(int i=0;i<values.length;i++)
			if(!values[i].equals(thoseValues[i])){
				if(debug){
					trace(".stateEquals: Values unequal at "+i,values);
					Util.printOut("",thoseValues);
				}
				return false;
			}
		for(int i=0;i<children.length;i++)
			if(!children[i].stateEquals(thoseChildren[i])){
				if(debug)trace(".stateEquals: All equal except child "+treeString(children[i])
							+"\nunequal to\n"+treeString(thoseChildren[i]));
				return false;
			}
		return true;
	}
}
