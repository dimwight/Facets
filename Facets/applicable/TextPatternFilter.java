package applicable;
import java.util.regex.Pattern;
public final class TextPatternFilter<T>extends SimpleFilter<TextSearchable>{
	private final Pattern pattern;
	public TextPatternFilter(String pattern){
		this.pattern=Pattern.compile(pattern);
		if(false)trace(": pattern=",pattern);
	}
	@Override
	protected boolean passes(TextSearchable s){
		String text=s.searchableText().trim();
		if(false)trace(".passes: text=",text);
		return pattern.matcher(text).find();
	}
}