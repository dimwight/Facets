package pub.view;
import static facets.core.superficial.Notice.*;
import static facets.facet.AreaFacets.*;
import static facets.facet.FacetFactory.*;
import static facets.util.tree.DataConstants.*;
import static pub.view.FieldsSpec.*;
import facets.core.app.ActionViewerTarget;
import facets.core.app.AppContenter;
import facets.core.app.AppSpecifier;
import facets.core.app.AreaRoot;
import facets.core.app.Dialogs.MessageTexts;
import facets.core.app.Dialogs.Response;
import facets.core.app.FeatureHost.LayoutFeatures;
import facets.core.app.NestedView;
import facets.core.app.NodeViewable;
import facets.core.app.PathSelection;
import facets.core.app.TreeView;
import facets.core.app.ViewerContenter;
import facets.core.superficial.FacetedTarget;
import facets.core.superficial.SFrameTarget;
import facets.core.superficial.STarget;
import facets.core.superficial.STargeter;
import facets.core.superficial.SToggling;
import facets.core.superficial.app.SAreaTarget;
import facets.core.superficial.app.SContentAreaTargeter;
import facets.core.superficial.app.SContenter;
import facets.core.superficial.app.SSelection;
import facets.core.superficial.app.SView;
import facets.core.superficial.app.SViewer;
import facets.core.superficial.app.ViewableFrame;
import facets.facet.AreaFacets;
import facets.facet.FacetFactory;
import facets.facet.ViewerAreaMaster;
import facets.facet.app.FacetAppSurface;
import facets.util.Debug;
import facets.util.Stateful;
import facets.util.Times;
import facets.util.TypesKey;
import facets.util.Util;
import facets.util.ValueProxy;
import facets.util.tree.DataNode;
import facets.util.tree.NodePath;
import facets.util.tree.TypedNode;
import facets.util.tree.ValueNode;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Comparator;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import pub.PubValues;
import pub.view.PubsView.SlaveSpec;
import applicable.field.FieldSet;
import applicable.field.FieldSet.SidebarChooser;
import applicable.field.FieldTableView;
class ListingContenter extends ViewerContenter{
	public static final String ARG_TREE="tableTree";
	static final boolean checkFieldsOnSort=false;
	static final int TARGET_CHOOSER_DIALOG=0,TARGET_CHOOSER_SIDEBAR=1,PREVIEW_AT=1;
	private static final String STATE_OFFSETS="selectionOffsets",TYPE_RECORD="Record",
		TITLE_TREE="Source Records";
	private static final Object KEY_TYPE_NORMAL=new Object(){},KEY_TYPE_DEBUG=new Object(){};
	final FacetAppSurface app;
	final AppSpecifier spec;
	final FieldTableView table;
	final FieldsSpec fieldsSpec;
	final SidebarChooser chooser;
	final boolean inForm;
	private final Class targetType;
	private RecordContenter preview;
	private final FacetFactory ff;
	private final String title;
	private MessageTexts resultTexts;
	private Runnable showResults;
	@Override
	public void wasRemoved(){
		((ListingContent)sink()).dispose();
	}
	final ViewerContenter getPreview(){
		DataNode record=(DataNode)contentFrame().selection().single();
		if(preview==null||!preview.title().equals(record.title()))
			preview=new RecordContenter(
					new RecordContent(record,((ListingContent)sink()).disposable()),
					spec,app,true);
		return preview;
	}
	ListingContenter(ListingContent listing,PubFields fields,FacetAppSurface app){
		super(listing);
		this.app=app;
		ff=app.ff;
		this.spec=app.spec;
		String listingTitle=listing.tree.title();
		title=!listingTitle.endsWith(ListingViewable.CLONE_MARK)?fields.title()
				:listingTitle.replace(ListingViewable.CLONE_MARK,"");
		fieldsSpec=fields.spec;
		chooser=new FieldSet.SidebarChooser(app,new TypesKey(targetType=fieldsSpec.getClass()));
		inForm=fieldsSpec==LINKS;
		table=new FieldTableView(fieldsSpec.toString(),fields){
			private final String typeKey=FieldsSpec.class.getSimpleName();
			@Override
			protected void contentSorted(List<Stateful>content){
				if(!checkFieldsOnSort)return;
				Times.printElapsed("ListingContenter..contentSorted");
				ListingContent listing=((ListingContent)sink()).disposable();
					for(Stateful record:content.subList(0,25))
						new RecordContent((DataNode)record,listing);
				Times.printElapsed("ListingContenter..contentSorted~");
			}
			@Override
			public String typeKey(){
				return typeKey;
			}
			@Override
			protected ValueProxy newRowProxy(Stateful source){
				return new RecordProxy((TypedNode)source,fields.liveFields());
			}
			@Override
			public boolean contextClickSelects(){
				return true;
			}
			@Override
			public boolean sortInContent(){
				return true;
			}
			@Override
			protected Comparator<Stateful>baseSort(){
				return fieldsSpec.baseSort();
			}
		};
	}
	@Override
	public boolean useActiveFeatures(SContentAreaTargeter active){
		return active.targetType()==LINKS.getClass();
	}
	@Override
	public Class targetType(){
		return targetType;
	}
	@Override
	protected ViewableFrame newContentViewable(Object source){
		DataNode tree=source instanceof DataNode?(DataNode)source:((ListingContent)source).tree;
		final ValueNode state=spec.state();
		NodeViewable viewable=new ListingViewable(tree,this);
		if(tree.children().length==0)throw new IllegalStateException(
				"Empty tree in "+Debug.info(this));
		int[]offsets=state.getInts(STATE_OFFSETS);
		PathSelection definition;
		if(false)try{
			NodePath path=new NodePath(offsets);
			final boolean testPath=true;
			if(testPath)path.members(tree);
			definition=offsets.length<2||offsets[0]>0?null:new PathSelection(tree,path);
			viewable.defineSelection(definition);
		}catch(Exception e){
			trace(".newContentViewable: e=",e);
		}
		else viewable.defineSelection(tree.children()[0]);
		return viewable;
	}
	@Override
	final protected FacetedTarget[]newContentViewers(ViewableFrame viewable){
		SFrameTarget table=newViewFrame(this.table),
				tree=newViewFrame(new TreeView(TITLE_TREE){
			@Override
			public boolean isLive(){
				return false;
			}
			@Override
			public boolean hideRoot(){
				return false;
			}
			@Override
			public String nodeRenderText(TypedNode node){
				return(TYPE_XML+"|"+PubValues.TYPE_DOC).indexOf(node.type())>=0?node.title()
						:super.nodeRenderText(node);
			}
		}),
		form=false?null:newViewFrame(new NestedView("Record &Preview"){
			@Override
			public Object getSourceSelectionContent(SSelection selection){
				return getPreview();
			}
			@Override
			public AppContenter newViewerContenter(Object source){
				return (AppContenter)source;
			}
		});
		return ActionViewerTarget.newViewerAreas(viewable,
			inForm?new SFrameTarget[]{false?tree:table}
				:new SFrameTarget[]{false&&PubsView.dev?tree:table,
						form==null||spec.args().getBoolean(ARG_TREE)?tree:form});
	}
	@Override
	final protected void attachContentAreaFacets(AreaRoot area){
		STarget[]viewers=area.indexableTargets();
		final boolean vertical=viewers.length>1&&
				((SViewer)((SAreaTarget)viewers[1]).activeFaceted()).view()instanceof TreeView;
		ff.areas().attachViewerAreaPanes(area,new ViewerAreaMaster(){
			@Override
			protected String hintString(){
				return inForm?HINT_NONE:HINT_BARE;
			}
			@Override
			public String typeKey(){
				return Util.shortTypeNameKey(false?KEY_TYPE_DEBUG:KEY_TYPE_NORMAL);
			}
			@Override
			protected ViewerAreaMaster newChildMaster(SAreaTarget area){
				return true||area.title()!=TITLE_TREE?null:
					new ViewerAreaMaster(){
					};
			}
		},
		vertical?PANE_SPLIT_VERTICAL:PANE_SPLIT_HORIZONTAL);
	}
	private SFrameTarget newViewFrame(SView view){
		return new SFrameTarget(view){
			@Override
			protected STarget[]lazyElements(){
				return inForm?new STarget[]{}:new STarget[]{
					table.fields.newChooseTrigger(app),
					chooser.newSidebarToggling(table.fields)
				};
			}
		};
	}
	@Override
	final public void areaRetargeted(SContentAreaTargeter area){
		if(area.elements().length!=0)((ListingViewable)contentFrame()).record.setLive(
				((SToggling)((STargeter)findElement(area,AreaFacets.PANE_SHOW,PREVIEW_AT
						)).target()).isSet());
		if(area.view().elements().length>0)
			chooser.updateToggling(((STargeter)findElement(area.view(),TARGET_CHOOSER_SIDEBAR)));
		if(resultTexts!=null&&showResults==null){
			showResults=new Runnable(){public void run(){
					app.dialogs().infoMessage(resultTexts);
					resultTexts=null;
					showResults=null;
				}
			};
			if(false)showResults.run();
			else SwingUtilities.invokeLater(false?showResults:new Runnable(){public void run(){
				new Timer(100,new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent e){
						showResults.run();
					}
					}) {
						@Override
						public boolean isRepeats(){
							return false;
						}
					}.start();
			}});
		}
	}
	@Override
	public
	final STarget[]lazyContentAreaElements(SAreaTarget area){
		return ff.areas().panesGetTarget(area).elements();
	}
	@Override
	public LayoutFeatures newContentFeatures(SContentAreaTargeter area){
		return ListingFeatures.newForFields(area,this);
	}
	@Override
	final public boolean hasChanged(){
		return!((ListingContent)this.sink()).isLatest();
	}
	@Override
	public void wasAdded(){
		if(!app.isBuilt()||inForm)return;
		ViewerContenter[]viewers=app.findViewerContents();
		for(ViewerContenter vc:viewers)
			if(vc instanceof ListingContenter&&vc.hasChanged()){
				if(app.dialogs().confirmYesNo("Records changed","Close stale views?")!=Response.Yes)
					return;
				for(ViewerContenter vc1:viewers)if(vc1 instanceof ListingContenter&&vc1!=this)
						app.removeContent(vc1);
				break;
			}
	}
	@Override
	final public String title(){
		return (hasChanged()?"*":"")+title;
	}
	protected String boxOpeningText(){
		String searchNum=spec.args().getString(ListingSearcher.ARG_SEARCH);
		String text=inForm?"":!searchNum.matches("\\w.*")?"":("\\num "+searchNum);
		if(!text.equals(""))Util.printOut("ListingContenter.boxOpeningText: ",text);
		return text;
	}
	final FacetAppSurface newSlaveApp(ListingContent listing){
		final ListingContent copy=listing.disposable();
		return new SlaveSpec((PubsView)spec,ff){
			@Override
			protected SContenter newSlaveContenter(FacetAppSurface app){
				return new ListingContenter(copy,(PubFields)table.fields,app);
			}
		}.newSlave();
	}
	final void setResultsMessage(MessageTexts texts){
		this.resultTexts=texts;
	}
}