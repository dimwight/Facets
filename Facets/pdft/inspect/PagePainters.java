package pdft.inspect;
import static java.awt.RenderingHints.*;
import static pdft.inspect.PageAvatarPolicies.*;
import facets.core.app.avatar.AvatarContent;
import facets.core.app.avatar.Painter;
import facets.core.app.avatar.Pickable;
import facets.facet.app.FacetAppSurface;
import facets.facet.kit.avatar.ImageProviderAwt;
import facets.util.Debug;
import facets.util.SizeEstimable;
import facets.util.Tracer;
import facets.util.Util;
import facets.util.app.WatchableOperation;
import facets.util.geom.Point;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.pdfbox.pdfviewer.PageDrawer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.util.TextPosition;
import org.apache.pdfbox.util.TextPositionComparator;
import pdft.inspect.DocTexts.PageChars;
final class PagePainters extends Tracer implements Pickable,AvatarContent,SizeEstimable{
	static final Color hiLite=new Color(153,193,218);
	@Override
	protected void traceOutput(String msg){
		if(true)super.traceOutput(msg);
	}
	PageChar[]selectionChars=new PageChar[]{};
	private final List<PageChar>plainChars=new ArrayList(),fullChars=new ArrayList();
	private final FacetAppSurface app;
	private final boolean empty;
	private final PDDocument doc;
	private final PDPage page;
	private final int pageAt,rotation;
	private final PDRectangle mediaBox;
	private List<PageChar>paintChars;
	PagePainters(DocTexts texts,int pageAt,FacetAppSurface app){
		empty=(this.app=app)==null;
		doc=texts.doc;
		PageChars chars=texts.getChars(this.pageAt=pageAt);
		page=chars.page;
		rotation=page.findRotation();
		mediaBox=page.findMediaBox();
		for(TextPosition textChar:chars.textChars)
			if(textChar!=null)plainChars.add(new PageChar(textChar,rotation,0,false));
	}
	@Override
	public String toString(){
		return Debug.info(this)+": Page #"+pageAt;
	}
	Painter newTextPainter(final int viewsAt){
		final boolean drawPage=viewsAt!=DISPLAY_TEXT;
		final Dimension pageSize=mediaBox.createDimension();
		final boolean flip=rotation!=0;
		final int paintWidth=!flip?pageSize.width:pageSize.height,
				paintHeight=!flip?pageSize.height:pageSize.width;
		return empty?new Painter(){
			@Override
			public void paintInGraphics(Object graphics){}
		}
		:new Painter(){
			final double scale=1;
			int width=(int)(paintWidth*scale),height=(int)(paintHeight*scale);
			final Image image=!drawPage?null:new ImageProviderAwt(
					app.ff.providingCache(),
					PagePainters.this,PagePainters.class.getSimpleName()+".newTextPainter",
					width,height){
				@Override
				protected BufferedImage newPaintedImage(int width,int height){
					BufferedImage paintable=newPaintableImage(width,height,Color.white);
					drawPageGraphics((Graphics2D)paintable.getGraphics(),scale);
					return paintable;
				}
				@Override
				protected long buildByteCount(){
					return width*height*3;
				}
			}.getImageForValues(pageAt,viewsAt);
			public void paintInGraphics(Object graphics){
				Graphics2D paint=(Graphics2D)((Graphics)graphics).create();
				if(drawPage){
					if(image==null)drawPageGraphics((Graphics2D)paint.create(),scale);
					else paint.drawImage(image.getScaledInstance(paintWidth,-1,
							Image.SCALE_SMOOTH),0,0,null);
					paintChars=fullChars;
				}
				else paintChars=plainChars;
				for(PageChar c:paintChars)c.paintInGraphics(paint.create());
			}
			private void drawPageGraphics(final Graphics2D drawer,final double scale){
			WatchableOperation op=new WatchableOperation("PagePainters.drawPageGraphics"){
			@Override
			public void doSimpleOperation(){
				final List<TextPosition>texts=new ArrayList();
				final Map<TextPosition,Color>colors=new HashMap();
				drawer.scale(scale,scale);
				if(flip){
					drawer.translate(pageSize.height,0);
					drawer.rotate(rotation/360f*2*Math.PI);
				}
				try{
					new PageDrawer(){
						@Override
						protected void processTextPosition(TextPosition text){
							if(false)super.processTextPosition(text);
							else try{
								colors.put(text,getGraphicsState().getNonStrokingColor().getJavaColor());
								texts.add(text);
							}catch(IOException e){
								throw new RuntimeException(e);
							}
						}
					}.drawPage(drawer,page,pageSize);
				}catch(IOException e){
					throw new RuntimeException(e);
				}
				if(fullChars.size()>0)return;
        Collections.sort(texts,new TextPositionComparator());
        for(TextPosition t:texts)
        	fullChars.add(new PageChar(t,rotation,colors.get(t).getRGB(),false));
			}};
				if(true)app.runWatched(op);
				else op.doOperations();
			}
			@Override
			public int hashCode(){
				return Arrays.hashCode(new Object[]{pageSize,pageAt,viewsAt});
			}
		};
	}
	Painter newSelectionPainter(){
		return selectionChars.length==0?Painter.EMPTY:new Painter(){
			public void paintInGraphics(Object graphics){
				for(PageChar draw:selectionChars)draw.paintInGraphics(((Graphics)graphics).create());
			}
		};
	}
	@Override
	public Object checkCanvasHit(Point canvasAt,double hitGap){
		if(paintChars==null||
			!mediaBox.contains((float)canvasAt.x(),(float)canvasAt.y()))return null;
		for(PageChar c:paintChars)if(c.bounds.contains(canvasAt.x(),canvasAt.y()))return c;
		return null;
	}
	PageChar[]newDragSelection(Point anchor,Point drag){
		PageChar anchorChar=(PageChar)checkCanvasHit(anchor,0);
		boolean checkBefore=anchorChar.isAfter(drag);
		int anchorAt=paintChars.indexOf(anchorChar),charCount=paintChars.size();
		final List<PageChar>select=new ArrayList();
		if(paintChars==null)throw new IllegalStateException(
				"Null paintChars in "+Debug.info(this));
		for(PageChar check:paintChars.subList(
				checkBefore?0:anchorAt,checkBefore?anchorAt:charCount))
			if((checkBefore&&check.isAfter(drag))||(!checkBefore&&!check.isAfter(drag)))
				select.add(check.newHilit());
		return select.toArray(new PageChar[]{});
	}
	void setDragSelection(Point anchorAt,Point dragAt){
		selectionChars=newDragSelection(anchorAt,dragAt);
	}
	void clearSelection(){
		selectionChars=new PageChar[]{};
	}
	public long estimateSize(){
		int charCount=plainChars.size();
		int estimate=charCount==0?0
			:charCount*Util.byteCount(plainChars.get(0));
		return estimate;
	}
}