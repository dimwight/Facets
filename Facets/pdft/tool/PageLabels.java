package pdft.tool;
import facets.util.Objects;
import facets.util.Util;
import java.io.File;
import java.io.IOException;
import org.apache.pdfbox.pdmodel.common.PDPageLabelRange;
import org.apache.pdfbox.pdmodel.common.PDPageLabels;
public final class PageLabels extends PdfTool{
	public PageLabels(File pdf)throws IOException{
		super(pdf);
	}
	public void addLabels(String[]offsets,boolean andWrite)throws IOException{
		if(offsets==null||offsets.length==0||offsets[0].equals(""))
			throw new IllegalArgumentException("No offsets");
		PDPageLabels labels=new PDPageLabels(document);
		PDPageLabelRange preface=new PDPageLabelRange();
		preface.setStart(1);
		preface.setStyle(PDPageLabelRange.STYLE_ROMAN_LOWER);
		labels.setLabelItem(0,preface);
		int at=0;
		for(String offset:offsets){
			PDPageLabelRange section=new PDPageLabelRange();
			section.setStart(1);
			if(offsets.length>1)section.setPrefix(++at+".");
			section.setStyle(PDPageLabelRange.STYLE_DECIMAL);
			labels.setLabelItem(Integer.valueOf(offset),section);
		}
		document.getDocumentCatalog().setPageLabels(labels);
		if(false)Util.printOut("PageLabels.addLabels: ",
				labels.getLabelsByPageIndices());
		if(andWrite)writePdf(newSuffixedFile("p"),true);
	}
	public static void main(String[]args)throws IOException{
		//${file_prompt:issue-ready PDF} /${string_prompt:preface length:12}
		String argsText=Objects.toString(args," ");
		String[]pathAndLength=argsText.split("/");
		File in=new File(pathAndLength[0]),dir=in.getParentFile(),
			backup=false?null:new Util.FileBackup(in).doBackup(),
			out=backup==null?new File(dir,"_"+in.getName()):in;
		Util.printOut("PageLabels.main: in=",Util.kbs(in.length()));
		PageLabels labels=new PageLabels(in);
		labels.addLabels(new String[]{pathAndLength[1]},false);
		if(false)return;
		labels.writePdf(out,true);
		Util.printOut("PageLabels.main~: out="+Util.kbs(out.length()));
	}
}
