package pdft.inspect;
import static facets.core.app.AppSurface.ContentStyle.*;
import static facets.core.app.TextView.*;
import static pdft.inspect.PdfPages.*;
import facets.core.app.FeatureHost.LayoutFeatures;
import facets.core.app.MenuFacets;
import facets.core.superficial.SFacet;
import facets.core.superficial.SNumeric;
import facets.core.superficial.STargeter;
import facets.core.superficial.STextual;
import facets.core.superficial.TargeterCore;
import facets.core.superficial.app.SContentAreaTargeter;
import facets.core.superficial.app.ViewerTarget;
import facets.facet.AppFacetsBuilder;
import facets.facet.AreaFacets.PaneDialogStyle;
import facets.facet.FacetFactory;
import facets.facet.app.FacetAppSurface;
import facets.util.NumberPolicy;
final class PdfFeatures extends FacetFactory{
	private final FacetAppSurface app;
	private final SContentAreaTargeter area;
	STargeter pages[],
		goTo,
		lastPage,
		fonts,
		views;
	SFacet mainMenu,
		paneMenu,
		windowMenu,
		helpMenu,
		menus[],
		toolbar;
	PdfFeatures(FacetAppSurface app,SContentAreaTargeter area){
		super(app.ff);
		this.app=app;
		this.area=area;
		pages=area.content().elements();
		goTo=pages[COS_GO_TO_PAGE];
		lastPage=pages[COS_PAGE_COUNT];
		fonts=pages[COS_FONTS];
		views=pages[COS_LAST+1];
		mainMenu=menuRoot(new AppFacetsBuilder(this,area
				).newMenuFacets());
		paneMenu=menuRoot(areas().new PaneFacets("Pane",area){
			@Override
			protected PaneDialogStyle dialogStyle(){
				return PaneDialogStyle.Options;
			}
		});
		windowMenu=app.contentStyle==SINGLE?null
				:menuRoot(windowMenuFacets(area,false));
		helpMenu=menuRoot(helpMenuFacets(area));
		menus=windowMenu==null?
				new SFacet[]{mainMenu,paneMenu,menuRoot(area,"View",
						indexingRadioButtonMenuItems(views,HINT_NONE)),helpMenu}
			:new SFacet[]{mainMenu,paneMenu,windowMenu,helpMenu};
		toolbar=toolGroups(area,HINT_NONE,
				numericNudgeButtons(goTo,HINT_NUMERIC_FIELDS+
						HINT_NUMERIC_NUDGERS_FIRST+HINT_TITLE2),
				textualLabel(lastPage,HINT_NONE),spacerWide(5),
				indexingIteratorButtons(fonts,HINT_BARE+HINT_TITLE2),
				spacerWide(5),
				indexingRadioButtons(views,HINT_BARE)
		);
	}
	@Override
	public SFacet[]header(){
		return menus;
	}
	@Override
	public SFacet extras(){
		return appExtras(app);
	}
	@Override
	public SFacet toolbar(){
		return toolbar;
	}
	@Override
	protected MenuFacets getServicesContextMenuFacets(){
		return new MenuFacets(area,""){
			public SFacet[]getContextFacets(ViewerTarget viewer,
					SFacet[]viewerFacets){
				return new SFacet[]{};
			}
		};
	}
	@Override
	public SFacet status(){
		return null;
	}
	static LayoutFeatures newEmpty(final FacetAppSurface app,final SContentAreaTargeter area){
		return new FacetFactory(app.ff){
			STargeter goTo=TargeterCore.newRetargeted(
					new SNumeric("Page",1,new SNumeric.Coupler(){
						@Override
						public NumberPolicy policy(SNumeric n){
							return new NumberPolicy(0, 0){
								@Override
								public String[]incrementTitles(){
									return new String[]{PAGE_PREVIOUS,PAGE_NEXT};
								}
							};
						}
					}),false),
				lastPage=TargeterCore.newRetargeted(new STextual("pageCount","/  0",
						new STextual.Coupler()),false);
			@Override
			public SFacet[]header(){
				return new SFacet[]{
					menuRoot(new AppFacetsBuilder(this,area).newMenuFacets()),
					menuRoot(helpMenuFacets(area))
				};
			}
			@Override
			public SFacet toolbar(){
				return toolGroups(area,HINT_NONE,
					numericNudgeButtons(goTo,
							HINT_NUMERIC_FIELDS+HINT_NUMERIC_NUDGERS_FIRST+HINT_TITLE2),
					textualLabel(lastPage,HINT_NONE)
				);
			}
			@Override
			public SFacet extras(){
				return appExtras(app);
			}
		};
	}
}