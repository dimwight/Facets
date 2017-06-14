package applicable.avatar;
import static facets.core.app.ActionViewerTarget.Action.*;
import static facets.util.tree.DataConstants.*;
import facets.core.app.NodeViewable;
import facets.core.app.PathSelection;
import facets.core.app.TextView;
import facets.core.app.TreeView;
import facets.core.app.avatar.AvatarView;
import facets.core.superficial.app.SSelection;
import facets.core.superficial.app.SView;
import facets.core.superficial.app.SViewer;
import facets.core.superficial.app.ViewableAction;
import facets.util.Debug;
import facets.util.tree.DataNode;
import facets.util.tree.NodePath;
import facets.util.tree.Nodes;
import facets.util.tree.TypedNode;
import facets.util.tree.ValueNode;
public abstract class AvatarViewable extends NodeViewable{
	public AvatarViewable(String title,TypedNode[]items,ClipperSource clip){
		super(new ValueNode(TYPE_DATA,title,items),clip);
		defineSelection(framed);
	}
	@Override
	protected SSelection newViewerSelection(SViewer viewer){
		SSelection selection=selection();
		SView view=viewer.view();
		return view instanceof TreeView?selection:view instanceof AvatarView?
						((AvatarView)view).avatars().newAvatarSelection(viewer,selection())
				:new SSelection(){
			@Override
			public Object single(){
				StringBuilder text=new StringBuilder();
				for(TypedNode item:((TypedNode)framed).children())text.append(newItemText(item));
				return text.toString();
			}
			@Override
			public Object content(){
				throw new RuntimeException("Not implemented in "+Debug.info(this));
			}
			@Override
			public Object[]multiple(){
				throw new RuntimeException("Not implemented in "+Debug.info(this));
			}
		};
	}
	@Override
	protected void viewerSelectionEdited(SViewer viewer,Object edit,boolean interim){
		if(viewer.view()instanceof TextView){
			DataNode root=(DataNode)framed;
			TypedNode[]then=root.children();
			try{
				root.setChildren(newTextItems(((String)edit)));
				defineSelection(framed);
			}
			catch(Exception e){
				if(false)throw new RuntimeException(e);
				else trace(".viewerSelectionEdited: e=",e);
				root.setChildren(then);
			}
		}
		else((ValueNode)selection().single()).setState(((Object[])edit)[0]);
	}
	protected abstract String newItemText(TypedNode data);
	protected abstract TypedNode[]newTextItems(String text);
	@Override
	protected void viewerSelectionChanged(SViewer viewer,SSelection selection){
		if(viewer.view()instanceof TreeView)super.viewerSelectionChanged(viewer,selection);
		else if(selection.single()instanceof SView)
			setSelection(PathSelection.newMinimal(selection().content()));
		else for(TypedNode values:tree().children())
			if(selection.single().equals(values))setSelection(new PathSelection(framed,
						new NodePath(Nodes.ancestry(values))));
	}
	@Override
	public ViewableAction[]viewerActions(SView view){
		return view instanceof TextView?new ViewableAction[]{}
				:new ViewableAction[]{ITERATE_BACK,ITERATE_FORWARD,COPY,CUT,PASTE,DELETE,EDIT};
	}
	@Override
	public boolean actionIsLive(SViewer viewer,ViewableAction action){
		SView view=viewer.view();
		return view instanceof TextView?false
			:action==ITERATE_BACK||action==ITERATE_FORWARD?view instanceof AvatarView
			:action==EDIT||action==COPY||action==PASTE
				||tree().children().length>1?super.actionIsLive(viewer,action)
			:false;
	}
}