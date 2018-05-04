package apps.codeview;
 
import static apps.codeview.CodeViewConstants.*;
import static facets.core.app.SAreaTarget.*;
import static facets.facet.AreaFacets.*;
import static facets.facet.FacetFactory.*;
import facets.core.app.ActionViewerTarget;
import facets.core.app.AppContenter;
import facets.core.app.AreaRoot;
import facets.core.app.NodeViewable;
import facets.core.app.SAreaTarget;
import facets.core.app.SViewer;
import facets.core.app.TreeView;
import facets.core.app.ViewableAction;
import facets.core.app.ViewableFrame;
import facets.core.app.ViewerContenter;
import facets.core.app.ViewerTarget;
import facets.core.superficial.SFacet;
import facets.core.superficial.SFrameTarget;
import facets.core.superficial.STarget;
import facets.core.superficial.STrigger;
import facets.core.superficial.TargetCore;
import facets.core.superficial.app.FacetedTarget;
import facets.core.superficial.app.SSelection;
import facets.core.superficial.app.SelectingFrame;
import facets.facet.AreaFacets;
import facets.facet.FacetFactory;
import facets.facet.ViewerAreaMaster;
import facets.util.Debug;
import facets.util.Util;
import facets.util.tree.DataConstants;
import facets.util.tree.DataNode;
import facets.util.tree.ExceptionNode;
import facets.util.tree.TypedNode;


import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URL;
import applicable.LiveExternalWindow;


/**
Java code viewer. 
<p>{@link CodeViewContenter} and its helper classes provide an example of how
a useful application can be coded by configuring framework types. 
*/
public abstract class CodeViewContenter extends ViewerContenter {	

	/**
	{@link ActionViewerTarget} that stores HTML for browser display 
	and can trigger write. 
	 */
	public final class SourceViewer extends ActionViewerTarget {

		//Trivial constructor
		SourceViewer(STarget view, ViewableFrame viewable) {
			
			//Pass real and dummy parameters to superclass
			super(view.title(), viewable, view);
		}
		
		//Returns trigger for writing
		@Override
		protected STarget[] lazyElements() {
			
			//Single member array, trigger with coupler
			return new STarget[]{
					new STrigger("Write to external window", new STrigger.Coupler() {					
					@Override
					public void fired(STrigger t) {
						writeExternal(viewable.selection(), true);
					}
				})};
		}

		//For call by trigger or method
		void writeExternal(SSelection selection, boolean forceOpen) {
			
			String html = ((CodeSourceView) view()).getSelectionHtml(selection, true);
			
			//Make window call to browser
			window.writeExternal(html, forceOpen);		
		}
		
		//Get HTML for a class from a headless instance
		public String getExternalHtml(String codeClass) {
			
			//Set selection
			viewable.defineSelection(codeClass);
			
			//Get and return HTML
			return ((CodeSourceView) view()).getSelectionHtml(
					viewable.selection(), true);
		}

		//Write HTML for current selection to external window
		public void writeExternalHTML() {
			writeExternal(viewable.selection(), false);			
		}
	}

	//Debug flag 
	public final static boolean debugEditingSource = false;

	public static final String CODE_ROOT="demo";
	
	//facet builder core
	protected final FacetFactory ff;
	
	//Viewable framing content tree
	private NodeViewable viewable;
	
	//Code to display on opening
	private final String openingClass;

	//May be passed to constructor (applet surface only)
	private final CodeSourceView sharedView;

	//External browser viewer window (applet surface only)
	private final LiveExternalWindow window;
	
	/**
	Unique constructor. 
	@param source as created by {@link #newTreeSource()}
	@param sharedView if non-<code>null</code>, allows the view to be 
	shared between instances (possibly in different browser windows
	@param window applet browser proxy
	@param ff core facet builder
	@param openingClass to display on opening
	 */
	public CodeViewContenter(ContentSource source, LiveExternalWindow window, 
			CodeSourceView sharedView, FacetFactory ff, String openingClass) {
		
		//Pass to superclass
		super(source);
		
		//Set references
		this.window = window;
		this.sharedView = sharedView;
		this.ff = ff;
		this.openingClass=true?CodeViewContenter.class.getSimpleName():openingClass;
		
		//Complain if no opening code
		if(this.openingClass == null || this.openingClass.trim().equals(""))
			throw new IllegalArgumentException("Null or blank openingClass");
	}
	
