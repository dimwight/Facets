package pub.view;
import static applicable.TextQuery.*;
import static applicable.field.CodeQuery.*;
import static pub.view.ListingViewable.*;
import static pub.view.RecordProxy.*;
import facets.core.app.ActionViewerTarget.Action;
import facets.core.app.AppSpecifier;
import facets.core.app.Dialogs;
import facets.core.app.Dialogs.MessageTexts;
import facets.core.app.NestedView;
import facets.core.app.PathSelection;
import facets.core.app.ViewerContenter;
import facets.core.superficial.Notifying.Impact;
import facets.core.superficial.SIndexing;
import facets.core.superficial.STarget;
import facets.core.superficial.STextual;
import facets.core.superficial.SToggling;
import facets.core.superficial.STrigger;
import facets.core.superficial.STrigger.Coupler;
import facets.core.superficial.TargetCore;
import facets.core.superficial.app.SSelection;
import facets.core.superficial.app.SView;
import facets.core.superficial.app.SViewer;
import facets.core.superficial.app.ViewableAction;
import facets.core.superficial.app.ViewableFrame;
import facets.facet.FacetFactory.SuggestionsCoupler;
import facets.facet.FacetFactory.TriggerCodeCoupler;
import facets.facet.app.FacetAppSurface;
import facets.util.Debug;
import facets.util.HtmlBuilder;
import facets.util.HtmlBuilder.RenderTarget;
import facets.util.Stateful;
import facets.util.Times;
import facets.util.Titled;
import facets.util.Util;
import facets.util.tree.DataNode;
import facets.util.tree.TypedNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import pub.PubIssue;
import pub.PubValues;
import pub.view.RecordViewable.ExportCoupler;
import pub.view.ListingSearcher.Results;
import applicable.ItemHistory;
import applicable.field.CodeQuery;
import applicable.field.FieldSet;
import applicable.field.FieldTableView;
final class ListingViewable extends ListingViewableCore{
	public static final String TITLE_PDF="View &PDF",
		TITLE_EXPORT=PubValues.searchView?"E&xport Results":"Table E&xport...|Table...";
	static final int TARGETS_ACTION=0,TARGETS_SEARCH=1,TARGET_REFS=2,TARGETS_EXPORT=3,
		TARGETS_RECORD=4,TARGETS_OPEN=5,TARGETS_MOVE=6,TARGETS_RESULTS=7,TARGETS_CLONE=8,
		TARGET_SEARCH_BOX=0,TARGET_SEARCH_GO=1,TARGET_SEARCH_MATCH=2,TARGET_SEARCH_EXACT=3,
		VIEW_ROOT=0,VIEW_DOC=1;
	static final String TITLE_OPEN="Open",OPEN_TAB="In &Tab",OPEN_SLAVE="In &Window",
			CLONE_MARK="[[CLONE]]";
	static final ViewableAction[]OPEN_ACTIONS=new ViewableAction[]{
			Action.newTitled(OPEN_TAB),
			Action.newTitled(OPEN_SLAVE),
	};
	final STrigger pdf=new STrigger(TITLE_PDF,new STrigger.Coupler(){
			@Override
			public void fired(STrigger t){
				TypedNode record=(TypedNode)selection().single();
				((PubsView)spec).openPdf(getPdfPath(record),getPdfOpenPage(record));
			}
		});
	private final SIndexing clone,move,open;
	private final STarget export=new STrigger(TITLE_EXPORT,new STrigger.Coupler(){
		@Override
		public void fired(STrigger t){
			String proposal=search.resultsTitle,input;
			if(!PubValues.userView&&title().startsWith(FieldsSpec.VIEW_PUBSEARCH.toString())){
				PubFields fields=(PubFields)contenter.table.fields;
				StringBuilder sb=new StringBuilder();
				for(Stateful node:output)
					sb.append(fields.newExportRow((TypedNode)node,true)+"\n");
				trace(proposal+":\n",sb);
				return;
			}
			else while((input=dialogs.getTextInput((TITLE_EXPORT+" - "+TITLE_EXPORT
					).replaceAll("[.&]",""),
					"Exported page will have title as below",proposal,0)
				)!=null&&input.trim().equals(""));
			if(input==null)return;
			search.results.items.get(0).retitle(input);
			((PubsView)spec).openPage(newTableExportHtml(input));
		}
	});
	private final AppSpecifier spec;
	private final Dialogs dialogs;
	final ListingSearcher search;
	final STarget record=new ExportCoupler(true){
		@Override
		protected RecordViewable getViewable(){
			ViewerContenter preview=contenter.getPreview();
			ViewableFrame contentFrame=preview.contentFrame();
			if(!(contentFrame instanceof RecordViewable))throw new IllegalStateException(
					"Preview not built: "+Debug.info(preview));
			else return(RecordViewable)contentFrame;
		}
	}.newTarget();
	ListingViewable(DataNode root,ListingContenter contenter){
		super(root,contenter);
		spec=contenter.spec;
		dialogs=contenter.app.dialogs();
		move=contenter.table.newMoveIndexing(this);
		search=new ListingSearcher(this);
		SIndexing.Coupler openCoupler=new SIndexing.Coupler(){
			@Override
			public void indexSet(SIndexing i){
				try{
					openIndexed(i);
				}catch(Exception e){
					PubFiles.checkLockException(e,dialogs);
				}	
			}
		};
		open=new SIndexing(TITLE_OPEN,spec.inSlave()?new Object[]{OPEN_SLAVE}
			:new Object[]{OPEN_TAB,OPEN_SLAVE},0,openCoupler);
		clone=new SIndexing("Open &Copy",spec.inSlave()?new Object[]{OPEN_SLAVE+"|Clone"}
			:new Object[]{OPEN_TAB+"|Clone",OPEN_SLAVE+"|Clone"},0,openCoupler);
	}
	@Override
	protected STarget[]lazyElements(){
		STarget actions=new TargetCore("Actions",search.remove,search.clear
//,				new STrigger("Clear &Sort",new STrigger.Coupler(){
//					@Override
//					public void fired(STrigger arg0){
//						contenter.app.openApp();
//					}
//				})
		);
		return search.results==null?new STarget[]{actions,search.targets,export,open} 
			:new STarget[]{actions,search.targets,search.refs,export,record,open,move,
				search.results.targets,clone};
	}
	@Override
	protected ViewableAction[]viewerActions(SView view){
		return OPEN_ACTIONS;
	}
	@Override
	public boolean actionIsLive(SViewer viewer,ViewableAction action){
		return true&&selection().single()!=RecordContent.NO_LINKS;
	}
	@Override
	protected void actionTriggered(SViewer viewer,ViewableAction action){
		open.setIndex(action==OPEN_ACTIONS[1]?1:0);
	}
	@Override
	public SSelection defineSelection(Object definition){
		SSelection selection=super.defineSelection(definition);
		TypedNode selected=(TypedNode)selection.single();
		boolean subset=output.size()!=input.size(),actions=true||subset;
		if(search.results!=null)search.results.setRetitleLive(subset);
		clone.setLive(subset);
		open.setLive(output.size()>0&&selected!=RecordContent.NO_LINKS);
		export.setLive(actions);
		move.setLive(actions);
		search.setLives();
		contenter.table.updateMover(output);
		return selection;
	}
	@Override
	protected SSelection newViewerSelection(SViewer viewer){
		return viewer.view()instanceof NestedView?PathSelection.newMinimal(newSelectionContent())
			:super.newViewerSelection(viewer);
	}
	@Override
	protected SSelection newOutputSelection(Object selected,boolean forTree){
		defineSelection(selected);
		SSelection selection=super.newOutputSelection(selected,forTree);
		pdf.setLive(!getPdfPath((TypedNode)selection.single()).equals(PubIssue.PDF_NONE));
		return selection;
	}
	private void openIndexed(SIndexing i){
		boolean cloning=i==clone,slaving=i.indexed()==OPEN_SLAVE;
		FacetAppSurface app=contenter.app;
		if(cloning){
			if(!search.results.retitle())return;
			ListingContent listing=listing().newListing(
					search.results.items.get(0).toString()+CLONE_MARK,
				output.toArray(new TypedNode[]{}));
			if(slaving)app.openSlave(contenter.newSlaveApp(listing));
			else app.addContent(listing);
		}
		else{
			RecordContent content=newSelectionContent();
			if(slaving)app.openSlave(RecordContenter.newSlaveApp(app,content));
			else app.addContent(content);
		}
	}
	private String newTableExportHtml(final String title){
		return new HtmlBuilder(RenderTarget.Outlook){
			FieldTableView table=contenter.table;
			final FieldSet fields=table.fields;
			@Override
			protected String pageTitle(){
				return "Search results: "+title;
			};
			@Override
			public String newPageContent(){
				StringBuilder sb=new StringBuilder(
					"<h2>"+title+"</h2>"+
					"<table class=para border=1 cellpadding=2 cellspacing=0>" +
					"<tr align=left>\n<th>" +
					fields.toString().replaceAll("\t","</th><th>")+
					"</th></tr>\n");
				for(Stateful node:output)
					sb.append(((PubFields)fields).newExportRow((TypedNode)node, false)+"\n");
				sb.append("</table>");
				return sb.toString();
			}
		}.buildPage();
	}
}