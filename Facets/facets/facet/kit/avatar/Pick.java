package facets.facet.kit.avatar;
import facets.core.app.avatar.AvatarContent;
import facets.core.app.avatar.PickPainter;
import facets.util.Debug;
import facets.util.geom.Point;
import java.awt.event.MouseEvent;
class Pick{
	final PickPainter[]painters;
	final Object picked,hit;
	Pick(Object picked,Object hit,PickPainter[]painters){
		this.picked=picked;
		this.hit=hit;
		this.painters=painters;
	}
	public String toString(){
		return Debug.info(this)+": "+Debug.info(picked)+", "+hit;
	}
	PickPainter[]respondToMouse(AvatarCanvas canvas,Point at,int eventVals){
		return picked instanceof Avatar&&eventVals!=MouseEvent.MOUSE_EXITED?
				painters:null;
	}
	static Pick newNullPick(Object source){
		return new Pick(source,null,null){
			public PickPainter[]respondToMouse(AvatarCanvas canvas,Point at,int eventVals){
				switch(eventVals){
				case MouseEvent.MOUSE_ENTERED://defaultsWait.start();
					break;
				case MouseEvent.MOUSE_EXITED://defaultsWait.stop();
					break;
				}
				return null;
			}
		};
	}
}