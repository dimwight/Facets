package pdft.block;
import static java.awt.RenderingHints.*;
import facets.core.app.avatar.Painter;
import facets.util.Tracer;
import facets.util.Util;
import facets.util.geom.Point;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.io.Serializable;
import org.apache.pdfbox.pdmodel.font.PDSimpleFont;
import org.apache.pdfbox.util.TextPosition;
import applicable.TextAvatar;
final class PageChar extends Tracer implements TextAvatar,Serializable{
	private static final Color hiLite=new Color(153,193,218);
	private final String text;
	private final Font awtFont;
	private final int rotate;
	private final int rgb;
	private final BoundsBox box;
	private final float fontSize;
	static final int SIZE_ESTIMATE=106;
	@Override
	public BoundsBox getBounds(){
		return box;
	}
	@Override
	public String getText(){
		return text;
	}
	PageChar(CharFonts fonts,TextPosition from,int rotation,int rgb){
		this.rgb=rgb;
		this.text=from.getCharacter();
		rotate=rotation-(int)from.getDir();
		PDSimpleFont pdFont=(PDSimpleFont)from.getFont();
		fontSize=from.getFontSizeInPt();
		try{
			awtFont=fonts==null?pdFont.getawtFont():fonts.getAwt(pdFont).deriveFont(fontSize);
		}catch(IOException e1){
			throw new RuntimeException(e1);
		}
		float x=from.getX(),y=from.getY(),width=from.getWidth(),height=from.getHeight();
		if(width<0||height<0)try{
			trace(".PageChar: box=",Util.sf(height*width));
			width=pdFont.getFontWidth(text.getBytes(),0,1)/100;
			height=pdFont.getFontHeight(text.getBytes(),0,1)/100;
			trace(".PageChar: box=",Util.sf(height*width));
		}catch(IOException e){
			throw new RuntimeException(e);
		}
		box=new BoundsBox(x,y-height,x+width,y);
	}
	PageChar(PageChar src,int rgb){
		text=src.text;
		awtFont=src.awtFont;
		fontSize=src.fontSize;
		box=src.box;
		rotate=src.rotate;
		this.rgb=rgb;
	}
	@Override
	public Painter newViewPainter(final boolean selected){
		return new Painter(){
			@Override
			public void paintInGraphics(Object graphics){
		Graphics2D g2=(Graphics2D)graphics;
		if(true){
			g2.setRenderingHint(KEY_ANTIALIASING,VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(KEY_FRACTIONALMETRICS,VALUE_FRACTIONALMETRICS_ON);
		}
		if(selected){
			Graphics2D gh=(Graphics2D)g2.create();
			gh.setPaint(hiLite);
			double dX=box.width*.05,dY=box.height*.25;
			gh.fill((false?box:new BoundsBox(box.left,box.top-dY*.75,
					box.right+dY,box.bottom+dY*1.5)).asRectangle());
		}
		g2.setPaint(false?Color.red:new Color(rgb));
		g2.translate(box.left,box.bottom);
		if(rotate==0)drawText(g2);
		else{
			g2.rotate(rotate/360f*2*Math.PI);
			if(false)g2.fill(awtFont.createGlyphVector(g2.getFontRenderContext(),text
					).getOutline());
			else drawText(g2);
		}
			}
		};
	}
	private void drawText(Graphics2D g2){
		g2.setFont(awtFont);
		g2.drawString(text,0,0);
	}
	@Override
	protected void traceOutput(String msg){
		if(false)super.traceOutput(msg);
	}
	@Override
	public Object checkCanvasHit(Point canvasAt,double hitGap){
		return box.contains(canvasAt.x(),canvasAt.y())?canvasAt:null;
	}
	boolean isAfter(Point point){
		double y=point.y();
		return box.bottom>y||(box.bottom==y&&box.left>point.x());
	}
	boolean boundsWithin(BoundsBox bounds){
		return bounds.contains(this.box);
	}
	boolean boundsContain(Point canvasAt){
		return box.contains(canvasAt.x(),canvasAt.y());
	}
	@Override
	public Painter[]newPickPainters(final boolean selected){
		return new Painter[]{};
	}
}