package apps.idiom;
import static apps.idiom.AppContent.*;
import static apps.idiom.FacetsApp.ContentTypes.*;
import static facets.core.app.AppActions.*;
import static facets.core.app.AppConstants.*;
import static facets.core.app.AppSurface.ContentStyle.*;
import static facets.facet.app.FacetPreferences.*;
import static facets.util.tree.Nodes.*;
import facets.core.app.ActionAppSurface;
import facets.core.app.AppActions;
import facets.core.app.AppConstants;
import facets.core.app.FeatureHost;
import facets.core.app.PagedContenter;
import facets.core.app.SContenter;
import facets.core.app.TextView;
import facets.core.app.AppSurface.ContentStyle;
import facets.core.app.ViewerContenter.ContentSource;
import facets.core.superficial.SIndexing;
import facets.core.superficial.STarget;
import facets.core.superficial.SToggling;
import facets.core.superficial.STrigger;
import facets.core.superficial.TargetCore;
import facets.core.superficial.app.SSurface;
import facets.facet.FacetFactory;
import facets.facet.app.FacetAppActions;
import facets.facet.app.FacetAppSpecifier;
import facets.facet.app.FacetAppSurface;
import facets.facet.app.FacetConstants;
import facets.facet.app.FacetPreferences;
import facets.facet.app.FileAppActions;
import facets.facet.kit.Toolkit;
import facets.facet.kit.swing.KitSwing;
import facets.util.Debug;
import facets.util.Util;
import facets.util.app.Events;
import facets.util.app.HostBounds;
import facets.util.tree.ValueNode;
public final class FacetsApp extends FacetAppSpecifier{
	public static final String ARG_TYPES="contentTypes",ARG_KB="contentKb",ARG_TIMES="contentTimes",
		ARG_SLAVABLE="slavable",ARG_CHANGES="contentChanges";
	static final String TITLE_TEXT="Te&xt";
	enum ContentTypes{Text,Table,Both,Nested;
		public String toString(){
			return this==Table?"T&able":this==Text?TITLE_TEXT:super.toString();
		};
	}
	static final boolean testing=false;
	private static int contentSize;
	private static boolean contentTimes=true;
	final private static ContentSource contentSource=new ContentSource(){
		private int ids=0;
		@Override
		public Object newContent(){
			AppContent c=new AppContent(ids++,contentSize,!contentTimes?0:contentSize*5);
			if(contentTimes)Events.traceEvent(">Simulating build in "+Debug.info(this));
			c.build();
			Events.traceEvent(">Built content "+c);
			return c;
		}
	};
	private final boolean forApplet;
	ContentTypes contentBuild,contentTypes;
	public FacetsApp(Class nameClass,boolean forApplet){
		super(nameClass);
		this.forApplet=forApplet;
		if(testing)trace(".FacetsApp: testing=",testing);
	}
	private FacetsApp(FacetsApp master){
		super(master);
		forApplet=false;
		contentTypes=master.contentTypes;
		contentBuild=master.contentBuild;
	}
	@Override
	public String[]argumentKeys(){
		return new String[]{FacetConstants.NATURE_SWING_SYSTEM,ContentStyle.NATURE_KEY};
	}
	@Override
	public boolean hasSystemAccess(){
		return !forApplet;
	}
	@Override
	public boolean canSaveContent(){
		return true&&!forApplet;
	}
	@Override
	public boolean offersHelp(){
		return true;
	}
	@Override
	public Object[][]decorationValues(){
		return joinDecorations(super.decorationValues(),AppPreferences.DECORATIONS);
	}
	@Override
	public ContentStyle contentStyle(){
		return forApplet||forSlave()?SINGLE:super.contentStyle();
	}
	@Override
	public Toolkit newToolkit(){
		return new KitSwing(!forApplet,false,args().getBoolean(FacetConstants.NATURE_SWING_SYSTEM));
	}
	@Override
	public PagedContenter[]adjustPreferenceContenters(SSurface surface,
			PagedContenter[]contenters){
		return false?contenters:new PagedContenter[]{
				contenters[PREFERENCES_GRAPH],
				contenters[PREFERENCES_TRACE],
				contenters[PREFERENCES_VALUES],
		};
	}
	@Override
	protected FacetPreferences newArgPreferences(SSurface headless,FacetFactory ff){
		return new AppPreferences(this,headless,ff);
	}
	@Override
	protected void addNatureDefaults(ValueNode root){
		super.addNatureDefaults(root);
		mergeContents(root,new Object[]{
				HostBounds.NATURE_SIZE_MIN+"=500,400",
		});
	}
	@Override
	protected AppActions newActions(ActionAppSurface app){
		final class AppMods{
			private ActionAppSurface app;
			AppMods(ActionAppSurface app){
				this.app=app;
			}
			STarget[]modifyAreaElements(STarget[]elements){
				ContentStyle style=app.spec.contentStyle();
				ValueNode args=args();
				final boolean mixedTypes=contentTypes.compareTo(Both)>=0,
				slavable=false||style==DESKTOP?false:!mixedTypes?false
					:style==TABBED?args.getOrPutBoolean(ARG_SLAVABLE,false):true;
				final SToggling external=!slavable||style!=TABBED?null
						:new SToggling("E&xternal Windows",true,new SToggling.Coupler());
				if(external!=null){
					STarget windows=elements[TARGETS_WINDOW];
					elements[TARGETS_WINDOW]=new TargetCore(windows.title(),
							external,windows.elements()[0]);
				}
				if(mixedTypes){
					ContentTypes[]types=ContentTypes.values();
					elements[TARGETS_NEW]=new SIndexing(TITLE_CONTENT_NEW,new Object[]{types[0],types[1]},0,
							new SIndexing.Coupler(){
						@Override
						public void indexSet(SIndexing i){
							contentBuild=(ContentTypes)i.indexed();
							addContent(slavable,external);
						}
					});
				}
				else elements[TARGETS_NEW]=new STrigger(TITLE_CONTENT_NEW,
						new STrigger.Coupler(){
					@Override
					public void fired(STrigger t){
						addContent(slavable,external);
					}
				});
				return elements;
			}
			private void addContent(boolean slavable,SToggling external){
				if(slavable&&(external!=null?external.isSet():contentBuild==Table)){
					FacetsApp master=FacetsApp.this;
					app.openSlave(new FacetsApp(master).newApp(
							FacetFactory.newDesktopCore(newToolkit(),master),null));
				}
				else app.addInternalContent();
			}
		}
		if(contentTypes==null)try{
			ValueNode args=args();
			ContentTypes defaultType=testing?Table:Text;
			contentTypes=ContentTypes.values()[(args.getOrPutInt(ARG_TYPES,defaultType.ordinal()))];
			contentBuild=contentTypes.compareTo(Both)<0?contentTypes:defaultType;
			contentSize=args.getOrPutInt(ARG_KB,SIZES[0]);
			contentTimes=args.getOrPutBoolean(ARG_TIMES,false);
		}catch(Exception e){
			Util.printOut("FacetsApp.newActions: ",e);
		}
		FileAppActions.dummy=true;
		return isFileApp()?new FileAppActions(app){
			@Override
			protected SToggling[]useBarLayoutTargets(SToggling tools,SToggling side,
					SToggling status){
				return new SToggling[]{tools,side};
			}
			@Override
			protected STarget[]newAppAreaElements(){
				return new AppMods(app).modifyAreaElements(super.newAppAreaElements());
			}
		}
		:new FacetAppActions(app){
			@Override
			protected SToggling[]useBarLayoutTargets(SToggling tools,SToggling side,
					SToggling status){
				return new SToggling[]{tools,side};
			}
			@Override
			protected STarget[]newAppAreaElements(){
				return new AppMods(app).modifyAreaElements(super.newAppAreaElements());
			}
		};
	}
	@Override
	protected FacetAppSurface newApp(FacetFactory ff,final FeatureHost host){
		return new FacetAppSurface(this,ff){
			@Override
			protected FeatureHost getPassedHost(){
				if(host==null)throw new IllegalStateException("Null host in "
						+Debug.info(this));
				else return host;
			}
			@Override
			public Object getInternalContentSource(){
				return contentSource;
			}
			@Override
			protected Object[]getFixedOpeningContentSources(){
				return false?new Object[]{contentSource,contentSource}
					:super.getFixedOpeningContentSources();
			}
			@Override
			protected SContenter newContenter(Object source){
				AppContent content=(AppContent)
				 (source instanceof AppContent?source:((ContentSource)source).newContent());
				return((FacetsApp)spec).contentBuild==Text?new TextContenter(content,this)
					:new TableContenter(content,this);
			}
			@Override
			protected CachingStyle cachingStyle(){
				return CachingStyle.noCache;
			}
			@Override
			protected void appOpened(){
				if(false&&dialogs().getTextInput("Debug","",TextView.DEBUG_TEXT,30)==null)System.exit(0);
			}
		};
	}
	public static void main(String[]args){
		new FacetsApp(FacetsApp.class,false).buildAndLaunchApp(
				args.length==0?new String[]{ARG_PREFERENCES}:args);
	}
}