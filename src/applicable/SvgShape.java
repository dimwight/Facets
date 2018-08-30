package applicable;
import facets.core.app.avatar.PainterMaster.Outlined;
import facets.util.FileSpecifier;
import facets.util.Titled;
import facets.util.Tracer;
import facets.util.shade.Shade;
import facets.util.tree.DataNode;
import facets.util.tree.ValueNode;
import facets.util.tree.XmlPolicy;
public abstract class SvgShape extends Tracer implements Titled{
	final public static String TYPE_SVG="svg",KEY_ID="id";
	public static final XmlPolicy XML_POLICY=new XmlPolicy(){
		protected boolean treeAsXmlRoot(){
			return true;
		}
		protected ValueNode getTitleAttributeNames(){
			return newTitleAttributeNames("id",new String[]{"style=type"});
		}
		protected boolean isSegregated(DataNode node){
			return node.parent().type().equals("text");
		}
	};
	public final static FileSpecifier file=new FileSpecifier("svg","Scalable Vector Graphics");
	public final ValueNode values;
	protected SvgShape(ValueNode values){
		this.values=values;
	}
	public String title(){
		return values.title();
	}
	public abstract Outlined newOutlined(Shade fill,Shade pen,boolean pickable);
}
