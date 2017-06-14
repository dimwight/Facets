package facets.util.tree;
import static facets.util.tree.DataConstants.*;
import static facets.util.tree.TypedNode.*;
import facets.util.Strings;
import facets.util.Tracer;
class TestTrees extends Tracer{
	private static int datas,numbers;
	private final static TypedNode 
	keyPairs=newNode("text=A simple text value",
			"keyedInts=" +"1,2,3,4",
			"doubles=" +newDoublesString(120*1000)
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
	private final int broad;
	private final double shrinkBy;
	TestTrees(String rootType,int broad,double shrinkBy){
		this.rootType=rootType;
		this.broad=broad+2;
		this.shrinkBy=shrinkBy;
		tree=new ValueNode(rootType,"Tree"+datas++);
		if(broad<1)tree.setContents(newDataNode("Dummy").contents());
		else addDescendants(tree,1.0,1);
		if(false)tree.setTitle(tree.title()+" nodes="+Nodes.descendants(tree).length);
	}
	private void addDescendants(DataNode parent,double shrunkBy,int level){
		int across=(int)(broad*shrunkBy);
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