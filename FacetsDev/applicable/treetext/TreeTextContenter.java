package applicable.treetext;
import static facets.util.tree.DataConstants.*;
import facets.core.app.ActionViewerTarget;
import facets.core.app.AreaRoot;
import facets.core.app.FeatureHost.LayoutFeatures;
import facets.core.app.NodeViewable;
import facets.core.app.SAreaTarget;
import facets.core.app.SContentAreaTargeter;
import facets.core.app.SView;
import facets.core.app.SViewer;
import facets.core.app.TextView;
import facets.core.app.TreeView;
import facets.core.app.ViewableFrame;
import facets.core.app.ViewerContenter;
import facets.core.app.ViewerTarget;
import facets.core.superficial.Notice;
import facets.core.superficial.Notifying.Impact;
import facets.core.superficial.SFrameTarget;
import facets.core.superficial.STarget;
import facets.core.superficial.TargetCore;
import facets.core.superficial.app.FacetedTarget;
import facets.core.superficial.app.SSelection;
import facets.facet.AreaFacets;
import facets.facet.app.FacetAppSurface;
import facets.facet.app.FileAppActions;
import facets.util.Debug;
import facets.util.FileSpecifier;
import facets.util.Stateful;
import facets.util.TextLines;
import facets.util.tree.DataNode;
import facets.util.tree.TypedNode;
import facets.util.tree.ValueNode;
import facets.util.tree.XmlDocRoot;
import facets.util.tree.XmlPolicy;
import facets.util.tree.XmlSpecifier;
import java.io.File;
import java.io.IOException;
public abstract class TreeTextContenter extends ViewerContenter{
	public static final int TARGETS_PANE=0,TARGETS_CONTENT=1;
	public static final String STATE_OFFSETS="selectionOffsets";
	public class TreeTextView extends TextView{
		private final boolean canEdit;
		public TreeTextView(String title, boolean canEdit){
			super(title);
			this.canEdit=canEdit;
		}
		@Override
		public boolean isLive(){
			return canEdit;
		}
		@Override
		public SSelection newViewerSelection(SViewer viewer,SSelection viewable){
			return viewable;
		}
	}
	protected final FacetAppSurface app;
	private final XmlPolicy xmlPolicy;
	private Object stateStamp=null;
	private NodeViewable viewable;
	public TreeTextContenter(Object source,FacetAppSurface app){
		super(source);
		this.app=app;
		xmlPolicy=textTreeSpec().xmlPolicy;
	}
	private TreeTextSpecifier textTreeSpec(){
		return (TreeTextSpecifier)app.spec;
	}
	@Override
	final protected ViewableFrame newContentViewable(Object source){
		DataNode tree=null;
		if(source instanceof File){
			File file=(File)source;
			for(FileSpecifier fileType:xmlPolicy.fileSpecifiers()){
				if(!fileType.specifies(file))continue;
				XmlSpecifier xml=(XmlSpecifier)fileType;
				XmlDocRoot root=xml.newTreeRoot(xml.newRootNode(file));
				root.readFromSource(file);
				tree=root.tree;
				tree.setTitle(file.getName());
				tree.setValidType("xml");
				stateStamp=tree.stateStamp();
				break;
			}
			if(tree==null)throw new IllegalStateException("Bad file type in "+file);
		}
		else tree=(DataNode)source;
		final ValueNode state=app.spec.state();
		NodeViewable viewable=newViewable(tree);
		viewable.readSelectionState(state,STATE_OFFSETS);
		return this.viewable=viewable;
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
	protected void attachContentAreaFacets(AreaRoot area){
		app.ff.areas().attachViewerAreaPanes(area,"",AreaFacets.PANE_SPLIT_VERTICAL);
	}
	@Override
	final public LayoutFeatures newContentFeatures(SContentAreaTargeter area){
		return newFeatures(area);
	}
	protected TreeTextViewable newViewable(DataNode tree){
		return new TreeTextViewable(tree,app.ff.statefulClipperSource(false),app){};
	}
	protected SFrameTarget[] newViewTargets(TreeView debugTree,boolean liveViews){
		return ViewerTarget.newViewFrames(new SView[]{debugTree,new TreeTextView("Text",liveViews)});
	}
	protected STarget[]newContentRootTargets(){
		return new STarget[]{};
	}
	protected TreeTextFeatures newFeatures(SContentAreaTargeter area){
		return new TreeTextFeatures(app,area);
	}
	@Override
	public boolean hasChanged(){
		Object framedStamp=((Stateful)contentFrame().framed).stateStamp();
		boolean changed=framedStamp!=stateStamp;
		if(false&&changed)trace(".hasChanged: framedStamp=",framedStamp);
		return(app.actions instanceof FileAppActions)&&changed;
	}
	@Override
	final public FileSpecifier[]sinkFileSpecifiers(){
		Object sink=sink();
		String name=sink instanceof File?((File)sink).getName()
				:((TypedNode)sink).title()+"."+((TypedNode)sink).type();
		return FileSpecifier.filterByName(xmlPolicy.fileSpecifiers(),name);
	}
	@Override
	final public boolean setSink(Object sink){
		 return sink instanceof File&&((File)sink).getName().startsWith("_")||
			!app.spec.canOverwriteContent()?false
		:super.setSink(sink);
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
		tree.setValidType(tree.type());
		new XmlDocRoot(tree,xmlPolicy).writeToSink(file);
		stateStamp=tree.updateStateStamp();
		if(false)trace(".setSink: area="+Debug.info(app.activeContentTargeter().target().title())
				+ "\nfile="+name);
		app.notify(new Notice(frame,Impact.DEFAULT));
	}
}
