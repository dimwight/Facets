package facets.util.geom;
import facets.util.Debug;
import facets.util.Identified;
import facets.util.Util;
import facets.util.shade.Shade;
import facets.util.shade.Shades;
import java.io.Serializable;
import java.util.Arrays;
/**
Abstract 2D line. 
 */
public class Line implements Serializable,Identified{
	public Line(double[]doubles)
	  {this(new Point(doubles[0],doubles[1]),new Point(doubles[2],doubles[3]));}
	public Line(Point[]points){this(points[0],points[1]);}
	public final Point from,to;
	public Shade color=Shades.cyan;
	private static int lines;
	private final int line=lines++;
	private int hashCode=Integer.MAX_VALUE;
  public Line(Point from,Point to){this.from=from;this.to=to;}
  public Line(Point from,Point to,Shade color)
    {this.from=from;this.to=to;this.color=color;}
  public double length(){return to.distance(from);}
  public boolean equals(Object o){
    Line other=(Line)o;
    return other.color.equals(color)&&other.from.equals(from)
    	&&other.to.equals(to);
  }
  public String toString(){
		return Debug.info(this);
			//+" "+from+" "+to+" "+Doubles.sf(length(),3);
	}
  public int hashCode(){
  	return hashCode==Integer.MAX_VALUE?
				hashCode=Arrays.hashCode(new Object[]{from,to}):hashCode;
  }
	public Object identity(){
		return line;
	}
}
