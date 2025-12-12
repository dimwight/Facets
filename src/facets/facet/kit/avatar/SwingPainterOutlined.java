package facets.facet.kit.avatar;
import facets.core.app.avatar.PainterMaster.Outlined;
import facets.core.app.avatar.PainterSource.Transform;
import facets.core.app.avatar.PdfCanvas.PdfPainter;
import facets.core.app.avatar.PdfCode;
import facets.core.app.avatar.PickPainter;
import facets.util.Debug;
import facets.util.Identified;
import facets.util.Tracer;
import facets.util.Util;
import facets.util.geom.Point;
import facets.util.shade.Shade;
import facets.util.shade.Shades;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
final class SwingPainterOutlined extends Tracer implements PickPainter,Identified,
        PdfPainter{
    private static final boolean hitPen=true;
    private static final float hairline=hitPen?0.8f:1;
    private final Outlined master;
    private final SwingPdfCode pdf = new SwingPdfCode(this, false);
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
    public void paintInGraphics(Object graphics){
        Graphics2D g2=(Graphics2D)graphics;
        pdf.setTransform(g2);
        checkPaintValues();
        if(fill!=null){
            Color color=new Color(fill.rgb());
            g2.setColor(color);
            if(fillShape==null)throw new IllegalStateException("No fillShape in "+this);
            g2.fill(fillShape);
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
        if(false)throw new RuntimeException("Not implemented in "+this);
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