package tones;
public enum Interval{First,Second,Third,Fourth,Fifth,Sixth,Seventh;
	public static Interval between(Tone t1,Tone t2){
		int gap=Math.abs(t1.pitch-t2.pitch)%7;
		switch(gap){
		case 0:return First;
		case 1:return Second;
		case 2:return Third;
		case 3:return Fourth;
		case 4:return Fifth;
		case 5:return Sixth;
		case 6:return Seventh;
		default:throw new IllegalStateException("Impossible gap="+gap);
		}
	}
	public boolean isDissonant(Tone sounding){
		return this==Second||this==Seventh||(this==Fourth&&sounding.voice==Voice.Bass);
	}
}
