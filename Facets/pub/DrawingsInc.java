package pub;
import static pub.IncBuild.*;
import static pub.PubValues.*;
import facets.util.Objects;
import facets.util.TextLines;
import facets.util.Tracer;
import facets.util.Util;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
public final class DrawingsInc extends Tracer{
	private final class Item implements Comparable{
		private final String pdf,number,issue,sheet;
		Item(String pdf){
			this.pdf=pdf;
			String core=pdf.toUpperCase().replace("D_","").replace(".PDF","");
			number=core.replaceAll("(\\d+).+","$1");
			issue=number+"_"+core.replaceAll("[^.]+\\.(\\d+)","$1");
			sheet=core.replaceAll("[^_]+_(\\d+).*","$1");
			if(false)trace(": ",this);
		}
		public String toString(){
			return pdf+(true?"":(">"+number+"s"+sheet));
		};
		public boolean equals(Object o){
			if(false)return this==o;
			Item i=(Item)o;
			return number.equals(i.number)&&sheet.equals(i.sheet);
		};
		String newOptionLine(List<Item>items){
			int itemAt=items.indexOf(this);
			boolean withSheet=itemAt>0&&items.get(itemAt-1).number.equals(number);
			withSheet|=itemAt<items.size()-1&&items.get(itemAt+1).number.equals(number);
			return OPTION_TOP+
					new File(drawings,pdf).getPath().replace(techpubs.getPath()+"\\","")+
					OPTION_VALUE_CLOSE+
					number+(!withSheet?"":" Sheet "+sheet)+
					OPTION_TAIL;
		}
		public int compareTo(Object o){
			return toString().replace("D_","").compareTo(o.toString().replace("D_",""));
		}
	}
	private final List<Item>items=new ArrayList();
	private final File techpubs,drawings;
	public DrawingsInc(File pubsRoot){
		techpubs=pubsRoot;
		drawings=new File(pubsRoot,"Drawings");
		for(String pdf:drawings.list(new FilenameFilter(){
			@Override
			public boolean accept(File dir,String name){
				return name.toLowerCase().endsWith(".pdf");
			}
		})){
			Item item=new Item(pdf);
			if(items.contains(item))
				throw new IllegalArgumentException("Duplicate cores " 
						+items.get(items.indexOf(item))+">"+item);
			else items.add(item);
		}
		Collections.sort(items);
		if(false)trace(".DrawingsInc: items=",items);
	}
	public void writeInc(boolean live)throws IOException{
		File inc=new File(techpubs,"drawings.inc"),
			bak=new Util.FileBackup(inc).doBackup();
		TextLines write=!live?TextLines.newBuffer():new TextLines(inc);
		for(Item item:items)write.writeNextLine(item.newOptionLine(items));
		write.closeLineWriter();
		if(false&&!live)trace(".writeInc:\n"+write.readLinesString());
	}
}
