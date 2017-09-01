package pub.view;
import static pub.PubValues.*;
import static pub.view.ListingViewable.*;
import static pub.view.RecordContenter.ViewIs.*;
import static pub.view.RecordViewable.*;
import facets.core.app.AppSpecifier;
import facets.core.app.MenuFacets;
import facets.core.app.SurfaceServices;
import facets.core.superficial.SFacet;
import facets.core.superficial.STargeter;
import facets.core.superficial.app.SContentAreaTargeter;
import facets.facet.AppFacetsBuilder;
import facets.facet.WindowFacetBuilder;
import facets.facet.AreaFacets.PaneDialogStyle;
import facets.facet.FacetFactory;
import facets.facet.app.FacetAppSpecifier;
import facets.facet.app.FacetAppSurface;
import pub.PubValues;
import pub.view.RecordContenter.StateView;
import pub.view.RecordContenter.ViewIs;
final class RecordFeatures extends FacetFactory{
	private final FacetAppSurface app;
	private final SContentAreaTargeter area;
	private final AppSpecifier spec;
	private final ViewIs viewIs;
	private final STargeter record[],exports,notes;
	private final boolean stateViewer,minimal,ribbon;
	RecordFeatures(FacetAppSurface app,SContentAreaTargeter area,AppSpecifier spec,
			ViewIs viewIs){
		super(app.ff);
		this.app=app;
		this.area=area;
		this.spec=spec;
		this.viewIs=viewIs;
		ribbon=((FacetAppSpecifier)app.spec).headerIsRibbon();
		minimal=area.areaTarget().indexableTargets().length==2;
		record=area.content().elements();
		exports=record[TARGET_EXPORT];
		notes=record[TARGET_NOTES];
		stateViewer=area.viewer().targetType()==StateView.class;
	}
	@Override
	protected MenuFacets getServicesContextMenuFacets(){
		if(viewIs==Preview)throw new RuntimeException("Not implemented in "+this);
		else return new MenuFacets(area,"Context facets"){
			@Override
			public SFacet[]getFacets(){
				return new SFacet[]{triggerMenu(exports,HINT_NONE)};
			}
		};
	}
	@Override
	public SFacet toolbar(){
		return minimal?null:toolGroups(area,HINT_NONE,
				!stateViewer?new SFacet[]{triggerButtons(exports,HINT_BARE),spacerWide(10),
				triggerButtons(notes,HINT_BARE)}:editTools(area.viewer()));
	}
	@Override
	public SFacet[]header(){
		if(ribbon)return ribbon();
		SFacet appMenu=menuRoot(new AppFacetsBuilder(this,area).newMenuFacets()),
		windowMenu=menuRoot(windowMenuFacets(area,false)),
		panesMenu=menuRoot(areas().new PaneFacets("Panes",area){
			@Override
			protected PaneDialogStyle dialogStyle(){
				return minimal||userView?super.dialogStyle():PaneDialogStyle.Simple;
			}
		}),
		recordMenu=menuRoot(area,RecordContenter.KEY_TOP,new SFacet[]{
				triggerMenu(exports,HINT_NONE),
				triggerMenu(notes,HINT_NONE),
				triggerMenuItems(record[TARGET_ATTACH],HINT_NONE),BREAK,
				triggerMenuItems(record[TARGET_RESET],HINT_NONE),
		});
		return false?null:
			userView?new SFacet[]{
				appMenu,
				windowMenu,
				panesMenu,
				recordMenu
			}
			:viewIs!=Window?stateViewer?new SFacet[]{
			appMenu,
			windowMenu,
			panesMenu,
			recordMenu,
			menuRoot(new EditFacets(area,"State"))
		}
		:new SFacet[]{
			appMenu,
			windowMenu,
			panesMenu,
			recordMenu
		}
		:new SFacet[]{
			appMenu,
			panesMenu,
			recordMenu
		};
	}
	private SFacet[]ribbon(){
		AppFacetsBuilder app=new AppFacetsBuilder(this,area);
		WindowFacetBuilder window=new WindowFacetBuilder(this,area);
		return new SFacet[]{
			ribbonTab(area,"Home",app.ribbonNew(),window.ribbonList(),
					window.ribbonNew(),spacerWide(5),app.ribbonCore()),
		};
	}
}