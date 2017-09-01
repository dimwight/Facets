package pdft.tool;
import static facets.util.Regex.*;
import static java.lang.Math.*;
import facets.util.Regex;
import facets.util.Util;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.font.PDFont;
import pdft.PdfCore;
import pdft.PdfCore.PageIterator;
public class PdfTool extends PdfCore{
	public static final String SUFFIX_MODIFIED="+";
	final static String _between="\\)[^\\(]+\\(",_toNext="("+_between+")?";
	protected final HashMap<Integer,Integer>pageAts=new HashMap();
	protected final File fileIn;
	public PdfTool(File pdf)throws IOException{
		super(pdf);
		fileIn=pdf;
	  new PageIterator(this){
	  	protected boolean pageIterated(PDPage page,int pageAt){
				pageAts.put(page.hashCode(),pageAt);
				return false;
	  	};
	  }.iteratePages();
	}
	final protected int findPageAt(PDPage page){
		return pageAts.get(page.hashCode());
	}
	final protected File newSuffixedFile(String suffix){
		String name=fileIn.getName(),pdfId=name.replaceAll("\\.pdf$","");
		if(name.equals(pdfId))throw new IllegalArgumentException("Not a PDF file: "+fileIn);
		File parent=fileIn.getParentFile();
		return new File(parent,pdfId+suffix+".pdf");
	}
	final public void cleanFonts()throws IOException{
		new PageIterator(this){
			@Override
			protected boolean pageIterated(PDPage page,int pageAt){
				for(Object each:page.getResources().getFonts().values().toArray()){
					PDFont font=(PDFont)each;
					String baseFont=font.getBaseFont();
					font.setBaseFont(true?"Times New Roman"
							:baseFont.replaceFirst("[A-Z]+\\+",""));
					trace(".cleanFonts:" +" baseFont="+baseFont+" font="+font.getBaseFont());
					if(false){
						font.setFirstChar(0);
						font.setLastChar(255);
					}
				}
				return true;
			}
		}.iteratePages();
		writePdf(newSuffixedFile(SUFFIX_MODIFIED),true);
	}
	final public void setIssue(String issue)throws IOException{
		CodeChanger.newIssueChanger(this,issue).writeChanges(
				newSuffixedFile(SUFFIX_MODIFIED));
	}
	final public void setVts()throws IOException{
		CodeChanger.newVtsChanger(this).writeChanges(newSuffixedFile(SUFFIX_MODIFIED));
	}
	static String splitCodeChars(String s,String top){
		String split=top;
		for(int i=0;i<s.length();i++)
			split+=s.substring(i,i+1)+_toNext;
		return split;
	}
	public static void main(String[]args) throws Exception{
	  String pdfId="4001041_13";
		File dir=new File("C:/Tray"),
			in=new File(dir,pdfId+".pdf"),
	  	out=new File(dir,pdfId+"+.pdf");
		Util.printOut("TaskCore: Processing "+in.getAbsolutePath());
		if(out.exists())out.delete();
		if(false)new PdfTool(in).cleanFonts();
		else new PdfTool(in).setVts();
		if(out.exists())Util.printOut("TaskCore: Saved "+out.getAbsolutePath());
	
	}
}