	/**
	Reimplements superclass method to return a suitable content root. 
	@see facets.core.app.ViewerContenter#newContentViewableArea(java.lang.Object, boolean)
	 */
	protected AreaRoot newContentViewableArea(Object source, boolean faceted){
		
		//Only construct single content and viewable instances
		if(viewable==null) {
			
			//Get root of content tree, construct viewable
			final TypedNode root = (TypedNode)((ContentSource)source).newContent();
			
			viewable = new NodeViewable(root);
		}
		
		//Define code tree view unless flag set
		TreeView treeView = sourcePaneOnly() ? null : new TreeView("Classes") {
			
			@Override
			public Object[]nodeContents(SViewer viewer,TypedNode node){
				
				//Hide exception contents
				return node instanceof ExceptionNode ? new Object[]{}:
					super.nodeContents(viewer,node);
			}
			
			@Override
			protected boolean filterNodeContents(){
				return true;
			}
			
			@Override
			protected boolean includeNode(TypedNode parent,TypedNode node){
				
				//Only include top levels
				String type = node.type();
		    return type.equals("Folder") || type.equals(DataConstants.TYPE_JAVA);
		  }
			
			@Override
			public String nodeRenderText(TypedNode node){
				return node.title();
			};
			
			@Override
			public String contentIconKey(Object content) {
				
				//Suppress icon for 'leaf' nodes
				String superKey = super.contentIconKey(content);
				return superKey.equals(DataConstants.TYPE_JAVA) ? 
						DataConstants.NO_ICON : superKey;
			}
		};
			
		//Source view frame, any tree and debug frames
		STarget 
		sourceFrame = sharedView != null? sharedView.newFrame()
				: new CodeSourceView("Source", null).newFrame(),
		treeFrame = treeView == null? null : new SFrameTarget(treeView),
		debugFrame = true? null : new CodeSourceView("Debug", null) {			
			public boolean showSource() {
				return true;
			}
		}.newFrame();		
		
		//Create viewers (and usually areas) from the right views
		ViewerTarget sourceViewer = new SourceViewer(sourceFrame, viewable);
		final boolean heliosWorkaround=true;
		SAreaTarget
		sourceArea = treeFrame == null ? null :
			newSingleViewerArea(sourceViewer),
		treeArea = treeFrame == null ? null : 
				newSingleViewerArea(new ActionViewerTarget(treeFrame.title(), viewable, treeFrame){}),
		debugArea = heliosWorkaround || debugFrame == null ? null :
				newSingleViewerArea(new ActionViewerTarget(debugFrame.title(), viewable, debugFrame){});
		FacetedTarget[] viewers = treeArea == null ? 
				new ViewerTarget[]{sourceViewer}
				: debugFrame == null ? new SAreaTarget[]{treeArea, sourceArea}
				: new SAreaTarget[]{treeArea, sourceArea, debugArea};
	
		//Create root, set opening selection
		AreaRoot area = newViewersArea("Code",viewers);
		((SelectingFrame)area.contenterFrame()).defineSelection(openingClass);
	
		//Reference to sub-factory
		AreaFacets areas = ff.areas();
		
		//Single view?
		if(viewers.length == 1) {
			
			//Set single viewer facet and return faceted root
			areas.viewerArea(area, new ViewerAreaMaster() {
				protected String hintString(){return HINT_BARE;}
			});			
			return area;
		}
	
		//Otherwise, define suitable sash layout
		boolean threeSashes = viewers.length == 3;
		int[] codes = threeSashes ? 
			new int[]{PANE_SPLIT_VERTICAL, PANE_RIGHT, PANE_SPLIT_HORIZONTAL} 
				: new int[]{PANE_SPLIT_VERTICAL};
		double[] splits = threeSashes ? new double[]{0.5, 0.25} : new double[]{0.25};
		
		//Create viewer facets from root and assemble in sashes, return faceted root
		areas.attachPanes(area, areas.viewerAreaChildren(area,new ViewerAreaMaster(){}), 
				codes, splits);
		return area;
	}
	
