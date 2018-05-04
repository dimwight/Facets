package applicable.eval.app;
import static applicable.eval.app.EvalFormViewer.*;
import static facets.core.app.ActionViewerTarget.Action.*;
import facets.core.app.ActionViewerTarget;
import facets.core.app.AreaRoot;
import facets.core.app.FeatureHost.LayoutFeatures;
import facets.core.app.NodeViewable;
import facets.core.app.PathSelection;
import facets.core.app.SAreaTarget;
import facets.core.app.SContentAreaTargeter;
import facets.core.app.SView;
import facets.core.app.SViewer;
import facets.core.app.TreeView;
import facets.core.app.ValueEdit;
import facets.core.app.ViewableAction;
import facets.core.app.ViewableFrame;
import facets.core.app.ViewerContenter;
import facets.core.app.ViewerTarget;
import facets.core.superficial.Notice;
import facets.core.superficial.Notifying.Impact;
import facets.core.superficial.STarget;
import facets.core.superficial.STrigger;
import facets.core.superficial.app.FacetedTarget;
import facets.core.superficial.app.SSelection;
import facets.facet.AreaFacets;
import facets.facet.app.FacetAppSurface;
import facets.facet.app.tree.TreeTargets;
import facets.util.Debug;
import facets.util.FileSpecifier;
import facets.util.TextLines;
import facets.util.tree.DataNode;
import facets.util.tree.NodePath;
import facets.util.tree.Nodes;
import facets.util.tree.TypedNode;
import facets.util.tree.ValueNode;
import facets.util.tree.XmlDocRoot;
import facets.util.tree.XmlPolicy;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import applicable.eval.EvalTypes;
import applicable.eval.form.EvalForm;
import applicable.eval.form.EvalRecord;
/**
{@link ViewerContenter} that can edit a graph of {@link EvalTypes} 
and launch an {@link EvalFormViewer} 
 */
