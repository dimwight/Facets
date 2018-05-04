package apps.idiom;
import static apps.idiom.FacetsApp.*;
import static apps.idiom.FacetsApp.ContentTypes.*;
import static facets.core.app.AppSurface.ContentStyle.*;
import static facets.facet.AreaFacets.*;
import static facets.facet.app.FacetAppActions.*;
import facets.core.app.AppSpecifier;
import facets.core.app.FacetHostable;
import facets.core.app.FeatureHost.LayoutFeatures;
import facets.core.app.MenuFacets;
import facets.core.app.PagedActions;
import facets.core.app.PagedContenter;
import facets.core.app.SAreaTarget;
import facets.core.app.SContentAreaTargeter;
import facets.core.app.SurfaceServices;
import facets.core.app.ViewerTarget;
import facets.core.superficial.Notice;
import facets.core.superficial.Notifying.Impact;
import facets.core.superficial.SFacet;
import facets.core.superficial.SFrameTarget;
import facets.core.superficial.STarget;
import facets.core.superficial.STargeter;
import facets.core.superficial.SToggling;
import facets.facet.AppFacetsBuilder;
import facets.facet.AreaFacets;
import facets.facet.FacetFactory;
import facets.facet.app.FacetAppSurface;
import facets.facet.app.FacetPagedContenter;
import java.awt.Dimension;
import apps.idiom.FacetsApp.ContentTypes;
final class TableContenter extends TableContenterBase{
	public static final String KEY_PLAIN="Plain",KEY_GROUPS="Groups";
	private final ContentTypes types;
	private Boolean wrapThen;
	TableContenter(AppContent source,FacetAppSurface app){
		super(source,app);
		types=((FacetsApp)app.spec).contentTypes;
	}
	protected SFrameTarget[]chooseViewFrames(SFrameTarget basic,SFrameTarget full,
			SFrameTarget text,SFrameTarget nestedText,SFrameTarget nestedTable,SFrameTarget tree,
			SFrameTarget form){
		return types==Both?false?new SFrameTarget[]{basic,full,text,nestedText,nestedTable}
			:new SFrameTarget[]{basic,full,testing?nestedText:text}
				:types==Nested?new SFrameTarget[]{full,nestedText,nestedTable}
			:new SFrameTarget[]{false&&testing?tree:basic,
					new SFrameTarget[]{full,text,nestedText,form}[testing?3:0]};
	}
	@Override
	public void areaRetargeted(SContentAreaTargeter area){
		if(wrapThen==null)return;
		SToggling wrapNow=getWrapToggling((SAreaTarget)area.target());
		if(wrapNow!=null){
			wrapNow.set(wrapThen);
			if(false)trace(".areaRetargeted: wrapNow=",wrapNow);
			wrapThen=null;
		}
	}
	@Override
	public void alignContentAreas(SAreaTarget then,SAreaTarget now){
		SToggling wrap=getWrapToggling(then);
		if(wrap!=null)wrapThen=wrap.isSet();
	}
	private SToggling getWrapToggling(SAreaTarget area){
		STarget[]indexables=area.indexableTargets();
		if(indexables.length<2)return null;
		STarget[]elements=((ViewerTarget)
				((SAreaTarget)indexables[1]).activeFaceted()).viewFrame().elements();
		return elements.length==0?null:((SToggling)elements[0]);
	}
	@Override
	protected void attachAreaPanes(AreaFacets areas,SAreaTarget area,SFacet[]viewers){
		if(types==Both){
			if(viewers.length>3){
				if(false)areas.attachPanes(area,viewers,
						new int[][]{{PANE_SPLIT_HORIZONTAL,PANE_UPPER,PANE_SPLIT_HORIZONTAL},{},{},
								{PANE_SPLIT_VERTICAL,PANE_RIGHT,PANE_SPLIT_VERTICAL}},
						new double[]{0.65,0.3,0.45,0.45},
						new int[]{0,0,0},new String[]{"Basic","Full","Others"});
				else areas.attachPanes(area,viewers,
						new int[][]{{PANE_SPLIT_HORIZONTAL},{PANE_SPLIT_HORIZONTAL},
							{PANE_SPLIT_VERTICAL,PANE_RIGHT,PANE_SPLIT_VERTICAL}},
						new double[]{0.65,0.3,0.45,0.45},
						new int[]{0,0},new String[]{"T&ables","Others"});
			}
			else areas.attachPanes(area,viewers,
					new int[][]{{PANE_SPLIT_HORIZONTAL},{PANE_STACK}},
					new double[]{0.7,0.5},
					new int[]{1,0},new String[]{"T&ables","Te&xt"});
		}
		else if(true&&types==Nested&&nested2)areas.attachPanes(area,viewers,
				new int[]{PANE_SPLIT_HORIZONTAL,PANE_UPPER,PANE_SPLIT_HORIZONTAL},
				new double[]{0.5,0.66});
		else areas.attachPanes(area,viewers,PANE_SPLIT_HORIZONTAL);
	}
	@Override
	protected String layoutKey(SAreaTarget area){
		ContentTypes contentTypes=ContentTypes.values()[app.spec.args().getInt(FacetsApp.ARG_TYPES)];
		return TableContenter.class.getSimpleName()+"_"+contentTypes.name()+
			(contentTypes==Nested&&nested2?"2":"");
	}
	@Override
	public LayoutFeatures newContentFeatures(SContentAreaTargeter area){
		final AppSpecifier spec=app.spec;
		final Class type=area.targetType();
		final boolean rowText=RowTextContenter.class.isAssignableFrom(type);
		final SContentAreaTargeter top=area.contentAncestor(TableContenter.class),
			panes=type==TableContenter.class?top
				:area.contentAncestor(TableContenterBase.class);
		final STargeter layout=panes.elements()[PANE_LAYOUT],
			panesShow=panes.elements()[PANE_SHOW],
			wrap=!rowText?null:area.view().elements()[0];
		return new FacetFactory(app.ff){
			@Override
			public SFacet[]header(){
				SFacet appMenu=menuRoot(new AppFacetsBuilder(this,top).newMenuFacets()),
					panesMenu=menuRoot(panesShow,"Panes",new SFacet[]{
						types==Both?triggerMenuItems(panes.elements()[PANE_SELECT],HINT_TOOLTIPS)
							:togglingCheckboxMenuItems(panesShow,HINT_BARE+HINT_GRID)
					});
				return spec.contentStyle()==SINGLE?new SFacet[]{appMenu,panesMenu}
					:new SFacet[]{appMenu,menuRoot(windowMenuFacets(top,true)),panesMenu};
			}
			@Override
			public SFacet toolbar(){
				SFacet layoutMenu=menuRoot(layout,layout.title(),
						triggerMenuItems(layout,HINT_NONE));
				return toolGroups(top,HINT_NONE,wrap==null?new SFacet[]{layoutMenu}
					:new SFacet[]{layoutMenu,togglingButtons(wrap,HINT_BARE)});
			}
			@Override
			public SFacet sidebar(){
				return sidebarHost((STargeter)Notice.findElement((STargeter)top.notifiable(),
						TARGETS_LAYOUT,TARGET_SIDEBAR),
					constructSidebarHostable(panes,app));
			}
			@Override
			public SurfaceServices services(){
				final SFacet[]facets=wrap==null?new SFacet[]{
					togglingCheckboxMenuItems(panesShow,HINT_NONE),
					triggerMenu(layout,HINT_NONE)
				}
				:new SFacet[]{
					togglingCheckboxMenuItems(wrap,HINT_BARE),
					togglingCheckboxMenu(panesShow,HINT_TITLE1),
					triggerMenu(layout,HINT_NONE)
				};
				return app.newFullServices(new MenuFacets(top,"rowText="+rowText){
					@Override
					public SFacet[]getFacets(){
						return facets;
					}
				});
			}
			@Override
			public SFacet extras(){
				return panes!=top?null:appExtras(app);
			}
		};
	}
	private static FacetHostable constructSidebarHostable(final SContentAreaTargeter area,
			final FacetAppSurface app){
		return new FacetHostable(){
			@Override
			public void facetRetargeted(Hosting host,STarget target,Impact impact){
				host.refreshPaged("Panes",PagedActions.NONE,area.target(),app);
			}
			@Override
			public PagedContenter[]newPagedContenters(Object source){
				final STarget content=(STarget)source;
				return new PagedContenter[]{new FacetPagedContenter("Panes",app.ff){
					@Override
					public STarget[]lazyContentAreaElements(SAreaTarget area){
						return content.elements();
					}
					@Override
					public Dimension contentAreaSize(){
						return new Dimension(130,100);
					}
					@Override
					protected PanelFactory newPanelFactory(FacetFactory core){
						return new PanelFactory(core){
							@Override
							public SFacet newContentPanel(SContentAreaTargeter t){
								STargeter layout=t.elements()[PANE_LAYOUT],show=t.elements()[PANE_SHOW];
								return rowPanel(t,0,5,HINT_NONE,false?new SFacet[]{
										togglingCheckboxes(show,HINT_TALL+HINT_HEADED),BREAK,
										triggerButtons(layout,HINT_TALL+HINT_HEADED),
										BREAK,fill()}
								:new SFacet[]{
										togglingCheckboxes(show,HINT_BARE+HINT_TALL),BREAK,
										triggerButtons(layout,HINT_BARE+HINT_GRID),
										BREAK,fill()});
							}
						};
					}
				}};
			}
		};
	}
	@Override
	public boolean hasChanged(){
		return app.spec.nature().getBoolean(FacetsApp.ARG_CHANGES);
	}
}