package facets.core.app.avatar;
import static facets.util.Debug.*;
import static facets.util.Util.*;
import static java.lang.Double.*;
import facets.core.app.SViewer;
import facets.core.superficial.SFrameTarget;
import facets.core.superficial.STarget;
import facets.core.superficial.SToggling;
import facets.core.superficial.app.SSelection;
import facets.util.geom.Line;
import facets.util.geom.Point;
import facets.util.geom.Vector;
import facets.util.shade.Shades;
/**
{@link AvatarViewWorks} that implements {@link PlaneView}.
<p>Also has a number of utility class methods.  
 */
public class PlaneViewWorks extends AvatarViewWorks implements PlaneView{
	final public static int LINES_SINGLE=1,LINES_GROUP=4,
		MODEL_CONSTRAIN=0,MODEL_SNAP=1,MODEL_EDIT=2,MODEL_ELEMENTS=3;
	private final PlaneGrid grid;
	private final double sizeWidth,sizeHeight;
  private boolean doSnap=true,doConstrain=true,doEdit=true;  
	private double showHeight=NaN,showWidth=NaN,scale=NaN;
	private Vector plotShift=new Vector(0,0);
	private int stamp;
	public PlaneViewWorks(String title,double sizeWidth,double sizeHeight,
			Vector plotShift,AvatarPolicies avatars){
		super(title,avatars);
		grid=null;
		setShowValues(this.sizeWidth=sizeWidth,this.sizeHeight=sizeHeight,plotShift,1);
	}
	public PlaneViewWorks(String title,PlaneGrid grid,AvatarPolicies avatars){
		super(title,avatars);
  	this.grid=grid;
		Vector show=grid.newShow();
		setShowValues(sizeWidth=show.x,sizeHeight=show.y,grid.insets(),1);
	}
	@Override
	public void setShowValues(double width,double height,Vector plotShift,double scale){
		if((showWidth=width)<0||(showHeight=height)<0)throw new IllegalArgumentException(
				"Invalid size width="+fx(width)+", height="+fx(height)+" in "+info(this));
		else if((this.plotShift=plotShift)==null)throw new IllegalArgumentException(
				"Null plotShift in "+info(this));
		else if((this.scale=scale)!=scale)throw new IllegalArgumentException(
				"Scale NaN in "+info(this));
		else stamp++;
		if(grid!=null&&!scaleToViewer())grid.sizeToViewer(new Vector(width,height));
	}
	@Override
  public void setState(Object state){
		PlaneViewWorks src=(PlaneViewWorks)state;
		showHeight=src.showHeight;showWidth=src.showWidth;
	}
	@Override
  final public String toString() {
		return true?super.toString()
				:("showWidth="+showWidth+", showHeight="+showHeight+" plotShift="+plotShift);
	}
	@Override
	final public Vector plotShift(){
		return grid!=null?grid.insets():plotShift;
	}
	@Override
	final public double scale(){
		return scale;
	}
	@Override
	final public double showHeight(){
		return showHeight;
	}
	@Override
	final public double showWidth(){
		return showWidth;
	}
	@Override
	public boolean scaleToViewer(){
		return true;
	}
	@Override
	public int ySign(){
		return 1;
	}
	@Override
	public Object backgroundStyle(){
		return Shades.white;
	}
	@Override
	public boolean isLive(){
		return doEdit;
	}
	@Override
	final public int markLeapPixels(){
		return markPixels()/2+pickHitPixels();
	}
	@Override
	public int markPixels(){
		return 6;
	}
	@Override
	public int pickHitPixels(){
		return 2;
	}
	@Override
	public Object stamp(){
		return stamp;
	}
	@Override
	public SSelection newViewerSelection(SViewer viewer,SSelection viewable){
		return avatars().newAvatarSelection(viewer,viewable);
	}
	public SFrameTarget newFrame(){
	  return new SFrameTarget(this){
	  	@Override
	    protected STarget[]lazyElements(){
	  	  final STarget constrain=new SToggling("Constrain",doConstrain,
	  	      new SToggling.Coupler(){
	  	    public void stateSet(SToggling t){doConstrain=t.isSet();}
	  	  }),
	  	  snap=new SToggling("Snap",doSnap,new SToggling.Coupler(){
	  	    public void stateSet(SToggling t){doSnap=t.isSet();}
	  	  }),
	  	  edit=new SToggling("Editable",doEdit,new SToggling.Coupler(){
	  	    public void stateSet(SToggling t)
	  	    	{doEdit=t.isSet();snap.setLive(doEdit);constrain.setLive(doEdit);}
	  	  });
	  	  return new STarget[]{constrain,snap,edit};
	  	}
	  };
	}
	/**
	Generate sets of abstract lines. 
	 <p>The number and arrangement of lines is unspecified, but each set will fit
	 within a box proportioned as defined by <code>width</code> and <code>height</code>; 
	 the sets will be generated right and down from <code>at</code>. 
	 @param at the starting point for generating line sets
	 @param width the width of the defining bounding box
	 @param height the height of the defining bounding box
	 @param count how many line sets to generate (the more, the smaller)
	 */
	public static Line[][]newLineSets(Point at,double width,double height,
			int count){
		Line[][]lineSets=new Line[count][];
		for(int i=0;i<lineSets.length;i++)
			lineSets[i]=newTestLineSet(1d/count,at,height,width,count-i);
		return lineSets;
	}
	public static Line[]newTestLineSet(double scale,Point boxAt,double height,
			double width,int shiftCount){
		final double left=boxAt.x(),top=boxAt.y();
	  Vector shift=scale==1?new Vector(0,0):
	  		new Vector(width/shiftCount-scale,-height/shiftCount+scale);
	  Point tl=new Point(left,top),tr=new Point(left+width*scale,top),
	    br=new Point(left+width*scale,top-height*scale),
	    bl=new Point(left,top-height*scale);
	  return new Line[]{
	    new Line(tl.shifted(shift),tr.shifted(shift)),
	    new Line(tr.shifted(shift),br.shifted(shift)),
	    new Line(br.shifted(shift),bl.shifted(shift)),
	    new Line(bl.shifted(shift),tl.shifted(shift)),
	  };
	}
	private boolean doConstrain_(){
		return doConstrain;
	}
	private boolean doSnap_(){
		return doSnap;
	}
}
