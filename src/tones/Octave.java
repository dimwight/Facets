package tones;

public enum Octave {
    Above(0), AboveTwo(1), AboveThree(2),
    Below(-1), BelowTwo(-2), BelowThree(-3);
    public static final byte pitches = 7;
    public final byte pitchAt;

    Octave(int at) {
        pitchAt = (byte) (pitches * at);
    }

    public static Octave octave(int at) {
        switch (at) {
            case 0:
                return Above;
            case 1:
                return AboveTwo;
            case 2:
                return AboveThree;
            case -1:
                return Below;
            case -2:
                return BelowTwo;
            case -3:
                return BelowThree;
            default:
                throw new IllegalArgumentException("Invalid octave at=" + at);
        }
    }

    public Octave below() {
        switch (this) {
            case BelowTwo:
                return BelowThree;
            case Below:
                return BelowTwo;
            case Above:
                return Below;
            case AboveTwo:
                return Above;
            case AboveThree:
                return AboveTwo;
            default:
                throw new IllegalStateException("No octave below " + this);
        }
    }

    public Octave above() {
        switch (this) {
            case BelowThree:
                return BelowTwo;
            case BelowTwo:
                return Below;
            case Below:
                return Above;
            case Above:
                return AboveTwo;
            case AboveTwo:
                return AboveThree;
            default:
                throw new IllegalStateException("No octave above " + this);
        }
    }
}