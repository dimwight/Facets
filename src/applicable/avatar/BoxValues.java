package applicable.avatar;
import static facets.util.Doubles.*;
import static facets.util.Util.*;
import static facets.util.tree.Nodes.*;
import static facets.util.tree.TypedNode.*;
import static java.lang.Math.*;
import facets.util.Debug;
import facets.util.Stateful;
import facets.util.Strings;
import facets.util.tree.TypedNode;
import facets.util.tree.ValueNode;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.util.Arrays;
public class BoxValues extends AvatarValues{
	public static final String KEY_LEFT="left",KEY_TOP="top",KEY_BOTTOM="bottom",
		TYPE_BOX="Box",TYPE_RECT="Rect",KEY_RIGHT="right",KEY_AT="at",KEY_SIZE="size",
		TEST_ARGS=true?"Box=11,9,21,19 Rect=10,7,10,10"
			:"Box1=2,2,18,15 Box2=10,4,22,23 Rect1=14,8,13,14";
	public static ValueNode newCheckedPairData(String pair){
		String splits[]=splitPair(pair),key=splits[0];
		if(!key.startsWith(TYPE_BOX)&&!key.startsWith(TYPE_RECT))return null;
		String type=key.startsWith(TYPE_BOX)?TYPE_BOX:TYPE_RECT;
		return newValues(type,key.replace(type,""),Strings.toDoubles(splits[1]));
	}
	public static ValueNode newValues(String type,String title,double[]vals){
		ValueNode data=new ValueNode(type,title.trim().equals("")?UNTITLED:title);
		putValues(data,type.equals(TYPE_BOX)?vals
			:new double[]{vals[RectX],vals[RectY],vals[RectX]+vals[RectW],vals[RectY]+vals[RectH]});
		return new BoxValues(data).data;
	}
	private static String normalText(double[]vals){
		return Strings.fxString(vals);
	}
	private static String normalText(double val){
		return fxs(val);
	}
	final public String newDataPair(){
		double[]vals=data.type().equals(TYPE_BOX)?new double[]{left,top,right,bottom}
			:new double[]{left,top,width(),height()};
		return title()+"="+normalText(vals)+"\n";
	}
	private static void putValues(ValueNode data,double[]vals){
		String type=data.type();
		if(type.equals(TYPE_BOX)){
			data.put(KEY_LEFT,normalText(vals[BoxL]));
			data.put(KEY_TOP,normalText(vals[BoxT]));
			data.put(KEY_RIGHT,normalText(vals[BoxR]));
			data.put(KEY_BOTTOM,normalText(vals[BoxB]));
		}
		else if(type.equals(TYPE_RECT)){
			data.put(KEY_AT,normalText(new double[]{vals[RectX],vals[RectY]}));
			data.put(KEY_SIZE,normalText(new double[]{vals[RectW]-vals[RectX],
					vals[RectH]-vals[RectY]}));
		}
		else throw new IllegalArgumentException("Invalid "+type);
	}
	private void putValues(){
		putValues(data,new double[]{left,top,right,bottom});
	}
	void normalise(){
		double left=this.left,top=this.top,right=this.right,bottom=this.bottom;
		this.left=min(left,right);
		this.right=max(left,right);
		this.top=min(top,bottom);	
		this.bottom=max(top,bottom);
		putValues();
	}
	private double left,top,right,bottom;
	public BoxValues(ValueNode data){
		super(data);
		if(data.type().equals(TYPE_RECT)){
			double[]at=data.getDoubles(KEY_AT),size=data.getDoubles(KEY_SIZE);
			left=at[0];
			top=at[1];
			right=left+size[0];
			bottom=top+size[1];
		}
		else{
			left=data.getDouble(KEY_LEFT);
			top=data.getDouble(KEY_TOP);
			right=data.getDouble(KEY_RIGHT);
			bottom=data.getDouble(KEY_BOTTOM);
		}
	}
	final public ValueNode normalisedData(){
		BoxValues copy=new BoxValues((ValueNode)data.copyState());
		copy.normalise();
		return copy.data;
	}
	final public double left(){
		return left;
	}
	final public double top(){
		return top;
	}	
	final public double right(){
		return right;
	}
	final public double bottom(){
		return bottom;
	}
	final public double width(){
		return abs(right-left);
	}
	final public double height(){
		return abs(bottom-top);
	}
	final public void setLeft(double x){
		left=x;
		putValues();
	}
	final public void setTop(double y){
		top=y;
		putValues();
	}
	final public void setRight(double x){
		right=x;
		putValues();
	}
	final public void setBottom(double y){
		bottom=y;
		putValues();
	}
	final public void setWidth(double width){
		right=left+width;
		putValues();
	}
	final public void setHeight(double height){
		bottom=top+height;
		putValues();
	}
	final public boolean equals(Object o){
		Stateful data=o instanceof ValueNode?(ValueNode)o:((BoxValues)o).data;
		return this.data.stateEquals(data);
	}
	final public int hashCode(){
		return Arrays.hashCode(new double[]{left,top,right,bottom});
	}
	public String toString(){
		return Debug.info(this)+" "+data;
	}
	@Override
	public String title(){
		String title=data.title();
		return data.type()+(title.equals(UNTITLED)?"":title);
	}
	private RectangularShape toAwt_(){
		return new Rectangle2D.Double(left,top,width(),height());
	}
}
