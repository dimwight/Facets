package facets.util.geom;

import facets.util.Debug;
import facets.util.Doubles;
import facets.util.Util;

/**
Represents an angle. 
 */
final public class Angle implements java.io.Serializable{
  public  static final double CIRCLE_DEGREES=360,CIRCLE_RADIANS=Math.PI*2,
		RADIANS_PER_DEGREE=CIRCLE_RADIANS/CIRCLE_DEGREES;
  static int angles,antiTans;
  final static Angle PI=new Angle(Math.PI),HALF_PI=new Angle(Math.PI/2),
    NIL=new Angle(0),THIRD_PI=new Angle(Math.PI/3),TWO_PI=new Angle(Math.PI*2);  
  private double rad=Double.NaN,sin,cos;
  private final static double tolerance=0.001;
  /*private int quadrant(){
    if(cos()<0&&sin()>=0)return 1;
    else if(cos()<0&&sin()<0)return 2;
    else if(cos()>=0&&sin()<0)return 3;
    else return 0;
  }*/
  private final static int cPerRadian=100;
  private static final double tooClose=10/(double)cPerRadian;
  private static double []tans=new double[(int)(Math.PI/2*cPerRadian)+1];
  static{
    for(int radVal=0;radVal<tans.length;radVal++)
      tans[radVal]=Math.tan((double)radVal/cPerRadian);
  }
  public Angle(double radians){rad=radians;angles++;}
  public Angle(double sin,double cos){this(sin,cos,false);}
  Angle(double sin,double cos,boolean lookUp){
		this.sin=sin;
		this.cos=cos;
		if(lookUp) checkRad();
		angles++;
	}
  void add(double val){checkRad();rad+=val;}
  void add(Angle other){checkRad();other.checkRad();rad+=other.rad;}
  private static double antiTan(double sin,double cos){
    if(true)return Math.atan2(sin,cos);
    double radius=Doubles.hypotenuse(sin,cos);
    if(radius<tooClose)return 0;
    double rad=(double)radVal(sin,cos)/cPerRadian
      //,calcRad=calcRad(Math.abs(sin/radius))
      //,error=rad-calcRad
      ;
    //Util.printOut(rad+"\t"+Util.sf3(-error));
    if(cos<0&&sin>=0)rad=Math.PI-rad;
    else if(cos<0&&sin<0)rad=Math.PI+rad;
    else if(cos>=0&&sin<0)rad=Math.PI*2-rad;
    antiTans++;
    return rad;
  }
  Angle by(double divisor){checkRad();return new Angle(rad/divisor);}
  private static double calcRad(double sin){    
    return ((Math.PI/2-1)*sin+1)*sin;
  }
  private void checkRad(){
    if(rad!=rad)rad=antiTan(sin,cos);
    if(rad<0)rad+=Math.PI*2;
    if(rad!=rad)throw new IllegalStateException("rad!=rad in "+Debug.info(this));
  }
  double cos(){checkRad();return Math.cos(rad);}
  void set(Angle other){rad=other.rad;}
  Angle reverse(){return new Angle(rad+Math.PI);}
  Angle times(double multiple){checkRad();return new Angle(rad*multiple);}
  boolean isNegative(){return radians()<0;}
  public boolean equals(Object o){return radians()==((Angle)o).radians();}
  static void initialise(){double a=antiTan(1,1);}
  private Angle lessThanPI(){
    checkRad();
    double shift=(rad>0?Math.PI:-Math.PI);
    rad+=shift;rad%=Math.PI*2;rad-=shift;rad%=Math.PI;
    return this;
  }
  static void main(String[]args){
     double rad=0,inc=.05;
     for(;rad<Math.PI/2;rad+=inc)
       antiTan(Math.sin(rad),Math.cos(rad));
     System.exit(0);    
   }
  Angle mean(Angle other){
		checkRad();
		other.checkRad();
		return new Angle((rad+other.rad)/2);
	}
  Angle minus(Angle other){
		checkRad();
		other.checkRad();
		return new Angle(rad-other.rad);
	}
  Angle neg(){
		if(rad!=rad) checkRad();/*return new Angle(-sin,cos);else */
		return new Angle(-rad);
	}
  Angle newAngle(){
		if(rad!=rad) return new Angle(sin,cos);
		return new Angle(rad);
	}
  Angle plus(Angle other){
		checkRad();
		other.checkRad();
		return new Angle(rad+other.rad);
	}
  public double radians(){checkRad();return true?rad:lessThanPI().rad;}
  private static int radVal(double sin,double cos){
	  boolean positive=(cos>=0&&sin>=0)||(cos<0&&sin<0);
	  Double tan=sin/cos*(positive?1:-1);
	  if(Double.isNaN(tan))tan=
	  	positive?Double.POSITIVE_INFINITY:Double.NEGATIVE_INFINITY;
	  int stop=tans.length,jump=stop/2,at=jump,passes=0;
	  boolean inRange;
		do{
	  	passes++;
	    if((jump/=2)==0)jump=1;
	    Double test=tans[at];
	    if(tan.compareTo(test)>0&&at<stop)at+=jump;
	    else if(at>0)at-=jump;
	    inRange=at>=0&&at<stop;
	  }while(inRange&&(tan.compareTo(tans[at])>0||tan.compareTo(tans[at-1])<0));
	  return!inRange?-1:at;
	}
  void set(double radians){rad=radians;}
  double sin(){checkRad();return Math.sin(rad);}
  double tan(){checkRad();return Math.tan(rad);}
  static Angle tiny(){return new Angle(0.00001);}
  public String toString(){return Util.fx(radians())+"";}
  boolean withinTolerance(Angle other){return withinTolerance(other,tolerance);}
  boolean withinTolerance(Angle other,double tolerance){
		checkRad();
		other.checkRad();
		return Math.abs(radians()-other.radians())<tolerance;
	}
  /**
  Convert radians to degrees. 
   */
	public static double toDegrees(double radians) {
		return radians / RADIANS_PER_DEGREE;
	}
  /**
  Convert degress to radians. 
   */
	final public static double toRadians(double degrees) {
		return degrees * RADIANS_PER_DEGREE;
	}
}
