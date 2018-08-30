package facets.util;
import static facets.util.Regex.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/**
Reads from the console using a {@link Tracer} for output.
 */
public class TracerInput{
	private static final String QUIT="Q",QUIT_RUBRIC=QUIT+" quits without input\n";
	private static final BufferedReader in=new BufferedReader(
			new InputStreamReader(System.in));
	private final String prompt,error;
	/**
	Unique constructor. 
	@param prompt passed to {@link Tracer#trace(String)} before blocking for input
	@param error passed to {@link Tracer#trace(String)} if {@link #newValidInput(String)} 
	returns <code>null</code>
	 */
	TracerInput(String prompt,String error){
		this.prompt=prompt;
		this.error=error;
	}
	/**
	Returns a value following console input. 
	@param tracer issues the messages passed to the constructor
	@return non-<code>null</code> return from {@link #newValidInput(String)} 
	or <code>null</code> if quit requested
	 */
	public final Object getInput(Tracer tracer){
		tracer.trace(prompt);
		try{
			Object input=null;
			while(input==null){
				String read=in.readLine().trim();
				if(read==null||read.equalsIgnoreCase(QUIT)){
					return null;
				}
				else if((input=newValidInput(read.trim()))==null)
					tracer.trace(error);
			}
			return input;
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}
	/**
	Processes console input. 
	@param read passed from from {@link System#in} by {@link #getInput(Tracer)}
	@return by default <code>read</code>; may convert this to some other value, or <code>null</code>
	to signal invalid input
	 */
	protected Object newValidInput(String read){
		return read;
	}
	/**
	Convenience method for pausing at the console. 
	@param msg incorporated into pause prompt
	 */
	public static void pause(String msg){
		new TracerInput("Press Enter" +msg+"...","").getInput(new Tracer(){
			protected void traceOutput(String msg){
				Util.printOut(msg);
			}
		});
	}
	private static boolean isOptionChoice(Object choice,String optionName){
		if(!(choice instanceof String))return false;
		String text=(String)choice;
		return text!=null&&text.length()==1&& 
			contains(optionName,"\\([" +text.toLowerCase()+text.toUpperCase()+"]\\)");
	}
	private static class FileSearch{
		final static String optionStart="(S)earch",optionAgain="Search (A)gain";
		private final Tracer tracer;
		private final FileSpecifier specifier;
		FileSearch(Tracer tracer,FileSpecifier specifier){
			this.tracer=tracer;
			this.specifier=specifier;
		}
		Object searchBelow(File dir){
			while(true){
				String pattern=(String)new TracerInput("Enter search pattern","Bad pattern"){
					protected Object newValidInput(String read){
						try{
							find("",read);
						}catch(Exception e){
							return null;
						}
						return read;
					}
				}.getInput(tracer);
				if(pattern==null)return null;
				final List<String>results=new ArrayList();
				addDirChildrenResults(dir,pattern,results);
				List<String>pathsBelow=new ArrayList();
				for(String each:results)
					pathsBelow.add(each.replace(dir.getAbsolutePath()+"\\",""));
				Object choice=getItemChoice(tracer,"Files below "+dir+" matching '"+pattern+"'",
						pathsBelow.toArray(),optionAgain);
				if(!isOptionChoice(choice,optionAgain))
					return choice==null?null:new File(dir,(String)choice);
			}
		}
		private void addDirChildrenResults(File dir,String pattern,List<String>results){
			tracer.trace("Searching "+dir.getName()+"...");
			for(File child:dir.listFiles())
				if(!child.isDirectory()&&
						specifier.specifies(child)&&contains(child.getName(),pattern))
					results.add(child.getAbsolutePath());
			for(File child:dir.listFiles())
				if(child.isDirectory())addDirChildrenResults(child,pattern,results);
		}
	}
	/**
	Encapsulates getting a file using console input.
	<p>Uses {@link #getItemChoice(Tracer, String, String[])}. 
	@param tracer issues messages
	@param dir starting directory
	@param specifier filters files found
	@return a valid file below <code>dir</code> or <code>null</code> if abandoned
	 */
	public static File getFile(Tracer tracer,final File dir,FileSpecifier specifier){
		if(dir==null||!dir.isDirectory())throw new IllegalArgumentException(
				"Null or non-directory dir="+dir);
		final String parentItem="..";
		File file=dir;
		while(file.isDirectory()){
			final File parentDir=file.getParentFile();
			List<String>items=new ArrayList(),files=new ArrayList();
			items.add(parentItem);
			for(File listFile:file.listFiles())
				if(listFile.isDirectory())items.add(listFile.getName());
				else if(specifier.specifies(listFile))files.add(listFile.getName());
			Collections.sort(items);Collections.sort(files);
			items.addAll(files);
			Object choice=getItemChoice(tracer,"Folders" +
					" and files matching " +specifier.rubric+
					" in "+file,items.toArray(),
					FileSearch.optionStart);
			if(choice==null)return null;
			if(isOptionChoice(choice,FileSearch.optionStart)){
				choice=new FileSearch(tracer,specifier).searchBelow(file);
				if(choice==null)continue;
			}
			file=choice instanceof File?(File)choice
				:!choice.equals(parentItem)?new File(file,(String)choice)
				:parentDir!=null?parentDir:file;
			if(!file.getAbsoluteFile().exists())throw new IllegalStateException(
					"Bad file="+file);
		}
		return file;
	}
	/**
	Cover for {@link #getItemChoice(Tracer, String, Object[], String...)}. 
	 */
	public static Object getItemChoice(Tracer tracer,String rubric,String[]items){
		return getItemChoice(tracer,rubric,items,"");
	}
	/**
	Returns a value chosen from a list. 
	@param tracer issues messages
	@param rubricTop appears before prompt
	@param items to choose
	@param optionNames listed as choices and checked against input
	@return a valid value or <code>null</code> if abandoned
	 */
	public static Object getItemChoice(Tracer tracer,String rubricTop,final Object[]items,
			final String...optionNames){
		List<String>listing=new ArrayList();
		for(Object item:items)listing.add(listing.size()+1+".\t"+item);
		final String more="";
		final int maxItems=10,lastAt=listing.size();
		int startAt=0;
		Object input=more;
		if(false)tracer.trace(QUIT_RUBRIC);
		while(more.equals(input)){
			if(startAt==0)tracer.trace(rubricTop+":");
			String rubric="";
			int stopAt=Math.min(lastAt,startAt+maxItems);
			for(String s:listing.subList(startAt,stopAt))rubric+=(s+"\n");
			rubric+=("\nEnter item number");
			if(stopAt<lastAt)rubric+=(", <Enter> for " +(lastAt-stopAt)+
					" more items");
			for(String option:optionNames)
				if(!option.equals(""))rubric+=(", "+option);
			input=new TracerInput(rubric.toString(),"Invalid number"){
				final public Object newValidInput(String read){
					for(String option:optionNames)
						if(isOptionChoice(read,option))return read;
					if(read.equals(more))return more;
					try{
						int at=new Integer(read)-1;
						return at>=0&&at<items.length?items[at]:null;
					}catch(NumberFormatException e){
						return null;
					}
				}
			}.getInput(tracer);
			startAt=stopAt>=lastAt?0:stopAt;
		}
		return input;
	}
	static void main(String[]args){
		Tracer tracer=new Tracer(){
			protected void traceOutput(String msg){
				Util.printOut(msg);
			}
		};
		File file=getFile(tracer,new File("facets").getAbsoluteFile(),
				new FileSpecifier("java","*."));
		tracer.trace("Got file=",file);
	}
}