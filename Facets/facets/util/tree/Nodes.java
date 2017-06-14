package facets.util.tree;
import static facets.util.ByteStrings.*;
import static facets.util.Bytes.*;
import static facets.util.Objects.*;
import static facets.util.Regex.*;
import static facets.util.tree.TypedNode.*;
import facets.util.ByteStrings;
import facets.util.Bytes;
import facets.util.Debug;
import facets.util.ItemList;
import facets.util.Objects;
import facets.util.Strings;
import facets.util.Tracer;
import facets.util.Util;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
/**
Utilities for manipulating and querying trees of {@link TypedNode}s and its subclasses. 
 */
public final class Nodes{
	/**
	Provides for (de)persisting the contents of a {@link DataNode}.   
		 */
	abstract static public class TreeRoot extends Tracer{
		/**
		Root node of the tree to read or write. 
		 */
		final public DataNode tree;
		/**
		Unique constructor. 
		@param node to (de)persist
		 */
		public TreeRoot(DataNode node){
			this.tree=node;
		}
		/**
		Read content into {@link #tree}. 
		*/
		public abstract void readFromSource(Object src)throws IOException;
		/**
		Write out content of {@link #tree}. 
		 */
		public abstract void writeToSink(Object sink)throws IOException;
	}
	public static void storeEncodedText(DataNode store,String...texts){
		Object[]encoded=new Object[Strings.linesString(texts).trim().length()>0?texts.length:0];
		for(int textAt=0;textAt<encoded.length;textAt++)
			encoded[textAt]=encodeBytes(pack(texts[textAt]),0);
		store.setContents(encoded);
	}
	public static String restoreEncodedText(DataNode store){
		StringBuilder restore=new StringBuilder();
		for(String coded:store.values())
			restore.append((String)unpack(decodeString(coded))+"\n");
		return restore.toString();
	}
	public static DataNode encode(final DataNode node,int breakAt){
		Object[]contents=node.contents();
		if(contents.length==0)return null;
		for(Object item:contents)if(item instanceof TypedNode)((TypedNode)item).setParent(null);
		TypedNode<byte[]>bytes=newPacked(node);
		String encoded=encodeBytes(bytes.values()[0],breakAt);
		if(encoded==null)return null;
		if(false)Util.printOut("Nodes.encode: "+encoded.length()+"\n"+encoded.replaceAll("\\s*",""));
		node.setContents(encoded.split("\n"));
		return node;
	}
	public static DataNode decode(DataNode node){
		Object[]contents=node.contents();
		if(contents.length==0)return node;
		for(Object object:contents)if(!(object instanceof String))return node;
		byte[]packed=decodeString(Strings.linesString(newTyped(String.class,contents)));
		if(packed==null)return node;
		Object[]unpacked=(Object[])(unpack(packed));
		if(unpacked!=null)node.setContents(unpacked);
		return node;
	}
	public static TypedNode<byte[]>newPacked(DataNode node){
		for(TypedNode child:node.children())child.setParent(null);
		final byte[]packed=pack(newTyped(Serializable.class,node.contents()));
		if(false)Util.printOut("Nodes.newPacked: packed=",Util.kbs(packed.length));
		return new TypedNode<byte[]>(byte[].class,node.type(),node.title()){
			@Override
			public Object[]contents(){
				return new Object[]{packed};
			}
		};
	}
	public static ValueNode newUnpacked(TypedNode<byte[]>packed){
		Util.printOut("Nodes.newUnpacked: "+packed);
		Object[]contents=(Object[])unpack(packed.values()[0]);
		return new ValueNode(packed.type(),packed.title(),contents);
	}
	public static File writeSerialized(DataNode tree,File dir,boolean packed)
			throws IOException{
		File file=new File(dir,tree.title()+"."+tree.type()+"." +DataConstants.TYPE_TREE);
		Util.printOut("Nodes.writeSerialized: file=",file);
		new ObjectOutputStream(new FileOutputStream(file)).writeObject(!packed?tree
				:pack(tree));
		return file;
	}
	public static void readSerialized(DataNode tree,File dir,boolean packed) 
			throws ClassNotFoundException,IOException{
		String type=tree.type(),title=tree.title();
		File file=new File(dir,title+"."+type+"." +DataConstants.TYPE_TREE);
		Util.printOut("Nodes.readSerialized: file=",file);
		Object read=new ObjectInputStream(new FileInputStream(file)).readObject();
		if(packed)read=unpack((byte[])read);
		DataNode check=(DataNode)read;
		if(!check.type().equals(type)||!check.title().equals(title))
			throw new IllegalStateException("Check does not match tree="+Debug.info(tree));
		else tree.setContents(check.contents());
	}
	public static void readAdjustedArgs(ValueNode target,String...args){
		String EQU=ValueNode.KEY_EQUALS;
		for(String arg:args){
			if(arg==null||arg.trim().equals(""))continue;
			boolean scored=arg.startsWith("_");
			if(arg.indexOf(EQU)<0)arg=scored?(arg.substring(1)+EQU+"false")
				:(arg+EQU+"true");
			else if(scored)continue;
			int equalsAt=arg.indexOf(EQU);
			String[]pair={arg.substring(0,equalsAt).trim(),
					arg.substring(equalsAt+1).trim()};
			String key=pair[0],value=pair[1];
			target.put(key,value);
		}
	}
	/**
	Merges new with existing node contents. 
	<p>Existing values with the same keys are overwritten. 
	@param node contains any existing contents
	@param mergeContents may include further {@link ValueNode}s which will be 
	merged with any of the same type, name and path
	@return <code>node</code> passed with merged contents
	 */
	public static ValueNode mergeContents(ValueNode node,Object[]mergeContents){
		if(node==null)throw new IllegalArgumentException(
				"Null node for merge of "+Debug.info(mergeContents));
		else if(mergeContents==null)throw new IllegalArgumentException(
				"Null mergeContents for "+Debug.info(node));
		if(false){
			List values=new ArrayList();
			for(String value:Arrays.asList(node.values()))
				if(!value.endsWith("=false")&&!value.endsWith("=true"))values.add(value);
			node.setValues(values.toArray());
		}
		boolean trace=false&&node.type().equals("args");
		if(trace)Util.printOut("Nodes.mergeContents: mergeContents=",mergeContents);
		for(Object c:mergeContents)if(c instanceof String){
				String[]splits=((String)c).split("=");
				if(splits.length==2)node.put(splits[0],splits[1]);
			}
		NodeList merged=new NodeList(node,false);
		for(TypedNode merge:new DataNode(DataConstants.TYPE_DATA,"Merge",mergeContents
				).children()){
			TypedNode child=child(node,merge.type(),merge.title());
			if(child!=null)mergeContents((ValueNode)child,merge.contents());
			else merged.add(merge);
		}
		node.setContents(node.values());
		merged.updateParent();
		if(trace)Util.printOut("Nodes.mergeContents~: node="+node);
		return node;
	}
	public static void minimiseTree(DataNode node){
		NodeList children=new NodeList(node,false);
		for(TypedNode child:new NodeList(node,false)){
			minimiseTree((DataNode)child);
			if(child.contents().length==0)children.remove(child);
		}
		children.updateParent();
		if(true)return;
		List values=new ArrayList();
		for(String value:Arrays.asList(node.values()))
			if(!value.endsWith("=false"))values.add(value);
		node.setValues(values.toArray());
	}
	public static boolean isKeyPair(String text){
		return contains(text,"^[\\w:]+=");
	}
	public static String[]splitPair(String text){
		return text.split("=");
	}
	/**
	Returns stringifications of the tree root and its descendants' 
	<code>type</code>, <code>title</code> and <code>values</code> 
	in contents order.
		 */
	public static String treeString(TypedNode root){
		StringBuilder lines=new StringBuilder();
		for(TypedNode node:descendants(root)){
			lines.append("type="+node.type());
			lines.append("|title="+node.title());
			for(String each:((DataNode)node).values())lines.append("|"+each);
			lines.append("\n");
		}
		return lines.toString().trim();
	}
	public static TypedNode[]ancestry(TypedNode node){
		if(node.parent()==null)return new TypedNode[]{node};
		ItemList<TypedNode>ascent=new ItemList(TypedNode.class);
		TypedNode maybeTop=node.parent();
		ascent.addItems(new TypedNode[]{node,maybeTop});
		while(maybeTop.parent()!=null)
			ascent.addItem(maybeTop=maybeTop.parent());
		return Objects.reverse(TypedNode.class,ascent.items());
	}
	public static TypedNode child(TypedNode node,String type){
		return child(node,new String[]{type},UNTITLED);
	}
	public static TypedNode child(TypedNode node,String type,String title){
		return child(node,new String[]{type},title);
	}
	public static TypedNode child(TypedNode node,String[]types){
		return child(node,types,UNTITLED);
	}
	public static TypedNode[]children(TypedNode node,String...types){
		if(node==null)throw new IllegalArgumentException("Null node");
	  TypedNode[]children=node.children();
		ItemList<TypedNode>matches=new ItemList(TypedNode.class);
	  for(int c=0;c<children.length;c++)for(int i=0;i<types.length;i++)
	  		if(children[c].type().equalsIgnoreCase(types[i]))
					matches.addItem(children[c]);
	  return matches.items();
	}
	public static TypedNode descendantTyped(TypedNode node,String type){
		for(TypedNode d:descendants(node))if(d.type().equals(type))return d;
		return null;
	}
	public static TypedNode descendantTitled(TypedNode node,String title){
		for(TypedNode d:descendants(node))if(d.title().equals(title))return d;
		return null;
	}
	public static TypedNode[]descendantsTyped(TypedNode node,String...types){
		List<TypedNode>list=new ArrayList();
		for(TypedNode d:descendants(node))for(String type:types)if(d.type().equals(type))list.add(d);
		return list.toArray(new TypedNode[]{});
	}
	public static TypedNode[]descendants(TypedNode node){
		if(node==null)throw new IllegalStateException("Null node");
		List<TypedNode>list=new ArrayList();
		list.add(node);
		for(TypedNode child:node.children())
		  list.addAll(Arrays.asList(descendants(child)));
		return list.toArray(new TypedNode[]{});
	}
	public static ValueNode guaranteedChild(DataNode parent,String type){
		return guaranteedChild(parent,type,UNTITLED);
	}
	public static ValueNode guaranteedChild(DataNode parent,
			String type,String title){
		ValueNode guaranteed=(ValueNode)child(parent,type,title);
		if(guaranteed!=null)return guaranteed;
		guaranteed=new ValueNode(type,title);
		appendChild(parent,guaranteed);
		return guaranteed;
	}
	public static ValueNode guaranteedDescendant(DataNode node,String...typePath){
		for(String type:typePath)node=guaranteedChild(node,type);
		return(ValueNode)node;
	}
	public static void setChild(DataNode parent,TypedNode child){
		List<TypedNode>children=new NodeList(parent,true);
		if(!children.contains(child))children.add(child);
		else for(TypedNode check:children)
			if(check.type().equals(child.type())&&check.title().equals(child.title()))
				children.set(children.indexOf(check),child);
	}
	public static void appendChild(DataNode node,TypedNode child){
		TypedNode[]children=node.children(),appended=new TypedNode[children.length+1];
		System.arraycopy(children,0,appended,0,children.length);
		appended[children.length]=child;
		node.setChildren(appended);
	}
	public static String valuesAsLine(TypedNode node,int limit){
		String[]strings=((DataNode)node).values();
		StringBuffer b=new StringBuffer();
		for(int i=0;i<strings.length;i++){
			String s=strings[i];
			b.append(s+" ");			
		}
		String line=b.toString().replaceAll("\\s+"," ");
		return limit<=0||line.length()<limit?line:line.substring(0,limit)+"...";
	}
	/**
	@return a tree sized by the parameters;	for <code>broad</code>&lt=0 a minimal tree  
	 */
	public static TypedNode newTestTree(String rootType,int broad,double shrinkBy){
		return new TestTrees(rootType,broad,shrinkBy).tree;
	}
	public static String treeInfo(TypedNode tree,boolean chars){
		return ""+Util.sf(!chars?Nodes.descendants(tree).length:Nodes.treeString(tree).length());
	}
	public static boolean hasMixedContents(TypedNode node){
		if(node.children().length==0)return false;
		for(Object v:node.values())if(!isKeyPair(v.toString()))return true;
		return false;
	}
	public static String removeValue(ValueNode node,String key){
		String value=node.get(key);
		if(value==null){
			if(false)throw new IllegalStateException("Null value for key="+key+" in "+node);
			else return "";
		}
		String[]then=node.values();
		ArrayList<String>now=new ArrayList();
		for(String check:then)if(!check.matches("^"+key+"\\s*=.*"))now.add(check);
		if(now.size()!=then.length-1)throw new IllegalStateException(
				"Bad now in "+node);
		else node.setValues(now.toArray());
		return value;
	}
	public static void removeNullValues(ValueNode node){
		if(node.children().length>0)throw new IllegalArgumentException(
				"Can't process children in "+node);
		String[]then=node.values();
		ArrayList now=new ArrayList();
		for(String value:then)
			if(!value.matches("\\w+=("+ValueNode.NULL_MARKER+")?$"))now.add(value);
		int removed=then.length-now.size();
		if(false&&removed>0)Util.printOut("ValueField.removeNullValues: removed="+removed
//				+" then=\n",then
			);
		node.setValues(now.toArray());
	}
	public static void sortValues(ValueNode values){
		values.setValues(Strings.sortLines(values.values()));
	}
	private static TypedNode child(TypedNode node,String[]types,String title){
	  TypedNode[]children=children(node,types);
	  for(int c=0;c<children.length;c++)
	  		if(title.equals(UNTITLED)||children[c].title().equals(title))
	  			return children[c];   	
	  return null;
	}
	private static void appendValue_(DataNode node,Object value){
		Object[]values=node.values(),appended=new Object[values.length+1];
		System.arraycopy(values,0,appended,0,values.length);
		appended[values.length]=value;
		node.setValues(appended);
	}
	private static String valuesFirstLine_(TypedNode node){
		String[]lines=((DataNode)node).values();
		return lines[0]+(lines.length==1?"":"...");
	}
	private static String newValuesTitle_(DataNode node){
		String[]values=node.values();
		if(values.length==0)return UNTITLED;
		String text=values[0];
		final int stop=20;
		return " ["+(text.length()>stop?text.substring(0,stop)+"...":text)+"]";
	}
}
