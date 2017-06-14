package applicable.refs;
import static applicable.refs.StringReferences.*;
import static applicable.refs.TextReferences.RefsStrategy.*;
import static facets.util.Debug.*;
import static facets.util.Times.*;
import static facets.util.Util.*;
import static facets.util.tree.Nodes.*;
import static java.lang.Math.*;
import static java.util.Collections.*;
import facets.util.Debug;
import facets.util.FileNode;
import facets.util.Identified;
import facets.util.Objects;
import facets.util.TextLines;
import facets.util.Times;
import facets.util.Titled;
import facets.util.Tracer;
import facets.util.TracerInput;
import facets.util.Util;
import facets.util.tree.DataConstants;
import facets.util.tree.DataNode;
import facets.util.tree.Nodes;
import facets.util.tree.TypedNode;
import facets.util.tree.ValueNode;
import facets.util.tree.XmlDocRoot;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import applicable.TextQuery;
import applicable.refs.StringReferences.RefsFilter;
import applicable.refs.TextReferences.WordSource;
final public class TextReferences{
	public static abstract class WordSource extends Tracer implements Identified,
		Serializable,Comparable<WordSource>{
		private transient String text,words[];
		final Set<String>wordSet(){
			SortedSet<String>words=new TreeSet();
			for(String word:getWords())
				if(!word.trim().equals(""))words.add(word);
			return words;
		}
		public abstract String identity();
		private int[]getWordAts(){
			String text=getText(),words[]=getWords();
			int ats[]=new int[words.length],textAt=0;
			for(int at=0;at<ats.length&&textAt<text.length();at++){
				String word=words[at];
				ats[at]=text.indexOf(word,textAt);
				textAt+=words[at].length();
				while(textAt<text.length()&&!isWordChar(text,textAt))textAt++;
			}
			return ats;
		}
		private String[]getWords(){
			return words==null?words=splitTextWords(getText()):words;
		}
		final protected String getText(){
			return text==null?text=newText():text;
		}
		protected abstract String newText();
		protected String[]splitTextWords(String text){
			return text.split("\\W+");
		}
		protected Set<WordSource>subFindSources(String find){
			return singleton(this);
		}
		protected boolean isWordChar(String text,int at){
			char charVal=text.charAt(at);
			String strVal=String.valueOf(charVal);
			boolean is=true?Character.isLetterOrDigit(charVal)
			 :true?strVal!=" ":false?!strVal.equals(" "):strVal.matches("\\w");
			return is;
		}
		final public int compareTo(WordSource o){
			return identity().compareTo(o.identity());
		}
		public String toString(){
			return identity().toString();
		}
		public boolean equals(Object o){
			return identity().equals(((WordSource)o).identity());
		}
		public final void setTitle(String title){
		}
	}
	public interface ReadSourceProvider extends Serializable{
		ReadSourceProvider DEFAULT=new ReadSourceProvider(){
			public WordSource newReadSource(final Object identity){
				return new WordSource(){
					protected String newText(){
						throw new RuntimeException("Not implemented in "+Debug.info(this));
					}
					public String identity(){
						return(String)identity;
					}
				};
			}
		};
		WordSource newReadSource(Object identity);
	}
	public enum RefsStrategy{None,ByStrs,RootByStrsSq}
	public static class Policy{
		public final int sourcesMax;
		public Policy(int sourcesMax){
			if(sourcesMax==0)throw new IllegalArgumentException(
					"Nil sourcesMax in "+Debug.info(this));
			this.sourcesMax=sourcesMax;
		}
		protected boolean caseSensitive(){
			return true;
		}
		RefsFilter refsFilter(){
			RefsStrategy s=refsStrategy();
			return s==None?RefsFilter.NONE:new RefsFilter(s,refsThreshold());
		}
		protected RefsStrategy refsStrategy(){
			return None;
		}
		protected int refsThreshold(){
			return 0;
		}
		protected Policy newSubPolicy(int size,final RefsStrategy s,final int threshold){
			final Policy superPolicy=this;
			return new Policy(sourcesMax){
				final protected boolean caseSensitive(){
					return superPolicy.caseSensitive();
				}
				protected RefsStrategy refsStrategy(){
					return s;
				}
				protected int refsThreshold(){
					return threshold;
				}
			};
		}
		protected String newWordStr(String word){
			return(caseSensitive()?word:word.toLowerCase()).trim();
		}
	}
	final public static String TYPE="txtrefs",EXT="."+TYPE,
		EXT_ZIP=EXT+DataConstants.EXT_XML_ZIP;
	private final StringReferences<WordSource>refs;
	private final String title;
	private final Policy policy;
	public TextReferences(String title,Policy policy){
		this.policy=policy;
		this.title=title;
		refs=new StringReferences(policy.sourcesMax);
	}
	public void addSourceWords(Iterator<WordSource>sources){
		while(sources.hasNext()){
			WordSource source=sources.next();
			Collection<String>words=source.wordSet();
			for(String word:words)
				refs.addSourceReference(policy.newWordStr(word),source);
			if(false&&!StringReferences.miniTrace)printOut(
					"TextReferences.addSourceWords: source="+source+
					" words=",words.size());
		}
	}
	public void close(){
		refs.close(policy);
	}
	public static File writeTree(DataNode tree,File dir,String extXml){
		if(tree.type()!=TYPE)throw new IllegalArgumentException(
				"Type should be " +TYPE+" in "+tree);
		File file=new File(dir,tree.title()+"."+TYPE+extXml);
		new XmlDocRoot(tree,StringReferences.XML_POLICY).writeToSink(file);
		printElapsed("TextReferences.writeTree: ");
		return file;
	}
	public static ValueNode readTree(TextLines lines){
		ValueNode tree=new ValueNode(TYPE,lines.getFileName());
		if(false)Util.printOut("TextReferences.readTree: lines="+lines.reference);
		new XmlDocRoot(tree,StringReferences.XML_POLICY).readFromSource(lines);
		return tree;
	}
	public ValueNode newTree(){
		ValueNode tree=new ValueNode(TYPE,title);
		tree.put(StringReferences.KEY_COUNT,policy.sourcesMax);
		tree.put(KEY_CASE,policy.caseSensitive());
		tree.put(KEY_DATE,new Date().toString());
		boolean verbose=false;
		if(verbose)tree.put(KEY_VERBOSE,true);
		refs.buildTree(tree);
		if(false)Util.printOut("TextReferences.writeToFile: verbose="+verbose+
				" tree=" +Nodes.descendants(tree).length);
		return tree;
	}
	public static TextReferences newTreeReferences(ValueNode tree,ReadSourceProvider rsp){
		if(false)Util.printOut("TextReferences.newTreeReferences: tree=" +
				(Util.sfs(true?Nodes.descendants(tree).length:Nodes.treeString(tree).length())));
		final boolean caseSensitive=tree.getBoolean(KEY_CASE);
		TextReferences refs=new TextReferences(tree.title(),
				new Policy(tree.getInt(KEY_COUNT)){
			protected boolean caseSensitive(){
				return caseSensitive;
			}
		});
		refs.refs.buildFromTree(tree,rsp);
		return refs;
	}
	public Set<WordSource>wordSources(String word){
		return refs.getReferenceSources(policy.newWordStr(word));
	}
	public static Collection<String>getFileWordSet(File file){
		DataNode tree=new DataNode(TYPE,file.getName());
		new XmlDocRoot(tree,StringReferences.XML_POLICY).readFromSource(file);
		return StringReferences.getTreeStrings(tree);
	}
	public Collection<String>wordSet(){
		return refs.wordSet();
	}
	private static class FindContext implements Titled,Comparable<FindContext>{
		public final int findAt;
		public final String contextWords;
		public FindContext(int findAt,String contextWords){
			this.findAt=findAt;
			this.contextWords=contextWords;
		}
		public String title(){
			return (findAt<0?"":("\tat " +findAt+": "))+contextWords;
		}
		public int compareTo(FindContext that){
			return findAt-that.findAt;
		}
	}
	private static abstract class FileWordSource extends WordSource{
		private final String path;
		private transient Set<WordSource>subSources;
		public FileWordSource(String path){
			this.path=path;
		}
		public String identity(){
			return path;
		}
		protected Set<WordSource>subFindSources(String find){
			return getSubSources();
		};
		protected final Set<WordSource>getSubSources(){
			if(subSources!=null)return subSources;
			FileNode node=new FileNode(new File(path));
			node.checkReadContents();
			return subSources=newSubSources(node);
		}
		protected abstract Set<WordSource>newSubSources(FileNode node);
		protected String newText(){
			StringBuilder text=new StringBuilder();
			for(WordSource sub:getSubSources())text.append(sub.getText()+"\n");
			return text.toString();
		}
		protected FindContext newContextTitled(int findAt,String contextWords){
			return new FindContext(findAt, contextWords);
		}
		final List<FindContext>getFindContexts(String find,int contextWords, 
				boolean caseSensitive){
			List<Integer>findAts=new ArrayList();{
				String text=getText();
				if(!caseSensitive){
					text=text.toLowerCase();
					find=find.toLowerCase();
				}
				int findAt=text.length();
				while((findAt=text.substring(0,max(0,findAt)).lastIndexOf(find))>=0)
					findAts.add(findAt);
				reverse(findAts);
			}
			List<FindContext>contexts=new ArrayList();
			for(Integer findAt:findAts){
				String text=getText();
				int skips=contextWords/2+1,start=findAt,stop=findAt,wordStop=findAt;
				while(wordStop<findAt+find.length()&&isWordChar(text,wordStop))wordStop++;
				for(int skip=0;skip<skips;skip++){
					while(start>0&&!isWordChar(text,start))start--;
					while(start>0&&isWordChar(text,start))start--;
				}
				int length=text.length();
				for(int skip=0;skip<skips;skip++){
					while(stop<length&&isWordChar(text,stop))stop++;
					while(stop<length&&!isWordChar(text,stop))stop++;
				}
				String words=text.substring(start,findAt)+"'"+
					text.substring(findAt,wordStop)+
					"'"+text.substring(wordStop,stop);
				contexts.add(newContextTitled(findAt,words));
			}
			return contexts;
		}
	}
	private static final class TextLineContext extends FindContext{
		private final int lineAt;
		public TextLineContext(int lineAt,int findAt,String contextWords){
			super(findAt,contextWords);
			this.lineAt=lineAt;
		}
		public String title(){
			return "\tat " +(lineAt+1)+" : "+(findAt+1)+
			" "+contextWords.replaceAll("\\s+"," ");
		}
		public int compareTo(FindContext that){
			int line=!(that instanceof TextLineContext)?0
				:lineAt-((TextLineContext)that).lineAt;
			return line!=0?line:super.compareTo(that);
		}
	}
	private List<FindContext>findContexts(String find){
		List<WordSource>wordSources=new ArrayList(wordSources(find));
		sort(wordSources);
		List<FindContext>contexts=new ArrayList();
		printOut("TextReferences.findContexts: find='"+find +
				"' foundSources=",wordSources.size());
		if(true)return contexts;
		else if(true||miniTrace){
			int contextWords=5;
			for(WordSource wordSource:wordSources){
				contexts.add(new FindContext(-1," >>>>> "+wordSource.identity()));
				List<FindContext>subContexts=new ArrayList();
				for(WordSource subSource:wordSource.subFindSources(find))
					subContexts.addAll(((FileWordSource)subSource).getFindContexts(
							find,contextWords,policy.caseSensitive()));
				sort(subContexts);
				contexts.addAll(subContexts);
			}
		}
		return contexts;
	}
	public Set<WordSource>findQuerySources(TextQuery query){
		Set<WordSource>sources=new HashSet();
		String[]words=query.text.split("\\W");
		for(String word:words)sources.addAll(wordSources(word));
		for(String word:words)sources.retainAll(wordSources(word));
		return sources;
	}
	public Collection<WordSource>findSources(String[]finds,boolean any){
		if(false)Util.printOut("PubSearch.indexFindSources: finds=" +Objects.toString(finds)+
				" matchAny=" +any);
		Set<WordSource>all=new HashSet();
		final int showMax=5;
		if(!any)all.addAll(wordSources(""));
		for(String find:finds){
			Collection<WordSource>sources=wordSources(find);
			if(any)all.addAll(sources);
			else all.retainAll(sources);
			int count=sources.size();
			if(false)Util.printOut("PubSearch.indexFindSources: find='" +find+
					"' sources="+(count>showMax?count:"\n"+Objects.toLines(sources.toArray())));
		}
		List<WordSource>sort=new ArrayList(all);
		Collections.sort(sort,new Comparator<Identified>(){
			public int compare(Identified p,Identified q){
				return ((String)p.identity()).compareTo((String)q.identity());
			}
		});
		int count=sort.size();
		return sort;
	}
	static int miniSources;
	final private static String miniTexts[]={
		"The fat cat sat sat on the flat mat ",
		"The fat cow cow jumped over the Moon",
		"The quick brow fox jumped over the lazy cow on the mat",
		"a.c",
		"a.b.b",
		"c.b.a.c",
	},
	miniFind="t";
	public static boolean trace=false;
	private static WordSource newTraceMiniSource(final int textAt){
		return new WordSource(){
			private final String identity="Source"+miniSources++;
			public String identity(){
				return identity;
			}
			public String toString(){
				return miniTexts[textAt]+" > "+Objects.toString(wordSet().toArray());
			}
			protected String newText(){
				return miniTexts[textAt];
			}
		};
	}
	private static WordSource newTraceTextFileSource(final String path,final Policy p){
		return new FileWordSource(path){
			int lines=0;
			private transient TextReferences textRefs;
			private transient Policy policy=p;
			protected Set<WordSource>newSubSources(FileNode node){
				Set<WordSource>sources=new HashSet();
				final String fileIdentity=identity();
				for(final String str:node.values()){
					sources.add(new WordSource(){
						private final int lineAt=lines++;
						protected String newText(){
							return str.replaceAll("\t","  ");
						}
						public String identity(){
							return fileIdentity+":line="+lineAt;
						}
						protected FindContext newContextTitled(int findAt,String contextWords){
							return new TextLineContext(lineAt,findAt,contextWords);
						}
					});
				}
				textRefs=new TextReferences(path,policy.newSubPolicy(sources.size(),
						ByStrs,12));
				return sources;
			}
			protected Set<WordSource>subFindSources(String find){
				if(!textRefs.refs.closed){
					textRefs.addSourceWords(getSubSources().iterator());
					textRefs.close();
				}
				return textRefs.wordSources(find);
			}
		};
	}
	private static ReadSourceProvider newTraceProvider(final Policy p){
		return new ReadSourceProvider(){
			public WordSource newReadSource(Object identity){
				return!miniTrace?newTraceTextFileSource((String)identity,p)
					:newTraceMiniSource((Integer)identity);
			}
		};
	}
	private static void traceInSources(List<WordSource>sources,Policy p,String...finds){
		TextLines.setDefaultEncoding(true);
		TextReferences tr=new TextReferences(miniTrace?"Mini":"Maxi",p);
		printElapsed("TextReferences.traceInSources: adding sources="+sources.size());
		tr.addSourceWords(sources.iterator());
		memCheck("Added sources: ");
		printElapsed("TextReferences.traceInSources: closing filter="+p.refsFilter());
		tr.close();
		memCheck("Closed: ");
		if(true){
			File file=writeTree(tr.newTree(),Util.runDir(),".xml");
			if(false){
				memCheck("Written tree: ");
				memCheck("New: ");
				tr=TextReferences.newTreeReferences(readTree(new TextLines(file)),newTraceProvider(p));
				memCheck("Read tree: ");
				writeTree(tr.newTree(),Util.runDir(),".xml");
			}
		}
		for(String find:finds)
			for(FindContext context:tr.findContexts(find))
				if(false)printOut(context.title());
		Times.printElapsed("TextReferences.~traceInSources: ");
	}
	static void main(String[]args){
			if(miniTrace){
				final int useTexts=microLimit;
				for(int sourceCount:new int[]{
						true?1:false?BYTES_LIMIT:microLimit,
	//					BYTES_LIMIT+useTexts,
	//					SHORTS_LIMIT+useTexts
					}){
					printOut("TextReferences.main: sourcesMax="+sourceCount);
					List<WordSource>sources=new ArrayList();
					for(int sourceAt=0;sourceAt<sourceCount;sourceAt++){
						final int textAt=sourceAt%useTexts;
						sources.add(newTraceMiniSource(textAt));
					}
					traceInSources(sources,new Policy(sourceCount){
						protected boolean caseSensitive(){
							return false;
						}
					});
				}
			}
			else{
				printOut("TextReferences.main: ");
				memCheck=false;
				FileNode.checkReadDirs=true;
				final FileNode tree=new FileNode(new File("."));
				tree.checkReadContents();
				final List<WordSource>sources=new ArrayList();
				memCheck("Built sources: ");
				class MaxiTrace{
					void trace(Policy p){
						for(TypedNode source:descendantsTyped(tree,"java"))
							sources.add(newTraceTextFileSource(
									((FileNode)source).file.getAbsolutePath(
											).replace("C:\\Eclipse\\workspace\\Facet\\.\\",""
													).replace("\\","/"),new Policy(-1)));
						Times.resetWait=20*1000;
						traceInSources(sources,p,
							"TextLineAvatarPolicies"//boolean,int,AvatarPolicies
						);
						sources.clear();
						memCheck("Cleared sources: ");
					}
				}
				int sourcesMax=BYTES_LIMIT+1;
				final boolean useSquares=true;
				final RefsStrategy strategy=useSquares?
						RootByStrsSq
						:ByStrs;
				if(false)new MaxiTrace().trace(new Policy(sourcesMax){
					protected RefsFilter refsFilter(){
						return new RefsFilter(strategy,useSquares?7:12);
					}
				});
				else for(final RefsFilter filter:false?new RefsFilter[]{
						RefsFilter.NONE,
						new RefsFilter(strategy,useSquares?6:10),
						new RefsFilter(strategy,useSquares?7:12),
						new RefsFilter(strategy,useSquares?8:14),
					}
				:new RefsFilter[]{
							RefsFilter.NONE,
							new RefsFilter(ByStrs,10),
							new RefsFilter(RootByStrsSq,7),
						}){
						Times.resetWait=0;
						TracerInput.pause("");
						printElapsed("MaxiTrace.run: minWeighting=" +filter);
						new MaxiTrace().trace(new Policy(sourcesMax){
							protected RefsFilter refsFilter(){
								return filter;
							}
						});
					}
				printOut("TextReferences.~main: ");
			}
		}
}
