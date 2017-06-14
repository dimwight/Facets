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
import facets.util.tree.DataNode;
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
	private final static ViewableAction[]EDIT_ACTIONS={COPY,CUT,PASTE,PASTE_INTO,DELETE,EDIT};
	private final FacetAppSurface app;
	private final TreeAppSpecifier treeSpec;
	private Object stateStamp=null;
	TreeAppContenter(Object source,FacetAppSurface app){
		super(source);
		this.app=app;
		treeSpec=(TreeAppSpecifier)app.spec;
	}
	@Override
	protected ViewableFrame newContentViewable(Object source){
		DataNode tree=null;
		if(source instanceof File){
			File file=(File)source;
			for(FileSpecifier fileType:app.getFileSpecifiers())
				if(fileType.specifies(file))try{
					XmlSpecifier xml=(XmlSpecifier)fileType;
					TreeRoot root=xml.newTreeRoot(xml.newRootNode(file));
					root.readFromSource(file);
					tree=root.tree;
					stateStamp=tree.stateStamp();
					break;
				}catch(IOException e){
					throw new RuntimeException(e);
				}
			if(tree==null)throw new IllegalStateException("Bad file type in "+file);
		}
		else tree=(DataNode)source;
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
		return app.actions instanceof FileAppActions
			&&((Stateful)contentFrame().framed).stateStamp()!=stateStamp;
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
		String sinkName=file.getName();
		if(file.exists()&&!sinkName.startsWith("_"))
			new TextLines(file).copyFile("_"+sinkName);
		SFrameTarget frame=contentFrame();
		DataNode root=(DataNode)frame.framed;
		new XmlDocRoot(root,treeSpec.xmlPolicy()).writeToSink(file);
		stateStamp=root.updateStateStamp();
		root.setTitle(Strings.fileNameTop(file));
		app.notify(new Notice(frame,Impact.DEFAULT));
	}
	@Override
	public void wasAdded(){
		if(false&&Debug.natureDebug)
			((STrigger)contentFrame().elements()[TARGET_SEARCH].elements()[0]).fire();
	}
}
