package applicable.treecode;
import static facets.util.tree.TypedNode.*;
import facets.util.Debug;
import facets.util.Objects;
import facets.util.Regex;
import facets.util.Titled;
import facets.util.Tracer;
import facets.util.tree.TypedNode;
import facets.util.tree.Nodes;
import facets.util.tree.TypedNode;
import java.util.Comparator;
/**
Wraps a {@link TypedNode} containing its code and has a (possibly empty) {@link Label}. 
 */
public abstract class TreeCoded extends Tracer implements Titled,Comparable<TreeCoded>{
	/**
	Identifies a {@link TreeCoded}. 
	<p>As well as carrying general-purpose text, a {@link Label} can identify either 
	the {@link TreeCoded} that returns it, or
	another to be referenced within a {@link TreeCodeContext}. 
	</ul>
	 */
	public static final class Label{
		public static final Label NONE=new Label(TypedNode.UNTITLED);
		public final String text;
		public Label(String text){
			this.text=text;
		}
		@Override
		public int hashCode(){
			return text.hashCode();
		}
		@Override
		public boolean equals(Object that){
			return text.equals(((Label)that).text);
		}
		@Override
		public String toString(){
			return Debug.info(this)+" text="+text;
		}
		public boolean isRem(){
			return text.startsWith("'");
		}
	}
	public final TypedNode source;
	public final Label label;
	private final TreeCodeType treeType;
	private final String ancestry;
	@Override
	protected void traceOutput(String msg){
		super.traceOutput((false?" ["+ancestry+"]":" "+title())+msg);
	}
	protected TreeCoded(TypedNode source,TreeCodeType type){
		if(source==null)throw new IllegalArgumentException(
				"Null source for "+type);
		String sourceType=source.type();
		if(false&&!type.name.equals(sourceType))throw new IllegalArgumentException(
				"Invalid source type="+sourceType+" for type="+type);
		this.source=source;
		this.treeType=type;
		String title=source.title();
		label=new Label(title);
		ancestry=Regex.replaceAll(Objects.toString(Nodes.ancestry(source),">\n\t"),
				"No values","","#\\d+","",
				"(?s)^.*Form\\s*>\\s*","",
				"","").trim();
	}
	@Override
	final public String title(){
		return label.text;
	}
	@Override
	public String toString(){
		return source.type()+" "+title();
	}
	@Override
	public int compareTo(TreeCoded q){
		int byType=treeType.name.compareTo(q.treeType.name);
		return byType!=0?byType:label.text.compareTo(q.label.text);
	}
}
