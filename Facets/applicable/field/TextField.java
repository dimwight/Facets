package applicable.field;
import facets.core.app.TextView.LongText;
import facets.util.tree.ValueNode;
public class TextField extends ValueField<String>{
	public static class LongTextField extends ValueField<LongText>{
		public LongTextField(String name){
			super(name);
		}
		@Override
		protected LongText parseInputText(String text){
			return new LongText(text);
		}
		@Override
		protected LongText newNullValue(ValueNode values){
			return new LongText("");
		}
		@Override
		protected LongText textToValue(String text,ValueNode values){
			return new LongText(text);
		}
		@Override
		public TableComparator sorter(boolean sortDown){
			return new TableComparator<LongText>(sortDown){
				public boolean isEmpty(LongText t){
					return t.toString().equals("");
				};
				public int compareNonEmpties(LongText p,LongText q){
					return p.toString().compareTo(q.toString());
				};
			};
		}
		@Override
		public int inputCols(){
			return 30;
		}
		public int inputRows(){
			return 1;
		}
	}
	public TextField(String name){
		super(name);
	}
	@Override
	public TableComparator sorter(boolean sortDown){
		return TableComparator.simpleString(sortDown);
	}
	@Override
	protected String textToValue(String text,ValueNode values){
		return text;
	}
	@Override
	protected String newNullValue(ValueNode values){
		return "";
	}
	@Override
	protected String parseInputText(String text){
		return text;
	}
}