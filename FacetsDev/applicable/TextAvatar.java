package applicable;
import facets.core.app.avatar.AvatarContent;
import facets.core.app.avatar.Painter;
import facets.core.app.avatar.Pickable;
import facets.util.Debug;
import facets.util.geom.Point;
import facets.util.shade.Shade;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.io.Serializable;
public interface TextAvatar extends AvatarContent,Pickable{
	public final class BoundsBox implements Serializable{
		public final double left,top,right,bottom,width,height;
		public BoundsBox(double left,double top,double right,double bottom){
			this.left=left;
			this.top=top;
			this.right=right;
			this.bottom=bottom;
			width=right-left;
			height=bottom-top;
			if(width<0)throw new IllegalStateException(
					"Negative width=" +width+" in "+Debug.info(this));
			else if(height<0)throw new IllegalStateException(
					"Negative height=" +height+" in "+Debug.info(this));
		}
		public boolean contains(double x,double y){
			return asRectangle().contains(x,y);
		}
		public Rectangle2D asRectangle(){
			return new Rectangle2D.Double(left,top,width,height);
		}
		public boolean contains(BoundsBox bounds){
			return asRectangle().contains(bounds.asRectangle());
		}
		public Point getCenter(){
			throw new RuntimeException("Not implemented in "+Debug.info(this));
		}
	}
	String getText();
	BoundsBox getBounds();
	Painter newViewPainter(boolean selected);
	Painter[]newPickPainters(boolean selected);
}
