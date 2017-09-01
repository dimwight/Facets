package pub.view;
import static facets.util.Strings.*;
import static facets.util.tree.Nodes.*;
import static pub.PubValues.*;
import facets.util.Objects;
import facets.util.Util;
import facets.util.tree.DataConstants;
import facets.util.tree.DataNode;
import facets.util.tree.Nodes;
import facets.util.tree.TypedNode;
import facets.util.tree.ValueNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import pdft.PdfCore;
import pub.PubIssue;
import applicable.TextSearchable;
import applicable.field.FieldProxy;
import applicable.field.ValueField;
class RecordProxy extends FieldProxy implements TextSearchable{
	RecordProxy(TypedNode node,ValueField[]fields){
		super((ValueNode)node,fields);
	}
	@Override
	protected ValueNode getFieldValues(){
		return fieldValues((TypedNode)source);
	}
	@Override
	final public String searchableText(){
		return newFieldTexts();
	}
	static boolean ownsLock(ValueNode record){
		return record.getString(KEY_USER).equals(userName);
	}
	static boolean hasPubNum(TypedNode record){
		String pubNum=fieldValues(record).getString(FIELD_PUBNUM).trim();
		return !pubNum.equals("")&&!pubNum.equals(ValueNode.NULL_MARKER);
	}
	static ValueNode fieldValues(TypedNode record){
		ValueNode values=guaranteedChild((ValueNode)record,TYPE_FIELDS);
		if(values==null)throw new IllegalStateException(
				"Null values in record="+record);
		else return values;
	}
	static void adjustListable(TypedNode record,Map<PubIssue,String>paths,boolean summarise){
		ValueNode values=fieldValues(record);
		String pubNum=values.getString(FIELD_PUBNUM),rev=values.get("Rev"),
			path=paths.get(new PubIssue(pubNum+(rev==null?"":("_"+rev)).trim()));
		if(false&&pubNum.matches("30038(6|8|9).+"))
			Util.printOut("PubFields: pubNum="+pubNum+" rev="+rev+"path="+path);
		values.put("PDF",path!=null?path.trim():PubIssue.PDF_NONE);
		if(adjustEcoOrder(record))new RecordContent((DataNode)record,null).saveState();
		if(summarise){
			TypedNode texts=child(record,TYPE_TEXTS),notes=texts==null?null
					:child(texts,DataConstants.TYPE_TEXT,TITLE_BODY);
			if(notes!=null){
				String text=linesString((String[])notes.values()).replaceAll("\\s+"," ").trim();
				if(!text.equals("")&&!text.equals(RecordContent.EMPTY_NOTES))
					values.put("SummaryNotes",text);
			}
			for(String type:new String[]{TYPE_LINKS,TYPE_ATTACHMENTS}){
				TypedNode root=child(record,type);
				if(root==null)continue;
				List<String>titles=new ArrayList();
				for(TypedNode child:root.children())titles.add(child.title());
				if(!titles.isEmpty())
					values.put("Summary"+type,linesString(
							titles.toArray(new String[]{})).replaceAll("\\s+"," ").trim());
			}
		}
		removeNullValues(values);
		trimFields(record,PubFields.LIST_KEYS.split(","));
		record.setChildren(values);
	}
	static boolean adjustEcoOrder(TypedNode record){
		ValueNode values=fieldValues(record);
		if(values.get("Order")!=null)return false;
		String eco=values.get("EcoTail");
		if(eco==null)return false;
		if(true)throw new RuntimeException("Not implemented for "+record);
		values.put("Order","5-"+eco);
		removeValue(values,"EcoTail"); 
		if(false)setChild((DataNode)record,values);
		return true;
	}
	static void trimFields(TypedNode record,String[]fields){
		ValueNode values=fieldValues(record),
				trim=new ValueNode("trimFields",TypedNode.UNTITLED);
		for(String field:fields){
			String got=values.get(field);
			if(got!=null)trim.put(field,got);
		}
		values.setValues(trim.values());
		sortValues(values);
	}
	static long getModified(TypedNode record){
		return((ValueNode)record).getLong(KEY_MODIFIED);
	}
	static void putPdfOpenPage(TypedNode record,PubIssue issue){
		fieldValues(record).put(PdfCore.KEY_PAGE,issue.textQueryPages()[0]);
	}
	static int getPdfOpenPage(TypedNode record){
		return fieldValues(record).getOrPutInt(PdfCore.KEY_PAGE,0);
	}
	static void adjustPubSearchStatus(TypedNode record){
		ValueNode values=fieldValues(record);
		String got=values.get("Release");
		if(got!=null)return;
		String status=values.get("Status");
		if(status!=null&&!status.equals("Archived"))values.put("Release","["+status+"]");
		if(false)Util.printOut("RecordProxy.adjustPubSearchStatus~: values=",values.values());
	}
	static String getPdfPath(TypedNode record){
		return fieldValues(record).getString("PDF").trim();
	}
}