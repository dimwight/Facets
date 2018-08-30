package facets.facet.app;
import static facets.core.app.AppConstants.*;
import static facets.facet.FacetFactory.*;
import static facets.facet.app.FacetAppActions.*;
import static facets.facet.app.FacetAppSpecifier.*;
import static facets.facet.app.FacetConstants.*;
import static facets.facet.app.FacetPagedSurface.*;
import static facets.util.app.AppValues.*;
import static facets.util.app.Events.*;
import static facets.util.tree.Nodes.*;
import static facets.util.tree.ValueNode.*;
import facets.core.app.ActionAppSurface;
import facets.core.app.Headless;
import facets.core.app.HtmlView;
import facets.core.app.PagedContenter;
import facets.core.app.PagedSurface;
import facets.core.app.SAreaTarget;
import facets.core.app.SContentAreaTargeter;
import facets.core.app.TreeView;
import facets.core.app.AppSurface.ContentStyle;
import facets.core.app.avatar.PainterMaster.Textual;
import facets.core.superficial.Notice;
import facets.core.superficial.Notifying;
import facets.core.superficial.SFacet;
import facets.core.superficial.SIndexing;
import facets.core.superficial.SNumeric;
import facets.core.superficial.STarget;
import facets.core.superficial.STargeter;
import facets.core.superficial.STextual;
import facets.core.superficial.SToggling;
import facets.core.superficial.STrigger;
import facets.core.superficial.STextual.Coupler;
import facets.core.superficial.app.SSurface;
import facets.core.superficial.app.SSurface.WindowAppSurface;
import facets.facet.FacetFactory;
import facets.facet.ValueDialogContenter;
import facets.facet.app.FacetAppActions.BarHide;
import facets.facet.app.FacetPagedContenter.PanelFactory;
import facets.util.NumberPolicy;
import facets.util.Tracer;
import facets.util.TypesKey;
import facets.util.app.AppValues;
import facets.util.tree.Nodes;
import facets.util.tree.ValueNode;
import java.awt.Dimension;
/**
Supplies {@link PagedContenter}s exposing {@link ValueNode}s within {@link AppValues}
for use by {@link FacetAppSpecifier#adjustPreferenceContenters(SSurface, PagedContenter[])}
and {@link FacetAppSpecifier#newArgPreferences(SSurface, FacetFactory)}. 
 */
