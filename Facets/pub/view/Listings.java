package pub.view;
import static facets.util.tree.DataConstants.*;
import static pub.PubValues.*;
import static pub.view.ListingContent.*;
import static pub.view.PubsView.*;
import static pub.view.RecordProxy.*;
import facets.util.HtmlBuilder;
import facets.util.HtmlBuilder.RenderTarget;
import facets.util.Objects;
import facets.util.TextLines;
import facets.util.Times;
import facets.util.Tracer;
import facets.util.Util;
import facets.util.app.AppValues;
import facets.util.tree.Nodes;
import facets.util.tree.TypedNode;
import facets.util.tree.ValueNode;
import facets.util.tree.XmlSpecifier;
import facets.util.tree.Nodes.TreeRoot;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import pub.DrawingsInc;
import pub.PubIndexer;
import pub.PubValues;
import applicable.HtmlRecordApplet.SourceXmlBuilder;
import applicable.field.FieldProxy;
final class Listings extends Tracer{
	public static final String ARG_STORED="checkStored",ARG_MCIR="checkMCIR";
	private final static String viewName=VIEW_TITLE+EXT_XML_SZIP;
	public static void main(String[]args)throws IOException{
		if(userView)throw new RuntimeException("Not implemented for userView="+userView);
		AppValues values=new AppValues(Listings.class);
		values.readValues(args);
		ValueNode read=values.args();
		if(read.getBoolean(ARG_STORED)){
			new ListingContent(new File(DIR_USER,viewName)).checkStored();
			return;
		}
		else if(read.getBoolean(Listings.ARG_MCIR)){
			checkMcir();
			return;
		}
		else if(true){
			PubIndexer.doUpdates();
			return;
		}
		else if(false){
			TextLines.setEncoding("Cp1252");
			new DrawingsInc(new File(ROOT_PDFS)).writeInc(true);
			return;
		}
		else newChecked("_"+viewName);
	}
	private static void checkMcir()throws IOException{//C:\Tray\MCIR.txt
		final File admin=userView?null:DIR_USER,records=new File(admin,"~records.xml");
		new SourceXmlBuilder(){
			protected Iterable<String[]>newRecordFields(File file){
				TextLines.setDefaultEncoding(false);
				final XmlSpecifier spec=fileSpec;
				TreeRoot root=spec.newTreeRoot(spec.newRootNode(file));
				try{
					root.readFromSource(file);
				}catch(IOException e){
					throw new RuntimeException(e);
				}
				PubFields fields=FieldsSpec.MCIR.newFields(null);
				List<String[]>listing=new ArrayList();
				listing.add(Objects.toString(fields.liveFields()).split(","));
				int stopAt=5,at=0;
				for(TypedNode child:root.tree.children()){
					if(false&&at++>stopAt)break;
					adjustPubSearchStatus(child);
					adjustEcoOrder(child);
					listing.add(fields.newProxy(child).newFieldTexts(
							).split(FieldProxy.FIELD_SEPARATOR));
				}
				return listing;
			}
		}.buildSourceXml(new File(admin,viewName),records);
		String existing=new TextLines(records).readLinesString();
		final StringBuilder ignores=new StringBuilder("<table cellspacing=0>\n"),
			actions=new StringBuilder(ignores);
		List<ValueNode>edits=new ArrayList();
		for(String row:new TextLines(new File("C:/Tray/MCIR.txt")).readLines()){
			String[]cells=row.split("\t");
			if(cells.length<12)continue;
			String proposedType=cells[11];
			if(proposedType.equals("")||proposedType.startsWith("Pre-Oracle 5March2012"))continue;
			String description=cells[1].toUpperCase();
			if(!description.matches(".*((MANUAL)|(ADDENDUM)).*"))continue;
			String pubNum=cells[0].trim(),engComplete=cells.length<17?"":cells[17],order=cells[5],
					unquote=description.substring(1,description.length()-1),
					top=newCell(pubNum)+newCell(unquote);
			t.trace(".checkMcir: pubNum="+pubNum+" order="+order+" engComplete="+engComplete+" "+unquote);
			if(!pubNum.matches("\\d{7}.*"))continue;
			boolean notComplete=!engComplete.matches(".+/.+");
			if(false&&notComplete)t.trace(": No engComplete for pubNum="+pubNum);
			if(notComplete||existing.contains("<Record>\n"+pubNum)
					||(description.matches(".*KIT.*")&&!pubNum.endsWith("1"))
				)
				ignores.append("<tr>"+top+"</tr>\n");
			else{
				String from=cells[7],release=cells[29];
				actions.append("<tr>"+top+newCell(order)+newCell(from)+newCell(release)+"</tr>\n");
				ValueNode values=new ValueNode(TYPE_FIELDS,new Object[]{
					FIELD_PUBNUM+"="+pubNum,
					"PubDetails="+unquote,
					"Originator="+from,
					"Order="+order,
					"Due=MCIR "+release,
					"Status=Planned",
					"Type=[Admin]"
				});
				Nodes.removeNullValues(values);
				edits.add(new ValueNode(TYPE_DOC,pubNum,new Object[]{
					values,
					KEY_MODIFIED+"="+System.currentTimeMillis(),
					KEY_USER+"="+userName
				}));
			}
		}
		if(dev)t.trace(": edits=",edits);
		else for(ValueNode edit:edits)PubFiles.writeEdit(edit);
		final String title=Listings.class.getSimpleName();
		if(false&&dev)new TextLines(new File(admin,title+".html")).writeLines(
				new HtmlBuilder(RenderTarget.Dreamweaver){
			@Override
			protected String pageTitle(){
				return title;
			}
			protected double pagePoints(){
				return 9;
			}
			@Override
			public String newPageContent(){
				return actions+"</table>\n"+"\n<h3>[Ignores]\n"+ignores+"</table>\n";
			}
		}.buildPage().split("\n"));
	}
	private static String newCell(String text){
		return "<td>&nbsp;"+text+"&nbsp;</td>" ;
	}
	final private static Tracer t=new Tracer(Listings.class);
}
