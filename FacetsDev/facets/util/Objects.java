package facets.util;
import static facets.util.Util.*;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
/**
Utility methods for arrays. 
 */
public final class Objects{
  private final static boolean debug=false;
	static Class getMemberType(Object[]array){
  	String fullName=array.getClass().getName(),
  		trimSemiColon=fullName.substring(0,fullName.length()-1),
  		trimDimensions=trimSemiColon.substring(fullName.indexOf("L")+1,trimSemiColon.length());
		Class useClass=null;
		try{
			useClass=Class.forName(trimDimensions);
		}
		catch(ClassNotFoundException e){
			throw new RuntimeException(e+" for "+Debug.info(trimDimensions));
		}
		return useClass;
	}
	static String toString(double[]vals,String spacer,boolean fixed){	  
	  if(vals==null)return "null";
	  StringBuilder list=new StringBuilder();
	  for(int i=0;i<vals.length;i++){
			double val=vals[i];
			list.append((fixed?fxs(val):sfs(val))+(i<vals.length-1?spacer:""));
		}
	  return list.toString();
	}
	static String toString(int[]ints,String spacer){	  
	  if(ints==null)return "null";
	  StringBuilder list=new StringBuilder();
	  for(int i=0;i<ints.length;i++)
	    list.append(ints[i]+(i<ints.length-1?spacer:""));
	  return list.toString();
	}
	public static <T>T[]reverse(Class<T>type,Object[]src){
    T[]reverse=(T[])Array.newInstance(type,src.length);
		for(int i=0,srcFrom=src.length-1;i<reverse.length;i++)
			reverse[i]=(T)src[srcFrom-i];
		return reverse;
	}
	/**
	Converts the type of an array. 
	@param type element type of the new array 
	@param src must contain only objects of a suitable type
	 */
	public static <T>T[]newTyped(Class<T>type,Object[]src){
    T[]array=(T[])Array.newInstance(type,src.length);
    if(src.length>0&&!type.isAssignableFrom(src[0].getClass()))
      throw new IllegalArgumentException(Debug.info(src[0])+" should be of type \n"+type);
    else for(Object s:src)
      if(!type.isAssignableFrom(s.getClass()))
        throw new IllegalArgumentException(Debug.info(s)+" should be of type \n"+type);
    System.arraycopy(src,0,array,0,array.length);
    return array;
  }
  public static <T>T[]join(Class<T>type,T[]front,T[]back){
  	for(T t:front)if(t==null)throw new IllegalStateException(
  			"Null member in "+Debug.info(front));
  	for(T t:back)if(t==null)throw new IllegalStateException(
  			"Null member in "+Debug.info(back));
		if(front==null)return back;if(back==null)return front;
		T[]join=(T[])Array.newInstance(type,front.length+back.length);
		System.arraycopy(front,0,join,0,front.length);
		System.arraycopy(back,0,join,front.length,back.length);
		return join;
  }
  public static <T>T[]uniqued(Class<T>type,Object[]objects){
	  if(debug)Util.printOut("Objects.uniqued: ",objects);
	  Set set=new HashSet(Arrays.asList(objects));
	  if(debug)Util.printOut("",set.toArray());
	  return newTyped(type,set.toArray());
	}
	public static String toLines(Object[]array){
    if(array==null)return "null";
    StringBuilder list=new StringBuilder(array.length*40);
    for(int i=0;i<array.length;i++)
      list.append((array[i]==null?"null"
      		:false?Debug.info(array[i]):array[i].toString())
      	+(i<array.length-1?"\n":""));
    return list.toString();
  }
  public static String toStringWithHeader(Object[]array){
		return Debug.info(array)+" ["+array.length+"] " +
				"{\n"+toLines(array)+"\n}";
	}
  public static String toString(int[]ints){
		return "{"+toString(ints,",")+"}";
	}
  public static String toString(Object[]array){
		return toString(array,",");
	}
  public static String toString(Object[]items,String spacer){
    if(items==null)return "null";
    else if(items.length==0)return "";
    StringBuilder list=new StringBuilder();
		boolean trim=false&&!spacer.equals("\n");
		int at=0;
    for(Object item:items)list.append(
				(item==null?"null":trim?item.toString().trim():item)
				+(++at==items.length?"":spacer)
			);
    return list.toString();
  }
  private static int indexOf_(Object[]array,Object member){
    for(int i=0;i<array.length;i++)if(array[i]==member)return i;
    throw new RuntimeException(Debug.info(member)+" not found in \n"+Debug.arrayInfo(array));
  }
}
