package facets.core.app.avatar;
import static java.lang.Integer.*;
import facets.util.Tracer;
import facets.util.geom.Line;
import facets.util.geom.Point;
import facets.util.shade.Shade;
import facets.util.shade.Shades;
import facets.util.tree.Nodes;
import facets.util.tree.ValueNode;
import java.util.Arrays;
/**
Factory for creating and transforming avatar painters. 
<p>A {@link PainterSource} defines methods for use by 
{@link facets.core.app.avatar.AvatarPolicy} and {@link facets.core.app.avatar.DragPolicy} that
<ul>
<li>create {@link Painter} instances 
<li>create and apply {@link Transform} instances
</ul> 
<p>Instances of {@link PainterSource} can be supplied by the facet builder via
viewer facets. 
 */
public abstract class PainterSource extends Tracer{
	final public static int NO_PATTERN=0,HAIRLINE=-1;
	public static final String 
		KEY_SHADE_PEN="shadePen",
		KEY_SHADE_FILL="shadeFill",
		KEY_SHADE="shade",
		KEY_BACKGROUND="background",
		KEY_ANCHOR="anchor",
		KEY_POINTS="points",
		KEY_FACE="face",
		KEY_STYLE="style",
		KEY_PICKABLE="pickable";
  public static ValueNode newValues(String...args){
		try{
			ValueNode values=new ValueNode("PainterValues",args);
			Nodes.readAdjustedArgs(values,args);
			return values;
		}catch(Exception e){
			throw new RuntimeException("Bad contents="+args,e);
		}
	}
	public Painter textCaption(String text,double x,double y,String...args){
		return mastered(new PainterMaster.Textual(newValues(args),text,x,y));
	}
	public abstract Painter rectangle(double x,double y,double width,double height,
			String values);
	public Painter bar(double x,double y,double length,double width,String...args){
		ValueNode v=newValues(args);
		return bar(x,y,length,width,Shades.named(v.getString(KEY_SHADE_FILL)),
				v.getBoolean(KEY_PICKABLE));
	}
	final public Painter rectangle(double x,double y,double width,double height,Shade pen){
		return rectangle(x,y,width,height,"shadePen="+pen.title());
	}
	public abstract Painter bar(double x,double y,double across,double down,Shade fill,
	boolean pickable);
	public abstract Painter textTooltip(String text,double x,double y);
	public abstract Painter textOutline(String text,String face,int points,boolean bold,
			boolean italic,Shade fill,Shade pen);
	public abstract double textLength(String text,String face,int size,boolean bold,boolean italic);
	public abstract Painter stretchMark(double height,double length,double width,Shade fill);
	public abstract Painter turnMark(double size,double width,Shade fill);
	/**
	Create a (maybe pickable) painter to mark a position. 
	<p>The appearance of the painter is defined by the implementation. 
	@param at the position
	@param fill for the painting
	@param pickable should the mark be pickable?
	 */
	public abstract Painter pointMark(Point at,Shade fill,boolean pickable);
	/**
	Create a possibly-pickable line painter. 
	@param line to be painted
	@param shade for the painting
	@param style defines the pattern or stroke
	@param pickable defines line behaviour
	 */
	public abstract Painter line(Line line,Shade shade,int style,boolean pickable);
	public abstract Painter mastered(PainterMaster master);
	/**
	Create non-pickable painter of the lines passed. 
	@param lines will be painted at minimum width 
	@param shade for the painting
	 */
	public abstract Painter backgroundLines(Line[]lines,Shade shade);
	public abstract Painter marquee(Point anchorAt,Point dragAt,Shade shade);
	/**
	Marks an immutable transform.   
	<p>{@link Transform} provides type safety
	while manipulating transforms created by the <i>transformX</i> methods 
	of {@link PainterSource} before they are passed to 
	{@link PainterSource#applyTransforms(facets.core.app.avatar.PainterSource.Transform[],
		boolean, Painter...)}. 
	 */
	public interface Transform{}
	public abstract Transform transformTurn(double radians,double atX,double atY);
	public abstract Transform transformScale(double x, double y);
	public abstract Transform transformAt(double x,double y);
	/**
	Apply transforms to painters. 
	@param transforms should be applied to each member of <code>painters</code>
	@param painters to be transformed
	 */
	public abstract void applyTransforms(Transform[]transforms,boolean concatenate,
			Painter...painters);
	private int newHashes;
	public Painter bundle(final Painter[]painters,final Object...hashValues){
		return new PickPainter(){
			public Object checkCanvasHit(Point canvasAt,double hitGap){
				for(Painter p:painters){
					Object hit=p instanceof Pickable?
							((Pickable)p).checkCanvasHit(canvasAt,hitGap):null;
					if(hit!=null)return hit;
				}
				return null;
			}
			int hashCode=MAX_VALUE;
			public void paintInGraphics(Object graphics){
				for(Painter painter:painters)painter.paintInGraphics(graphics);
			}
			public int hashCode(){
				return hashCode==MAX_VALUE?
						hashCode=false?newHashes++
							:Arrays.hashCode(hashValues.length>0?hashValues:painters)
					:hashCode;
			}
			public boolean equals(Object o){
				return hashCode()==o.hashCode();
			}
		};
	}
}