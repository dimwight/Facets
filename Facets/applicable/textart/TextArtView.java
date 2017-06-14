package applicable.textart;

import static applicable.textart.TextArtConstants.*;
import facets.core.app.NodeViewable;
import facets.core.app.avatar.AvatarPolicies;
import facets.core.app.avatar.PlaneView;
import facets.core.app.avatar.PlaneViewWorks;
import facets.core.superficial.SFrameTarget;
import facets.core.superficial.SIndexing;
import facets.core.superficial.STarget;
import facets.core.superficial.SToggling;
import facets.core.superficial.SToggling.Coupler;
import facets.core.superficial.app.SView;
import facets.core.superficial.app.ViewableAction;
import facets.util.Debug;
import facets.util.Util;
import facets.util.geom.Vector;

/**
Plane avatars view for {@link TextArt} content. 
 */
public class TextArtView extends PlaneViewWorks{

	/**
	 Subclass to implement zooming. 
	 <p>{@link ChildView} returns several properties shared with and defined by
	 its enclosing {@link TextArtView}. 
	 */
	private final class ChildView extends TextArtView {

		ChildView(String title, double showWidth, double showHeight,
				TextArtView base) {
			super(title, showWidth, showHeight, base.plotShift(), base.avatars, 
					base.gridShow);
		}

		@Override
		public boolean allowMultipleSelection() {
			return TextArtView.this.allowMultipleSelection();
		}

		@Override
		public boolean isLive() {
			return TextArtView.this.isLive();
		}
	}
	
	//Set grid display flag and share it with avatar policies
	final SToggling gridShow;	
	boolean showGrid;

	//For zooming
	private static final double scaleMiddle = 1.66,
		scaleFar = scaleMiddle * scaleMiddle;

	/**
	Constructor for use by {@link TextArtContenter}. 
	 */
	public TextArtView(String title, double showWidth, double showHeight, 
			AvatarPolicies policies) {
		
		//Call core constructor with appropriate parameters
		this(title, showWidth, showHeight, new Vector(0, 0), policies, null);
	}

	/**
	 Core constructor for base and child views. 
	 @param title passed to superclass
	 @param showWidth passed to superclass
	 @param showHeight passed to superclass
	 @param policies passed to superclass
	 @param lines for grid reference
	 @param gridShow for sharing with child views
	 */
	TextArtView(String title, double showWidth, double showHeight,
			Vector plotShift, AvatarPolicies policies, SToggling gridShow) {
		
		//Pass some parameters to superclass 
		super(title, showWidth, showHeight, plotShift, policies);
		
		//Share or create coupled toggling
		Coupler showCoupler = new SToggling.Coupler() {
					
			public void stateSet(SToggling t) {
				showGrid = t.isSet();
			}
		};
		this.gridShow = gridShow !=null ? gridShow :
			new SToggling("Show Grid|Show", false, showCoupler);
	}

	/**
	Re-implements interface method. 
	@see SView#allowMultipleSelection()
	 */
	@Override
	public boolean allowMultipleSelection() {
		return true;
	}

	/**
	Re-implements interface method. 
	@see SView#isLive()
	 */
	@Override
	public boolean isLive() {
		return true;
	}

	/**
	Re-implements interface method. 
	@see facets.core.app.avatar.AvatarViewWorks#doesDnD()
	 */
	@Override
	public boolean doesDnD() {
		return true;
	}

	/**
	 Creates indexing containing base and child views. 
	 <p>Called by {@link TextArtContenter#newContentViews(NodeViewable)}
	 */
	final static SIndexing newZoomIndexing(double nearWidth, 
			double nearHeight, AvatarPolicies policies) {
		
		//Create base and child views, array of frames
		TextArtView
			near = new TextArtView("Near", nearWidth, nearHeight, policies),
			middle = near.new ChildView("Middle", nearWidth * scaleMiddle,
					nearHeight * scaleMiddle, near), 
			far = near.new ChildView("Fa&r", nearWidth * scaleFar,
					nearHeight * scaleFar, near); 
		STarget[]frames = {
				near.newFrame(),
				middle.newFrame(),
				far.newFrame()
			};
		
		//Return indexing with coupler specifying iteration titles
		return new SIndexing("View|Zoom", frames, 1, new SIndexing.Coupler() {

			public String[] iterationTitles(SIndexing i) {
				return new String[]{
					"In", "O&ut"
				};
			}
		});
	}

	/**
	Called by {@link #newZoomIndexing(double, double, AvatarPolicies)} 
	to create frames. 
	*/
	public SFrameTarget newFrame() {
		return new SFrameTarget(this) {	

			protected STarget[] lazyElements() {
				
				//Shared toogling to keep grid state in all zoom states
				return new STarget[]{
					gridShow, 
				};
			}
		};
	}
}