package facets.facet.app.tree;
import static facets.core.app.ActionViewerTarget.Action.*;
import static facets.facet.app.tree.TreeTargets.*;
import facets.core.app.ActionViewerTarget;
import facets.core.app.AppSurface;
import facets.core.app.AreaRoot;
import facets.core.app.NodeViewable;
import facets.core.app.PathSelection;
import facets.core.app.ValueEdit;
import facets.core.app.ViewerContenter;
import facets.core.app.FeatureHost.LayoutFeatures;
import facets.core.superficial.FacetedTarget;
import facets.core.superficial.Notice;
import facets.core.superficial.SFrameTarget;
import facets.core.superficial.STarget;
import facets.core.superficial.STrigger;
import facets.core.superficial.TargetCore;
import facets.core.superficial.Notifying.Impact;
import facets.core.superficial.app.SAreaTarget;
import facets.core.superficial.app.SContentAreaTargeter;
import facets.core.superficial.app.SSelection;
import facets.core.superficial.app.SView;
import facets.core.superficial.app.SViewer;
import facets.core.superficial.app.SelectionView;
import facets.core.superficial.app.ViewableAction;
import facets.core.superficial.app.ViewableFrame;
import facets.core.superficial.app.ViewerTarget;
import facets.facet.AreaFacets;
import facets.facet.app.FacetAppSpecifier;
import facets.facet.app.FacetAppSurface;
import facets.facet.app.FileAppActions;
import facets.util.Debug;
import facets.util.FileSpecifier;
import facets.util.Stateful;
import facets.util.Strings;
import facets.util.TextLines;
import facets.util.Util;
import facets.util.tree.DataNode;
import facets.util.tree.Nodes;
import facets.util.tree.TypedNode;
import facets.util.tree.ValueNode;
import facets.util.tree.XmlDocRoot;
import facets.util.tree.XmlSpecifier;
import facets.util.tree.Nodes.TreeRoot;
import java.io.File;
import java.io.IOException;
/**
{@link ViewerContenter} that manages content for {@link TreeAppSpecifier}. 
<p>Effectively a package-private class; 
declared <code>public</code> for documentation purposes only. 
<p>The code exemplifies 'filling out' the <code>abstract</code> {@link ViewerContenter} 
to provide real-world functionality.  
 */
