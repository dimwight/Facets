package tones;
import facets.util.Debug;
import facets.util.Objects;
import facets.util.Tracer;
import facets.util.Util;
import java.util.ArrayList;
import java.util.List;
public abstract class Mark extends Tracer{
	public static final class ClefMark extends Mark{
		public final Clef clef;
		public ClefMark(Clef clef){
			this.clef=clef;
		}
	}
	public static final class Tie extends Mark{
		public final Tone before,after;
		public Tie(Tone before,Tone after){
			this.before=before;
			this.after=after;	
			if(false&&before.barAt<4)trace(": ",this);
		}
		@Override
		public String toString(){
			return "before="+before+" to="+after;
		}
		@Override
		public boolean equals(Object o){
			Tie that=(Tie)o;
			return that.before.equals(before)&&that.after.equals(after);
		}
	}
	public static final class Tails extends Mark{
		public final List<Tone>tones=new ArrayList();
		public final Voice voice;
		public Tails(Voice voice){
			this.voice=voice;
		}
		public void addTone(Tone add){
			tones.add(add);
			if(false&&add.barAt==6&&voice==Voice.Alto)trace(".addTone: ",add);
		}
		@Override
		protected void traceOutput(String msg){
			Util.printOut(Debug.info(this)+" "+msg);
		}
		@Override
		public String toString(){
			return Objects.toString(tones.toArray());
		}
	}
}
