package pdft.inspect;
import static java.awt.RenderingHints.*;
import facets.core.app.avatar.AvatarContent;
import facets.core.app.avatar.Painter;
import facets.core.app.avatar.Pickable;
import facets.util.geom.Point;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.io.Serializable;
import org.apache.pdfbox.pdmodel.font.PDFontDescriptor;
import org.apache.pdfbox.pdmodel.font.PDSimpleFont;
import org.apache.pdfbox.util.TextPosition;
final class PageChar implements Pickable,Painter,AvatarContent,Serializable{
	private final String text;
	private final Font awtFont;
	private final int rotate;
	private final int rgb;
	private final float x,y;
	final Rectangle2D.Double bounds;
	private final boolean hilit;
	PageChar(TextPosition text,int rotation,int rgb,boolean hilit){
		x=text.getX();
		y=text.getY();
		rotate=rotation-(int)text.getDir();
		this.text=text.getCharacter();
		this.hilit=hilit;
		this.rgb=rgb;
		awtFont=defineDrawFont(text);
		float height=awtFont.getSize2D()*1.3f,ascend=0.7f;
		bounds=new Double(x,y-height*ascend,text.getWidth(),height);
	}
	PageChar(String text,float x,float y,Font font,Double bounds,int rotate,
			int rgb,boolean hilit){
		this.text=text;
		this.x=x;
		this.y=y;
		this.awtFont=font;
		this.bounds=bounds;
		this.hilit=hilit;
		this.rotate=rotate;
		this.rgb=rgb;
	}
	@Override
	public Object checkCanvasHit(Point canvasAt,double hitGap){
		return bounds.contains(canvasAt.x(),canvasAt.y())?canvasAt:null;
	}
	PageChar newHilit(){
		return new PageChar(text,x,y,awtFont,bounds,rotate,rgb,true);
	}
	boolean isAfter(Point point){
		double y=point.y();
		return this.y>y||(this.y==y&&this.x>point.x());
	}
	private Font defineDrawFont(TextPosition start){
		PDSimpleFont textFont=(PDSimpleFont)start.getFont();
		Font font;
		try{
			font=textFont.getawtFont();
		}catch(Exception e){
			font=new Font("Times New Roman",0,1);
		}
		String family=font.getFamily();
		if(!"Times New Roman|Arial|Wingdings".contains(family))
			font=new Font(family.replaceAll("[A-Z]+\\+",""),0,1);
		final double upFont=1.0005;
		PDFontDescriptor fd=textFont.getFontDescriptor();
		return font.deriveFont(
			(fd!=null&&fd.isItalic()?Font.ITALIC:0)|(fd!=null&&fd.getFontWeight()>500?Font.BOLD:0),
			(float)(start.getFontSizeInPt()*upFont));
	}
	@Override
	public void paintInGraphics(Object graphics){
		Graphics2D g2=(Graphics2D)graphics;
		if(true){
			g2.setRenderingHint(KEY_ANTIALIASING,VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(KEY_FRACTIONALMETRICS,VALUE_FRACTIONALMETRICS_ON);
		}
		if(hilit){
			Graphics2D gh=(Graphics2D)g2.create();
			gh.setPaint(PagePainters.hiLite);
			gh.fill(bounds);
		}
		g2.setFont(awtFont);
		g2.setPaint(false?Color.red:new Color(rgb));
		final boolean renderOutlines=false;
		if(rotate==0&&!renderOutlines)g2.drawString(text,x,y);
		else{
			g2.translate(x,y);
			float toRads=360f;
			g2.rotate(rotate/toRads*2*Math.PI);
			Shape shape=awtFont.createGlyphVector(g2.getFontRenderContext(),text).getOutline();
			if(renderOutlines)g2.fill(shape);
			else g2.drawString(text,0,0);
		}
	}
}