public final class TreeAppContenter extends ViewerContenter{
	/** Index into return of {@link #lazyContentAreaElements(SAreaTarget)}. */
	public static final int TARGETS_PANE=0,TARGETS_CONTENT=1;
	public static final String STATE_OFFSETS="selectionOffsets";
	private final static ViewableAction[]EDIT_ACTIONS=
		{COPY,CUT,PASTE,PASTE_INTO,DELETE,MODIFY,UNDO,REDO};
	private final FacetAppSurface app;
	private final TreeAppSpecifier treeSpec;
	private Object stateStamp=null;
	TreeAppContenter(Object source,FacetAppSurface app){
		super(source);
		this.app=app;
		treeSpec=(TreeAppSpecifier)app.spec;
	}
	/**
	Delegates to {@link TreeAppSpecifier} 
	 */
	@Override
	protected FacetedTarget[]newContentViewers(ViewableFrame viewable){
		return ActionViewerTarget.newViewerAreas(viewable,ViewerTarget.newViewFrames(
			treeSpec.newContentViews((NodeViewable)viewable)
		));
	}
	@Override
	protected void attachContentAreaFacets(AreaRoot area){
		app.ff.areas().attachViewerAreaPanes(area,"",AreaFacets.PANE_SPLIT_VERTICAL);
	}
	@Override
	public LayoutFeatures newContentFeatures(SContentAreaTargeter area){
		return new TreeAppFeatures(app,area);
	}
	/**
	Re-implementation returning pane and extra tree menu targets. 
	@return {@link STarget}<code>[]</code> indexable by {@link #TARGETS_PANE}
	and {@link #TARGETS_CONTENT}; the latter created in 
		{@link TreeAppSpecifier#newContentRootTargets(FacetAppSurface)}
	 */
	@Override
	public STarget[]lazyContentAreaElements(SAreaTarget area){
		return new STarget[]{
				app.ff.areas().panesGetTarget(area),
				new TargetCore("TreeMenuAdjustmentTargets",
						treeSpec.newContentRootTargets(app)),
		};
	}
	@Override
	public boolean hasChanged(){
		Object framedStamp=((Stateful)contentFrame().framed).stateStamp();
		boolean changed=framedStamp!=stateStamp;
		if(false&&changed)trace(".hasChanged: framedStamp=",framedStamp);
		return (false||app.actions instanceof FileAppActions)&&changed;
	}
	@Override
	public FileSpecifier[]sinkFileSpecifiers(){
		Object sink=sink();
		String name=sink instanceof File?((File)sink).getName()
				:((TypedNode)sink).title()+"."+((TypedNode)sink).type();
		return FileSpecifier.filterByName(app.getFileSpecifiers(),name);
	}
	@Override
	public boolean setSink(Object sink){
		if(sink instanceof File&&((File)sink).getName().startsWith("_")||
			!app.spec.canOverwriteContent())return false;
		else return super.setSink(sink);
	}
	@Override
	public void saveToSink(Object sink)throws IOException{
		File file=(File)sink;
		String name=file.getName();
		if(file.exists()&&!name.startsWith("_"))
			new TextLines(file).copyFile("_"+name);
		SFrameTarget frame=contentFrame();
		DataNode tree=(DataNode)frame.framed;
		tree.setTitle(name);
		tree.setValidType("File");
		new XmlDocRoot(tree,treeSpec.xmlPolicy()).writeToSink(file);
		stateStamp=tree.updateStateStamp();
		if(false)trace(".setSink: area="+Debug.info(app.activeContentTargeter().target().title())
				+ "\nfile="+name);
		app.notify(new Notice(frame,Impact.DEFAULT));
	}
	private static DataNode newSourceNode(Object source){
			DataNode node=(DataNode)source;
			if(true)source=Nodes.decode(new DataNode(node.type(),node.title(),new Object[]{
	"789CB592B16FD35010C6AF6E0C2D0594A4430181E411446553582AA513658964CAE0829032BD2497E0EAC5CF3C9F43E880D4A50C2C508A18A9501744A884F8039860428058BA22982BB121C400F7628BA6C008CBE9FCFC7DF7BBBBF7FA3B60A71AA66AFE92E80A37546E803A14325C16758995ADF7E3FD607BA76A01F46200B0120DC75AA28194B82985D2258DE85E1132C505D5C4E9B193EF5E5DFCF6D2C8351CFD4378419030BAA9EDB5CB0F9F9C0E2C18A9C15843458411250493591B9E1451DBBB545FC206557A7F232EDE8CB1692A3DFEF17AE643CDE64A852A5861D3877DB1D05C8DE0B89FD93C63F38CCDFB65ABF85020FE20280F1103D261D4E67FE35D33D1E240501A12CC4B9124A6A323C31D0524085BA99C571AE79E97BEBF19F53E5B60F96027243A318F3554A2CAB3B65133C4A690245E875BB0BF17F35E4B831B30223717953F6D6C7E5DB93DCB6BAA823DE889D9C55DDD42DAA9A35EED3F3831B1FEF14E7E4923CF086C33E419CEEF33BE60D6DEDD63CC26DDF8B239BB5C79717E60E45790AF3F93E4EBBFFBF6EAA362724AEEBE00D371160F32E0690E9BE1FC9E399C30A16842393571924D05824381EAA043D82347450E374537D434C161BAC617E328ED6017CDF981964AB523C30893DF48FD9C7496F3B5FF4ADACA49E7385F8FFF19E5277EB2127D6C030000\r\n"}));
			return new DataNode("Created",node.title(),new Object[]{source});
		}
	@Override
	protected ViewableFrame newContentViewable(Object source){
		DataNode tree=null;
		if(source instanceof File){
			File file=(File)source;
			for(FileSpecifier fileType:app.getFileSpecifiers()){
				if(!fileType.specifies(file))continue;
				XmlSpecifier xml=(XmlSpecifier)fileType;
				XmlDocRoot root=xml.newTreeRoot(xml.newRootNode(file));
				root.readFromSource(file);
				tree=root.tree;
				tree.setTitle(file.getName());
				tree.setValidType("File");
				stateStamp=tree.stateStamp();
				break;
			}
			if(tree==null)throw new IllegalStateException("Bad file type in "+file);
		}
		else tree=newSourceNode(source);
		final ValueNode state=app.spec.state();
		NodeViewable viewable=new NodeViewable(tree,app.ff.statefulClipperSource(false)){
			@Override
			protected SSelection newViewerSelection(SViewer viewer){
				return ((SelectionView)viewer.view()).newViewerSelection(viewer,selection());
			}
			@Override
			public ViewableAction[]viewerActions(SView view){
				return view.isLive()?EDIT_ACTIONS:new ViewableAction[]{};
			}
			@Override
			protected void viewerSelectionChanged(SViewer viewer,SSelection selection){
				super.viewerSelectionChanged(viewer,selection);
				putSelectionState(state,STATE_OFFSETS);
			}
			@Override
			protected STarget[]lazyElements(){
				return new TreeTargets(app,this).appTargets();
			}
			@Override
			public boolean editSelection(){
				return new ValueEdit(((PathSelection)selection())){
					protected String getDialogInput(String title,String rubric,
							String proposal){
						return app.dialogs().getTextInput(title,rubric,proposal, 0);
					}
				}.dialogEdit();
			}
		};
		viewable.readSelectionState(state,STATE_OFFSETS);
		return viewable;
	}
	@Override
	public void wasAdded(){
		if(false&&Debug.natureDebug)
			((STrigger)contentFrame().elements()[TARGET_SEARCH].elements()[0]).fire();
	}
}
