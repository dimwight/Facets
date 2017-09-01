package applicable.eval.app;
import static facets.facet.app.FileAppActions.*;
import static facets.facet.app.tree.TreeTargets.*;
import facets.core.app.MenuFacets;
import facets.core.app.SurfaceServices;
import facets.core.superficial.Notice;
import facets.core.superficial.SFacet;
import facets.core.superficial.STargeter;
import facets.core.superficial.app.SContentAreaTargeter;
import facets.core.superficial.app.ViewerTarget;
import facets.facet.AppFacetsBuilder;
import facets.facet.FacetFactory;
import facets.facet.app.FacetAppSurface;
import facets.facet.kit.KitFacet;
import facets.util.ItemList;
import facets.util.TitledList;
import java.awt.Component;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
class EvalFeatures extends FacetFactory{
	private final FacetAppSurface app;
	private final SContentAreaTargeter area;
	private final STargeter content,viewer;
	private final TitledList<STargeter>files;
	private final boolean forForm;
	EvalFeatures(FacetAppSurface app,SContentAreaTargeter area,boolean forForm){
		super(app.ff);
		this.app=app;
		this.area=area;
		this.forForm=forForm;
		content=area.content();
		viewer=area.viewer();
		files=!app.spec.canSaveContent()?null:new TitledList(
				Notice.findElement((STargeter)area.notifiable(),TARGETS_FILE).elements());
	}
	@Override
	public SFacet toolbar(){
		ItemList<SFacet>facets=new ItemList(SFacet.class);
		if(files!=null&&!forForm){
			facets.addItems(
					triggerButtons(files.titled(TITLE_SAVE),HINT_BARE),
					triggerButtons(files.titled(TITLE_REVERT),HINT_BARE));
			facets.addItem(spacerWide(5));
			facets.addItems(editTools(area.viewer()));
			facets.addItem(spacerWide(10));
		}
		STargeter areaFirst=area.elements()[0];
		if(forForm)
			facets.addItems(indexingDropdownList(areaFirst,HINT_NONE),
					spacerTall(25));
		else {
			STargeter[]search=content.elements()[TARGET_SEARCH].elements();
			if(files!=null)facets.addItems(new SFacet[]{
				textualField(search[0],15,HINT_NONE),
				spacerWide(5),
				indexingIteratorButtons(search[1],HINT_BARE),
				spacerWide(5),
				textualLabel(search[2],HINT_NONE),
			});
			if(true)facets.addItems(editTools(area.viewer()));
			else facets.addItems(triggerButtons(areaFirst,HINT_BARE));
		}
		return toolGroups(area,HINT_PANEL_MIDDLE,facets.items());
	}
	@Override
	public SFacet[]header(){
		return new SFacet[]{
			menuRoot(new AppFacetsBuilder(this,area).newMenuFacets()),
			files==null||forForm?null:menuRoot(new EditFacets(area)),
			files==null||forForm?null:menuRoot(new MenuFacets(content,"Content"){
				@Override
				public SFacet[]getFacets(){
					STargeter[]triggers=content.elements();
					return forForm?null:new SFacet[]{
						triggerMenuItems(triggers[TARGET_TYPE],HINT_NONE),
						triggerMenu(triggers[TARGET_ENCODE],HINT_NONE),
						triggerMenuItems(triggers[TARGET_TABLE],HINT_NONE),
					};
				}
			}),
			forForm?null:menuRoot(new MenuFacets(content,"View"){
				@Override
				public SFacet[]getFacets(){
					return new SFacet[]{
						triggerMenuItems(area.elements()[0],HINT_NONE),
					};
				}
			}),
			menuRoot(windowMenuFacets(area,false)),
			menuRoot(helpMenuFacets(area))
		};
	}
	@Override
	public SFacet status(){
		if(forForm)return null;
		STargeter targeter=area.selection().elements()[0];
		return true?null:toolGroups(targeter,HINT_NONE, 
				spacerTall(15),textualLabel(targeter,HINT_NONE));
	}
	@Override
	public SurfaceServices services(){
		if(forForm)return null;
		MenuFacets context=new MenuFacets(area,"Tree facets"){
			SFacet[]editFacets=new EditFacets(area).getFacets();
			public SFacet[]getContextFacets(ViewerTarget viewer, SFacet[]viewerFacets){
				return FacetFactory.join(
						viewerFacets,FacetFactory.join(new SFacet[]{BREAK},editFacets));
			}
		};
		return app.newFullServices(context);
	}
}