package pub.view;
import static applicable.HtmlEditEncoder.*;
import static facets.core.app.AppConstants.*;
import static facets.core.app.AppSurface.ContentStyle.*;
import static facets.facet.app.FacetConstants.*;
import static facets.facet.app.FacetPreferences.*;
import static facets.util.tree.Nodes.*;
import static java.awt.event.KeyEvent.*;
import static pdft.PdfCore.*;
import static pub.PubIssue.*;
import static pub.PubValues.*;
import static pub.view.FieldsSpec.*;
import static pub.view.PubFields.*;
import facets.core.app.ActionAppSurface;
import facets.core.app.AppActions;
import facets.core.app.AppContenter;
import facets.core.app.AppSpecifier;
import facets.core.app.AppSurface.ContentStyle;
import facets.core.app.FeatureHost;
import facets.core.app.PagedContenter;
import facets.core.app.ViewerContenter;
import facets.core.superficial.SIndexing;
import facets.core.superficial.STarget;
import facets.core.superficial.app.SContenter;
import facets.core.superficial.app.SSurface;
import facets.facet.AreaFacets;
import facets.facet.FacetFactory;
import facets.facet.FacetFactory.SuggestionsCoupler;
import facets.facet.app.FacetAppActions;
import facets.facet.app.FacetAppActions.BarHide;
import facets.facet.app.FacetAppSpecifier;
import facets.facet.app.FacetAppSurface;
import facets.facet.app.FacetPreferences;
import facets.facet.kit.Toolkit;
import facets.facet.kit.swing.KitSwing;
import facets.util.Debug;
import facets.util.FileSpecifier;
import facets.util.TextLines;
import facets.util.Util;
import facets.util.app.AppValues;
import facets.util.app.Events;
import facets.util.app.HostBounds;
import facets.util.tree.DataNode;
import facets.util.tree.NodeList;
import facets.util.tree.Nodes;
import facets.util.tree.TypedNode;
import facets.util.tree.ValueNode;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.SwingUtilities;
import pdft.PdfCore;
import pub.PubValues;
import applicable.HtmlEditEncoder;
import applicable.field.CodeQuery;
import applicable.field.FieldSet;
import applicable.field.ValueField;
public class PubsView extends FacetAppSpecifier{
	final static boolean dev=System.getProperty("PubsViewDev")!=null;
			static abstract class SlaveSpec extends PubsView{
		private final AppSpecifier master;
		private final FacetFactory ff;
		SlaveSpec(PubsView master,FacetFactory ff){
			super(master);
			this.master=master;
			this.ff=ff;
		}
		final FacetAppSurface newSlave(){
			return newApp(ff,null);
		}
		@Override
		final public boolean hasSystemAccess(){
			return false;
		}
		@Override
		final public ContentStyle contentStyle(){
			return ContentStyle.SINGLE;
		}
		@Override
		final public PagedContenter[]adjustPreferenceContenters(SSurface surface,
				PagedContenter[]contenters){
			return((FacetAppSpecifier)master).adjustPreferenceContenters(surface,
					contenters);
		}
		@Override
		final protected FacetAppSurface newApp(FacetFactory ff,FeatureHost host){
			adjustValues();
			return new FacetAppSurface(this,ff){
				@Override
				protected Object getInternalContentSource(){
					return "Dummy";
				};
				@Override
				protected SContenter newContenter(Object source){
					return newSlaveContenter(this);
				}
			};
		}
		protected abstract SContenter newSlaveContenter(FacetAppSurface app);
	}
	private FieldsSpec addView=false?FieldsSpec.VIEW_All:null;
	private PubsView(){
		super(PubsView.class);
		System.setProperty(KEY_DIR_USER,DIR_USER.getName());
		if(!userView)trace(": userView=",userView);
		if(offNetwork)Util.printOut("PubsView: offNetwork=",offNetwork);
	}
	private PubsView(PubsView master){
		super(master);
		addView=master.addView;
	}
	@Override
	protected boolean userLog(){
		return!dev;
	}
	@Override
	public boolean headerIsRibbon(){
		return args().getOrPutBoolean(ARG_RIBBON,false);
	}
	@Override
	public Object[][]decorationValues(){
		ValueField[]standards=newStandards(null);
		return joinDecorations(super.decorationValues(),joinDecorations(
				searchView?new Object[][]{
					{NATURE_APP_ICON_LARGE,"","pubs48.png"},
					{NATURE_APP_ICON_INTERNAL,"","pubs48.png"},
					{CodeQuery.CODE_WITHOUT,"","","Removes selected record from current results"},
					{ListingSearcher.TITLE_CLEAR,"Clear Results","","Shows all records in database"},
					{ListingViewable.TITLE_EXPORT,"","","View current results as browser page"},
					{standards[STD_LINKNUM].name,"","","Publication number"},
					{standards[STD_ISSUE].name,"","","Issue number (1 if blank)"},
					{standards[STD_DETAILS].name,"","","Core information on publication"},
					{standards[STD_COMMENTS].name,"","","Temporary or supplementary details"},
					{standards[STD_TYPE].name,"","","If [Admin] refer to Technical Author"},
					{standards[STD_STATUS].name,"","","Planned/Draft/Ready/Released/Archived"},
					{standards[STD_RELEASE].name,"","","Release month and year"},
					{standards[STD_ORDER].name,"","","Order or other reference"},
					{standards[STD_ORIGINATOR].name,"","","Initials of designer/engineer(s)"},
					{ListingSearcher.TITLE_BOX,"","","Enter one or more text items, then click Go or hit Enter"},
					{ListingSearcher.TITLE_MATCH,"","","Match all or any items?"},
					{ListingSearcher.TITLE_EXACT,"","","Match each item exactly?"},
					{ListingSearcher.TITLE_REFS,"","","Search within PDF texts"},
					{ListingSearcher.TITLE_CLEAR,"","","Shows all records in database"},
					{ListingSearcher.TITLE_NO_RESULTS,"","","Search details and result counts appear here"},
				}
			:new Object[][]{
			{NATURE_APP_ICON_LARGE,"","pubs48.png"},
			{NATURE_APP_ICON_INTERNAL,"","pubs48.png"},
			{standards[STD_LINKNUM].name,"","","Publication number"},
			{standards[STD_ISSUE].name,"","","Issue number (1 if blank)"},
			{standards[STD_DETAILS].name,"","","Core information on publication"},
			{standards[STD_COMMENTS].name,"","","Temporary or supplementary details"},
			{standards[STD_TYPE].name,"","","If [Admin] refer to Technical Author"},
			{standards[STD_STATUS].name,"","","Planned/Draft/Ready/Released/Archived"},
			{standards[STD_RELEASE].name,"","","Release month and year"},
			{standards[STD_ORDER].name,"","","Order or other reference"},
			{standards[STD_ORIGINATOR].name,"","","Initials of designer/engineer(s)"},
			{standards[STD_STD].name,"","","Standard publication?"},
			{FieldSet.TITLE_CHOOSE,"Check each column to display.","",
				"A minimum number must be checked; obligatory columns always disabled"},
			{ListingSearcher.TITLE_BOX,"","","Enter one or more text items, then click Go or hit Enter"},
			{ListingSearcher.TITLE_MATCH,"","","Match all or any items?"},
			{ListingSearcher.TITLE_EXACT,"","","Match each item exactly?"},
			{ListingSearcher.TITLE_REFS,"","","Search within PDF texts"},
			{ListingSearcher.TITLE_CLEAR,"","","Shows all records in database"},
			{ListingSearcher.TITLE_NO_RESULTS,"","","Search details and result counts appear here"},
			{ListingSearcher.TITLE_RETITLE,"","","Change title of current results"},
			{ListingSearcher.ARG_SEARCH,"Search for number","","Equivalent to '\\num ...'; blank with *"},
			{ListingViewable.TITLE_PDF,"","","Or click hyperlink"},
			{ListingViewable.TITLE_EXPORT,"","","Opens new page with current results and sort"},
			{OpenPreferences.TITLE_LONG,"","","Set opening values"},
			{OpenPreferences.TITLE_SHORT,"","","Choose opening view and action"},
			{OpenPreferences.TITLE_PROGRAMS,"","","Define PDF reader and HTML editors"},
			{CodeQuery.CODE_WITHOUT,"","","Removes selected record from current results"},
			{ListingContenter.ARG_TREE,"Table tree","","Show tree view of source records"},
			{RecordContenter.ARG_OPEN,"Open &record","","Open first record in opening view"},
			{FieldsSpec.ARG_OPEN,"&View","","View for first table to be launched"},
			{RecordViewable.TITLE_STATE,"","save.gif","Export state (Ctrl+S)",KEY_CTRL+VK_S},
			{RecordViewable.TITLE_RESET,"Undo changes","","Reset to saved state (Ctrl+Z)",
				KEY_CTRL+VK_Z},
			{RecordViewable.TITLE_FORM,"","viewform_obj.gif","Export form (Ctrl+P)",KEY_CTRL+VK_P},
			{RecordViewable.TITLE_NOTE_NEW,"","new_untitled_text_file.gif","Add new note (F2)",VK_F2},
			{RecordViewable.TITLE_NOTE_TOP,"","text_edit.gif","Edit latest note (Shift+F2)",KEY_SHIFT+VK_F2},
			{RecordViewable.TITLE_NOTES_ALL,"","segment_edit.gif","Edit all notes"},
			{ListingViewable.OPEN_TAB,"","","",VK_F3},
			{ListingViewable.OPEN_SLAVE,"","","",KEY_SHIFT+VK_F3},
			{ListingViewable.TITLE_COMMENTS,"Edit &Comments","","",VK_F2},
			{ListingViewable.TITLE_PRIORITY,"Set &Priority"},
			{PdfCore.KEY_READER,"PDF"},
			{HtmlEditEncoder.KEY_STRONG,"HTML &Strong"},
			{HtmlEditEncoder.KEY_WEAK,"HTML &Weak"},
			{"PriorityHigh","","waiting.gif","High priority"},//list-moveup.gif
			{"PriorityMedium","","expandAll.gif","Medium priority"},//line_match.gif
			{"PriorityLow","","collapseall.gif","Low priority"},//list-movedown.gif
			{FieldSet.TITLE_CHOOSE_SIDEBAR,"Column Chooser","","Sets columns to appear in view"},
			{BarHide.Toolbar.title.replaceAll("\\|.*",""),"Search Bar"},
			{BarHide.Toolbar.title,"Search Bar","","",KEY_CTRL+VK_F},
			{TITLE_CONTENT_NEW,"Open &View: ","","Open a new view of the database table"},
//			{TITLE_WINDOW_CLOSE,"Close"},
		},!headerIsRibbon()?new Object[][]{}:new Object[][]{
			{TITLE_WINDOW_ACTIVATE,"View","","Switch to existing view"},
			{TITLE_WINDOW_CLOSE,"Close View","","Remove existing view"},
			{TITLE_APP_CLOSE,"Close View","","Close program"},
			{ListingSearcher.TITLE_MENU,"","","List and manage search results"},
		}));
	}
	private void openingError(FacetFactory ff,String msgTail){
		ff.warningCritical(searchView?"PubSearch":"PubsView",
				new RuntimeException(msgTail+"."),true);
		System.exit(0);
	}
	@Override
	protected FacetAppSurface newApp(FacetFactory ff,final FeatureHost host){
		try{
			if(searchView&&!("admin\n"+new TextLines(new File(VIEW_DIR,"roll")).readLinesString()
					).contains(userName))openingError(ff,"user '"+userName+"' not enrolled");
		}catch(IOException e1){
			throw new RuntimeException(e1);
		}
		return new FacetAppSurface(this,ff){
			final String fileName=ListingContent.fileSpec.newFileName(VIEW_TITLE);
			@Override
			protected Object[]getFixedOpeningContentSources(){
				return false&&dev?new Object[]{getInternalContentSource(),getInternalContentSource()}
					:super.getFixedOpeningContentSources();
			}
			@Override
			public Object getInternalContentSource(){
				try{
					Events.traceEvent(">Creating checked listing for "+fileName);
					long now=System.currentTimeMillis();
					ValueNode build=state();
					ListingContent listing=ListingContent.newChecked(fileName);
					Events.traceEvent(">Created listing "+Debug.info(listing));
					return listing;
				}catch(IOException e){
					throw new RuntimeException(e);
				}catch(Error e){
					openingError(ff,"bad data");
					return null;
				}
			}
			@Override
			protected void appOpened(){
				int openAt=ListingViewable.TARGETS_OPEN,tabOrWindow=0;
				STarget open=true?activeContentTargeter().content().elements()[openAt].target()
						:(findActiveContent().contentFrame()).elements()[openAt];
				if(false?dev:args().getBoolean(ARG_OPEN))
					((SIndexing)open).setIndex(tabOrWindow);
			}
			@Override
			protected void appClosing(){
				Util.stopProcesses();
				if(!searchView)try{
					PubFiles.checkUpdateListing(dialogs(),fileName);
				}catch(IOException e){
					throw new RuntimeException(e);
				}
			}
			@Override
			protected SContenter newContenter(Object source){
				if(!isBuilt())addView=addView==null?searchView?VIEW_PUBSEARCH:userView?VIEW_WIP
						:FieldsSpec.getOpenView(args())
						:addView==VIEWS[VIEWS.length-1]?VIEWS[0]:VIEWS[addView.ordinal()+1];
				return source instanceof RecordContent?
						new RecordContenter((RecordContent)source,spec,this,false)
					:addView.newFields(PubsView.this).newTableContenter((ListingContent)source,this);
			}
			@Override
			protected FeatureHost getPassedHost(){
				return host;
			}
			@Override
			protected String newTitleBarText(){
				return searchView?"PubSearch"
						:super.newTitleBarText().replaceFirst("\\*","")+(userView?"":" ADMIN");
			}
			@Override
			public FileSpecifier[]getFileSpecifiers(){
				throw new RuntimeException("Not implemented in "+Debug.info(this));
			};
		};
	}
	@Override
	public ContentStyle contentStyle(){
		return !userView?super.contentStyle():searchView?SINGLE:TABBED;
	}
	@Override
	protected void addNatureDefaults(ValueNode root){
		super.addNatureDefaults(root);
		if(false)ValueNode.putCheckKey="paneSplits_ListingContenter_2";
		mergeContents(root,new Object[]{
			NATURE_ICON_PATH+"=icon",
			HostBounds.NATURE_SIZER_DEFAULT+"=0.66,0.66",
			HostBounds.NATURE_SIZE_MIN+"=400,300",
		});
	}
	@Override
	public Toolkit newToolkit(){
		boolean noApplet=true;
		return new KitSwing(noApplet,false,
				noApplet&&args().getOrPutBoolean(NATURE_SWING_SYSTEM,false));
	}
	@Override
	protected AppActions newActions(ActionAppSurface app){
		return new FacetAppActions(app){
			@Override
			protected STarget[]newAppAreaElements(){
				STarget[]elements=super.newAppAreaElements();
					elements[TARGETS_NEW]=new SIndexing(TITLE_CONTENT_NEW,FieldsSpec.VIEWS,addView,
							new SIndexing.Coupler(){
					@Override
					public void indexSet(SIndexing i){
						addView=(FieldsSpec)i.indexed();
						app.addInternalContent();
					}
				});
				return elements;
			}
			@Override
			protected boolean contentIsRemovable(String dialogTitle,AppContenter content){
				ViewerContenter vc=(ViewerContenter)content;
				if(!vc.hasChanged()||vc instanceof ListingContenter)return true;
				else switch(app.dialogs().warningYesNoCancel("Export record?",
				"Export state for "+vc.title().replace("*","")+"?")){
					case Yes:try{
						vc.saveToSink(vc.sink());
					}catch(IOException e){
						throw new RuntimeException(e);
					}return true;
					case No:return true;
					default:return false;
				}
			}
		};
	}
	@Override
	public PagedContenter[]adjustPreferenceContenters(SSurface surface,
			PagedContenter[]pages){
		return new PagedContenter[]{
			pages[PREFERENCES_GRAPH],
			pages[PREFERENCES_TRACE],
			pages[PREFERENCES_VALUES],
		};
	}
	@Override
	protected FacetPreferences newArgPreferences(SSurface headless,FacetFactory ff){
		if(false)ValueNode.putCheckKey=KEY_STRONG;
		return new OpenPreferences(this,headless,ff);
	}
		@Override
		public void adjustValues(){
			super.adjustValues();
			ValueNode args=state(PATH_ARGS);
			if(false&&args.get(KEY_READER)==null){
	//			pdfReader=args.getOrPutString(KEY_READER,pdfReader);
				strongEditor=args.getOrPutString(KEY_STRONG,strongEditor);
				weakEditor=args.getOrPutString(KEY_WEAK,weakEditor);
			}
			String type=SuggestionsCoupler.TYPE;
			ValueNode suggestions=state(type),areas=state(AreaFacets.TYPE);
			if(suggestions.children().length==0)suggestions.setChildren(
				Nodes.decode(new ValueNode(type,"ListingViewable",new Object[]{
					"ACED0005757200175B4C6A6176612E696F2E53657269616C697A61626C653BAED009AC53D7ED490200007870000000097400045C6973737400085C697373203220337400225C6973732031302031312031322031332031342031352031362031372031382031397400115C737461742064726120706C61207265617400047639393474001067687820657870616E646572206862787400076C7074206862747400036D3130740003393030FFFFFFFF"	
				}) 
			));
			else{
				DataNode trim=(DataNode)suggestions.children()[0];
				List<String>values=new ArrayList(Arrays.asList(trim.values()));
				int max=100;
				if(values.size()>max)values.retainAll(values.subList(0,max));
				trim.setValues(values.toArray());
			}
			if(areas.values().length==0)areas.setValues(new Object[]{
					"tableColumns_FieldsSpec_keys="+
							"Release,Status,Issue,Comments,Due,Number,Details,Type,Originator,Order,Priority", 
					"tableColumns_FieldsSpec_values="+
							"1193,1318,816,1467,1027,1475,6500,1617,1318,1758,1216",
			});
			if(!userView)return;
			areas.put("paneTabsAndHides_ListingContenter_1","0,"+(searchView?"1":"0"));
			if(true||!searchView)return;
			String linkName="PubSearch.lnk";
			File linkTo=new File(DIR_USER,linkName);
			if(!linkTo.exists())try{
				Util.copyFile(new File(MASTER_DIR,linkName),linkTo);
				}catch(IOException e){
					throw new RuntimeException(e);
				}
			}
	public static String newCookieKey(){
		String dateStamp="22 Jun 2012 10:33:07";
		return dateStamp;
	}
	void openPdf(String path,int openPage){
		PdfCore.openViewPdf((path.startsWith(ROOT_TECHPUBS.replace("/","\\"))?"":ROOT_PDFS)
				+path,openPage);
	}
	void openPage(String page){
		try{
			File file=new File(AppValues.userDir(),"export.html");
			new TextLines(file).writeLines(page.split("\n"));
			String path=file.getCanonicalPath();
			Util.windowsOpenUrl("file://" +path);
			if(false&&!userView)Runtime.getRuntime().exec(new String[]{
				new File(Util.programs32,HtmlEditEncoder.strongEditor).getCanonicalPath(),
						path});
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	public static void main(String[]args){
		new PubsView().buildAndLaunchApp(args);
	}
}
