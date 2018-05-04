package facets.facet.kit.swing.tree;
import facets.util.Debug;
import facets.util.OffsetPath;
import facets.util.Titled;
import facets.util.Tracer;
import facets.util.Util;
import facets.util.app.ProvidingCache;
import facets.util.app.ProvidingCache.ItemProvider;
import facets.util.tree.NodePath;
import facets.util.tree.Nodes;
import facets.util.tree.TypedNode;
import java.awt.Insets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
final class MasterPathTree extends OffsetPathTree{
	final PathTreePaneMaster master;
	private final Map<String,OffsetPath[]>pathStore=new HashMap();
	@Override
	protected void putOffsets(OffsetPath[]offsets){
		pathStore.put(nowKey(),offsets);
	}
	@Override
	protected OffsetPath[]getOffsets(){
		return pathStore.get(nowKey());
	}
	private String nowKey(){
		TypedNode root=(TypedNode)getModel().getRoot();
		return root.type()+"|"+root.title();
	}
	MasterPathTree(PathTreePaneMaster master,boolean multiples){
		super(master.model);
		this.master=master;
		setShowsRootHandles(true);
		getSelectionModel().setSelectionMode(
				multiples?TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION
						:TreeSelectionModel.SINGLE_TREE_SELECTION);
	}
	public String convertValueToText(Object value,boolean selected,
			boolean expanded,boolean leaf,int row,boolean hasFocus){
		return super.convertValueToText(value instanceof Titled?((Titled)value).title():value,
				selected,expanded,leaf,row,hasFocus);
	}
	public OffsetPath newOffsetPath(TreePath path){
		Object last=path.getLastPathComponent();
		TreePath nodePath=last instanceof TypedNode?path:path.getParentPath();
		TypedNode lastNode=(TypedNode)nodePath.getLastPathComponent(),
			ancestry[]=Nodes.ancestry(lastNode);
		NodePath ancestryPath=(NodePath)new NodePath(ancestry
				).procrusted(ancestry[0],path.getPathComponent(0));
		t.trace(".newEventOffsetPath: nodePath==path=",nodePath==path);
		if(nodePath==path)return ancestryPath;
		int rowAt=getRowForPath(nodePath)+1,valueAt=getRowForPath(path),pathAt=-1;
		for(;rowAt<=valueAt;rowAt++){
			TreePath rowPath=getPathForRow(rowAt);
			boolean underNodePath=nodePath.equals(rowPath.getParentPath()),
				isValue=rowPath.getLastPathComponent()instanceof StringBuilder;
			t.trace(".newOffsetPath: underNodePath="+underNodePath+
					" isValue="+isValue);
			if(underNodePath&&isValue)pathAt++;
		}
		t.trace(".~newOffsetPath: pathAt="+pathAt+" valueAt="+valueAt
				+" rowAt="+rowAt);
		return ancestryPath.valueAtChecked(pathAt);
	}
	public void addOffsetPath(OffsetPath offsets){
		TreePath nodePath=new TreePath(offsets.members(getModel().getRoot()));
		int pathValueAt=((NodePath)offsets).valueAt();
		if(pathValueAt>=0){
			expandPath(nodePath);
			int rowAt=getRowForPath(nodePath)+1,valueAt=-1;
			for(;valueAt<pathValueAt;rowAt++)try{
				TreePath rowPath=getPathForRow(rowAt);
				boolean underNodePath=nodePath.equals(rowPath.getParentPath()),
					isValue=rowPath.getLastPathComponent()instanceof StringBuilder;
				t.trace(".setOffsetPath: underNodePath="+
						underNodePath+" isValue="+isValue);
				if(underNodePath&&isValue)valueAt++;
			}
			catch(Exception e){
				t.trace(".setOffsetPath: e=",e);
				return;
			}
			t.trace(".~setOffsetPath: pathAt="+pathValueAt
					+" valueAt="+valueAt+" rowAt="+rowAt);
			nodePath=getPathForRow(rowAt-1);
		}
		addSelectionPath(nodePath);
		if(isShowing())scrollPathToVisible(nodePath);
	}
}