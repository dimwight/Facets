package facets.core.app;
import static facets.core.app.AppSurface.ContentStyle.*;
import static facets.util.app.Events.*;
import facets.core.app.AppSurface.ContentStyle;
import facets.core.app.FeatureHost.LayoutFeatures;
import facets.core.app.ViewerContenter.ContentSource;
import facets.core.superficial.Facetable;
import facets.core.superficial.FacetedTarget;
import facets.core.superficial.Notice;
import facets.core.superficial.SFacet;
import facets.core.superficial.SFrameTarget;
import facets.core.superficial.STarget;
import facets.core.superficial.STrigger;
import facets.core.superficial.TargeterCore;
import facets.core.superficial.Notifying.Impact;
import facets.core.superficial.app.AreaTargeter;
import facets.core.superficial.app.SAreaTarget;
import facets.core.superficial.app.SContentAreaTargeter;
import facets.core.superficial.app.SContenter;
import facets.core.superficial.app.SHost;
import facets.core.superficial.app.SSelection;
import facets.core.superficial.app.SSurface;
import facets.core.superficial.app.SView;
import facets.core.superficial.app.ViewableAction;
import facets.core.superficial.app.ViewableFrame;
import facets.core.superficial.app.ViewerTarget;
import facets.util.Debug;
import facets.util.HtmlBuilder;
import facets.util.Titled;
import facets.util.Util;
import facets.util.tree.ValueNode;
/**
Allows an {@link AppSurface} to be run headless eg for debugging.
 */
final public class Headless{
	public static SSurface newHeadlessSurface(final String title){
		return new SSurface(){
			@Override
			public String title(){
				return title;
			}
			@Override
			public SHost host(){
				return new HeadlessHost(this);
			}
			@Override
			public boolean isBuilt(){
				return false;
			}
			@Override
			public void notify(Notice notice){
				throw new RuntimeException("Not implemented in "+Debug.info(this));
			}
			@Override
			public AreaTargeter surfaceTargeter(){
				throw new RuntimeException("Not implemented in "+Debug.info(this));
			}
			@Override
			public void buildRetargeted(){
				throw new RuntimeException("Not implemented in "+Debug.info(this));
			}
		};
	}
	private static final class Contenter extends ViewerContenter{
		Contenter(ContentSource source){
			super(source);
		}
		@Override
		protected ViewableFrame newContentViewable(Object source){
			return new ViewableFrame("ViewableFrame",((ContentSource)source).newContent()){{
				setSelection(new SSelection(){
					@Override
					public Object content(){
						return framed;
					}
					@Override
					public Object single(){
						return content();
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
			STarget view=new SFrameTarget(new SView(){
				@Override
				public String title(){
					return "View";
				}
				@Override
				public boolean isLive(){
					throw new RuntimeException("Not implemented in "+Debug.info(this));
				}
				@Override
				public boolean allowMultipleSelection(){
					throw new RuntimeException("Not implemented in "+Debug.info(this));
				}
			});
			return new FacetedTarget[]{new ViewerTarget(viewable.title(),viewable,view){
				@Override
				protected ViewableAction getTriggerAction(STrigger trigger){
					throw new RuntimeException("Not implemented in "+Debug.info(this));
				}
				@Override
				protected STrigger[]newActionTriggers(
						ViewableAction[]actions){
					return new STrigger[]{};
				}}};
		}
		@Override
		protected void attachContentAreaFacets(AreaRoot area){
			for(STarget each:area.descendants())
				if(each instanceof Facetable)((Facetable)each).attachFacet(newAreaFacet());
		}
		@Override
		public LayoutFeatures newContentFeatures(SContentAreaTargeter area){
			return newFeatures();
		}
	}
	static MountFacet newAreaFacet(){
		return new MountFacet(){
			@Override
			public void retarget(STarget target,Impact impact){}
			@Override
			public void setFacets(SFacet...facets){}
			@Override
			public void dispose(){
				throw new RuntimeException("Not implemented in "+Debug.info(this));
			}
		};
	}
	private static final class App extends AppSurface{
		private SHost host;
		App(AppSpecifier spec){
			super(spec);
		}
		@Override
		protected boolean isHeadless(){
			return true;
		}
		@Override
		protected Object[]getOpeningContentSources(){
			return new Object[]{contentStyle==DESKTOP?DESKTOP:contentSource};
		}
		@Override
		protected SContenter newContenter(Object source){
			return new Contenter((ContentSource)source);
		}
		@Override
		public SHost host(){
			if(host!=null)return host;
			else return host=newHeadlessHost();
		}
		@Override
		protected MountFacet newMultiContentFacet(SAreaTarget appArea){
			return newAreaFacet();
		}
		@Override
		public boolean contentIsRemovable(AppContenter content){
			return true;
		}
		@Override
		protected LayoutFeatures newEmptyDesktopFeatures(SContentAreaTargeter root){
			return newFeatures();
		}
		@Override
		public boolean attemptClose(){
			throw new RuntimeException("Not implemented in "+Debug.info(this));
		}
		private final ContentSource contentSource=new ContentSource(){
			private int contents;
			@Override
			public Object newContent(){
				throw new RuntimeException("Not implemented in "+Debug.info(this));
			}
		};
		@Override
		public void openApp(){
			super.openApp();
			if(false)Debug.printStackTrace(10);
			else if(false)throw new RuntimeException("Debug");
			else{
				int goes=Debug.memCheck?3:0;
				for(int g=0;g<goes;g++){
					Util.printOut("");
					switch(contentStyle){
					case SINGLE:replaceSingleContent(contentSource);break;
					case DESKTOP:addContent(contentSource);break;
					case TABBED:revertActiveContent();break;
					default:
					}
				}
			}
			if(false)trace(".openApp: targeters=",TargeterCore.targeters);
		}
	}
	public static void main(String[]args){
		AppSpecifier spec=new AppSpecifier(Headless.class){
			@Override
			public boolean hasSystemAccess(){
				return false;
			}
			@Override
			public ContentStyle contentStyle(){
				return ContentStyle.SINGLE;
			}
			@Override
			protected void addStateDefaults(ValueNode root){
				stateDebug.setContents(new Object[]{
					KEY_TRACE+"="+true,
					KEY_EVENTS+"="+true,
					KEY_MEM+"="+true,
					DEFAULT_FILTERS
				});
			}
			@Override
			protected AppActions newActions(ActionAppSurface app){
				throw new RuntimeException("Not implemented in "+Debug.info(this));
			}
		};
		spec.readValues(args);
		((FeatureHost)new App(spec).host()).openHostedSurface();
	}
	private static LayoutFeatures newFeatures(){
		return new LayoutFeatures(){
			@Override
			public SFacet toolbar(){
				return null;
			}
			@Override
			public SFacet status(){
				return null;
			}
			@Override
			public SFacet sidebar(){
				return null;
			}
			@Override
			public SurfaceServices services(){
				return null;
			}
			@Override
			public SFacet[] header(){
				return null;
			}
			@Override
			public SFacet extras(){
				return null;
			}
		};
	}
}