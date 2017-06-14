package facets.facet.kit.avatar;
import facets.core.app.avatar.Painter;
import facets.core.app.avatar.PainterSource;
import facets.core.app.avatar.PickPainter;
import facets.util.Debug;
import facets.util.Util;
import facets.util.geom.Line;
import facets.util.geom.Point;
import facets.util.geom.Vector;
import facets.util.shade.Shade;
final class PlaneSnapLine extends Line implements PickPainter,Snappable{
  private final Painter painter;
  private final Shade shade;
  private final byte pattern=true?PainterSource.NO_PATTERN:25;
	private final PlaneCanvas canvas;
  private static Point seekHitPoint(Point from,Point to,Point test,
  		double missGap){
    double left=Math.min(from.x(),to.x())-missGap,
    	right=Math.max(from.x(),to.x())+missGap,
      bottom=Math.min(from.y(),to.y())-missGap,
      top=Math.max(from.y(),to.y())+missGap;
    boolean hit=test.x()>=left&&test.x()<right&&test.y()>=bottom&&test.y()<top;
    if(!hit)return null;
    Point hitAt,midAt=new Point(from.at().mean(to.at()));
    return midAt.distance(from)<missGap?hitAt=midAt
	    :(hitAt=seekHitPoint(from,midAt,test,missGap))!=null?hitAt
	    :seekHitPoint(midAt,to,test,missGap);
  }
  PlaneSnapLine(Line src,Shade shade,PainterSource source,PlaneCanvas canvas){
    super(src.from,src.to);
    this.shade=shade;
    this.canvas=canvas;
    painter=((SwingPainterSource)source).line(src,shade,pattern,false);
  }
  public Object checkCanvasHit(Point canvasAt,double hitGap){
    if(hitGap<0)throw new IllegalArgumentException("hit gap in "+Debug.info(this));
    Point hit=seekHitPoint(from,to,canvasAt,hitGap);if(hit==null)return null;
    double along=hit.distance(from)/length(),
    leapGap=canvas.markLeap();
    Point pickAt=hit.distance(from)<leapGap?from
  		:hit.distance(to)<leapGap?to
  		:new Point(from.at().plus(to.jumpFrom(from).scaled(along)));
    return new Pick(this,pickAt,null);
  }
  public Vector checkSnap(AvatarCanvas canvas,Point check,double snapGap){
  	if(false)Util.printOut("PlaneSnapLine.checkPointSnap: ",
  			(Util.fx(from.distance(check))));
  	return from.distance(check)<snapGap&&!check.equals(from)?
    	from.jumpFrom(check)
    :to.distance(check)<snapGap&&!check.equals(to)?
    	to.jumpFrom(check)
    :null;
  }
  public void paintInGraphics(Object graphics){
    ((PickPainter)painter).paintInGraphics(graphics);
  }
}
