package facets.facet.kit.avatar;
import facets.core.superficial.app.SViewer;
import facets.util.Debug;
import facets.util.geom.Angle;
import facets.util.geom.Point;
import facets.util.geom.Vector;
abstract class DragConstraints{
  public final static int NO_SECTOR=0,NORTH=0X1,SOUTH=0X2,EAST=0X4,WEST=0X8,
    NORTH_EAST=0X10,SOUTH_WEST=0X20,NORTH_WEST=0X40,SOUTH_EAST=0X80;  
  public final double lockGap;
  private int inSector;  
  final public static DragConstraints newCross(double lockGap){
    return new DragConstraints(lockGap){
      protected int inSectorNorthEast(Vector shift)
        {if(shift.y>shift.x)return NORTH;else return EAST;}
      protected int inSectorNorthWest(Vector shift)
        {if(shift.y>-shift.x)return NORTH;else return WEST;}
      protected int inSectorSouthEast(Vector shift)
        {if(-shift.y>shift.x)return SOUTH;else return EAST;}
      protected int inSectorSouthWest(Vector shift)
        {if(-shift.y>-shift.x)return SOUTH;else return WEST;}
      int dragCursor(){
        int in=inSector();
        return in==NORTH||in==SOUTH?SViewer.CURSOR_N:
            in==WEST||in==EAST?SViewer.CURSOR_W:
              SViewer.CURSOR_MOVE;
      }
    };
  } 
  final public static DragConstraints newSixAxial(double lockGap){
    return new DragConstraints(lockGap){
      protected int inSectorNorthWest(Vector shift)
        {if(shift.y>-shift.x)return NORTH;else return WEST;}
      protected int inSectorSouthEast(Vector shift)
        {if(-shift.y>shift.x)return SOUTH;else return EAST;}
      int dragCursor(){return SViewer.CURSOR_MOVE;}
    };
  }
  final public static DragConstraints newThreeAxial(double lockGap){
    return new DragConstraints(lockGap){
      protected int inSectorNorthEast(Vector shift)
        {if(shift.y>shift.x)return NORTH;else return EAST;}
      protected int inSectorNorthWest(Vector shift)
        {if(shift.y*1.3>-shift.x)return NORTH;else return SOUTH_WEST;}
      protected int inSectorSouth(Vector shift)
        {if(shift.x<0||-shift.y<shift.x*1.3)return SOUTH_WEST;else return EAST;}
      protected int inSectorSouthEast(Vector shift)
        {if(-shift.y>shift.x*1.3)return SOUTH_WEST;else return EAST;}
      protected int inSectorWest(Vector shift)
        {if(shift.y>0&&shift.y*1.3>-shift.x)return NORTH;else return SOUTH_WEST;}
      int dragCursor(){return SViewer.CURSOR_MOVE;}
    };
  }
  final public static String sectorString(int inSector){
    switch(inSector){
    case NORTH:return "NORTH";
    case SOUTH:return "SOUTH";
    case EAST:return "EAST";
    case WEST:return "WEST";
    case NORTH_EAST:return "NORTH_EAST";
    case SOUTH_WEST:return "SOUTH_WEST";
    case NORTH_WEST:return "NORTH_WEST";
    case SOUTH_EAST:return "SOUTH_EAST";
    case NO_SECTOR:return "NO_SECTOR";
    default:throw new IllegalArgumentException("Bad sector " + inSector);
    }		
  }
  protected DragConstraints(double lockGap){this.lockGap=lockGap;}
  final public int inSector(){return inSector;}
  protected int inSectorEast(Vector shift){return EAST;}
  protected int inSectorNorth(Vector shift){return NORTH;}
  protected int inSectorNorthEast(Vector shift){return NORTH_EAST;}
  protected int inSectorNorthWest(Vector shift){return NORTH_WEST;}
  protected int inSectorSouth(Vector shift){return SOUTH;}
  protected int inSectorSouthEast(Vector shift){return SOUTH_EAST;}
  protected int inSectorSouthWest(Vector shift){return SOUTH_WEST;}
  protected int inSectorWest(Vector shift){return WEST;}
  Point constrain(Point anchor,Point mouseAt){
    double distance=anchor.distance(mouseAt);
    if(distance<lockGap){inSector=NO_SECTOR;return mouseAt;}
    if(inSector==NO_SECTOR)inSector=findSector(anchor,mouseAt);
    return constrainedAt(anchor,mouseAt,inSector);
  }
  Point constrainedAt(Point anchor,Point mouseAt,int inSector){
    final Vector shift=mouseAt.jumpFrom(anchor);
    double x=shift.x,y=shift.y;
    switch(inSector){
    case NORTH:case SOUTH:x=0;break;
    case EAST:case WEST:y=0;break;
    case NORTH_EAST:x=y=(x+y)/2;break;
    case SOUTH_WEST:x=y=(x+y)/2;break;
    case NORTH_WEST:x=-(y=(-x+y)/2);break;
    case SOUTH_EAST:x=-(y=(-x+y)/2);break;
    default:throw new IllegalArgumentException("Bad sector in " + inSector);
    }
    return anchor.shifted(new Vector(x,y));
  }
  abstract int dragCursor();
  private int findSector(Point anchor,Point mouseAt){
    final Angle checkTurn=new Angle(-Math.PI/8);
    Point checkAt=mouseAt.newPoint();checkAt.rotate(anchor,checkTurn);
    final Vector trueShift=mouseAt.jumpFrom(anchor),
      checkShift=checkAt.jumpFrom(anchor);
    double x=checkShift.x,y=checkShift.y;
    if(y>0&x>0&y>x)return inSectorNorth(trueShift);
    else if(y>0&x>0)return inSectorNorthEast(trueShift);
    else if(y>0&x<0&y>-x)return inSectorNorthWest(trueShift);
    else if(y>0&x<0)return inSectorWest(trueShift);
    else if(y<0&x<0&x<y)return inSectorSouthWest(trueShift);
    else if(y<0&x<0)return inSectorSouth(trueShift);
    else if(x<-y)return inSectorSouthEast(trueShift);		
    else return inSectorEast(trueShift);		
  }
}
