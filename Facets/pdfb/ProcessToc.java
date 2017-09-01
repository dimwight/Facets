package pdfb;
import facets.util.Debug;
import facets.util.TextLines;
import facets.util.Tracer;
import java.io.File;
import java.io.IOException;
public final class ProcessToc extends Tracer{
	private static final String _toEnd=".*",
		_chapter="^((Chapter|Kapitel|Chapitre) )",
		_section="^\\d\\.\\s+",
		_sub="^\\d\\.\\d+\\s+",
		_appendix="(Appendix|Anhang|Annexe) [A-Z]\\t";
	public void process(File file)throws IOException{
		TextLines lines=new TextLines(file);
		for(String in:lines.readLines()){
			if(in.matches(_chapter+_toEnd))
				in=in.replaceAll(_chapter,"+").toUpperCase();
			else if(in.matches(_section+_toEnd))
				in=in.replaceAll(_section,"-");
			else if(in.matches(_sub+_toEnd))
				in=in.replaceAll(_sub,"");
			else if(in.matches(_appendix+_toEnd))
				in=in.replaceAll(_appendix,"-");
			in=in.replaceFirst("^(Figure|Abb\\.|Table|Tabelle|Tableau)\\s","");
			in=in.replaceFirst("^([\\d.A+]+)\\t","$1 ");
			lines.writeNextLine(in);
		}
		lines.closeLineWriter();
	}
}
