package pub;
import static facets.util.tree.DataConstants.*;
import static pub.PubIssue.*;
import static pub.PubValues.*;
import facets.util.Bytes;
import facets.util.Debug;
import facets.util.Objects;
import facets.util.TextLines;
import facets.util.Tracer;
import facets.util.Util;
import facets.util.tree.DataNode;
import facets.util.tree.NodeList;
import facets.util.tree.Nodes;
import facets.util.tree.TypedNode;
import facets.util.tree.ValueNode;
import facets.util.tree.XmlDocRoot;
import facets.util.tree.XmlPolicy;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import pdft.tool.PageTexts;
import applicable.refs.TextReferences;
import applicable.refs.TextReferences.WordSource;
final class IndexerIssue extends Tracer{
	static final String TYPE_WORDS="words",
			TYPE_PDF="pubPdf",TYPE_PUBTEXT="pubText",TYPE_PAGETEXT="pageText",
			TYPE_INDEX_SOURCE="indexSource",TITLE_REFS="Issues",
			TYPE_INDEX_SOURCES="indexSources",
			PAIR_EQUALS="=",KEY_PAGE="page",KEY_PDF="pdf",KEY_DATE="date",
			KEY_TITLE="title",KEY_ISSUE="issue",KEY_ISSUES="issues";
	static final XmlPolicy POLICY=new XmlPolicy(){
		protected boolean handleReadExceptions(){
			return false;
		};
		@Override
		protected boolean treeAsXmlRoot(){
			return true;
		}
		@Override
		public ValueNode getTitleAttributeNames(){
			return newTitleAttributeNames(KEY_ISSUE,new String[]{
				TYPE_PDFS+PAIR_EQUALS+KEY_DATE,
				TYPE_PAGETEXT+PAIR_EQUALS+KEY_PAGE,
				TYPE_INDEX_SOURCES+PAIR_EQUALS+KEY_TITLE,
				TYPE_INDEX+PAIR_EQUALS+KEY_TITLE,
				TYPE_WORDS+PAIR_EQUALS+KEY_COUNT,
			});
		}
	};
	private static final class RefsWordSource extends WordSource{
		private final transient TypedNode page;
		private RefsWordSource(TypedNode page){
			this.page=page;
		}
		@Override
		public String identity(){
			return KEY_PAGE+page.title();
		}
		@Override
		protected String newText(){
			Object[]contents=page.contents();
			return contents.length==0?"":(String)contents[0];
		}
	}
	private final static boolean debug=false;
	private final String issue;
	private final boolean isOcrText=false;
	IndexerIssue(PubIssue pub){
		this.issue=pub.issue;
	}
	boolean addIndexSource(Map<String,TypedNode>sources,File src,
			Map<PubIssue,String>paths,boolean encodeWords)throws IOException{
		if(sources.get(issue)!=null&&(true||!isOcrText))return false;
		Set<String>words=new TreeSet();
		if(src.isDirectory()){
			File file=new File(src,issue+"."+TextReferences.TYPE+EXT_XML_ZIP);
			if(!file.exists())writeTextRefs(src,src,paths);
			words.addAll(TextReferences.getFileWordSet(file));
		}else for(String word:(isOcrText?new OcrTexts(src).readAllPages()
				:new PageTexts(src).readAllPageTexts().split("\\W+")))
		 words.add(word.toLowerCase());
		int count=words.size();
		trace("addIndexSource: issue=" +issue+" words=",count);
		DataNode content=new DataNode(TYPE_WORDS,""+count,
				new Object[]{Objects.toString(words.toArray(),", ")});
		if(encodeWords&&count>0)Nodes.encode(content,0);
		sources.put(issue,new DataNode(TYPE_INDEX_SOURCE,issue,new Object[]{content}));
		return true;
	}
	boolean writePubText(File pdf,File textsDir){
		File text=new File(textsDir,issue+"."+TYPE_PUBTEXT+EXT_XML_ZIP);
		if(text.exists())return false;
		ValueNode pagesRoot=newFilledPagesRoot(pdf);
		new XmlDocRoot(pagesRoot,POLICY).writeToSink(text);
		trace("writePubText" +" pdf="+pdf+" text=" +text);
		return true;
	}
	boolean writeTextRefs(File textsDir,File refsDir,Map<PubIssue,String>paths)
			throws IOException{
		File file=new File(refsDir,issue+"."+TextReferences.TYPE+EXT_XML_ZIP);
		if(file.exists())return false;
		DataNode pagesRoot=new ValueNode(TYPE_PUBTEXT,issue);
		File text=new File(textsDir,issue+"."+TYPE_PUBTEXT+EXT_XML_ZIP);
		if(text.exists())new XmlDocRoot(pagesRoot,POLICY).readFromSource(text);
		else{
			String path=paths.get(new PubIssue(issue));
			if(path==null)throw new IllegalStateException(
					"Null path for issue="+issue+" in "+Debug.info(this));
			File pdf=new File(ROOT_PDFS+path);
			try{
				pagesRoot=newFilledPagesRoot(pdf);
				if(false)writePubText(pdf,textsDir);
			}catch(Exception e){
				String[]ocr=new OcrTexts(pdf).readAllPages();
				if(ocr.length==0)throw new RuntimeException("No PDF or OCR content");
				TextLines words=TextLines.newBuffer(ocr);
				new XmlDocRoot(pagesRoot,POLICY).readFromSource(words);
			}
			if(false)trace("writeTextRefs: issue=" +issue);
		}
		TypedNode[]pages=pagesRoot.children();
		List<WordSource>sources=new ArrayList();
		for(final TypedNode page:pages)sources.add(new RefsWordSource(page));
		int sourceCount=sources.size();
		TextReferences refs=new TextReferences(issue,newRefsPolicy(sourceCount));
		refs.addSourceWords(sources.iterator());
		refs.close();
		ValueNode tree=refs.newTree();
		TextLines.trace=true;
		file=TextReferences.writeTree(tree,refsDir,EXT_XML_ZIP);
		trace("writeTextRefs: issue=" +issue+
				(true?"":(" pack="+Util.kbs(Bytes.pack(tree).length)))+
				" file="+Util.kbs(file.length()));
		TextLines.trace=false;
		return true;
	}
	private ValueNode newFilledPagesRoot(File pdf) {
		ValueNode root=new ValueNode(TYPE_PUBTEXT,issue,
				new Object[]{KEY_DATE+PAIR_EQUALS+new Date(),KEY_PDF+PAIR_EQUALS+pdf});
		final NodeList pages=new NodeList(root,false);
		PageTexts.retainLineBreaks=debug;
		try{
			new PageTexts(pdf){
				protected void pageTextRead(int pageAt,String text){
					if(debug&&pageAt>1)return;
					pages.add(new DataNode(TYPE_PAGETEXT,""+pageAt,new Object[]{text}));
					if(debug)trace("pageTextRead: pageAt="+pageAt+" text=\n" +text);
				}
			}.readPageTexts();
		}catch(Throwable t){
			DataNode node=new DataNode(TYPE_PAGETEXT,"No pages in "+pdf.getName());
			pages.add(node);
			trace(".newFilledPagesRoot: failed due to "+t+",\n\tadded ",node);
		}finally{
			TextLines.setEncoding(TextLines.DEFAULT_ENCODING);
			pages.updateParent();
		}
		return root;
	}
}