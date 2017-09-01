package pdft.block;
import static pdft.block.PdfPages.*;
import facets.core.app.avatar.Painter;
import facets.facet.app.FacetAppSurface;
import facets.facet.kit.avatar.ImageProviderAwt;
import facets.util.Bytes;
import facets.util.Times;
import facets.util.Tracer;
import facets.util.Util;
import facets.util.app.DirCache;
import facets.util.app.ProvidingCache;
import facets.util.app.ProvidingCache.ItemProvider;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.pdfbox.pdfviewer.PageDrawer;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.util.TextPosition;
import applicable.TextAvatar.BoundsBox;
final class PagePainters extends Tracer{
	final static boolean timeImage=true,timePaint=true;
	private final int rotation,pageAt;
	private final FacetAppSurface app;
	private final PDPage page;
	private final CharFonts fonts;
	private final BoundsBox drawBox,pageBox;
	private final Collection<PageChar>plainChars,fullChars=new ArrayList();
	private final DirCache disk;
	PagePainters(FacetAppSurface app,PDPage page,BoundsBox drawBox,BoundsBox pageBox,
			int rotation,CharFonts fonts,Collection<PageChar>plainChars,DirCache disk,
			int pageAt){
		this.app=app;
		this.page=page;
		this.drawBox=drawBox;
		this.pageBox=pageBox;
		this.rotation=rotation;
		this.fonts=fonts;
		this.plainChars=plainChars;
		this.disk=disk;
		this.pageAt=pageAt;
	}
	Painter newPainter(final int renderAt){
		final double drawWidth=drawBox.width,drawHeight=drawBox.height;
		final ProvidingCache cache=app.ff.providingCache();
		final String titleTop=PagePainters.class.getSimpleName()+".newPainter";
		final double quality=renderAt==RENDER_FINE?2.0
				:renderAt==RENDER_GRAPHICS?1.0:Double.NaN;
		final int width=(int)(drawWidth*quality),height=(int)(drawHeight*quality);
		final Image image=renderAt==RENDER_TEXT?null:new ImageProviderAwt(cache,
				page.getCOSDictionary(),titleTop+":image",width,height){
			@Override
			public CancelStyle cancelStyle(){
				return CancelStyle.Timeout;
			}
			@Override
			protected BufferedImage newPaintedImage(int width,int height){
				if(false)throw new RuntimeException("Debug");
				imageTime("...newPaintedImage pageAt="+pageAt);
				BufferedImage image=newPaintableImage(width,height,Color.white);
				drawGraphicsPage((Graphics2D)image.getGraphics(),quality);
				imageTime("...newPaintedImage~ fullChars="+fullChars.size());
				return image;
			}
			protected String newDiskName(Object[]values){
				return String.format("%03d",pageAt)+"-"+renderAt;
			}
			@Override
			protected void putDiskItem(String diskName,Packer packImage){
				imageTime("...putDiskItem diskName="+diskName);
				imageTime("...putDiskItem packImage="+packImage);
				byte[]packChars=Bytes.pack((Serializable)fullChars);
				imageTime("...putDiskItem packChars="+Util.kbs(packChars.length));
				disk.put(diskName,new Object[]{packImage,packChars});
				imageTime("...putDiskItem~");
			}
			@Override
			protected Packer getDiskItem(String diskName){
				imageTime("...getDiskItem diskName="+diskName);
				Object packed[]=(Object[])disk.get(diskName);
				if(packed==null)return null;
				Packer packImage=(Packer)packed[0];
				byte[]packChars=(byte[])packed[1];
				byte[]chars=packChars;
				if(fullChars.isEmpty())fullChars.addAll(
						(Collection<PageChar>)Bytes.unpack(chars));
				imageTime("...getDiskItem~ fullChars="+fullChars.size());
				return packImage;
			}
		}.getImageForValues(pageAt,renderAt);
		return new Painter(){
			boolean textOnly=renderAt==RENDER_TEXT;
			@Override
			public void paintInGraphics(Object graphics){
				Graphics2D g2=(Graphics2D)((Graphics)graphics).create(),
					gi=(Graphics2D)g2.create();
				if(timePaint)Times.printElapsed("PagePainters.paintInGraphics");
				if(!textOnly){
					final double scale=false?gi.getTransform().getScaleX():1;
					gi.scale(1/scale,1/scale);
					Image page=new ItemProvider<Image>(cache,PagePainters.this,titleTop+":page"){
						protected boolean passThrough(){
							return true;
						}
						@Override
						protected Image newItem(){
							int width=(int)(drawBox.width*scale),height=(int)(drawBox.height*scale);
							return image==null?ImageProviderAwt.newPaintableImage(width,height,Color.darkGray)
									:image.getScaledInstance(width,-1,
											renderAt==RENDER_FINE?Image.SCALE_SMOOTH:Image.SCALE_FAST);
						}
						@Override
						protected long buildByteCount(){
							return(long)(drawWidth*drawHeight*4);
						}
					}.getForValues(pageAt);
					gi.drawImage(page,0,0,null);
				}
				if(timePaint)Times.printElapsed("PagePainters.paintInGraphics: chars="
						+plainChars.size());
				for(PageChar c:textOnly?plainChars:fullChars)
					c.newViewPainter(false).paintInGraphics(g2.create());
				if(timePaint)Times.printElapsed("PagePainters.~paintInGraphics");
			}
			public int hashCode(){
				return !textOnly&&image==null?Integer.MAX_VALUE:0;
			}
		};
	}
	private static void imageTime(String msg){
		if(timeImage)Times.printElapsed("PagePainters"+msg);
	}
	private void drawGraphicsPage(final Graphics2D g2,final double quality){
		double dX=0,dY=0;
		g2.scale(quality,quality);
		switch(rotation){
		case 90:
			dX=pageBox.height;
			break;
		case 180:
			dX=pageBox.width;
			dY=pageBox.height;
			break;
		case 270:
			dY=pageBox.width;
			break;
		default:
		}
		g2.translate(dX,dY);
		g2.rotate(rotation/360f*2*Math.PI);
		final List<TextPosition>texts=new ArrayList();
		final Map<TextPosition,Integer>colors=new HashMap();
		try{
			new PageDrawer(){
				@Override
				protected void processTextPosition(TextPosition text){
			  	if(!text.getCharacter().trim().equals(""))try{
						colors.put(text,getGraphicsState().getNonStrokingColor(
								).getJavaColor().getRGB());
						texts.add(text);
					}catch(IOException e){
						throw new RuntimeException(e);
					}
				}
			}.drawPage(g2,page,new Dimension((int)pageBox.width,(int)pageBox.height));
		}catch(IOException e){
			throw new RuntimeException(e);
		}
		if(fullChars.isEmpty())for(TextPosition text:texts)
	  		fullChars.add(new PageChar(fonts,text,rotation,colors.get(text)));
	}
}