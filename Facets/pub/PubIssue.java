package pub;
import static applicable.refs.TextReferences.RefsStrategy.*;
import static facets.util.Regex.*;
import static java.lang.Math.*;
import facets.util.Debug;
import facets.util.Objects;
import facets.util.Tracer;
import facets.util.Util;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import pdft.PdfCore;
import applicable.refs.TextReferences.Policy;
import applicable.refs.TextReferences.RefsStrategy;
import applicable.refs.TextReferences.WordSource;
final public class PubIssue extends Tracer implements Comparable<PubIssue>,Serializable{
	public static final String TYPE_PDFS="pubPdfs",PDF_NONE="unlinked",
		FILE_IPUBS=false?"http://ukintranet/techpubs/":PubValues.ROOT_PDFS;
	static Policy newRefsPolicy(int sourceCount){
		final RefsStrategy strategy=!PubIndexer.optimiseRefs?None:true?RootByStrsSq:ByStrs;
		final int threshold=strategy==RootByStrsSq?10:15;
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
	public final String issue;
	private final List<Integer>textQueryPages=new ArrayList();
	protected void traceOutput(String msg){
		System.out.println(msg);
	};
	public PubIssue(String issue){
		this.issue=issue;
	}
	void addPageFinds(Set<WordSource>finds){
		if(!textQueryPages.isEmpty())throw new IllegalStateException(
				"Can't add to textQueryPages="+textQueryPages.size()+" in "+Debug.info(this));
		else for(WordSource page:finds)
			textQueryPages.add(Integer.valueOf(page.identity().replace("page","")));
		Collections.sort(textQueryPages);
	}
	public Integer[]textQueryPages(){
		if(textQueryPages.isEmpty())throw new IllegalStateException(
				"Empty textQueryPages in "+Debug.info(this));
		else return textQueryPages.toArray(new Integer[]{});
	}
	@Override
	public int compareTo(PubIssue o){
		return issue.compareTo(o.issue);
	}
	@Override
	public String toString(){
		return issue;
	}
	@Override
	public boolean equals(Object o){
		return issue.equals(((PubIssue)o).issue);
	}
	@Override
	public int hashCode(){
		return issue.hashCode();
	}
	private void _openTextQueryPdf(Map<PubIssue,String>paths){
		PdfCore.openViewPdf(PubValues.ROOT_PDFS+paths.get(this),textQueryPages()[0]);
	}
	public static Map<PubIssue,String>newIssuePaths(String[]pdfPaths){
		final Map<PubIssue,String>paths=new HashMap(pdfPaths.length);
		if(false)Util.printOut("PubIssue: pdfPaths=",pdfPaths.length);
		List<String>bad=new ArrayList();
		for(String path:pdfPaths){
			String check=path+=" ";
			int ocrAt=check.indexOf(".ocr.txt"),pdfAt=check.indexOf(".pdf"),
				extAt=max(pdfAt,ocrAt);
			check=check.substring(path.lastIndexOf("/")+1,
					min(extAt,check.indexOf(" "))); 
			if(!check.substring(0,1).matches("E|\\d"))bad.add(path);
			else{
				PubIssue issue=new PubIssue(replaceAll(check,"_E","","(\\d)E","$1").trim());
				if(paths.get(issue)!=null)Util.printOut("PubIssue: duplicate paths for issue="+issue+
							"\n"+path+"\n"+paths.get(issue));
				paths.put(issue,path.trim());
			}
		}
		ArrayList<PubIssue>good=new ArrayList(paths.keySet());
		Collections.sort(good);
		Collections.sort(bad);
		if(true)Util.printOut("PubIssue:" +
				" paths="+paths.size()/*+
				" bad=" +(true?bad.size():Objects.toString(
						false?pdfPaths:true?paths.keySet().toArray():good.toArray()))*/);
		return Collections.unmodifiableMap(paths);
	}
}