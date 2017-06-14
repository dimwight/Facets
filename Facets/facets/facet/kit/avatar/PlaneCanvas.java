package facets.facet.kit.avatar;
import facets.core.app.avatar.PlaneView;
import facets.core.app.avatar.ZoomPanView;
import facets.core.superficial.Notifying.Impact;
import facets.core.superficial.app.ViewerTarget;
import facets.facet.kit.KViewer;
import facets.facet.kit.KitFacet;
import facets.util.Debug;
import facets.util.StringFlags;
import facets.util.Util;
import facets.util.geom.Point;
import facets.util.geom.Vector;
final class PlaneCanvas extends AvatarCanvas{
	interface PlaneHost extends CanvasHost{
		void adjustToPlane(int planeWidth,int planeHeight,int viewerLeft,int viewerTop);
	}
	Point origin;
	int paneWidth,paneHeight;
	double scale=false?Double.NaN:1;
	private boolean initialPortAtZeros=true;
	private PlaneView view;
	private Object stamp="No stamp";
	private void adjustToPaneSize(final int paneWidth,final int paneHeight){
		if(view==null&&paneHeight!=0){
			this.paneWidth=paneWidth;
			this.paneHeight=paneHeight;
		}
		if(view==null||paneHeight==0)return;
		final double showWidth=view.showWidth(),showHeight=view.showHeight();
		final Vector plotShift=view.plotShift();
		final boolean scaleToViewer=view.scaleToViewer();
		if(scale!=scale||scaleToViewer){
			scale=showWidth/showHeight>(double)paneWidth/(double)paneHeight?
					paneWidth/showWidth:paneHeight/showHeight;
			if(view instanceof ZoomPanView){
				ZoomPanView zoom=(ZoomPanView)view;
				double planeW=zoom.planeWidth(),planeH=zoom.planeHeight();
				origin=new Point(-zoom.originToLeft(),zoom.originToTop());
				Point planeFocus=plot(zoom.focusAt());
				double viewerLeft=scale(planeFocus.x())-paneWidth/2,
					viewerTop=scale(planeFocus.y())-paneHeight/2;
				((PlaneHost)host).adjustToPlane((int)scale(planeW),(int)scale(planeH),
						(int)viewerLeft,(int)viewerTop);
			}
			else origin=new Point((unscale(paneWidth)-showWidth)/2,(unscale(paneHeight)-showHeight)/2
					).shifted(plotShift);
		}
		if(!scaleToViewer){
			scale=1;
			double setWidth=unscale(paneWidth),setHeight=unscale(paneHeight);
			if(false)trace(".adjustToPaneSize: paneHeight="+paneHeight+
					" setHeight="+Util.sfs(setHeight));
			Vector setShift=false?new Vector(plotShift.x-(setWidth-showWidth)/2,
				plotShift.y-(setHeight-showHeight)/2*view.ySign()):plotShift;
			if(this.paneWidth!=paneWidth||this.paneHeight!=paneHeight||stamp==null){
				view.setShowValues(setWidth,setHeight,setShift,scale);
				stamp=view.stamp();
				scale=view.scale();
			}
			origin=false?new Point(setWidth/2,setHeight/2).shifted(view.plotShift())
					:new Point(view.plotShift());
			((ViewerTarget)viewer()).clearSelection();
		  ((KViewer)facet.base()).refresh(Impact.CONTENT);
		}
		if(false)trace(".adjustToPaneSize: showHeight="+showHeight+" scale=",scale);
		this.paneWidth=paneWidth;
		this.paneHeight=paneHeight;
	}
	double scale(double value){
		if(scale!=scale)throw new IllegalStateException("No scale in "+Debug.info(this));
		else return value*scale;
	}
	double unscale(double scaled){
		if(scale!=scale)throw new IllegalStateException("No scale in "+Debug.info(this));
		else return scaled/scale;
	}
	@Override
	protected boolean consumeMouseEvent(int type,double atX,double atY,int mods){
		if(scale!=scale)return false;
		Point planeAt=unplot(atX,atY);
		return super.consumeMouseEvent(type,planeAt.x(),planeAt.y(),mods);
	}
	public String toString(){
		return //Debug.info(this)+
		Util.fx(scale)+" "+new Point(paneWidth,paneHeight)+" "+origin;
	}
	PlaneCanvas(KitFacet facet,StringFlags hints){
		super(facet,hints);
	}
	@Override
	protected void refreshViewPainters(){
		PlaneView viewNow=planeView();
		Object stampNow=viewNow.stamp();
		if(!viewNow.equals(view)||!stampNow.equals(stamp)){
			view=viewNow;
			stamp=null;
			adjustToPaneSize(paneWidth,paneHeight);
		}
		super.refreshViewPainters();
	}
	@Override
	protected void paneSizeSet(int width,int height){
		if(width<0||height<0)throw new IllegalArgumentException(
				"Negative dimensions in "+Debug.info(this));
		else adjustToPaneSize(width,height);
	}
	private PlaneView planeView(){
		return (PlaneView)viewer().view();
	}
	Point plot(Point point){
		if(origin==null)throw new IllegalStateException("Null origin in "+Debug.info(this));
		double x=scale(origin.x()+point.x()),y=scale(origin.y()+point.y()
				*planeView().ySign());
		return new Point(x,y);
	}
	Point unplot(double plotX,double plotY){
		if(origin==null)throw new IllegalStateException("Null origin in "+Debug.info(this));
		double x=unscale(plotX)-origin.x(),y=(unscale(plotY)-origin.y())
				*planeView().ySign();
		return new Point(x,y);
	}
	Vector checkSnap(Point check,double snapGap){
		Vector snap=null;
		for(Avatar a:avatarPickables)
			if(true)throw new RuntimeException("Not tested in "+this);
			else if((snap=a.checkSnap(this,check,snapGap))!=null)return snap;
		return null;
	}
	void panAreaSet(int left,int top){
		initialPortAtZeros&=left<=0&&top<=0;
		if(initialPortAtZeros)return;
		PlaneView plane=planeView();
		if(plane instanceof ZoomPanView)
			((ZoomPanView)plane).setFocusAt(
					unplot(left+paneWidth/2,top+paneHeight/2));
	}
	@Override
	protected double hitGap(){
		return unscale(super.hitGap());
	}
	@Override
	protected double markSize(){
		return unscale(super.markSize());
	}
	double markLeap(){
		return unscale(planeView().markLeapPixels());
	}
}