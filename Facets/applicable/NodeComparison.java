package applicable;
import static facets.util.tree.Nodes.*;
import facets.util.Debug;
import facets.util.Tracer;
import facets.util.Util;
import facets.util.tree.DataConstants;
import facets.util.tree.DataNode;
import facets.util.tree.NodeList;
import facets.util.tree.Nodes;
import facets.util.tree.TypedNode;
import facets.util.tree.ValueNode;
import facets.util.tree.XmlDocRoot;
import facets.util.tree.XmlSpecifier;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
public class NodeComparison extends Tracer{
	private final ValueNode now;
	public NodeComparison(ValueNode now){
		this.now=now;
	}
	@Override
	protected void traceOutput(String msg){
		Util.printOut(msg);
	}
	public void compare(TypedNode then){
		trace("ADDED to ",now);
		mergeCheckContents((ValueNode)then.copyState(),
				((ValueNode)now.copyState()).contents());
		trace("REMOVED from ",then);
		mergeCheckContents((ValueNode)now.copyState(),
				((ValueNode)then.copyState()).contents());
	}
	private void mergeCheckContents(ValueNode check,Object[]contents){
		for(Object c:contents)if(c instanceof String){
				String[]splits=((String)c).split("=");
				if(splits.length!=2)continue;
				String key=splits[0],put=splits[1],got=check.get(key);
				if(!put.equals(got))
					trace(" check="+check.type()+" key="+key+" got="+got+" put="+put);
				check.put(key,put);
			}
		NodeList merged=new NodeList(check,false);
		for(TypedNode put:new DataNode(DataConstants.TYPE_DATA,"Merge",contents
				).children()){
			TypedNode got=child(check,put.type(),put.title());
			if(got!=null)mergeCheckContents((ValueNode)got,put.contents());
			else{
				trace(" check="+check.type()+" put=\n"+treeString(put));
				merged.add(put);
			}
		}
		check.setContents(check.values());
		merged.updateParent();
	}
}
