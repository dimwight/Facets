package applicable.avatar;
import facets.core.app.avatar.AvatarContent;
import facets.core.app.avatar.AvatarContent.Applicable;
import facets.core.app.avatar.AvatarPolicies;
import facets.core.app.avatar.AvatarPolicy;
import facets.core.app.avatar.AvatarView;
import facets.core.app.avatar.DragPolicy;
import facets.core.app.avatar.Painter;
import facets.core.app.avatar.PainterSource;
import facets.core.app.avatar.PlaneView;
import facets.core.app.avatar.PlaneViewWorks;
import facets.core.superficial.app.SSelection;
import facets.core.superficial.app.SViewer;
import facets.core.superficial.app.SelectionView;
import facets.util.geom.Point;
import facets.util.geom.Vector;
import facets.util.tree.TypedNode;
public class PlaneShapingView extends SelectionView implements PlaneView{
	private final PlaneView plane;
	private final double showWidth,showHeight;
	public PlaneShapingView(PlaneView plane){
		super(plane.title());
		this.plane=plane;
		showWidth=plane.showWidth();
		showHeight=plane.showHeight();
	}
	public AvatarPolicies avatars(){
		final AvatarPolicies plane=this.plane.avatars();
		return new AvatarPolicies(){
			@Override
			public SSelection newAvatarSelection(SViewer viewer,SSelection viewable){
				return plane.newAvatarSelection(viewer,viewable);
			}
			@Override
			public Painter getBackgroundPainter(SViewer viewer,PainterSource p){
				return plane.getBackgroundPainter(viewer,p);
			}
			@Override
			public AvatarPolicy avatarPolicy(SViewer viewer,AvatarContent content,
					PainterSource p){
				return plane.avatarPolicy(viewer,content,p);
			}
			@Override
			public boolean isContentSelectable(AvatarContent content){
				return false&&plane.isContentSelectable(content);
			}
			@Override
			public DragPolicy dragPolicy(AvatarView view,AvatarContent[] content,
					Object hit,PainterSource p){
				if(true)throw new RuntimeException("Not implemented in "+this);
				else return plane.dragPolicy(view,content,hit,p);
			}
		};
	}
	@Override
	public Vector plotShift(){
		return plane.plotShift();
	}
	@Override
	public void setShowValues(double width,double height,Vector plotShift,
			double scale){
		throw new RuntimeException("Not implemented in "+this);
	}
	@Override
	public boolean scaleToViewer(){
		return true;
	}
	@Override
	public double showWidth(){
		return showWidth;
	}
	@Override
	public double showHeight(){
		return showHeight;
	}
	@Override
	public int ySign(){
		return plane.ySign();
	}
	@Override
	public double scale(){
		return plane.scale();
	}
	@Override
	public Object backgroundStyle(){
		return plane.backgroundStyle();
	}
	@Override
	public boolean isLive(){
		return plane.isLive();
	}
	@Override
	public boolean allowMultipleSelection(){
		return plane.allowMultipleSelection();
	}
	@Override
	public Object stamp(){
		return plane.stamp();
	}
	@Override
	public int pickHitPixels(){
		return plane.pickHitPixels();
	}
	@Override
	public int markPixels(){
		return plane.markPixels();
	}
	@Override
	public int markLeapPixels(){
		return plane.markLeapPixels();
	}
	@Override
	public boolean doesDnD(){
		return plane.doesDnD();
	}
}