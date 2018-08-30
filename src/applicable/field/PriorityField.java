package applicable.field;
import facets.util.Debug;
import facets.util.Objects;
import facets.util.Util;
import facets.util.app.AppValues;
import facets.util.tree.ValueNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public abstract class PriorityField extends OptionField{
	public static final String PATH="priorities";
	private static final boolean live=true;
	final String nullValue;
	private final boolean listing;
	private final ValueNode state;
	public PriorityField(boolean listing,AppValues spec){
		super("Priority","High,Medium,Low,None");
		this.listing=listing;
		nullValue=listing?"":"None";
		state=spec==null?null:spec.state(statePath());
	}
	protected String[]statePath(){
		return new String[]{PATH};
	}
	@Override
	protected void traceOutput(String msg){
		if(false&&state!=null)Util.printOut(Debug.info(this)+msg);
	}
	@Override
	final protected String textToValue(String text,ValueNode values){
		Integer got=readTable().get(getTableKey(values));
		if(got==null)return nullValue;
		String option=options[Integer.valueOf(got.toString())-1];
		return !listing?option:(name+option);
	}
	@Override
	final protected String newNullValue(ValueNode values){
		return state==null?nullValue:textToValue(null,values);
	}
	@Override
	public void putInputValue(ValueNode values,String text){
		if(!live)super.putInputValue(values,text);
		if(state==null)return;
		Map<String,Integer>table=readTable();
		table.put(getTableKey(values),optionNumber(text));
		state.setValues(new Object[]{});
		for(String o:options){
			if(o.equals("None"))continue;
			List<String>items=new ArrayList();
			Integer number=optionNumber(o);
			for(String key:table.keySet())
				if(table.get(key).equals(number))items.add(key);
			Collections.sort(items);
			if(!items.isEmpty())state.put(o,Objects.toString(items.toArray()));
		}
		trace(".putInputValue: ",state.values());
	}
	protected abstract String getTableKey(ValueNode values);
	@Override
	final protected String formatInputValue(String value){
		return value.equals("None")?"99":optionNumber(value).toString();
	}
	private Map<String,Integer>readTable(){
		Map<String,Integer>table=new HashMap();
		for(String o:options){
			if(o.equals("None")||state.get(o)==null)continue;
			for(String item:state.getString(o).split(","))
				table.put(item,optionNumber(o));
		}
		return table;
	}
	private Integer optionNumber(String value){
		value=value.replace(name,"");
		if("1,2,3".contains(value))return Integer.valueOf(value);
		else for(int i=0;i<options.length;i++)
			if(options[i].equals(value))return i+1;
		throw new IllegalStateException("No number for value="+value);
	}
	@Override
	final public TableComparator sorter(boolean sortDown){
		return new TableComparator<String>(sortDown){
			@Override
			protected Integer integerValue(String t){
				return optionNumber(t);
			}
		};
	}
}