package pdfb;
import facets.util.Debug;
import facets.util.TextLines;
import facets.util.Util;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import pdft.tool.Links;
import pdft.tool.OffsetOutline;
import pdft.tool.PageCopy;
import pdft.tool.PageLabels;
import pdft.tool.PdfTool;
public final class PdfBuildTask extends Task{
  public static final String CREATE="createBookmarks",CLEAN_WORK="cleanWorkPs",
  	TOC="processToc",OUTLINE="readOutline",LINKS="readLinks",LABELS="addPageLabels",
  	ISSUE="setIssue",VTS="setVts",COPY="copyPages";
  private File fileIn,fileOut;
  private String type,offsets[],issue;
  public void execute()throws BuildException{
	  TextLines.setEncoding("ISO-8859-1");
  	Util.printOut("Starting "+type+" with " +fileIn+"...");
    if(fileOut!=null&&fileOut.exists())fileOut.delete();
    try{
      if(type==null||type.equals(""))throw new IllegalStateException(
      		"Null or empty task type in "+Debug.info(this));
      else if(type.equals(CREATE))
        new CodetoMark().fileCodetoMarks(fileIn,fileOut,newOffsetInts(offsets));
      else if(type.equals(CLEAN_WORK))
      	new CleanPubPs().clean(fileIn,fileOut);
      else if(type.equals(TOC))
      	new ProcessToc().process(fileIn);
      else if(type.equals(OUTLINE))
      	new OffsetOutline(fileIn,newOffsetInts(offsets)).writeOutlineText(fileOut);
      else if(type.equals(LINKS))
      	new Links(fileIn).writeLinks(fileOut);
      else if(type.equals(LABELS))
      	new PageLabels(fileIn).addLabels(offsets,true);
      else if(type.equals(ISSUE))
      	new PdfTool(fileIn).setIssue(issue);
      else if(type.equals(VTS))
      	new PdfTool(fileIn).setVts();
      else if(type.equals(COPY))
      	new PageCopy(fileIn).copyPages();
			else throw new IllegalStateException(
      		"Unknown task type '" +type+"' in "+Debug.info(this));
    }catch(Exception e){
    	Util.printOut("PdfBuildTask.execute: ",e.getStackTrace());
    	throw new BuildException(e);
    }
    if(fileOut!=null&&fileOut.exists())
    	Util.printOut("Finished "+type+" with " +fileOut);
  }
  public static List<Integer>newOffsetInts(String[]offsets){
		List<Integer>add=new ArrayList(Arrays.asList(0));
		if(offsets!=null){
			if(offsets.length==0)throw new IllegalArgumentException("Empty offsets");
			else for(String offset:offsets)add.add(new Integer(offset));
		}
		return add;
	}
	public void setType(String type){
		this.type=type;
	}
	public void setFileIn(File fileIn){
		if(!fileIn.exists())throw new IllegalArgumentException(
				"File not found "+fileIn);
		this.fileIn=fileIn;
	}
	public void setFileOut(File fileOut){
		this.fileOut=fileOut;
	}
	public void setOffsets(String offsets){
		this.offsets=offsets.startsWith("$")?null:offsets.equals("")?
				new String[]{}:offsets.split(",");
	}
	public void setIssue(String issue){
		this.issue=issue.replaceAll("Issue ","");
	}
	public static void main(String[]args){
	  TextLines.setEncoding("ISO-8859-1");
		PdfBuildTask task=new PdfBuildTask();
		String name=args[0],type=args[1];
		String in=type.equals(CREATE)?name+"bk.txt":"C:/Tray/"+name+".pdf",
			out=type.equals(CREATE)?name+"bk.ps"
					:type.equals(LINKS)?("link/"+name+"lk.txt")
				:type.equals(OUTLINE)?("bookmark/"+name+"bk.txt"):"";
		task.setFileIn(new File(in));
		task.setFileOut(new File(out));
		task.setType(type);
		task.setOffsets("$");
		task.execute();
	}
}
