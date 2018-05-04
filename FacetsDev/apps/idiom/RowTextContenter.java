package apps.idiom;
import static facets.facet.FacetFactory.*;
import facets.core.app.ActionViewerTarget;
import facets.core.app.AreaRoot;
import facets.core.app.SAreaTarget;
import facets.core.app.SContentAreaTargeter;
import facets.core.app.ViewableFrame;
import facets.core.app.ViewerContenter;
import facets.core.app.ViewerTarget;
import facets.core.app.FeatureHost.LayoutFeatures;
import facets.core.superficial.SFacet;
import facets.core.superficial.STargeter;
import facets.core.superficial.SToggling;
import facets.core.superficial.app.FacetedTarget;
import facets.core.superficial.app.SSelection;
import facets.facet.FacetFactory;
import facets.facet.ViewerAreaMaster;
import facets.util.Debug;
abstract class RowTextContenter extends ViewerContenter{
	private final FacetFactory ff;
	private final String title;
	RowTextContenter(AppContent source,FacetFactory ff,String title){
		super(source);
		this.ff=ff;
		this.title=title;
	}
	@Override
	final public String title(){
		return title;
	}
	@Override
	final protected ViewableFrame newContentViewable(Object source){
		return new ViewableFrame(title(),((AppContent)source).asText()){{
				setSelection(new SSelection(){
					@Override
					public Object content(){
						return framed;
					}
					@Override
					public Object single(){
						return framed;
					}
					@Override
					public Object[] multiple(){
						throw new RuntimeException("Not implemented in "+Debug.info(this));
					}
				});
			}
		};
	}
	@Override
	final protected FacetedTarget[]newContentViewers(ViewableFrame viewable){
		String title=title();
		return new ViewerTarget[]{new ActionViewerTarget(title,viewable,
				TableContenterBase.newTextFrame(title)){}};
	}
	@Override
	final protected void attachContentAreaFacets(AreaRoot area){
		ff.areas().viewerArea(area,new ViewerAreaMaster(){
			@Override
			protected String hintString(){
				return false?HINT_PANEL_ABOVE:HINT_BARE;
			}
			@Override
			protected SFacet newViewTools(STargeter t){
				return ff.toolGroups(t,HINT_NONE,ff.togglingCheckboxes(t.elements()[0],HINT_BARE));
			}
		});
	}
	@Override
	public void alignContentAreas(SAreaTarget then,SAreaTarget now){
		getWrapToggling(now).set(getWrapToggling(then).isSet());
	}
	private SToggling getWrapToggling(SAreaTarget area){
		return((SToggling)((ViewerTarget)area.activeFaceted()).viewFrame().elements()[0]);
	}
	@Override
	final public LayoutFeatures newContentFeatures(final SContentAreaTargeter t){
		return null;
	}
}