package tones;
import static tones.Octave.*;
import static tones.ScaleNote.*;
import facets.util.Util;
public enum Clef{
	TREBLE(B.octaved(Above),0),
	BASS(D.octaved(Below),1);
	public final byte staveMidPitch;
	public final int staveAt;
	private Clef(byte staveMidPitch,int staveAt){
		this.staveMidPitch=staveMidPitch;
		this.staveAt=staveAt;
		if(false)Util.printOut("Clef: "+this+" staveMidPitch="+ScaleNote.pitchNote(staveMidPitch));
	}
	public static Clef forVoice(Voice voice){
		return voice==Voice.Soprano||voice==Voice.Alto?Clef.TREBLE:Clef.BASS;
	}
}