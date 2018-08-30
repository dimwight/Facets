package facets.facet.app;
import static facets.facet.FacetFactory.*;
import facets.core.app.ActionViewerTarget;
import facets.core.app.AppConstants;
import facets.core.app.AreaRoot;
import facets.core.app.NodeViewable;
import facets.core.app.PagedContentArea;
import facets.core.app.PagedContenter;
import facets.core.app.PagedSurface;
import facets.core.app.SAreaTarget;
import facets.core.app.SContentAreaTargeter;
import facets.core.app.SViewer;
import facets.core.app.TreeView;
import facets.core.app.ViewableFrame;
import facets.core.app.ViewerContenter;
import facets.core.app.ViewerTarget;
import facets.core.app.FeatureHost.LayoutFeatures;
import facets.core.superficial.SFacet;
import facets.core.superficial.SFrameTarget;
import facets.core.superficial.STarget;
import facets.core.superficial.app.SSelection;
import facets.facet.FacetFactory;
import facets.facet.ViewerAreaMaster;
import facets.util.Debug;
import facets.util.app.AppValues;
import facets.util.tree.DataNode;
import facets.util.tree.ValueNode;
import java.awt.Dimension;
final class AppValuesContenter extends ViewerContenter implements PagedContenter{
	private final String title;
	private final FacetFactory ff;
	private final ValueNode state;
	private final String key=FacetPreferences.VALUES_PATH;
  public AppValuesContenter(AppValues values,String title,FacetFactory ff,ValueNode state){
  	super(values);
		this.title=title;
		this.ff=ff;
		this.state=state;
  }
  protected AreaRoot newContentViewableArea(Object source,boolean faceted){
		AppValues values=(AppValues)source;
		DataNode content=new DataNode("Array",title,
				new Object[]{values.nature(),values.state()});
		NodeViewable viewable=new NodeViewable(content){
			@Override
			protected void viewerSelectionChanged(SViewer viewer,SSelection selection){
				super.viewerSelectionChanged(viewer,selection);
				putSelectionState(state,key);
			}
		};
		viewable.readSelectionState(state,key);
		STarget view=new SFrameTarget(
				new TreeView(title+" View"){				
					public boolean hideRoot(){
						return true;
					}
				}
			);
		ViewerTarget viewer=new ActionViewerTarget(viewable.title(),viewable,view){};
		AreaRoot area=new PagedContentArea(title,
				new SAreaTarget[]{SAreaTarget.newSingleViewerArea(viewer)},this);
		if(faceted)ff.areas().mount(area,true);
		return area;
	}
	@Override
  protected void traceOutput(String msg){
  	if(false)super.traceOutput(msg);
  }
  public SFacet newContentPanel(SContentAreaTargeter t){
		SAreaTarget root=(SAreaTarget)t.target();
		traceDebug(".newContentPanel: root=",root);
	  SAreaTarget rootChild=(SAreaTarget)root.indexedTarget();
	  traceDebug(".newContentPanel: rootChild=",rootChild);
		SFacet facet=ff.areas().viewerArea(rootChild,new ViewerAreaMaster(){
			protected String hintString(){
				return HINT_BARE;
			}
		});
		traceDebug(".newContentPanel: rootChild=",rootChild.attachedFacet());
		return facet;
	}
	public Dimension contentAreaSize(){
		return new Dimension(200,200);
	}
	public void areaRetargeted(SContentAreaTargeter area){
		PagedSurface.findDialogTrigger(area,AppConstants.TITLE_APPLY).setLive(false);
	}
	public void applyChanges(){}
	public void reverseChanges(){}
	public String newHostTitle(String surfaceTitle){
		return surfaceTitle+"\n - \n"+AppValues.PATH_DEBUG;
	}
	public String title(){
		return title;
	}
	public void hostHidden(){}
	public LayoutFeatures newContentFeatures(SContentAreaTargeter area){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	@Override
	public void setSurface(PagedSurface surface){}
}