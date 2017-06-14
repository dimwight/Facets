package demo.codeview;
import facets.util.Debug;
import facets.util.HtmlBuilder;
import facets.util.ItemList;
import facets.util.Regex;
import facets.util.Strings;
import facets.util.Util;
import facets.util.app.ProvidingCache;
import facets.util.app.ProvidingCache.ItemProvider;
import facets.util.tree.ValueNode;
/**
Converts Java source code to syntax-coloured HTML. 
 */
public final class JavaHtmlPages{	
	private final ProvidingCache cache=new ProvidingCache(20,null);
	private static final String 
		findTitles="\\b([A-L,N-Z]\\w+)",
		fixTitles="<span title=\"Maybe title?\" contentStyle=\"cursor:help\">$1</span>",
		findImports="(import )[^/]+",fixImports="import[...] \n\n",
		reserved=
		"package|import|final|public|protected|private" +
		"|extends|implements|class|interface|abstract" +
		"|void|null|transient|static|new|return|super|this" +
		"|if|for|while|do|try|catch|else|continue|break|switch" +
		"|int|double|boolean|true|false",
		findReserved="\\b(" +reserved+")\\b",
		delimiter="([<\\+\\-\\*/=:||?\\s.,;&!|\\[\\]\\(\\)\\{\\}\"])",
		findDelimiter=delimiter,doubleDelimiter="$1\\$$1",
		findDelimiters=delimiter+"\\$"+delimiter,halveDelimiters="$1",
		findConstant=delimiter+"([A-Z_]+)"+delimiter,
		findDigit=delimiter+"([0-9]+)"+delimiter,
		find="\\b(S[A-Z][a-z][A-Za-z]+)\\b",
		findBlankLine="(?m)^$",fixBlankLine="&nbsp;",
		findSpaces="  ",fixSpaces="\t",
		findTab="\t",fixTab="&nbsp;&nbsp;",
		findLine="([^\n\r]+)\n",fixLine="<p>$1</p>\n",
		findLessThan="<",fixLessThan="&lt;",
		findMoreThan=">",fixMoreThan="&gt;",				
		findString="\"([^\"]*)\"",
		findMethodName="([^w]\\s+)" +"([A-Za-z]*)" 
			+"\\s*\\(" +(true?"([^)]*)":"([A-z_\\s,\\[\\]]*)") +"\\)\\s*\\{",
		toStar="[^\\*]*\\*",notSlashToStars="([^/][^\\*]*\\*)*",
		toClosingSlash=toStar+notSlashToStars+"/",
		markSwap="#0",findSwap="#0\\b",notSwapped="[^#]",
		startCComment="/\\*[^\\*#+-]",
		startCPP="//",startJavadoc="/\\*\\*",
		endMultiLine="\\*/",
		startDelete="/\\*\\-",endDelete="-\\*/",
		deleteSpan="<span class=\"delete\">",
		startInsert="/\\*\\+",endInsert="\\+\\*/",
		insertSpan="<span class=\"insert\">",
	  fixChanges[][]={
			{
				"(</?)p>","$10>",
				"<[^0>]*>","",
				"(</?)0>","$1p>",
				"</p>","</span></p>",
				"<p>((&nbsp;)*)","<p>$1"+deleteSpan,			
				startDelete,deleteSpan,
				endDelete,"</span>",
		  },
			{
				"</p>","</span></p>",
				"<p>((&nbsp;)*)","<p>$1"+insertSpan,			
				startInsert,insertSpan,
				endInsert,"</span>",
		  }
		},
		findParaText="(<p>)([^<]*)(</p>)",
		tabPair="&nbsp;&nbsp;",
		titleableStart="\\b(",titleableStop=")\\b",
		titleSpanHead="<span title=\"",
		titleSpanTail="\"" //+" contentStyle=\"cursor:help\""
				+">$1</span>",
		titleValues[] ={
			"Target","Represents an application element to the surface",
			"Targeter","Connects a target and its exposing facet",
			"Facet","Creates and manages widgets in surface GUI",
			"Textual","Represents a text value to the surface",
			"FacetsFactory","Abstract factory for facet",
			"Coupler","Supplies policy and mechanism for a target",
			"Toggling","Represents a Boolean value to the surface",
			"Trigger","Represents an action or process to the surface",
			"Numeric","Represents a number to the surface",
			"NumberPolicy","Supplies policy for a number",
			"Indexing","Represents the index into a list",
			"FrameTarget","Frames application content",
			"AreaTarget","Root of an area target tree",
			"ContentAreaTargeter","Root of a content targeter tree",
		};
	private static String[]titleFixes;
	static {
		titleFixes = new String[titleValues.length];
		for(int i=0;i<titleFixes.length;i+=2) {
			titleFixes[i]=titleableStart+titleValues[i]+titleableStop;
			titleFixes[i+1]=titleSpanHead+titleValues[i+1]+titleSpanTail;
		}
	}
	private static String javadocHTML(String doc,double points,boolean javadocFirst){
		String tableTop="<table cellspacing=0 cellpadding=2 width=100%>" +
		"<tr valign=middle bgcolor=#ffffcc>\n" +
		"<td>&nbsp;</td><td width=80% class=\"jdoc\">" +
		"<font surface=sans-serif color=#666633>\n",
				tableTail=
		"</font></td><td width=20%>&nbsp;</td></tr></table>",
		disable="1234566",
		fixes[]={
		 "<p>","","</p>"," ",
		 "("+startJavadoc+")([^\\.\\?]*[\\.\\?]\\s)(.*)("+endMultiLine+")",
		 	javadocFirst?"$1$2$4":"$1$2$3$4",
		 "&lt;","<",
		 "&gt;",">",
		 "&nbsp;&nbsp;"," ",
		 "  "," ",
		 startJavadoc,tableTop,
		 endMultiLine,tableTail,
		 disable+"<p>","</font><p><font color=navy>",
		 disable+"</?(u|o)l>","",
		 disable+"<li>","<p> - ",
		 disable+"</li>","</p>",
		 "\\{@link\\s*#?([^\\}]*)\\}","<b>$1</b>",
		 "@param (\\w+)([^\n]*)","<br><code><i>$1</i></code>$2",
		 "(</?)code>","$1b>",
		 "([\\s(])[a-z.]+\\.([A-Z])","$1$2",
		 "@see ([^\n]*)","<br><b><i>See</i></b> $1",
		 "java.lang.","",
		 //"spike.","",
		 "(\\w)#(\\w)","$1.$2"
		};
		doc=Regex.replaceAll(doc,true,fixes);
		return doc;
	}
	final static class Page extends HtmlBuilder{
		private double points,leading;
		private int width,tabSpaces;
		private boolean styled,coloured,superficial,imports,javadocHTML,
			javadocFirst=true;
		private String text,title;
		public Page(ValueNode v){
			text=v.getString(0);
			points=v.getDouble(1);
			width=v.getInt(2);
			tabSpaces=v.getInt(3);
			styled=v.getBoolean(4);
			coloured=v.getBoolean(5);
			superficial=v.getBoolean(6);
			imports=v.getBoolean(7);
			javadocHTML=v.getBoolean(8);
			leading=v.getDouble(9);
			title=v.getString(10);
		}
		@Override
		public String newPageContent(){
	final String
			fixString=styled&&coloured?"<font color=navy><i>\"$1\"</i></font>":
				styled?"<i>\"$1\"</i>":coloured?"<font color=navy><i>\"$1\"</i></font>":
					"\"$1\"",
			fixMethodName=styled?"$1<b>$2</b>($3){":"$1$2($3){",
			fixReserved=styled&&coloured?"<font color=purple><b>$1</b></font>":
				styled?"<b>$1</b>":coloured?"<font color=purple>$1</font>":"$1",
			fixConstant=coloured?"$1<font color=blue>$2</font>$3":"$1$2$3",
			fixDigit=coloured?"$1<font color=fuchsia>$2</font>$3":"$1$2$3",
			fix=superficial&&coloured?
					"<font color=green>$1</font>":"$1",
			fixTab=tabPair+(tabSpaces<4?"":tabPair)+(tabSpaces<8?"":tabPair+tabPair),
			fixCComment=coloured?"<font color=green>$1</font>":"$1",
			fixCCommentLine=coloured?"$1<font color=green>$2</font>$3":"$1$2$3",
			fixJavadoc=coloured?"<font color=teal>$1</font>":"$1",
			fixJavadocLine=coloured?"$1<font color=teal>$2</font>$3":"$1$2$3",
			fixCPPCommentLine=coloured?"<font color=#999999>$1</font>":"$1",
			fixFirst[]={
				!imports?findImports:"nonsense",fixImports,
				findBlankLine,fixBlankLine,
				findSpaces,fixSpaces,
				findLessThan,fixLessThan,findMoreThan,fixMoreThan,
				findTab,fixTab,
				findLine,fixLine,
		  },
			fixComments[][]={
			{
				findParaText,fixCCommentLine,
				"(" +startCComment+toClosingSlash+")",fixCComment,
				"(" +startCComment+")",fixCComment,
				"(" +endMultiLine+")",fixCComment,
			},
			{"(.*)",fixCPPCommentLine},
			{
				findParaText,fixJavadocLine,
				"(" +startJavadoc+toClosingSlash+")",fixJavadoc,
				"(" +startJavadoc+")",fixJavadoc,
				"(" +endMultiLine+")",fixJavadoc,
			}
		},
		fixCode[]={
			findDelimiter,doubleDelimiter,
			findConstant,fixConstant,
			findDigit,fixDigit,
			findDelimiters,halveDelimiters,
			findString,fixString,
			findMethodName,fixMethodName,
			findReserved,fixReserved,
			find,fix,
	  },
		fixLast[]={
			"<p>[^<]*<table","<table"
	  };
		JavaHtmlPages.TextSwaps swapCComments=new JavaHtmlPages.TextSwaps(startCComment+toClosingSlash,
				"/*"+markSwap+"*/","/\\*"+findSwap+"\\*/"),
		swapCPPComments=new JavaHtmlPages.TextSwaps("(" +startCPP+notSwapped+"[^\n]*" +")",
				startCPP+markSwap,startCPP+findSwap),
		swapJavadocs=new JavaHtmlPages.TextSwaps("(" +startJavadoc+notSwapped+toClosingSlash+")",
				"/**"+markSwap+"*/",startJavadoc+findSwap+"\\*/"){
			public void applySwapFixes(String[]fixes){
				if(javadocHTML)for(int i=0;i<swaps.length;i++)
						swaps[i]=javadocHTML(swaps[i],points,javadocFirst);
				else super.applySwapFixes(fixes);
			}
		},
		swapDeletes=new JavaHtmlPages.TextSwaps("(" +startDelete+notSwapped+toClosingSlash+")",
				"/*-"+markSwap+"*/","/\\*-"+findSwap+"\\*/"),
		swapInserts=new JavaHtmlPages.TextSwaps("(" +startInsert+notSwapped+toClosingSlash+")",
				"/*+"+markSwap+"*/","/\\*\\+"+findSwap+"\\*/"){
			public void applySwapFixes(String[]fixes){
				if(false)return;
				super.applySwapFixes(fixes);
			}
		},
		swapComments[]={swapCComments,swapCPPComments,swapJavadocs},
		swapChanges[]={swapDeletes,swapInserts};
		text=Regex.replaceAll(text,true,fixFirst);
		for(int i=0;i<swapComments.length;i++){
			text=swapComments[i].storeSwaps(text);
			swapComments[i].applySwapFixes(fixComments[i]);
		}
		text=Regex.replaceAll(text,true,fixCode);
		if(titleFixes!=null)text=Regex.replaceAll(text,true,titleFixes);
		for(int i=0;i<swapComments.length;i++)
			text=swapComments[i].restoreSwaps(text);
		if(true)for(int i=0;i<swapChanges.length;i++){
			text=swapChanges[i].storeSwaps(text);
			swapChanges[i].applySwapFixes(fixChanges[i]);
			text=swapChanges[i].restoreSwaps(text);
		}
		text=Regex.replaceAll(text,true,fixLast);
		return text;
	}
	@Override
	protected String[]buildPageStyles(double points){
		return new String[]{
		"p{font-family:\"Courier New\",Courier;" +
			"font-size:+" +points+(points<50?"pt;":"%;")+
			"margin-top:" +leading+"pt;line-height:normal;" +
			"margin-left:0pt;margin-bottom:0pt;}",
		"td.jdoc{font-family:\"Arial\",sans-serif;font-size:+"+ 
			points+(points<50?"pt;":"%;")+"}",
		"li{list-contentStyle-position:outside;" +
			"font-family:sans-serif;font-size:" +points+"pt;"+
			"margin-top:" +leading+"pt;margin-left:0pt;margin-bottom:0pt;}",
		"ul{font-family:sans-serif;font-size:+" +points+"pt;"+
			"margin-top:" +leading+"pt;margin-left:24pt;margin-bottom:0pt;}"};
		}
	}
	private static class TextSwaps{
		private final String find,markSwap,findSwap;
		public String[]swaps;
		public TextSwaps(String find,String markSwap,String findSwap){
			this.find="("+find+")";
			this.markSwap=markSwap;
			this.findSwap=findSwap;
		}
		final public String storeSwaps(String text){
			if(false)Util.printOut("TextSwaps: find=",find+", mark="+markSwap);
			ItemList<String>swaps=new ItemList(String.class);
			String textThen=null;
			for(int marks=0;textThen==null||!textThen.equals(text);marks++){
				textThen=text;
				String swap=Regex.find(text,find),mark=markSwap.replaceAll("\\d+",""+marks);
				text=text.replaceFirst(find,mark);
				if(swap!=null)swaps.addItem(swap);
			}
			this.swaps=swaps.items();
			return text;
		}
		public void applySwapFixes(String[]fixes){
			if(swaps==null)throw new IllegalStateException("No swaps in "+Debug.info(this));
			else if(fixes==null)throw new IllegalArgumentException("Null fixes in "+Debug.info(this));
			for(int i=0;i<swaps.length;i++)swaps[i]=Regex.replaceAll(swaps[i],true,fixes);
		}
		final public String restoreSwaps(String text){
			if(swaps==null)throw new IllegalStateException("No swaps in "+Debug.info(this));
			for(int i=0;i<swaps.length;i++)
				text=text.replaceAll(findSwap.replaceAll("\\d+",""+i),swaps[i]);
			return text;
		}
	}
	/**
	Get HTML that renders source code.
	<p>The page returned may be from an internal store of previously 
	created pages, stored against keys composed of the parameters to this 
	method.   
	 @param java source code lines
	 @param title to be set as the page title
	 @param points the font size
	 @param width of enclosing table
	 @param tabSpaces the number of non-breaking spaces to represent each tab
	 @param styled use bold/italic formatting
	 @param coloured use colour
	 @param imports show imports
	 @param superficial highlight SXxx Facets types
	 @param javadocHTML render Javadoc comments 
	 */
	public String getHtmlPage(String[]java,String title,
			final double points,double leading,
			int width,int tabSpaces,boolean styled, boolean coloured,
			boolean imports, boolean superficial, 
			boolean javadocHTML){
		if(width<0){
			for(int i=0;i<java.length;i++)
				width=Math.max(java[i].length(),width);
				width=(int)(width*7.5);
		}
		final String lines=Strings.linesString(java);
		final Object[]keyValues={
				lines,//0
				points,//1
				width,//2
				tabSpaces,//3
				styled,//4
				coloured,//5
				superficial,//6
				imports,//7
				javadocHTML,//8
				leading,//9
				title
			};
		return new ItemProvider<String>(cache,this,JavaHtmlPages.class.getSimpleName()){
			@Override
			protected String newItem(){
				return new Page(new ValueNode("KeyValues",keyValues)).buildPage();
			}
			@Override
			protected long buildByteCount(){
				return lines.getBytes().length*2;
			}
		}.getForValues(keyValues);
	}
	static void main(String[]args){
		new JavaHtmlPages().getHtmlPage(args,"",1,1,1,1,true,true,true,true,true);
	}
}
