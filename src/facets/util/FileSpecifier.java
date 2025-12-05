package facets.util;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
/**
Extension-based file filtering and name building.
 */
public class FileSpecifier extends Tracer{
	/**
	The *.* specifier, does not filter or add an extension. 
	 */
	public static final FileSpecifier ALL=new FileSpecifier("*","All file types");
	public static final String dot=".",_dot="\\.",_toFirstDot="^[^.]+\\.";
	/**
	Immutable fields based on those passed to the constructor. 
	 */
	public final String extension,rubric;
	public static final String KEY_AT="fileSpecifierAt";
	/**
	Unique constructor. 
	@param extension becomes {@link #extension} after any leading '.' is stripped
	@param description joined to extension to become {@link #rubric}
	 */
	public FileSpecifier(String extension,String description){
		this.extension=extension.replaceFirst("^\\.","");
		this.rubric=description+" *"+dot+extension;
	}
	/**
	Returns a file named to match the specification. 
	@param source either a file or typically the source of content to be stored 
	@return a file with name ending (usually) with {@link #extension}, if necessary calling
	{@link #newFileName(Object)}
	 */
	final public File specifiedFile(Object source){
		if(source instanceof File&&this==ALL)return(File)source;
		File file=source instanceof File?(File)source:new File(newFileName(source));
		String filePath=file.getPath();
		if(filePath.endsWith(extension))return file;
		int lastDotAt=filePath.lastIndexOf(dot);
		String usePath=lastDotAt>-1?filePath.substring(0,lastDotAt)
				:filePath;
		return new File(usePath+dot+extension);
	}
	/**
	Constructs a filename by looking at the source passed. 
	@param source typically the source of content to be stored in the file  
	@return by default a file name obtained by casting <code>source</code> to a {@link Titled}
		or {@link String}, with appropriate extension 
	 */
	public String newFileName(Object source){
		return source instanceof Titled? ((Titled)source).title()
				:source+(this==ALL?"":(dot+extension));
	}
	/**
	Does the file match the specification? 
	@param path may be a directory
	 */
	final public boolean specifies(File path){
		return Regex.contains(path.getName(),".*" +_dot+extension+"$");
	}
	public String toString(){
		return rubric;
	}
	/**
	Attempt to filter {@link FileSpecifier}s against the name passed. 
	@param specs to be filtered
	@param name to be checked
	@return all members of <code>specs</code> where <code>name</code> matches {@link #extension},
	or all passed if there are no matches
	 */
	public static FileSpecifier[]filterByName(FileSpecifier[]specs,
			String name){
		List<FileSpecifier>matched=new ArrayList();
		FileSpecifier last=null;
		for(FileSpecifier spec:specs)
			if(spec!=ALL&&name.replaceAll(_toFirstDot,"").matches(spec.extension))
				last=spec;
			else matched.add(spec);
		if(last==null)return specs;
		else matched.add(last);
		return matched.toArray(new FileSpecifier[]{});
	}
	public String stripExtension(String fileName){
		return fileName.replace("."+extension,"");
	}
}