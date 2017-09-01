package pdfb;
import facets.util.Debug;
import facets.util.Objects;
import facets.util.Strings;
import facets.util.TextLines;
import facets.util.Util;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public final class CodetoMark{
	private final static boolean debug=false;
	private final List<Integer> offsets=new ArrayList();
	private final class MarkLine{
		private static final String afterTwisty="[^+-].*",twisty="^[+-]";
		final String groupSign,markText;
		final int pageAt;
		int groupCount;
		MarkLine(String code){
			String splits[]=code.split("\t");
			if(splits.length==1) throw new IllegalArgumentException(
					"Can't split\n"+code+"\nin "+Debug.info(this));
			String beforeTab=splits[0],afterTab=splits[1];
			groupSign=beforeTab.replaceFirst(afterTwisty,"");
			markText=beforeTab.replaceFirst(twisty,"");
			try{
				pageAt=true&&!groupSign.equals("")&&afterTab.startsWith("+")
						?newOffsetPageAt(afterTab):newSimplePageAt(afterTab);
			}catch(Exception e){
				Util.printOut("CodetoMark.MarkLine: "+code);
				throw new RuntimeException(e);
			}
		}
		private int newOffsetPageAt(String afterTab){
			int offset=Integer.valueOf(afterTab.substring(1));
			offsets.add(offset);
			return offset+1;
		}
		private int newSimplePageAt(String afterTab){
			String[] codes=afterTab.split("\\.");
			int local=new Integer(codes[codes.length-1]),
					offsetAt=codes.length==1?0:new Integer(codes[0]);
			return local+offsets.get(offsetAt);
		}
		private String newLine(){
			String groupText=groupCount==0?"":("/Count "+groupSign+groupCount+" ");
			return "["+groupText+"/Page "+pageAt+" /View [/Fit] /Title ("+markText
					+") /OUT pdfmark";
		}
		@Override
		public String toString(){
			return debug?(groupSign+groupCount+" "+markText+" "+pageAt):newLine();
		}
	}
	private final class MarkGroup{
		final MarkLine[] marks;
		MarkGroup(String code){
			if(debug) Util.printOut("MarkGroup: "+code);
			List<String> lines=Arrays.asList(code.split("\n"));
			List<MarkLine> marks=new ArrayList();
			for(String line:lines)
				marks.add(new MarkLine(line));
			marks.get(0).groupCount=marks.size()-1;
			this.marks=marks.toArray(new MarkLine[]{});
		}
		@Override
		public String toString(){
			return Objects.toLines(marks);
		}
	}
	private static final String groupStart="\n[+-]";
	private String codeToMarks(String code){
		Matcher m=Pattern.compile(groupStart).matcher(code="\n"+code);
		List<String> codeSplits=new ArrayList();
		//Find the first group
		if(!m.find())
			throw new RuntimeException("Bad data - no match for \\n[+-] in "+code);
		//Prepare for loop
		int end=m.start(),start;
		boolean more;
		do{
			//Set start 
			start=end;
			//Another group?
			more=m.find();
			//Set end from new start or end of code
			end=more?m.start():code.length();
			//Add up to the next group
			codeSplits.add(code.substring(start,end).trim());
			//Until out of groups
		}while(more);
		List<String> groupLines=new ArrayList();
		for(String split:codeSplits)
			groupLines.add(new MarkGroup(split).toString());
		return Strings.linesString(groupLines.toArray(new String[]{}));
	}
	public void fileCodetoMarks(File fileIn,File fileOut,List<Integer> offsets)
			throws IOException{
		this.offsets.addAll(offsets);
		String code=Strings.linesString(new TextLines(fileIn).readLines()),
				mark=codeToMarks(code);
		if(debug)Util.printOut(mark+"\n");
		String lines[]=Strings.stringLines(mark),topLevel="";
		boolean v8900=true;
		if(v8900)for(int at=0,start=at,count=0;at<lines.length;at++){
			final String _numbered=".*/Title \\((\\d+\\.).*";
			String line=lines[at];
			if(!line.matches(_numbered))continue;
			String numberTop=line.replaceAll(_numbered,"$1");
			if(!topLevel.equals(numberTop)){
				if(!topLevel.equals(""))
					lines[start]=lines[start].replace("[/Page","[/Count +"+count+" /Page");
				topLevel=numberTop;
				start=at;
				count=0;
			}
			else{
				final String _count=".*/Count \\+(\\d+).*";
				count++;
				if(line.matches(_count))at+=Integer.valueOf(line.replaceAll(_count,"$1"));
			}
		}
		new TextLines(fileOut).writeLines(lines);
		if(debug) Util.printOut(mark);
	}
	public static void main(String[] args){
		TextLines.setEncoding("ISO-8859-1");
		File dir=new File("../Pubs/texts/").getAbsoluteFile(),
				pdf=new File(dir,"Samples.pdf"),bk=new File(dir,"Samples.bk.txt"),
				ps=new File(dir,"Samples.bk.ps");
		try{
			bk.createNewFile();
			new CodetoMark().fileCodetoMarks(bk,ps,null);
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}
}
