package facets.util;
import static facets.util.Util.*;
import static java.lang.Math.*;
import java.awt.Color;
import java.text.DecimalFormat;
import java.text.NumberFormat;
/**
Floating-point utilities. 
<p>{@link Doubles} extends its superclass by
 */
public final class Doubles{
	/** Convenience index into geometry values. */
	final public static int BoxL=0,BoxT=1,BoxR=2,BoxB=3,RectX=0,RectY=1,RectW=2,RectH=3,SizeW=0,SizeH=1;
	public static int DIGITS_SF=3,DECIMALS_FX=2;
	public static void main(String[]args){
		DIGITS_SF=3;
		for(int range=3,shift=range;shift>-range;shift--){
			double val=3.45678*pow(10,shift)*(true?-1:1);
			Util.printOut("sf" +DIGITS_SF+"("+val+")="+Util.sfs(val));
		}
	}
	static double sigFigs(double val,int digits){
		if(digits<0)throw new IllegalArgumentException("Digits <1="+digits);
		else if(Double.isInfinite(val))throw new IllegalArgumentException("Infinite value");
		else if(digits==0||val==0||val!=val)return val;
		double ceiling=pow(10,digits),floor=ceiling/10,signum=signum(val),sf=abs(val);
		if(sf<1E-3)return 0;
		boolean shiftUp=sf<floor;
		double factor=shiftUp?10:0.1;
		int shifted=0;
		for(;sf>ceiling||sf<floor;shifted+=shiftUp?1:-1)sf*=factor;
		double exp=pow(10,shifted);
		if(false)Util.printOut("Doubles.sigFigs: shiftUp="+shiftUp+" shifted="+shifted);
		sf=rint(sf)/exp;
		if(true||!shiftUp)return sf*signum;
		NumberFormat trimmer=DecimalFormat.getInstance();
		trimmer.setMaximumFractionDigits(shifted);
		double trim=Double.valueOf(trimmer.format(sf));
		if(trim!=sf)Util.printOut("Doubles.sigFigs: val="+sf+" trim="+trim);
		return trim*signum;
	}
	static double fixed(double val,int decimals){
		if(decimals<0)throw new IllegalArgumentException("Decimals <0="+decimals);
		double shift=pow(10,decimals),rint=rint(val*shift);
		rint/=shift;
		return rint;
	}
	static double stepped(double[]steps,double[]stepped,double val){
		int at=0;for(double step:steps)
			if(val<step)return stepped[at];
			else at++;
		return stepped[at];
	}
	public static Double[]toObjects(double[]array){
		if(array==null) return null;
		Double[]doubles=new Double[array.length];
		for(int i=0;i<doubles.length;i++)
			doubles[i]=new Double(array[i]);
		return doubles;
	}
	public static double[]toArray(Object[]objects){
		double[]array=new double[objects.length];
		for(int i=0;i<array.length;i++)
			array[i]=((Double)objects[i]).doubleValue();
		return array;
	}
	public static double hypotenuse(double a,double b){
		return sqrt(a*a+b*b);
	}
	public static double[]fromFloats(float[]in){
		double[]out=new double[in.length];
		for(int i=0;i<out.length;i++)out[i]=in[i];
		return out;
	}
	private final double vals[];
	public Doubles(double[]vals){
		this.vals=vals;
	}
	public double mean(){
		double all=0;
		for(int i=0;i<vals.length;i++)all+=vals[i];
		return all/vals.length;
	}
	public String toString(){
		return sf(mean())+", "+sf(rms())+", "+sf(mean()/rms());
	}
	private double rms(){
		double all=0,floor=0;
		for(int i=0;i<vals.length;i++)if(vals[i]<floor)floor=vals[i];
		if(floor<0)for(int i=0;i<vals.length;i++)vals[i]-=floor;
		for(int i=0;i<vals.length;i++)all+=sqrt(vals[i]);
		all/=vals.length;
		return all*all+floor;
	}
	private static double[]join_(double[]front,double[]back){
		if(front==null)return back;
		else if(back==null)return front;
		double[]join=new double[front.length+back.length];
		System.arraycopy(front,0,join,0,front.length);
		System.arraycopy(back,0,join,front.length,back.length);
		return join;
	}
	private static int[]asInts_(double[]in){
		int[]out=new int[in.length];
		for(int i=0;i<out.length;i++)out[i]=(int)rint(in[i]);
		return out;
	}
	private static boolean equal_(double a,double b,double tolerance){
		return abs(a-b)<tolerance;
	}
	private static double interpolate_(double from,double to,double ratio){
		return from+(to-from)*ratio;
	}
	private double weightedMean_(double[]weights){
		if(weights.length!=vals.length)throw new IllegalArgumentException(Debug.info(this));;
		double all=0,allWeights=0;
		for(int i=0;i<vals.length;i++){
			all+=vals[i]*weights[i];
			allWeights+=weights[i];
		}
		return all/allWeights;
	}
	private static double maxAbs_(double a,double b){
		return abs(a)>abs(b)?a:b;
	}
	private double meanOfAbsolutes_(){
		double all=0;
		for(int i=0;i<vals.length;i++)all+=abs(vals[i]);
		return all/vals.length;
	}
}
