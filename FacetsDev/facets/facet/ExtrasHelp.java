package facets.facet;
import static facets.facet.FacetFactory.*;
import static facets.facet.HelpPages.*;
import facets.core.app.ActionAppSurface;
import facets.core.app.ActionViewerTarget;
import facets.core.app.SAreaTarget;
import facets.core.app.SView;
import facets.core.app.TextTreeView;
import facets.core.app.TreeView;
import facets.core.superficial.SFacet;
import facets.core.superficial.SFrameTarget;
import facets.core.superficial.STarget;
import facets.core.superficial.STargeter;
import facets.core.superficial.Notifying.Impact;
import facets.core.superficial.app.FacetedTarget;
import facets.facet.kit.KMount;
import facets.facet.kit.KWrap;
import facets.facet.kit.KitFacet;
import facets.util.Debug;
import facets.util.OffsetPath;
import facets.util.Tracer;
import facets.util.Util;
import facets.util.tree.NodePath;
import facets.util.tree.TypedNode;
/**
Displays a {@link HelpPages} in the extras pane. 
<p>Constructed package-private in {@link FacetFactory#extras()}
 */
final class ExtrasHelp extends Tracer implements KitFacet{
	protected final static OffsetPath NO_PATH=new NodePath(new int[]{0,0});
	private final static String traceTop=ExtrasHelp.class.getSimpleName();
	private final HelpPages pages; 
	private final ViewerFacet viewer;
	private final KitFacet area;
	@Override
  public void dispose(){
  	throw new RuntimeException("Not implemented in "+this);
  };
	protected void traceOutput(String msg){
		Util.printOut(traceTop+msg);
	}
	ExtrasHelp(final HelpPages pages,FacetFactory ff){
		this.pages=pages;
		SView view=new TextTreeView(title()){
			protected String newPathNodeText(TypedNode[]path,String text){
				return pages.newPathNodeText(path,text);
			}
		};
		viewer=newViewerFacet(view,ff,false);
		area=(KitFacet)viewer.newAreaFacet(SAreaTarget.newArea(title(),viewer.target));
	}
	public void retarget(STarget target,Impact impact){
		pages.updateFacet(this);
		((FacetedTarget)area.target()).retargetFacets(impact);
	}
	private ViewerFacet newViewerFacet(SView view,final FacetFactory ff,final boolean tree){
		return new ViewerFacet(new ActionViewerTarget((tree?"Debug ":"")+title(),
				pages.viewable,
			tree?new SFrameTarget(new TreeView(title()){
				public boolean hideRoot(){
					return false;
				}
			})
			:new SFrameTarget(view){
				protected STarget[]lazyElements(){
					return pages.targets;
				};
			}){
			public boolean isActive(){
				return true;
			}
		},
		ff,
		new ViewerAreaMaster(){
			protected SFacet newViewTools(STargeter t){
				STargeter[]elements=t.elements();
				return tree?super.newViewTools(t)
					:ff.toolGroups(t,HINT_NONE, new SFacet[]{
							true?BREAK:ff.textualField(elements[TARGET_TOPIC],10,HINT_USAGE_FORM),
							ff.indexingIteratorButtons(elements[TARGET_HISTORY],HINT_BARE+HINT_TITLE1),
							ff.indexingIteratorButtons(elements[TARGET_FONT],HINT_BARE+HINT_TITLE1)});
			}
			protected String hintString(){
				return HINT_EXTRAS_PANE+HINT_PANEL_ABOVE+HINT_PANEL_BORDER;
			}
		},
		null);
	}
	public KWrap base(){
		return area.base();
	}
	public String title(){
		return "Help";
	}
	public STarget target(){
		return area.target();
	}
	public KWrap[]items(){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	public void targetNotify(Object notice,boolean interim){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
}