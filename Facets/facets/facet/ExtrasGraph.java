package facets.facet;
import static facets.facet.FacetFactory.*;
import static facets.util.app.AppValues.*;
import facets.core.app.ActionViewerTarget;
import facets.core.app.AppContenter;
import facets.core.app.AppSpecifier;
import facets.core.app.AreaRoot;
import facets.core.app.FacetHostable;
import facets.core.app.MenuFacets;
import facets.core.app.NodeViewable;
import facets.core.app.PagedContenter;
import facets.core.app.PathSelection;
import facets.core.app.SurfaceServices;
import facets.core.app.TreeView;
import facets.core.app.ViewerContenter;
import facets.core.app.FacetHostable.Hosting;
import facets.core.app.FeatureHost.LayoutFeatures;
import facets.core.superficial.FacetedTarget;
import facets.core.superficial.Notifiable;
import facets.core.superficial.SFacet;
import facets.core.superficial.SFrameTarget;
import facets.core.superficial.STarget;
import facets.core.superficial.STargeter;
import facets.core.superficial.TargeterCore;
import facets.core.superficial.Notifying.Impact;
import facets.core.superficial.app.AreaTargeter;
import facets.core.superficial.app.SContentAreaTargeter;
import facets.core.superficial.app.SSelection;
import facets.core.superficial.app.SViewer;
import facets.core.superficial.app.SelectingFrame;
import facets.core.superficial.app.ViewableFrame;
import facets.core.superficial.app.ViewerTarget;
import facets.facet.FacetFactory.EditFacets;
import facets.facet.HostingFacetSwing.Viewer;
import facets.facet.kit.Toolkit;
import facets.util.Debug;
import facets.util.OffsetPath;
import facets.util.Times;
import facets.util.Util;
import facets.util.tree.DataNode;
import facets.util.tree.NodePath;
import facets.util.tree.Nodes;
import facets.util.tree.TypedNode;
import facets.util.tree.ValueNode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
final class ExtrasGraph extends ViewerContenter{
	private final List<OffsetPath>findPaths=new ArrayList();
	private final ValueNode state;
	private final Toolkit kit;
	private final FacetFactory ff;
	private final String keyOffsets;
	private Object findThen;
	static SFacet newFaceted(final AreaTargeter targeter,final ValueNode stateApp,
			final Toolkit kit,final FacetFactory ff){
		return new Viewer(new FacetHostable(){
			private Object framed;
			@Override
			public void facetRetargeted(Hosting host,STarget target,Impact impact){
				if(impact==Impact.MINI)return;
				SContentAreaTargeter active=(SContentAreaTargeter)targeter.areaAt(AreaTargeter.AREA_ACTIVE);
				target=active.content().target();
				Object framed=((SFrameTarget)
						(target instanceof SelectingFrame?active.selection().target():target)).framed;
				if(false&&this.framed==framed)return;
				else this.framed=framed;
				host.refreshViewer(targeter);
			}
			@Override
			public AppContenter newViewerContenter(Object source){
				return new ExtrasGraph((AreaTargeter)source,stateApp,kit,new FacetFactory(ff));
			}
		},kit);
	}
	@Override
	protected void traceOutput(String msg){
		if(false)Times.printElapsed("ExtrasGraph"+msg);
		else if(false)Util.printOut(Debug.info(this)+msg);
		else super.traceOutput(msg);
	}
	private ExtrasGraph(AreaTargeter targeter,ValueNode state,Toolkit kit,FacetFactory ff){
		super(targeter);
		this.state=state;
		this.kit=kit;
		this.ff=ff;
		String keyTail=Util.shortTypeNameKey(targeter.targetType());
		keyOffsets=KEY_GRAPH_OFFSETS+keyTail;
	}
	@Override
	protected ViewableFrame newContentViewable(Object source){
		AreaTargeter targeter=(AreaTargeter)source;
		final TypedNode root=targeter.newGraphNode();
		NodeViewable viewable=new NodeViewable(root){
			@Override
			protected void viewerSelectionChanged(SViewer viewer,SSelection selection){
				super.viewerSelectionChanged(viewer,selection);
				putSelectionState(state,keyOffsets);
			}
		};
		final OffsetPath GRAPH_NO_PATH=new NodePath(new int[]{0,0});
		OffsetPath startPath=GRAPH_NO_PATH;
		try{
			int[]offsets=state.getInts(keyOffsets);
			startPath=offsets.length>0?new NodePath(offsets):GRAPH_NO_PATH;
		}catch(Exception e){
			startPath=GRAPH_NO_PATH;
		}
		String find=graphFindNow?findGraphValue:GRAPH_FIND_NONE;
		boolean sameFind=find.equals(findThen);
		if(find.equals(GRAPH_FIND_NONE))findThen=null;
		else{
			if(!sameFind){
				findPaths.clear();
				graphFindAt=0;
			}
			for(TypedNode d:Nodes.descendants(root))
				if(d.title().toLowerCase().matches(".*\\Q"+find.toLowerCase()+"\\E.*"))
					findPaths.add(new NodePath(Nodes.ancestry(d)));
			findThen=find;
		}
		int findCount=findPaths.size();
		if(graphFindNow&&findCount>0){
			graphFindAt=findCount>graphFindAt?graphFindAt:0;
			if(true)trace(".newContentViewable: find="+find+" sameFind="+sameFind+" finds="
					+findPaths.size()+" graphFindAt=",graphFindAt);
			state.put(keyOffsets,(startPath=findPaths.get(graphFindAt++)).offsets);
		}
		try{
			startPath.members(root);
		}catch(Exception e){
			startPath=GRAPH_NO_PATH;
		}
		viewable.defineSelection(new PathSelection(root,startPath));
		return viewable;
	}
	@Override
	protected FacetedTarget[]newContentViewers(ViewableFrame viewable){
		TreeView view=new TreeView(title()){
			public String nodeRenderText(TypedNode node){
				return node.title();
			}
			public String contentIconKey(Object content){
				return null;
			}
			@Override
			public boolean canChangeSelection(){
				return true;
			}
			@Override
			public boolean hideRoot(){
				return false;
			}
		};
		return new FacetedTarget[]{new ActionViewerTarget(title(),viewable,
				new SFrameTarget(view)){
			public boolean isActive(){
				return true;
			}
		}};
	}
	@Override
	protected void attachContentAreaFacets(AreaRoot area){
		ff.areas().viewerArea(area,new ViewerAreaMaster(){
			protected String hintString(){
				return HINT_EXTRAS_PANE+HINT_BARE;
			}
		});
	}
	@Override
	public LayoutFeatures newContentFeatures(final SContentAreaTargeter area){
		return new FacetFactory(ff){
			@Override
			public SurfaceServices services(){
				return new SurfaceServices(){
					MenuFacets context=new MenuFacets(area,"Tree facets"){
						public SFacet[]getContextFacets(ViewerTarget viewer,SFacet[]viewerFacets){
							return viewerFacets;
						}
					};
					public MenuFacets getContextMenuFacets(){
						return context;
					}
					final public void handleInvalidInput(STarget target,Object input){}
				};
			}
		};
	}
}