package pdft.tool;
import facets.util.Regex;
import facets.util.Strings;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.pdfbox.pdmodel.PDPage;
import pdft.PdfCore;
public class PageCopy extends PdfTool{
	public static final String SUFFIX_SRC="p",SUFFIX_REVIEW="r";
	private static final String NO_ID="No ID";
	private final Map<String,PDPage>srcPages=new HashMap();
	private File fileOut,fileReview;
	private final PdfCore src,copy;
	private final String _pageIdTop,page1Id;
	public PageCopy(File pdf)throws IOException{
		super(pdf);
		copy=new PdfCore(pdf);
		src=new PdfCore(newSuffixedFile(SUFFIX_SRC));
		fileOut=newSuffixedFile(SUFFIX_MODIFIED);
		fileReview=newSuffixedFile(SUFFIX_REVIEW);
		String name=pdf.getName();
		_pageIdTop=name.contains("SV")?"Sida":name.contains("DE")?"Seite":"Page";
				page1Id=_pageIdTop+" i";
	}
	@Override
	protected void traceOutput(String msg){
		if(msg.startsWith(">"))traceOutputWithClass(msg.replace(">",""));
	}
	public void copyPages()throws IOException{
		new PageIterator(src){
			protected boolean pageIterated(PDPage page,int pageAt)throws IOException{
				String code=readPageCode(page),pageId=findPageId(code);
				if(pageId.equals(NO_ID))
					trace(">: No ID for '" +_pageIdTop+"' in code:\n"+code);
				else srcPages.put(pageId,page);
				return false;
			};
		}.iteratePages();
		final List<String>copyKeys=new ArrayList(srcPages.keySet());
		Collections.sort(copyKeys);
		trace(">: Stored " +copyKeys.size()+
				":\n"+Strings.linesString(copyKeys.toArray(new String[]{}))+
				"\n");
		final PdfCore review=new PdfCore();
		new PageIterator(this){
			List copyPages=copy.document.getDocumentCatalog().getAllPages();
			protected boolean pageIterated(PDPage page,int pageAt)throws IOException{
				boolean usePage1Id=true;
				String pageId=usePage1Id&&pageAt==1?page1Id:findPageId(readPageCode(page));
				if(pageId.equals(NO_ID))trace("> pageAt="+pageAt+" pageId="+pageId);
				if(!copyKeys.contains(pageId))return false;
				PDPage src=srcPages.remove(pageId);
				copyKeys.remove(pageId);
				src.setMediaBox(src.findMediaBox());
				src.setCropBox(src.findCropBox());
				src.setRotation(src.findRotation());
				review.document.addPage(src);
				review.document.addPage((PDPage)copyPages.get(pageAt-1));
				page.setContents(src.getContents());
				page.setResources(src.findResources());
				trace(">: Copied " +pageId+ " = "+pageAt);
				return false;
			};
		}.iteratePages();
		List<String>keysLeft=new ArrayList<>(srcPages.keySet());
		if(keysLeft.size()!=0){
			Collections.sort(keysLeft);
			System.err.println("Unused keys: \n"+
							Strings.linesString(keysLeft.toArray(new String[]{})));
		}
		if(true)review.writePdf(fileReview,true);
		if(true)writePdf(fileOut,true);
		else trace(">: Not writing fileOut="+fileOut);
		src.document.close();
		copy.document.close();
	}
	private String findPageId(String code){
		String id=Regex.find(code,
				splitCodeChars(_pageIdTop,"")+
				"\\s+" +_toNext+
				"(\\d\\.)?[0-9ivx]+");
		return id.equals("")?NO_ID:id.replaceAll(_between,"");
	}
	public static void main(String[]args){
	  String pdfId=args[0];
	  File fileIn=new File("C:/Tray",pdfId+".pdf");
	  try{
			new PageCopy(fileIn).copyPages();
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}
}
