package facets.facet.kit.avatar;
import static facets.facet.kit.avatar.ImageProviderAwt.*;
import static java.awt.RenderingHints.*;
import facets.core.app.avatar.PainterMaster.Outlined;
import facets.core.app.avatar.PainterSource.Transform;
import facets.core.app.avatar.PdfCanvas.PdfPainter;
import facets.core.app.avatar.PdfCode;
import facets.core.app.avatar.PickPainter;
import facets.util.Debug;
import facets.util.Identified;
import facets.util.Tracer;
import facets.util.Util;
import facets.util.app.ProvidingCache;
import facets.util.app.ProvidingCache.ItemProvider;
import facets.util.geom.Point;
import facets.util.shade.Shade;
import facets.util.shade.Shades;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
final class SwingPainterOutlined extends Tracer implements PickPainter,Identified,
		PdfPainter{
	private static final boolean hitPen=true;
	private static final float hairline=hitPen?0.8f:1;
	private final Outlined master;
	private final SwingPdfCode pdf=new SwingPdfCode(this,false);
	private static int identities;
	private final int identity=identities++;
	private final PlaneCanvas canvas;
	private final int hash;
	private Shade fill,pen;
	private Shape penShape,fillShape;
	private AffineTransform transform=new AffineTransform();
	private BasicStroke stroke;
	private double scaleThen=Double.NaN;
	SwingPainterOutlined(Outlined master,PlaneCanvas canvas){
		this.master=master;
		this.canvas=canvas;
		hash=Arrays.hashCode(new Object[]{master,transform});  
	}
	public int hashCode(){
		return hash;
	}
	private void checkPaintValues(){
		if(false&&scaleThen==scaleThen&&canvas.scale!=scaleThen)
			throw new RuntimeException("Not implemented in "+this);
		else if(penShape!=fillShape||scaleThen==canvas.scale)return;
		scaleThen=canvas.scale;
		Shape shape=(Shape)master.getOutline();
		if(shape==null)throw new IllegalStateException("No shape in "+Debug.info(this));
		else shape=transform==null?shape:transform.createTransformedShape(shape);
		fill=master.getFill();
		pen=master.getPen();
		if(fill!=null)fillShape=shape;
		if(pen!=null){
			Object penStyle=master.penStyle();
			boolean forGrid=pen==Shades.lightGray;
			float min=hairline*(forGrid?0.5f:1f),unscale=(float)canvas.unscale(min);
			stroke=penStyle!=Outlined.PEN_HAIRLINE?(BasicStroke)penStyle
					:new BasicStroke(unscale<min?min:unscale);
			penShape=hitPen&&fill==null?stroke.createStrokedShape(shape):shape;
		}
	}
	final private static ProvidingCache cache=new ProvidingCache(20,null);
	public void paintInGraphics(Object graphics){
		Graphics2D g2=(Graphics2D)graphics;
		pdf.setTransform(g2);
		checkPaintValues();
		if(fill!=null){
			if(fillShape==null)throw new IllegalStateException("No fillShape in "+this);
			Rectangle2D bounds=fillShape.getBounds2D();
			double wD=bounds.getWidth(),hD=bounds.getHeight(),
					xD=bounds.getX(),yD=bounds.getY(),mD=2;
			Class<SwingPainterOutlined>spo=SwingPainterOutlined.class;
			Color color=new Color(fill.rgb());
			Image image=true||wD>100?null
					:new ItemProvider<Image>(cache,spo,spo.getSimpleName()){
			@Override
			protected Image newItem(){
				int scaleI=8;
				int mI=(int)(mD*scaleI),wI=(int)(wD*scaleI)+2*mI,hI=(int)(hD*scaleI+2*mI);
				Image raw=newPaintableImage(wI,hI,Color.lightGray);
				Graphics2D g2=(Graphics2D)raw.getGraphics();
				g2.setRenderingHint(KEY_ANTIALIASING,VALUE_ANTIALIAS_ON);
				g2.setColor(color);
				g2.scale(scaleI,scaleI);
				g2.translate(-xD+mD,-yD+mD);
				g2.fill(fillShape);
				trace(".newItem: ");
				if(false)return raw.getScaledInstance((int)wD,(int)hD,Image.SCALE_SMOOTH);
				else if(false)return raw;
				Image out=newPaintableImage(wI/scaleI,hI/scaleI,Color.lightGray);
				Graphics2D gOut=(Graphics2D)out.getGraphics();
				gOut.setRenderingHint(KEY_ANTIALIASING,VALUE_ANTIALIAS_ON);
				gOut.scale(1.0/scaleI,1.0/scaleI);
				if(true){
					gOut.setColor(color);
					gOut.translate(-xD+mD,-yD+mD);
					gOut.fill(fillShape);
				}
				else gOut.drawImage(raw,0,0,null);
				return out;
			}}.getForValues(master.hashCode());
			if(image==null){
				if(false)trace(".paintInGraphics: ");
				g2.setColor(color);
				g2.fill(fillShape);
			}else{
				g2.translate(0,0);
				g2.drawImage(image,0,0,null);
			}
			pdf.definePath(fillShape);
			pdf.fillPath(color);
		}
		if(pen!=null){
			g2.setStroke(stroke);
			Color color=new Color(pen.rgb());
			g2.setColor(color);
			if(hitPen&&fill==null)g2.fill(penShape);
			else g2.draw(penShape);
			pdf.definePath(penShape);
			pdf.strokePath(color,(float)canvas.unscale(1));
		}
		pdf.closeCode();
	}
	public Object checkCanvasHit(Point canvasAt,double hitGap){
		if(!master.isPickable())return null;
		double 
			canvasGap=canvas.unscale(hitGap),boxSize=canvasGap>0?canvasGap*2:Double.MIN_VALUE,
			boxX=canvasAt.x()-boxSize/2,
			ySign=-1,
			boxY=canvasAt.y()+boxSize/2*ySign;
		Rectangle2D hitBox=new Rectangle2D.Double(boxX,boxY,boxSize,boxSize);
		checkPaintValues();
		if(!(penShape!=null&&penShape.intersects(hitBox)||
				fillShape!=null&&fillShape.intersects(hitBox))
				)return null;
		if(false)Util.printOut("SwingPainterOutlined.checkCanvasPick: ",
				fillShape.getBounds()
				+"\n"+
				hitBox.getBounds()
				);
		Point2D shapeAt=null;
		try{
			shapeAt=transform.inverseTransform(new Point2D.Double(
						canvasAt.x(),
					canvasAt.y()),null);
		}catch(NoninvertibleTransformException e){
			throw new RuntimeException(e.getMessage());
		}
		return new Pick(this,new Point(shapeAt.getX(),shapeAt.getY()),null);
	}
	void addTransform(Transform add,boolean concatenate){
		if(concatenate)transform.concatenate((AffineTransform)add);
		else transform=((AffineTransform)add);
		if(false)penShape=fillShape=null;
	}
	public Object identity(){
		return identity;
	}
	public final void setTitle(String title){
	}
	@Override
	public PdfCode code(){
		return pdf;
	}
}