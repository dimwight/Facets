package applicable.field;
import static java.util.Calendar.*;
import facets.util.Util;
import facets.util.tree.ValueNode;
import java.text.DateFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
public class DateField extends ValueField<Date>{
	public static final DateFormat STYLE_FULL=DateFormat.getDateInstance(DateFormat.SHORT),
	 STYLE_MM_YYYY=new SimpleDateFormat("MM/yyyy"),STYLE_MM_YY=new SimpleDateFormat("MM/yy");
	public static final String NULL_DATE_TEXT="01/01/1000";
	public static Date nullDate;
	static{
		try{
			nullDate=STYLE_FULL.parse(NULL_DATE_TEXT);
		}catch(ParseException e){
			throw new RuntimeException(e);
		}
	}
	public static boolean dateIsToday(Date date){
		return STYLE_FULL.format(date).equals(STYLE_FULL.format(new Date()));
	}
	public DateField(String title){
		super(title);
	}
	private Date parseDate(String text){
		if(text.trim().equals(""))return nullDate;
		Date date=new Date();
		DateFormat[]formats=new DateFormat[]{
			STYLE_FULL,
			new SimpleDateFormat("dd/MM"),
		};
		int at=0;
		while(at<formats.length)try{
			DateFormat format=formats[at];
			date=format.parse(text);
			GregorianCalendar dates=new GregorianCalendar();
			int yearNow=dates.get(YEAR);
			dates.setTime(date);
			if(dates.get(YEAR)==1970)dates.set(YEAR,yearNow);
			if(false)trace(".parseDate: at="+at+" year=",dates.get(YEAR));
			return dates.getTime();
		}catch(ParseException e){}finally{
			at++;
		}
		return date;
	}
	@Override
	public TableComparator sorter(boolean sortDown){
		return new TableComparator<Date>(sortDown){
			public boolean isEmpty(Date t){
				return t.equals(nullDate);
			};
			public int compareNonEmpties(Date p, Date q){
				return q.compareTo(p);
			};
		};
	}
	@Override
	public Format format(){
		return STYLE_FULL;
	}
	@Override
	protected Date newNullValue(ValueNode values){
		return nullDate;
	}
	@Override
	protected Date textToValue(String text,ValueNode values){
		return parseDate(text);
	}
	@Override
	protected Date parseInputText(String text){
		return parseDate(text);
	}
	@Override
	protected String formatInputValue(Date value){
		return STYLE_FULL.format(value);
	}
}