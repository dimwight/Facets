package applicable.eval.app;
import static facets.core.app.ActionViewerTarget.Action.*;
import static facets.util.tree.Nodes.*;
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
import facets.core.superficial.SFrameTarget;
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
import facets.util.tree.TypedNode;
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
import applicable.eval.EvalTypes;
import applicable.eval.form.EvalForm;
import applicable.eval.form.EvalForms;
import applicable.eval.form.EvalRecord;
/**
{@link ViewerContenter} that can edit a graph of {@link EvalTypes} 
and launch an {@link EvalViewer} 
 */
public final class EvalCoder extends ViewerContenter{
	static final String TITLE_LAUNCH="Evaluators";
	private final static ViewableAction[]EDIT_ACTIONS={COPY,CUT,PASTE,PASTE_INTO,DELETE,EDIT};
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
			return newTitleAttributeNames("label",new String[]{});
		}
	};
	private final FacetAppSurface app;
	private final EvalSpecifier spec;
	private Object stateStamp=null;
	final void launchEvaluate(){
		TypedNode code=(TypedNode)((TypedNode)contentFrame().framed).children()[0].copyState();
		code.setTitle("#"+launches+++" "+code.title());
		EvalForm form=new EvalForm(code,EvalTypes.fromCleanedTree(code));
		if(true)app.addContent(spec.newEvalViewer(form,app,this));
	}
	protected EvalCoder(Object source,FacetAppSurface app,EvalSpecifier spec){
		super(source);
		this.app=app;
		this.spec=spec;
	}
	@Override
	protected ViewableFrame newContentViewable(Object source){
		final DataNode root;
		if(source instanceof File){
			File file=(File)source;
			XmlPolicy policy=XML_POLICY;
			root=new DataNode(spec.codeRootType(),newContentName(file));
			new XmlDocRoot(root,policy).readFromSource(file);
			stateStamp=root.stateStamp();
		}
		else if(false)root=(DataNode)source;
		else if(true)throw new RuntimeException("Not implemented for "+Util.helpfulClassName(source));
		NodeViewable viewable=new NodeViewable(root,
				app.ff.statefulClipperSource(app.spec.hasSystemAccess())){
			@Override
			public String title(){
				return root.type()+" "+root.title();
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
	protected FacetedTarget[]newContentViewers(ViewableFrame viewable){
		SView view=new TreeView("View"){
			@Override
			public SSelection newViewerSelection(SViewer viewer,SSelection viewable){
				if(true)throw new RuntimeException("Not implemented in "+this);
				return super.newViewerSelection(viewer,viewable);
			}
			public boolean hideRoot(){
				return true;
			}
			public String nodeRenderText(TypedNode node){
				return super.nodeRenderText(node);
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
	public STarget[]lazyContentAreaElements(SAreaTarget area){
		return new STarget[]{new STrigger(TITLE_LAUNCH,
				new STrigger.Coupler(){
			@Override
			public void fired(STrigger t){
				launchEvaluate();
			}
		})};
	}
	@Override
	public void areaRetargeted(SContentAreaTargeter area){
		spec.setFeatureLives(app);
	}
	@Override
	public void saveToSink(Object sink)throws IOException{
		File file=(File)sink;
		String sinkName=file.getName();
		if(file.exists()&&!sinkName.startsWith("_"))new TextLines(file).copyFile("_"+sinkName);
		DataNode root=(DataNode)contentFrame().framed;
		new XmlDocRoot(root,XML_POLICY).writeToSink(file);
		stateStamp=root.updateStateStamp();
		root.setTitle(newContentName(file));
		app.notify(new Notice(contentFrame(),Impact.DEFAULT));
	}
	void updateFromForm(EvalForm form){
		TypedNode active=form.activeRecord().source;
		boolean matched=false;
		DataNode root=(DataNode)((TypedNode)contentFrame().framed).children()[0];
		for(TypedNode record:Nodes.children(root,form.codeForType(EvalRecord.type)))
			if((matched=record.stateEquals(active)))break;
		if(!matched)Nodes.appendChild(root,active);
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
	private String newContentName(File file){
		String fileName=file.getName();
		String name=true?fileName:app.getFileSpecifiers()[0].stripExtension(fileName);
		return name;
	}
}
