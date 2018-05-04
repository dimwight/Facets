package applicable.field;
import facets.util.Debug;
import facets.util.tree.ValueNode;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
public class BooleanField extends ValueField<Boolean>{
	public static final Format FORMAT_YES=new Format(){
		@Override
		public StringBuffer format(Object o,StringBuffer sb,FieldPosition pos){
			sb.append(o.equals(true)?"Yes":"No");
			return sb;
		}
		@Override
		public Object parseObject(String source,ParsePosition pos){
			throw new RuntimeException("Not implemented in "+Debug.info(this));
		}
	};
	public BooleanField(String name){
		super(name);
	}
	@Override
	final public TableComparator sorter(boolean sortDown){
		return new TableComparator<Boolean>(sortDown){
			public boolean isEmpty(Boolean t){
				return t.equals(false);
			};
			public int compareNonEmpties(Boolean p,Boolean q){
				return p.compareTo(q);
			};
		};
	}
	@Override
	final protected Boolean newNullValue(ValueNode values){
		return new Boolean(false);
	}
	@Override
	protected Boolean textToValue(String text,ValueNode values){
		return Boolean.valueOf(text);
	}
	@Override
	protected Boolean parseInputText(String text){
		return text.trim().equals("")?false
			:text.trim().toLowerCase().matches("y|yes|true");
	}
	@Override
	public Format format(){
		return FORMAT_YES;
	}
}