package pub.view;
import static facets.core.app.PathSelection.*;
import static facets.util.Regex.*;
import static facets.util.Util.*;
import static facets.util.tree.Nodes.*;
import static pub.PubValues.*;
import static pub.view.ListingViewable.*;
import static pub.view.PubFiles.*;
import static pub.view.RecordContent.*;
import static pub.view.RecordContenter.*;
import static pub.view.RecordProxy.*;
import facets.core.app.HtmlView;
import facets.core.app.HtmlView.InputView;
import facets.core.app.NestedView;
import facets.core.app.NodeViewable;
import facets.core.app.PathSelection;
import facets.core.app.TableView;
import facets.core.app.TreeView;
import facets.core.app.ViewerContenter;
import facets.core.superficial.SFrameTarget;
import facets.core.superficial.STarget;
import facets.core.superficial.STrigger;
import facets.core.superficial.STrigger.Coupler;
import facets.core.superficial.TargetCore;
import facets.core.superficial.app.SSelection;
import facets.core.superficial.app.SView;
import facets.core.superficial.app.SViewer;
import facets.core.superficial.app.ViewableAction;
import facets.facet.app.FacetAppSurface;
import facets.util.Debug;
import facets.util.HtmlBuilder;
import facets.util.HtmlBuilder.RenderTarget;
import facets.util.HtmlFormBuilder;
import facets.util.Objects;
import facets.util.OffsetPath;
import facets.util.Util;
import facets.util.app.AppValues;
import facets.util.tree.DataConstants;
import facets.util.tree.DataNode;
import facets.util.tree.ValueNode;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import pub.PubValues;
import applicable.HtmlEditEncoder;
import applicable.field.FieldFormBuilder;
class RecordViewable extends NodeViewable{
	private static final String CODE_NEW_NOTE="<p>[New note]<hr>";
	public static final String KEY_TREE="RecordTree";
	static final int TARGET_EXPORT=0,TARGET_ATTACH=1,TARGET_RESET=2,TARGET_NOTES=3;
	static final String TITLE_FORM="Form|Form",TITLE_STATE="State|State",
		TITLE_NOTE_TOP="Latest|Latest Note",TITLE_NOTE_NEW="New|New Note",
		TITLE_NOTES_ALL="All|All Notes",TITLE_RESET="Reset To Stored";
	static abstract class ExportCoupler extends STrigger.Coupler{
		private final boolean inPreview;
		ExportCoupler(boolean inPreview){
			this.inPreview=inPreview;
		}
		@Override
		public void fired(STrigger t){
			RecordViewable v=getViewable();
			if(t.title()==TITLE_FORM)v.exportForm();
			else{
				v.closeNoteEdits();
				v.content.saveState();
			}
		}
		protected abstract RecordViewable getViewable();
		final STarget newTarget(){
			return new TargetCore("E&xport|Record",new STrigger(TITLE_STATE,this),
					new STrigger(TITLE_FORM,this));
		}
	}
	private HtmlEditEncoder notes;
	private final STrigger attachmentsView=new STrigger("Attachments...",new Coupler(){
		@Override
		public void fired(STrigger t){
			if(!titleDir.exists()&&!titleDir.mkdir())throw new IllegalStateException(
					"Bad titleDir="+titleDir);
			else Util.windowsOpenFile(titleDir);
		}
	}),
	reset=new STrigger(TITLE_RESET,new STrigger.Coupler(){
		@Override
		public void fired(STrigger t){
			content.resetState();
		}
	});
	STrigger.Coupler notesCoupler=new STrigger.Coupler(){
		@Override
		public void fired(STrigger t){
			final String trigger=t.title();
			openEdits(trigger);
		}
	};
	private final STarget note=new TargetCore("Edit &Notes",userView?
			new STarget[]{new STrigger(TITLE_NOTE_NEW,notesCoupler),
				new STrigger(TITLE_NOTE_TOP,notesCoupler)}
			:new STarget[]{new STrigger(TITLE_NOTE_NEW,notesCoupler),
				new STrigger(TITLE_NOTE_TOP,notesCoupler),
				new STrigger(TITLE_NOTES_ALL,notesCoupler)}),
		export;
	private final File titleDir=new File(ATTACHMENTS_DIR,title().trim());
	protected final RecordContent content;
	private final FacetAppSurface app;
	private final boolean inPreview;
	RecordViewable(RecordContent content,FacetAppSurface app){
		this(content,app,null);
	}
	private void openEdits(final String trigger){
		final String title=title();
		final boolean editTop=trigger!=TITLE_NOTES_ALL;
		(notes=new HtmlEditEncoder(new File(
				userView?AppValues.userDir():PubValues.MASTER_DIR,
				title+DataConstants.EXT_HTML),
				editTop||userView){
			@Override
			protected File editor(boolean strong){
				return new File("C:/Program Files (x86)/OpenOffice3/program/swriter.exe");
			}
			@Override
			protected boolean editTop(){
				return editTop;
			}
			@Override
			protected HtmlBuilder newPageBuilder(){
				return new HtmlBuilder(){
					@Override
					protected String pageTitle(){
						return title+" - Notes";
					}
					@Override
					protected double pagePoints(){
						return 9;
					}
					@Override
					protected boolean inPageTable(){
						return inPreview;
					}
					@Override
					protected int pageInset(){
						return 10;
					}
					@Override
					public String newPageContent(){
						String notes=content.newNotesContent().replaceAll("_(\\d+\">)",_dummy +
								"$1");						
						if(false)Util.printOut(notes);						
						return (trigger==TITLE_NOTE_NEW&&!notes.contains(CODE_NEW_NOTE)?CODE_NEW_NOTE
								:"")+notes;
					}
				};
			}
			private final String _dummy=false?"-":"i";
			@Override
			protected String adjustNormalisedContent(String normalised){
				normalised=super.adjustNormalisedContent(normalised);
				if(false)Util.printOut(normalised);
				normalised=replaceAll(normalised,
						"<col[^>]+>\n","",
						"<td[^>]+>","<td>",
						"(<td>\\s*)<p>","$1",
						"<A HREF=\"(http://)?([0-9A-Z-"+_dummy +"]+)/?",
							"<a href=\"http://$2",_dummy+"(\\d+\">)","_$1"
				);
				if(false)Util.printOut(normalised);
				return normalised.trim().equals("")?EMPTY_NOTES
						:!normalised.contains(EMPTY_NOTES)?normalised
						:normalised.replaceAll("<hr>.*","");
			};
			@Override
			protected ValueNode encodingStore(){
				return guaranteedChild((DataNode)child(content.state(),TYPE_TEXTS),
						PubFiles.TYPE_ENCODED,TITLE_BODY);
			}
			@Override
			protected void fileContentStored(){
				for(ViewerContenter content:app.findViewerContents())
					if(content instanceof RecordContenter)
						((RecordContenter)content).refreshChangeTitle();
				notifyParent(Impact.CONTENT);
			}
		}).openEdits();
	}
	void closeNoteEdits(){
		if(notes!=null)notes.closeEdits();
	}
	protected RecordViewable(RecordContent content,FacetAppSurface app,ClipperSource clipper){
		super(content.viewable,clipper);
		this.content=content;
		this.app=app;
		inPreview=clipper==null;
		export=new ExportCoupler(inPreview){
			protected RecordViewable getViewable(){
				return RecordViewable.this;
			}
		}.newTarget();
		if(!inPreview&&true||!PubValues.userView)content.normaliseState();
		setSelection(getOffsetSelection(framed,app.spec.state(),KEY_TREE));
	}
	@Override
	protected ViewableAction[]viewerActions(SView view){
		return OPEN_ACTIONS;
	}
	@Override
	public boolean actionIsLive(SViewer viewer,ViewableAction action){
		SView view=viewer.view();
		return view instanceof HtmlView||view.title()==VIEW_TREE;
	}
	final protected boolean nodeActionLive(SViewer viewer,ViewableAction action){
		return super.actionIsLive(viewer,action);
	}
	@Override
	public SFrameTarget selectionFrame(){
		return !content.isDisposed()?super.selectionFrame():new SFrameTarget("Disposed");
	}
	@Override
	protected void actionTriggered(SViewer viewer,ViewableAction action){
		RecordContent content=this.content.newLinkedContent(title());
		if(action==OPEN_ACTIONS[1])app.openSlave(RecordContenter.newSlaveApp(app,content));
		else app.addContent(content);
	}
	final protected void nodeActionTriggered(SViewer viewer,ViewableAction action){
		super.actionTriggered(viewer,action);
	}
	@Override
	protected void viewerSelectionChanged(SViewer viewer,SSelection selection){
		if(false)ValueNode.putCheckKey=KEY_TREE;
		SView view=viewer.view();
		String title=view.title();
		if(title==VIEW_TREE||title==VIEW_STATE){
			putSelectionOffsets((PathSelection)setSelection(
					procrust((PathSelection)selection,framed)),
					app.spec.state(),KEY_TREE);
		}
		else if(title==VIEW_NOTES||title==VIEW_DATA){
			Object url=selection.content();
			if(url==null)return;
			String linked=url.toString().substring(HtmlBuilder.HTTP.length());
			if(linked.toLowerCase().endsWith(".pdf")){
				((PubsView)app.spec).openPdf(linked,0);
				return;
			}
			RecordContent content=this.content.newLinkedContent(linked);
			if(content!=null)try {
				app.addContent(content);
			}catch(Exception e) {
				PubFiles.checkLockException(e,app.dialogs());
			}
		}
		else throw new RuntimeException("Not implemented for "+view);
	}
	@Override
	protected SSelection newViewerSelection(final SViewer viewer){
		boolean changed=content.stateChanged();
		reset.setLive(changed);
		export.elements()[0].setLive(false&&PubsView.dev||changed);
		final SView view=viewer.view();
		final String title=view.title();
		final ValueNode state=content.state();
		if(view instanceof TreeView){
			PathSelection pathsIn=(PathSelection)selection();
			if(title==VIEW_TREE)return pathsIn;
			if(!Arrays.asList(pathMembers(pathsIn,0)).contains(state))return newMinimal(state);
			PathSelection procrusted=procrust(pathsIn,state);
			if(false)trace(".newViewerSelection: view="+view+" procrusted="+procrusted);
			return procrusted;
		}
		else if(view instanceof TableView)return((TableView)view).newViewerSelection(viewer,
			new PathSelection(Objects.newTyped(ValueNode.class,
					child(content.viewable,title).children()),OffsetPath.empty));
		else if(view instanceof NestedView)
			throw new RuntimeException("Not implemented in "+Debug.info(this));
		boolean input=view instanceof InputView;
		final HtmlFormBuilder data=title!=VIEW_DATA?null
				:newDataForm(RenderTarget.Swing,input);
			return new SSelection(){
				final String code=new HtmlBuilder(){
					@Override
					protected boolean inPageTable(){
						return data==null&&inPreview;
					}
					@Override
					final protected int pageInset(){
						return 5;
					}
					@Override
					protected double pagePoints(){
						return inPreview?11:12;
					}
					@Override
					public String newPageContent(){
						return data!=null?data.buildForm():content.newNotesContent();
					}
				}.buildPage();
				@Override
				public Object content(){
					return code;
				}
				@Override
				public Object single(){
					throw new RuntimeException("Not implemented in "+Debug.info(this));
				}
				@Override
				public Object[]multiple(){
					throw new RuntimeException("Not implemented in "+Debug.info(this));
				}
			};
	}
	@Override
	protected STarget[]lazyElements(){
		return new STarget[]{export,attachmentsView,reset,note};
	}
	private void exportForm(){
		((PubsView)app.spec).openPage(new HtmlBuilder(RenderTarget.Outlook){
			@Override
			protected String pageTitle(){
				return VIEW_TITLE+": "+title();
			}
			@Override
			public String newPageContent(){
				return newDataForm(rt,false).buildForm()+"<hr>"+content.newNotesContent();
			}
		}.buildPage());
	}
	HtmlFormBuilder newDataForm(RenderTarget rt,final boolean input){
		return new FieldFormBuilder(rt,FieldsSpec.DATA.newFields((PubsView)app.spec),
				fieldValues(content.state()),new String[]{
			"Number,Issue,Release,Due,Priority",
			"Details,Comments",
			"Type,Status,Order,Originator",
			"External,Links"
		}){
			final boolean isActive=!content.isDisposed()
					&&"Planned,Draft,Ready".contains(values.get("Status"));
			@Override
			protected boolean isNullField(String name,Object value){
				return super.isNullField(name,value)?true:
					!isActive&&"Priority,Due".contains(name);
			}
			@Override
			protected boolean hideNullField(String name,Object value){
				return !input||"External,LinksText".contains(name)
						||!isActive&&"Priority,Due,Issue".contains(name);
			}
			@Override
			protected boolean useInputField(String name){
				return input&&!"Number,External,Links".contains(name);
			}
		};
	}
}