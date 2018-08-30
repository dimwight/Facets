package facets.facet.kit.swing.tree;
import static facets.util.tree.DataConstants.*;
import facets.core.app.SViewer;
import facets.core.app.TreeView;
import facets.facet.kit.swing.KitSwing;
import facets.facet.kit.swing.ViewerMaster;
import facets.util.Debug;
import facets.util.Util;
import facets.util.tree.DataConstants;
import facets.util.tree.TypedNode;
import java.awt.Font;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
abstract class PathNodePaneMaster extends ViewerMaster{
	private static final String findInfoTitle="(#\\d+\\s+\\d+).*",fixInfoTitle="$1 ";
	public String toString(){
		return Debug.info(this)+"\n(" +Debug.info(viewerTarget());
	}
	protected void modifyNodeValueRendering(Object value,JLabel label){
  	SViewer viewerTarget=viewerTarget();
		TreeView view=(TreeView)viewerTarget.view();
	  if(value instanceof TypedNode){
			TypedNode node=(TypedNode)value;
			label.setText(TreeView.debug?
					(node.type()+" "+Debug.info(node).replaceAll(findInfoTitle,fixInfoTitle))
					:view.nodeRenderText(node));
		}
		String key=view.contentIconKey(value);
	  KitSwing kit=base().kit;
		Object icon=key==null?null:kit.getDecorationIcon(key,false);
	  if(icon==null)
	  	icon=kit.getDecorationIcon(value instanceof TypedNode?
	  			TYPE_DATA:VALUE,false);
	  if(true&&(key==null||key.equals(NO_ICON)||!(value instanceof TypedNode)))
	  	label.setIcon(null);
		else if(icon!=null)label.setIcon((Icon)icon);
	  if(false&&value instanceof String){
	  	if(false)label.setText("");
			label.setText(value.toString());//.replaceAll("^("+RENDER_FUDGE+"\\d+)",""));
		}
	}
}
