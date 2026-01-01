package applicable.treetext;
import applicable.treeplus.TreePlusFeatures;
import facets.core.app.ActionViewerTarget;
import facets.core.app.FeatureHost.LayoutFeatures;
import facets.core.app.NodeViewable;
import facets.core.app.SAreaTarget;
import facets.core.app.SContentAreaTargeter;
import facets.core.app.SView;
import facets.core.app.TreeView;
import facets.core.app.ViewableFrame;
import facets.core.app.ViewerTarget;
import facets.core.superficial.Notice;
import facets.core.superficial.Notifying.Impact;
import facets.core.superficial.SFrameTarget;
import facets.core.superficial.STarget;
import facets.core.superficial.TargetCore;
import facets.core.superficial.app.FacetedTarget;
import facets.facet.app.FacetAppSurface;
import facets.facet.app.FileAppActions;
import facets.facet.app.tree.TreeAppContenter;
import facets.facet.app.tree.TreeAppSpecifier;
import facets.util.Debug;
import facets.util.FileSpecifier;
import facets.util.Stateful;
import facets.util.TextLines;
import facets.util.tree.DataNode;
import facets.util.tree.NodeList;
import facets.util.tree.Nodes;
import facets.util.tree.TypedNode;
import facets.util.tree.ValueNode;

import java.io.File;
import java.io.IOException;
public abstract class TreePlusContenter extends TreeAppContenter {
	public static final int TARGETS_PANE=0;
	public static final String STATE_OFFSETS="selectionOffsets";

	private Object stateStamp=null;
	private NodeViewable viewable;
	public TreePlusContenter(Object source, FacetAppSurface app){
		super(source, app);
	}

	protected static int contents;
	@Override
	final protected ViewableFrame newContentViewable(Object source){
		String[]lines;
		DataNode tree;
		File file=null;
		if(source instanceof File){
			file=(File)source;
			try{
				lines=new TextLines(file).readLines();
			}catch(IOException e){
				throw new RuntimeException(e);
			}
		}
		else lines=(String[])source;
		NodeList content=new NodeList(new ValueNode("TextTree",TypedNode.UNTITLED),
				false);
		for(String line:lines)
			content.add(new ValueNode(TreeTextViewable.TYPE_LINE,new Object[]{line}));
		content.updateParent();
		String title=file!=null?file.getName().replaceAll("\\..*","")
				:("Content"+contents++);
		tree=new ValueNode("xml",title,new Object[]{content.parent});
		stateStamp=tree.stateStamp();
		viewable= new TreeTextViewable(tree, app.ff.statefulClipperSource(false), app) {
		};
		viewable.readSelectionState(app.spec.state(),STATE_OFFSETS);
		return viewable;
	}
	@Override
	final public void saveToSink(Object sink)throws IOException{
		File file=(File)sink;
		String name=file.getName();
		if(file.exists()&&!name.startsWith("_"))
			new TextLines(file).copyFile("_"+name);
		SFrameTarget frame=contentFrame();
		DataNode tree=(DataNode)frame.framed;
		tree.setTitle(name);
		if(false)tree.setValidType(tree.type());
		StringBuilder lines=new StringBuilder();
		for(TypedNode node:Nodes.descendantsTyped(tree, TreeTextViewable.TYPE_LINE))
			lines.append(node.values()[0]+"\n");
		new TextLines(file).writeLines(lines.toString().split("\n"));
		stateStamp=tree.updateStateStamp();
		if(false)trace(".setSink: area="+Debug.info(app.activeContentTargeter().target().title())
				+ "\nfile="+name);
		app.notify(new Notice(frame,Impact.DEFAULT));
	}
	@Override
	final protected FacetedTarget[]newContentViewers(ViewableFrame viewable){
		TreeView tree=((TreeTextViewable)viewable).debugView;
		return ActionViewerTarget.newViewerAreas(viewable,
				newViewTargets(tree,tree.isLive()));
	}
	@Override
	final public STarget[]lazyContentAreaElements(SAreaTarget area){
		return new STarget[]{
				app.ff.areas().panesGetTarget(area),
				new TargetCore("ContentRootTargets",newContentRootTargets())
		};
	}

	@Override
	final public LayoutFeatures newContentFeatures(SContentAreaTargeter area){
		return newFeatures(area);
	}

	protected SFrameTarget[] newViewTargets(TreeView debugTree,boolean liveViews){
		return ViewerTarget.newViewFrames(new SView[]{debugTree,new TreeTextViewable.TextLineView("Text",liveViews)});
	}
	protected STarget[]newContentRootTargets(){
		return new STarget[]{};
	}
	protected TreePlusFeatures newFeatures(SContentAreaTargeter area){
		return new TreePlusFeatures(app,area);
	}
	@Override
	public boolean hasChanged(){
		Object framedStamp=((Stateful)contentFrame().framed).stateStamp();
		boolean changed=framedStamp!=stateStamp;
		if(false&&changed)trace(".hasChanged: framedStamp=",framedStamp);
		return(app.actions instanceof FileAppActions)&&changed;
	}
	@Override
	public FileSpecifier[]sinkFileSpecifiers(){
		Object sink=sink();
		String name=sink instanceof File?((File)sink).getName()
				:((TypedNode)sink).title()+"."+((TypedNode)sink).type();
		return FileSpecifier.filterByName(((TreeAppSpecifier) app.spec).fileSpecifiers(),name);
	}
}
