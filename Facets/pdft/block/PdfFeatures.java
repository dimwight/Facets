package pdft.block;
import static facets.core.app.AppSurface.ContentStyle.*;
import static facets.core.app.TextView.*;
import static pdft.block.PdfPages.*;
import facets.core.app.MenuFacets;
import facets.core.app.SurfaceServices;
import facets.core.app.FeatureHost.LayoutFeatures;
import facets.core.app.AppSurface.ContentStyle;
import facets.core.superficial.SFacet;
import facets.core.superficial.SNumeric;
import facets.core.superficial.STargeter;
import facets.core.superficial.STextual;
import facets.core.superficial.TargeterCore;
import facets.core.superficial.app.SContentAreaTargeter;
import facets.core.superficial.app.SView;
import facets.core.superficial.app.SelectingFrame;
import facets.core.superficial.app.ViewerTarget;
import facets.facet.AppFacetsBuilder;
import facets.facet.AreaFacets.PaneDialogStyle;
import facets.facet.FacetFactory;
import facets.facet.app.FacetAppSurface;
import facets.util.ItemList;
import facets.util.NumberPolicy;
import pdft.block.PageAvatarPolicies.PageRenderView;
final class PdfFeatures extends FacetFactory{
	private final static boolean minimal=false;
	private final FacetAppSurface app;
	private final SContentAreaTargeter area;
	private final boolean forBlockTexts;
	private final STargeter pages[],goTo,lastPage,fonts,render;
	private final MenuFacets blocks;
	PdfFeatures(FacetAppSurface app,final SContentAreaTargeter root,boolean forBlockTexts){
		super(app.ff);
		this.app=app;
		this.area=root;
		if(false)traceDebug(".PdfFeatures: root=",root);
		this.forBlockTexts=forBlockTexts;
		pages=root.content().elements();
		goTo=pages[GO_TO_PAGE];
		lastPage=pages[PAGE_COUNT];
		fonts=pages[FONTS];
		render=pages[RENDER];
		blocks=!minimal&&forBlockTexts?new MenuFacets(root,"Edit"){
			private SFacet[]selected,editing;
			@Override
			public SFacet[]getContextFacets(ViewerTarget viewer,SFacet[]viewerFacets){
				boolean textBlockSelected=((SelectingFrame)viewer.viewable).selectionFrame().framed
						instanceof TextBlock;
				if(!textBlockSelected)return new SFacet[]{};
				PageRenderView view=(PageRenderView)viewer.view();
				return ((PageAvatarPolicies)view.avatars()).forBlockEdit?
				editing==null?editing=new SFacet[]{
					triggerMenuItems(pages[BLOCK_EDITING],HINT_NONE),
					}:editing
				:selected==null?selected=new SFacet[]{
						triggerMenuItems(pages[BLOCK_SELECTED],HINT_NONE),
					}:selected;
			}
		}:null;
	}
	@Override
	public SFacet extras(){
		return appExtras(app);
	}
	@Override
	public SFacet[]header(){
		SFacet appMenu=menuRoot(new AppFacetsBuilder(this,area).newMenuFacets());
		if(minimal)return new SFacet[]{appMenu};
		ItemList<SFacet>menus=new ItemList(SFacet.class);
		menus.add(appMenu);
		if(area.elements().length>0)menus.add(menuRoot(
				areas().new PaneFacets("Pane",area){
			@Override
			protected PaneDialogStyle dialogStyle(){
				return forBlockTexts?PaneDialogStyle.None:PaneDialogStyle.Options;
			}
		}));
		if(false)menus.add(menuRoot(area,"View",indexingRadioButtonMenuItems(render,HINT_NONE)));
		if(app.contentStyle!=ContentStyle.SINGLE)menus.add(
				menuRoot(windowMenuFacets(area,false)));
		if(true||!forBlockTexts)menus.add(menuRoot(helpMenuFacets(area)));
		return menus.items();
	}
	@Override
	protected MenuFacets getServicesContextMenuFacets(){
		return blocks;
	}
	@Override
	public SFacet toolbar(){
		return minimal?null:toolGroups(area,HINT_NONE,
				numericNudgeButtons(goTo,HINT_NUMERIC_FIELDS+
						HINT_NUMERIC_NUDGERS_FIRST+HINT_TITLE2),
				textualLabel(lastPage,HINT_NONE),spacerWide(5),//BREAK,
				indexingIteratorButtons(fonts,HINT_BARE+HINT_TITLE2),
				spacerWide(5),//BREAK,
				indexingRadioButtons(render,HINT_BARE)
		);
	}
	static LayoutFeatures newEmpty(final FacetAppSurface app,final SContentAreaTargeter area){
		return new FacetFactory(app.ff){
			STargeter goTo=TargeterCore.newRetargeted(
					new SNumeric("Page",1,new SNumeric.Coupler(){
						@Override
						public NumberPolicy policy(SNumeric n){
							return new NumberPolicy(0,0){
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
				return minimal?null:toolGroups(area,HINT_NONE,
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