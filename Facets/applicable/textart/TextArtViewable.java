package applicable.textart;

import static applicable.textart.TextArtConstants.*;
import facets.core.app.ActionViewerTarget;
import facets.core.app.ArrayPath;
import facets.core.app.FrameGroup;
import facets.core.app.NodeViewable;
import facets.core.app.PathSelection;
import facets.core.app.TreeView;
import facets.core.app.avatar.AvatarContent;
import facets.core.app.avatar.AvatarView;
import facets.core.superficial.SFrameTarget;
import facets.core.superficial.SIndexing;
import facets.core.superficial.STarget;
import facets.core.superficial.SToggling;
import facets.core.superficial.TargetCore;
import facets.core.superficial.SIndexing.Coupler;
import facets.core.superficial.app.SAreaTarget;
import facets.core.superficial.app.SSelection;
import facets.core.superficial.app.SView;
import facets.core.superficial.app.SViewer;
import facets.core.superficial.app.SelectionView;
import facets.core.superficial.app.ViewableAction;
import facets.core.superficial.app.ViewableFrame;
import facets.util.NumberPolicy;
import facets.util.Objects;
import facets.util.OffsetPath;
import facets.util.Stateful;
import facets.util.Util;
import facets.util.geom.AnglePolicy;
import facets.util.tree.Nodes;
import facets.util.tree.TypedNode;
import facets.util.tree.ValueNode;


/**
 Provides viewer content by framing a set of {@link TextArt}s. 
 <p>The lines are supplied as {@link ValueNode} content ultimately 
 created by {@link TextArt#LINES_SOURCE}. 
 */
final class TextArtViewable extends NodeViewable {

	//Couplers for snap and limit targets
	private final Coupler snapCoupler = new SIndexing.Coupler() {
		
		@Override
		public void indexSet(SIndexing s) {

			//Cast to content type
			ValueNode lineSet = (ValueNode)framed;
			
			//Store current values
			lineSet.put(KEY_GRID_SNAP, GRID_SNAPS[gridSnap.index()]);
			lineSet.put(KEY_ANGLE_SNAP, ANGLE_SNAPS[angleSnap.index()]);
			
			//Force lines update 
			updateLineValues(lineSet);
		}
	};
	final SToggling.Coupler limitsCoupler = new SToggling.Coupler() {
	
		@Override
		public void stateSet(SToggling t) {

			//Cast to content type
			ValueNode lineSet = (ValueNode)framed;
			
			//Store current value
			lineSet.put(KEY_LIMITS, t.isSet());		
			
			//Force lines update 
			updateLineValues(lineSet);
			
		}
	};
	
	//Settings targets, package-visible
	final SIndexing 
		gridSnap,	
		angleSnap;	
	final SToggling limits;

	//For XY policy and view background
	final double 
		limitWidth,
		limitHeight;
	
	//Creating contenter
	private final TextArtContenter contenter;
	private Object selection; 
	private SFrameTarget selectionGroup;

	/**
	 Unique constructor. 
	 @param lineSet passed to superclass, 
	 contains the lines array and settings values
	 @param clipperSource passed to superclass, 
	 @param contenter set as final field
	 */
	TextArtViewable(ValueNode lineSet, TextArtContenter contenter, 
			ClipperSource clipperSource) {
		
		//Pass parameters to superclass, set reference
		super(lineSet, clipperSource);
		this.contenter = contenter;
		
		//Get settings
		limitWidth = lineSet.getDouble(KEY_LIMIT_WIDTH);
		limitHeight = lineSet.getDouble(KEY_LIMIT_HEIGHT);
		String 
			gridSnapValue = lineSet.getString(KEY_GRID_SNAP), 
			angleSnapValue = lineSet.getString(KEY_ANGLE_SNAP);
		boolean limitsValue = lineSet.getBoolean(KEY_LIMITS);
		
		//Convert snap values into indexes
		int gridSnapAt = 0, angleSnapAt = 0;
		for (int i = 0; i < GRID_SNAPS.length; i++)
			if (GRID_SNAPS[i].equals(gridSnapValue)) gridSnapAt = i;
		for (int i = 0; i < ANGLE_SNAPS.length; i++)
			if (ANGLE_SNAPS[i].equals(angleSnapValue)) angleSnapAt = i;
		
		//Construct targets
		gridSnap = new SIndexing("Grid", GRID_SNAPS, gridSnapAt, snapCoupler);
		angleSnap = new SIndexing("Angle Snap|Snap", ANGLE_SNAPS, angleSnapAt,
				snapCoupler);
		limits = new SToggling(TITLE_LIMITS, limitsValue, limitsCoupler);
		
		boolean avoidingUndoOffsetsBug=false;
		int selectAt=Integer.valueOf(title().replaceAll("\\D*",""))%3;
		if(!avoidingUndoOffsetsBug)defineSelection(lineSet.children()[selectAt]);
	}
	
