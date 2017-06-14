package facets.core.app.avatar;
import facets.util.geom.Point;
/**
Policy for a viewer that zooms and pans over a bounded plane area.    
 */
public interface ZoomPanView extends PlaneView{
  /**
  The unscaled distance from the viewport origin to the left edge 
  of the bounded plane. 
  @return any <code>double</code>
   */
  double originToLeft();
  /**
  The unscaled distance from the viewport origin to the top edge 
  of the bounded plane. 
  @return any <code>double</code>
   */
  double originToTop();
  /**
  The unscaled height of the bounded plane. 
  @return a positive <code>double</code>
   */
  double planeHeight();
  /**
  The unscaled width of the bounded plane. 
  @return a positive <code>double</code>
   */
  double planeWidth();
  /**
  The last position set with {@link #setFocusAt(Point)}. 
   */
  Point focusAt();
	/**
  Set a position within the bounded plane that should appear as near as possible
  to the centre of the viewport. 
  @param at should define a position within the bounded plane
   */
  void setFocusAt(Point at);
}
