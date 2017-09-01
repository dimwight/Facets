package pdft.tool;
import static facets.util.Regex.*;
import facets.util.Util;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.util.PDFTextStripper;
public class PageTexts extends PdfTool{
	public static boolean retainLineBreaks=true;
	public PageTexts(File pdf)throws IOException{
		super(pdf);
	}
	public String readAllPageTexts()throws IOException{
		final StringWriter writer=new StringWriter();
		PDFTextStripper stripper=new PDFTextStripper();
		stripper.writeText(document,writer);
		document.close();
		return writer.toString();
	}
	final public void readPageTexts()throws Throwable{
		final StringWriter writer=new StringWriter();
		try {
			new PDFTextStripper(){
				private String thenText="";
				protected void endPage(PDPage page){
					String _specialMark="\\?",_tabLeaders="\\.{3,}",
						newText=writer.toString().substring(thenText.length());
					thenText+=newText;
					pageTextRead(findPageAt(page),replaceAll(newText.toString(),
							retainLineBreaks?"\r":"\\s+"," ",
									" +"," ",
									_tabLeaders,"...",
									_specialMark,""));
				};
			}.writeText(document,writer);
		} finally {
			PDFont.clearResources();
			COSName.clearResources();
			document.close();
		}
	}
	protected void pageTextRead(int pageAt,String text){
		for(String line:text.split("\n")){
			String trim=line.trim();
			boolean check=false;
			if(false)check=trim.matches("\\d+\\.\\d*\\s+\\w+.+");
			check|=trim.toLowerCase().matches("figure\\s*\\d+\\.\\d*\\w*.+\\d");
			if(false)check|=trim.toLowerCase().matches("chapter\\s*\\d+.+");
			if(false)check&=trim.length()<50;
			if(false&&check)Util.printOut(trim);
		}
	}
}
