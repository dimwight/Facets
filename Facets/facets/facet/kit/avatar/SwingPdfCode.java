package facets.facet.kit.avatar;
import static facets.util.Doubles.*;
import facets.core.app.avatar.PdfCode;
import facets.core.app.avatar.PdfCanvas.PdfPainter;
import facets.core.app.avatar.PdfCode.Segment;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
final class SwingPdfCode extends PdfCode{
	private double[]coords;
	SwingPdfCode(PdfPainter painter){
		super(painter);
	}
	void drawText(String text,Font font,Color color){
		super.drawText(text,font.getName(),font.getSize2D(),(font.getStyle()&Font.BOLD)!=0,
				(font.getStyle()&Font.ITALIC)!=0,fromFloats(color.getColorComponents(null)));
	}
	void setTransform(Graphics2D g2){
		g2.getTransform().getMatrix(coords=new double[6]);
		super.setTransform(coords);
	}
	void definePath(Shape path){
		PathIterator i=path.getPathIterator(null);
		while(!i.isDone()){
			int segment=i.currentSegment(coords=new double[6]);
			super.addPathSegment(Segment.values()[segment],coords);
			i.next();
		}
	}
	void strokePath(Color c,double width){
		super.strokePath(fromFloats(c.getColorComponents(null)),width);
	}
	void fillPath(Color c){
		super.fillPath(fromFloats(c.getColorComponents(null)));
	}
}
