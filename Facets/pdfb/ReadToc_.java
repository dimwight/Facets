package pdfb;
import facets.util.Regex;
import facets.util.Strings;
import facets.util.TextLines;
import facets.util.Times;
import facets.util.Util;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
final class ReadToc_{
	private static final boolean debug=false,DE=false;
	private static final String toEndOfLine="[^\n]*\n",
		findFixes[]=new String[]{
		"Kapitel","Chapter",
		"Abbildungen","Figures",
		"Abb. (\\d)","Figure $1",
		"Tabellen","Tables",
		"Tabelle (\\d)","Table $1",
//		"\u00e4","ae",
//		"�","oe",
//		"�","ue",
		"CHAPTER","Chapter",
		"Figures","FIGURES",
		"Tables","TABLES",
		"PREFACE","Preface",
//		"Contents of this chapter" +toEndOfLine,"",
//		"Preface" +toEndOfLine,"",
		"\\biii\\b","0.3",
		"\\bv\\b","0.5",
		"\\bvii\\b","0.7",
		"\\bix\\b","0.9",
		"\\s*(\t|\n)\\s*","$1",
		"\n\\s*","\n",
		"\\s\n","\n",
		"\n\n+","\n",
//		"\n\\d+\\.\\t","\n",
		"(Chapter \\d+\\t[^\\t]+\\t)\\d+\\.\\d+","$1+^",
		"(Appendix \\w)\\t","-$1 ",
		"Chapter (\\d+)\\t","+$1 ",
		"FIGURES",DE?"-ABBILDUNGEN\t1000":"-FIGURES\t1000",
		"TABLES",DE?"-TABELLEN\t1000":"-TABLES\t1000",
		"\\d\\.\t([^\t]+)\t([1-7])","-$1\t$2",
		"\\d\\.\t([^\t]+)\t([8])","$1\t$2",
		"(?m)^\\d+\\.\\d+\\t","",
		"(?m)^Figure (\\S+)\t","$1 ",
		"(?m)^Table (\\S+)\t","$1 ",
		"([A-Z])\\.?\\t([A-Z])","$1 $2",
		"\t\t","\t",
		"(\\+\\d )"+"lcase"+"(\t\\+\\d+)","$1"+"UCASE"+"$2",
		"1 Specification","1 SPECIFICATION",
		"2 Description","2 DESCRIPTION",
		"3 Installation","3 INSTALLATION",
		"4 Operation","4 OPERATION",
		"5 Maintenance","5 MAINTENANCE",
		"6 Spares","6 SPARES",
		"7 Options","7 OPTIONS",
		"8 Appendices","8 APPENDICES",
//		"\u07dd","-",
//		"(\\+\\d [A-Z]+\t\\+\\d+)\n(\\w+)","$1\n-$2",
		"","",
	};
	private final String pubSubject;
	public ReadToc_(String pubSubject){
		this.pubSubject=pubSubject.equals("")?"$pubSubject":pubSubject;
	}
	public void read(File fileIn,File fileOut,String[]offsets) throws IOException{
		TextLines in=new TextLines(fileIn),out=new TextLines(fileOut);
		String toc=Strings.linesString(in.readLines());
		toc="-" + pubSubject + "\t0.1\n"+
			Regex.replaceAll(toc,true,findFixes);
		String[]chunks=toc.split("\\^");
		if(offsets.length+1!=chunks.length)throw new IllegalArgumentException(
				"Bad offsets: "+offsets.length+" for "+chunks.length+" chunks");
		StringBuilder merge=new StringBuilder(chunks[0]);
		for(int i=0;i<offsets.length;i++)merge.append(offsets[i]+chunks[i+1]);
    out.writeLines(Strings.stringLines(merge.toString()));
  }
	public static void main(String[]args){
	  String title="V875LS Vibration Test Systems", 
	  	pubCode="3002311_3",offsets="12,32,60,74,98,130,136,152";
		File fileIn=new File("toc/"+pubCode+"toc.txt"),
			fileOut=new File("bookmark/"+pubCode+"bk.txt");
		try{
			new ReadToc_(title).read(fileIn,fileOut,offsets.split(","));
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}
}
