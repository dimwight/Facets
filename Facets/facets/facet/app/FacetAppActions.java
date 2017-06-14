package facets.facet.app;
import static facets.facet.app.FacetAppActions.BarHide.*;
import static facets.util.app.AppValues.*;
import facets.core.app.ActionAppSurface;
import facets.core.app.AppActions;
import facets.core.app.AppSurface;
import facets.core.superficial.STarget;
import facets.core.superficial.SToggling;
import facets.core.superficial.TargetCore;
import facets.util.TypesKey;
import facets.util.tree.ValueNode;
/**
{@link AppActions} for a {@link FacetAppSurface}. 
<p>{@link FacetAppActions} extends its superclass by implementing abstract actions 
in terms of {@link FacetAppSurface}. 
 */
public class FacetAppActions extends AppActions{
	public static final int TARGETS_LAYOUT=AppActions.TARGETS_LAST+1,
		TARGETS_LAST=TARGETS_LAYOUT,TARGET_SIDEBAR=1;
	public enum BarHide{Toolbar("Toolbar"),Sidebar("S&idebar"),Status("St&atus");
		public static void updateTogglings(AppSurface app,TypesKey features){
			ValueNode state=app.spec.state(PATH_APP);
			for(STarget e:app.surfaceTargeter().target().elements()[TARGETS_LAYOUT].elements())
				for(BarHide bar:values())if(e.title().equals(bar.title))
					((SToggling)e).set(!bar.readHide(state,features));
		}
		public final String title;
		private BarHide(String title){
			this.title="Show "+title+"|"+title;
		}
		public static String key(TypesKey features){
			return "hideBars"+(features.equals(TypesKey.EMPTY)?"":("_"+features.keyText));
		}
		SToggling newToggling(final ActionAppSurface app){
			final ValueNode state=app.spec.state(PATH_APP);
			return new SToggling(title,readHide(state,TypesKey.EMPTY),
					new SToggling.Coupler(){
				public void stateSet(SToggling t){
					boolean hide=!t.isSet();
					writeHide(state,hide,app.activeFeatures());
					app.updateLayout();
				}
			});
		}
		public boolean readHide(ValueNode state,TypesKey features){
			String key=key(features);
			int[]hides=state.getInts(key);
			return hides.length==0?false:hides[ordinal()]!=0;
		}
		public void writeHide(ValueNode state,boolean hide,TypesKey features){
			String key=key(features);
			int[]hides=state.getInts(key);
			if(hides.length==0)hides=new int[]{0,0,0};
			hides[ordinal()]=hide?1:0;
			state.put(key,hides);
			if(features==TypesKey.EMPTY)state.put(key(TypesKey.EMPTY),hides);
		}
	}
	public FacetAppActions(ActionAppSurface app){
		super(app);
	}
	/**
	Overrides superclass method. 
	<p>Adds the {@link SToggling}s returned by
	 {@link #useBarLayoutTargets(SToggling, SToggling, SToggling)}
	 */
	protected STarget[]newAppAreaElements(){
		STarget[]superTargets=super.newAppAreaElements();
		return TargetCore.join(superTargets,new STarget[]{
				new TargetCore("Layout",useBarLayoutTargets(Toolbar.newToggling(app),
						Sidebar.newToggling(app),Status.newToggling(app)))
		});
	}
	/**
	Define which targets should be exposed by {@link SToggling}s in the surface. 
	<p>Indexed by {@link #TARGETS_LAYOUT}
	 */
	protected SToggling[]useBarLayoutTargets(SToggling tools,SToggling side,
			SToggling status){
		return new SToggling[]{tools,side,status};
	}
}
