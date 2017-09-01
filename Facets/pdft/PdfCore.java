package pdft;
import static facets.util.Util.*;
import facets.util.Debug;
import facets.util.TextLines;
import facets.util.Times;
import facets.util.Tracer;
import facets.util.Util;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.io.RandomAccess;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDStream;
public class PdfCore extends Tracer{
	public static final String ENCODING="ISO-8859-1",KEY_PAGE="pdfPage",KEY_READER="pdfReader";
	public static File pdfReader=new File[]{
		new File(programs32,"Adobe/Acrobat 11/Acrobat/Acrobat.exe"),
		new File(programs64,"Tracker Software/PDF Viewer/PDFXCview.exe")}[1];
	public static void openViewPdf(String path,int openPage){
		if(false)try{
			String command=("\""+pdfReader.getCanonicalPath() +
				(true||openPage<1?"\"":(" /A \"page="+openPage+"\""))
				+" \""+(path).replaceAll("/","\\\\")+"\""
			);
			printOut("PdfCore.openViewPdf: command=\n",command);
			addProcess(command);
		}catch(IOException e){
			throw new RuntimeException(e);
		}
		else windowsOpenFile(new File(path));
	}
	public static abstract class PageIterator extends Tracer{
		protected final PdfCore core;
		protected PageIterator(PdfCore pdfCore){
			this.core=pdfCore;
		}
		final public void iteratePages(){
			int pageAt=0;
			try{
				for(Object each:core.document.getDocumentCatalog().getAllPages())
					if(pageIterated((PDPage)each,++pageAt)==true)break;
			}catch(Exception e){
				Util.printOut("",e.getStackTrace());
				throw new RuntimeException("Exception in " +
						Debug.info(this).replaceAll("([^$]+)\\$.*","$1")+
						" at page "+pageAt+":\n"+e.getMessage());
			}
		}
		protected abstract boolean pageIterated(PDPage page,int pageAt)throws IOException;
	}
	private static final boolean debug=false;
	public final PDDocument document;
	@Override
	protected void traceOutput(String msg){
		if(true)super.traceOutput(msg);
	}
	public PdfCore(File pdf)throws IOException{
		setTextEncoding();
		FileInputStream input=new FileInputStream(pdf);
		PDFParser parser=new PDFParser(input){
			private int grain=100;
			private int dictionaries=1,streams=1,strings=1,arrays=1;
			//dictionaries=7978 streams=1530 strings=73 arrays=1484
			//dictionaries=11397 streams=375 strings=10208 arrays=13003
			//dictionaries=13762 streams=767 strings=4072 arrays=10985
			boolean verbose=false;
			@Override
			protected COSDictionary parseCOSDictionary()throws IOException{
				if(verbose&&++dictionaries%grain==0)trace(".parseCOSDictionary: dictionaries="+dictionaries);
				return super.parseCOSDictionary();
			}
			@Override
			protected COSStream parseCOSStream(COSDictionary dic,RandomAccess file)
					throws IOException{
				if(verbose&&++streams%grain==0)trace(".parseCOSDictionary: streams="+streams);
				return super.parseCOSStream(dic,file);
			}
			@Override
			protected COSArray parseCOSArray() throws IOException{
				if(verbose&&++arrays%grain==0)trace(".parseCOSArray: arrays=",arrays);
				return super.parseCOSArray();
			}
			@Override
			public void parse()throws IOException{
				super.parse();
				int scale=1;
				if(verbose)trace(".parse: dictionaries="+dictionaries/scale+" streams="+streams/scale
						+" strings="+strings/scale+" arrays="+arrays/scale);
			}
		};
		if(false)Times.printElapsed("PdfCore: parser="+parser);
		File tmpDir=tmpDir();
		if(false&&tmpDir!=null)parser.setTempDirectory(tmpDir);
		parser.parse();
		input.close();
		document=parser.getPDDocument();
		if(false)Times.printElapsed("PdfCore: document="+document);
	
	}
	public PdfCore()throws IOException{
		setTextEncoding();
		document=new PDDocument();
	}
	final public int countPages()throws IOException{
		int pages=document.getNumberOfPages();
		document.close();
		return pages;
	}
	protected File tmpDir(){
		return null;
	}
	private void setTextEncoding(){
	  TextLines.setEncoding(ENCODING);
	}
	public final void writePdf(File pdfOut,boolean closeDocument)throws IOException{
		try{
			File temp=File.createTempFile(PdfCore.class.getSimpleName(),null,Util.runDir());
			Util.printOut("PdfCore.writePdf: temp=",temp.getName());
			document.save(new FileOutputStream(temp));
			Util.copyFile(temp,pdfOut);
			temp.delete();
			Util.printOut("PdfCore.writePdf: size=",Util.kbs(pdfOut.length()));
		}catch(COSVisitorException e){
			throw new IOException(e);
		}
		if(closeDocument)document.close();
	}
	public final void writePageCode(PDPage page,String code)throws IOException{
		if(page==null||page.equals(""))throw new IllegalArgumentException(
				"Null or empty page in "+Debug.info(this));
		PDStream contents=new PDStream(document);
		contents.addCompression();
		if(false)trace(": file.encoding=",System.getProperty("file.encoding"));
		new TextLines(contents.createOutputStream()).writeLines(code);
		page.setContents(contents);
	}
	final public String readPageCode(PDPage page)throws IOException{
		return page.getContents().getInputStreamAsString();
	}
}
