package facets.util.app;
import static java.lang.Math.*;
import facets.util.Debug;
import facets.util.Tracer;
import facets.util.tree.ValueNode;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
public abstract class HostBounds extends Tracer{
	public final static String 
		NATURE_SIZER_DEFAULT="hostDefaultSizer",NATURE_SIZE_MIN="hostMinSize",
		STATE_BOUNDS_MASTER="masterBounds",STATE_BOUNDS_SLAVE="slaveBounds";
	private final ValueNode nature,state;
	public HostBounds(ValueNode nature,ValueNode state){
		this.nature=nature;
		this.state=state;
	}
	final public Rectangle newCheckedBounds(){
		boolean forSlave=forSlave();
		int[]masters=state.getInts(STATE_BOUNDS_MASTER),
			slaves=state.getInts(STATE_BOUNDS_SLAVE),
			ints=forSlave?slaves:masters;
		boolean noMasters=masters.length<4;
		if(ints.length<4){
			if(noMasters){
				double[]sizer=nature.getDoubles(NATURE_SIZER_DEFAULT);
				if(sizer.length>=2){
					Dimension screen=screenSize();
					boolean literals=sizer[0]*sizer[1]>1;
					ints=new int[]{
						(int)(sizer[0]*(literals?1:screen.width)),
						(int)(sizer[1]*(literals?1:screen.height))
					};
				}
				else if((ints=nature.getInts(NATURE_SIZE_MIN)).length<2)
					throw new IllegalStateException("No size in nature="+nature);
			}
			else ints=new int[]{masters[2],masters[3]};
			ints=new int[]{0,0,ints[0],ints[1]};
			if(forSlave){
				int offset=25;
				ints[0]=offset+(noMasters?0:masters[0]);
				ints[1]=offset+(noMasters?0:masters[1]);
			}
		}
		Dimension size=minAdjustedSize(ints[2],ints[3]);
		return new Rectangle(ints[0],ints[1],size.width,size.height);
	}
	/**
	Current window bounds unless maximised or too small.  
	 */
	final public int[]storableWindowBounds(){
		Rectangle bounds=windowBounds();
		Dimension size=bounds.getSize();
		if(size.width>=screenSize().width)return null;
	  bounds=new Rectangle(bounds.getLocation(),minAdjustedSize(size.width,size.height));
		return new int[]{bounds.x,bounds.y,bounds.width,bounds.height};
	}
	final public void putBounds(){
		int[]bounds=storableWindowBounds();
		if(bounds!=null)state.put(forSlave()?STATE_BOUNDS_SLAVE:STATE_BOUNDS_MASTER,bounds);
	}
	final public Point calculateSmartDialogAt(Dimension box){
		Dimension screen=screenSize();
		double screenRatio=screen.width/(double)screen.height;
	  Rectangle bounds=windowBounds();
	  if(bounds.width==0)bounds=new Rectangle(screen);
	  if(bounds.x+bounds.width>screen.width)screen.width*=2;
	  int screenX=screen.width/2,hostX=bounds.x+bounds.width/2,
	  	atX=screenX+(hostX-screenX)/2-box.width/2,
	  	screenY=screen.height/2,hostY=bounds.y+bounds.height/2,
	  	atY=screenY+(hostY-screenY)/2-box.height/2;
	  return new Point(atX,atY);
	}
	private Dimension minAdjustedSize(int width,int height){
		int[]ints=nature.getInts(NATURE_SIZE_MIN);
		if(ints.length<2)throw new IllegalStateException("Empty ints for "+NATURE_SIZE_MIN);
		else if(false)trace(".minAdjustedSize: ints=",nature.get(NATURE_SIZE_MIN));
		return new Dimension(max(width,ints[0]),max(height,ints[1]));
	}
	protected boolean forSlave(){
		return false;
	}
	/**
	Find the current screen size.
	 */
	protected abstract Dimension screenSize();
	/**
	Find the current window bounds. 
	 */
	protected abstract Rectangle windowBounds();
}