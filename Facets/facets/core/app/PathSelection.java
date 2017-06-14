package facets.core.app;
import facets.core.superficial.app.SSelection;
import facets.util.Debug;
import facets.util.Objects;
import facets.util.OffsetPath;
import facets.util.Strings;
import facets.util.Tracer;
import facets.util.Util;
import facets.util.tree.NodePath;
import facets.util.tree.TypedNode;
import facets.util.tree.ValueNode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
/**
Implements {@link SSelection} using {@link OffsetPath}s. 
<p>The immutable {@link #paths} members of {@link PathSelection} should describe 
paths rooted on {@link #content()}. 
	 */
public class PathSelection extends Tracer implements SSelection{
	/** 
	The {@link OffsetPath}s passed to the constructor and rooted on {@link #content()}. 
	 */
	final public OffsetPath[]paths;
	final private transient Object content;
	/**
	Core constructor. 
	@param paths set as {@link #paths}; they be rooted on <code>content</code>, 
	which is returned as {@link #content()}
	 */
	public PathSelection(Object content,OffsetPath...paths){
		this.content=content;
		this.paths=paths;
		if(paths.length==0)throw new IllegalArgumentException(
				"Empty paths in "+Debug.info(this));
	}
	/**
	Creates a selection with a single, single-membered path. 
	@param content is passed to {@link #PathSelection(Object, OffsetPath...)}
	 together with {@link OffsetPath#singleMembered}
	 */
	public static PathSelection newMinimal(Object content){
		return new PathSelection(content,OffsetPath.singleMembered);
	}
	public static PathSelection procrust(PathSelection src,Object to){
		List<OffsetPath>out=new ArrayList();
		for(OffsetPath in:src.paths)out.add(in.procrusted(src.content,to));
		return new PathSelection(to,out.toArray(new OffsetPath[]{}));
	}
	public static Object[]pathMembers(PathSelection selection,int pathAt){
		return selection.paths[pathAt].members(selection.content());
	}
	/**
	Implements interface method. 
	@return the {@link OffsetPath#target(Object)}s of {@link #paths}
	 */
	public Object[]multiple(){
		Object[]selected=new Object[paths.length];
		for(int i=0;i<selected.length;i++)selected[i]=paths[i]==OffsetPath.empty?
			OffsetPath.singleMembered:paths[i].target(content);
		return selected;
	}
	/**
	Implements interface method. 
	@return the first member of {@link #multiple()}
	 */
	public Object single(){
		return multiple()[0];
	}
	final public Object content(){
		if(content==null)throw new IllegalStateException("Null content (deserialized) in "+Debug.info(this));
		return content;
	}
	public String toString(){
		return Debug.info(this)+"\ncontent="+Debug.info(content)+" selected="+
			Objects.toLines(multiple())+
			"\npaths[0]=" +Debug.info(paths[0])
					+" [" +Strings.intsString(paths[0].offsets)+"]"
			+(true?"":Objects.toLines(paths[0].members(content)));
	}
	public static void putSelectionOffsets(PathSelection selection,ValueNode state,String key){
		state.put(key,selection.paths[0].offsets);
	}
	public static PathSelection getOffsetSelection(Object root,ValueNode state,String key){
		PathSelection minimal=newMinimal(root),selection;
		try{
			int[]offsets=state.getInts(key);
			selection=offsets.length==0?minimal:new PathSelection(root,new NodePath(offsets));
			selection.paths[0].members(root);
		}
		catch(Exception e){
			selection=minimal;
		}
		return selection;
	}
}