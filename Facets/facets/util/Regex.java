package facets.util;
import static facets.util.Regex.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
Regular-expression utilities. 
 */
final public class Regex{
	public static final String DOT_ALL="(?s)",AT_START="^",ANYTHING=".*";
	public static String FIX_CHECK="";
	public final static Map<String,Pattern>patterns=new HashMap();
	private static Pattern getPattern(String pattern){
		if(true)return Pattern.compile(pattern);
		Pattern got=patterns.get(pattern);
		if(got!=null)return got;
		patterns.put(pattern,got=Pattern.compile(pattern));
		return got;
	}
	public static String replaceAll(String text,boolean dotAll,String...fixPairs){
		boolean checkMe=!FIX_CHECK.equals("")&&text.contains(FIX_CHECK);
		if(checkMe){
			Util.printOut("Regex.applyFixes: checking ", FIX_CHECK+" in "+text);
			if(true)Debug.printStackTrace(2);
		}
		for(int i=0;i<fixPairs.length;i+=2){
			text=doReplaceAll(text,(dotAll?DOT_ALL:"")+fixPairs[i],fixPairs[i+1]);
			if(checkMe)Util.printOut("Regex.applyFixes: '", fixPairs[i]+"' >"+text);
		}
		return text;
	}
	private static String doReplaceAll(String text,String find,String fix){
		if(text==null)throw new IllegalArgumentException(
				"Null text for "+find);
		return false?text.replaceAll(find,fix)
				:getPattern(find).matcher(text).replaceAll(fix);
	}
	private static String[]finds(String text,String pattern,boolean reverse,
			boolean single){
		if(reverse)text=Strings.reverse(text);
		Matcher m=getPattern(pattern).matcher(text);
		if(single)return new String[]{
				m.find()?!reverse?m.group():Strings.reverse(m.group()):""};
		ItemList<String>finds=new ItemList(String.class);		
		while(m.find())
			finds.addItem(!reverse?m.group():Strings.reverse(m.group()));
		return finds.items();
	}
	public static String[]captures(String text,String pattern,boolean reverse){
		if(reverse)text=Strings.reverse(text);
		Matcher m=getPattern(pattern).matcher(text);
		if(!m.find())return null;
		String[]matches=new String[m.groupCount()];
		for(int i=0;i<matches.length;i++)
			matches[i]=!reverse?m.group(i+1):Strings.reverse(m.group(i+1));
		return matches;
	}
	public static String replaceAll(String text,String...fixPairs){
		return replaceAll(text,false,fixPairs);
	}
	public static  boolean contains(String text,String pattern){
		return !find(text,pattern).equals("");
	}
	public static String find(String text,String pattern){
		return find(text,pattern,false);
	}
	public static String[]finds(String text,String pattern){
		return finds(text,pattern,false);
	}
	private static String find(String text,String pattern,boolean reverse){
		String[]finds=finds(text,pattern,reverse,true);
		return finds.length==0?"":finds[0];
	}
	public static String[]finds(String text,String pattern,boolean reverse){
		return finds(text,pattern,reverse,false);
	}
	public static String[]captures(String text,String pattern){
		return captures(text,pattern,false);
	}
}
