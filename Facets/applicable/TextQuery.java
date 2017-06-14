package applicable;
import facets.util.Tracer;
public class TextQuery extends Tracer{
	public static final String FIELD_TOP="\\",PATTERN_TOP="\\\\",
		BARE_FIELD_TOP="BARE FIELD";
	public final String text,qualifier;
	public boolean exact,any,patterns;
	public TextQuery(boolean any,boolean exact,String text){
		this.any=any;
		this.exact=exact;
		String qualifier="";
		boolean patterns=text.startsWith(PATTERN_TOP);
		if(patterns){
			String patterned=text.substring(PATTERN_TOP.length());
			qualifier=patterned.replaceAll("(\\w+).*","$1");
			patterned=patterned.substring(qualifier.length()).trim();
			if(false)trace(": qualifier="+qualifier+" patterned="+patterned);
			if(patterned.trim().equals(""))
				throw new RuntimeException("Invalid pattern text="+text);
			else this.text=patterned;
		}
		else if(text.startsWith(FIELD_TOP)){
			String[]items=text.split("\\W+");
			if(items.length>1){
				qualifier=items[1];
				boolean bareField=items.length==2;
				if(bareField)qualifier=BARE_FIELD_TOP+qualifier;
				this.text=bareField?qualifier
						:text.replace(FIELD_TOP+qualifier,"").trim();
			}
			else this.text="";
		}
		else this.text=text;
		this.qualifier=qualifier;
		this.patterns=patterns;
	}
	final public SimpleFilter newFilter(){
		return patterns?new TextPatternFilter(text)
			:new TextQueryFilter(TextQueryFilter.textToTerms(text),any,exact);
	}
}