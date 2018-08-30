package facets.core.app;
import static facets.util.tree.Nodes.*;
import facets.util.ItemList;
import facets.util.Objects;
import facets.util.Regex;
import facets.util.Util;
import facets.util.tree.NodePath;
import facets.util.tree.Nodes;
import facets.util.tree.TypedNode;
/**
{@link TreeView} that converts {@link TypedNode}s to (typically HTML) text. 
 */
public class TextTreeView extends TreeView{
	public TextTreeView(String title){
		super(title);
	}
	final public String nodeRenderText(TypedNode node){
		return newPathNodeText(Nodes.ancestry(node),nodeText(node));
	}
	public static String nodeText(TypedNode node){
		return Objects.toLines(node.values());
	}
	protected String newPathNodeText(TypedNode[]path,String text){
		return text;
	}
	/**
	Defines a {@link PathSelection} based on an HTML path. 
	@param selectionThen identifies the HTML page containing the path
	@param path value of HREF attribute within the page 
	 */
	public static PathSelection newHtmlPathSelection(PathSelection selectionThen,String path){
		Object content=selectionThen.content(),
			membersThen[]=selectionThen.paths[0].members(content);
		TypedNode focus=(TypedNode)membersThen[membersThen.length-
				Regex.finds(path,"\\Q../\\E").length-2];
		ItemList<TypedNode>membersNow=new ItemList(TypedNode.class);
		if(path.startsWith("/")){
			path=path.substring(1);
			focus=(TypedNode)content;
		}
		if(false)Util.printOut("TextTreeView.newHtmlPathSelection: path="+path+" focus=",focus);
		membersNow.addItems(ancestry(focus));
		String[]downs=("../"+path).substring(("../"+path).lastIndexOf("../")+3).split("/");
		for(String down:downs){
			TypedNode found=null;
			String title=down.replaceAll("\\..*","");
			for(TypedNode child:focus.children())if(child.title().equals(title))found=child;
			if(found==null)throw new IllegalStateException("Not found "+title+" in "+focus);
			else membersNow.addItem(focus=found);
		}
		return false?selectionThen:new PathSelection(content,new NodePath(membersNow.items()));
	}
}