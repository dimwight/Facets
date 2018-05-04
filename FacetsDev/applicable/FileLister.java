package applicable;

import facets.util.FileNode;
import facets.util.Util;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

public class FileLister extends FileNode{
	private final FilenameFilter filter;
	private final FileLister top;
	public FileLister(File dir,FilenameFilter filter,FileLister top){
		super(dir);
		this.filter=filter;
		this.top=top!=null?top:this;
		if(!dir.isDirectory())throw new IllegalArgumentException(
				"Not a dir="+dir);
	}
	@Override
	final protected FilenameFilter fileFilter(){
		return filter;
	}
	@Override
	final protected void traceOutput(String msg){
		Util.printOut(FileLister.class.getSimpleName()+msg);
	}
	@Override
	final protected FileNode newDirChild(File dir){
		return new FileLister(dir,filter,top);
	}
	@Override
	final protected FileNode newFileChild(File file){
		return new FileNode(file){
			@Override
			protected void readFileContents(File file){
				top.readFileChild(this);
			}
		};
	}
	protected void readFileChild(FileNode child){
		if(false)trace(".readFileChild: ",child);
	}
	@Override
	protected boolean readDirs(){
		return true;
	}
}