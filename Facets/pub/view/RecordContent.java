package pub.view;
import static applicable.FileTableView.*;
import static facets.util.HtmlBuilder.*;
import static facets.util.Regex.*;
import static facets.util.tree.DataConstants.*;
import static facets.util.tree.Nodes.*;
import static facets.util.tree.ValueNode.*;
import static pub.PubIssue.*;
import static pub.PubValues.*;
import static pub.view.PubFiles.*;
import static pub.view.RecordProxy.*;
import facets.util.Debug;
import facets.util.HtmlBuilder;
import facets.util.Objects;
import facets.util.Regex;
import facets.util.Stateful;
import facets.util.Strings;
import facets.util.Times;
import facets.util.Titled;
import facets.util.Tracer;
import facets.util.Util;
import facets.util.tree.DataConstants;
import facets.util.tree.DataNode;
import facets.util.tree.NodeList;
import facets.util.tree.Nodes;
import facets.util.tree.TypedNode;
import facets.util.tree.ValueNode;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import pdft.PdfCore;
import pub.PubIssue;
import pub.PubPaths;
import applicable.field.ValueField;
final class RecordContent extends Tracer implements Titled{
	static final ValueNode NO_LINKS=new ValueNode(TYPE_DOC,"Empty",new Object[]{
			new ValueNode(TYPE_FIELDS,"Empty",new String[]{FIELD_PUBNUM+"=[No links]"})});
	public static final String TYPE_SOURCES="Sources",TYPE_TREES="Trees",
			EMPTY_NOTES="[No notes]";
	static final File SOURCES_DIR=new File(ROOT_TECHPUBS,"archive");
	private static final int STATE_AT=0,STORE_AT=1,LISTED_COPY_AT=2;
	final DataNode viewable;
	private final DataNode listed;
	private final ListingContent listing;
	private ListingContent linkListing;
	private Stateful checkNormalised;
	void dispose(){
		listing.dispose();
		if(linkListing!=null)linkListing.dispose();
	}
	RecordContent(DataNode listed,ListingContent listing){
		this.listed=listed;
		this.listing=listing;
		viewable=guaranteedChild(listed,"Viewable",listed.title());
		if(false)Times.printElapsed("RecordContent: viewable="+viewable);
		if(listing==null||viewable.contents().length==0)buildViewable();
		if(false)Times.printElapsed("RecordContent~: viewable="+viewable);
	}
	private void buildViewable(){
		viewable.setParent(null);
		viewable.setContents(new Object[]{});
		ValueNode copyListed=(ValueNode)listed.copyState(),stored;
		copyListed.setChildren(fieldValues(copyListed));
		stored=tryReadStored();
		if(ListingContenter.checkFieldsOnSort){
			ValueNode fieldsThen=(ValueNode)fieldValues(listed).copyState();
			mergeContents(fieldValues(listed),fieldValues(stored).contents());
			ValueNode fieldsNow=(ValueNode)fieldValues(listed).copyState();
			removeValue(fieldsNow,"LinksText");
			sortValues(fieldsThen);
			sortValues(fieldsNow);
			if(!fieldsNow.stateEquals(fieldsThen)){
				trace(".buildViewable: fieldsThen=",fieldsThen.contents());
				trace(".buildViewable: fieldsNow=",fieldsNow.contents());
			}
		}
		stored.put(KEY_USER,userName);
		stored.put(KEY_STAMP,new Date(stored.getLong(KEY_MODIFIED)).toString());
		stored.setChildren(guaranteedChild(listing==null?listed:stored,TYPE_FIELDS),
				guaranteedChild(stored,TYPE_TEXTS),guaranteedChild(stored,TYPE_LINKS),
				guaranteedChild(stored,TYPE_ATTACHMENTS));
		ValueField.addNullMarkers(fieldValues(stored),PubFields.STATE_KEYS.split(","));
		ValueNode state=(ValueNode)stored.copyState();
		if(listing==null)Util.printOut("RecordContent.buildViewable: ",
				fieldValues(stored).get("Order"));
		guaranteedChild(viewable,TYPE_TREES).setContents(new Object[]{state,stored,copyListed});
		buildRenderTrees(viewable,state);
	}
	void saveState(){
		ValueNode trees[]=trees(),state=trees[STATE_AT];
		long time=System.currentTimeMillis();
		state.put(KEY_MODIFIED,time);
		state.put(KEY_STAMP,new Date(time).toString());
		adjustReleaseFormat(state);
		trees[STORE_AT].setState(state);
		ValueNode save=(ValueNode)state.copyState();
		removeNullValues(fieldValues(save));
		if(listing==null)Util.printOut("RecordContent.saveState: ",fieldValues(save).get("Order"));
		else try{
			PubFiles.writeEdit(save);
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}
	private ValueNode tryReadStored(){
		try{
			return readTitleStored(listed.title());
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}
	private static void buildRenderTrees(DataNode viewable,ValueNode state){
		NodeList attachments=new NodeList(guaranteedChild(viewable,TYPE_ATTACHMENTS),false),
			sources=new NodeList(guaranteedChild(viewable,TYPE_SOURCES),false);
		attachments.clear();sources.clear();
		TypedNode[]attached=guaranteedChild(state,TYPE_ATTACHMENTS).children();
		final File attachDir=new File(ATTACHMENTS_DIR,state.title().trim());
		if(attached.length>0){
			if(!attachDir.exists()||listNetworkFiles(attachDir).length!=attached.length){
				if(!attachDir.exists()&&!attachDir.mkdir())throw new IllegalStateException(
							"Bad attachDir="+attachDir);
				else Util.printOut("RecordContent: attachDir=",attachDir);
				for(TypedNode details:attached)try{
					File then=new File(ATTACHMENTS_DIR,details.title().trim());
					if(then.exists())Util.moveFile(then,attachDir);
					else Util.printOut("RecordContent: missing then=",then);
				}catch(IOException e){
					throw new RuntimeException(e.getMessage()+" attachDir="+attachDir);
				}
				Util.printOut("RecordContent: attachDir=",listNetworkFiles(attachDir));
			}
		}
		if(attachDir.exists())for(File file:listNetworkFiles(attachDir))
				attachments.add(newPathNode(file));
		if(attachments.isEmpty())attachments.add(newEmptyNode("attachments"));
		ValueNode fields=fieldValues(state);
		for(String path:PubPaths.Archive.getPaths()){
			String pubNum=fields.getString(FIELD_PUBNUM).trim(),
					order=fields.getString("Order").trim();
			if(!pubNum.equals("")&&path.contains(pubNum)
					||!order.equals("")&&path.contains(order))
				sources.add(newPathNode(new File(SOURCES_DIR,path)));
		}
		if(sources.isEmpty())sources.add(newEmptyNode("sources"));
		attachments.updateParent();
		sources.updateParent();
	}
	void normaliseState(){
		TypedNode state=state();
		checkNormalised=state.copyState();
		trimFields(state,PubFields.STATE_KEYS.split(","));
		checkNormalised("fields");
		ValueNode fields=fieldValues(state);
		NodeList links=new NodeList(guaranteedChild((DataNode)state,TYPE_LINKS),true);
		TypedNode texts=child(state,TYPE_TEXTS),linksText=descendantTitled(texts,TYPE_LINKS);
		final String linksKey="LinksText";
		if(fields.get(linksKey).equals(NULL_MARKER)&&linksText!=null
				&&linksText.values().length>0){
			String text=(String)linksText.values()[0];
			for(TypedNode link:links)text+="["+link.title()+"]";
			fields.put(linksKey,text);
			checkNormalised("linksText="+text);
		}
		String blank=TypedNode.UNTITLED;
		texts.setTitle(blank);
		checkNormalised("texts count");
		for(String find:Objects.join(String.class,finds(newNotesContent(),HTTP+"\\w+"),
				finds(fieldValues(state).getString(linksKey),"\\[\\w+\\]"))){
			if(find.equals(NULL_MARKER))continue;
			String title=replaceAll(find,HTTP,"","\\[|\\]","");
			TypedNode listed=listing.getTitled(title);
			if(listed!=null&&links.titled(title)==null)links.add(new DataNode(TYPE_LINK,title));
		}
		checkNormalised("links");
		int linksCount=links.size();
		links.parent.setTitle(linksCount>0?(linksCount+""):blank);
		checkNormalised("links count");
		String path=getPdfPath(listed);
		if(path.equals(PubIssue.PDF_NONE)||!NULL_MARKER.equals(fields.get("PageCount")))return;
		int pages=0;
		try{
			pages=new PdfCore(new File(ROOT_PDFS,path)).countPages();
		}catch(IOException e){
			throw new RuntimeException(e);
		}
		if(pages==0)throw new IllegalStateException("Bad page count in "+Debug.info(this));
		checkNormalised=state.copyState();
		fields.put("PageCount",pages);
		checkNormalised("page count");
	}
	private void checkNormalised(String msgTail){
		ValueNode state=state();
		if(checkNormalised==null)throw new IllegalStateException(
				"Null checkNormalised in "+Debug.info(this));
		else if(checkNormalised.stateEquals(state))return;
		Util.printOut(state.title()+": normalised "+msgTail);
		checkNormalised=state.copyState();
	}
	boolean stateChanged(){
		ValueNode[]trees=trees();
		if(trees.length==0)return false;
		TypedNode state=trees[STATE_AT];
		ValueNode stateFields=fieldValues(state),listedFields=fieldValues(listed);
		ValueNode count=(ValueNode)stateFields.copyState();
		removeNullValues(count);
		stateFields.setTitle(""+count.values().length);
		mergeContents(listedFields,stateFields.contents());
		return !state.stateEquals(trees[STORE_AT]);
	}
	String newNotesContent(){
		DataNode texts=guaranteedChild(state(),TYPE_TEXTS),
			body=guaranteedChild(texts,TYPE_TEXT,TITLE_BODY),
			encoded=guaranteedChild(texts,PubFiles.TYPE_ENCODED,body.title());
		if(encoded.contents().length==0){
			if(body.contents().length==0)body.setContents(new Object[]{EMPTY_NOTES});
			String[]lines=body.values();
			if(Strings.linesString(lines).trim().equals(""))lines=new String[]{EMPTY_NOTES};
			StringBuilder coder=new StringBuilder();
			for(String line:lines)if(!line.trim().equals(""))coder.append("<p>"+line+"\n");
			storeEncodedText(encoded,
					coder.toString().replaceAll("([a-z,])\n<p>([a-z])","$1 $2"));
		}
		String code=restoreEncodedText(encoded);
		if(code==null)throw new IllegalStateException(
				"Can't restore code in "+Debug.info(this));
		else if(code.trim().equals(""))code=EMPTY_NOTES;
		else body.setContents(HtmlBuilder.codeToText(code).split("\n"));
		return code;
	}
	void setField(ValueField field,String text,boolean save){
		field.putInputValue(fieldValues(state()),text);
		stateChanged();
		if(save)saveState();
	}
	void setUserLock(){
		try{
			writeEdit((ValueNode)trees()[STATE_AT].copyState());
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}
	void resetUserLock(){
		ValueNode trees[]=trees();
		PubFiles.checkUserLock(trees[STATE_AT],trees[LISTED_COPY_AT]);
	}
	private ValueNode[]trees(){
		return isDisposed()?new ValueNode[]{} 
				:Objects.newTyped(ValueNode.class,child(viewable,TYPE_TREES).children());
	}
	boolean isDisposed(){
		return viewable.children().length==0;
	}
	ValueNode state(){
		return isDisposed()?new ValueNode(TYPE_TREES,"Disposed")
				:trees()[STATE_AT];
	}
	private static void adjustReleaseFormat(ValueNode source){
		ValueNode values=fieldValues(source);
		String key="Release",release=values.get(key);
		if(release.equals(ValueNode.NULL_MARKER)||release.matches("\\d+/\\d+/\\d{4}"))return;
		values.put(key,Regex.replaceAll(release,
//				"(\\d+/\\d+/)(\\d{2})\\b.*","$120$2",s
				" 00:00:00",""));
		if(false)Util.printOut("RecordContent.adjustReleaseFormat: Release="+values.get(key)+
				" source=",source.title());
	}
	void resetState(){
		fieldValues(listed).setContents(fieldValues(trees()[LISTED_COPY_AT]).contents());
		buildViewable();
	}
	boolean hasValidStateTree(){
		ValueNode state=trees()[STATE_AT];
		for(String type:new String[]{TYPE_TEXTS,TYPE_LINKS,TYPE_ATTACHMENTS,TYPE_FIELDS})
			if(child(state,type)==null)return false;
		return true;
	}
	ListingContent getLinkListing(){
		if(linkListing!=null)return linkListing;
		List<TypedNode>linked=new ArrayList();
		if(hasPubNum(listed))linked.add((TypedNode)listed.copyState());
		for(TypedNode link:guaranteedChild(state(),TYPE_LINKS).children()){
			String title=link.title();
			TypedNode listed=listing.getTitled(title);
			if(listed!=null&&!linked.contains(listed))linked.add((TypedNode)listed.copyState());
		}
		if(linked.isEmpty())linked.add(NO_LINKS);
		return linkListing=listing.newListing("LinkListing."+listed.title(),
				linked.toArray(new TypedNode[]{}));
	}
	RecordContent newLinkedContent(String linked){
		TypedNode listed=listing.getTitled(linked);
		if(listed==null){
			if(true)Util.printOut("RecordContent: no listed for linked=",linked);
			else throw new IllegalStateException("Null listed for "+linked);
		}
		return listed==null?null:new RecordContent((DataNode)listed,listing.disposable());
	}
	@Override
	public String title(){
		return listed.title();
	}
}