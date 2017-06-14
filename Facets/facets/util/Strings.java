package facets.util;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
/**
String management and conversion. 
 */
public final class Strings{
	final public static String SPLIT_ARRAY=",",SPLIT_ARRAYS=" \t";
	private static String NUMBER_SPACER=SPLIT_ARRAY+" ",
			SPLIT_MARK="12345 splitMark 67890";
	public static String sfString(double[]vals){
		return Objects.toString(vals,NUMBER_SPACER,false);
	}
	public static String fxString(double[]vals){
		return Objects.toString(vals,NUMBER_SPACER,true);
	}
	public static String intsSelectable(int[]ints){
		return Objects.toString(ints,NUMBER_SPACER);
	}
	public static String intsString(int[]ints){
		return Objects.toString(ints,SPLIT_ARRAY);
	}
	public static String linesString(String[]lines){
		return Objects.toString(lines,"\n");
	}
	public static String hexString(int bits){
		return Integer.toHexString(bits).toUpperCase();
	}
	public static String trimToLength(String text,int max){
		return text.substring(0,Math.min(max,text.length()));
	}
	public static String fileNameTop(File file){
		String name=file.getName();
		if(name.trim().equals(""))name=file.getAbsolutePath();
		if(name.trim().equals(""))throw new IllegalStateException(
				"Empty name file="+file);
		int extAt=name.indexOf(".");
		return extAt<1?name:name.substring(0,extAt);
	}
	public static String fileNameTail(File file){
		String name=file.getName();
		int extAt=name.indexOf(".");
		return extAt<1?"":name.substring(extAt+1);
	}
	public static String delete(String from,String what){
		if(from==""||what=="")return from;
		int start=from.indexOf(what),end=start+what.length();
		if(start<0)return from;
		return new StringBuilder(from).delete(start,end).toString();
	}
	public static String[]sortLines(String[]lines){
		List<String>sorter=false?new ArrayList(Arrays.asList(lines)):Arrays.asList(lines);
		Collections.sort(sorter);
		return sorter.toArray(new String[]{});
	}
	public static String[]stringLines(String source){
		return source.split("\n");
	}
	public static double[]toDoubles(String src){
		String[]strings=toStrings(src);
		double[]doubles=new double[strings.length];
		for(int i=0;i<doubles.length;i++)doubles[i]=new Double(strings[i].trim());
		return doubles;
	}
	public static int[]toInts(String string){
	 String[]strings=toStrings(string);
	 int[]ints=new int[strings.length];
	 for(int i=0;i<ints.length;i++)ints[i]=new Integer(strings[i].trim());
	 return ints;
 }
 public static String[]splitAfter(String text,String pattern,int limit){
		return text.replaceAll(pattern,"$1"+SPLIT_MARK).split(SPLIT_MARK,limit);
	}
static String reverse(String string){
		return new StringBuilder(string).reverse().toString();
	}
	static String[]toStrings(String src){
	 return src.split("\\s*" +SPLIT_ARRAY+"\\s*");
	}
 private static String[]arrayStrings(String src){
	 return src.split("\\s*" +SPLIT_ARRAYS+"\\s*");
 }
 private static double[][]toDoubleArrays_(String src){
	 String[]strings=arrayStrings(src);
	 double[][]arrays=new double[strings.length][];
	 for(int i=0;i<arrays.length;i++)arrays[i]=toDoubles(strings[i]);
	 return arrays;
	}
	private static double initialDouble_(String text){
		String digits=text.replaceAll("([0-9.]+).*","$1");
		return Double.valueOf(digits);
	}
	private static String fixEmptyLines_(String text){
  return text.replaceAll("[\n\r]\\s*[\n\r]","\n");
 }
	private static boolean equal_(String test,String...candidates){
		for(String string:candidates)if(test.equals(string))return true;
		return false;
	}	
}
