package facets.util;
import facets.util.tree.DataNode;
import facets.util.tree.ExceptionNode;
import facets.util.tree.TypedNode;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
/**
{@link DataNode} that knows about {@link File}s. 
 */
public class FileNode extends DataNode{
	public static final String TYPE_FOLDER="Folder",TYPE_FILE="File";
	public static boolean checkReadDirs,checkReadFiles;
	public final File file;
  private boolean hasReadContents;
  public FileNode(File file){
		super(fileType(file),fileTitle(file));
		TextLines.setDefaultEncoding(true);
		boolean writeOnly=writeOnly();
		if(!(this.file=file).exists()&&!writeOnly)setTitle("No file found");
		else if(writeOnly)setTitle(file.getAbsolutePath());
		if(!file.isDirectory())return;
		String[]list=file.list();
		if(list!=null&&list.length>0)
			setChildren(new DataNode("Unread","children"));
	}
	final public void checkReadContents(){
		if(writeOnly()||hasReadContents)return;
		try{
			if(!file.isDirectory())readFileContents(file);
			else readDirContents(file);
		}catch(Exception e){
			setContents(new Object[]{new ExceptionNode(e)});
		}
		hasReadContents=true;
	}
	protected boolean writeOnly(){
		return false;
	}
	protected void readFileContents(File file)throws IOException{
		setContents(new TextLines(file).readLines());
	}
	protected void readDirContents(File dir){
		setChildren(new TypedNode[]{});
		List<TypedNode>children=new ArrayList();			
		String[]names=dir.list(dirFilter());
		if(names!=null)for(String name:names){
			FileNode child=newDirChild(new File(dir,name));
			if(child==null||!child.file.isDirectory())throw new IllegalStateException(
					"Null or non-dir child in "+Debug.info(this));
			else children.add(child);
			if(checkReadDirs)child.checkReadContents();
		}
		names=dir.list(fileFilter());
		if(names!=null)for(String name:names){
			FileNode child=newFileChild(new File(dir,name));
			if(child==null||child.file.isDirectory())throw new IllegalStateException(
					"Null or dir child in "+Debug.info(this));
			else children.add(child);
			if(checkReadFiles)child.checkReadContents();
		}
		setChildren(orderDirContents(children.toArray(new TypedNode[]{})));
		if(false)return;
		List<FileNode>dirs=new ArrayList(),files=new ArrayList();
		for(FileNode child:Objects.newTyped(FileNode.class,children()))
			if(child.file.isDirectory())dirs.add(child);
			else files.add(child);
		dirWillReadFileChildren();
		for(FileNode child:files)child.checkReadContents();
		dirHasReadFileChildren();
		if(readDirs())for(FileNode child:dirs)child.checkReadContents();
	}
	protected FilenameFilter dirFilter(){
		return new FilenameFilter(){
			public boolean accept(File dir,String name){
			  return new File(dir,name).isDirectory();
			}
		};
	}
	protected FilenameFilter fileFilter(){
		return new FilenameFilter(){
			public boolean accept(File dir,String name){
			  return!new File(dir,name).isDirectory();
			}
		};
	}
	protected FileNode newDirChild(File dir){
		return new FileNode(dir);
	}
	protected FileNode newFileChild(File file){
		return new FileNode(file);
	}
	protected TypedNode[]orderDirContents(TypedNode[]contents){
		return contents;
	}
	protected void dirWillReadFileChildren(){}
	protected void dirHasReadFileChildren(){}
	protected boolean readDirs(){
		return false;
	}
	final public void writeFileContentLines()throws IOException{
		new TextLines(file).writeLines((String[])contents());
	}
	public static FileNode newLinesOutput(File file,String[]lines){
		FileNode node=new FileNode(file){
			@Override
			protected boolean writeOnly(){
				return true;
			}
		};
		node.setContents(lines);
		return node;
	}
	public static void deserializeTree(DataNode root,boolean saveNewTree){
		Object[]defaults=root.contents(),contents=null;
		String type=root.type(),title=root.title(),path=title+"."+type+".tree";
		while(contents==null)try{
			InputStream stream=FileNode.class.getClassLoader().getResource(path).openStream();
			contents=((TypedNode)new ObjectInputStream(stream).readObject()).contents();
			stream.close();
		}catch(Exception e){
			FileNode srcFolder=new FileNode(new File("_" +type));
			checkReadDirs=checkReadFiles=true;
			srcFolder.checkReadContents();
			checkReadDirs=checkReadFiles=false;
			contents=srcFolder.contents();
			if(saveNewTree&&srcFolder.file.exists())try{
				root.setContents(contents);
				serializeTree(root,Util.runDir());
			}catch(IOException i){
				contents=defaults;
			}
		}
		if(contents[0]instanceof ExceptionNode)contents=defaults;
		root.setContents(contents);
	}
	public static void serializeTree(TypedNode root,File dirPath)throws IOException{
		class SerialNode extends TypedNode{
			Object[]contents;	
			SerialNode(TypedNode parent,TypedNode node){
				super(Object.class,node.type(),node.title());
				setParent(parent);
				Object[]contents=node.contents();
				ItemList<Object>list=new ItemList(Object.class);
				for(int i=0;i<contents.length;i++)
					list.addItem(contents[i]instanceof TypedNode?
						new SerialNode(this,(TypedNode)contents[i]):contents[i]);
				this.contents=list.items();
			}
			public Object[]contents(){return contents;}
		}
	  File tree=new File(dirPath,root.title()+"." +root.type()+".tree");
		OutputStream stream=new FileOutputStream(tree);
		root.setParent(null);
	  new ObjectOutputStream(stream).writeObject(
	  		true?root:new SerialNode(null,root));    
	  stream.close();
	  Util.printOut("Stored ",tree.getAbsolutePath());
	}
	private void readObject(ObjectInputStream s)throws IOException, 
			ClassNotFoundException{
		s.defaultReadObject();
		flags=StringFlags.EMPTY;
	}
	final public static String FLAG_LIST=StringFlags.newFlag("List");
	private transient StringFlags flags=StringFlags.EMPTY;
	final public void clearFlags(){
		flags=StringFlags.EMPTY;
	}
	final public StringFlags flags(){
		return flags;
	}
	final public boolean hasFlag(String flag){
		return flags.includeFlag(flag);
	}
	final public void addFlag(String flag){
		flags=flags.addFlag(flag);
	}
	final public void removeFlag(String flag){
		flags=flags.removeFlag(flag);
	}
	public static String fileTitle(File file){
		String top=Strings.fileNameTop(file.getAbsoluteFile());
		if(top==null||top.equals(""))throw new IllegalStateException(
				"Null or empty fileName in "+Debug.info(file));
		else return top;
	}
	public static String fileType(File file){
	  if(file==null)throw new IllegalArgumentException("Null file");
	  if(file.isDirectory())return TYPE_FOLDER;
	  String tail=Strings.fileNameTail(file);
	  return tail.equals("")?TYPE_FILE:tail.replace(".","_");
	}
}
