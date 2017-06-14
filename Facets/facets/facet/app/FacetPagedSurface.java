package facets.facet.app;
import static facets.core.app.AppConstants.*;
import static facets.core.app.Dialogs.*;
import static facets.facet.FacetFactory.*;
import static facets.util.Debug.*;
import static java.lang.Math.*;
import facets.core.app.AppSpecifier;
import facets.core.app.Dialogs;
import facets.core.app.HideableHost;
import facets.core.app.ListView;
import facets.core.app.PagedActions;
import facets.core.app.PagedContenter;
import facets.core.app.PagedSurface;
import facets.core.app.TreeView;
import facets.core.app.Dialogs.Response;
import facets.core.superficial.Notice;
import facets.core.superficial.SFacet;
import facets.core.superficial.STarget;
import facets.core.superficial.STargeter;
import facets.core.superficial.SToggling;
import facets.core.superficial.STrigger;
import facets.core.superficial.Notifying.Impact;
import facets.core.superficial.STrigger.Coupler;
import facets.core.superficial.app.AreaTargeter;
import facets.core.superficial.app.SAreaTarget;
import facets.core.superficial.app.SView;
import facets.facet.AreaFacets;
import facets.facet.FacetFactory;
import facets.facet.ViewerAreaMaster;
import facets.facet.kit.DialogHost;
import facets.facet.kit.KitFacet;
import facets.util.Debug;
import facets.util.app.AppValues;
import facets.util.tree.NodeList;
import facets.util.tree.TypedNode;
import java.util.ArrayList;
import java.util.List;
/**
{@link PagedSurface} with attached {@link FacetFactory}. 
<p>Declared <code>abstract</code> to enable use of classname in values keys. 
 */
