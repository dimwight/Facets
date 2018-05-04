package facets.core.app.avatar;
import facets.util.Titled;
import facets.util.geom.Point;
public abstract class AvatarPart<C>implements PickPainter,Titled{
	private final PainterSource p;
	private final String type;
	public AvatarPart(PainterSource p,String type){
		this.p=p;
		this.type=type;
	}
	@Override
	final public Object checkCanvasHit(Point canvasAt,double hitGap){
		return newPickPainter(p,true).checkCanvasHit(canvasAt,hitGap)!=null?this:null;
	}
	@Override
	final public void paintInGraphics(Object graphics){
		newPickPainter(p,false).paintInGraphics(graphics);
	}
	@Override
	public String title(){
		return type;
	}
	protected abstract PickPainter newPickPainter(PainterSource p,boolean pickable);
	public abstract int pickCursor();
	public abstract void adjustContentForDrag(C content,Point dragAt);
}