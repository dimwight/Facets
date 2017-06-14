package facets.core.app.avatar;
import facets.util.geom.Vector;
/**
Policy for a custom viewer that plots to an unbounded plane.     
 */
public interface PlaneView extends AvatarView{
	String ZOOM_OUT="Zoom &Out",ZOOM_IN="Zoom &In";
	final public static double INCH_PTS=72;
	/**
	The minimum unscaled height of the viewport onto the plane. 
	@return a positive <code>double</code>
	 */
	double showHeight();
	/**
	The minimum unscaled width of the viewport onto the plane. 
	@return a positive <code>double</code>
	 */
  double showWidth();
  /**
  Which way up is the unscaled Y axis?. 
  @return either +1 or -1. 
   */
  int ySign();
  /**
  The location of the viewport origin. 
  @return any {@link Vector}
   */
  Vector plotShift();
	/**
	Should the view scale be adjusted when the viewer is resized? 
	 */
	boolean scaleToViewer();
	/**
	Set the view area. 
	@param width will be returned as {@link #showWidth()}
	 @param height will be returned as {@link #showHeight()}
	 @param plotShift will be returned as {@link #plotShift()}
	 @param scale will be returned as 
	 */
	void setShowValues(double width,double height,Vector plotShift,double scale);
  /**
  Identifier that should change with each change in view properties. 
  @return typically an {@link Integer} containing an auto-incremented value
   */
  Object stamp();
	double scale();
}