	/**
	Re-implementation. 
	@see SFrameTarget#lazyElements()
	 */
	@Override
	protected STarget[] lazyElements(){
		
		//Return members 
		return new STarget[]{
			gridSnap, 
			angleSnap,
			limits, 
			SAreaTarget.newSingleViewerArea(newTreeViewer())
		};
	}

	ActionViewerTarget newTreeViewer(){
		TreeView treeView=new TreeView(TAB_TITLE_TREE){
			protected boolean includeValue(TypedNode parent,Object value){
				return true;
			}
		};
		return new ActionViewerTarget(treeView.title(),this,new SFrameTarget(treeView)){};
	}

	/**
	Re-implements superclass method. 
	 <p>The target returned is actually created by 
	 {@link #newSelectionNodeFrame(ValueNode)} via 
	 {@link #newEditCheckedSelectionFrame(ValueNode)}. 
	@see ViewableFrame#selectionFrame()
	 */
	@Override
	public SFrameTarget selectionFrame(){
		
		//Get current selection, if possible return existing frame
		SSelection selectionNow = selection();
		if(false&&selection == selectionNow) return selectionGroup;
		
		//Update test reference, get new selections
		selection = selectionNow;
		Object[] selections=selectionNow.multiple();
		if(false)Util.printOut("TextLineViewable.selectionFrame: ",selections);
		SFrameTarget[] frames;
		
		//Not a line selection?
		if (selections[0] == selectionNow.content()){
	
			//Create dummy 
			frames = new SFrameTarget[]{newEditCheckedSelectionFrame(NO_SELECTION)};
		}
		 
		else {
		
			//Create possibly multiple targets 
			frames=new SFrameTarget[selections.length];
			for (int i = 0; i < frames.length; i++)
				frames[i] = newEditCheckedSelectionFrame((ValueNode) selections[i]);
		}
		
		//Create and return group
		return selectionGroup = new FrameGroup(this, frames);
	}

	/**
	 Creates a new selection frame, checking for edit, updating superclass states and 
	 adjusting tools display as required. 
	 @param node is passed to {@link #newSelectionNodeFrame(TextArt)}
	 */
	private SFrameTarget newEditCheckedSelectionFrame(ValueNode node) {
	
		//Get content lines and check content line
		TypedNode 
			nodesNow[] = ((TypedNode)framed).children(),
			nodesThen[] = copyFramed().children();
		
		for (int i = 0; i < nodesNow.length; i++)
			
			if (nodesNow[i].equals(node)) {
			
			//Check for edit
			if(contenter.advanceFacets!=null)
				contenter.advanceFacets.adjustToolIndexing((ValueNode)nodesThen[i],node);		
			
			//Don't check any more lines
			break;
		}
		
		//Record new content state
		copyFramedState();
		
		//Returm new frame using copy of line
		return newSelectionNodeFrame((ValueNode)node.copyState());
	}

	/**
	 Returns a frame to represent a selected line. 
	 */	
	private SFrameTarget newSelectionNodeFrame(ValueNode node) {
		
		return new TextArtSelection(node, this, contenter.dialogs);
	}
	