public class FacetPreferences extends Tracer{
	public static final String TITLE_TRACE=TITLE_TOP+"Trace",
		TITLE_TRACE_TYPE="eventsOrTimesOrMem",
		TITLE_VIEW=TITLE_TOP+"View",TITLE_FEATURES=TITLE_TOP+"Features",
		TITLE_GRAPH=TITLE_TOP+"Graph",
		TITLE_DRAG=TITLE_TOP+"Targeting",TITLE_VALUES=TITLE_TOP+"Values",
		TITLE_SURFACE=TITLE_TOP+"Surface",
		TITLE_SURFACE_STYLE=ContentStyle.class.getSimpleName(),TITLE_SURFACE_LAF="LaF";
	protected final FacetAppSpecifier spec;
	protected final SSurface parent;
	protected final FacetFactory ff;
	protected final ValueNode debug,copyDebug,stateApp,copyApp,args;
	public static final String VALUES_PATH="valuesPath";
	public FacetPreferences(FacetAppSpecifier spec,SSurface parent,FacetFactory ff){
		this.spec=spec;
		this.parent=parent;
		this.ff=ff;
		debug=AppValues.stateDebug;
		copyDebug=(ValueNode)debug.copyState();
		stateApp=spec.state(PATH_APP);
		copyApp=(ValueNode)stateApp.copyState();
		args=spec.args();
	}
	/**
	Index into default {@link #newContenters()} or {@link #newArgContenters()}. 
	 */
	final public static int 
		PREFERENCES_VALUES=0,
		PREFERENCES_TRACE=1,
		PREFERENCES_GRAPH=2,
		PREFERENCES_VIEW=3,
		PREFERENCES_FEATURES=4,
		PREFERENCES_DRAG=5,
		ARGS_SURFACE=0;
	protected PagedContenter[]newArgContenters(){
		return false?new PagedContenter[]{}:new PagedContenter[]{
			new SurfaceArgs(this,args)
		};
	}
	static void openArgs(AppValues spec){
		ValueNode args=spec.args();
		String[]argsCopy=args.values();
		mergeContents(args,spec.state(PATH_ARGS).values());
		mergeContents(args,argsCopy);
	}
	protected void storeWizardArgs(){
		mergeContents(spec.state(PATH_ARGS),args.values());
	}
	static void applyArgs(AppValues spec){
		mergeContents(spec.nature(),spec.args().values());
	}
	final PagedContenter[]newContenters(){
		return new PagedContenter[]{
			new AppValuesContenter(spec,TITLE_VALUES,ff,copyDebug),
			new Trace(this,debug,copyDebug),
			new Graph(this,debug,copyDebug),
			new View(this,debug,copyDebug),
			new Features(TITLE_FEATURES,this,stateApp,copyApp),
			new Drag(this,debug,copyDebug),
		};
	}
	private final static class Drag extends ValueDialogContenter{
		protected static final int TARGET_INTERIM=0,TARGET_WAIT=1;
		private final NumberPolicy dragWaitPolicy=new NumberPolicy(
				DRAG_WAIT_MIN,1000){
			public int format(){
				return FORMAT_DECIMALS_0;
			}
			public int columns(){
				return 4;
			}
		};
		private final SNumeric dragWait;
		private final SToggling notifyInterim;
		@Override
		public Dimension contentAreaSize(){
			return new Dimension(150,100);
		}
		Drag(FacetPreferences fp,ValueNode master,ValueNode working){
			super(TITLE_DRAG,fp.ff,fp.parent,fp.spec,master,working);
			notifyInterim=content.newToggling(working,KEY_DRAG_NOTIFY);
			dragWait=content.newNumeric(working,KEY_DRAG_PAUSE,dragWaitPolicy);
			contentRetargeted(null);
		}
		protected void targetValuesUpdated(STarget target,ValueNode values,String keys){}
		protected void contentRetargeted(ValueNode working){
			boolean interim=notifyInterim.isSet();
			dragWait.setLive(interim);
		}
		public STarget[]lazyContentAreaElements(SAreaTarget area){
			return new STarget[]{notifyInterim,dragWait};
		}
		protected PanelFactory newPanelFactory(FacetFactory core){
			return new PanelFactory(core){
				public SFacet newContentPanel(SContentAreaTargeter t){
					STargeter[]elements=t.elements();
					return rowPanel(t,0,5,HINT_PANEL_INSET,
						togglingCheckboxes(elements[TARGET_INTERIM],HINT_BARE),
						BREAK,
						spacerWide(15),
						numericFields(elements[TARGET_WAIT],HINT_NUMERIC_UNGROUPED),
						BREAK,
						fill());
				}
			};
		}		
	}
	private final static class Graph extends ValueDialogContenter{
		private static final Object[]WHERES={"Non&e","A&pplication","Dialogs"};
		private static final int TARGET_BUILD=0,TARGET_WHERE=1,
			TARGET_FIND_FIELD=2,TARGET_FIND_GO=3;
		private static final boolean showFinds=false;
		private final boolean inArgs;
		private final SIndexing where;
		private final STextual findField;
		private final STrigger findGo;
		private final SToggling build;
		private PagedSurface dialog;
		private final ValueNode argsMaster,argsWorking;
		public Dimension contentAreaSize(){
			return showFinds?new Dimension(320,200):new Dimension(250,150);
		}
		Graph(final FacetPreferences fp,ValueNode master,final ValueNode working){
			super(TITLE_GRAPH,fp.ff,fp.parent,fp.spec,master,working);
			argsMaster=fp.args;
			build=content.newToggling(argsWorking=fp.args,ARG_GRAPH_BUILD);
			inArgs=!(parent instanceof WindowAppSurface);
			where=content.newIndexing(working,KEY_GRAPH_WHERE,
					WHERES,WHERES[graphShowWhere]);
			String stateFind=working.getString(KEY_GRAPH_FIND);
			findField=new STextual(KEY_GRAPH_FIND,
					findGraphValue=stateFind.equals("")?findGraphValue:stateFind,
					new Coupler(){
				public boolean updateInterim(STextual t){
					graphFindNow=false;
					return true;
				}
				public void textSet(STextual t){
					storeFind(working);
				}
			});
			findGo=new STrigger("Find Ne&xt",new STrigger.Coupler(){
				public void fired(STrigger t){
					storeFind(working);
					graphFindNow=true;
					fp.parent.notify(new Notice(t,Notifying.Impact.DEFAULT));
					graphFindNow=false;
				}
			});
			build.setLive(inArgs);
			findGo.setLive(!inArgs);
		}
		protected void targetValuesUpdated(STarget target,ValueNode values,String keys){
			if(target==where)values.put(KEY_GRAPH_WHERE,((SIndexing)target).index());
			else if(target==build)Nodes.mergeContents(argsMaster,argsWorking.values());
		}
		protected void contentRetargeted(ValueNode working){		
			boolean graphs=false?working.getBoolean(ARG_GRAPH_BUILD):build.isSet();
			if(!graphs)where.setIndex(0);
			where.setLive(graphs);
			boolean showNone=graphShowWhere!=GRAPH_NONE&&where.index()>0;
			findField.setLive(showNone);
			findGo.setLive(findField.isLive()&&!findField.text().trim().equals("")&&!inArgs);
		}
		public void applyChanges(){
			super.applyChanges();
			super.applyChanges();
			dialog.host().updateLayout(parent);
		}
		public STarget[]lazyContentAreaElements(SAreaTarget area){
			return new STarget[]{build,where,findField,findGo};
		}
		private void storeFind(final ValueNode copy){
			String fieldFind=findField.text().trim();
			copy.put(KEY_GRAPH_FIND,
					findGraphValue=fieldFind.equals("")?GRAPH_FIND_NONE:fieldFind);
		}
		protected PanelFactory newPanelFactory(FacetFactory core){
			return new PanelFactory(core){
				public SFacet newContentPanel(SContentAreaTargeter t){
					STargeter[]elements=t.elements();
					return rowPanel(t,0,5,HINT_PANEL_INSET,join(new SFacet[]{
						togglingCheckboxes(elements[TARGET_BUILD],HINT_BARE+(inArgs?HINT_TOOLTIPS:"")),
						BREAK,spacerWide(10),
						indexingRadioButtons(elements[TARGET_WHERE],HINT_TALL),
					},
					showFinds?new SFacet[]{
						BREAK,spacerWide(10),
						rowPanel(t,5,0,HINT_PANEL_MIDDLE,
							textualField(elements[TARGET_FIND_FIELD],15,HINT_NONE),
							triggerButtons(elements[TARGET_FIND_GO],HINT_BARE)),
						BREAK,fill()
					}:new SFacet[]{BREAK,fill()}));
				}
			};
		}
		public void setSurface(PagedSurface surface){
			this.dialog=surface;
		}
	}
	private static final class SurfaceArgs extends ValueDialogContenter{
		private static final int STYLE_AT=0,EMPTY_AT=1,LAF_AT=2;
		private final SIndexing style;
		private final SToggling empty,laf;
		public Dimension contentAreaSize(){
			return new Dimension(250,180);
		}
		private SurfaceArgs(FacetPreferences fp,ValueNode working){
			super(TITLE_SURFACE,fp.ff,fp.parent,fp.spec,working,working);
			ContentStyle[]styles=ContentStyle.values();
			String styleKey=ContentStyle.NATURE_KEY,lafKey=NATURE_SWING_SYSTEM;
			boolean readStyle=app.hasArgument(styleKey),readLaf=app.hasArgument(lafKey);
			int styleAt=!readStyle?NO_INT:working.getInt(styleKey);
			ContentStyle styleNow=styleAt==NO_INT?fp.spec.contentStyle():styles[styleAt];
			style=content.newIndexing(working,TITLE_SURFACE_STYLE,styles,styleNow);
			style.setLive(readStyle);
			laf=content.newToggling(working,lafKey+"|"+TITLE_SURFACE_LAF);
			laf.setLive(readLaf);
			if(!readLaf)laf.set(true);
			empty=content.newToggling(working,NATURE_OPEN_EMPTY);
		}
		@Override
		public STarget[]lazyContentAreaElements(SAreaTarget area){
			return new STarget[]{style,empty,laf};
		}
		protected PanelFactory newPanelFactory(FacetFactory core){
			return new PanelFactory(core){
				@Override
				public SFacet newContentPanel(SContentAreaTargeter t){
					STargeter elements[]=t.elements(),laf=elements[LAF_AT];
					return rowPanel(t,0,0,HINT_PANEL_INSET+HINT_NONE,
						indexingRadioButtons(elements[STYLE_AT],HINT_TALL+HINT_HEADED),BREAK,
						spacerWide(30),togglingCheckboxes(elements[EMPTY_AT],HINT_BARE),BREAK,
						spacerTall(5),BREAK,
						rowPanel(laf,10,5,HINT_PANEL_BORDER+HINT_TITLE1,
							togglingCheckboxes(laf,HINT_BARE),
							BREAK,fill()),
						BREAK,fill()
					);
				}
			};
		}
		@Override
		protected void targetValuesUpdated(STarget target,ValueNode values,String keys){
			if(target==style)values.put(ContentStyle.NATURE_KEY,((SIndexing)target).index());
		}
		@Override
		protected void contentRetargeted(ValueNode working){
			boolean desktop=style.indexed()==ContentStyle.DESKTOP;
			empty.setLive(desktop);
			if(!desktop)empty.set(false);
		}
	}
	private final static class Trace extends ValueDialogContenter{
		private static final int TRACE_AT=0,MODE_AT=1,FILTERS_AT=2,RESET_AT=3;
		private final NumberPolicy timesResetPolicy=new NumberPolicy(
				50,10000){
			public int format(){
				return FORMAT_DECIMALS_0;
			}
			public int columns(){
				return 4;
			}
		};
		private final SToggling trace;
		private final SIndexing mode;
		private final STarget eventFilters;
		private final SNumeric timesReset;
		public Dimension contentAreaSize(){
			return new Dimension(280,275);
		}
		Trace(FacetPreferences fp,ValueNode master,ValueNode working){
			super(TITLE_TRACE,fp.ff,fp.parent,fp.spec,master,working);
			trace=content.newToggling(working,KEY_TRACE);
			mode=content.newIndexing(working,TITLE_TRACE_TYPE,
				new String[]{KEY_EVENTS,KEY_TIMES,KEY_MEM},
				working.getBoolean(KEY_TIMES)?KEY_TIMES
				:working.getBoolean(KEY_MEM)?KEY_MEM
				:KEY_EVENTS);
			eventFilters=content.newMultipleIndexing((ValueNode)Nodes.child(working,
					KEY_FILTERS),KEY_FILTERS,FILTERS,true);
			timesReset=content.newNumeric(working,KEY_TIMES_RESET,timesResetPolicy);
		}
		protected PanelFactory newPanelFactory(FacetFactory core){
			return new PanelFactory(core){
				public SFacet newContentPanel(SContentAreaTargeter t){
					STargeter elements[]=t.elements();
					final SFacet[]modes=indexingRadioButtonSingles(elements[MODE_AT],HINT_BARE);
					return rowPanel(t,0,5,HINT_PANEL_INSET+HINT_NONE,
						togglingCheckboxes(elements[TRACE_AT],HINT_BARE),BREAK,
						rowPanel(t,15,0,HINT_NONE,
							modes[0],
							BREAK,spacerWide(10),
							indexingPaneChecked(elements[FILTERS_AT],200,6,
									HINT_TEXT_FONT+HINT_TALL),
							BREAK,modes[1],
							BREAK,spacerWide(10),
							numericFields(elements[RESET_AT],HINT_NUMERIC_UNGROUPED),
							BREAK,modes[2],
							BREAK,fill()),
						BREAK,fill()
					);
				}
			};
		}
		protected void targetValuesUpdated(STarget target,ValueNode values,String keys){
			if(target==mode){
				int index=((SIndexing)target).index();
				values.put(KEY_EVENTS,index==0);
				values.put(KEY_TIMES,index==1);
				values.put(KEY_MEM,index==2);
			}
		}
		protected void contentRetargeted(ValueNode working){
			boolean trace=working.getBoolean(KEY_TRACE),
				events=mode.index()==0;
			mode.setLive(trace);
			eventFilters.setLive(trace&events);
			timesReset.setLive(trace&&!events);
		}
		public STarget[]lazyContentAreaElements(SAreaTarget area){
			return new STarget[]{trace,mode,eventFilters,timesReset};
		}
	}
	private final static class View extends ValueDialogContenter{
		private static final int TARGET_TREE=0,TARGET_HTML=1,TARGET_ALIASING=2,TARGET_FONT=3;
		private final SToggling treeDebug,htmlSource,aliasing;
		public Dimension contentAreaSize(){
			return new Dimension(240,110);
		}
		View(final FacetPreferences fp,ValueNode master,final ValueNode working){
			super(TITLE_VIEW,fp.ff,fp.parent,fp.spec, master, working);
			treeDebug=content.newToggling(working,TreeView.KEY_DEBUG);
			htmlSource=content.newToggling(working,HtmlView.KEY_SOURCE);
			aliasing=content.newToggling(working,Textual.KEY_TEXT_ALIASING);
			aliasing.setLive(false);
		}
		protected void targetValuesUpdated(STarget target,ValueNode values,String keys){}
		public STarget[]lazyContentAreaElements(SAreaTarget area){
			return new STarget[]{treeDebug,htmlSource,aliasing,fontIndexing};
		}
		protected void contentRetargeted(ValueNode working){}
		protected PanelFactory newPanelFactory(FacetFactory core){
			return new PanelFactory(core){
				public SFacet newContentPanel(SContentAreaTargeter t){
					STargeter[]elements=t.elements();
					return rowPanel(t,0,5,HINT_PANEL_INSET,
						togglingCheckboxes(elements[TARGET_TREE],HINT_BARE),
						BREAK,
						togglingCheckboxes(elements[TARGET_HTML],HINT_BARE),
						BREAK,
						indexingDropdownList(elements[TARGET_FONT],HINT_NONE),
						BREAK,
						togglingCheckboxes(elements[TARGET_ALIASING],HINT_BARE),
						BREAK,fill()
					);
				}
			};
		}
	}
	private final static class Features extends ValueDialogContenter{
		private static final int TARGET_STATUS=0,TARGET_SPLASH=1,TARGET_TIMEOUTS=2;
		private final SToggling status,splash,timeouts;
		private final ValueNode values,copy;
		public Dimension contentAreaSize(){
			return new Dimension(160,100);
		}
		Features(String title,FacetPreferences fp,ValueNode master,
				ValueNode working){
			super(title,fp.ff,fp.parent,fp.spec, master, working);
			this.values=master;
			this.copy=working;
			status=content.newToggling(working,FacetAppSurface.KEY_DEBUG_STATUS);
			splash=content.newToggling(working,FacetAppSurface.KEY_SPLASH);
			timeouts=content.newToggling(working,ActionAppSurface.KEY_TIMEOUTS+"|"+TITLE_FEATURES);
			status.setLive(!BarHide.Status.readHide(master,TypesKey.EMPTY));
			splash.setLive(fp.spec.contentStyle()!=ContentStyle.DESKTOP);
		}
		protected void targetValuesUpdated(STarget target,ValueNode values,String keys){}
		protected void contentRetargeted(ValueNode working){}
		public STarget[]lazyContentAreaElements(SAreaTarget area){
			return new STarget[]{status,splash,timeouts};
		}
		protected PanelFactory newPanelFactory(FacetFactory core){
			return new PanelFactory(core){
				public SFacet newContentPanel(SContentAreaTargeter t){
					STargeter[]elements=t.elements();
					return rowPanel(t,0,10,HINT_PANEL_INSET,
						togglingCheckboxes(elements[TARGET_STATUS],HINT_BARE),BREAK,
						togglingCheckboxes(elements[TARGET_SPLASH],HINT_BARE),BREAK,
						togglingCheckboxes(elements[TARGET_TIMEOUTS],HINT_BARE),BREAK,
						fill());
				}
			};
		}
	}
}