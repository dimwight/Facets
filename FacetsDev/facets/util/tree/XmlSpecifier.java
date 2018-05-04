package facets.util.tree;
import facets.util.Debug;
import facets.util.FileSpecifier;
import facets.util.TextLines;
import facets.util.tree.Nodes.TreeRoot;
import java.io.File;
/**
{@link FileSpecifier} with attached {@link XmlPolicy}. 
 */
public class XmlSpecifier extends FileSpecifier{
	public final XmlPolicy policy;
	public final String dataType;
	public XmlSpecifier(String extension,String description,XmlPolicy policy){
		super(extension,description);
		this.policy=policy;
		if(!extension.contains(DataConstants.TYPE_XML))throw new IllegalArgumentException(
				"Non-xml extension " +extension+" in "+Debug.info(this));
		final String[]raw=extension.replace(TextLines.EXT_ZIP,"").split("\\.");
		StringBuilder safe=new StringBuilder(raw[0]);
		for(int i=1;i<raw.length;i++)
			raw[i]=raw[i].substring(0,1).toUpperCase()+raw[i].substring(1);
		dataType=safe.toString();
	}
	/**
	Convenience method creating a temporary node for use as {@link TreeRoot#tree}. 
	<p>Instances supplies {@link #dataType} and {@link XmlPolicy}
	@param data the data tree to be wrapped
	@return an {@link XmlDocRoot} with {@link TreeRoot#tree} of type {@link #dataType}, 
	titled from <code>data</code> which is set as its single child
	 */
	final public XmlDocRoot newTreeRoot(ValueNode data){
		return new XmlDocRoot(policy.treeAsXmlRoot()?data
				:new ValueNode(dataType,data.title(),new Object[]{data}),policy);
	}
	final public File newFile(File dir,String nameTop){
		return new File(dir,nameTop+dot+extension);
	}
	final public ValueNode newRootNode(File file){
		return new ValueNode(dataType,file.getName().replaceFirst(_dot+".+",""));
	}
}
