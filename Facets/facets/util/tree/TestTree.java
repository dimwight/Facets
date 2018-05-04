package facets.util.tree;
import static facets.util.Util.*;
import static facets.util.tree.DataConstants.*;
import facets.util.Strings;
import facets.util.Tracer;
class TestTree extends Tracer{
	private static int datas,numbers;
	private final static TypedNode 
	keyPairs=newNode("text=A simple text value",
			"keyedInts=" +"1,2,3,4",
			"doubles=" +newDoublesString(120)
		),
	text=newNode("Some text on ",
			"two," ,
			"three or even " ,
			"four lines"),
	mixed=new ValueNode(TYPE_DATA,"Mixed",
			new Object[]{
			"First simple text value",
			newNode(),
			"Second simple text value",
			newNode(),
			"Last simple text value",
		});
	private static ValueNode newNode(Object...contents){
		return new ValueNode(TYPE_DATA,"Node"+numbers++,contents);
	}
	private static String newDoublesString(int count){
		double[]vals=new double[count];
		for(int i=0;i<vals.length;i++)vals[i]=Math.sqrt(100d*i);
		return Strings.fxString(vals);
	}
	final DataNode tree;
	private final String rootType;
	private final int width;
	private final double shrinkBy=0.7;
	TestTree(String rootType,int width){
		this.rootType=rootType;
		this.width=width+2;
		tree=new ValueNode(rootType,"Tree"+datas++);
		if(width<1)tree.setContents(newDataNode("Dummy").contents());
		else addDescendants(tree,1.0,1);
		int nodes=Nodes.descendants(tree).length;
		if(false)tree.setTitle(tree.title()+" nodes="+nodes);
		else if(false)trace("<li>width="+width+" nodes="+nodes
				+ " bytes=",false?"":kbs(Nodes.treeString(tree).length())+"</li");
	}
	private void addDescendants(DataNode parent,double shrunkBy,int level){
		int across=(int)(width*shrunkBy);
		if(across<3){
			if(false)trace(": descendants="+Nodes.descendants(tree).length+" parent=",parent);
			return;
		}
		NodeList children=new NodeList(parent,true);
		for(int i=0;i<across;i++){
			ValueNode child=newDataNode("Level "+level);//+" Child "+i
			children.add(child);
			addDescendants(child,shrunkBy*shrinkBy,level+1);
		}
	}
	private ValueNode newDataNode(String title){
		return new ValueNode(TYPE_DATA,title,new Object[]{
			keyPairs.copyState(),
			text.copyState(),
			mixed.copyState(),
		});
	}
}