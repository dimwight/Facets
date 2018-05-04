package facets.util.tree;
import static facets.util.Debug.*;
import static facets.util.Strings.*;
import static java.util.Arrays.*;
import facets.util.Debug;
import facets.util.OffsetPath;
import facets.util.Strings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
/**
{@link OffsetPath} for {@link TypedNode} trees that can index into their 
{@link TypedNode#values()}.   
 */
public final class NodePath extends OffsetPath{
  public NodePath(Object[]members){
		super(members);
		if(false){
			Debug.printStackTrace(4);
			trace(":",this);
		}
	}
  public NodePath(int[]offsets){
  	super(offsets);
	}
	protected Object[]newMembers(Object root,int[]offsets){
	  if(false)return newContentMembers_(root,offsets);
	  int valueAt=valueAt();
		TypedNode[]nodeMembers=new TypedNode[offsets.length-(valueAt>=0?2:0)];
	  if(false&&valueAt>=0)
	  	trace(".newMembers: valueAt="+valueAt+
	  			" offsets="+Strings.intsString(offsets)
	  		+" nodeMembers="+nodeMembers.length);
	  nodeMembers[0]=(TypedNode)root;
	  if(nodeMembers.length==1)return nodeMembers;
		for(int i=1;i<nodeMembers.length;i++)
			nodeMembers[i]=nodeMembers[i-1].children()
				[offsets[i]];
	  return nodeMembers;
	}
	private Object[]newContentMembers_(Object root,int[]offsets){
		int valueAt=valueAt();
	  Object[]members=new Object[offsets.length];
	  if(false&&valueAt>=0)
	  	trace(".newMembers: valueAt="+valueAt+
	  			" offsets="+Strings.intsString(offsets)
	  		+" members="+members.length);
	  members[0]=root;
	  if(members.length==1)return members;
		for(int i=1;i<members.length;i++)
			members[i]=((TypedNode)members[i-1]).contents()[offsets[i]];
	  return members;
	}
	protected int[]newOffsets(Object[]members){
		if(false)return newContentOffsets(members);
		int[]offsets=new int[members.length];
		offsets[0]=0;
		for(int m=1;m<members.length;m++){
			TypedNode parent=(TypedNode)members[m-1];
			Object children[]=parent.children(),find=members[m];
			offsets[m]=-1;
			for(int i=0;i<children.length;i++)
				if(children[i]==find){
					offsets[m]=i;
					break;
				}				
			if(offsets[m]==-1)throw new IllegalStateException(
					Debug.info(find)+"\nnot found as child of "
					+parent+" children=\n"+Debug.arrayInfo(parent.children()));
		}
		return offsets;
	}
	private static int[]newContentOffsets(Object[] members){
		int[]offsets=new int[members.length];
		offsets[0]=0;
		for(int m=1;m<members.length;m++){
			TypedNode parent=(TypedNode)members[m-1];
			Object contents[]=parent.contents(),find=members[m];
			offsets[m]=-1;
			for(int i=0;i<contents.length;i++)
				if(contents[i]==find){
					offsets[m]=i;
					break;
				}				
			if(offsets[m]==-1)throw new IllegalStateException(
					Debug.info(find)+"\nnot found as contents of "
					+parent+" contents=\n"+Debug.arrayInfo(parent.contents()));
		}
		return offsets;
	}
	@Override
	public OffsetPath procrusted(Object rootThen,Object rootNow){
		List<Object>members=new ArrayList(Arrays.asList(members(rootThen)));
		members.remove(0);
		members.addAll(0,Arrays.asList(Nodes.ancestry((TypedNode)rootThen)));
		int nowAt=members.indexOf(rootNow);
		if(nowAt<0){
			if(members.size()==0)return empty;
			else throw new IllegalStateException(info(rootNow)+
					" not found in "+Debug.arrayInfo(members.toArray()));
		}
		members=members.subList(nowAt,members.size());
		if(false)trace(".procrusted: " +this+" nowAt="+nowAt+"\n\trootThen="+rootThen+
				" rootNow="+rootNow+" members=",members);
		return new NodePath(members.toArray()).valueAtChecked(valueAt());
	}
	public int valueAt(){
		int count=offsets.length;
		return count<3||offsets[count-2]>=0?-1:offsets[count-1];
	}
	public NodePath valueAtChecked(int valueAt){
		if(false)trace(".valueAtChecked: ",this);
		boolean thisAt=this.valueAt()>=0;
		if(valueAt<0)return !thisAt?this
				:new NodePath(copyOf(offsets,offsets.length-2));
		int offsetsNow[]=copyOf(offsets,offsets.length+(thisAt?0:2)),
			count=offsetsNow.length;
		offsetsNow[count-2]=-1;
		offsetsNow[count-1]=valueAt;
		NodePath pathNow=new NodePath(offsetsNow);
		if(false)trace(".valueAtChecked: pathNow=",pathNow);
		return pathNow;
	}
	@Override
	public String toString(){
		return Debug.info(this)+" offsets=["+Strings.intsString(offsets)+"] valueAt="+valueAt();
	}
	public boolean isParent(OffsetPath that){
		if(isSibling(that))return false;
		int[]these=this.offsets,those=that.offsets;
		int count=these.length;
		if(count>2&&these[count-2]==-1)return false;
		these=parentOffsets(this.offsets);those=parentOffsets(that.offsets);
		if(these.length>those.length)return false;
		if(false)trace(".: these="+intsString(these)+" those=",intsString(those));
		for(int i=0;i<these.length;i++){
			int theseNext=these[i],thoseNext=those[i];
			if(theseNext!=thoseNext)return false;
		}
		return true;
	}
	public boolean isSibling(OffsetPath that){
		int[]these=parentOffsets(this.offsets),those=parentOffsets(that.offsets);
		if(false)trace(".: these="+intsString(these)+" those=",intsString(those));
		if(these.length!=those.length)return false;
		for(int i=0;i<these.length;i++){
			int theseNext=these[i],thoseNext=those[i];
			if(theseNext!=thoseNext)return false;
		}
		return true;
	}
	private int[]parentOffsets(int[]all){
		int count=all.length;
		return copyOf(all,count-(count>2&&all[count-2]==-1?2:false?1:0));
	}
}