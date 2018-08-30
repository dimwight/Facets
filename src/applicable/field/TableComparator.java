package applicable.field;
import java.util.Comparator;
/**
Encapsulates user-friendly sorting. 
<p>{@link TableComparator} enables sorting by
<ul>
<li>pushing empty values to the end of the sort
<li>comparing the returns of {@link #compareNonEmpties(Object, Object)}
</ul>
 */
public class TableComparator<T>implements Comparator<T>{
	private final boolean invert;
	public TableComparator(boolean invert){
		this.invert=invert;
	}
	@Override
	final public int compare(T p,T q){
		boolean noP=isEmpty(p),noQ=isEmpty(q);
		return noP&&noQ?0:noP&&!noQ?invert?-1:1:noQ&&!noP?invert?1:-1 
				:compareNonEmpties(p,q);
	}
	public boolean isEmpty(T t){
		return t.toString().equals("");
	}
	/**
	Called from <code>final</code> implementation of {@link #compare(Object, Object)}. 
	@return by default comparison using {@link #integerValue(Object)}
	 */
	public int compareNonEmpties(T p,T q){
		return integerValue(p).compareTo(integerValue(q));
	}
	/**
	Enables arbitrary ranking of stringified values. 
	@param t passed from default {@link #compareNonEmpties(Object, Object)}
	@return by default {@link Integer#valueOf(String)}
	 */
	protected Integer integerValue(T t){
		return Integer.valueOf(t.toString().replaceAll("\\D",""));
	}
	public static TableComparator simpleString(boolean invert){
		return new TableComparator<String>(invert){
			public int compareNonEmpties(String p,String q){
				return p.compareTo(q);
			};
		};
	}
}