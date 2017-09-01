package pub;
import static applicable.refs.TextReferences.*;
import static applicable.refs.TextReferences.RefsStrategy.*;
import static facets.util.Debug.*;
import static facets.util.Regex.*;
import static facets.util.Util.*;
import static facets.util.tree.DataConstants.*;
import static facets.util.tree.Nodes.*;
import static pub.PubIssue.*;
import static pub.PubValues.*;
import static pub.IndexerIssue.*;
import facets.util.Debug;
import facets.util.TextLines;
import facets.util.Times;
import facets.util.Tracer;
import facets.util.Util;
import facets.util.Util.FileBackup;
import facets.util.tree.DataNode;
import facets.util.tree.NodeList;
import facets.util.tree.TypedNode;
import facets.util.tree.ValueNode;
import facets.util.tree.XmlDocRoot;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import applicable.ItemProcessor;
import applicable.SimpleFilter;
import applicable.refs.NodeQueryRefs;
import applicable.refs.TextReferences;
import applicable.refs.TextReferences.Policy;
import applicable.refs.TextReferences.ReadSourceProvider;
import applicable.refs.TextReferences.RefsStrategy;
import applicable.refs.TextReferences.WordSource;
public final class PubIndexer extends Tracer{
	final static boolean optimiseRefs=true;
	public static final String TEST_TERMS=new String[]{
		"3003290 6006340",
		"page",
		"thermocouple panel display critical disconnected"
	}[0];
	static final File INDEX_ROOT=false?DIR_USER:INDEX_DIR,
		INDEX_SOURCES_FILE=new File(INDEX_ROOT,SEARCH_TITLE+"."+TYPE_INDEX_SOURCES+EXT_XML);
	private final static boolean directSources=false,encodeWords=false;
	private abstract class PubProcessor extends ItemProcessor<PubIssue>{
		PubProcessor(String title,int modTrace,boolean memChecks){
			super(title,pubs,modTrace,memChecks);
		}
		protected boolean includeItem(PubIssue pub){
			String issue=pub.issue;
			return false?issue.startsWith("?")
				:issue.hashCode()%filterMod==0&&!isIssueLocalised(issue);
		}
		protected boolean throwErrors(){
			return true;
		}
		protected boolean listErrors(){
			return false;
		}
	}
	final private static Set notIndexed=new TreeSet(); 
	private final File issuesRoot,indexRoot,textsDir,refsDir;
	private final ArrayList<PubIssue>pubs=new ArrayList();
	private final Map<PubIssue,String>issuePaths;
	private final int filterMod,modTrace;
	private PubIndexer(File issuesRoot,File indexRoot,Map<PubIssue,String>issuePaths,
			int filterMod){
		this.issuesRoot=issuesRoot;
		this.indexRoot=indexRoot;
		this.issuePaths=issuePaths;
		this.filterMod=filterMod;
	  modTrace=100/filterMod;
		textsDir=new File(indexRoot,TEXTS_DIR);
		refsDir=new File(indexRoot,REFS_DIR);
		for(PubIssue issue:issuePaths.keySet())pubs.add(issue);
		Collections.sort(pubs);
		trace("PubIndex: issuesRoot="+issuesRoot+" indexRoot=" +indexRoot+"" +
				(filterMod==1?"":(" filterMod=" +filterMod))+" pubs="+pubs.size());
	}
	private void updatePubTexts(){
		new PubProcessor("updatePubTexts",modTrace,true){
			protected boolean processItem(PubIssue pub) {
				return new IndexerIssue(pub).writePubText(pubSourceFile(pub),textsDir);
			}
		}.processIncluded();
	}
	private Map<String,TypedNode>updateIndexSources()throws IOException{
		final String REFRESH_ISSUES="";
		ValueNode sourcesRoot=new ValueNode(TYPE_INDEX_SOURCES,SEARCH_TITLE);
		if(INDEX_SOURCES_FILE.exists()){
			new FileBackup(INDEX_SOURCES_FILE).doBackup();
			new XmlDocRoot(sourcesRoot,POLICY).readFromSource(INDEX_SOURCES_FILE);		
		}
		final Map<String,TypedNode>sources=new HashMap();
		for(TypedNode source:sourcesRoot.children()){
			String issue=source.title();
			if(!REFRESH_ISSUES.contains(issue)&&!isIssueLocalised(issue))
				sources.put(issue,source);
		}
		new PubProcessor("updateIndexSources",modTrace,true){
			protected boolean processItem(PubIssue pub)throws IOException{
				return new IndexerIssue(pub).addIndexSource(sources,!directSources?refsDir:pubSourceFile(pub),
						issuePaths,encodeWords);
			}
		}.processIncluded();
		NodeList sourcesSort=new NodeList(sourcesRoot,false);
		sourcesSort.clear();
		sourcesSort.addAll(sources.values());
		Collections.sort(sourcesSort,new Comparator<TypedNode>(){
			public int compare(TypedNode p,TypedNode q){
				return p.title().compareTo(q.title());
			}
		});
		sourcesSort.updateParent();
		sourcesRoot.put(KEY_COUNT,sources.size());
		sourcesRoot.put(KEY_DATE,new Date().toString());
		new XmlDocRoot(sourcesRoot,IndexerIssue.POLICY).writeToSink(INDEX_SOURCES_FILE);
		trace("updateIndexSources: "+INDEX_SOURCES_FILE+
				"=",Util.mbs(INDEX_SOURCES_FILE.length()));
		return sources;
	}
	private File updateIndex(String indexName,final SimpleFilter<String>filter)
			throws IOException{
		notIndexed.clear();
		memCheck=true;
		memCheck("updateIndex: ");
		final Map<String,TypedNode>update=updateIndexSources();
		final List<WordSource>sources=new ArrayList();
		new PubProcessor("updateIndex",modTrace,true){
			protected boolean processItem(PubIssue pub){
				DataNode content=(DataNode)child(update.get(pub.issue),TYPE_WORDS);
				if(encodeWords)decode(content);
				String[]values=content.values();
				boolean hasText=values.length!=0;
				if(hasText)sources.add(newWordSource(pub.issue,values[0],filter));
				else PubIndexer.this.trace(".updateIndex: no text in pub=",pub);
				return true;
			}
		}.processIncluded();
		TextReferences refs=new TextReferences(SEARCH_TITLE,newIndexPolicy(sources.size()));
		if(false)refs.addSourceWords(sources.iterator());
		else for(WordSource source:sources)
			refs.addSourceWords(Collections.singletonList(source).iterator());
		refs.close();
		memCheck=true;
		memCheck("updateIndex: ");
		trace("updateIndex: index words="+refs.wordSet().size()+" "+
				"notIndexed=",notIndexed.size());
		File file=TextReferences.writeTree(refs.newTree(),indexRoot,EXT_XML_ZIP);
		trace("updateIndex: " +indexName +" MB="+Util.mbs(indexName.length()));
		memCheck("~updateIndex: ");
		return file;
	}
	@Override
	protected void traceOutput(String msg){
		System.out.println(msg);
	}
	private File pubSourceFile(PubIssue pub){
		return new File(issuesRoot,issuePaths.get(pub));
	}
	private boolean isIssueLocalised(String issue){
		return contains(issue,"\\d+[A-Z]+");
	}
	private static Policy newIndexPolicy(int sourceCount){
		final RefsStrategy strategy=!optimiseRefs?None:true?RootByStrsSq:ByStrs;
		final int threshold=strategy==RootByStrsSq?25:600;
		return new Policy(sourceCount){
			protected boolean caseSensitive(){
				return false;
			}
			protected RefsStrategy refsStrategy(){
				return strategy;
			}
			protected int refsThreshold(){
				return threshold;
			}
		};
	}
	static private WordSource newWordSource(final String issue,final String text, 
			final SimpleFilter<String> filter){
		return new WordSource(){
			protected void traceOutput(String msg){
				if(false)System.out.println(msg);
			}
			public String identity(){
				return issue;
			}
			protected String[]splitTextWords(String text){
				List<String>raw=Arrays.asList(super.splitTextWords(text));
				Collections.sort(raw);
				Collection<String>filtered=false?raw:filter.filter(raw);
				trace("splitTextWords:" +"issue="+issue+" raw="+raw.size()
						+" filtered="+filtered.size());
				return filtered.toArray(new String[]{});
			}
			protected String newText(){
				return text;
			}
		};
	}
	private void updateIssueRefs()throws IOException{
		new PubProcessor("writeTextRefs",modTrace,true){
			protected boolean processItem(PubIssue pub)throws IOException{
				return new IndexerIssue(pub).writeTextRefs(textsDir,refsDir,issuePaths);
			}
		}.processIncluded();
		final File indexRefs=new File(INDEX_ROOT,REFS_DIR);
		final List<TypedNode>refs=new ArrayList();
		new PubProcessor("updateIssueRefs",modTrace,true){
			protected boolean processItem(PubIssue pub){
				refs.add(NodeQueryRefs.newSourceTree(new File(indexRefs,pub.issue+EXT_ZIP)));
				return true;
			}
		}.processIncluded();
		NodeQueryRefs.writeRefTrees(refs,INDEX_ROOT,TITLE_REFS);
	}
	public static TextReferences newLinesIndex(TextLines lines){
		Debug.memCheck=true;
		Times.setResetWait(15*1000);
		Times.printElapsed("PubIndexer.newLinesIndex: loading "+lines.reference);
		TextReferences index=newTreeReferences(readTree(lines),
				ReadSourceProvider.DEFAULT);
		Times.printElapsed("PubIndexer.newLinesIndex: loaded");
		if(false)Debug.memCheck=false;
		Times.times=false;
		return index;
	}
	public static void doUpdates()throws IOException{
		if(false){
			printOut("PubIndexer.main: INDEX_ROOT="+INDEX_ROOT);
			TextReferences index=newLinesIndex(new TextLines(new File(INDEX_ROOT,INDEX_FILENAME)));
			Collection<WordSource>sources=index.findSources(TEST_TERMS.split("\\W"),false);
			Util.printOut("PubIndexer.main: sources=",sources);
			return;
		}
		else if(false){
			TextLines.setDefaultEncoding(true);
			TextReferences.trace=true;
			Collection<String>words=newTreeReferences(
					readTree(new TextLines(new File(runDir(),INDEX_FILENAME))),
					ReadSourceProvider.DEFAULT).wordSet();
			for(Iterator<String>i=words.iterator();i.hasNext();)
				if(!i.next().matches("[a-z]+"))i.remove();
			return;
		}
	  final boolean deleteExisting=false;
		PubIndexer indexer=new PubIndexer(new File(ROOT_PDFS),INDEX_ROOT,
				PubIssue.newIssuePaths(PubPaths.Index.getPaths()),1);//pathsIndex
		TextLines.setEncoding("UTF-8");
		if(false){
			if(deleteExisting){
				for(File file:new File(INDEX_ROOT,TEXTS_DIR).listFiles())file.delete();
				indexer.updatePubTexts();
			}
			indexer.updatePubTexts();
		}
		else if(false){//pathsIndex, refs, Issues.txtrefs
			if(deleteExisting){
				for(File file:new File(INDEX_ROOT,REFS_DIR).listFiles())file.delete();
				indexer.updateIssueRefs();
			}
			indexer.updateIssueRefs();
		}
		else if(false){//PubSearch.indexSources
			if(deleteExisting){
				if(INDEX_SOURCES_FILE.exists())INDEX_SOURCES_FILE.delete();
				indexer.updateIndexSources();
			}
			indexer.updateIndexSources();
		}
		else if(false){//PubSearch.txtrefs.xml.zip->PubSearch.index.xml.zip
			final String indexName=false?INDEX_SHORT_FILENAME:INDEX_FILENAME;
			TextReferences.trace=false;
			indexer.updateIndex(indexName,new SimpleFilter<String>(){
				boolean shortIndex=indexName==INDEX_SHORT_FILENAME;
				protected String newExceptionResult(Exception e){
					throw new RuntimeException(e);
				}
				protected boolean passes(String word){
					if(word.length()<20
						&&!word.matches("\\d{1,4}\\D*")
						&&(!shortIndex||!word.matches("[a-z]{5,}")))return true;
					notIndexed.add(word);
					return false;
				}
			});
		}
		else getRefsInstance();
	}
	static NodeQueryRefs refs;
	public static NodeQueryRefs<PubIssue>getRefsInstance(){
		File indexRoot=PubIndexer.INDEX_ROOT;
		try{
			return refs==null?refs=new NodeQueryRefs<PubIssue>(TITLE_REFS,indexRoot,
					PubIndexer.newLinesIndex(new TextLines(new File(indexRoot,INDEX_FILENAME)))){
				@Override
				protected PubIssue newPassed(String id,Set<WordSource>finds){
					PubIssue issue=new PubIssue(id);
					issue.addPageFinds(finds);
					return issue;
				}
			}
			:refs;
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
}
