package facets.core.app;
import static facets.core.app.AppConstants.*;
import static facets.core.app.AppSurface.ContentStyle.*;
import static facets.core.app.Dialogs.Response.*;
import static facets.util.app.AppValues.*;
import facets.core.app.AppSurface.ContentStyle;
import facets.core.superficial.SIndexing;
import facets.core.superficial.SNumeric;
import facets.core.superficial.STarget;
import facets.core.superficial.STarget.Targeted;
import facets.core.superficial.app.SAreaTarget;
import facets.core.superficial.STargeter;
import facets.core.superficial.SToggling;
import facets.core.superficial.STrigger;
import facets.core.superficial.TargetCore;
import facets.core.superficial.TargeterCore;
import facets.util.Debug;
import facets.util.NumberPolicy;
import facets.util.Tracer;
import facets.util.app.WatchableOperation;
import facets.util.tree.ValueNode;
import java.io.File;
/**
Enables extension by composition of {@link ActionAppSurface} hierarchy. 
<p>{@link AppActions} defines a range of extensible application actions, together with
{@link STarget}s to represent them in the surface (typically as menu items). 
 */
public abstract class AppActions extends Tracer{
	public static final int 
		TARGETS_CORE=0,
		TARGETS_WINDOW=1,
		TARGETS_NEW=2,
		TARGETS_HELP=3,
		TARGETS_LAST=TARGETS_HELP,
		TARGET_WINDOW_MOVE=0,
		TARGET_WINDOW_CLOSE=1;
	public final ActionAppSurface app;
	public final SToggling showHelp;
	private STarget windowTops;
	public AppActions(final ActionAppSurface app){
		this.app=app;
		final boolean offersHelp=app.spec.offersHelp();
		showHelp=new SToggling(TITLE_APP_HELP,false&&offersHelp,new SToggling.Coupler(){
			@Override
			public void stateSet(SToggling t){
				if(t.isSet()&&!offersHelp)throw new IllegalStateException(
						"No help offer for "+Debug.info(app));
			}
		});
	}
	/**
	Create targets representing the actions defined by the instance type. 
	<p>Called from {@link ActionAppSurface#lazyAppAreaElements()} when creating
	the {@link SAreaTarget} which in turn creates {@link AppSurface#surfaceTargeter()}.  
	Default returns triggers grouped as they are likely to be 
	required for application menus; some expose member methods.  
	<p>Targeter elements can be accessed 
	using the following indexes into <code>elements</code>:
	<ul>
	<li>{@link #TARGETS_CORE} - {@link ActionAppSurface#launchPreferences()} and  
	{@link ActionAppSurface#attemptClose()}
	<li>{@link #TARGETS_WINDOW} etc; 
	</ul>
	 */
	protected STarget[]newAppAreaElements(){
		final AppSpecifier spec=app.spec;
		boolean forSlave=spec.forSlave();
		STarget close=new STrigger(forSlave?TITLE_SLAVE_CLOSE:TITLE_APP_CLOSE,
					new STrigger.Coupler(){
				@Override
				public void fired(STrigger t){
					app.attemptClose();
				}
			}),
			about=new STrigger(TITLE_APP_ABOUT+" "+app.title()+"...",new STrigger.Coupler(){
				@Override
				public void fired(STrigger t){
					app.launchAbout();
				}
			});
			return new STarget[]{ 
				new TargetCore("Core Menu",!spec.nature().getBoolean(NATURE_PREFERENCES)?
					new STarget[]{close}:new STarget[]{new STrigger(TITLE_APP_PREFERENCES+"...",
							new STrigger.Coupler(){
						@Override
						public void fired(STrigger t){
							app.launchPreferences();
						}
					}),close}),
				app.contentStyle==ContentStyle.SINGLE?new TargetCore("No Window Menu")
					:new TargetCore("Window Menu",TargetCore.join(newWindowTops().elements(),
						((Targeted)app.surfaceTargeter().areaTarget().attachedFacet()).targets())),
				new STrigger(TITLE_CONTENT_NEW,new STrigger.Coupler(){
					final WatchableOperation newContent=new WatchableOperation("AppActions.newContent"){
						@Override
						public void doSimpleOperation(){
								app.addInternalContent();
						}};
					@Override
					public void fired(STrigger t){
						if(false)app.runWatched(newContent);
						else newContent.doOperations();
					}
				}),
				new TargetCore("Help Menu",forSlave?new STarget[]{}
						:!spec.offersHelp()?new STarget[]{about}:new STarget[]{showHelp,about})
			};
	}
	private STarget newWindowTops(){
		final STarget	windowClose=new STrigger(TITLE_WINDOW_CLOSE,new STrigger.Coupler(){
		  public void fired(STrigger t){
		  	app.tryCloseContent();
		  }
		});
		windowTops=new TargetCore("Window Tops",app.contentStyle==DESKTOP?
				new STarget[]{
			new STrigger(TITLE_WINDOW_NEW,new STrigger.Coupler(){
				@Override
			  public void fired(STrigger t){
					app.cloneActiveArea();
				}
			}),				
			windowClose,				
		}
		:new STarget[]{
				new SNumeric("Move",0,new SNumeric.Coupler(){
					@Override
					public void valueSet(SNumeric n){
						int at=(int)n.value();
						if(false)trace(".valueSet: at=",at);
						if(at!=appAreas().index())app.moveActiveArea(at);
					}
					@Override
					public NumberPolicy policy(SNumeric n){
						return new NumberPolicy(0,appAreas().indexables().length-1){
							@Override
							public String[]incrementTitles(){
								return new String[]{"Up|\u25c4","Down|\u25ba"};
							}
							@Override
							protected boolean reverseIncrements(){
								return false;
							}
						};
					}
			}),
			windowClose
		});
		updateWindowTops();
		return windowTops;
	}
	final void updateWindowTops(){
		if(windowTops==null)return;
		STarget[]elements=windowTops.elements();
		SIndexing areas=appAreas();
		boolean multiple=areas.indexables().length>1;
		if(elements[0]instanceof SNumeric){
			SNumeric move=(SNumeric)elements[0];
			move.setValue(areas.index());
			move.setLive(multiple);
		}
		elements[elements.length-1].setLive(multiple||app.contentStyle==DESKTOP);
	}
	private SIndexing appAreas(){
		return app.areas.appArea().indexing();
	}
	/**
	Check via dialog if content passed may be removed. 
	@param dialogTitle should specify the context for the check
	 */
	protected boolean contentIsRemovable(String dialogTitle,AppContenter content){
		return!app.spec.canEditContent()||!content.hasChanged()
			||app.dialogs().confirmYesNo(dialogTitle,
					"Abandon changes to " +content.title()+"?")==Yes;
	}
	public boolean appCloseAcceptable(){
		String dialogTitle="Closing "+app.title();
		for(ViewerContenter vc:app.findViewerContents())
			if(!contentIsRemovable(dialogTitle,vc))return false;
		return true;
	}
	/**
	May return source file on behalf of surface.
	<p>Called by {@link ActionAppSurface#getOpeningContentSources()};
	default returns <code>null</code>. 
	 */
	public File getOpeningContentSourceFile(){
	  return null;
	}
	/**
	Called from {@link ActionAppSurface#appRetargeted()}. 
	<p>Default is empty stub. 
	 */
	protected void appRetargeted(){}
}