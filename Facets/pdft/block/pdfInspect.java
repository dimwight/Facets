package pdft.block;
import static facets.util.Debug.*;
import facets.util.Debug;
import java.io.File;
import org.apache.pdfbox.cos.COSDocument;
import pdft.PdfCore;
public final class pdfInspect extends PdfApp{
	public static final String ARG_GRAPHICS="renderGraphics",ARG_WRAP="wrapCode";
	public pdfInspect(){
		super(pdfInspect.class);
	}
	protected boolean jarReady(){
		return false;
	}
	@Override
	protected void traceOutput(String msg){
		if(false)super.traceOutput(msg);
	}
	@Override
	public boolean offersHelp(){
		return true;
	}
	static void main(String[]args)throws Exception{
		memCheck=true;
		for(int i=0;i<4;i++){
			memCheck("\nPdfApp.main");
			COSDocument doc=new PdfCore(new File("PdfApp.pdf")).document.getDocument();
			memCheck(Debug.info(doc));
			doc.close();
			memCheck("PdfApp.main~");
		}
		System.exit(0);
	}
}