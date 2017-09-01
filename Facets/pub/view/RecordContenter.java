package pub.view;
import static facets.core.superficial.Notice.*;
import static facets.facet.AreaFacets.*;
import static facets.facet.FacetFactory.*;
import static pub.PubValues.*;
import static pub.view.PubsView.*;
import static pub.view.RecordContenter.ViewIs.*;
import facets.core.app.ActionViewerTarget;
import facets.core.app.AppContenter;
import facets.core.app.AppSpecifier;
import facets.core.app.AreaRoot;
import facets.core.app.FeatureHost.LayoutFeatures;
import facets.core.app.HtmlView;
import facets.core.app.HtmlView.InputView;
import facets.core.app.NestedView;
import facets.core.app.TreeView;
import facets.core.app.ViewerContenter;
import facets.core.superficial.FacetedTarget;
import facets.core.superficial.Notifying.Impact;
import facets.core.superficial.SFacet;
import facets.core.superficial.SFrameTarget;
import facets.core.superficial.SIndexing;
import facets.core.superficial.STarget;
import facets.core.superficial.STargeter;
import facets.core.superficial.SToggling;
import facets.core.superficial.STrigger;
import facets.core.superficial.app.SAreaTarget;
import facets.core.superficial.app.SContentAreaTargeter;
import facets.core.superficial.app.SContenter;
import facets.core.superficial.app.SSelection;
import facets.core.superficial.app.ViewableFrame;
import facets.core.superficial.app.SContentAreaTargeter.ContentArea;
import facets.facet.AreaFacets;
import facets.facet.FacetFactory;
import facets.facet.ViewerAreaMaster;
import facets.facet.app.FacetAppSurface;
import facets.util.Debug;
import facets.util.TypesKey;
import java.io.File;
import java.io.IOException;
import pdft.PdfCore;
import pub.PubValues;
import pub.view.PubsView.SlaveSpec;
import applicable.FileTableView;
final class RecordContenter extends ViewerContenter{
	public static final String ARG_OPEN="openRecord",KEY_TOP="Record";
	static final String VIEW_DATA="Data",VIEW_NOTES="Notes",VIEW_TREE="Tree",VIEW_STATE="State";
	public enum ViewIs{Full,Window,Preview}
	static final class StateView extends TreeView{
		final SToggling flag=new SToggling("Flag",false,new SToggling.Coupler());
		StateView(){
			super(VIEW_STATE);
		}
		@Override
		public boolean isLive(){
			return true;
		}
	}
	private final AppSpecifier spec;
	private final FacetAppSurface app;
	private final ViewIs viewIs;
	private STarget paneControls;
	private String changeTitle=super.title();
	RecordContenter(RecordContent source,AppSpecifier spec,FacetAppSurface app,
			boolean inPreview){
		super(source);
		this.spec=spec;
		this.app=app;
		viewIs=inPreview?Preview:spec.inSlave()?Window:Full;
	}
	@Override
	public void wasAdded(){
		if(viewIs==Preview)return;
		SAreaTarget area=(SAreaTarget)app.activeContentTargeter().target();
		boolean debug=false&&dev;
		String viewerTitle=viewIs==Window||debug?VIEW_NOTES:VIEW_DATA;
		for(STarget viewer:area.indexableTargets())if(viewer.title()==viewerTitle)
				((SAreaTarget)viewer).activeFaceted().ensureActive(Impact.SELECTION);
		STrigger activeMaximise=(STrigger)findElement(area,2,0),
			editNote=(STrigger)findElement(((ContentArea)area).contenter.contentFrame(),3,1);
		if(debug||viewIs==Window){
			activeMaximise.fire();
			if(false&&debug&&viewerTitle==VIEW_NOTES)editNote.fire();
		}
		if(hasChanged())app.dialogs().infoMessage("Record Normalised",
				"State has changed, data is unaffected.");
	}
	@Override
	public void wasRemoved(){
		((RecordContent)sink()).dispose();
		if(viewIs==Preview)return;
		RecordViewable viewable=(RecordViewable)contentFrame();
		viewable.closeNoteEdits();
		viewable.content.resetUserLock();
	}
	@Override
	protected ViewableFrame newContentViewable(Object source){
		RecordContent content=(RecordContent)source;
		return viewIs==Preview?new RecordViewable(content,app):new EditViewable(content,app);
	}
	@Override
	protected FacetedTarget[]newContentViewers(final ViewableFrame viewable){
		changeTitle=super.title();
		SFrameTarget data=newHtmlFrame(VIEW_DATA),notes=newHtmlFrame(VIEW_NOTES),
			links=new SFrameTarget(new NestedView(TYPE_LINKS){
				@Override
				public Object getSourceSelectionContent(SSelection selection){
					return ((RecordContent)sink()).getLinkListing();
				}
				@Override
				public AppContenter newViewerContenter(Object source){
					ListingContent copy=((ListingContent)source).disposable();
					PubFields fields=FieldsSpec.LINKS.newFields((PubsView)app.spec);
					return false?new ListingContenter(copy,fields,app)
						:fields.newTableContenter(copy,app);
				}
			}),
			state=new SFrameTarget(new StateView()){
				@Override
				protected STarget[]lazyElements(){
					return new STarget[]{((StateView)framed).flag};
				}
			},
			sources=new SFrameTarget(new FileTableView(RecordContent.TYPE_SOURCES,
					RecordContent.SOURCES_DIR)),
			attachments=new SFrameTarget(new FileTableView(TYPE_ATTACHMENTS,
					PubFiles.ATTACHMENTS_DIR){
				@Override
				protected void openFile(File file){
					if(file.getName().toLowerCase().endsWith(".pdf"))
						((PubsView)spec).openPdf(file.getAbsolutePath(),0);
					else super.openFile(file);
				}
			}),
			tree=new SFrameTarget(new TreeView(VIEW_TREE){
				@Override
				public boolean isLive(){
					return false;
				}
				@Override
				public boolean hideRoot(){
					return false;
				}
			});
		boolean inPreview=viewIs==Preview;
		return ActionViewerTarget.newViewerAreas(viewable,
				userView?new SFrameTarget[]{data,notes,links,sources,attachments}
				:false&&dev&&inPreview?new SFrameTarget[]{data,notes}//,sources,attachments,links
				:inPreview?new SFrameTarget[]{data,notes,links,sources,attachments,tree}
			:new SFrameTarget[]{data,notes,state,links,sources,attachments,tree});
	}
	@Override
	public STarget[]lazyContentAreaElements(SAreaTarget area){
		return false&&viewIs==Preview?new STarget[]{}:app.ff.areas().panesGetTarget(area).elements();
	}
	@Override
	protected void attachContentAreaFacets(final AreaRoot area){
		final FacetFactory ff=app.ff;
		AreaFacets areas=ff.areas();
		SFacet[]viewers=areas.viewerAreaChildren(area,new ViewerAreaMaster(){
			@Override
			public String typeKey(){
				return KEY_TOP+viewIs.name()+(userView?"User":"");
			}
			@Override
			protected ViewerAreaMaster newChildMaster(SAreaTarget child){
				final String view=child.title();
				final boolean devState=false&&dev&&view==VIEW_STATE;
				return new ViewerAreaMaster(){
					@Override
					protected SFacet newViewTools(STargeter t){
						return true||view!=VIEW_STATE?null:ff.toolGroups(t,HINT_NONE,
								ff.togglingCheckboxes(t.elements()[0],HINT_BARE));
					}
					@Override
					public String typeKey(){
						return KEY_TOP+viewIs.name()+(userView?"User":"");
					}
					@Override
					protected String hintString(){
						return viewIs==Preview?AreaFacets.HINT_PANE_STACK:HINT_NONE;
					}
				};
			}
		});
		if(viewIs==Preview)areas.attachPanes(area,viewers,false?PANE_SPLIT_VERTICAL:PANE_STACK);
		else if(viewers.length==2)areas.attachPanes(area,viewers,PANE_SPLIT_VERTICAL);
		else areas.attachPanes(area,viewers,
			userView?new int[][]{{PANE_SPLIT_HORIZONTAL},
				{PANE_SPLIT_HORIZONTAL},{PANE_STACK,PANE_STACK,PANE_STACK}}:
			new int[][]{{PANE_SPLIT_HORIZONTAL},
				{PANE_SPLIT_VERTICAL,PANE_LEFT,PANE_SPLIT_HORIZONTAL},
				{PANE_SPLIT_VERTICAL,PANE_LEFT,PANE_SPLIT_HORIZONTAL,PANE_LOWER,PANE_SPLIT_VERTICAL}},
			userView?new double[]{0.78,0.8,0.5,0.48}
:new double[]{0.66,0.8,0.5,0.45,0.75,0.45},
				new int[]{0,1},new String[]{"Editors","Ta&bles"});
	}
	@Override
	public LayoutFeatures newContentFeatures(SContentAreaTargeter area){
		return new RecordFeatures(app,area,spec,viewIs);
	}
	@Override
	public boolean useActiveFeatures(SContentAreaTargeter active){
		return viewIs!=Preview;
	}
	@Override
	public TypesKey featuresKey(SContentAreaTargeter use){
		return viewerFeaturesKey(this,use,true);
	}
	@Override
	public String title(){
		return changeTitle;
	}
	void refreshChangeTitle(){
		changeTitle=(hasChanged()?"*":"")+super.title();
	}
	@Override
	public void areaRetargeted(SContentAreaTargeter area){
		refreshChangeTitle();
	}
	@Override
	public boolean hasChanged(){
		return((RecordContent)sink()).stateChanged();
	}
	@Override
	public void saveToSink(Object sink){
		((RecordContent)sink).saveState();
	}
	private SFrameTarget newHtmlFrame(final String title){
		return new SFrameTarget(title==VIEW_DATA&&viewIs!=Preview?
				new InputView(title):new HtmlView(title));
	}
	static FacetAppSurface newSlaveApp(FacetAppSurface app,final RecordContent source){
		return new SlaveSpec((PubsView)app.spec,app.ff){
			@Override
			protected SContenter newSlaveContenter(FacetAppSurface app){
				return new RecordContenter(source,this,app,false);
			}
		}.newSlave();
	}
}