package facets.util;
import java.util.Arrays;
/**
{@link OffsetPath} for array-based content. 
 */
final public class ArrayPath extends OffsetPath{
	public ArrayPath(Object[]array,Object indexed){
		super(new Object[]{array,indexed});
	}
	public ArrayPath(int[]offsets){
		super(offsets);
	}
	protected int[]newOffsets(Object[]members){
    if(members.length!=2)throw new IllegalArgumentException("Bad members in "+Debug.info(this));
    Object array[]=(Object[])members[0],node=members[1];
    for(int i=0;i<array.length;i++)if(array[i]==node)return new int[]{0,i};
    throw new RuntimeException("Node " + Debug.info(node)+" not found in root "+Debug.info(array));
  }    
  protected Object[]newMembers(Object root,int[]indices){
    if(indices.length==1)return new Object[]{root};
    Object[]array=(Object[])root;
    return new Object[]{array,array[indices[1]]};
  }
	/**
	Invalid stub implementation. 
	 */
	public OffsetPath procrusted(Object root,Object to){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
}