public final class EvalCoder extends ViewerContenter{
	static final String TITLE_LAUNCH="&Configurations...";
	private final static ViewableAction[]EDIT_ACTIONS=
		{COPY,CUT,PASTE,PASTE_INTO,DELETE,MODIFY,UNDO,REDO};
	private static final String STATE_OFFSETS="codeOffsets";
	private static int launches=1;
	private final static XmlPolicy xmlPolicy=new XmlPolicy(){
		@Override
		protected boolean treeAsXmlRoot(){
			return false;
		}
		@Override
		protected boolean dataUsesAttributes(){
			return true;
		}
		@Override
		protected ValueNode getTitleAttributeNames(){
			return newTitleAttributeNames("label",new String[]{"Value=text"});
		}
	};
	private static int contents=1;
	private final FacetAppSurface app;
	private final EvalSpecifier spec;
	private Object stateStamp=null;
	private DataNode memoData;
	public EvalCoder(Object source,FacetAppSurface app,EvalSpecifier spec){
		super(source);
		this.app=app;
		this.spec=spec;
	}
	private DataNode newDataRoot(Object source){
		TextLines lines;
		String contentName;
		if(source instanceof File){
			File file=(File)source;
			contentName=newFileContentName(file);
			lines=new TextLines(file);
		}
		else if(source instanceof URL){
			contentName="Configurator"+contents++;
			lines=new TextLines((URL)source);
			setSink(new File(contentName+".config.xml"));
		}
		else throw new RuntimeException("Not implemented for "+Debug.info(source));
		DataNode data=new DataNode(spec.codeRootType(),contentName);
		new XmlDocRoot(data,xmlPolicy).readFromSource(lines);
		return memoData=data;
	}
	public EvalFormViewer newViewer(Object source){
		final TypedNode code=source instanceof TypedNode?
				(TypedNode)source:(TypedNode)newDataRoot(source).children()[0];
		if(jar)stateStamp=memoData.stateStamp();
		TypedNode copyCode=(TypedNode)code.copyState();
		copyCode.setTitle(copyCode.title()+" : "
				+memoData.title().replaceAll("\\..*",""));
		return spec.newFormViewer(copyCode,app,this);
	}
	@Override
	protected ViewableFrame newContentViewable(Object source){
		final DataNode root=source instanceof DataNode?
				(DataNode)source:newDataRoot(source);
		stateStamp=memoData.stateStamp();
		NodeViewable viewable=new NodeViewable(root,app.ff.statefulClipperSource(true)){
			@Override
			public String title(){
				return root.title();
			}
			@Override
			protected STarget[]lazyElements(){
				return new TreeTargets(app,this){
					protected String getNameListType(){
						return "Value";
					};
				}.appTargets();
			}
			@Override
			protected void viewerSelectionChanged(SViewer viewer,
					SSelection selection){
				super.viewerSelectionChanged(viewer,selection);
				spec.state().put(STATE_OFFSETS,((PathSelection)selection).paths[0].offsets);
			}
			@Override
			public ViewableAction[]viewerActions(SView view){
				return EDIT_ACTIONS;
			}
			@Override
			public boolean editSelection(){
				return new ValueEdit(((PathSelection)selection())){
					@Override
					protected String getDialogInput(String title,String rubric,
							String proposal){
						return app.dialogs().getTextInput(title,rubric,proposal,0);
					}
				}.dialogEdit();
			}
		};
		final int[]offsets=app.spec.state().getInts(STATE_OFFSETS);
		NodePath path=new NodePath(offsets);
		Object definition;
		try{
			path.members(root);
			definition=offsets.length==0?null:new PathSelection(root,path);
		}catch(Exception e){
			definition=Nodes.descendantTitled(root,"Miscellaneous options");
		}
		if(definition!=null)viewable.defineSelection(definition);
		return viewable;
	}
	@Override
	public STarget[]lazyContentAreaElements(SAreaTarget area){
		return new STarget[]{
			new STrigger(TITLE_LAUNCH,new STrigger.Coupler(){
				@Override
				public void fired(STrigger t){
					launchFormViewer();
				}
			}),
		};
	}
	@Override
	protected FacetedTarget[]newContentViewers(ViewableFrame viewable){
		SView view=new TreeView("View"){
			public boolean hideRoot(){
				return false;
			}
			public boolean isLive(){
				return true;
			}
		};
		return ActionViewerTarget.newViewerAreas(viewable,ViewerTarget.newViewFrames(
			new SView[]{view}
		));
	}
	@Override
	final protected void attachContentAreaFacets(AreaRoot area){
		app.ff.areas().attachViewerAreaPanes(area,"",AreaFacets.PANE_SPLIT_VERTICAL);
	}
	@Override
	public void areaRetargeted(SContentAreaTargeter area){
		spec.setFeatureLives(app);
	}
	@Override
	public LayoutFeatures newContentFeatures(final SContentAreaTargeter area){
		return spec.getDesktopFeatures(app,area);
	}
	void launchFormViewer(){
		app.addContent(newViewer(codeRoot()));
	}
	public void updateFromRecords(List<EvalRecord>records,String recordType){
		EvalForm.updateRecordSources(codeRoot(),records,recordType,1);
		memoData.updateStateStamp();
	}
	private TypedNode codeRoot(){
		return (true||jar?memoData:(TypedNode)contentFrame().framed).children()[0];
	}
	@Override
	public boolean hasChanged(){
		return false&&jar?false:memoData.stateStamp()!=stateStamp;
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
		return super.setSink(sink)
			&&(sink instanceof File&&!((File)sink).getName().startsWith("_"));
	}
	@Override
	public void saveToSink(Object sink)throws IOException{
		File file=(File)sink;
		String sinkName=file.getName();
		if(file.exists()&&!sinkName.startsWith("_"))
			new TextLines(file).copyFile("_"+sinkName);
		DataNode root=jar?memoData:(DataNode)contentFrame().framed;
		new XmlDocRoot(root,xmlPolicy).writeToSink(file);
		stateStamp=memoData.updateStateStamp();
		root.setTitle(newFileContentName(file));
		app.notify(new Notice(contentFrame(),Impact.DEFAULT));
	}
	private String newFileContentName(File file){
		return app.getFileSpecifiers()[0].stripExtension(file.getName());
	}
}
