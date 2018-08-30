package facets.util.geom;
import static facets.util.Util.*;
import static java.lang.Math.*;
import facets.util.Debug;
import facets.util.Doubles;
import facets.util.Util;
import java.io.Serializable;
/**
Abstract 2D point. 
 */
final public class Point implements Serializable{
  private Vector at;
  public int hashCode(){
  	return at.hashCode();
  }
  public Point(double x,double y){set(x,y);}
	public Point(double[]vals){
		this(vals[0],vals[1]);
		if(vals.length>2)throw new IllegalArgumentException(
				"Too many values length="+vals.length);
	}
  public Point(Point src){this(src.at.x,src.at.y);}
  public Point(Vector src){this(src.x,src.y);}
  public Vector at(){return new Vector(at);}
  public double distance(Point from){return at.minus(from.at).reach();}
  final public boolean eq(double a,double b){return Math.abs(a-b)<0.01;}
	public boolean equals(Object o){
		return o==null?false:at.equals(((Point)o).at);
	}
  public int intX(){return(int)at.x;}
  public int intY(){return(int)at.y;}
  public Vector jumpFrom(Point point){return at.minus(point.at);}
  public Point newPoint(){return new Point(this);}
  public void rotate(Point about,Angle angle){
    double cos=at.x-about.at.x,sin=at.y-about.at.y,r=Doubles.hypotenuse(cos,sin);
    if(angle.withinTolerance(Angle.NIL)||r<.00001)return;
    double addTan=r*angle.tan(),newR=Doubles.hypotenuse(addTan,r),
    newSin=sin+cos*addTan/r,newCos=cos-sin*addTan/r;
    newCos*=r/newR;newSin*=r/newR;
    if(angle.cos()<0){newCos*=-1;newSin*=-1;}
    set(about.at.x+newCos,about.at.y+newSin);
  }
  public void set(double x,double y){at=new Vector(x,y);}
  public void set(Vector to){set(to.x,to.y);}
  public void shift(Vector shift){set(at.plus(shift));}
  public Point shifted(Vector shift){return new Point(at.plus(shift));}
	public double storeX(){
		return fx(at.x);
	}
	public double storeY(){
		return fx(at.y);
	}
  public String toString(){return at.toString();}
	public double x(){
		checkNan();
		return at.x;
	}
	public double y(){
		checkNan();
		return at.y;
	}
  Angle bearing(Point from){return at.minus(from.at).bearing();}
	public double[]storeXY(){
		return new double[]{storeX(),storeY()};
	}
	public double[]rectangleValues(Point with){
		Vector from=jumpFrom(with);
		return new double[]{min(with.x(),x()),min(with.y(),y()),
			abs(from.x),abs(from.y)};
	}
	public double[]boxValues(Point with){
		Vector from=jumpFrom(with);
		double x=min(with.x(),x()),y=min(with.y(),y());
		return new double[]{x,y,x+abs(from.x),y+abs(from.y)};
	}
	private void checkNan(){
		if(at.x!=at.x||at.y!=at.y)throw new IllegalStateException("NaN in "+Debug.info(this));
	}
}
