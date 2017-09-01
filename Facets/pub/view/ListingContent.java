package pub.view;
import static facets.util.Times.*;
import static facets.util.app.AppWatcher.*;
import static facets.util.tree.DataConstants.*;
import static pub.PubValues.*;
import static pub.view.ListingContent.*;
import static pub.view.PubFiles.*;
import static pub.view.RecordProxy.*;
import facets.util.Debug;
import facets.util.Doubles;
import facets.util.FileNode;
import facets.util.Stateful;
import facets.util.Times;
import facets.util.Titled;
import facets.util.Tracer;
import facets.util.Util;
import facets.util.app.AppWatcher;
import facets.util.tree.NodeList;
import facets.util.tree.Nodes;
import facets.util.tree.TypedNode;
import facets.util.tree.ValueNode;
import facets.util.tree.XmlSpecifier;
import facets.util.tree.Nodes.TreeRoot;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import pub.PubIssue;
import pub.PubPaths;
import pub.PubValues;
final class ListingContent extends Tracer implements Titled{
	static final XmlSpecifier fileSpec=PubFiles.Listing.newXmlSpecifier();
	private static final String KEY_FILE="fromFile";
	static final Comparator<Stateful>SORT_MODIFIED=new Comparator<Stateful>(){
		@Override
		public int compare(Stateful p,Stateful q){
			int modified=new Long(((ValueNode)q).getLong(KEY_MODIFIED)
					).compareTo(((ValueNode)p).getLong(KEY_MODIFIED));
			return modified!=0?modified:p.title().compareTo(q.title());
		}
	};
	private final static Tracer t=new Tracer(ListingContent.class){
		@Override
		protected void traceOutput(String msg){
			if(true||PubsView.dev)super.traceOutput(msg); 
		}
	};
	private static String latest;
	final ValueNode tree;
	final private Map<String,TypedNode>byTitles=new HashMap();
	private static final long MASTER_LEAD=9*minute,STALE_LAG=90*minute;
	void dispose(){
		boolean dispose=false;
		if(dispose){
			tree.setChildren();
			byTitles.clear();
			Debug.memCheck("ListingContent.dispose: "+dispose);
		}
	}
	ListingContent disposable(){
		return true?this:new ListingContent(title(),tree.children(),byTitles);
	}
	ListingContent newListing(String title,TypedNode[]listeds){
		return new ListingContent(title,listeds,byTitles);
	}
	private ListingContent(String title,TypedNode[]listeds,Map<String,TypedNode>byTitles){
		tree=new ValueNode(fileSpec.dataType,title,listeds);
		if(latest!=null)tree.put(KEY_STAMP,latest);
		this.byTitles.putAll(byTitles);
	}
	ListingContent(Object source)throws IOException{
		URL url=source instanceof URL?(URL)source:null;
		File file=url!=null?new File(url.getFile()):(File)source;
		XmlSpecifier spec=fileSpec;
		TreeRoot root=spec.newTreeRoot(spec.newRootNode(file));
		root.readFromSource(source);
		tree=(ValueNode)root.tree;
		String name=file.getName(),from=tree.get(KEY_FILE);
		if(from!=null&&!name.equals(from))throw new IllegalStateException(
				"Wrong listing from="+from);
		else tree.put(KEY_FILE,name);
		for(TypedNode listed:tree.children())byTitles.put(listed.title().trim(),listed);
	}
	static ListingContent newChecked(String fileName)throws IOException{
		final File search=new File(new File(ROOT_TECHPUBS,"pubsearch"),fileName),
				file=searchView?search:new File(DIR_USER,fileName);
		final ListingContent listing=false||(!file.exists()||(file!=search&&fileIsStale(file))
				||!fileName.startsWith(VIEW_TITLE))?null:new ListingContent(file);
		if(file==search){
			if(listing==null)throw new IllegalStateException("Missing file="+file);
			else return listing;
		}
		Times.times=true;
		Times.resetWait=1000*120;
		Times.printElapsed("ListingContent.newChecked");
		boolean logTest=false;
		File[]edits=listEdits(),stored=logTest?new File[]{}:listStored();
		final int editCount=edits.length,storeCount=stored.length;
		if(!logTest&&storeCount==0)throw new IllegalStateException(
				"No stored found in "+STORED_DIR);
		else if(listing!=null&&editCount==0)return listing;
		t.trace(": stored="+storeCount+" edits="+editCount);
		final ValueNode tree=listing!=null?listing.tree
				:new ValueNode(TYPE_XMLS,FileNode.fileTitle(file));
		final Map<PubIssue,String>paths=PubIssue.newIssuePaths(PubPaths.Record.getPaths());
		int pubAt=0;
		final Map<String,TypedNode>byTitles=listing==null?new HashMap():listing.byTitles;
		boolean forView=fileName.startsWith(VIEW_TITLE);
		int processed=listing!=null?0:processInputs(stored,byTitles,forView);
		if(edits.length>0)processed+=processInputs(edits,byTitles,true);
		if(!logTest&&processed==0){
			if(listing==null)throw new IllegalStateException(
					"Null listing for "+Debug.info(tree));
			else return listing;
		}
		final NodeList listables=new NodeList(tree,false);
		listables.clear();
		listables.addAll(byTitles.values());
		for(TypedNode pub:listables)adjustListable(pub,paths,forView);
		Collections.sort(listables,SORT_MODIFIED);
		listables.updateParent();
		int count=listables.size();
		tree.put(KEY_COUNT,count);
		tree.put(KEY_STAMP,latest=new Date().toString());
		tree.put(KEY_LIVE,LIVE);
		Util.printOut("ListingContent.newChecked: file="+file);
		if(true)fileSpec.newTreeRoot(tree).writeToSink(file);
		if(true&&!userView&&forView)try{
			Util.copyFile(file,search);			
		}catch(Exception e){
			t.trace(".newChecked: e=",e.getStackTrace());
		}
		t.trace(": count="+count+" processed="+processed+" file=",file);
		long elapsed=Times.elapsed()/1000;
		if(elapsed>10){
			Doubles.DIGITS_SF=2;
			String msg=(int)elapsed+"\t"+new Date().toGMTString();
			File log=new File(file.getParentFile(),"ListingContent"+".log");
			FileWriter writer=new FileWriter(log,true);
			writer.write(msg+"\n");
			writer.close();
			t.trace(": "+msg+" > "+log);
			Times.times=false;
		}
		return new ListingContent(file);
	}
	private static boolean fileIsStale(File file){
		return file.lastModified()+STALE_LAG<System.currentTimeMillis();
	}
	void checkStored()throws IOException{
		File[]stored=listStored();
		trace(".checkStored: stored="+stored.length+" byTitle="+byTitles.values().size());
		Map<String,File>titleFiles=new HashMap();
		XmlSpecifier zip=StoredZip.newXmlSpecifier();
		for(File file:stored){
			ValueNode pub=(zip.specifies(file)?StoredZip:StoredClear).readPub(file);
			String title=pub.title();
			if(title.equals(ValueNode.UNTITLED)){
				trace(".checkStored: setting title in file=",file.getName());
				title=Nodes.removeValue(pub,FIELD_PUBNUM).trim();
				if(title.equals(""))throw new IllegalStateException("No title for file="+file);
				else pub.setTitle(title);
				writeStored(pub,false);
				if(false)System.exit(0);
			}
			File titleFile=titleFiles.get(title);
			if(titleFile!=null){
				trace(".checkStored: duplicate title="+title+
						" in ",new File[]{titleFile,file});
				if(true)System.exit(0);
			}
			else titleFiles.put(title,file);
			if(!file.getName().startsWith(title))trace(".checkStored: title="+title+
					" does not match file="+file);
			if(byTitles.get(title)==null){
				String trimmed=title.trim();
				if(byTitles.get(trimmed)==null)trace(".checkStored: unlisted title="+title+
						" for file="+file.getName()+
						" fieldValues=",RecordProxy.fieldValues(pub).values());
				else{
					pub.setTitle(trimmed);
					File renamed=StoredZip.newXmlSpecifier().newFile(file.getParentFile(),trimmed);
					if(!file.renameTo(renamed))throw new IllegalStateException(
							"Bad renamed="+renamed);
					else trace(".checkStored: renamed file=",file);
				}
				if(true)System.exit(0);
			}
		}
		trace(".checkStored: titleFiles="+titleFiles.size()+
				" key sets equal="+titleFiles.keySet().equals(byTitles.keySet()));
	}
	private static int processInputs(File[]inputs,Map<String,TypedNode>byTitle,
			boolean forView)throws IOException{
		int processed=0;
		boolean edits=inputs.length>0&&!inputs[0].getParentFile().equals(STORED_DIR);
		XmlSpecifier zip=StoredZip.newXmlSpecifier();
		for(File input:inputs){
			boolean isZip=zip.specifies(input);
			if(false&&!isZip)t.trace(": Not zipped: ",input.getName());
			ValueNode nowPub=edits?Edit.readPub(input)
					:(isZip?StoredZip:StoredClear).readPub(input);
			String title=nowPub.title();
			ValueNode thenPub=(ValueNode)byTitle.get(title);
			if(edits){
				t.trace(": reading edit input=",input);
				if(thenPub!=null){
					t.trace(": replacing title=",title);
					if(!ownsLock(nowPub)||checkUserLock(nowPub,thenPub))continue;
				}
				else if(userView)continue;
			}
			if(hasPubNum(nowPub)||forView)byTitle.put(title,nowPub);
			if(++processed%50==0&&false)t.trace(": processed="+processed);
			if(!edits)continue;
			writeStored(nowPub,true);
			if(false)Util.copyFile(input,new File(input.getParent(),"_"+input.getName()));
			input.delete();
		}
		t.trace(": processed="+processed+" edits="+edits);
		return processed;
	}
	TypedNode getTitled(String title){
		return byTitles.get(title);
	}
	boolean isLatest(){
		return latest==null||tree.getString(KEY_STAMP).equals(latest);
	}
	@Override
	public String title(){
		return tree.title();
	}
}