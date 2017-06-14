package facets.util.app;
import static facets.util.tree.Nodes.*;
import facets.util.Debug;
import facets.util.FileSpecifier;
import facets.util.Tracer;
import facets.util.TracerInput;
import facets.util.tree.DataNode;
import facets.util.tree.ValueNode;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
/**
Manages file history and other file values. 
<p>{@link AppFileValues} encapsulates file-related operations that read and write 
values stored in an {@link AppValues}.  
 */
public abstract class AppFileValues extends Tracer{
	/**
	Key for flag specifying file behaviour. 
	 */
	public static final String NATURE_FILES="Files",NATURE_FILES_RECENT_MAX="maximumRecent",
		NATURE_FILES_OPENING="openingFile",KEY_VIEW="viewType";
	/**
	Type of session values node. 
	 */
	public static final String 
	STATE_TYPE_FILES="files",
	STATE_TYPE_RECENT="recentFiles",
	STATE_TYPE_DIALOGS="dialogs",
	STATE_FILES_PATH="path";
private final AppValues appValues;
	private final ValueNode natureFiles;
	private final int maxRecent;
	private File[]recentFiles;
	/**
	Unique constructor. 
	@param appValues stores values. 
	 */
	public AppFileValues(AppValues appValues){
		if(!appValues.hasSystemAccess())throw new IllegalStateException(
				"No system access in in "+Debug.info(this));
		else this.appValues=appValues;
		natureFiles=guaranteedChild(appValues.nature(),NATURE_FILES);
		maxRecent=natureFiles.getOrPutInt(NATURE_FILES_RECENT_MAX,9);
	}
	/**
	Returns a validated array of recently opened files.
	@return files updated by {@link #updateRecentFiles(File)}
	 */
	final public File[]recentFiles(){
		return recentFiles;
	}
	/**
	Maintains the list returned by {@link #recentFiles()}. 
	<p>Files are guaranteed to exist, and are returned in reverse order of opening.  
	@param add a file to add, or <code>null</code> to test the values stored
	@return the updated {@link ValueNode}
	 */
	final public ValueNode updateRecentFiles(File add){
		ValueNode state=appValues.state(STATE_TYPE_FILES,STATE_TYPE_RECENT);
		String[]existing=state.values();
		List<File>tested=new ArrayList();
		for(int i=0,count=tested.size();i<existing.length;i++){
			File test=new File(existing[i]);
			if(!test.isFile()||test.equals(add)||count++==maxRecent)continue;
			else if(!tested.contains(test))tested.add(test);
		}
		if(add!=null&&add.isFile()){
			tested.remove(add);
			tested.add(0,add);
			statePutPath(add.getParentFile());
		}
		state.setValues(recentFiles=tested.toArray(new File[]{}));
		return state;
	}
	/**
	Tries to get a suitable opening content file. 
	@param mustGet <code>null</code> return not allowed
	@return the file or <code>null</code> unless <code>mustGet</code> is <code>true</code>,
	in which case calls {@link #gotNoOpening()}
	 */
	final public File getOpeningFile(boolean mustGet){
		DataNode recent=updateRecentFiles(null);
		String[]recentPaths=recent.values();
		FileSpecifier[]specifiers=getOpenSpecifiers();
		File got=recentPaths.length>0?getOpeningRecent(recentPaths)
				:getOpeningOther(specifiers);
		if(got==null&&mustGet)got=getOpeningOther(specifiers);
		if(got==null||!got.isFile()){
			if(!mustGet)return got;
			else gotNoOpening();
		}
		else updateRecentFiles(got);
		return got;
	}
	/**
	Get {@link FileSpecifier}s specifying application content types. 
	@return by default {@link FileSpecifier#ALL} 
	 */
	public FileSpecifier[]getOpenSpecifiers(){
		return new FileSpecifier[]{FileSpecifier.ALL};
	}
	/**
	Called from {@link #getOpeningFile(boolean)}. 
	 */
	protected File getOpeningRecent(String[]recentPaths){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	/**
	Called from {@link #getOpeningFile(boolean)}. 
	 */
	protected File getOpeningOther(FileSpecifier[]specifiers){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	/**
	Called from {@link #getOpeningFile(boolean)}. 
	 */
	protected void gotNoOpening(){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	/**
	Gets the current file search path. 
	@return the last valid file passed to {@link #statePutPath(File)}, or else 
	(for Windows) the user desktop. 
	 */
	final public File stateGetPath(){
		String path=stateRoot().getString(STATE_FILES_PATH);
		File file=new File(path).getAbsoluteFile();
		if(path.equals("")||!file.isDirectory())
			statePutPath(file=new File(System.getProperty("user.home"),"Desktop"
			).getAbsoluteFile());
		return file;
	}
	/**
	Stores the path to be returned by {@link #stateGetPath()}. 
	@param file must be non-<code>null</code> and a directory
	 */
	final public void statePutPath(File file){
		if(file==null||!file.isDirectory())throw new IllegalArgumentException(
				"Null or non-directory path in "+Debug.info(this));
		else stateRoot().put(STATE_FILES_PATH,file);
	}
	public final ValueNode stateRoot(){
		return appValues.state(STATE_TYPE_FILES);
	}
	/**
	Creates an {@link AppFileValues} to read input from the console. 
	@param values for reading and writing
	@param s filter for file types to input
	 */
	public static AppFileValues newConsole(final AppValues values,final FileSpecifier s){
		return new AppFileValues(values){
			protected File getOpeningRecent(String[]recentPaths){
				String rubric="Recent files";
				Object got=TracerInput.getItemChoice(values,rubric,recentPaths);
				return got==null?null:new File((String)got).getAbsoluteFile();
			}
			protected File getOpeningOther(FileSpecifier[]specifiers){
				return TracerInput.getFile(values,stateGetPath(),specifiers[0]);
			}
			protected void gotNoOpening(){
				values.tryWriteValues("Exiting without file: ");
				System.exit(-1);
			}
			public FileSpecifier[]getOpenSpecifiers(){
				return new FileSpecifier[]{s};
			}
		};
	}
	static void main(String[]args){
		AppValues values=new AppValues(AppFileValues.class){
			protected void traceOutput(String msg){
				if(false)traceOutputWithClass(msg);
			}
			protected void addNatureDefaults(ValueNode root){
				if(false)root.setContents(new Object[]{
						NATURE_WRITABLE+"="+false,
						NATURE_RECORD_RUNS+"="+true,
					});
			}
		};
		values.readValues(args);
		values.trace("Got file ",newConsole(values,FileSpecifier.ALL
			).getOpeningFile(true));
		values.tryWriteValues("");
	}
}