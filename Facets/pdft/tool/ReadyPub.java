package pdft.tool;
import facets.util.Objects;
import facets.util.Util;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.pdfbox.pdmodel.interactive.viewerpreferences.PDViewerPreferences;
import pdft.PdfCore;
import pdft.block.PdfApp;
public final class ReadyPub extends PdfCore{
	private static final boolean topUc=true;
	private final boolean de,fr;
	private final String figureTop,tableTop;
	private final List<PDOutlineItem>topItems=new ArrayList();
	private final PDOutlineItem figures=new PDOutlineItem(),
		tables=new PDOutlineItem();
	private boolean ucTop=topUc;
	public ReadyPub(File pdf)throws IOException{
		super(pdf);
		String name=pdf.getName().toLowerCase();
		de=name.contains("de");
		fr=name.contains("fr");
		figureTop=de?"Abb.":"Figure";
				tableTop=de?"Tabelle":fr?"Tableau":"Table";
		PDDocumentCatalog c=document.getDocumentCatalog();
		COSDictionary prefs=new COSDictionary();
		prefs.setBoolean("DisplayDocTitle",true);
		c.setViewerPreferences(new PDViewerPreferences(prefs));
		c.setPageLayout(null);
		PDDocumentOutline outline=c.getDocumentOutline();
		if(outline==null)return; 
		c.setPageMode("UseOutlines");
		PDOutlineItem topItem=outline.getFirstChild();
		while(topItem!=null){
			PDOutlineItem copyItem=processOutlineItem(topItem,true);
			if(true||!"FIGURES|TABLES".contains(topItem.getTitle()))topItems.add(copyItem);
			topItem=topItem.getNextSibling();
		}
		outline=new PDDocumentOutline();
		for(PDOutlineItem i:topItems)outline.appendChild(i);
		if(figures.getFirstChild()!=null){
			figures.setTitle(de?ucTop?"ABBILDUNGEN":"Abbildungen":ucTop?"FIGURES":"Figures");
			outline.appendChild(figures);
			figures.openNode();
		}
		if(tables.getFirstChild()!=null){
			tables.setTitle(de?ucTop?"TABELLEN":"Tabellen"
				:fr?ucTop?"TABLEAUX":"Tableaux":ucTop?"TABLES":"Tables");
			outline.appendChild(tables);
			tables.openNode();
		}
		c.setDocumentOutline(outline);
	}
	private PDOutlineItem processOutlineItem(PDOutlineItem in,boolean isTop){
		String titleIn=in.getTitle(),title=titleIn.replaceFirst("("+
				"^\\Q"+tableTop+"\\E"+"|Figure|Abb.|Chapter|Section|" +
						"(^\\d+\\.\\d+))\\s+","")
				.replaceFirst("(\\d+)|(\\d+\\.\\d+)\\.?","$1");
		if(title.equals(title.toUpperCase())){
			if(isTop){
				ucTop|=true;
				title=title.replaceAll("(\\d*)\\.","$1");
				trace(": ",title);
			}
			else{
				String stripNumber=title.replaceAll("^\\d*\\W*(.)","$1");
				title=false||"LDS|CU-FPS".contains(stripNumber.trim())?stripNumber:
						stripNumber.substring(0,1)+stripNumber.substring(1).toLowerCase();
			}
		}
		else if(true&&topUc&&isTop)title=title.toUpperCase();
		PDOutlineItem out=new PDOutlineItem();
		out.setTitle(title);
		out.setAction(in.getAction());
		PDOutlineItem srcChild=in.getFirstChild();
		while(srcChild!=null){
			PDOutlineItem cpyChild=processOutlineItem(srcChild,false);
			titleIn=srcChild.getTitle();
			if(titleIn.startsWith(figureTop))figures.appendChild(cpyChild);
			else if(titleIn.startsWith(tableTop))tables.appendChild(cpyChild);
			else out.appendChild(cpyChild);
			srcChild=srcChild.getNextSibling();
		}
		out.openNode();
		return out;
	}
	public static void main(String[]args)throws IOException{
		String argPath=Objects.toString(args," ");
		File in;
		if(!argPath.trim().equals(""))in=new File(argPath);
		else{
			File pdfs[]=Util.runDir().listFiles(new FilenameFilter(){
				@Override
				public boolean accept(File dir,String name){
					return name.endsWith(".pdf")&&!name.startsWith(PdfApp.class.getSimpleName())
					 &&!name.startsWith("_");
				}
			});
			if(pdfs.length!=1)throw new IllegalStateException("Must be single PDF");
			in=pdfs[0];
		}
		File dir=in.getParentFile(),backup=false?null:new Util.FileBackup(in).doBackup(),
			out=backup==null?new File(dir,"_"+in.getName()):in;
		Util.printOut("ReadyPub.main: in=",Util.kbs(in.length()));
		ReadyPub pub=new ReadyPub(in);
		if(false)pub.writePdf(out,true);
	}
}
