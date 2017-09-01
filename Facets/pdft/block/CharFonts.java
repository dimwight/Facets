package pdft.block;
import static facets.util.Regex.*;
import facets.util.Debug;
import facets.util.Times;
import facets.util.Tracer;
import facets.util.Util;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdfviewer.PageDrawer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDFontDescriptor;
import org.apache.pdfbox.pdmodel.font.PDSimpleFont;
import org.apache.pdfbox.util.PDFOperator;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.util.TextPosition;
import org.apache.pdfbox.util.TextPositionComparator;
final class CharFonts extends Tracer{
	private final int pageAt;
	private static final Font BASIC_FONT=new Font("Times New Roman",0,1);
	private static final Collection<String>families=Arrays.asList(
			GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
	private final static Set<String>fontChecks=new HashSet();
	private final static Map<PDFont,Font>fonts=new HashMap();
	public static void traceSummary(){
		List<String>byPageAts=new ArrayList(fontChecks);
		Collections.sort(byPageAts);
		Util.printOut(CharFonts.class.getSimpleName()+".traceSummary: ",byPageAts);
	}
	CharFonts(int pageAt){
		this.pageAt=pageAt;
	}
	@Override
	protected void traceOutput(String msg){
		if(false)super.traceOutput(msg);
	}
	Font getAwt(PDFont pdFont){
		Font awt=fonts.get(pdFont);
		if(awt!=null)return awt;
		try{
			awt=((PDSimpleFont)pdFont).getawtFont();
		}catch(IOException e){
			throw new RuntimeException(e);
		}
		if(false)trace(".getAwt: awt=",awtInfo(awt));
		PDFontDescriptor fd=pdFont.getFontDescriptor();
		final boolean dubious=fd==null;
		String family=dubious?pdFont.getBaseFont():fd.getFontFamily();
		if(!dubious&&families.contains(family))
			fontChecks.add("families->"+family+"^"+awtInfo(awt));
		else{
			String fixPairs[]={
					"[A-Z]+\\W","",
					"\\W.*","",
					"[A-Z]+$","",
					"([a-z])([A-Z])","$1 $2"
				},
				name=pdFont.getBaseFont(),
				psCore=replaceAll(name,fixPairs),
				familyCore=family==null?null:replaceAll(family,fixPairs),
				useFamily=(familyCore!=null?familyCore:psCore);
			boolean italic=!dubious&&fd.isItalic()||name.toLowerCase().contains("italic"),
				bold=!dubious&&fd.getFontWeight()>500||name.toLowerCase().contains("bold");
			awt=new Font(useFamily,(italic?Font.ITALIC:0)|(bold?Font.BOLD:0),1);
			String subInfo=family+"^"+name+"^"+useFamily+"\n\t->"+awtInfo(awt);
			fontChecks.add("pageAt="+String.format("%3d",pageAt)+": "+subInfo);
			trace(".getAwt: subInfo=",subInfo);
		}
		fonts.put(pdFont,awt);
		if(false)trace(".getAwt: awt=",awtInfo(awt));
		return awt;
	}
	static String awtInfo(Font awt){
		return awt==null?null:("["+awt.getPSName()+" " +(true?awt:("size2D="+awt.getSize2D()))+"]");
	}
	List<PageChar>newPageChars(COSDocument doc,int pageAt,PDPage page,
			int rotation){
		Times.printElapsed("PageContent.newChars");
		final List<TextPosition>texts=new ArrayList();
		final Map<TextPosition,Integer>colors=new HashMap();
		if(true)try{
			PDFTextStripper stripper=new PDFTextStripper(){
				public void processStream(PDPage aPage, PDResources resources, COSStream cosStream)
					throws IOException{
					Times.printElapsed("PageContent...processStream: "+Debug.info(resources));
					super.processStream(aPage,resources,cosStream);
					Times.printElapsed("PageContent...processStream~");
				};
				protected void processOperator(PDFOperator operator, List<COSBase> arguments)
					throws IOException {
					if(false)Times.printElapsed("PageContent...processOperator: "+operator.getOperation());
					super.processOperator(operator,arguments);
				};
				@Override
				protected void processTextPosition(TextPosition text){
					if(false)throw new RuntimeException("Debug");
					if(!text.getCharacter().trim().equals(""))texts.add(text);
					try{
						colors.put(text,getGraphicsState().getNonStrokingColor(
								).getJavaColor().getRGB());
					}catch(IOException e){
						throw new RuntimeException(e);
					}
				}
				protected void writePage(){}
			};
			stripper.setStartPage(pageAt+1);
			stripper.setEndPage(pageAt+1);
			stripper.getText(new PDDocument(doc));
		}catch(IOException e){
			throw new RuntimeException(e);
		}
		else try{
			new PageDrawer(){
				public void processStream(PDPage aPage, PDResources resources, COSStream cosStream)
					throws IOException {
					Times.printElapsed("PageContent...processStream: "+Debug.info(resources));
					super.processStream(aPage,resources,cosStream);
					Times.printElapsed("PageContent...processStream~");
				}
				protected void processOperator(PDFOperator operator, List<COSBase> arguments)
					throws IOException {
					if(false)Times.printElapsed("PageContent...processOperator: "+operator.getOperation());
					super.processOperator(operator,arguments);
				}
				@Override
				protected void processTextPosition(TextPosition text){
					if(false)throw new RuntimeException("Debug");
					if(text.getCharacter().trim().equals(""))return;
			  	if(true)try{
						colors.put(text,getGraphicsState().getNonStrokingColor(
								).getJavaColor().getRGB());
					}catch(IOException e){
						throw new RuntimeException(e);
					}
					texts.add(text);
				}
			}.drawPage(new BufferedImage(1,1,BufferedImage.TYPE_BYTE_BINARY).getGraphics(),
					page,new Dimension(1,1));
		}catch(IOException e){
			throw new RuntimeException(e);
		}
		Times.printElapsed("PageContent.newChars texts="+texts.size());
	  Collections.sort(texts,new TextPositionComparator());
		final List<PageChar>chars=new ArrayList();
		for(TextPosition text:texts)chars.add(new PageChar(this,text,rotation,
					colors.size()>0?colors.get(text):0));
		if(false)Times.printElapsed("PageContent.newChars~");
		return chars;
	}
}
