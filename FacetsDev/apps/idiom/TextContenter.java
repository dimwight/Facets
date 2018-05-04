package apps.idiom;
import static facets.core.app.AppSurface.ContentStyle.*;
import static facets.facet.FacetFactory.*;
import facets.core.app.ActionViewerTarget;
import facets.core.app.AppSpecifier;
import facets.core.app.AreaRoot;
import facets.core.app.SContentAreaTargeter;
import facets.core.app.TextView;
import facets.core.app.ViewableFrame;
import facets.core.app.ViewerContenter;
import facets.core.app.ViewerTarget;
import facets.core.app.FeatureHost.LayoutFeatures;
import facets.core.superficial.SFacet;
import facets.core.superficial.SFrameTarget;
import facets.core.superficial.app.FacetedTarget;
import facets.core.superficial.app.SSelection;
import facets.facet.AppFacetsBuilder;
import facets.facet.FacetFactory;
import facets.facet.ViewerAreaMaster;
import facets.facet.app.FacetAppSurface;
import facets.util.Debug;
import facets.util.app.Disposer;
final class TextContenter extends ViewerContenter{
	private final FacetAppSurface app;
	TextContenter(AppContent content,FacetAppSurface app){
		super(content);
		this.app=app;
	}
	@Override
	protected ViewableFrame newContentViewable(Object source){
		AppContent content=(AppContent)source;
		return new ViewableFrame(content.title(),new Disposer(content)){{
				setSelection(new SSelection(){
					@Override
					public Object single(){
						return((Disposer<AppContent>)framed).disposable().asText();
					}
					@Override
					public Object content(){
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
	protected FacetedTarget[]newContentViewers(ViewableFrame viewable){
		return new ViewerTarget[]{new ActionViewerTarget(viewable.title(),viewable,
				new SFrameTarget(new TextView(FacetsApp.TITLE_TEXT) {
					@Override
					public boolean wrapLines(){
						return true;
					}
				})){}};
	}
	@Override
	protected void attachContentAreaFacets(AreaRoot area){
		app.ff.areas().viewerArea(area,new ViewerAreaMaster(){
			protected String hintString(){
				return HINT_BARE;
			};
		});
	}
	@Override
	public LayoutFeatures newContentFeatures(final SContentAreaTargeter area){
		final AppSpecifier spec=app.spec;
		return!spec.hasSystemAccess()?null:new FacetFactory(app.ff){
			@Override
			public SFacet[]header(){
				SFacet appMenu=menuRoot(new AppFacetsBuilder(this,area).newMenuFacets());
				return spec.contentStyle()==SINGLE?new SFacet[]{appMenu}
					:new SFacet[]{appMenu,menuRoot(windowMenuFacets(area,false))};
			}
			@Override
			public SFacet extras(){
				return false?null:appExtras(app);
			};
		};
	}
	@Override
	public boolean hasChanged(){
		return app.spec.nature().getBoolean(FacetsApp.ARG_CHANGES);
	}
	@Override
	public void wasRemoved(){
		((Disposer)contentFrame().framed).dispose();
	}
}