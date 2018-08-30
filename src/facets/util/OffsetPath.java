package facets.util;
import java.io.Serializable;
import java.util.Arrays;
/**
Abstract path through a data structure. 
<p>{@link OffsetPath} extends the principle of the array index to any 
structure that can be described using a set of integer offsets. 
It records an arbitrary path 
to one of a structure's elements as a sequence of offsets describing the relation
 of each intermediate element to its parent.
 <p>While its most obvious and significant use is to describe paths
 to the nodes of tree from its root, an {@link OffsetPath} can be used
 for any data structure that can be indexed - indeed for 
 a simple array. It is therefore defined <code>abstract</code> to enable concrete
implementations that know the type of a structure and the indexing 
convention that it uses.
<p>A major advantage of {@link OffsetPath} is that it 
is indifferent to the <i>identity</i> of its members,
describing only the <i>relationships</i> between them. 
An {@link OffsetPath} can be created 
describing a path in one data structure, and then used to create the 
corresponding path through another; the other structure need not even 
be of the same type, proving it subscribes to the same indexing convention.        
 */
abstract public class OffsetPath extends Tracer implements Serializable{
	/**
	Convenience instance. 
	<p>A fixed single-member root path. 
	 */
	final public static OffsetPath singleMembered=new OffsetPath(new int[]{0}){
		public String toString(){
			return "OffsetPath.singleMembered";
		}
    protected int[]newOffsets(Object[]members){
      throw new RuntimeException("Not implemented in "+Debug.info(this));
    }
    protected Object[]newMembers(Object root,int[]offsets){
			return new Object[]{root};
		}
		@Override
		public OffsetPath procrusted(Object root,Object to){
			throw new RuntimeException("Not implemented in "+Debug.info(this));
		}
  }, 
	empty=new OffsetPath(new int[]{}){
    protected int[]newOffsets(Object[]members){
      throw new RuntimeException("Not implemented in "+Debug.info(this));
    }
    protected Object[]newMembers(Object root,int[]offsets){
			return new Object[]{};
		}
		@Override
		public OffsetPath procrusted(Object root,Object to){
			return this;
		}
		@Override
		public Object target(Object root){
			return root;
		}
  };
	public final int[]offsets;
	@Override
	public boolean equals(Object o){
		return Arrays.equals(offsets,((OffsetPath)o).offsets);
	}
  /**
  Core constructor. 
  <p>Stores immutable <code>offsets</code> 
  for use by {@link #newMembers(Object,int[])}</li>
  </ul>
  @param offsets define path
   */
  protected OffsetPath(int[]offsets){
		this.offsets=offsets;
	}
  /**
  Core constructor. 
  <p>Creates path by 
  <ul>
  <li>storing the first element of <code>members</code> for return as
  <code>root</code></li>
  <li>storing the offsets returned by {@link #newOffsets(Object[])}</li>
</ul>
  @param members members of the path, the first being the root data 
  object
   */
  protected OffsetPath(Object[]members){
  	offsets=members.length==1?new int[]{0}:newOffsets(members);
    if(members.length!=offsets.length)
      throw new IllegalArgumentException("members.length!=offsets.length in "+Debug.info(this));
    else if(offsets[0]!=0)
      throw new IllegalStateException("offsets[0]!=0 in "+Debug.info(this));
  }
  /**
	Return the members of <code>root</code> referenced by the stored offsets. 
	<p>The array returned is that created in {@link #newMembers(Object,int[])}. 
	 */
	final public Object[]members(Object root){
		if(root==null)throw new IllegalArgumentException("Null root in "+Debug.info(this));
	  Object members[]=newMembers(root,offsets);
	  if(members==null)throw new IllegalStateException("Null members in "+Debug.info(this));
	  else if(members.length>0&&members[0]!=root)throw new IllegalStateException(Debug.info(members[0]) +
	  		"!=" + Debug.info(root)+" in "+Debug.info(this));
	  else if(false&&members.length!=offsets.length)
	    throw new RuntimeException("members.length!=offsets.length in "+Debug.info(this));
	  else return members;
	}
	/**
	 The primary target of the path. 
	 <p>Returns the last element of <code>members</code>. 
	 <p>For many applications this will be the 'selected' element of <code>root</code>. 
	 */
	public Object target(Object root){
		Object[]members=members(root);
		return members[members.length-1];
	}
  /**
  Construct offsets recording a path described by its members. 
  <p>Called by {@link #OffsetPath(Object[])}. 
  @param members the path to be recorded 
   */
  abstract protected int[]newOffsets(Object[]members);
  /**
  Return the members of <code>root</code> referenced by <code>offsets</code>. 
  <p>Called by {@link #members(Object)}.  
   */
  abstract protected Object[]newMembers(Object root,int[]offsets);
	public abstract OffsetPath procrusted(Object root, Object to);
	public String toString(){
	  return Debug.info(this)+" "+Strings.intsString(offsets);
	}
}
