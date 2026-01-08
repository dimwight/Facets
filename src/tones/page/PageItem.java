package tones.page;
import static tones.Voice.*;
import static tones.page.PageItem.PageTie.TieType.*;
import facets.core.app.avatar.AvatarContent;
import facets.util.Objects;
import facets.util.Tracer;
import facets.util.geom.Line;
import facets.util.geom.Point;
import facets.util.geom.Vector;
import tones.Tone;
import tones.Voice;
import tones.bar.Bar;
public abstract class PageItem extends Tracer implements AvatarContent{
	public static final class PageTie extends PageItem{
		public enum TieType{BeforeAfter,BeforeNull,AfterNull}
		public final PageBar bar;
		public final TieType type;
		public final Voice voice;
		public final Vector beforeAt,afterAt;
		public final PageNote before,after;
		public final boolean selected;
		public PageTie(PageNote before,PageNote after,PageBar bar,boolean selected){
			this.before=before;
			this.after=after;
			this.bar=bar;
			this.selected=selected;
			type=after==null?AfterNull:before==null?BeforeNull:BeforeAfter;
			voice=(type==AfterNull?before:after).tone.voice;
			this.beforeAt=before==null?null:before.staveAt();
			this.afterAt=after==null?null:after.staveAt();
			if(false)trace(":",this);
		}
		@Override
		public String toString(){
			return " before="+before==null?"null":before+
					" after="+(after==null?"null":after);
		}
	}
	public static final class PageTails extends PageItem{
		private final PageNote[]notes;
		public final Line geom;
		public final PageNote from,to;
		public final boolean selected,single;
		public PageTails(PageNote[]notes,boolean selected){
			this.notes=notes;
			this.selected=selected;
			Tone tone=notes[0].tone;
			if(false&&tone.barAt==11&&tone.voice==Voice.Tenor)trace(": ",notes);
			from=notes[0];
			int count=notes.length;
			to=notes[count-1];
			single=to==from;
			Point fromTailTo=from.tail.to;
			if(single){
				geom=new Line(fromTailTo,fromTailTo.shifted(new Vector(3,5)));
				return;
			}
			geom=new Line(fromTailTo,to.tail.to);
			double yDiff=to.at.y-from.at.y;
			for(int at=1;at<count-1;at++){
				PageNote note=notes[at];
				note.tail.to.shift(new Vector(0,from.at.y-note.at.y
						+(false?0:yDiff/(count-1)*at)));
			}
		}
		public String toString(){
			return Objects.toString(notes);
		}
	}
	public static final int STAVE_GRID=11;
	final static Vector scaleToNoteWidth=new Vector(Bar.WIDTH_NOTE,1);
	
}
