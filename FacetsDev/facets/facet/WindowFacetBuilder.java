package facets.facet;
import static facets.core.app.AppConstants.*;
import static facets.facet.app.FacetAppActions.*;
import facets.core.app.AppActions;
import facets.core.app.AppConstants;
import facets.core.app.AreaTargeter;
import facets.core.app.MenuFacets;
import facets.core.app.SContentAreaTargeter;
import facets.core.superficial.SFacet;
import facets.core.superficial.SNumeric;
import facets.core.superficial.STarget;
import facets.core.superficial.STargeter;
import facets.core.superficial.STrigger;
import facets.core.superficial.app.IndexingTargeter;
import facets.facet.app.FacetAppActions;
import facets.util.Debug;
import facets.util.ItemList;
import java.util.ArrayList;
import java.util.List;
/**
Creates facets for an application window menu. 
 */
public final class WindowFacetBuilder extends FacetFactory{
	public final STargeter app,appElements[],windowElements[];
	public WindowFacetBuilder(FacetFactory src,SContentAreaTargeter area){
		super(src);
	  app=(AreaTargeter)area.notifiable();
	  appElements=app.elements();
		windowElements=appElements[TARGETS_WINDOW].elements();
	}
	/**
	Creates facets for an application window menu. 
	<p>The {@link MenuFacets} created by this method 
	returns a menu facet retargeted on the indexing of the surface root, 
	optionally appended to facets retargeted on elements defined  
	in {@link AppActions}.   
	@param withLayout include layout sub-menu?
	 */
	public MenuFacets newMenuFacets(boolean withLayout){
		final List<SFacet>facets=new ArrayList();
		for(STargeter t:windowElements){
			STarget target=t.target();
			facets.add(target instanceof STrigger?triggerMenuItems(t,HINT_NONE)
						:target instanceof SNumeric?numericNudgeMenu(t,HINT_NONE)
								:togglingCheckboxMenuItems(t,HINT_NONE));
		}
		if(withLayout)facets.add(togglingCheckboxMenu(appElements[TARGETS_LAYOUT],
				HINT_TITLE1));
		facets.add(BREAK);
		facets.add(newListItems());			
		return new MenuFacets(app,TITLE_WINDOW_MENU){
			public SFacet[]getFacets(){
				return facets.toArray(new SFacet[0]);
			}
		};
	}
	private SFacet newListItems(){
		return indexingRadioButtonMenuItems(((IndexingTargeter)app).indexing(),HINT_NONE);
	}
	public SFacet ribbonNew(){
		for(STargeter t:windowElements)
			if(t.target()instanceof STrigger)return triggerButtons(t,HINT_BARE);
		throw new IllegalStateException("No trigger in "+Debug.info(windowElements));
	}
	public SFacet ribbonList(){
		return rowPanel(app,menuRoot(app,TITLE_WINDOW_ACTIVATE,newListItems()));
	}
}
