package demo.appidiom;
import static facets.util.Times.*;
import static facets.util.Util.*;
import static java.lang.System.nanoTime;
import facets.core.app.TextView;
import facets.util.Debug;
import facets.util.Doubles;
import facets.util.HtmlBuilder;
import facets.util.HtmlBuilder.RenderTarget;
import facets.util.Identified;
import facets.util.Objects;
import facets.util.StatefulCore;
import facets.util.TextLines;
import facets.util.Util;
import facets.util.app.AppValues;
import facets.util.tree.DataNode;
import facets.util.tree.TypedNode;
import facets.util.tree.ValueNode;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import applicable.field.BooleanField;
import applicable.field.FieldFormBuilder;
import applicable.field.FieldSet;
import applicable.field.TextField;
import applicable.field.TextField.LongTextField;
import applicable.field.ValueField;
final public class AppContent extends StatefulCore implements Identified{
	public static final Integer[]SIZES=new Integer[]{200,true?10:1000,5000};
	public static final int TABLE_TEXT=0,TABLE_COUNT=1,TABLE_SHORT=2,TABLE_ROW=3;
	public static final String CHAPTER_SPLIT="$chapter";
	private static final int CHAR_BYTES=2,kbChars=1024/CHAR_BYTES;
	private static double timeMillis;
	private static final boolean debugTimes=false;
	static{
		final int TIMES=50,MEANS=TIMES/3;
		double[]means=new double[MEANS];
		long start=nanoTime();
		for(int t=0;t<TIMES;t++){
			long time=doTimeBuild();
			if(t<TIMES-MEANS)continue;
			int meanAt=t-(TIMES-MEANS);
			means[meanAt]=time;
			if(false&&meanAt>MEANS/2)
				Util.printOut("AppContent: t="+t+" time="+time+
						" mean=",sfs(new Doubles(Arrays.copyOfRange(means,0,meanAt)).mean()));
		}
		timeMillis=(false?(nanoTime()-start)/TIMES:new Doubles(means).mean())/1000/1000;
		if(debugTimes)Util.printOut("AppContent: timeMillis=",sfs(timeMillis));
	}
	private static long doTimeBuild(){
		long start=nanoTime();
		for(int r=0;r<10*1000;r++)new Random();
		long time=(nanoTime()-start);
		return time;
	}
	private static final String[]rowSplits={"\n","\\.","\\s+",""},chapters;
	static{
		try{
			File dir=false?AppValues.userDir():new File(Util.runDir(),"_doc");
			chapters=new TextLines(new File(dir,"chapters.txt")
				).readLinesString().replaceAll("(Mr|Mrs)\\.","$1")
				.split("\\" +CHAPTER_SPLIT);
		}
		catch(IOException e){
			throw new RuntimeException(e);
		}
	}
	public final int kb,time;
	private final int id;
	private final String rowSplit,texts[];
	private static int ids;
	@Override
	protected void traceOutput(String msg){
		if(debugTimes)printElapsed(this+msg);
		else if(true)Util.printOut(this+msg);
	}
	public AppContent(int id,int kb,int time){
		super("Content"+(id+1));
		this.id=id;
		this.kb=kb;
		this.time=time;
		texts=new String[kb];
		rowSplit=rowSplits[0];
	}
	public AppContent build(){
		if(id<0)throw new IllegalStateException("Can't build row "+Debug.info(this));
		else if(texts[0]!=null)throw new IllegalStateException("Already built "+Debug.info(this));
		else if(debugTimes)trace(".build");
		String chapter=chapters[id%chapters.length];
		int charCount=chapter.length();
		if(charCount<kbChars)throw new IllegalStateException(
				"Chapter too short in "+Debug.info(this));
		else for(int t=0,charAt=0;t<texts.length;t++){
			if(charAt>charCount)charAt=0;
			texts[t]=chapter.substring(charAt,Math.min(charAt+=kbChars,charCount));
		}
		if(time==0)return this;
		int times=time<0?Integer.MAX_VALUE:(int)(time/timeMillis);
		if(debugTimes){
			setResetWait(5000);
			trace(".build: time="+time+" times="+times);
		}
		for(int t=0;t<times;t++){
			doTimeBuild();
			if(debugTimes&&t%250==0)trace("times="+t);
		}
		if(debugTimes)trace(".build~");
		return this;
	}
	public String asText(){
		String text=Objects.toString(texts,"^").replaceAll("\\^","").trim();
		if(false)trace(".asText: text=",Util.kbs(text.length()*CHAR_BYTES));
		if(rowSplit!=rowSplits[3])text=text.replaceAll("([^.])$","$1\\.");
		return text.replaceAll("\nnull","");
	}
	public AppContent[]asRows(){
		int rowAt=0;
		List<AppContent>rows=new ArrayList();
		for(String text:splitTexts())
			if(rowSplit==rowSplits[0]&&text.trim().matches("[A-Z\\W]+"))continue;
			else{
				text=rowSplit==rowSplits[2]?text.replaceAll("\\W*(\\w+)\\W*","$1")
					:text.replaceAll("([^.])$","$1\\.");
				rows.add(new AppContent(title(),rowAt++,text,
				rowSplit));
			}
		return rows.toArray(new AppContent[]{});
	}
	public TypedNode asTree(){
		return new DataNode(AppContent.class.getSimpleName(),title(),splitTexts());
	}
	private String[] splitTexts(){
		return asText().replaceAll("\n+","\n").split(rowSplit);
	}
	private AppContent(String titleTop,int rowAt,String text,String lastSplit){
		super(titleTop+"["+rowAt+"]");
		texts=new String[]{text};
		kb=text.length()*CHAR_BYTES/1024+1;
		time=0;
		id=-1*ids++;
		String thisSplit=null;
		for(int i=0;i<rowSplits.length;i++)
			if(rowSplits[i].equals(lastSplit)){
				thisSplit=rowSplits[i+1];
				break;
			}
		if(thisSplit==null)throw new IllegalStateException(
				"Null split in "+Debug.info(this));
		else rowSplit=thisSplit;
	}
	public ValueNode newRowNode(){
		if(texts.length!=1)throw new IllegalStateException(
				"Not a row in "+Debug.info(this));
		String text=texts[0];
		int rowAt=id*-1,intValue=text.length();
		return new ValueNode("Row","#"+rowAt,new Object[]{
			text,
			intValue,
			intValue<200,
			rowAt
		});
	}
	static String[]fieldTitles(boolean full){
		return (!full?"Row No,Text":"Text,Characters,Row No,< 200").split(",");
	}
	public static HtmlBuilder newRowForm(ValueNode values,final boolean input){
		ValueField[]fields={
			new LongTextField("Text"){
				@Override
				public int inputCols(){
					return 50;
				}
				@Override
				public int inputRows(){
					return 0;
				}
				@Override
				protected String getValue(ValueNode values,String valueKey){
					return values.values()[0];
				}
			},
			new TextField("Characters"){
				@Override
				protected String getValue(ValueNode values,String valueKey){
					return false?TextView.DEBUG_TEXT:values.values()[1];
				}
				@Override
				public int inputCols(){
					return 3;
				}
			},
			new BooleanField("< 200"){
				@Override
				protected String getValue(ValueNode values,String valueKey){
					return values.values()[2];
				}
			},
			new TextField("Row No"){
				@Override
				protected String getValue(ValueNode values,String valueKey){
					return values.values()[3];
				}
				@Override
				public int inputCols(){
					return 2;
				}
			},
		};
		return new FieldFormBuilder(RenderTarget.Swing,new FieldSet("Full",fields,0),
				values,
				"Row No,Characters,< 200","Text"
				){
			@Override
			protected boolean useInputField(String name){
				return input&&!name.equals("Text_");
			}
		};
	}
	@Override
	public Object identity(){
		return Math.abs(id);
	}
	@Override
	public String toString(){
		return title()+" [kb="+kb+", time="+time+"]";
	}
	@Override
	public int hashCode(){
		return Arrays.hashCode(values());
	}
	@Override
	public boolean equals(Object that){
		return Arrays.equals(this.values(),((AppContent)that).values());
	}
	private int[]values(){
		return new int[]{kb,time};
	}
	static final class Tasks{
		private static int ids;
		public final String title;
		private final int[]kbs,times;
		public Tasks(String title,int[]kbs,int[]times){
			this.title=title;
			this.kbs=kbs;
			this.times=times;
		}
		public AppContent newTask(){
			final int id=ids++;
			return new AppContent(id,kbs[id%kbs.length],times[id%times.length]);
		}
	}}
