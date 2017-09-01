package pdft.tool;
import static pdft.tool.OffsetOutline.Special.*;
import facets.util.Regex;
import facets.util.Strings;
import facets.util.TextLines;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.interactive.action.type.PDActionGoTo;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineNode;
final public class OffsetOutline extends PdfTool{
	enum Special{Heading,FigureOrTable,None}
	private static final boolean DE=true,FR=false;
	private int offsetAt;
	private final List<PDOutlineItem>headings=new ArrayList(),
			figures=new ArrayList(),tables=new ArrayList();
		private final List<Integer>offsets;
		private final PDDocumentCatalog catalog;
		private boolean pastHeadings,pastFigures;
	final private class ItemText{
		private final String[]fixPairs;
		private final PDOutlineItem item;
		private final Special special;
		ItemText(PDOutlineItem item,Special special){
			this.item=item;
			this.special=special;
			fixPairs=new String[]{
				//"(\\d+)[.]\\D","$1",
				special==Heading?"\\d+[.]\\d+\\s+":"","",
				"CHAPTER ","",
				FIGURE,"",
				TABLE,"",
			};
		}
		void appendItemText(StringBuilder to){
			String title=tweakedItemTitle(item);
			boolean nextOffset=special==Heading&&title.toUpperCase().equals(title);
			if(special==FigureOrTable)offsetAt=new Integer(title.substring(0,1));
			else if(nextOffset)offsetAt++;
			int offsetCount=offsets.size(),offset=offsetCount<=offsetAt?0
					:special==FigureOrTable?offsets.get(offsetAt)
					:offsets.get(offsetAt),
				pageAt=itemPageAt(item)-offset;
			if(offsetCount==1)title+="\t"+pageAt;
			else if(nextOffset)title+="\t+"+offset;
			else title+="\t"+offsetAt+"."+pageAt;
			to.append(title+"\n");
		}
		private int itemPageAt(PDOutlineItem item){
			try{
				PDPageDestination dest=(PDPageDestination)item.getDestination();
				if(dest==null){
					PDActionGoTo goTo=(PDActionGoTo)item.getAction();
					dest=(PDPageDestination)goTo.getDestination();
				}
				return findPageAt(dest.getPage());
			}catch(Exception e){
				return 1000;
			}
		}
		private String tweakedItemTitle(PDOutlineItem item){
			PDOutlineItem firstChild=item.getFirstChild(),sibling=item.getNextSibling(),
				nextSiblingChild=sibling==null?null:sibling.getFirstChild();
			String title=item.getTitle();
			boolean manualHeading=title.toUpperCase().equals(title),
				digitAtStart=Character.isDigit(title.charAt(0)),
				addTwisty=true?digitAtStart
					:true?title.startsWith("Chapter")||manualHeading
							:firstChild!=null||nextSiblingChild!=null,
				closeTwisty=true?addTwisty&&digitAtStart&&!manualHeading
					:addTwisty&&!digitAtStart;
			if(false&&closeTwisty)title=title.replaceAll(_level,"").trim();
			title=(closeTwisty?"+":addTwisty?"+":"")+title;
			if(title.contains("Chapter"))title=title.toUpperCase();
			if(false)title=Regex.replaceAll(title,true,fixPairs);
			return title;
		}
	}
	private static final String FIGURE=DE?"Abb. ":"Figure ",
			TABLE=DE?"Tabelle ":FR?"Tableau ":"Table ",
			_level="(\\d+\\.)",_titleTail=" (\\w.*)";
	private void processOutlineChildren(PDOutlineNode parent){
		PDOutlineItem item=parent.getFirstChild();
		while(item!=null){
			String title=item.getTitle().replaceAll("  "," ");
			boolean atFiguresItem=title.startsWith("FIGURES"),
				atTablesItem=title.startsWith("TABLES")||title.startsWith("TABLEAUX"),
				skipItem=atFiguresItem|atTablesItem|title.startsWith("Contents ")
					|title.matches(_level+"{3,}\\d+.*");
			pastHeadings|=atFiguresItem;
			pastFigures|=atTablesItem;
			if(!skipItem){
				if(title.startsWith(TABLE)|pastFigures)tables.add(item);
				else if(title.startsWith(FIGURE)|pastHeadings)figures.add(item);
				else headings.add(item);
				if(title.matches(_level+_titleTail)){
					title=title.replaceAll(_level+" ","$1 ").toUpperCase();
				}
				else if(true&&title.matches(_level+"{3,}"+_titleTail)){
					title=title.replaceAll("^"+_level+"{3,} ","");
					System.out.println(title);
				}
				else if(true&&title.matches(_level+"{2,}"+_titleTail)){
					title=title.replaceAll("(\\d+)\\. ","$1 ");
				}
				item.setTitle(title);
			}
			processOutlineChildren(item);
			item=item.getNextSibling();
		}
	}
	public OffsetOutline(File pdf,List<Integer>offsets)throws IOException{
		super(pdf);
		this.offsets=offsets;
		catalog=document.getDocumentCatalog();
	}
	public void writeOutlineText(File fileOut)throws IOException{
		PDDocumentOutline outline=catalog.getDocumentOutline();
		if(outline==null)throw new IllegalStateException("No outline found");
		processOutlineChildren(outline);
	  StringBuilder out=new StringBuilder();
	  appendItemTexts(headings,out);
	  if(false){
	  	out.append("+Figures\t1000\n");
	  	appendItemTexts(figures,out);
	  	out.append("+Tables\t1000\n");
	  	appendItemTexts(tables,out);
	  }
		new TextLines(fileOut).writeLines(Strings.stringLines(out.toString()));
	}
	private void appendItemTexts(List<PDOutlineItem>items,StringBuilder to){
		Special special=items==headings?Heading:items==tables||items==figures?FigureOrTable:None;
		for(PDOutlineItem item:items)
			new ItemText(item,special).appendItemText(to);
	}
}
