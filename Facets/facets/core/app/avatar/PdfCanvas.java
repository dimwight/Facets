package facets.core.app.avatar;
import static facets.util.Util.*;
import facets.util.Debug;
import facets.util.TextLines;
import facets.util.Tracer;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
public final class PdfCanvas extends Tracer{
	private static final String COMMENTED="\n%-------------------------------------------------------\n",
		HUM="q 1 0 0 -1 0 300 cm\r\n" + 
			"q 1 0 0 1 100 120 cm 1 0 0 -1 0 0 cm\r\n" + 
			"BT /F1 12 Tf (Hum)Tj ET Q\r\n" + 
			"1.0 0.0 0.0 RG \r\n" + 
			"110 210 30 30 re S\r\n" + 
			"q 0.0 0.0 1.0 RG \r\n" + 
			"1 0 0 1 100 200 cm\r\n" + 
			"0 0 m 30 0 l 30 30 l 0 30 l 0 0 l\r\n" + 
			"S Q q\r\n" + 
			"0.0 1.0 0.0 RG\r\n" + 
			"50 50 150 15 re S Q \r\n" + 
			"250 200 50 50 re S " +
			"Q";
	public static final String PDF=PdfCanvas.class.getSimpleName();
	public static final boolean propertyIsSet=System.getProperty(PDF)!=null;
	public static final File pdf=new File(runDir(),PDF+".pdf");
	private static final String PDF_TOP="%PDF-1.6";
	private static final int LINE_SPACE=30;
	private static int objects,codeAt;
	public interface PdfPainter extends Painter{
		PdfCode code();
	}
	public PdfCanvas(){
		if(pdf.exists())trace(": Rendering active graphics pane to pdf=",pdf);
	}
	public static void testRender(){
		TextLines.setDefaultEncoding(true);
		new PdfCanvas().tryRender(new Dimension(400,300),new Painter[]{});
	}
	private final class PdfObject{
		final String code;
		PdfObject(String code){
			this.code=++objects+" 0 obj "+code+"endobj";
			if(false)trace(".PdfObject: codeAt=",codeAt);
			codeAt+=this.code.length()+LINE_SPACE;
		}
	}
	public void tryRender(Dimension size,Painter[]painters){
		if(!pdf.exists())return;
		StringBuilder stream=new StringBuilder(//HUM+COMMENTED+
				"1 0 0 -1 0 "+size.height+" cm q\n");
		for(Painter p:painters)
			if(p instanceof PdfPainter)stream.append(((PdfPainter)p).code().getCode());
			else if(false)traceDebug(": p=",p);
		objects=0;
		codeAt=PDF_TOP.length()+LINE_SPACE;
		TextLines lines=new TextLines(pdf);
		try{
			for(String line:new String[]{PDF_TOP, 
				new PdfObject("<</Type /Catalog /Pages 2 0 R>>").code, 
				new PdfObject("<</Type /Pages /Count 1 /Kids [5 0 R]>>").code, 
				new PdfObject("<</Type /Font /Subtype /Type1 /BaseFont /Helvetica>>").code+" %Times-Roman", 
				new PdfObject("<</Font <</F1 3 0 R>> >>").code, 
				new PdfObject("<</Type /Page /Parent 2 0 R /Contents 6 0 R /Resources 4 0 R  " +
						"/MediaBox [0 0\n " +size.width+" "+size.height+"\n]>>").code, 
				new PdfObject("<</Length "+stream.length()+">>\nstream" +COMMENTED+stream+
						"endstream"+COMMENTED).code, 
			})lines.writeNextLine(line);
			for(String line:new String[]{
//				"xref 0 4 0000000000 65535 f 0000000010 00000 n  0000000060 00000 n  0000000115 00000 n", 
				"trailer <<" +//"/Size "+"7"+
				"/Root 1 0 R>>",
//				"startxref ?", 
				"%EOF"
			})lines.writeNextLine(line);
			lines.closeLineWriter();
		}catch(IOException e){
			if(false)throw new RuntimeException(e);
			else trace(": e=",e);
		}
	}
}
