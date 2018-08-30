package facets.facet.kit.avatar;
import static facets.util.Doubles.*;
import static java.awt.BasicStroke.*;
import static java.awt.image.Raster.*;
import static java.lang.Integer.*;
import static java.lang.Math.*;
import facets.core.app.avatar.Painter;
import facets.core.app.avatar.PainterMaster;
import facets.core.app.avatar.PainterSource;
import facets.core.app.avatar.PickPainter;
import facets.core.app.avatar.Pickable;
import facets.core.app.avatar.PlaneView;
import facets.core.app.avatar.PainterMaster.Outlined;
import facets.core.app.avatar.PainterMaster.Rastered;
import facets.core.app.avatar.PainterMaster.Scaling;
import facets.core.app.avatar.PainterMaster.Textual;
import facets.facet.kit.KitFacet;
import facets.facet.kit.swing.ViewerMaster;
import facets.util.Debug;
import facets.util.StringFlags;
import facets.util.Tracer;
import facets.util.Util;
import facets.util.geom.Line;
import facets.util.geom.Point;
import facets.util.geom.Vector;
import facets.util.shade.Shade;
import facets.util.shade.Shades;
import facets.util.tree.ValueNode;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.net.URL;
import java.util.Arrays;
import javax.swing.JLabel;
final public class SwingPainterSource extends PainterSource{	
	private class SwingTransform extends AffineTransform implements Transform{}
	private final Component swing;
	private final PlaneCanvas canvas;
	private FontRenderContext liveFontContext;
	SwingPainterSource(PlaneCanvas canvas,Component swing){
		this.canvas=canvas;
		this.swing=swing;
	}
	@Override
	public Painter rectangle(double x,double y,double width,double height,String args){
		ValueNode values=newValues(args.split("\\s+"));
		Shade pen=Shades.named(values.getString(KEY_SHADE_PEN)),
			fill=Shades.named(values.getString(KEY_SHADE_FILL));
		Point lt=new Point(x,y),rt=new Point(x+width,y),lb=new Point(x,y+height),
				rb=new Point(x+width,y+height);
		Line[]square=new Line[]{new Line(lt,rt),new Line(lt,lb),new Line(rb,rt),new Line(rb,lb)};
		final Double r=new Rectangle2D.Double(x,y,width,height);
		final Object[]hashables=new Object[]{x,y,width,height,args};
		PainterMaster master=new Outlined(fill,pen,true){
			@Override
			protected Object[]lazySubHashables(){
				return hashables;
			}
			public Object getOutline(){
				return r;
			}
			public Scaling scaling(){
				return Scaling.PEN;
			}
		};
		return mastered(master);
	
	}
	@Override
	public Painter bar(double x,double y,double width,double height,Shade fill,
			boolean pickable){
		final Double r=new Rectangle2D.Double(x,y,width,height);
		final Object[]hashables={x,y,width,height};
		PainterMaster master=new Outlined(fill,null,pickable){
			@Override
			protected Object[]lazySubHashables(){
				return hashables;
			}
			public Object getOutline(){
				return r;
			}
			public Scaling scaling(){
				return Scaling.OUTLINE;
			}
			@Override
			public String toString(){
				return "Bar "+" "+r;
			}
		};
		return mastered(master);
	}
	private Graphics createGraphics(){
		return swing.getGraphics().create();
	}
	public Painter textTooltip(final String text,double x,double y){
		Util.printOut("SwingPainterSource:"+text);
		return new PickPainter(){
			public Object checkCanvasHit(Point canvasAt,double hitGap){
				throw new RuntimeException("Not implemented in "+Debug.info(this));
			}
			public void paintInGraphics(Object graphics){
			}
		};
	}
	public Painter marquee(Point anchorAt,Point dragAt,Shade shade){
		double[]vals=anchorAt.rectangleValues(dragAt);
		return rectangle(vals[BoxL],vals[BoxT],vals[RectW],vals[RectH],shade);
	}
	public Painter pointMark(final Point at,final Shade fill,final boolean pickable){
		double size=canvas.markSize();
		Point drawAt=at.newPoint().shifted(new Vector(size,size).scaled(-0.5));
		final Rectangle2D mark=new Rectangle2D.Double(drawAt.x(),drawAt.y(),size,size);
		return new PickPainter(){
			public void paintInGraphics(Object graphics){
				Graphics2D g=(Graphics2D)((Graphics)graphics).create();
				g.setColor(new Color(fill.rgb()));
				g.fill(mark);
			}
			public Object checkCanvasHit(Point canvasAt,double hitGap){
				if(!pickable)return null;
				boolean hit=mark.contains(canvasAt.x(),canvasAt.y());
				return!hit?null:new Pick(this,at.newPoint(),null);
			}
		};
	}
	public Painter backgroundRaster(URL url){
		PainterMaster master=new PainterMaster.Rastered(url);
		return mastered(master);
	}
	public Painter textOutline(final String text,String face,int points,
			boolean bold,boolean italic,Shade fill,Shade pen){
		final int style=(bold?Font.BOLD:0)|(italic?Font.ITALIC:0);
		if(face.equals(""))face="Serif";
		final Font font=new Font(face,style,points);
		final Object[]hashables={text,font};
		PainterMaster master=new Outlined(fill,pen,true){
			private Shape geometry;
			@Override
			protected Object[]lazySubHashables(){
				return hashables;
			}
			public Object getOutline(){
				if(geometry==null||liveFontContext==null)geometry=
					font.createGlyphVector(getFontContext(),text).getOutline(0,0);
				return geometry;
			}
			public Scaling scaling(){
				return Scaling.NONE;
			}
		};
		return mastered(master);
	}
	public Painter turnMark(final double size,final double width,Shade fill){
		PainterMaster master=new Outlined(null,fill,true) {
			private Shape geometry;
			private final Object stroke=new BasicStroke((float)width,CAP_ROUND,JOIN_ROUND,10,new float[]{5,10},0);
			public Object getOutline(){
				return geometry==null?geometry=new Arc2D.Double
						(-size/2,-size/2,size,size,60,290,Arc2D.OPEN)
	    	:geometry;
			}
			public Scaling scaling(){
				return Scaling.PEN;
			}
			public Object penStyle(){
				return stroke;
			}
		};
		return mastered(master);
	}
	public Painter stretchMark(final double height,final double length,
			final double width,Shade fill){
		PainterMaster master=new Outlined(fill,null,true){
			private Shape geometry;
			public Object getOutline(){
				return geometry==null?geometry=
					new BasicStroke((float)width,BasicStroke.CAP_ROUND,
	  	    BasicStroke.JOIN_ROUND,(float) height,new float[]{4,6},6)
		    	.createStrokedShape(new Line2D.Double(
		    			length,0,height*2+length,0))
		  	    	:geometry;
			}
			public Scaling scaling(){
				return Scaling.PEN;
			}
		};
		return mastered(master);
	}
	public Painter backgroundLines(Line[]lines,Shade shade){
		final Painter[]painters=new PickPainter[lines.length];
		for(int i=0;i<painters.length;i++)
			painters[i]=line(lines[i],shade,NO_PATTERN,false);
		return bundle(painters);
	}
	public Transform transformScale(double x,double y){
		SwingTransform t=new SwingTransform();
		t.setToScale(x,y);
		return t;
	}
	public Transform transformAt(double x,double y){
		SwingTransform t=new SwingTransform();
		t.setToTranslation(x,y);
		return t;
	}
  /**
   * Sets this transform to a shearing transformation.
   * The matrix representing this transform becomes:
   * <pre>
   *          [   1   shx   0   ]
   *          [  shy   1    0   ]
   *          [   0    0    1   ]
   * </pre>
   * @param shx the multiplier by which coordinates are shifted in the
   * direction of the positive X axis as a factor of their Y coordinate
   * @param shy the multiplier by which coordinates are shifted in the
   * direction of the positive Y axis as a factor of their X coordinate
   * @since 1.2
   */
	public Transform transformShear(double shx,double shy){
		SwingTransform t=new SwingTransform();
		t.setToShear(shx,shy);
		return t;
	}
	public Transform transformTurn(double radians, double atX, double atY){
		SwingTransform t=new SwingTransform();
		t.setToRotation(radians,atX,atY);
		return t;
	}
	public void applyTransforms(Transform[]transforms,boolean concatenate,
			Painter...painters){
		if(transforms==null||transforms.length==0)throw new IllegalArgumentException(
				"Null or empty transforms in "+this);
		Transform transform=transforms[0];
		for(int i=1;i<transforms.length;i++)
			((AffineTransform)transform).concatenate((AffineTransform)transforms[i]);
		for(int i=0;i<painters.length;i++){
			((SwingPainterOutlined)painters[i]).addTransform(transform,concatenate);
		}
	}
	public double textLength(String text,String face,int size,boolean bold,
			boolean italic){
		int style=(bold?Font.BOLD:0)|(italic?Font.ITALIC:0);
		Font font=new Font(face,style,size);
		return true?new JLabel().getFontMetrics(font).stringWidth(text)*.95:
			font.createGlyphVector(
				getFontContext(),text).getOutline(0,0).getBounds2D().getWidth();
	}
	public static ViewerMaster planeMaster(KitFacet facet,boolean panning,StringFlags hints){
		PlaneCanvas canvas=new PlaneCanvas(facet,hints);
		return panning?new SwingPanningMaster(canvas):new SwingPlaneMaster(canvas);
	}
	public Painter line(final Line line,Shade shade,int style,boolean pickable){
		final Shape line2d=new Line2D.Double(line.from.x(),line.from.y(),
				line.to.x(),line.to.y());
		final Object stroke=style==NO_PATTERN||style==HAIRLINE?Outlined.PEN_HAIRLINE:
			new BasicStroke(1.0f,CAP_ROUND,JOIN_ROUND,10,new float[]{3,style},0),
			hashables[]=new Object[]{stroke,line,style};
		return mastered(new Outlined(null,shade,pickable){
			@Override
			protected Object[]lazySubHashables(){
				return hashables;
			}
			public Scaling scaling(){
				return Scaling.PEN;
			}
			public Object penStyle(){
				return stroke;
			}
			public Object getOutline(){
				return line2d;
			}
		});
	}
	public Painter mastered(PainterMaster master){
		return master instanceof Textual?
				new SwingPainterTextual((Textual)master,canvas,swing)
			:master instanceof Rastered?
				new SwingPainterRastered((Rastered)master,canvas,swing)
			:new SwingPainterOutlined((Outlined)master,canvas);
	}
	private FontRenderContext getFontContext() {
		if(liveFontContext!=null)return liveFontContext;
		Graphics2D graphics=(Graphics2D)swing.getGraphics();
		if(graphics==null)return new FontRenderContext(null,true,true);
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		    RenderingHints.VALUE_ANTIALIAS_ON);
		return liveFontContext=graphics.getFontRenderContext();
	}
	public static SwingPainterSource newDummy(){
		return new SwingPainterSource(null,new JLabel());
	}
}