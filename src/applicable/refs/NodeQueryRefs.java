package applicable.refs;
import static applicable.refs.TextReferences.*;
import static facets.util.Debug.*;
import static facets.util.Util.*;
import static facets.util.tree.Nodes.*;
import facets.util.Debug;
import facets.util.TextLines;
import facets.util.Times;
import facets.util.Tracer;
import facets.util.Util;
import facets.util.tree.DataNode;
import facets.util.tree.TypedNode;
import facets.util.tree.ValueNode;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import applicable.TextQuery;
import applicable.refs.TextReferences.ReadSourceProvider;
import applicable.refs.TextReferences.WordSource;
public abstract class NodeQueryRefs<T>extends Tracer{
	final static boolean packFile=false;
	public static TypedNode newSourceTree(File file){
		if(false)Util.printOut("NodeQueryRefs.newSourceTree: file=",kbs(file.length()));
		return newPacked(readTree(new TextLines(file)));
	}
	public static void writeRefTrees(Collection<TypedNode>trees,File dir,String title)
			throws IOException{
		memCheck=true;
		DataNode root=new ValueNode(TextReferences.TYPE,title,trees.toArray());
		memCheck("NodeQueryRefs.writeRefs: children="+root.children().length);
		File file=writeSerialized(root,dir,packFile);
		Util.printOut("NodeQueryRefs.writeRefs: bytes="+kbs(file.length())+" file=",file);
	}
	private final Map<String,TypedNode>trees=new HashMap();
	private final Map<String,TextReferences>refs=new HashMap();
	private final TextReferences parent;
	public NodeQueryRefs(String title,File indexRoot,TextReferences parent)
			throws IOException,ClassNotFoundException{
		this.parent=parent;
		DataNode root=new DataNode(TextReferences.TYPE,title);
		readSerialized(root,indexRoot,packFile);
		if(false)memCheck=false;
		for(TypedNode tree:root.children())trees.put(tree.title(),tree);
		Collection<TypedNode>values=trees.values();
		memCheck("NodeQueryRefs: refs="+values.size());
		if(false)newTreeReferences(newUnpacked(
				(TypedNode)values.toArray()[0]),ReadSourceProvider.DEFAULT);
	}
	@Override
	final protected void traceOutput(String msg){
		if(false)Times.printElapsed("NodeQueryRefs"+msg);
		else if(true)Debug.memCheck("NodeQueryRefs"+msg);
		else super.traceOutput(msg);
	}
	public Collection<T>executeQuery(TextQuery query){
		WordSource[]sources=parent.findSources(query.text.split("\\W"),false
				).toArray(new WordSource[]{});
		trace(".executeQuery: sources=",sources.length);
		int maxAt=Math.min(10*1000,sources.length);
		List<T>passes=new ArrayList();
		if(false)Times.setResetWait(10000);
		for(WordSource source:sources){
			String id=source.identity();
			TextReferences next=refs.get(id);
			if(next==null){
				TypedNode packed=trees.get(id);
				if(packed==null)continue;
				refs.put(id,next=newTreeReferences(newUnpacked(packed),
						ReadSourceProvider.DEFAULT));
			}
			Set<WordSource>finds=next.findQuerySources(query);
			if(finds.size()>0)passes.add(newPassed(id,finds));
			else if(source==sources[maxAt-1])break;
		}
		refs.clear();
		trace(".executeQuery~");
		return passes;
	}
	protected abstract T newPassed(String id,Set<WordSource>finds);
}
