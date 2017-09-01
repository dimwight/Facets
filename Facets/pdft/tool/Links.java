package pdft.tool;
import facets.util.Strings;
import facets.util.TextLines;
import java.io.File;
import java.io.IOException;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.interactive.action.type.PDActionGoTo;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination;
final public class Links extends PdfTool{
	final private class PageLinks{
			private final PDPage page;
			PageLinks(PDPage page){
				this.page=page;
			}
			void appendLinksCode(StringBuilder to)throws IOException{
				int srcAt=findPageAt(page);
				PDPageDestination dest;
				for(Object each:page.getAnnotations())try{
					PDAnnotationLink link=(PDAnnotationLink)each;
					dest=(PDPageDestination)link.getDestination();
					if(dest==null){
						PDActionGoTo goTo=(PDActionGoTo)link.getAction();
						if(goTo!=null)dest=(PDPageDestination)goTo.getDestination();
					}
					if(dest==null)continue;
					PDRectangle r=link.getRectangle();
					to.append("[" +
							" /Rect ["+
							r.getLowerLeftX()+" " +
							r.getLowerLeftY()+" " +
							r.getUpperRightX()+" " +
							r.getUpperRightY()+"]" + 
							" /SrcPg "+srcAt+
	//						" /Action << /Subtype /GoTo "+
							" /Page "+findPageAt(dest.getPage())+
							" /View [/Fit]" +
	//						" >>"+
							" /Border [0 0 0]"+//" /Color [0 0 0]" + 
							" /Subtype /Link "+
							"/ANN pdfmark"+
							"\n");
				}
				catch (Exception e) {
					System.out.println(e);
				}
			}
		}
	public Links(File pdf) throws IOException{
		super(pdf);
	}
	public void writeLinks(File fileOut)throws IOException{
	  StringBuilder out=new StringBuilder();
	  for(Object page:document.getDocumentCatalog().getAllPages())
			new PageLinks((PDPage)page).appendLinksCode(out);			
		new TextLines(fileOut).writeLines(Strings.stringLines(out.toString()));
	}
}
