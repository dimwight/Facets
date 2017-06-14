package facets.core.app;
import facets.core.app.Dialogs.Response;
import facets.core.superficial.app.SContenter;
import facets.core.superficial.app.SSurface.WindowAppSurface;
import facets.facet.kit.DialogHost;
import facets.util.Debug;
import facets.util.FileSpecifier;
import facets.util.Tracer;
import facets.util.app.WatchableOperation;
import java.io.File;
import java.io.FileFilter;
import java.util.HashMap;
import java.util.Map;
/**
Builds and launches modal dialogs on behalf of an {@link AppSurface}. 
<p>{@link Dialogs} launches dialogs of two kinds:</p>
<ul>
  <li>general-purpose input dialogs
  <li>dialogs hosting client-defined {@link PagedSurface}s as described for 
     {@link #launchSurfaced(Surfacer, String, PagedActions, PagedContenter...)}
</ul>
<p>{@link Dialogs} instances will be created by subclasses of {@link AppSurface}.  
 */
public abstract class Dialogs extends Tracer{
	/**
	Parameter object for message dialogs. 
	 */
	public static class MessageTexts{
		final public String title,rubricTop,rubricTail,rubricBody,rubricsRaw;
		public MessageTexts(String title,String rubricTop,String rubricBody,
				String rubricTail){
			this.title=title;
			this.rubricTop=rubricTop;
			this.rubricTail=rubricTail;
			this.rubricBody=rubricBody;
			rubricsRaw=rubricTop+(rubricBody.equals("")?"":("\n"+rubricBody))+"\n"+rubricTail;
		}
		@Override
		public String toString(){
			return title+"\n"+rubricsRaw;
		}
	}
	/**
	Parameter object for {@link Dialogs#warningException(MessageTexts, Exception, boolean)}. 
	 */
	public static final class ExceptionTexts extends MessageTexts{
		public ExceptionTexts(String appTitle,Exception e){
			this(appTitle+" - Exception","An exception has occurred: ","",e.getMessage());
		}
		public ExceptionTexts(String title,String rubricTop,String rubricBody,
				String rubricTail){
			super(title,rubricTop,rubricBody,rubricTail);
		}
	}
	/**
	Type-safe definitions of possible dialog responses. 
	 */
	public static enum Response{Ok,Cancel,Yes,No,Close}
	/**
	Enables a {@link Dialogs} to create (and re-use) {@link PagedSurface}s. 
	 */
	public interface Surfacer{
		/**
		Create a {@link PagedSurface} to expose any {@link PagedContenter}s type-identical to
		those passed. 
		 * @param title for the dialog window
		 * @param host dialog window for the surface to be created
		 * @param actions defining top-level dialog buttons
		 * @param contents the initial contenters; can be replaced in
		the surface by any type-identical array
		 * @param app application surface
		 */
		PagedSurface newSurface(String title,HideableHost host,PagedActions actions,
				PagedContenter[]contents,WindowAppSurface app);
	}
	final public static String DEBUG_RESIZABLE="dialogsAllResizable",
		KEYTOP_NATURE_SIZE="dialogSize_",KEYTOP_BOUNDS="bounds_";
	public final WindowAppSurface app;
	private final Map<Object,PagedSurface>surfaces=new HashMap();
	private boolean dialogOpen;
	protected Dialogs(WindowAppSurface app){
		this.app=app;
	}
	/**
	Creates the {@link PagedSurface} to expose a set of {@link SContenter}s.
	 <p>The surface built by the {@link Dialogs.Surfacer} on the first invocation 
	 for the {@link PagedContenter}[] should be 
	 stored for use in subsequent invocations with equivalent arrays. 
	  @param surfacer to construct the surface on first invocation
	  @param title used by surfacer
	  @param actions used by surfacer
	  @param contents will be passed either to the surfacer or to
	 {@link PagedSurface#replaceContents(PagedContenter[])} in a previously-built
	 surface
	 */
	public final Response launchSurfaced(final Dialogs.Surfacer surfacer,final String title, 
			final PagedActions actions,final PagedContenter...contents){
		Object surfaceKey=PagedSurface.contentsKey(contents)
			+surfacer.getClass().getName();
		PagedSurface surface=surfaces.get(surfaceKey);
		if(surface==null){
			surface=surfacer.newSurface(title,newHost(),actions,contents,
					app);
			surface.buildRetargeted();
			surfaces.put(surfaceKey,surface);
		}
		else surface.replaceContents(contents);
		dialogOpen=true;
		Response response=((DialogHost)surface.host()).launchWindowedSurface(surface,app);
		if(response==Response.Cancel)for(PagedContenter c:contents)c.reverseChanges();
		return response;
	}
	/**
	Is a dialog surface already open? 
	<p>Only applies to dialogs launched with
     {@link #launchSurfaced(Surfacer, String, PagedActions, PagedContenter...)}
	 */
	public boolean dialogOpen(){
		return dialogOpen;
	}
	/**
	Enables concrete subclasses to create an appropriate {@link HideableHost}
	for use by 
     {@link #launchSurfaced(Surfacer, String, PagedActions, PagedContenter...)}
	 */
	protected abstract HideableHost newHost();
	final public void infoMessage(String title,String rubric){
		infoMessage(new MessageTexts(title,rubric,"",""));
	}
	final public int deleteDirFiles(final String title,final File dir,
			final FileSpecifier specifier){
		File[]files=dir.listFiles(new FileFilter(){
			@Override
			public boolean accept(File file){
				return specifier.specifies(file);
			}
		});
		String rubric=specifier.rubric;
		if(files.length==0){
			infoMessage(title,"No "+rubric +" found in '"+dir+"'.");
			return 0;
		}
		if(warningYesNo(title,"Are you sure you want to delete all "+rubric+
				" in '"+dir+"'?")!=Response.Yes)return -1;
		int deleteds=0;
		for(File file:files){
			file.delete();
			deleteds++;
		}
		infoMessage(title,"Deleted all " +deleteds+" "+rubric+" in '"+dir+"'.");
		return deleteds;
	}
	public Response warningYesNoCancel(String title,String rubric){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	public Response warningYesNo(String title,String rubric){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	public Response warningException(MessageTexts tt,Exception e,boolean inOpen){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	public void textPane(String title,String rubric,int rows,int cols){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	public File saveFile(File proposed,FileSpecifier[] filters){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	public File openFile(FileSpecifier[] filters){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	public void infoMessage(MessageTexts texts){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	public void htmlPane(String title,String path){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	public String getTextInput(String title,String rubric,String proposal, int cols){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	public void errorMessage(String title,String rubric){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	public Response confirmYesNo(String title,String rubric){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	public Response confirmOKCancel(String title,String rubric){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	public File confirmGetFile(String title,String path){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
}
