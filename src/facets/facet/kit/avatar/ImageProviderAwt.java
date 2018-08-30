package facets.facet.kit.avatar;
import static facets.util.Util.*;
import static java.awt.image.Raster.*;
import facets.facet.kit.avatar.ImageProviderAwt.Packer;
import facets.util.Bytes;
import facets.util.Debug;
import facets.util.Times;
import facets.util.Util;
import facets.util.app.ProvidingCache;
import facets.util.app.ProvidingCache.ItemProvider;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
public abstract class ImageProviderAwt extends ItemProvider<Packer>{
	protected final static class Packer{
		private final transient BufferedImage asIs;
		private final int width,height,size;
		private final byte[]packed;
		public Packer(BufferedImage image,boolean noPack){
			width=image.getWidth();
			height=image.getHeight();
			if(noPack){
				asIs=image;
				packed=new byte[0];
				size=0;
				return;
			}
			asIs=null;
			DataBufferInt buffer=(DataBufferInt)image.getData().getDataBuffer();
			packed=Bytes.pack(buffer.getBankData(),false);
			size=buffer.getSize();
		}
		public BufferedImage restoreImage(){
			if(asIs!=null)return asIs;
			timeRestore(" packed="+kbs(packed.length));
			int[][]ints=(int[][])Bytes.unpack(packed);
			timeRestore(" unpack="+mbs(ints[0].length*Integer.SIZE/8));
			BufferedImage image=newPaintableImage(width,height,Color.black);
			image.setData(createRaster(image.getSampleModel(),
					new DataBufferInt(ints,size),null));
			timeRestore("~");
			return image;
		}
		private void timeRestore(String msg){
			if(false)Times.printElapsed("ImageProviderAwt.restoreImage"+msg);
		}
		@Override
		public String toString(){
			return Debug.info(this)+" packed="+Util.kbs(packed.length);
		}
	}
	protected final int width,height;
	public ImageProviderAwt(ProvidingCache c,Object source,String title,int width,
			int height){
		super(c,source,ImageProviderAwt.class.getSimpleName()+"."+title);
		this.width=width;
		this.height=height;
	}
	@Override
	final protected Packer newItem(){
		return new Packer(newPaintedImage(width,height),!packImage());
	}
	protected abstract BufferedImage newPaintedImage(int width,int height);
	protected boolean packImage(){
		return false;
	}
	final public Image getImageForValues(Object... values){
		Packer packer=getForValues(values);
		return packer.asIs!=null?packer.asIs:packer.restoreImage();
	}
	@Override
	protected long buildByteCount(){
		return width*height*4*5;
	}
	@Override
	protected long finalByteCount(Packer p){
		int count=p.packed.length;
		return count>0?count:width*height*4;
	}
	public static BufferedImage newPaintableImage(int width,int height,Color c){
		BufferedImage image=new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
		Graphics g=image.getGraphics().create();
		g.setColor(c);
		g.fillRect(0,0,width,height);
		return image;
	}
}