	protected SSelection newViewerSelection(SViewer viewer){
		SSelection selection=selection();
		SView view=viewer.view();
		return view instanceof AvatarView?
			((AvatarView)view).avatars().newAvatarSelection(viewer,selection)
				:((SelectionView)view).newViewerSelection(viewer,selection);
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
	Re-implements superclass method. 
	@see facets.core.app.StatefulViewable#checkStateChange(Stateful, Stateful)
	 */
	@Override
	protected boolean checkStateChange(Stateful before,Stateful after){
		
		//Wrap states
		TextArt 
			lineBefore = new TextArt((ValueNode) before),
			lineAfter = new TextArt((ValueNode) after);
		
		//Define change
		String changeKey = lineBefore.valueChangeKey(lineAfter);
		
		//Strip values ready for merge testing
		lineBefore.stripToMergeableValue(changeKey);
		lineAfter.stripToMergeableValue(changeKey);
		
		//Return flag
		return changeKey!=TextArt.KEY_UNCHANGED;
	}
	
	protected void restoreState(Stateful content,Stateful state){
		Nodes.mergeContents((ValueNode)content,((TypedNode)state).contents());
	}

	/**
	Re-implementation. 
	@see facets.core.app.StatefulViewable#canMergeEdits(Stateful[], Stateful[])
	 */
	@Override
	protected boolean canMergeEdits(Stateful[]then,Stateful[]now){
		
		//May be obviously impossible
		if(then.length != now.length) return false;
		
		//Check member pairs, return final value
		boolean canMerge = true;
		for(int i=0;i<then.length;i++)
			canMerge &= new TextArt((ValueNode) then[i])
				.changeValuesMergeable(new TextArt((ValueNode) now[i]));
		return canMerge;
	}
	/**
	 Number policies for X and Y values. 
	 <p>Called by {@link TextArtSelection} and {@link TextArtAvatarPolicies}. 
	 @param forX determines which policy is returned
	 */
	final NumberPolicy xyPolicy(final boolean forX) {
		
		//Create and return policy that reads limits flag
		return new NumberPolicy.Ticked() {
			
			@Override
			public double min() {
				return !limits.isSet() ? NumberPolicy.MIN_VALUE : 0;
			}
			
			@Override
			public double max() {
				return !limits.isSet() ? NumberPolicy.MAX_VALUE
						: forX ? limitWidth : limitHeight;
			}
			
			@Override
			public double range() {
				return limits.isSet() ? max() - min()
						: forX ? limitWidth : limitHeight;
			}
	
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
				return gridSnap.index() == 0? SNAP_NONE : SNAP_TICKS;
			}
	
			@Override
			public int tickSpacing() {
				return gridSnap.index() == TextArtConstants.GRID_NONE ? 1
						: Integer.valueOf(((String) gridSnap.indexed())).intValue();
			}
			
			@Override
			public int labelSpacing(){
				int tick = tickSpacing();
				return tick < 20 ? LABEL_TICKS_DEFAULT : tick < 50 ? 5 : 2;
			}
		};
	}

	/**
	 Number policy for setting line angle. 
	 <p>Called by {@link TextArtSelection} and {@link TextArtAvatarPolicies}. 
	 */
	final NumberPolicy anglePolicy() {
		return new AnglePolicy() {
	
			public int snapType() {
				return angleSnap.index();
			}

			@Override
			final public String[] incrementTitles() {
				return new String[]{TITLE_LINE_CCW, TITLE_LINE_CW};
			}
		};
	}

	/**
	Forces all line properties to conform to current viewer settings. 
	@param lineSet contains lines
	 */
	private void updateLineValues(ValueNode lineSet) {
		
		//Get lines
		ValueNode[] lineNodes = Objects.newTyped(ValueNode.class, lineSet.children());
		
		//Use side-effects of constructing frame to update values
		for (int i = 0; i < lineNodes.length; i++)
			newSelectionNodeFrame(lineNodes[i]).elements();
	}
	@Override
	public ViewableAction[]viewerActions(SView view){
		return ACTIONS_ALL;
	}
}