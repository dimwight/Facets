package tones.bar;
import static java.lang.Math.*;
import static tones.Tone.*;
import facets.util.Debug;
import facets.util.Objects;
import facets.util.Tracer;
import facets.util.Util;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import tones.Tone;
import tones.bar.Incipit.Soundings;
final public class Bar extends Tracer{
	public static final int WIDTH_BEAT=SIXTEENTHS?4:8,
			WIDTH_NOTE=SIXTEENTHS?16:8;
	private static final int START_AT=WIDTH_NOTE/2;
	public final int at,rise,staveGap,fall,width;
	public final Set<Incipit>incipits;
	public final Soundings endSoundings;
	Bar(int barAt,List<Incipit>incipits){
		if(incipits==null)throw new IllegalStateException(
				"Null incipits in "+Debug.info(this));
		else this.incipits=new HashSet(incipits);
		this.at=barAt;
		if(false)trace(": ",this);
		int rise=-1,staveGap=-1,fall=-1,gridAt=START_AT;
		int beatsThen=0,jumpBeats=0;
		Function<Integer,Integer>jumpMod=beats->
			max(WIDTH_NOTE,(int)(pow(WIDTH_BEAT*beats,0.5)*3.5*NOTE_EIGHTH));
		if(false)trace(".Bar: incipits=",incipits);
		for(Incipit i:incipits){
			final int beatsNow=i.beatAt;
			jumpBeats=beatsNow-beatsThen;
			if(false&&this.at<6)trace(": ",beatsNow);
			gridAt=i.close(gridAt+(beatsNow==0?0:jumpMod.apply(jumpBeats)));
			if(false)trace(": ",i);
			beatsThen=beatsNow;
			rise=max(rise,i.rise);
			staveGap=max(staveGap,i.staveGap);
			fall=max(fall,i.fall);
		}
		width=gridAt+jumpMod.apply(jumpBeats);
		this.rise=rise;
		this.staveGap=staveGap;
		this.fall=fall;
		endSoundings=incipits.get(incipits.size()-1).soundings();
	}
	@Override
	public boolean equals(Object obj){
		Bar that=(Bar)obj;
		return this==that||incipits.equals(that.incipits);
	}
	public String toString(){
		return Debug.info(this)+" at="+at+" incipits="
		+ (true?incipits.size():"\n"+Objects.toLines(((Collection)incipits).toArray()));
	}
}