public abstract class FacetPagedSurface extends PagedSurface{
	public static final String TITLE_TOP=false?"":FacetPagedSurface.class.getSimpleName(),
			TITLE_HEADER=TITLE_TOP+"Header";
	/**
	{@link FacetPagedSurface} for multi-page dialogs. 
	 */
	public abstract static class MultiPaged extends FacetPagedSurface{
		public MultiPaged(String title,HideableHost host,PagedActions actions,
				PagedContenter[]contents,FacetAppSurface app){
			super(title,host,actions,contents,app);
		}
		public MultiPaged(String title,HideableHost host,PagedActions actions,
				PagedContenter[]contents,FacetFactory ff,AppSpecifier spec){
			super(title,host,actions,contents,ff,spec);
		}
		@Override
		protected PagingStyle pagingStyle(){
			return PagingStyle.Tree;
		}
		protected void attachContentAreas(SAreaTarget areaRoot){
			STarget[]children=areaRoot.indexableTargets();
			AreaFacets areas=ff.areas();
			SFacet[]childrenFacets={
				areas.viewerArea((SAreaTarget)children[MULTI_AREA_TREE],
						new ViewerAreaMaster(){
					protected String hintString(){
						return HINT_BARE;
					}
				}),
				areas.switchMount((SAreaTarget)children[MULTI_AREA_PAGES]),
			};
			areas.attachPanes(areaRoot,childrenFacets,AreaFacets.PANE_SPLIT_VERTICAL);
		}
		/**
		Re-implementation creating either a {@link ListView} or a {@link TreeView}. 
		 */
		final protected SView newPagesTreeView(TypedNode[]treeNodes){
			boolean fullTreeView=false;
			for(TypedNode node:treeNodes)fullTreeView|=node.children().length>0;
			return !fullTreeView?new ListView(title()){
				public String contentIconKey(Object content){
					return null;
				}
				public boolean useTextFont(){
					return super.useTextFont();
				}
				public String nodeRenderText(TypedNode node){
					return node.title();
				}
				protected boolean includeValue(TypedNode parent,Object value){
					return false;
				}
			}
			:new TreeView(title()){
				public String contentIconKey(Object content){
					return null;
				}
				public boolean hideRoot(){
					return true;
				}
				public boolean canChangeSelection(){
					return true;
				}
				protected boolean filterNodeContents(){
					return true;
				}
				protected boolean includeValue(TypedNode parent,Object value){
					return false;
				}
				public String nodeRenderText(TypedNode node){
					return node.title();
				}
			};
		}
		protected SAreaTarget[]newContentPages(PagedContenter[]contents, 
				SAreaTarget[]contentRoots){
			return contentRoots;
		}
	}
	public static final class WizardPaged extends MultiPaged{
		public static final String TITLE_BACK="< &Back",TITLE_NEXT="Next >",TITLE_FINISH="Finish",
				TITLE_CANCEL="Cancel";
		public final SToggling showHelp=new SToggling(TITLE_APP_HELP,false,new SToggling.Coupler(){
			@Override
			public void stateSet(SToggling t){
				if(t.isSet()&&!spec.offersHelp())throw new IllegalStateException(
						"No help offer for "+Debug.info(spec));
			}
		});
		public WizardPaged(String title,HideableHost host,PagedActions actions,
				PagedContenter[]contents,FacetFactory ff,AppSpecifier spec){
			super(title,host,actions,contents,ff,spec);
		}
		@Override
		protected SFacet newExtras(AreaTargeter targeter){
			return ff.extras(targeter,showHelp,false);
		}
		@Override
		protected PagingStyle pagingStyle(){
			return PagingStyle.Wizard;
		}
		protected void attachContentAreas(SAreaTarget area){
			STarget[]children=area.indexableTargets();
			AreaFacets areas=ff.areas();
			areas.mount(area,false).setFacets(
					areas.switchMount((SAreaTarget)children[MULTI_AREA_PAGES]));
		}
		public static PagedActions newActions(){
			return new PagedActions(){
				final STrigger[]triggers=new STrigger[]{
					new STrigger(TITLE_BACK,new Coupler(){
						@Override
						public void fired(STrigger t){
							triggers[1].setLive(true);
							triggers[0].setLive(--pageAt>0);
							showPage(pageAt);
						}
					}),
					new STrigger(TITLE_NEXT,new Coupler(){
						@Override
						public void fired(STrigger t){
							triggers[0].setLive(true);
							triggers[1].setLive(++pageAt<pageCount-1);
							showPage(pageAt);
						}
					}),
					new STrigger(TITLE_FINISH,new Coupler(){
						@Override
						public boolean makeDefault(STrigger t){
							return true;
						}
						@Override
						public void fired(STrigger t){
							for(PagedContenter content:contents)content.applyChanges();
							surface.hideHost(Response.Ok); 
						}
					}),
					new STrigger(TITLE_CANCEL,new Coupler(){
						@Override
						public void fired(STrigger t){
							for(PagedContenter content:contents)content.reverseChanges();
							surface.hideHost(Response.Cancel); 
						}
					})
				};
				private MultiPaged surface;
				private PagedContenter[]contents;
				private int pageCount,pageAt;
				@Override
				public STrigger[]newTriggers(){
					surface=(MultiPaged)surface();
					contents=surface.contents();
					pageCount=contents.length;
					triggers[0].setLive(pageAt>0);
					return triggers;
				}
				private void showPage(int pageAt){
					((SAreaTarget)((SAreaTarget)surface.surfaceTargeter().target()
						).indexableTargets()[FacetPagedSurface.MULTI_AREA_PAGES]).indexing(
							).setIndex(pageAt);
				}
			};
		}
	}
	protected final FacetFactory ff;
	private SFacet extension;
	private boolean extendSideways;
	/**
	Convenience constructor. 
	 */
	public FacetPagedSurface(String title,HideableHost host,PagedActions actions, 
			PagedContenter[]contents,FacetAppSurface app){
		this(title,host,actions,contents,app.ff,app.spec);
	}
	/**
	Core constructor. 
	 */
	public FacetPagedSurface(String title,HideableHost host,
			PagedActions actions,PagedContenter[]contents,FacetFactory ff,AppSpecifier spec){
		super(title,host,spec,actions,contents);
		this.ff=ff;
	}
	protected void attachContentAreas(SAreaTarget root){
		ff.areas().switchMount(root);
		if(false)trace(".attachcontentAreas: ",root.indexableTargets());
	}
	final protected void attachPageFacet(SAreaTarget panel){
		ff.areas().tabs(panel,HINT_NONE);
	}
	protected void traceOutput(String msg){
		if(false)super.traceOutput(msg);
	}
	final protected SFacet newControlButtons(STargeter link){
		return link.elements().length==0?FacetFactory.NO_FACET 
			:ff.triggerButtons(link,(false?HINT_DEBUG:"")+HINT_USAGE_PANEL+HINT_GRID+HINT_BARE);
	}
	protected SFacet newExtras(AreaTargeter targeter){
		return ff.extras(targeter,null,false);
	}
	/**
	Overrides superclass method. 
	<p>{@link Notice}s with {@link Impact#MINI} are ignored
	 */
	public void notify(Notice notice){
		if(notice==null)throw new IllegalArgumentException("Null notice in "+Debug.info(this));
		else if(notice.impact==Impact.MINI)return;
		if(extension!=null){
			((DialogHost)host()).setWindowExtension(((KitFacet)extension).target().isLive()?
					extension:null,extendSideways);
		}
		super.notify(notice);
	}
	public void addExtensionPanel(SFacet panel,boolean atSide){
		if(extension!=null)throw new IllegalArgumentException(
				"Already added "+info(panel)+" in " +info(this));
		else extension=panel;
		extendSideways=atSide;
	}
	/**
	Convenience method for creating a tabbed dialog. 
	@return {@link facets.core.app.Dialogs.Surfacer} that will created an appropriate {@link FacetPagedSurface}
	 */
	public static Dialogs.Surfacer newDefaultTabbedSurfacer(){
		return new Dialogs.Surfacer(){
			public PagedSurface newSurface(String title,HideableHost host,PagedActions actions,
					PagedContenter[]contents,WindowAppSurface parent){
				return new FacetPagedSurface(title,host,actions,contents,(FacetAppSurface)parent){};
			}
		};
	}
	/**
	Convenience method for creating a paged dialog controlled by a tree or list view. 
	@return {@link facets.core.app.Dialogs.Surfacer} that will create a {@link FacetPagedSurface} with a private
	implementation of {@link FacetPagedSurface#newPageTreeNodes(SAreaTarget[])} creating an
	appropriate tree. 
	@param apps contenters creating pages to appear directly under the tree root
	@param debugs contenters creating pages to appear under a child root created 
	from the first member which must be titled {@link #TITLE_HEADER}   
	 */
	public static Dialogs.Surfacer newDefaultPagedSurfacer(final PagedContenter[]apps,
			final PagedContenter[]debugs){
		return new Dialogs.Surfacer(){
			public PagedSurface newSurface(String title,
					HideableHost host,PagedActions actions,PagedContenter[]contents,WindowAppSurface parent){
				return new MultiPaged(title,host,actions,contents,(FacetAppSurface)parent){
		protected TypedNode[]newPageTreeNodes(SAreaTarget[]pages){
			if(true)throw new RuntimeException("Not implemented in "+Debug.info(this));
			List<TypedNode>nodes=new ArrayList();
			SAreaTarget debugHeader=pages[apps.length];
			if(!debugHeader.title().equals(TITLE_HEADER))
				throw new IllegalStateException("Bad header title in "+debugHeader);
			NodeList debugRoot=debugs.length==0?null
				:new NodeList(newPageTreeNode(debugHeader),true);
			for(SAreaTarget page:pages){
				if(page.title().equals(TITLE_HEADER))continue;
				TypedNode node=newPageTreeNode(page);
				cleanNodeTitle(node);
				if(debugRoot!=null&&page.title().startsWith(TITLE_TOP))
					debugRoot.add(node);
				else nodes.add(node);
			}
			if(debugRoot!=null){
				cleanNodeTitle(debugRoot.parent);
				nodes.add(debugRoot.parent);
			}
			return nodes.toArray(new TypedNode[]{});
		}
		private void cleanNodeTitle(TypedNode node){
			node.setTitle(node.title().replace(TITLE_TOP,""));
		}
	};
			}
		};
	}
}