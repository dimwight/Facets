package applicable.avatar;
import static facets.core.app.SViewer.*;
import static facets.core.app.avatar.AvatarView.Direction.*;
import static facets.core.app.avatar.Painter.Style.*;
import static facets.util.shade.Shades.*;
import facets.core.app.avatar.AvatarPart;
import facets.core.app.avatar.Painter;
import facets.core.app.avatar.PainterSource;
import facets.core.app.avatar.PickPainter;
import facets.core.app.avatar.PlaneGrid;
import facets.core.app.avatar.AvatarView.Direction;
import facets.core.app.avatar.Painter.Style;
import facets.util.geom.Line;
import facets.util.geom.Point;
import facets.util.geom.Vector;
import facets.util.shade.Shade;
import java.util.Arrays;
final public class BoxPartsGrid extends PlaneGrid{
	public static final String TYPE_FRAME="BoxFrame",TYPE_SIDE_GRAB="BoxSideGrab",
		TYPE_CORNER_GRAB="BoxCornerGrab";
	public BoxPartsGrid(PlaneGrid grid){
		super(grid);
	}
	public AvatarPart newFrame(final BoxValues box,final Style style,final boolean filled,
			PainterSource p){
		return new AvatarPart<BoxValues>(p,TYPE_FRAME){
			final double x=sizedX(Math.min(box.left(),box.right())),
				y=sizedY(Math.min(box.top(),box.bottom())),width=sizedX(box.width()),
				height=sizedY(box.height());
			Vector shift;
			double startL,startR,startT,startB;
			@Override
			protected PickPainter newPickPainter(PainterSource p,boolean pickable){
				return(PickPainter)(filled?p.bar(x,y,width,height,cyan,pickable)
					:p.rectangle(x,y,width,height,"pickable shadePen="+styleShade(style).title()));
			}
			@Override
			public int pickCursor(){
				return true||isRect(box)?CURSOR_MOVE:CURSOR_DEFAULT;
			}
			@Override
			public int hashCode(){
				return Arrays.hashCode(new Object[]{box,style,filled});
			}
			@Override
			public void adjustContentForDrag(BoxValues box,Point dragAt){
				dragAt=unsized(dragAt);
				if(shift==null){
					startL=box.left();
					startR=box.right();
					startT=box.top();
					startB=box.bottom();
					shift=new Point(startL,startT).jumpFrom(dragAt);
				}
				Vector jump=dragAt.shifted(shift).jumpFrom(new Point(startL,startT));
				box.setLeft(startL+jump.x);
				box.setRight(startR+jump.x);
				box.setTop(startT+jump.y);
				box.setBottom(startB+jump.y);
			}
		};
	}
	public AvatarPart[]newSideGrabs(BoxValues box,Style style,PainterSource p){
		double left=sizedX(box.left()),right=sizedX(box.right()),top=sizedY(box.top()),
			bottom=sizedY(box.bottom());
		Point lt=new Point(left,top),rt=new Point(right,top),
			rb=new Point(right,bottom),lb=new Point(left,bottom);
		Shade shade=styleShade(style);
		return false&&isRect(box)?new AvatarPart[]{
				newSideGrab(new Line(rb,lb),S,shade,p),
				newSideGrab(new Line(rt,rb),E,shade,p),
		} 
		:new AvatarPart[]{
				newSideGrab(new Line(lt,rt),N,shade,p),
				newSideGrab(new Line(rt,rb),E,shade,p),
				newSideGrab(new Line(rb,lb),S,shade,p),
				newSideGrab(new Line(lb,lt),W,shade,p)
		};
	}
	private AvatarPart newSideGrab(final Line line,final Direction d,final Shade shade,
			PainterSource p){
		return new AvatarPart<BoxValues>(p,TYPE_SIDE_GRAB){
			double shift=Double.NaN;
			private Point from;
			@Override
			protected PickPainter newPickPainter(PainterSource p,boolean pickable){
				return (PickPainter)p.pointMark(line.from.shifted(line.to.jumpFrom(line.from).scaled(0.5)),
							shade,true);
			}
			@Override
			public int hashCode(){
				return Arrays.hashCode(new Object[]{line,shade.rgb()});
			}
			@Override
			public
			int pickCursor(){
				return d==N?CURSOR_N:d==E?CURSOR_E:d==S?CURSOR_S:CURSOR_W;
			}
			@Override
			public
			void adjustContentForDrag(BoxValues box,Point dragAt){
				dragAt=unsized(dragAt);
				double x=dragAt.x(),y=dragAt.y();
				if(shift!=shift){
					switch(d){
					case N:shift=box.top()-y;break;
					case E:shift=box.right()-x;break;
					case S:shift=box.bottom()-y;break;
					case W:shift=box.left()-x;
					}
				}
				switch(d){
				case N:box.setTop(y+shift);break;
				case E:box.setRight(x+shift);break;
				case S:box.setBottom(y+shift);break;
				case W:box.setLeft(x+shift);
				}
			}
		};
	}
	public AvatarPart[]newCornerGrabs(BoxValues box,PainterSource p){
		double left=sizedX(box.left()),right=sizedX(box.right()),
				top=sizedY(box.top()),bottom=sizedY(box.bottom());
		return false&&isRect(box)?new AvatarPart[]{
				newCornerGrab(new Point(right,bottom),SE,p),
			}
		:new AvatarPart[]{
			newCornerGrab(new Point(left,top),NW,p),
			newCornerGrab(new Point(right,top),NE,p),
			newCornerGrab(new Point(right,bottom),SE,p),
			newCornerGrab(new Point(left,bottom),SW,p)
		};
	}
	private AvatarPart newCornerGrab(final Point at,final Direction type,PainterSource p){
		final int hash=Arrays.hashCode(new Object[]{at,type});
		return new AvatarPart<BoxValues>(p,TYPE_CORNER_GRAB){
			public int hashCode(){
				return hash;
			};
			Vector shift;
			double opposedX,opposedY;
			int pickCursor=type==NW?CURSOR_NW:type==NE?CURSOR_NE:type==SE?CURSOR_SE:CURSOR_SW;
			@Override
			public void adjustContentForDrag(BoxValues box,Point dragAt){
				dragAt=unsized(dragAt);
				boolean western=type==NW||type==SW,northern=type==NW||type==NE;
				if(shift==null){
					shift=unsized(at).jumpFrom(dragAt);
					opposedX=sizedX(western?box.right():box.left());
					opposedY=sizedY(northern?box.bottom():box.top());
				}
				dragAt=dragAt.shifted(shift);
				double x=dragAt.x(),y=dragAt.y();
				if(western)box.setLeft(x);
				else box.setRight(x);
				if(northern)box.setTop(y);
				else box.setBottom(y);
				double sizedX=sizedX(x),sizedY=sizedY(y);
				pickCursor=sizedX<opposedX?sizedY<opposedY?CURSOR_NW:CURSOR_SW
					:sizedY<opposedY?CURSOR_NE:CURSOR_SE;
			}
			@Override
			protected PickPainter newPickPainter(PainterSource p,boolean pickable){
				return(PickPainter)p.pointMark(at,styleShade(Selected),pickable);
			}
			@Override
			public int pickCursor(){
				return pickCursor;
			}
		};
	}
	public Painter newCaption(BoxValues box,Style style,PainterSource p){
		return p.textCaption(box.title(),sizedX(box.left())+5,sizedY(box.top())-5,
				"face=Sanserif","points=16",
				"anchor=NW","background=lightgray",
				"shade="+styleShade(style).title());
	}
	private boolean isRect(BoxValues box){
		return box.data.type().equals(BoxValues.TYPE_RECT);
	}
	private static Shade styleShade(Style style){
		return style==Dragging?gray:style==Plain?green:style==Selected?red:green.brighter();
	}
}