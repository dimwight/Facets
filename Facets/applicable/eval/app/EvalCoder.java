package applicable.eval.app;
import static facets.core.app.ActionViewerTarget.Action.*;
import facets.core.app.ActionViewerTarget;
import facets.core.app.AreaRoot;
import facets.core.app.FeatureHost.LayoutFeatures;
import facets.core.app.NodeViewable;
import facets.core.app.PathSelection;
import facets.core.app.TreeView;
import facets.core.app.ValueEdit;
import facets.core.app.ViewerContenter;
import facets.core.superficial.FacetedTarget;
import facets.core.superficial.Notice;
import facets.core.superficial.Notifying.Impact;
import facets.core.superficial.STarget;
import facets.core.superficial.STrigger;
import facets.core.superficial.app.SAreaTarget;
import facets.core.superficial.app.SContentAreaTargeter;
import facets.core.superficial.app.SSelection;
import facets.core.superficial.app.SView;
import facets.core.superficial.app.SViewer;
import facets.core.superficial.app.ViewableAction;
import facets.core.superficial.app.ViewableFrame;
import facets.core.superficial.app.ViewerTarget;
import facets.facet.AreaFacets;
import facets.facet.app.FacetAppSurface;
import facets.facet.app.tree.TreeTargets;
import facets.util.FileSpecifier;
import facets.util.Stateful;
import facets.util.TextLines;
import facets.util.Util;
import facets.util.tree.DataNode;
import facets.util.tree.NodeList;
import facets.util.tree.NodePath;
import facets.util.tree.Nodes;
import facets.util.tree.TypedNode;
import facets.util.tree.ValueNode;
import facets.util.tree.XmlDocRoot;
import facets.util.tree.XmlPolicy;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import applicable.eval.EvalTypes;
import applicable.eval.form.EvalForm;
import applicable.eval.form.EvalRecord;
/**
{@link ViewerContenter} that can edit a graph of {@link EvalTypes} 
and launch an {@link EvalViewer} 
 */
public final class EvalCoder extends ViewerContenter{
	static final String TITLE_LAUNCH="New &Configurator";
	private final static ViewableAction[]EDIT_ACTIONS=
		{COPY,CUT,PASTE,PASTE_INTO,DELETE,MODIFY,UNDO,REDO};
	private static final String STATE_OFFSETS="selectionOffsets";
	private static int launches=1;
	public final static XmlPolicy XML_POLICY=new XmlPolicy(){
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
	protected EvalCoder(Object source,FacetAppSurface app,EvalSpecifier spec){
		super(source);
		this.app=app;
		this.spec=spec;
	}
	void launchForm(){
		TypedNode code=(TypedNode)codeRoot().copyState();
		EvalForm form=new EvalForm(code,EvalTypes.fromCleanedTree(code));
		app.addContent(spec.newEvalViewer(form,app,this));
	}
	void updateFromForm(EvalForm form){
		TypedNode active=form.activeRecord().source;
		DataNode root=(DataNode)codeRoot();
		NodeList all=new NodeList(root,false);
		List<TypedNode>records=new ArrayList();
		String recordType=form.codeForType(EvalRecord.type);
		for(TypedNode each:all)
			if(each.type().equals(recordType))
				records.add(each);
		all.removeAll(records);
		for(TypedNode record:records)
			if((record.stateEquals(active))){
				records.remove(record);
				records.add(0,active);
				break;
			}
		if(!records.contains(active))
			records.add(0,active);
		all.addAll(1,records);
		all.updateParent();
	}
	@Override
	protected ViewableFrame newContentViewable(Object source){
		TextLines lines;
		String contentName;
		if(source instanceof File){
			File file=(File)source;
			contentName=newFileContentName(file);
			lines=new TextLines(file);
		}
		else if(source instanceof URL){
			lines=new TextLines((URL)source);
			contentName="Configurator"+contents++;
			setSink(new File(contentName+".config.xml"));
		}
		else throw new RuntimeException("Not implemented for "+Util.helpfulClassName(source));
		final DataNode root=new DataNode(spec.codeRootType(),contentName);
		XmlPolicy policy=XML_POLICY;
		new XmlDocRoot(root,policy).readFromSource(lines);
		stateStamp=root.stateStamp();
		NodeViewable viewable=new NodeViewable(root,app.ff.statefulClipperSource(false)){
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
					launchForm();
				}
			}),
		};
	}
	@Override
	protected FacetedTarget[]newContentViewers(ViewableFrame viewable){
		SView view=new TreeView("View"){
			public boolean hideRoot(){
				return true;
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
	@Override
	public boolean hasChanged(){
		return((Stateful)contentFrame().framed).stateStamp()!=stateStamp;
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
		if(file.exists()&&!sinkName.startsWith("_"))new TextLines(file).copyFile("_"+sinkName);
		DataNode root=(DataNode)contentFrame().framed;
		new XmlDocRoot(root,XML_POLICY).writeToSink(file);
		stateStamp=root.updateStateStamp();
		root.setTitle(newFileContentName(file));
		app.notify(new Notice(contentFrame(),Impact.DEFAULT));
	}
	private TypedNode codeRoot(){
		return ((TypedNode)contentFrame().framed).children()[0];
	}
	private String newFileContentName(File file){
		return app.getFileSpecifiers()[0].stripExtension(file.getName());
	}
}
