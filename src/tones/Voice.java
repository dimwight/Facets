package tones;
import static tones.Octave.*;
import static tones.ScaleNote.*;
import java.util.Arrays;
import java.util.List;
public enum Voice{
	Empty(E,Above,"E",1),
	Soprano(E,Above,"S",1),
	Alto(C,Above,"A",-1),
	Tenor(A,Below,"T",1),
	Bass(D,Below,"B",-1);
	public final ScaleNote midNote;
	public final Octave octave;
	public final String code;
	public final boolean tailsUp;
	public final static List<Voice>voiceList=Arrays.asList(values());
	private Voice(ScaleNote midPitch,Octave octave,String code,int tailsUp){
		this.octave=octave;
		this.midNote=midPitch;
		this.code=code;
		this.tailsUp=tailsUp>0;
	}
}