package facets.facet.kit.avatar;
import static facets.core.app.avatar.AvatarView.Direction.*;
import static facets.core.app.avatar.PainterSource.*;
import facets.core.app.avatar.PdfCanvas;
import facets.core.app.avatar.PdfCode;
import facets.core.app.avatar.PickPainter;
import facets.core.app.avatar.AvatarView.Direction;
import facets.core.app.avatar.PainterMaster.Textual;
import facets.core.app.avatar.PdfCanvas.PdfPainter;
import facets.util.Debug;
import facets.util.Tracer;
import facets.util.app.ProvidingCache;
import facets.util.app.ProvidingCache.ItemProvider;
import facets.util.geom.Point;
import facets.util.shade.Shades;
import facets.util.tree.ValueNode;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
final class SwingPainterTextual extends Tracer implements PickPainter,PdfPainter{
	private static final ProvidingCache fonts=new ProvidingCache(20,null);
	private final SwingPdfCode pd=new SwingPdfCode(this);
	private final Textual m;
	private final PlaneCanvas plane;
	private final Component swing;
	private final Color color,background;
	private final Font font;
	private final Direction anchor;
	@Override
	public PdfCode code(){
		return pd;
	}
	public int hashCode(){
		return m.hashCode();
	};
	SwingPainterTextual(Textual m,PlaneCanvas plane,Component swing){
		this.m=m;
		this.plane=plane;
		this.swing=swing;
		ValueNode values=m.values;
		color=new Color(Shades.named(values.getString(KEY_SHADE)).rgb());
		background=values.get(KEY_BACKGROUND)==null?null
				:new Color(Shades.named(values.getString(KEY_BACKGROUND)).rgb());
		final Object[]fonty=new Object[]{
				values.getString(KEY_FACE),values.getInt(KEY_STYLE),
				values.getInt(KEY_POINTS)};
		font=new ItemProvider<Font>(fonts,SwingPainterTextual.class,m.text){
			protected boolean passThrough(){
				return false;
			}
			protected long buildByteCount(){
				return 100;
			};
			@Override
			protected Font newItem(){
				return new Font((String)fonty[0],(Integer)fonty[1],(Integer)fonty[2]);
			}
		}.getForValues(fonty);
		anchor=values.get(KEY_ANCHOR)==null?SW:valueOf(values.getString(KEY_ANCHOR));
	}
	public void paintInGraphics(Object graphics){
		Graphics2D gt=(Graphics2D)((Graphics)graphics).create();
		if(false)gt.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_OFF);
		FontRenderContext frc=gt.getFontRenderContext();
		Rectangle2D box=font.getStringBounds(m.text,frc);
		LineMetrics lm=font.getLineMetrics(m.text,frc);
		double point=1,border=2/gt.getTransform().getScaleX(),
				descent=lm.getDescent(),leading=lm.getLeading(),
				height=lm.getAscent()+descent,width=box.getWidth();
		gt.translate(m.x-(anchor.isEastern?width:anchor.isWestern?0:width/2),
				m.y+(anchor.isNorthern?height:anchor.isSouthern?0:height/2)-descent-leading);
		if(background!=null){
			box.setRect(0,0,width+2*border,height+2*border);
			pd.definePath(box);
			Graphics2D gb=(Graphics2D)gt.create();
			int drop=(int)(false?2*border:4*point);
			gb.translate(-border+drop,-border-height+descent+2*leading+drop);
			Color borderPen=Color.gray,
				shadow=new Color(borderPen.getRGB()+(127<<24),true);
			gb.setColor(shadow);
			gb.fill(box);
			pd.setTransform(gb);
			pd.fillPath(shadow);
			gb.translate(-drop,-drop);
			gb.setColor(background);
			gb.fill(box);
			gb.setColor(color);
			float lineWidth=(float)(1*point);
			gb.setStroke(new BasicStroke(lineWidth));
			gb.draw(box);
			pd.setTransform(gb);
			pd.fillPath(background);
			pd.strokePath(color,lineWidth);
		}
		gt.setFont(font);
		gt.setColor(color);
		gt.drawString(m.text,0,0);
		pd.setTransform(gt);
		pd.drawText(m.text,font,color);
		pd.closeCode();
	}
	public Object checkCanvasHit(Point canvasAt,double hitGap){
		return null;		
	}
}