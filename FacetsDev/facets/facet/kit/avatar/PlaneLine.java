package facets.facet.kit.avatar;
import facets.core.app.avatar.PainterSource;
import facets.core.app.avatar.PickPainter;
import facets.util.Debug;
import facets.util.geom.Line;
import facets.util.geom.Point;
import facets.util.geom.Vector;
import facets.util.shade.Shade;
abstract class PlaneLine extends Line implements PickPainter{
  protected int pattern;
  protected boolean patterned;
  private Shade paintColor;  
  private transient Line[]patternLines;
  private transient double scaleCheck;
  public PlaneLine(Line line,Shade color){
		super(line.from,line.to,color);
		paintColor=color;
	}
  public PlaneLine(Line line,Shade color,int pattern){
  	this(line,color);
  	this.pattern=pattern;patterned=pattern!=PainterSource.NO_PATTERN;    
  }
	public Object checkCanvasHit(Point canvasAt,double hitGap){
		return null;
	}
  public Shade color(){return color;}
  public abstract void paintInGraphics(Object graphics);
  public void setColor(Shade c){paintColor=color=c;}
  protected Line[]patternLines(PlaneCanvas canvas){
    double gap=canvas.unscale(5);
    if(patternLines!=null&&scaleCheck==gap)return patternLines;
    scaleCheck=gap;Vector leap=to.jumpFrom(from);
    if(canvas.scale(leap.reach())<1)return new Line[]{this};
    double leapReach=leap.reach(),
    	dashReach=pattern>0?gap*(pattern-1):gap,
    	gapReach=pattern>0?gap:gap*(-pattern-1);
    Vector dashJump=leap.scaled(dashReach/leapReach),
    	gapJump=leap.scaled(gapReach/leapReach),
      line=dashJump.minus(gapJump);
    patternLines=new Line[(int)(leapReach/dashReach)];
    if(patternLines.length==0)return new Line[]{this};
    patternLines[0]=new Line(from,new Point(from.at().plus(line)),color);
    for(int i=1;i<patternLines.length;i++){
      Point from=new Point(patternLines[i-1].from.at().plus(dashJump));
      patternLines[i]=new Line(from,new Point(from.at().plus(line)),color);
    }
    return patternLines;
  }
	public boolean paintScaled(Object graphics){
		return false;		
	}
}