	/**
	Re-implementation of framework method. 
	@see facets.core.app.ViewerContenter#lazyContentAreaElements(facets.core.app.SAreaTarget)
	 */
	public final STarget[] lazyContentAreaElements(SAreaTarget area) {
		
		//Get or create target references
		STarget
		
			//How many viewer areas?
			areas[] = area.indexableTargets(),
			
			//Find source viewer area, source frame via area tree			
			sourceArea = areas.length == 1 ? area : areas[PANE_SOURCE], 
			sourceViewFrame = ((ViewerTarget)((SAreaTarget) sourceArea
					).activeFaceted()).viewFrame(),
			
			//To target groupings created by view frame
			elements[] = sourceViewFrame.elements(), 
			text = elements[VIEW_TEXT], 
			javadoc = elements[VIEW_CODE], 
	
			//Get targets for external viewer menu?
			external = window == null ? null
				: new TargetCore("External togglings", new STarget[]{
						window.externalToggling, 
						window.shrinkExternal,
				});
		
		//Return suitable elements
		return external != null ? new STarget[]{external, text, javadoc}
			: new STarget[]{text, javadoc};
	}
	
	/**
	Overrides invalid superclass implementation. 
	<p>Only required for spike app. 
	@see facets.core.app.AppContenter#alignContentAreas(facets.core.app.SAreaTarget, facets.core.app.SAreaTarget)
	 */
	public void alignContentAreas(final SAreaTarget existing,final SAreaTarget added){
		
		//Local class with utility method
		new Runnable() {

			//Gets reference
			CodeSourceView findSourceView(SAreaTarget root){
				
				//Find viewer as second pane in content area
				ViewerTarget viewer = (ViewerTarget) 
						((SAreaTarget)root.indexableTargets()[1]).indexedTarget();
				
				//Find and return view
				return (CodeSourceView) ((SFrameTarget) viewer.views).framed;
			}

			public void run(){
				
				//Align the new view to the old
				findSourceView(added).alignToView(findSourceView(existing));
			}
			
		}.run();
	}

	/**
	Should the surface just have a source pane?
	<p>Default returns <code>false</code>. 
	 */
	protected boolean sourcePaneOnly() {
		return false;
	}

	/**
	Returns a {@link facets.core.app.ViewerContenter.ContentSource} that attempts to load a serialized code tree. 
	@see facets.core.app.ViewerContenter.ContentSource
	 */
	final public static ViewerContenter.ContentSource newTreeSource() {
		
		//Define, create and return the source
		return new ViewerContenter.ContentSource() {
	
			//Content is (normally) unvarying, so can be based on unique tree
			TypedNode tree;
			
			//Count for tree titles
			private int trees;
	
			//Implement interface method
			public Object newContent() {
				
				//Try to load source tree on first invocation or when editing
				if (tree == null || debugEditingSource)
					try {
						
						//Load as resource or complain
						URL url = getClass().getClassLoader().getResource(CODE_ROOT+".tree");
						if(url==null)throw new IllegalStateException("Null url in "+Debug.info(this));						
						
						InputStream stream = url.openStream();
						
						//Stored as tree
						tree = (TypedNode) new ObjectInputStream(stream).readObject();
						
					} catch (Exception e) {
						
						//Return wrapped exception
						return new ExceptionNode(e);
					}
					
				//Return wrapping instance with numbered title
				return new DataNode(tree.type(), 
						"Code - " + tree.title() + " #" + (++trees),
						tree.children());
			}
		};
	}
}