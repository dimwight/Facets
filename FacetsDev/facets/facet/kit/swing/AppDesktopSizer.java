package facets.facet.kit.swing;
import static java.lang.Math.*;
import facets.facet.kit.swing.AppDesktop.FrameSignals;
import facets.util.Debug;
import facets.util.Tracer;
import facets.util.Util;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
final class AppDesktopSizer extends Dimension{
	private final Tracer t=Tracer.newTopped(getClass().getSimpleName().replace("AppDesktop",""),
			false);
	final int iconGap;
	private final double shrinkH;
	AppDesktopSizer(Dimension size,int iconGap){
		super(size);
		this.iconGap=iconGap;
		shrinkH=(double)(height-this.iconGap)/height;
	}
	/**
	Adjusts frames to current size and icon gap. 
	<p>Called (with size change shown) by  
	<ul>
		<li>{@link AppDesktop#updateLayout(AppDesktopSizer)} (maybe with old dimensions)
		<li>{@link FrameSignals#internalFrameIconified(InternalFrameEvent)}
		 of the first {@link AppDesktopFrame} to be iconified (old gap is zero)
		<li>{@link AppDesktop#tileWindows()} (unchanged) 
	</ul>
	*/
	void adjustFrames(Collection<AppDesktopFrame>frames,boolean scaleFrames,
			AppDesktopSizer thenSize){
		if(!equals(getSize()))throw new IllegalStateException(
				"Bad nowSize="+this);
		else t.trace(".adjustFrames: thenSize="+thenSize+" nowSize="+this);
		final double ratioW=!scaleFrames?1:(double)width/thenSize.width,
			ratioH=(!scaleFrames?1d:(double)height/thenSize.height)*shrinkH;
		for(AppDesktopFrame frame:frames){
			Rectangle bounds=frame.isMaximum()?frame.getRestoreBounds():frame.getBounds();
			frame.setRestoreBounds(new Rectangle(
					(int)round(bounds.x*ratioW),
					(int)round(bounds.y/thenSize.shrinkH*ratioH),
					(int)round(bounds.width*ratioW),
					(int)round(bounds.height/thenSize.shrinkH*ratioH)
				));
		}
		t.trace(".~adjustFrames: ",frames.size());
	}
	/**
	Sets restore sizes for all non-iconified frames.
	<p>Calculates tile sizes assuming no icon gap; called from
	<ul>
		<li>{@link AppDesktop#updateLayout()} where content has changed
		<li>{@link AppDesktop#tileWindows()} (client call) 
	</ul>
	 */
	void tileFrames(Iterable<AppDesktopFrame>frames){
		if(!this.equals(getSize()))throw new IllegalStateException(
				"Bad nowSize="+this);
		else t.trace(".tileFrames: nowSize=",this);
		List<JInternalFrame>tiles=new ArrayList();
		for(JInternalFrame frame:frames)
			if(!frame.isIcon())tiles.add(frame);
		int splitLong,splitShort,tileCount=tiles.size();
		switch(tileCount){
		case 0:return;
		case 1:splitLong=1;splitShort=1;break;
		case 2:splitLong=2;splitShort=1;break;
		case 3:case 4:splitLong=2;splitShort=2;break;
		case 5:case 6:splitLong=3;splitShort=2;break;
		case 7:case 8:case 9:splitLong=3;splitShort=3;break;
		case 10:case 11:case 12:splitLong=4;splitShort=3;break;
		case 13:case 14:case 15:case 16:splitLong=4;splitShort=4;break;
		default:
			throw new RuntimeException("Not implemented for tileCount="+tileCount+
					"in "+Debug.info(this));
		};
		boolean wide=width>height;
		double width=(double)this.width/(wide?splitLong:splitShort),
			height=(double)this.height/(!wide?splitLong:splitShort);
		for(int at=0;at<tileCount;at++){
			int divisor=wide?splitLong:splitShort,row=at/divisor,col=at%divisor;
			double useWidth=width*(at<tileCount-1?1:splitLong*splitShort-at);
			((AppDesktopFrame)tiles.get(at)).setRestoreBounds(new Rectangle(
					(int)rint(col*width),(int)rint(row*height*shrinkH),
					(int)rint(useWidth),(int)rint(height*shrinkH)));
		}
	}
	public String toString(){
		return "["+height+","+iconGap+","+Util.sf(shrinkH)+"]";
	}
}