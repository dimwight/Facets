package facets.util;
import static facets.util.HtmlBuilder.RenderTarget.*;
import static facets.util.Regex.*;
import static facets.util.Strings.*;
import static facets.util.Util.*;
import static java.lang.Character.*;
import java.util.Collections;
/**
Encapsulates assembly and management of HTML code. 
 */
public abstract class HtmlBuilder extends Tracer{
	private static final Tracer t=new Tracer(HtmlBuilder.class);
	public enum RenderTarget{Swing,Outlook,Dreamweaver;
		public int paraFontPoints(){
			return this==Swing?swingFontSize():10;
		}
		private int swingFontSize(){
			return 12;
		}
		public int cellPadding(){
			return false&&this==Outlook?9:false&&this==Swing?swingFontSize():10;
		}
		boolean pageTabled(){
			return false&&this==Swing;
		}
	}
	public static final String HTTP="http://";
	private static final String FONT_FAMILY="Arial,sans-serif";
	final static private String 
		comment="<![^>]+>",
		script="(?s)<script.*</script>",
		stripTags="</?(span|dir|div|font|img|blockquote|meta|o|!\\[)\\b[^>]*>",
		simpleLink="(a\\s+href=\"[^\"\\?]+\")",
		stripAtt="<(\\w+)[^>]*(style=|mso|%)[^>]*>",
		tableTag="<table>",
		formatBeforePara="<(b|i|u)>\\s*<p>",
		attValue="=((\"[^\"]+\")|('[^']+'))",
		allowAtt=" (align|border|cellspacing|cellpadding|bordercolor|valign"+attValue+")",
		attPair=" \\w+"+attValue,
		unhideAtt=" \\*(\\w+"+attValue+")",
		div="(</?)(div)[^>]*>",
		closePar="</p>",
		nbsp="(Â| |(&nbsp;))+",
		multiPar="(\\s*<p>\\s*)+",
		tdPar="(<td>[^/]*)<p[^>]*>([^/]*</td>)",
		tdParTop="<td>\\s*<p>",
		tdEmpty="<td([^>]*)>\\s*</td>",
		rowStart="(?s)(<tr>)\\s+(<td>)",
		rowEnd="(?s)(</td>)\\s+(</tr>)",
		ruleLike="<p>_+",
		openLine="<(p|P|tr|TR)>",
		htmlTop_="(?s).*<html.+</head>.*<body[^>]*>",
		htmlTail_="(?s)</body>\\s*</html>.*",
		tag="\\s*</?\\w+[^>]*>\\s*";	
	private static String readLcContent(String raw){
		StringBuilder lcTags=new StringBuilder();
		boolean doLc=false;
		for(int r=0;r<raw.length();r++){
			char charAt=raw.charAt(r),anchor='a';
			if(charAt=='<'&&toLowerCase(raw.charAt(r+1))!=anchor)doLc=true;
			else doLc&=charAt!='>';
			if(doLc)charAt=toLowerCase(charAt);
			lcTags.append(charAt);
		}
		if(false)t.trace(".readLcContent: raw=\n"+raw);
		raw=lcTags.toString();
		String content_=true?"":replaceAll(raw,htmlTop_,"",htmlTail_,""),
				content=raw.substring(raw.indexOf(">",
						raw.indexOf("<body"))+1,raw.indexOf("</body>"));
		if(false)t.trace(".readLcContent~:"
						+(content.length()-content_.length()));
		return content;
	}
	public static String normalisedContent(String rawNow,String rawThen,boolean strong){
			String content=readLcContent(rawNow),normalised;
			if(strong){
				normalised=replaceAll(content,new String[]{
					script,"",
					comment,"",
					stripTags,"",
					"<\\s*"+simpleLink+"\\s*>",":$1:",
					stripAtt,"<$1>",
					":"+simpleLink+":","<$1>",
					tableTag,"<table class='para' border='1' cellspacing='0' cellpadding='2' bordercolor='#999999'>",
					
					closePar,"",
					nbsp," ",
					multiPar,"\n<p>",
					
					tdPar,"$1$2",
					tdParTop,"<td>",
					tdEmpty,"<td$1>&nbsp;</td>",
					
					formatBeforePara,"<p><$1>",
					ruleLike,"<hr>",
					
					" +"," ",
					"\\s*\n+\\s*","\n",
	
					rowStart,"$1$2",
					rowEnd,"$1$2",
				}).trim();
			}
			else{
				String[]ruledNow=splitAfterRules(content,-1),
						ruledThen=rawThen==null||rawThen.equals("")?new String[]{}
							:splitAfterRules(readLcContent(rawThen),-1);
				for(int nowAt=0;nowAt<ruledNow.length;nowAt++)
					if(nowAt>ruledThen.length-1||!ruledNow[nowAt].equals(ruledThen[nowAt])){
						ruledNow[nowAt]=replaceAll(ruledNow[nowAt],new String[]{
	//						allowAtt," *$1",
	//						attPair,"",
	//						unhideAtt," $1",
								closePar,"",
								multiPar,"\n<p>",
								tdPar,"$1$2",
								tdEmpty,"<td$1>&nbsp;</td>",
								" +"," ",
								"\\s*\n+\\s*","\n",
								rowStart,"$1$2",
								rowEnd,"$1$2",
						}).trim();
						if(false)t.trace(".normalisedContent: changed ruledAt="+nowAt
								+" ruledNow="+ruledNow[nowAt].length());
					}
				normalised=linesString(ruledNow).trim();
			}
			if(false)t.trace(":>>>>>>>>>>>>>>>>>>>>>>> content=\n"+content+
					"\n>>>>>>>>>>>>>>>>>>>>>>> normalised=\n"+normalised);
			return codeToText(normalised).equals("")?"":normalised;
		}
	public static String codeToText(String code){
		String text=replaceAll(code,new String[]{
			openLine,"<*p>",
			tag," ",
			nbsp," ",
			"\\s+"," ",
			"\\s*<\\*p>\\s*","\n",
			"\\s*\n+\\s*","\n"
		}).trim();
		if(false&&text!=code)t.trace(".codeToLines: plain=\n",text);
		return text;
	}
	static public String[]splitAfterRules(String text,int limit){
		return splitAfter(text,"(<\\s*((hr)|(HR))\\s*>)",limit);
	}
	protected final RenderTarget rt;
	public HtmlBuilder(RenderTarget rt){
		this.rt=rt;
	}
	public HtmlBuilder(){
		this(RenderTarget.Swing);
	}
	@Override
	protected void traceOutput(String msg){
		if(false)super.traceOutput(msg);
	}
	final public String buildPage(){
		String top=newPageTop(),tail=newPageTail(),content=newPageContent();
		trace(".buildPage: content lines="+content.split("\n").length);
		return top+content+tail;
	}
	final public String newPageTop(){
		return"<html><head><title>"+pageTitle()+"</title>\n"+
			newPageStyles()+"</head><body bgcolor=\""+pageColor() +"\">\n"+newContentTop();
	}
	protected String pageTitle(){
		return "pageTitle()";
	}
	final public String newPageStyles(){
		String[]styles=buildPageStyles(pagePoints());
		if(styles==null||styles.length==0)return"";
		ItemList<String>out=new ItemList(String.class);
		for(String style:styles){
			if(style.trim().equals(""))continue;
			String[]splits=style.split("\\{");
			for(String selector:splits[0].split(","))
				out.add(selector+"{"+splits[1]);
		}
		Collections.sort(out);
		trace(".newPageStyles: out=",out);
		return "<style type=\"text/css\">\n"+linesString(out.items()
					).replaceAll("([{;]\\s*)","$1\n"
					).replaceAll(":\\s*",": ")
			+"\n</style>\n";
	}
	protected String[]buildPageStyles(double points){
		return newDefaultStyles(points,rt);
	}
	protected double pagePoints(){
		return rt==Swing?12:10;
	}
	protected String pageColor(){
		return"#ffffff";
	}
	protected String newContentTop(){
		return (inPageTable()?("<table width="+pageTableWidth()
				+" cellpadding="+pageInset()+" style='"+
		"border:"+
		" thin"+//1px,
		" none"+//,solid
		" #ff0000"+
		";"+
		"background-color: #ffffff;"+//,eeeeee
		"margin: 0px;"+//,10px
		"'><tr><td style='border-style: none;'>\n"):"") +
		(true?"":("<!--end of HtmlBuilder.newPageTop()-->\n"));
	}
	protected String pageTableWidth(){
		return"100%";
	}
	protected int pageInset(){
		return 0;
	}
	public String newPageContent(){
		return"<p>newPageContent()";
	}
	final public String newPageTail(){
		return newContentTail()+"</body></html>";
	}
	protected String newContentTail(){
		return(true?"":("\n<!--start of HtmlBuilder.newPageTail()-->"))+
			(inPageTable()?"\n</td></tr></table>\n":"");
	}
	protected boolean inPageTable(){
		return rt.pageTabled();
	}
	protected static String[]newDefaultStyles(double points,RenderTarget rt){
		double para=points/2;
		String beforePara="margin-top:"+sf(para)+"pt;",
			afterPara0="margin-bottom: 0pt;",
			indentLeft="margin-left: 24pt;",
			afterListOrTable="margin-bottom:"+sf(para*1)+"pt;",
			lineHeight="line-height: 1.25;";
		return new String[]{
			newHeadingStyle(points,1),
			newHeadingStyle(points,2),
			newHeadingStyle(points,3),
			newHeadingStyle(points,4),
			"p,td,th,li,dd,dt{"+
				"font-family:"+FONT_FAMILY+";"+
				"font-size:"+sf(points)+"pt;"+
				lineHeight+
				"}",
			"th{" +
				"font-weight: bold;" +
			"}",
			"p{"+
				beforePara+
				afterPara0 +
				"}",
			"li{"+
				afterPara0 +
				lineHeight+
				"list-contentStyle-position: outside;"+
				"}",
			"ul,ol,dl{"+
				beforePara +
				afterListOrTable+
				"}",
			"ul,ol{"+
				indentLeft+
				"}",
			"ul{"+
				"list-style-type: disc;"+//circle,,square
				"}",
			"dt{"+
				"font-size:"+sf(points*1.1)+"pt;"+
				"font-style: italic;"+
				"font-weight: bold;"+
				beforePara+
				"}",
			"dd{"+
				indentLeft+
				"margin-top: 0pt;"+
				"}",
			"table.para{"+
				beforePara +
				afterListOrTable+
				"}",
			"table.box,table.data{" +
				"margin-top: 0pt;"+
				"border-style: single;" +
				"border-color: #ff0000;" +
				"}",
			"table.box{" +
				"margin-top: 0pt;"+
				"border-style: single;" +
				"border-color: #eeeeee;" +
				"}",
			true?"":rt==Swing?("table,td{border: 0.75pt #0 dotted;}")//solid,,,thin
			:("table,td{"+
					"border-style: solid;"+//,dotted
					"border-width: thin;"+//,0.75pt,thick
					"border-color: black;"+//,red
					"border-collapse: collapse;"+//,red
					"}"),
			rt!=Swing?"a:link {color:#FF0000;}":"a:link {color:#FF0000;}"
		};
	}
	private static String newHeadingStyle(double points,int level){
		double multiple;
		switch(level){
		case 1:multiple=1.5;break;
		case 2:multiple=1.3;break;
		case 3:multiple=1.1;break;
		default:
			multiple=1;
		}
		return "h"+level+"{font-family:"+FONT_FAMILY+";" +
				"font-size:"+sf(points*multiple)+"pt;"+
				"margin-top:"+sf(points*multiple*0.75)+"pt;"+
				"margin-bottom:"+sf(points*0.25)+"pt;"+
				"}";
	}
	public static String tagContents(final String text,String tagName){
		return text.replaceAll("(?s).+<"+tagName+">(.*)</"+tagName+">.*","$1").trim();
	}
}