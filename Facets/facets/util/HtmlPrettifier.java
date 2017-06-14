package facets.util;
import static facets.util.Regex.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
public final class HtmlPrettifier extends Tracer{
	enum TagBreaks{
		HTML(false,false),
		HEAD(false,false),
		BODY(false,true),
		P(true,false),
		UL(true,true),
		OL(true,true),
		LI(true,false),
		B(false,false),
		TR(true,true),
		TD(true,true),
		INPUT(true,true),
		I(false,false);
		TagBreaks(boolean opening,boolean closing){
			this.opening=opening;
			this.closing=closing;
		}
		final boolean opening,closing;
		static TagBreaks match(String tag){
			for(TagBreaks spec:values())
				if(tag.toLowerCase().trim().replaceAll("</?(\\w+).+","$1"
						).equals(spec.name().toLowerCase()))return spec;
			throw new IllegalArgumentException("Invalid tag="+tag);
		}
	}
	public InputStream prettyStream(InputStream raw)throws IOException{
		String clean=replaceAll(new TextLines(raw).readLinesString(),
				"(?s)\\s+"," ","\\s*([<>])\\s*","$1");
		StringBuilder text=new StringBuilder();
		for(int c=0,contentAt=0,indent=0,tagAt;c<clean.length();c+=0){
			if(!clean.substring(c++).startsWith("<"))continue;
			text.append(clean.substring(contentAt,tagAt=--c));
			while(!clean.substring(++c).startsWith(">"));
			String tag=clean.substring(tagAt,contentAt=++c);
			TagBreaks breaks=TagBreaks.match(tag);
			if(false)trace(".prettyStream: tag=",breaks);
			boolean closing=tag.contains("/"),indents=breaks.opening||breaks.closing;
			if(indents){
				if(closing)indent--;
				if(closing?breaks.closing:breaks.opening){
					for(int spaces=0;spaces<indent;spaces++)tag="\t"+tag;
					text.append("\n");
				}
				if(!closing)indent++;
			}
			text.append(tag);
		}
		if(false)trace(".prettyStream: text=",text);
		return TextLines.newBuffer(new String[]{text.toString()}).newInputStream();
	}
	public static void main(String[]args)throws Exception{
		TextLines.setDefaultEncoding(true);
		new HtmlPrettifier().prettyStream(
				new FileInputStream(new File("_doc/HtmlSelected.html")));
	}
}
