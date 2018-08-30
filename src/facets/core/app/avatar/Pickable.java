package facets.core.app.avatar;
import facets.util.geom.Point;
/**
Can be picked within a viewer controlled by {@link AvatarView}. 
 */
public interface Pickable{
  Object checkCanvasHit(Point canvasAt,double hitGap);
}
