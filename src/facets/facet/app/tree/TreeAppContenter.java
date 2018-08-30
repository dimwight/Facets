package facets.facet.app.tree;
import static facets.core.app.ActionViewerTarget.Action.*;
import static facets.facet.app.tree.TreeTargets.*;
import facets.core.app.ActionViewerTarget;
import facets.core.app.AppSurface;
import facets.core.app.AreaRoot;
import facets.core.app.NodeViewable;
import facets.core.app.PathSelection;
import facets.core.app.SAreaTarget;
import facets.core.app.SContentAreaTargeter;
import facets.core.app.SView;
import facets.core.app.SViewer;
import facets.core.app.ValueEdit;
import facets.core.app.ViewableAction;
import facets.core.app.ViewableFrame;
import facets.core.app.ViewerContenter;
import facets.core.app.ViewerTarget;
import facets.core.app.FeatureHost.LayoutFeatures;
import facets.core.superficial.Notice;
import facets.core.superficial.SFrameTarget;
import facets.core.superficial.STarget;
import facets.core.superficial.STrigger;
import facets.core.superficial.TargetCore;
import facets.core.superficial.Notifying.Impact;
import facets.core.superficial.app.FacetedTarget;
import facets.core.superficial.app.SSelection;
import facets.core.superficial.app.SelectionView;
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
import java.util.function.BiConsumer;
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
	private final FacetAppSurface app;
	private final TreeAppSpecifier treeSpec;
	private Object stateStamp=null;
	private NodeViewable viewable;
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
	private DataNode newSourceNode(Object source){
			DataNode node=(DataNode)source;
			if(true)source=Nodes.decode(new ValueNode(node.type(),node.title(),new Object[]{
	"789CAD523D681451101E37B772A7467249202A0A5B2A865DA34DE052199B8335161B45B8EADDDDDCB9E1DDEEFA76F63C530869626123A2581A248D70047F11B1B0D24A546CD28AD6013B110B9DB7B797DCA9659A61DE7BDF37DF3733AFBB0566A260AAE22E89B6B0FDD0F650F942FACBA22AB1B4F1A9D0F536B7CA064027020093A0E0852DB4083B142B38D21035A4D84EC897362944FB9290092E84759CCE1FFFF8F6FCCF379AAAE0F03FC0738284C64D6DDEB978FFD149CF803D15C8D7C28030A09860A267C99122683A17AA4B58A352E77F8A8BD723ACEB4A0F7FBF9BF95C31B952AE0C865F77616F2414572338EAF6688EA6399AE66CD34A2EE4880F04E3038A1E293F68F25BA1AD3B5A4C01C501C0BC1471AC1D1D1A74E491206C24723E5438F7A4F8EBFD88F3CD00C3053326D18AB8AD811265EEB5898A454CF249E255B801F94EC4732DA6DBD0203B038D7F5D5BFFB1727396C7540633F5C4DA633BB885A45545B5DABD776CFFDD2FB7FA0B7B4560EA264F71FE94E5737AECED2162AFD3B5EFEBB3CBA567675322FF886CFC3D4836FEDB1F2E3F188B4FC87E7123D68E47D238CAE71799D80CE71BFAF2800E451D26121D27F9214730BAFD85AC30B0D8145D0BA7090ED2155E8C152A0BDBA8EFF735C24459D20F30FE4BE965A6749AF3C7BBAB34F4C387555F67AA67387FDE1FE66E6BFE01079F200692030000"}));
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
		else tree=true?(DataNode)source:newSourceNode(source);
		final ValueNode state=app.spec.state();
		NodeViewable viewable=new NodeViewable(tree,app.ff.statefulClipperSource(false)){
			@Override
			protected SSelection newViewerSelection(SViewer viewer){
				return ((SelectionView)viewer.view()).newViewerSelection(viewer,selection());
			}
			@Override
			public ViewableAction[]viewerActions(SView view){
				return treeSpecifier().viewerActions(view);
			}
			@Override
			protected void viewerSelectionChanged(SViewer viewer,SSelection selection){
				if(!treeSpec.viewerSelectionChanged(this,viewer,selection))
					super.viewerSelectionChanged(viewer,selection);
				putSelectionState(state,STATE_OFFSETS);
			}
			@Override
			protected STarget[]lazyElements(){
				return !treeSpecifier().usesTreeTargets()?super.lazyElements() 
						:new TreeTargets(app,this).appTargets();
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
		return this.viewable=viewable;
	}
	@Override
	public void wasAdded(){
		if(false&&Debug.natureDebug)
			((STrigger)contentFrame().elements()[TARGET_SEARCH].elements()[0]).fire();
	}
	private TreeAppSpecifier treeSpecifier(){
		return (TreeAppSpecifier)app.spec;
	}
}
