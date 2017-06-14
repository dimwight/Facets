package demo.applet;

import facets.core.app.NodeViewable;
import facets.core.app.TreeView;
import facets.core.superficial.FacetedTarget;
import facets.core.superficial.SFrameTarget;
import facets.core.superficial.STarget;
import facets.core.superficial.app.SAreaTarget;
import facets.core.superficial.app.ViewableFrame;
import facets.core.superficial.app.ViewerTarget;
import facets.facet.AreaFacets;
import facets.facet.FacetFactory;
import facets.facet.ViewerAreaMaster;


/**
Exposes spike content in two viewers. 
<p>{@link ViewsContenter} extends its superclass by adding a tree viewer to
its inherited graphical viewer.
 */
public final class ViewsContenter extends SimpleLineContenter {
	
	/**
	Unique constructor. 
	@param facets passed to superclass
	@param panelToSide passed to superclass
	 */
	public ViewsContenter(FacetFactory facets, boolean panelToSide) {
		
		super(facets);
	}

	/**
	Implements abstract framework method. 
	<p>Defines tree view in addtion to inherited avatars view. 
	@see facets.core.app.ViewerContenter#newContentViews(NodeViewable)
	 */
	STarget[] newLineViews(SimpleLineViewable lines) {
	
		//Define view frames
		STarget 
		
			//From superclass
			avatars = super.newLineViews(lines)[0],
			
			//Wrapping tree view
			tree = new SFrameTarget(new TreeView("Tree"));
		
		//Return array containing both frames
		return new STarget[]{				
				avatars,
				tree,
			};
		}

	/**
	Re-implements framework method. 
	<p>Creates area with two views. 
	@see facets.core.app.ViewerContenter#newContentViewers(facets.core.superficial.app.ViewableFrame, facets.core.superficial.STarget[])
	 */
	@Override
	FacetedTarget[] newLineViewers(ViewableFrame viewable,
			STarget[] viewTargets) {		
		
		//Create frames using inherited method
		ViewerTarget 
			avatars = newIteratingViewerTarget(viewable, viewTargets[0]),
			tree = newIteratingViewerTarget(viewable, viewTargets[1]);
		
		//Return array that meets method contract
		return new SAreaTarget[] {
				SAreaTarget.newSingleViewerArea(avatars),
				SAreaTarget.newSingleViewerArea(tree)
			};
	}

	/**
	Implements abstract framework method. 
	<p>Creates sash mount container for two viewers. 
	@see facets.core.superficial.app.SContenter#attachAreaMountFacet(
	facets.core.superficial.app.SAreaTarget)
	 */
	void attachLineAreaFacets(SAreaTarget area) {
		
		//Get reference
		AreaFacets areas = ff.areas();
	
		//Create sash mount to hold two viewers
		areas.attachPanes(area, areas.viewerAreaChildren(area,new ViewerAreaMaster(){}),
				AreaFacets.PANE_SPLIT_VERTICAL);
	}	
}