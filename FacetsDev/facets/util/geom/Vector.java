package facets.util.geom;
import facets.util.Util;
import java.io.Serializable;
import java.util.Arrays;
/**
2D Cartesian/polar vector. 
 */
final public class Vector implements Serializable{
	public static int vectors;
	private final int vector;
	public final double x,y;
	private int hashCode=Integer.MAX_VALUE;
	public Vector(double x,double y){
		if(x!=x||y!=y) throw new IllegalArgumentException("Not a number");
		this.x=x;
		this.y=y;
		vector=vectors++;
	}
	public Vector(Vector src){
		this(src.x,src.y);
	}
	public Angle bearing(){
		return new Angle(y,x);
	}
	public boolean equals(Object o){
		Vector v=(Vector)o;
		return x==v.x&&y==v.y;
	}
	public Vector mean(Vector jump){
		return new Vector((x+jump.x)/2,(y+jump.y)/2);
	}
	public Vector minus(Vector jump){
		return new Vector(x-jump.x,y-jump.y);
	}
	public Vector plus(Vector jump){
		return new Vector(x+jump.x,y+jump.y);
	}
	public double reach(){
		return Math.sqrt(x*x+y*y);
	}
	public Vector scaled(double by){
		return new Vector(x*by,y*by);
	}
	public Vector scaled(Vector by){
		return new Vector(x*by.x,y*by.y);
	}
	public String toString(){
		return "{"+Util.sfs(x)+","+Util.sfs(y)+"}";
	}
	public int hashCode(){
		return hashCode==Integer.MAX_VALUE
				?hashCode=Arrays.hashCode(new Object[]{x,y}):hashCode;
	}
}
