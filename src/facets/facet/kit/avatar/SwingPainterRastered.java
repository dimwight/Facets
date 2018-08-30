package facets.facet.kit.avatar;
import facets.core.app.avatar.PickPainter;
import facets.core.app.avatar.PlaneView;
import facets.core.app.avatar.PainterMaster.Rastered;
import facets.util.Util;
import facets.util.geom.Point;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import javax.swing.ImageIcon;
final class SwingPainterRastered implements PickPainter{
	private final Component swing;
	private final Image master;
	private final PlaneCanvas plane;
	private Image imagePaint;
	private int widthPane,heightPane,shiftY,shiftX;
	SwingPainterRastered(Rastered master,PlaneCanvas plane,Component swing){
		this.plane=plane;
		this.swing=swing;
		this.master=new ImageIcon(master.url).getImage();
	}
	public void paintInGraphics(Object graphics){
		if(plane.paneWidth!=widthPane||plane.paneHeight!=heightPane){
			widthPane=plane.paneWidth;heightPane=plane.paneHeight;
			rescalePaintImage();
		}
		((Graphics)graphics).drawImage(imagePaint,shiftX,shiftY,null);
	}
	private void rescalePaintImage(){
		PlaneView view=(PlaneView)plane.viewer().view();
		double widthImage=master.getWidth(null),heightImage=master.getHeight(null),
			widthPaint=plane.scale(view.showWidth()),
			heightPaint=plane.scale(view.showHeight());
		imagePaint=swing.createImage((int)widthPaint,(int)heightPaint);
		Graphics2D gi=(Graphics2D)imagePaint.getGraphics();
		gi.scale(widthPaint/widthImage,heightPaint/heightImage);
		gi.drawImage(master,0,0,null);gi.dispose();
		shiftY=(int)(heightPane>heightPaint?(heightPane-heightPaint)/2:0);
		shiftX=(int)(widthPane>widthPaint?(widthPane-widthPaint)/2:0);
	}
	public Object checkCanvasHit(Point canvasAt,double hitGap){
		return null;		
	}
}