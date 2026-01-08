package tones.page;
import static tones.Tone.*;
import static tones.page.PageNote.Dot.*;
import facets.util.geom.Line;
import facets.util.geom.Point;
import facets.util.geom.Vector;
import java.util.Arrays;
import tones.Clef;
import tones.Tone;
import tones.bar.Incipit;
public class PageNote extends PageItem{
  public enum Dot{NONE,LEVEL,BELOW,ABOVE};
  public final Tone tone;
  public final PageIncipit incipit;
  public final double pageX,pageY,ledgerLineShift;
  public final int ledgerLines;
  public boolean selected;
  public final Line tail,tailSwag;
  public final Vector at;
  public final Dot dotAt;
  final PageBar bar;
  private final String debugString;
  private final int[]intValues;
  PageNote(PageBar bar,Tone tone,PageIncipit incipit,double barPageY,Clef clef, 
      boolean selected){
    this.bar=bar;
    this.tone=tone;
		this.incipit=incipit;
    intValues=new int[]{tone.pitch,tone.beats};
    this.selected=selected;
    pageX=incipit.tonePageX(tone);
    final int stavePitch=tone.pitch-clef.staveMidPitch,
      staveToMidPitch=STAVE_GRID/2-1;
    pageY=barPageY+staveToMidPitch-stavePitch;
    at=new Vector(pageX,pageY);
    boolean aboveMidPitch=stavePitch>0;
    int beyondStave=Math.abs(stavePitch)-staveToMidPitch;
    ledgerLines=beyondStave<=0?0
        :beyondStave/2*(aboveMidPitch?1:-1);
    ledgerLineShift=aboveMidPitch?beyondStave%2+1:(beyondStave+1)%2;
    boolean tailsUp=tone.voice.tailsUp;
    dotAt=tone.beats%3!=0?NONE:stavePitch%2==0&&ledgerLines==0?ABOVE:LEVEL;
    debugString=tone.pitchNote()+
      " stavePitch="+stavePitch+" aboveMidPitch="+aboveMidPitch
      +" beyondStave="+beyondStave;
    double tailHeight=tone.beats>NOTE_QUARTER?4.7:5;
    Point tailFrom=new Point(tailFrom(tone.beats,tailsUp
        ).at().scaled(scaleToNoteWidth)),
      tailTo=tailFrom.shifted(new Vector(0,tailHeight*(tailsUp?-1:1)));
    tail=tone.beats>3*NOTE_QUARTER?null:new Line(tailFrom,tailTo);
    tailSwag=tone.beats>NOTE_EIGHTH?null
        :new Line(tailTo,tailTo.shifted(new Vector(3,5)));
  }
  public Point tailFrom(short note,boolean tailsUp){
    return tailsUp?
        note>NOTE_QUARTER?new Point(0.9,-0.5):new Point(0.82,-0.2)
        :note>NOTE_QUARTER?new Point(0.12,0.3):new Point(0.15,0.1);
  }
  public String toString(){
    return false?debugString:tone.toString()+" "+staveAt();
  }
  public int hashCode(){
    return Arrays.hashCode(intValues);
  }
  public boolean equals(Object obj){
    if(true)throw new RuntimeException("Untested");
    PageNote that=(PageNote)obj;
    return this==that||Arrays.equals(intValues,that.intValues);
  }
  public Vector staveAt(){
    return new Vector(pageX,pageY);
  }
}
