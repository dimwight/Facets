package demo.applet;

import static applicable.textart.TextArtConstants.*;
import facets.core.app.ActionViewerTarget;
import facets.core.app.ArrayPath;
import facets.core.app.NodeViewable;
import facets.core.app.PathSelection;
import facets.core.app.avatar.AvatarContent;
import facets.core.app.avatar.AvatarView;
import facets.core.superficial.SFrameTarget;
import facets.core.superficial.SIndexing;
import facets.core.superficial.app.SSelection;
import facets.core.superficial.app.SView;
import facets.core.superficial.app.SViewer;
import facets.core.superficial.app.SelectionView;
import facets.core.superficial.app.ViewableAction;
import facets.core.superficial.app.ViewableFrame;
import facets.util.NumberPolicy;
import facets.util.OffsetPath;
import facets.util.geom.AnglePolicy;
import facets.util.tree.TypedNode;
import facets.util.tree.ValueNode;
import applicable.textart.TextArt;
import applicable.textart.TextArtAvatarPolicies;

/**
 Provides viewer content by framing a set of {@link TextArt}s. 
 <p>The lines are supplied as {@link ValueNode} content ultimately 
 created by {@link TextArt#LINES_SOURCE}. 
 <p>This class is a simplified version of {@link TextArtViewable} as used
in the spike application; in particular it only handles single selections. 
 */
final class SimpleLineViewable extends NodeViewable {
	
	//For adjustment to match edits
	private final SIndexing toolSwitchIndexing;
	
	//Package-visible for use by view
	final double 
		limitWidth, 
		limitHeight;

	/**
	 Unique constructor. 
	 @param lineSet passed to superclass, contains the lines array
	 @param toolSwitchIndexing to enable switching based on edit made
	 */
	SimpleLineViewable(ValueNode lineSet, SIndexing toolSwitchIndexing) {
		super(lineSet);
		
		//Set reference
		this.toolSwitchIndexing = toolSwitchIndexing;
		
		//Get stored settings
		limitWidth = lineSet.getDouble(KEY_LIMIT_WIDTH);
		limitHeight = lineSet.getDouble(KEY_LIMIT_HEIGHT);
	}

	/**
	Re-implements superclass method. 
	@see ViewableFrame#selectionFrame()
	 */
	@Override
	public SFrameTarget selectionFrame(){
		
		//Get current selection 
		SSelection selection = selection();
		Object selected = selection.multiple()[0];
		
		//Trap no line selected
		ValueNode node = selected == selection.content()?
				NO_SELECTION : (ValueNode) selected;
	
		//Get current and previous line states
		TypedNode 
			nodesNow[] = ((TypedNode)framed).children(),
			nodesThen[] = copyFramed().children();
		
		//Look for selected line
		for (int i = 0; i < nodesNow.length; i++) {
			
			//Skip if not selected
			if (!nodesNow[i].equals(node)) continue;			
			
			//Check for edit type
			TextArt thenLine = new TextArt((ValueNode) nodesThen[i]),
				nowLine = new TextArt(node);
				int toolsThen = toolSwitchIndexing.index(),
					toolsNow = thenLine.atX() != nowLine.atX() 
							|| thenLine.atY() != nowLine.atY() ? TOOLS_XY
						: thenLine.atAngle() != nowLine.atAngle() ? TOOLS_ANGLE
						: !thenLine.fontFace().equals(nowLine.fontFace()) 
							|| !thenLine.shade().equals(nowLine.shade())
							|| thenLine.fontIsBold() != nowLine.fontIsBold()
							|| thenLine.fontIsItalic() != nowLine.fontIsItalic()
							|| thenLine.fontSize() != nowLine.fontSize() ? TOOLS_FONT
						: toolsThen;
	
			//Adjust tools display as required
			if (toolsThen != toolsNow)
				toolSwitchIndexing.setIndex(toolsNow);		
			
			//Don't check any more lines
			break;
		}
		
		//Record new content state
		copyFramedState();
		
		//Returm new frame 
		return new SimpleLineSelection(node, this);
	}

	protected SSelection newViewerSelection(SViewer viewer){
		SSelection selection=selection();
		SView view=viewer.view();
		return ((AvatarView)view).avatars().newAvatarSelection(viewer,selection);
	}
	
	protected void viewerSelectionChanged(SViewer viewer, SSelection selection) {
		
		Object content=selection.content();
		
		if(content instanceof AvatarContent[]){
			Object[]nodes=TextArt.getProxySourceNodes(selection.multiple());
			OffsetPath[]paths=new OffsetPath[nodes.length];
			for(int i=0;i<paths.length;i++)paths[i]=new ArrayPath(nodes,nodes[i]);
			selection=new PathSelection(nodes,paths);
		}
		
		super.viewerSelectionChanged(viewer,selection);
	}
	
	protected void viewerSelectionEdited(SViewer viewer,Object edit,
			boolean interim){
		
		super.viewerSelectionEdited(viewer,
				TextArt.getProxySourceNodes((Object[])edit),interim);
	}

	/**
	 Number policies for X and Y values. 
	 <p>Called by {@link SimpleLineSelection} and {@link TextArtAvatarPolicies}. 
	 @param forX determines which policy is returned
	 */
	final NumberPolicy xyPolicy(final boolean forX) {
		
		//Create and return policy constraining movement to view limits
		return new NumberPolicy.Ticked(0, forX ? limitWidth : limitHeight) {

			@Override
			public int columns() {
				return 3;
			}

			@Override
			public String[] incrementTitles() {
				return forX ? new String[]{
							TITLE_LINE_LEFT, 
							TITLE_LINE_RIGHT
						}
						: new String[]{
							TITLE_LINE_UP, 
							TITLE_LINE_DOWN
						};
			}
			
			@Override
			public int snapType() {
				return SNAP_NONE;
			}
		};
	}

	/**
	 Number policy for line angle. 
	 <p>Called by {@link SimpleLineSelection} and {@link TextArtAvatarPolicies}. 
	 */
	final NumberPolicy anglePolicy() {
		return new AnglePolicy() {
	
			@Override
			final public String[] incrementTitles() {
				return new String[]{TITLE_LINE_CCW, TITLE_LINE_CW};
			}
			
			@Override
			public int snapType(){return SNAP_NONE;}
		};
	}

	@Override
	public ViewableAction[]viewerActions(SView view){	
		return new ViewableAction[]{
				ActionViewerTarget.Action.ITERATE_BACK,
				ActionViewerTarget.Action.ITERATE_FORWARD,
			};
	}
}