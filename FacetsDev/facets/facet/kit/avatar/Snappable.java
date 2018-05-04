package facets.facet.kit.avatar;
import facets.util.geom.Point;
import facets.util.geom.Vector;
interface Snappable{
  Vector checkSnap(AvatarCanvas canvas,Point check,double snapGap);
}
