package applicable;
import facets.util.Debug;
import facets.util.HtmlBuilder;
import facets.util.Strings;
import facets.util.TextLines;
import facets.util.Tracer;
import facets.util.Util;
import facets.util.tree.DataNode;
import facets.util.tree.NodeList;
import facets.util.tree.TypedNode;
import facets.util.tree.ValueNode;
import facets.util.tree.XmlDocRoot;
import facets.util.tree.XmlPolicy;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JApplet;
public class HtmlRecordApplet extends JApplet{
	public static final String NO_RECORDS="No records found",
		PARAM_RECORDS_XML="recordsXml";
	private static final String TYPE_RECORD_SET="RecordSet",TYPE_RECORD="Record";
	private static final boolean facetsSource=false;
	private static final XmlPolicy XML_POLICY=new XmlPolicy(){
		@Override
		protected boolean treeAsXmlRoot(){
			return true;
		}
		@Override
		protected boolean dataUsesAttributes(){
			return true;
		}
		@Override
		protected ValueNode getTitleAttributeNames(){
			return newTitleAttributeNames("title",new String[]{
				TYPE_RECORD_SET+"=source",
				TYPE_RECORD+"=number"
			});
		}
	};
	protected final class HtmlQueryFilter extends TextQueryFilter<HtmlRecord>{
		public HtmlQueryFilter(String[] terms,boolean matchAny){
			super(terms,matchAny,false);
		}
		protected boolean passes(HtmlRecord f){
			if(!facetsSource)return super.passes(f);
			return f.searchableText().trim().toLowerCase().matches(".*("+terms+").*");
		}
		@Override
		protected TextSearchable newExceptionResult(Exception e){
			return HtmlRecord.ERROR;
		}
	}
	final public int applyQuery(String queryText,boolean matchAny){try{
		if(all.size()==0){
			trace(".applyQuery: No records");
			return 0;
		}
		results.clear();
		trace(".applyQuery: queryText='"+queryText+"' matchAny="+matchAny);
		TextQueryFilter filter=null;
		results.addAll(queryText.trim().equals("")?all
				:(filter=newQueryFilter(queryText,matchAny)).filter(all));
		trace(".applyQuery: filtered="+results.size()+(
				true||filter==null?"":"\n"+filter.summary()
		));
		if(!queryText.equals(queryTextThen))outStart=0;
		queryTextThen=queryText;
		return outCount=results.size();
	}catch(Throwable t){
		t.printStackTrace();
		throw new RuntimeException(t);
	}}
	public static class HtmlRecord implements TextSearchable{
		public static final HtmlRecord ERROR=new HtmlRecord("Invalid query text");
		protected final String[]fieldValues;
		public HtmlRecord(String...fieldValues){
			this.fieldValues=fieldValues;
		}
		protected String[]fieldNames(){
			return new String[]{};
		}
		@Override
		public String searchableText(){
			return Strings.linesString(fieldValues);
		}
		protected String listRowHtml(int rowAt){
			StringBuilder html=new StringBuilder("<tr valign=top>");
			String[]names=fieldNames();
			int at=0;
			for(String field:fieldValues)html.append(
					listCellHtml(at<names.length?names[at++]:"Field"+at++,
					field,rowAt));
			html.append("</tr>");
			return html.toString();
		}
		protected String listCellHtml(String name,String value,int rowAt){
			return"<td>&nbsp;"+(rowAt<0?"<b>"+value+"</b>":value)+"&nbsp;</td>";
		}
	}
	private final List<HtmlRecord>all=new ArrayList(),results=new ArrayList();
	private final HtmlBuilder html;
	private final boolean inSubClass;
	private String recordsXmlName;
	private int outStart,outStop,outCount;
	private final Map<Integer,HtmlRecord>rowRecords=new HashMap();
	private HtmlRecord headerRecord;
	private Object queryTextThen;
	public HtmlRecordApplet(){
		this(false,new HtmlBuilder(){});
	}
	public HtmlRecordApplet(boolean inSubClass,HtmlBuilder html){
		this.inSubClass=inSubClass;
		this.html=html;
	}
	public void setRecordsXmlValue(String value){
		recordsXmlName=value;
	}
	public void init(){
		super.init();
	  if(recordsXmlName==null)recordsXmlName=getParameter(PARAM_RECORDS_XML);
		try{
			if(recordsXmlName==null||recordsXmlName.trim().equals(""))
				throw new IllegalStateException("Null or empty recordsXmlName in "+Debug.info(this));
			URL url=new URL("");//getResourceUrl(recordsXmlName);
			DataNode recordSet=new DataNode(TYPE_RECORD_SET,url.getFile());
			new XmlDocRoot(recordSet,XML_POLICY).readFromSource(new TextLines(url));
			trace(".init: loaded url="+recordsXmlName);
			String[]fieldNames=null;
			for(TypedNode child:recordSet.children()){
				String[]fieldValues=((DataNode)child).values();
				if(headerRecord==null)
					headerRecord=newFieldsRecord(fieldNames=fieldValues,fieldValues);
				else all.add(newFieldsRecord(fieldNames,fieldValues));
			}
			trace(".init: records="+all.size());
		}catch(Exception e){
			trace(".init: Error loading recordsXmlName="+recordsXmlName);
			if(true)throw new RuntimeException(e);
		}
	}
	protected HtmlRecord newFieldsRecord(String[]fieldNames,String[]fieldValues){
		return new HtmlRecord(fieldValues);
	}
	protected void trace(String msg){
		Util.printOut(getClass().getSimpleName(),msg);
	}
	protected TextQueryFilter newQueryFilter(String queryText,boolean matchAny){
		if(inSubClass)throw new RuntimeException("Not implemented in "+Debug.info(this));
		else return new HtmlQueryFilter(TextQueryFilter.textToTerms(queryText),matchAny);
	}
	public final String getPageTop(){
		return html.newPageTop();
	}
	public final String getPageStyles(){
		return html.newPageStyles();
	}
	final public String getRowsTable(int rowCount){
		if(results.size()==0)return NO_RECORDS;
		if(rowCount<=0)rowCount=0;
		if(false)trace(".getRowsTable: rowCount="+rowCount+" outStart="+outStart);
		outStop=Math.min(outCount,outStart+rowCount);
		List<HtmlRecord>nextRecords=results.subList(outStart,outStop);
		trace(".getRowsTable: nextRecords="+nextRecords.size());
		if(false&&(outStart+=rowCount)>outCount)outStart=0;
		rowRecords.clear();
		List<String>htmlLines=new ArrayList();
		htmlLines.add("<table width=100% border=0 cellpadding=\"2\" cellspacing=\"0\">");
		int rowAt=-1;
		htmlLines.add(headerRecord.listRowHtml(rowAt++));
		for(HtmlRecord record:nextRecords){
			rowRecords.put(rowAt,record);
			htmlLines.add(record.listRowHtml(rowAt++));
		}
		htmlLines.add("</table>");
		return Strings.linesString(htmlLines.toArray(new String[]{}));
	}
	public HtmlRecord recordForRow(int rowAt){
		HtmlRecord record=rowRecords.get(rowAt);
		if(record==null)throw new IllegalStateException(
				"Null record for rowAt="+rowAt);
		return record;
	}
	public final String getPageTail(){
		return html.newPageTail();
	}
	static void main(String[]args)throws IOException{
	  TextLines.setDefaultEncoding(false);
		TextLines.createIfRequired(new File("records.html"),Pages.framesHtml);
		TextLines.createIfRequired(new File("records-header.html"),Pages.headerHtml);
		HtmlRecordApplet applet=new HtmlRecordApplet();
		File source=new File(facetsSource?"Facet-all.txt":"records.txt"),
			xml=new File("records.xml");
		if(true)throw new RuntimeException("Untested for source="+source);
		new SourceXmlBuilder(){
			protected Iterable<String[]>newRecordFields(File fileIn)throws IOException{
				List<String[]>fields=new ArrayList();
				fields.add(true||facetsSource?new String[]{"Output"}
					:new String[]{"Class Path","Dump text"});
				for(String line:new TextLines(fileIn).readLines()){
					String classPath=!facetsSource?line.replace(".","/").replace("[Loaded ",""
							).replace(" from shared objects file]",".class"
								).replace(" from file:/C:/eclipse/workspace/Facet/]",".class")
						:line;
					fields.add(true||facetsSource?new String[]{classPath}
							:new String[]{classPath,line});
				}
				return fields;
			}
		}.buildSourceXml(source,xml);
		applet.setRecordsXmlValue(xml.getName());
		applet.init();
		int rowCount=20;
		String[]facetsQueries={"","","class","class","interface","(","class|interface","enum","\\.java"},
			dumpQueries={"","facets"};
		for(String q:!facetsSource?dumpQueries:facetsQueries){
			int recordCount=applet.applyQuery(q, false);
			Util.printOut("HtmlRecordApplet.main: .main: text length=",
					applet.getRowsTable(rowCount+=0).length());
		}
	}
	public static abstract class SourceXmlBuilder{
		final public void buildSourceXml(File fileIn,File fileOut)throws IOException{
			NodeList recordSet=new NodeList(new DataNode(TYPE_RECORD_SET,fileIn.toString(),
					new Object[]{"date="+new Date()}),false);
			int count=0;
			for(String[]recordFields:newRecordFields(fileIn))
				recordSet.add(new DataNode(TYPE_RECORD,true?TypedNode.UNTITLED:
						""+count++,recordFields));
			recordSet.updateParent();
			Object xml=false?TextLines.newBuffer():
				new TextLines(fileOut);
			new XmlDocRoot(recordSet.parent,XML_POLICY).writeToSink(xml);
			Util.printOut("HtmlRecordApplet.SourceXmlBuilder: xml=",xml);
		}
		protected abstract Iterable<String[]>newRecordFields(File fileIn)
			throws IOException;	
	}
	private static final class Pages{
		private final static String framesHtml=
			"<html><head><title>Records List and Query</title></head>\r\n" + 
			"<frameset rows=\"90,*\" frameborder=\"no\" bordercolor=#FF9999>\r\n" + 
			"<frame src=\"records-header.html\" scrolling=\"no\"/>\r\n" + 
			"<frame name=\"list\" scrolling=\"auto\" marginwidth=\"5\"/>\r\n" + 
			"</frameset>\r\n" + 
			"</html>\r\n",
		headerHtml="<html>\r\n" + 
				"<head>\r\n" + 
				"<title>Records Header</title>\r\n" + 
				"<script type=\"text/javascript\">\r\n" + 
				"var loadMsg='<html><head><title>Loading...</title></head>'+\r\n" + 
				"'<body bgcolor=\"#FFFFFF\" text=\"#000000\">'+\r\n" + 
				"'<h3>&nbsp;</h3><p><i>Loading records, please wait...</i> '+\r\n" + 
				"'</body>'+\r\n" + 
				"'</html>'\r\n" + 
				"function getAndWriteRows(){\r\n" + 
				"	listDoc=parent.window.frames.list.document\r\n" + 
				"	listDoc.open()\r\n" + 
				"	listDoc.write(loadMsg)\r\n" + 
				"	listDoc.close()\r\n" + 
				"	if(false)return;\r\n" + 
				"	queryValue=document.forms.input.query.value\r\n" + 
				"	if(queryValue.length==0)queryValue=\"\"\r\n" + 
				"	applet=document.applets.records\r\n" + 
				"	listRowCount=applet.applyQuery(queryValue)\r\n" + 
				"	rowsHtml=applet.getRowsTable(50)\r\n" + 
				"	listDoc.open()\r\n" + 
				"	listDoc.write(applet.getPageTop())\r\n" + 
				"	listDoc.write(rowsHtml)\r\n" + 
				"	listDoc.write(applet.getPageTail())\r\n" + 
				"	listDoc.close()\r\n" + 
				"}\r\n" + 
				"function noenter(){\r\n" + 
				"	isEnter=window.event&&window.event.keyCode==13\r\n" + 
				"	if(isEnter)getAndWriteRows()\r\n" + 
				"  return !isEnter; \r\n" + 
				"}\r\n" + 
				"</script>\r\n" + 
				"</head>\r\n" + 
				"<body onload=\"getAndWriteRows()\" bgcolor=\"#FFFFFF\">\r\n" + 
				"<table cellspacing=\"0\">\r\n" + 
				"	<tr>\r\n" + 
				"		<td title=\"Enter one or more text items, then click Go or hit Enter\">\r\n" + 
				"		<form name=\"input\" action=\"\">Search for <input type=\"text\"\r\n" + 
				"			name=\"query\" onkeypress=\"return noenter()\" size=\"40\" value=\"\"\r\n" + 
				"		> <input type=\"button\" value=\"Go\" onclick=\"getAndWriteRows()\">\r\n" + 
				"		</form>\r\n" + 
				"		<applet name=\"records\" code=\"facets.util.ext.HtmlRecordApplet\"\r\n" + 
				"			height=\"0\" width=\"0\" alt=\"\">\r\n" + 
				"			<param name=\"recordsXml\" value=\"records.xml\">\r\n" + 
				"		</applet>\r\n" + 
				"		</td>\r\n" + 
				"	</tr>\r\n" + 
				"</table>\r\n" + 
				"</body>\r\n" + 
				"</html>\r\n";
	}
}
