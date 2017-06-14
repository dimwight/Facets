package facets.facet.app;
import static facets.core.app.AppConstants.*;
import static facets.core.app.AppSurface.ContentStyle.*;
import static facets.core.app.Dialogs.Response.*;
import facets.core.app.ActionAppSurface;
import facets.core.app.AppConstants;
import facets.core.app.AppContenter;
import facets.core.app.AppSpecifier;
import facets.core.app.Dialogs;
import facets.core.app.ViewerContenter;
import facets.core.superficial.SIndexing;
import facets.core.superficial.STarget;
import facets.core.superficial.STrigger;
import facets.core.superficial.TargetCore;
import facets.core.superficial.Notifying.Impact;
import facets.core.superficial.SIndexing.Coupler;
import facets.core.superficial.app.SAreaTarget;
import facets.core.superficial.app.SHost;
import facets.util.Debug;
import facets.util.FileSpecifier;
import facets.util.ItemList;
import facets.util.app.AppFileValues;
import facets.util.app.WatchableOperation;
import java.io.File;
import java.io.IOException;
/**
{@link FacetAppActions} for an application that reads and writes files. 
<p>{@link FileAppActions} extends its superclass with a practical API for
using files as content sources and sinks.  
 */
public class FileAppActions extends FacetAppActions{
	private static final int FIRST=FacetAppActions.TARGETS_LAST+1;
	/** Index into file {@link STarget}s added in {@link #newAppAreaElements()}. */
	public static final int TARGETS_FILE=FIRST,TARGETS_RECENT=FIRST+1;
	public static final String TITLE_SAVE=TITLE_FILE_SAVE,TITLE_REVERT=TITLE_FILE_REVERT;
	public static boolean dummy;
	private final Coupler recentCoupler=new SIndexing.Coupler(){
		@Override
		public Object[]getIndexables(){
			File[]recentFiles=values.recentFiles();
			return recentFiles.length>0?recentFiles:SIndexing.NO_INDEXABLES;
		}
		@Override
		public void indexSet(SIndexing i){
			fileOpen((File)i.indexed());
		}
		@Override
		public String[]newIndexableTitles(SIndexing x){
			File[]recentFiles=values.recentFiles();
			int recentCount=recentFiles.length;
			x.setLive(x.isLive()&&recentCount>0);
			if(recentCount==0)return new String[]{"No recent files"};
			String[]titles=new String[recentCount];
			for(int i=0;i<titles.length;i++)
				titles[i]=(i+1)+" "+recentFiles[i].getName()+"|"+recentFiles[i].getPath();
			return titles;
		}
	};
	private final STarget open=new STrigger(TITLE_FILE_OPEN,new STrigger.Coupler(){
	  public void fired(STrigger t){
	  	fileOpen(null);
	  }
	}),				
	save=new STrigger(TITLE_FILE_SAVE,new STrigger.Coupler(){
	  public void fired(STrigger t){
			fileSave((ViewerContenter)app.findActiveContent());
		}
	}),				
	saveAs=new STrigger(TITLE_FILE_SAVE_AS,new STrigger.Coupler(){
	  public void fired(STrigger t){fileSaveAs();}
	}),				
	revert=new STrigger(TITLE_FILE_REVERT,new STrigger.Coupler(){
	  public void fired(STrigger t){app.revertContent();}
	}),
	fileMultiple[]={
		new STrigger(TITLE_FILE_CLOSE,new STrigger.Coupler(){
		  public void fired(STrigger t){fileClose();}
		}),				
		new STrigger(TITLE_FILE_CLOSEALL,new STrigger.Coupler(){
		  public void fired(STrigger t){fileCloseAll();}
		}),
	},
	recent=new SIndexing(AppFileValues.STATE_TYPE_RECENT,recentCoupler);
	/**
	Adds target groups to super implementation. 
	<p>Targeter elements can be accessed using the following additional indices:
	<ul>
	<li>{@link #TARGETS_FILE} - {@link #fileOpen(File)} etc; 
	<li>{@link #TARGETS_RECENT} - recent files list
	</ul>
	<p>The contents of {@link #TARGETS_FILE} are determined by 
	{@link ActionAppSurface#spec}
	 */
	@Override
	protected STarget[]newAppAreaElements(){
		values.updateRecentFiles(null);
		AppSpecifier spec=app.spec;
		if(!spec.hasSystemAccess())throw new IllegalStateException(
				"No system access for "+Debug.info(app));
		ItemList<STarget>items=new ItemList(STarget.class);
		boolean forSlave=spec.forSlave();
		if(!forSlave&&!dummy)items.addItem(open);
		if(spec.canSaveContent()){
			items.addItem(save);
			items.addItem(saveAs);
		}
		if(spec.canEditContent())items.addItem(revert);
		if(app.contentStyle!=SINGLE)items.addItems(fileMultiple);
		TargetCore menuTopItems=new TargetCore("File Menu",items.items());
		return TargetCore.join(super.newAppAreaElements(),
				forSlave?new STarget[]{menuTopItems}
				:new STarget[]{menuTopItems,recent});
	}
	private final AppFileValues values;
	private final boolean appIsDesktop;
	public FileAppActions(final ActionAppSurface app){
		super(app);
		appIsDesktop=app.contentStyle==DESKTOP;
		values=new AppFileValues(app.spec){
			protected File getOpeningRecent(String[]recentPaths){
				return app.dialogs().confirmGetFile(TITLE_FILE_OPEN_PREVIOUS,recentPaths[0]);
			}
			protected File getOpeningOther(FileSpecifier[]specifiers){
				return app.dialogs().openFile(specifiers);
			}
			protected void gotNoOpening(){
				final Dialogs dialogs=app.dialogs();
				if(appIsDesktop)return;
				else if(app.spec.canCreateContent())
					dialogs.infoMessage(TITLE_NO_FILE,TITLE_NO_FILE);
				else{
					if(true)dialogs.infoMessage(TITLE_NO_NEW,TITLE_NO_NEW);
					System.exit(0);
				}
			}
			public FileSpecifier[]getOpenSpecifiers(){
				return ((FacetAppSurface)app).getFileSpecifiers();
			}
		};
	}
	/**
	Re-implementation adjusting file menu live states. 
	 */
	@Override
	final protected void appRetargeted(){
		AppContenter content=app.findActiveContent();
		boolean empty=content==app.emptyContent;
		STarget[]elements=app.surfaceTargeter().target().elements(
				)[TARGETS_FILE].elements();
		for(int i=1;i<elements.length;i++)elements[i].setLive(!empty);
		boolean contentChanged=content instanceof ViewerContenter
			&&((ViewerContenter)content).hasChanged();
		save.setLive(contentChanged);
		revert.setLive(contentChanged);
	}
	/**
	Re-implementation of superclass method. 
	<p>Depending on nature values, usually launches file chooser dialog. 
	 */
	@Override
	public File getOpeningContentSourceFile(){
		return values.getOpeningFile(!appIsDesktop&&!app.spec.canCreateContent());
	}
	private void fileOpen(final File open){
		WatchableOperation op=new WatchableOperation("FileAppActions.fileOpen"){
			public void doSimpleOperation(){
				File file=open;
				if(file==null||!file.isFile())
					file=app.dialogs().openFile(values.getOpenSpecifiers());
				if(file==null)return;
				values.updateRecentFiles(file);
				recent.setLive(true);
				SAreaTarget area=app.firstContentArea(file);
				if(area!=null){
					area.ensureActive(Impact.ACTIVE);
					return;
				}
				if(app.contentStyle==SINGLE){
					if(!contentIsRemovable(AppConstants.TITLE_CLOSE_CONTENT,
							app.findActiveContent()))
						return;
					app.replaceSingleContent(file);
				}
				else app.addContent(file);
			}
		};
		if(true)app.runWatched(op);
		else op.doOperations();
	}
	private void fileSaveAs(){
		ViewerContenter content=(ViewerContenter)app.findActiveContent();
		if(dummy){
			dummySaveMsg(content);
			return;
		}
		Object sink=content.sink();
		Dialogs dialogs=app.dialogs();
		FileSpecifier[]specifiers=content.sinkFileSpecifiers();
		File saveFile=sink instanceof File?(File)sink
				:specifiers[specifiers.length-1].specifiedFile(sink);
		while((saveFile=dialogs.saveFile(saveFile,specifiers))!=null){
			boolean badSink=false;
			for(ViewerContenter c:app.findViewerContents()){
				Object checkSink=c.sink();
				if(checkSink==sink){
					if(badSink=!content.setSink(saveFile))
						dialogs.infoMessage("Can't Save","Cannot save as " +saveFile);
				}
				else if(checkSink instanceof File&&
						(badSink=((File)checkSink).equals(saveFile))){
					dialogs.infoMessage("Can't Save","This file is already open.");
					break;
				}
			}
			if(!badSink)break;
		}
		if(saveFile==null)return;
		if(saveFile.exists()&&dialogs.confirmYesNo("Save File",
				"Overwrite " +saveFile+"?")!=Yes)return;
		content.setSink(saveFile);
		fileSave(content);
	}
	private void dummySaveMsg(ViewerContenter content){
		app.dialogs().infoMessage("Dummy Save",content.title()+" would be saved here.");
	}
	private void fileSave(ViewerContenter content){
		if(dummy){
			dummySaveMsg(content);
			return;
		}
		Object sink=content.sink();
		if(!(sink instanceof File&&content.setSink(sink))){
			fileSaveAs();
			return;
		}
		File sinkFile=(File)sink;
		SHost host=app.host();
		Dialogs dialogs=app.dialogs();
		try{
			content.saveToSink(sinkFile);
			values.updateRecentFiles(sinkFile);
			if(false)dialogs.infoMessage("File saved","Saved as "+sinkFile.getAbsolutePath());
		}catch(IOException e){
			if(true)dialogs.errorMessage("File not saved",sinkFile.getAbsolutePath()+
					" could not be saved:\n"+e);
			else throw new RuntimeException(e);
		}
	}
	private void fileClose(){
		boolean lastContent=app.findViewerContents().length==1;
		if(appIsDesktop||!lastContent)
			app.removeActiveContent();
		else if(app.dialogs().confirmOKCancel("Last Content",
				"This will close $appTitle")==Ok)
			app.attemptClose();
	}
	private void fileCloseAll(){
		if(appIsDesktop)app.removeAllContent(false);
		else if(app.dialogs().confirmOKCancel("Closing All",
				"This will close $appTitle")==Ok)
			app.attemptClose();
	}
	@Override
	final protected boolean contentIsRemovable(String dialogTitle,AppContenter content){
		ViewerContenter vc=(ViewerContenter)content;
		if(!app.spec.canSaveContent()||!vc.hasChanged())return true;
		switch(app.dialogs().warningYesNoCancel(dialogTitle,
				"Save changes to "+vc.title()+"?")){
			case Yes:fileSave(vc);return true;
			case No:return true;
			case Cancel:default:return false;
		}
	}
	@Override
	public final boolean appCloseAcceptable(){
		if(!super.appCloseAcceptable())return false;
		AppContenter activeContent=app.findActiveContent();
		if(!(activeContent instanceof ViewerContenter))return true;
		Object sink=((ViewerContenter)activeContent).sink();
		if(sink instanceof File)values.updateRecentFiles((File)sink);
		return true;
	}
	final public AppFileValues values(){
		if(values==null)throw new IllegalStateException(
				"Null values in "+Debug.info(this));
		return values;
	}
}
