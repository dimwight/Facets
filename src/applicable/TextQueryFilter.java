package applicable;
import facets.util.Debug;
import facets.util.Regex;
import facets.util.Strings;
import applicable.HtmlRecordApplet.HtmlRecord;
public class TextQueryFilter<T>extends SimpleFilter<TextSearchable>{
	protected final String[]terms;
	protected final int[]matches;
	protected final boolean matchAny;
	private final boolean exact;
	public TextQueryFilter(String[]terms,boolean matchAny,boolean exact){
		this.terms=terms;;
		matches=new int[terms.length];
		this.matchAny=matchAny;
		this.exact=exact;
	}
	protected boolean passes(TextSearchable s){
		String searchable=s.searchableText().trim().toLowerCase();
		if(false)trace(".passes: searchable="+searchable+
				"\nterms="+Strings.linesString(terms));
		boolean noFirstKey=true,passes=matchAny?false:true;
		int termAt=0;
		for(String term:terms){
			boolean matched=exact?!Regex.find(searchable,"\\b"+term+"\\b").equals("")
				:searchable.contains(term);
			if(matched)matches[termAt]++;
			if(matchAny){
				passes|=(noFirstKey||terms.length==1||termAt>0)&&matched;
			}else passes&=matched;
			termAt++;
		}
		return passes&(noFirstKey||searchable.contains(terms[0]));
	}
	protected TextSearchable newExceptionResult(Exception e){
		throw new RuntimeException(e);
	}
	public String summary(){
		String summary="matchAny="+matchAny;
		for(int i=0;i<terms.length;i++)
			summary+="\nterm="+terms[i]+" matches="+matches[i];
		return summary;
	}
	public static String[]textToTerms(String queryText){
		return queryText.trim().toLowerCase().split("\\s+");
	}
}