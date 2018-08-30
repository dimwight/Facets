package facets.facet.kit.avatar;
import facets.core.app.avatar.Painter;
import facets.core.app.avatar.PainterSource;
import facets.util.StringFlags;

import java.awt.Dimension;
import javax.swing.JComponent;
import javax.swing.JPanel;
class SwingPlaneMaster extends SwingAvatarMaster implements PlaneCanvas.PlaneHost{
	SwingPlaneMaster(PlaneCanvas canvas){
		super(canvas);
	}
	final public PainterSource newPainterSource(){
		return new SwingPainterSource((PlaneCanvas)canvas,avatarPane());
	}
	public void adjustToPlane(int planeWidth,int planeHeight,int viewerLeft,
			int viewerTop){
		Dimension planeSize=new Dimension(planeWidth,planeHeight);
		JComponent canvas=findCanvasPane();
		if(!canvas.getSize().equals(planeSize)){
			canvas.setPreferredSize(planeSize);
			canvas.revalidate();
		}
	}
}