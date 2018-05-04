package applicable;
import static facets.util.Regex.*;
import static facets.util.Strings.*;
import facets.core.app.avatar.PainterMaster.Outlined;
import facets.util.Debug;
import facets.util.Strings;
import facets.util.geom.Vector;
import facets.util.shade.Shade;
import facets.util.tree.ValueNode;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
public final class SvgPath extends SvgShape{
	public static boolean printArguments=false;
	public SvgPath(ValueNode values){
		super(values);
	}
	public SvgPath(String title,String data,int shift){
		this(new ValueNode(TYPE_PATH,title,new Object[]{
			KEY_DATA+"="+data,	
			KEY_SHIFT+"="+shift,	
		}));
	}
	final public static String TYPE_PATH="path",
		KEY_DATA="d",KEY_RAW="raw",KEY_SHIFT="shift",
		SEG_MOVE_ABS="moveAbsolute",SEG_MOVE="move",
		SEG_LINE="line",SEG_CUBIC="cubic";
	private final class Segment{
		final String type;
		final double[]vals;
		boolean closes;
		Segment(String type,double[]vals){
			this.type=type;
			this.vals=vals;
		}
		@Override
		public String toString(){
			return Debug.info(this)+" type="+type+" vals="+Strings.sfString(vals);
		}
	}
	private class SegmentIterator{
		private final Segment[]segments;
		SegmentIterator(Segment[]segments){
			this.segments=segments;
		}
		final public void iterateSegments(){
			for(Segment segment:segments){
				String type=segment.type;
				double[]vals=segment.vals;
				if(type.equals(SEG_CUBIC))
					nextCubicSegment(vals[0],vals[1],vals[2],vals[3],
							vals[4],vals[5]);
				else if(type.equals(SEG_LINE))
					nextLineSegment(vals[0],vals[1]);
				else if(type.equals(SEG_MOVE)||type.equals(SEG_MOVE_ABS))
					nextMoveSegment(vals[0],vals[1]);
				if(segment.closes)closeSegment();
			}
		}
		protected void nextCubicSegment(double cx1,double cy1,double cx2,double cy2,
				double x2,double y2){
			throw new RuntimeException("Not implemented in "+Debug.info(this));
		}
		protected void nextLineSegment(double x,double y){
			throw new RuntimeException("Not implemented in "+Debug.info(this));
		}
		protected void nextMoveSegment(double x,double y){
			throw new RuntimeException("Not implemented in "+Debug.info(this));
		}
		protected void closeSegment(){
			throw new RuntimeException("Not implemented in "+Debug.info(this));
		}
	}
	public Outlined newOutlined(Shade fill,Shade pen,boolean pickable){
		final int shift=values.getInt(KEY_SHIFT);
		final String data=values.getString(KEY_DATA);
		List<Segment>segments=new ArrayList();
		final String _runData="[^MmCcLl]+",_anyRun="[MmCcLl]"+_runData,
			_moveOrLineRun="[MmLl]";
		trace(" title=" +title()+" shift="+shift+" data=" +data);
		boolean firstRun=true;
		final double factor=Math.pow(10,shift);
		double pathX=0,pathY=0;
		for(String run:finds(data,_anyRun)){
			trace(" run=" +run);
			String runCode=run.substring(0,1),
				type=runCode.equals("M")?SEG_MOVE_ABS
					:runCode.equals("c")?SEG_CUBIC
					:runCode.equals("m")?SEG_MOVE:SEG_LINE;
			if(firstRun&&!type.equals(SEG_MOVE_ABS))throw new RuntimeException(
					"No MOVE_ABS in first run="+run);
			else if(!firstRun&&type.equals(SEG_MOVE_ABS))throw new RuntimeException(
						"Not implemented extra MOVE_ABS in run="+run);
			else firstRun=false;
			boolean closing=run.substring(run.length()-1).matches("\\D");
			if(closing)run=run.substring(0,run.length()-1);
			run=find(run,_runData).replaceAll("\\s+",",");
			trace(" type="+type+" closing="+closing);
			Segment seg=null;
			int tupleSize=type.equals(SEG_CUBIC)?6:2;
			for(String tuple:finds(run+",","([^,]+,){" +tupleSize +"}")){
				double[]vals=toDoubles(tuple);
				final int xAt=vals.length-2,yAt=vals.length-1;
				for(int i=0;i<vals.length;i++)
					vals[i]=vals[i]/factor+(type.equals(SEG_MOVE_ABS)?0:i%2==0?pathX:pathY);
				segments.add(seg=new Segment(type,vals));
				pathX=vals[xAt];pathY=vals[yAt];
				trace(" tuple=" +tuple+" > "+seg);
			}
			if(seg==null)throw new IllegalStateException(
					"Null currentSeg in "+Debug.info(this));
			else if(closing)seg.closes=true;
		}
		final Path2D path=new Path2D.Double();
		new SegmentIterator(segments.toArray(new Segment[]{})){
			protected void nextCubicSegment(double cx1,double cy1,double cx2,double cy2,
					double x,double y){
				path.curveTo(cx1,cy1,cx2,cy2,x,y);
			}
			protected void nextLineSegment(double x,double y){
				path.lineTo(x,y);
			}
			protected void nextMoveSegment(double x,double y){
				path.moveTo(x,y);
			}
			protected void closeSegment(){
				path.closePath();
			};
		}.iterateSegments();
		return new Outlined(fill,pen,pickable){
			@Override
			protected Object[]lazySubHashables(){
				return new Object[]{data};
			}
			public Object getOutline(){
				return path;
			}
			public Vector bounds(){
				Rectangle2D rect=path.getBounds2D();
				return new Vector(rect.getWidth(),rect.getHeight());
			}
			public Shade getFill(){
				return fill;
			}
			public Scaling scaling(){
				return Scaling.OUTLINE;
			}
			@Override
			public String toString(){
				return title();
			}
		};
	}
	final protected void traceOutput(String msg){
		if(false)System.out.println(SvgPath.class.getSimpleName()+":"+msg);
	}
}
