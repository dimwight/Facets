package facets.facet.kit.avatar;
import static facets.util.Objects.*;
import facets.core.app.SView;
import facets.core.app.SViewer;
import facets.core.app.avatar.AvatarContent;
import facets.core.app.avatar.AvatarPolicy;
import facets.core.app.avatar.AvatarView;
import facets.core.app.avatar.Painter;
import facets.core.app.avatar.PainterSource;
import facets.core.app.avatar.PickPainter;
import facets.core.app.avatar.Pickable;
import facets.core.app.avatar.Painter.Style;
import facets.util.Debug;
import facets.util.Objects;
import facets.util.Tracer;
import facets.util.Util;
import facets.util.geom.Point;
import facets.util.geom.Vector;
final class Avatar extends Tracer implements Pickable,Snappable{
	final AvatarContent content;
	private final AvatarCanvas canvas;
	final AvatarPolicy policy;
	private boolean selected;
	private Painter[]loPainters,hiPainters;
	private SView thenView;
	private int cursorThen=Integer.MAX_VALUE;
	Avatar(AvatarCanvas canvas,AvatarContent content, AvatarPolicy policy){
		this.canvas=canvas;
		this.content=content;
		this.policy=policy;
	}
	public Object checkCanvasHit(Point canvasAt,double hitGap){
		Object hit=null;
		PickPainter[]hitPainters={};
		if(content instanceof Pickable)
			hit=((Pickable)content).checkCanvasHit(canvasAt,hitGap);
		else{
			Pick hitPick=canvas.checkPickables(Objects.newTyped(Pickable.class,
					fetchPainters(selected,canvas.viewer().isActive())),
					canvasAt,hitGap);
			if(hitPick!=null){
				hit=hitPick.hit;
				hitPainters=hitPick.painters;
			}
		}
		int cursor=(Integer)policy.stateCursor(hit==null?Painter.Style.Plain
				:!selected?Painter.Style.Picked:Painter.Style.PickedSelected);
		if(cursorThen!=cursor)canvas.host.setCanvasCursor(cursorThen=cursor);
		if(hit==null){
			policy.avatarNotPicked();
			return null;
		}
		PickPainter[]painters=newTyped(PickPainter.class,policy.newPickPainters(hit,selected));
		return new Pick(this,hit,hitPainters==null?painters
				:join(PickPainter.class,hitPainters,painters));
	}
	public Vector checkSnap(AvatarCanvas canvas,Point check,double snapGap){
		Snappable[]snappables=fetchSnappables(selected);
		Vector snap=null;
		for(int i=0;i<snappables.length&&snap==null;i++)
			snap=snappables[i].checkSnap(canvas,check,snapGap);
		return snap;
	}
	private Snappable[]fetchSnappables(boolean selected){
		return Objects.newTyped(Snappable.class,
				fetchPainters(selected,canvas.viewer().isActive()));
	}
	public Painter[]getPainters(){
		return fetchPainters(selected,canvas.viewer().isActive());
	}
	private Painter[]fetchPainters(boolean selected,boolean active){
		Painter[]painters=selected?
			hiPainters==null?
				hiPainters=policy.newViewPainters(true,active):hiPainters
			:loPainters==null?
				loPainters=policy.newViewPainters(false,active):loPainters;
		if(painters==null)throw new IllegalStateException(
				"Null painters in "+Debug.info(this));
		return painters;
	}
	private void destroyPainters_(){
		loPainters=hiPainters=null;
	}
	boolean isSelected(){
		return selected;
	}
	void setSelected(boolean on){
		selected=on;
	}
	public String toString(){
		return Debug.info(this)+": "+Debug.info(content)+" selected="+selected;
	}
}
