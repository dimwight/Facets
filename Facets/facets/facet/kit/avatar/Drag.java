package facets.facet.kit.avatar;
import facets.core.app.SViewer;
import facets.core.app.avatar.AvatarContent;
import facets.core.app.avatar.AvatarView;
import facets.core.app.avatar.DragPolicy;
import facets.core.app.avatar.PainterSource;
import facets.core.app.avatar.DragPolicy.Constraints;
import facets.util.Debug;
import facets.util.geom.Point;
import facets.util.geom.Vector;
import java.awt.event.MouseEvent;
final class Drag{
	final Avatar[]avatars;
	final private Point anchorAt;
	final private DragConstraints constraints;
	final private AvatarContent[]content;
	final private DragPolicy policy;
	private final boolean doSnap;
	private Point thenSnapped;
	final boolean hasPolicy;
	Drag(AvatarView view,Avatar[]avatars,Point anchorAt,Object hit, 
			PainterSource hostPainters){
		this.avatars=avatars;
		this.anchorAt=anchorAt;
		content=new AvatarContent[avatars.length];
		for(int i=0;i<content.length;i++)
			content[i]=avatars[i].content;
		policy=view.avatars().dragPolicy(view,content,hit,hostPainters);
		hasPolicy=policy!=null;
		if(!hasPolicy){
			doSnap=false;constraints=null;
			return;
		}
		doSnap=policy.checkSnap();
		Constraints type=policy.constraints();
		int gap=10;
		constraints=type==DragPolicy.Constraints.Cross?
				DragConstraints.newCross(gap)
			:type==DragPolicy.Constraints.ThreeAxial?
					DragConstraints.newThreeAxial(gap)
			:type==DragPolicy.Constraints.SixAxial?
					DragConstraints.newSixAxial(gap)
						:null;
	}
	public Object[]respondToMouse(AvatarCanvas canvas,Point mouseAt,int eventVals, 
			PainterSource painters){
		Point dragAt=constraints==null?mouseAt:
			constraints.constrain(anchorAt,mouseAt);
		Vector snapShift=null;
		if(doSnap){
			PlaneCanvas plane=(PlaneCanvas)canvas;
			double snapGap=plane.markLeap();
			snapShift=plane.checkSnap(dragAt,snapGap);
			if(snapShift!=null&&thenSnapped==null)
				thenSnapped=new Point(dragAt.at().plus(snapShift));
			else if(thenSnapped!=null){
				double reachCheck=thenSnapped.jumpFrom(dragAt).reach()-snapGap;
				if(reachCheck>0)thenSnapped=null;
				else snapShift=new Vector(0,0);
			}		
		}
		switch(eventVals&0xFFF){
		case MouseEvent.MOUSE_DRAGGED:
			canvas.host.setCanvasCursor(constraints==null?(Integer)
					policy.stateCursor(DragPolicy.Constraints.None):
						constraints.dragCursor());
			return doSnap?policy.newSnapPainters(anchorAt,dragAt,snapShift)
				:policy.newDragPainters(anchorAt,dragAt);
		case MouseEvent.MOUSE_RELEASED:
			canvas.host.setCanvasCursor(SViewer.CURSOR_DEFAULT);
		case AvatarCanvas.MOUSE_DRAG_NOTIFY:
			return mouseAt.equals(anchorAt)?null:doSnap?
					policy.newSnapDropEdits(anchorAt,dragAt,snapShift)
					:policy.newDragDropEdits(anchorAt,dragAt);
		default:
			throw new IllegalArgumentException("Bad event values in "+Debug.info(this));
		}
	}
}