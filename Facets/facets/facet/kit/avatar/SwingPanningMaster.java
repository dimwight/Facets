package facets.facet.kit.avatar;
import static facets.core.superficial.app.SViewer.*;
import facets.facet.AreaFacets;
import facets.util.Debug;
import facets.util.StringFlags;


import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
final class SwingPanningMaster extends SwingPlaneMaster{
	private static class CentringLayout extends FlowLayout{
		private CentringLayout(){
			super(FlowLayout.CENTER,0,0);
		}
		public void layoutContainer(Container parent){
			super.layoutContainer(parent);
			Component check[]=parent.getComponents(),child=check[0];
			if(check.length>1)throw new IllegalStateException(
					"Bad container in "+Debug.info(this));
			Dimension size=parent.getSize();
			Rectangle bounds=child.getBounds();
			child.setLocation(bounds.x,(size.height-bounds.height)/2);
		}
	}
	private class Panner{
		final private AdjustmentListener barListener=new AdjustmentListener(){
			int thenValue=-1;
			public void adjustmentValueChanged(AdjustmentEvent e){
				int nowValue=e.getAdjustable().getValue();
				if(nowValue!=thenValue){
					thenValue=nowValue;
					return;
				}
				setPortAt(barX.getValue(),barY.getValue());
			}
		};
		final private JScrollBar barX,barY;
		private Point dragStart;
		final private JViewport port;
		Panner(){
			JScrollPane scroll=scrollPane();
			port=scroll.getViewport();
			barX=scroll.getHorizontalScrollBar();
			barX.addAdjustmentListener(barListener);
			barY=scroll.getVerticalScrollBar();
			barY.addAdjustmentListener(barListener);
		}
		void panToDrag(MouseEvent e){
			int shiftX=e.getX()-dragStart.x,shiftY=e.getY()-dragStart.y;
			setPortAt(barX.getValue()-shiftX,barY.getValue()-shiftY);
		}
		void setDrag(MouseEvent e){
			dragStart=new Point(e.getX(),e.getY());
		}
		void setPortAt(int left,int top){
			findCanvasPane().scrollRectToVisible(
					new Rectangle(new Point(left,top),port.getExtentSize()));
			Point portAt=port.getViewPosition();
			((PlaneCanvas)canvas).panAreaSet(portAt.x,portAt.y);
		}
	}
	private Panner panner;
	SwingPanningMaster(PlaneCanvas canvas){
		super(canvas);		
	}
	public boolean isScrollable(){
		return true;
	}
	public void adjustToPlane(int planeWidth,int planeHeight,
			final int viewerLeft,final int viewerTop){
		super.adjustToPlane(planeWidth,planeHeight,viewerLeft,viewerTop);
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				panner.setPortAt(viewerLeft,viewerTop);
			}
		});
	}
	protected CanvasPane findCanvasPane(){
		if(panner==null)panner=new Panner();
		return (CanvasPane)((JPanel)avatarPane()).getComponent(0);
	}
	protected JComponent newAvatarPane(){
		JPanel canvas=new CanvasPane(),centring=new JPanel(new CentringLayout());
		centring.setBackground(new Color(AreaFacets.COLOR_DESKTOP.rgb()));
		canvas.addMouseListener(new MouseAdapter(){
			public void mousePressed(MouseEvent e){
				setCanvasCursor(CURSOR_HAND);
			}
			public void mouseReleased(MouseEvent e){
				setCanvasCursor(CURSOR_DEFAULT);
				if(!e.isPopupTrigger())return;
				Point mouse=new Point(e.getX(),e.getY()),
					paneAt=scrollPane().getViewport()
						.getViewPosition(),planeAt=findCanvasPane().getLocation();
				SwingPanningMaster.this.base().popupRequested(
						(mouse.x+planeAt.x-paneAt.x),(mouse.y+planeAt.y-paneAt.y));
			}
		});
		centring.add(canvas);
		return centring;
	}
	protected void unconsumedMouseEvent(MouseEvent e){
		if(panner==null)return;
		switch(e.getID()){
		case MouseEvent.MOUSE_PRESSED:
			panner.setDrag(e);
			break;
		case MouseEvent.MOUSE_DRAGGED:
			panner.panToDrag(e);
			break;
		default:
			return;
		}
	}
}
