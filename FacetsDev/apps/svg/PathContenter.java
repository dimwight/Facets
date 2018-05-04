package apps.svg;
import facets.core.app.ActionViewerTarget;
import facets.core.app.AreaRoot;
import facets.core.app.FeatureHost.LayoutFeatures;
import facets.core.app.MenuFacets;
import facets.core.app.NodeViewable;
import facets.core.app.PathSelection;
import facets.core.app.SAreaTarget;
import facets.core.app.SContentAreaTargeter;
import facets.core.app.SView;
import facets.core.app.SViewer;
import facets.core.app.TreeView;
import facets.core.app.ViewableFrame;
import facets.core.app.ViewerContenter;
import facets.core.app.ViewerTarget;
import facets.core.app.avatar.AvatarContent;
import facets.core.app.avatar.AvatarView;
import facets.core.app.avatar.PlaneViewWorks;
import facets.core.superficial.SFacet;
import facets.core.superficial.SFrameTarget;
import facets.core.superficial.SIndexing;
import facets.core.superficial.STarget;
import facets.core.superficial.STargeter;
import facets.core.superficial.SToggling;
import facets.core.superficial.app.FacetedTarget;
import facets.core.superficial.app.SSelection;
import facets.facet.AppFacetsBuilder;
import facets.facet.AreaFacets;
import facets.facet.FacetFactory;
import facets.facet.app.FacetAppSurface;
import facets.util.Debug;
import facets.util.geom.Vector;
import facets.util.tree.NodePath;
import facets.util.tree.Nodes;
import facets.util.tree.ValueNode;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import applicable.FileModifiedChecker;
import applicable.SvgPath;
import apps.svg.SvgPathView.ReadValues;
public final class PathContenter extends ViewerContenter{
	final static class Paths implements AvatarContent{
		final SvgPath[]paths;
		Paths(SvgPath[]paths){
			this.paths=paths;
		}
	}
	static final String KEY_NO_ASK="readNoAsk",TITLE_RULE="UNIT_RULE",
		KEY_STATE="pathValues",KEY_SNAP="snapToTicks",
		KEY_BOX="showUnitBox",KEY_RENDER="render",
		RENDER_PEN="Pen",RENDER_FILL="Fill",RENDER_BOTH="Both",
		TYPE_PATHS="paths";
	static final int VALUE_X=0,VALUE_Y=1,VALUE_SCALE=2,VALUE_SPACING=3;
	static final double UNIT=1,ASPECT_X=8,ASPECT_Y=5,
		DRAW_WIDTH=ASPECT_X*UNIT,DRAW_HEIGHT=ASPECT_Y*UNIT,MARGINS=0.5*UNIT,
		SCALE_MIN=0,SCALE_RANGE=5,SCALE_DEFAULT=1,
		VALUES_DEFAULT[]={0,0,1,0};
	final SvgPath unitBox=new SvgPath("UnitBox","M0 0l1 0 0 1 -1 0z",0);
	final SToggling snap=new SToggling("Snap to Ticks",false,new SToggling.Coupler(){
			public void stateSet(SToggling t){
				state.put(KEY_SNAP,t.isSet());
			}
		}),
		box=new SToggling("Unit Box",false,new SToggling.Coupler(){
			public void stateSet(SToggling t){
				state.put(KEY_BOX,t.isSet());
			}
		});
	private final FacetAppSurface app;
	private final FileModifiedChecker fileChecker;
	final ValueNode state;
	final Preferences preferences;
	final SIndexing render;
	String valuesKey;
	Paths paths;
	PathContenter(File source,FacetAppSurface app){
		super(source);
		this.app=app;
		state=app.spec.state();
		preferences=new Preferences(app,state);
		if(!state.getBoolean(KEY_NO_ASK))preferences.launchReadPreferences();
		snap.set(state.getOrPutBoolean(KEY_SNAP,true));
		box.set(state.getOrPutBoolean(KEY_BOX,true));
		render=Preferences.newRenderIndexing(state);
		fileChecker=new FileModifiedChecker(app,source){
			@Override
			protected void fileChanged(){
				app.actions.app.revertContent();
			}
		};
	}
	protected ViewableFrame newContentViewable(Object source){
		ValueNode svg;
		{
			svg=SvgPathView.newScaledFileRoot((File)source,
					ReadValues.SF.useValue(state),ReadValues.SHIFT.useValue(state));
		}
		paths=new Paths(SvgPathView.findPaths(svg,null));
		NodeViewable viewable=new NodeViewable(svg){
			@Override
			public SFrameTarget selectionFrame(){
				return new PathFrame(PathContenter.this);
			}
			@Override
			protected SSelection newViewerSelection(SViewer viewer){
				SSelection selection=selection();
				SView view=viewer.view();
				if(!(view instanceof AvatarView))return selection;
				return PathSelection.newMinimal(new AvatarContent[]{paths});
			}
			@Override
			protected void viewerSelectionChanged(SViewer viewer,SSelection selection){
				super.viewerSelectionChanged(viewer,selection);
				NodePath path=(NodePath)((PathSelection)selection).paths[0];
				int dataAt=1;
				if(path.valueAt()!=dataAt)return;
				ValueNode node=(ValueNode)selection.single();
				PathContenter.this.trace(".viewerSelectionChanged: "+node.title()+"="+node.getString(SvgPath.KEY_DATA));
			}
		};
		viewable.defineSelection(Nodes.descendantTitled(svg,TITLE_RULE));
		return viewable;
	}
	public STarget[]lazyContentAreaElements(SAreaTarget area){
		return new STarget[]{snap,box,render};
	}
	protected FacetedTarget[]newContentViewers(ViewableFrame viewable){
		SView data=new TreeView("Data"),
			rendering=new PlaneViewWorks("Rendering",DRAW_WIDTH+MARGINS,DRAW_HEIGHT+MARGINS,
				new Vector(0,0),new PathAvatars(this)){
		};
		return ActionViewerTarget.newViewerAreas(viewable,ViewerTarget.newViewFrames(
			new SView[]{
				rendering,
				data,
			}));
	}
	protected void attachContentAreaFacets(AreaRoot area){
		app.ff.areas().attachViewerAreaPanes(area,"",AreaFacets.PANE_SPLIT_HORIZONTAL);
	}
	public LayoutFeatures newContentFeatures(final SContentAreaTargeter area){
		final STargeter appTargeter=(STargeter)area.notifiable(),view[]=area.elements();
		return new FacetFactory(app.ff){
			public SFacet[]header(){
				return new SFacet[]{
					menuRoot(new AppFacetsBuilder(this,area).newMenuFacets()),
					menuRoot(new MenuFacets(appTargeter,"View"){
						final SFacet[]facets={
							togglingCheckboxMenuItems(view[0],HINT_NONE),
							togglingCheckboxMenuItems(view[1],HINT_NONE),
							BREAK,
							indexingRadioButtonMenuItems(view[2],HINT_NONE)
						};
						public SFacet[]getFacets(){
							return facets;
						}
					})
				};
			}
			public SFacet toolbar(){
				List<SFacet>list=new ArrayList();
				STargeter[]elements=area.selection().elements();
				String hints=HINT_SLIDER_FIELDS_TICKS_LABELS;
				return false?null:toolGroups(area,HINT_NONE, false?new SFacet[]{
					numericFields(elements[VALUE_X],hints),BREAK,
					numericFields(elements[VALUE_Y],hints),BREAK,
					numericFields(elements[VALUE_SCALE],hints),BREAK,
					numericFields(elements[VALUE_SPACING],hints),
				}
				:new SFacet[]{
					numericSliders(elements[VALUE_X],(int)(ASPECT_X*40),hints),BREAK,
					numericSliders(elements[VALUE_Y],(int)(ASPECT_Y*40),hints),BREAK,
					numericSliders(elements[VALUE_SCALE],(int)(SCALE_RANGE*40),
							hints+HINT_SLIDER_LOCAL),BREAK,
					numericSliders(elements[VALUE_SPACING],(int)ASPECT_X*20,hints),
				});
			}
		};
	}
	public void areaRetargeted(SContentAreaTargeter area){
		render.setIndexed(Preferences.newRenderIndexable(state));
		fileChecker.startChecking();
	}
	double[]getPathValues(String key){
		double[]values=state.getDoubles(key);
		if(false){
			trace(".getValues: key="+key+" values="+state.get(key));
			Debug.printStackTrace(3);
		}
		return values.length==VALUES_DEFAULT.length?values:VALUES_DEFAULT;
	}
}