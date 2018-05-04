package facets.facet;
import static facets.core.app.AppConstants.*;
import facets.core.app.AppActions;
import facets.core.app.AreaTargeter;
import facets.core.app.MenuFacets;
import facets.core.app.SContentAreaTargeter;
import facets.core.superficial.SFacet;
import facets.core.superficial.STargeter;
import facets.core.superficial.STrigger;
import facets.facet.app.FacetAppActions;
import facets.facet.app.FacetAppSpecifier;
import facets.facet.app.FileAppActions;
import facets.util.Debug;
import facets.util.ItemList;
/**
Creates {@link MenuFacets} for an application main menu or {@link SFacet} panels for
a ribbon. 
 */
public class AppFacetsBuilder extends FacetFactory{
	private final boolean forSlave;
	private final STargeter appTargeter;
	private final SFacet newsItem,coreItems,fileItems,recentFiles;
	public AppFacetsBuilder(FacetFactory ff,SContentAreaTargeter area){
		super(ff);
		forSlave=spec.forSlave();
		appTargeter=(AreaTargeter)area.notifiable();
		STargeter[]appElements=appTargeter.elements();
		int elementCount=appTargeter.elements().length;
		if(elementCount==0)throw new IllegalStateException("No elements in "+Debug.info(this));
		STargeter core=appElements[AppActions.TARGETS_CORE],
			news=forSlave?null:appElements[AppActions.TARGETS_NEW],
			file=elementCount<=FacetAppActions.TARGETS_LAST+1?null
				:appElements[FileAppActions.TARGETS_FILE];
		boolean ribbon=ff.implementsRibbon(),
			newsTrigger=news!=null&&news.target()instanceof STrigger;
		coreItems=ribbon?triggerButtons(core,HINT_GRID+HINT_BARE)
				:triggerMenuItems(core,HINT_NONE);
		newsItem=!spec.canCreateContent()||forSlave?null
			:ribbon?newsTrigger?triggerButtons(news,HINT_NONE):indexingRibbonButtons(news,HINT_NONE)
			:newsTrigger?triggerMenuItems(news,HINT_NONE)
				:indexingRadioButtonMenu(news,HINT_INDEXING_SELECT);
		fileItems=file==null||file.elements().length==0?null
			:triggerMenuItems(file,HINT_NONE);
		recentFiles=fileItems==null||forSlave?null
			:indexingRadioButtonMenuItems(appElements[FileAppActions.TARGETS_RECENT],
    		HINT_INDEXING_SELECT+HINT_TOOLTIPS);
	}
	final public SFacet ribbonCore(){
		return rowPanel(appTargeter,coreItems);
	}
	final public SFacet ribbonNew(){
		return rowPanel(appTargeter,newsItem);
	}
	/**
	Creates facets for incorporation in menu. 
	 */
	final public MenuFacets newMenuFacets(){
		return new MenuFacets(appTargeter,
				fileItems!=null?TITLE_FILE_MENU:forSlave?TITLE_CORE_SLAVE:TITLE_CORE_MENU){
			public SFacet[]getFacets(){
				final SFacet[]facets=fileItems!=null?assembleFileFacets(
						newsItem,fileItems,recentFiles,coreItems)
					:assembleNonFileFacets(newsItem,coreItems);
				for(SFacet facet:facets)if(facet==null)throw new IllegalStateException(
						"Null facet in "+Debug.info(this));
				return facets;
			}
		};
	}
	protected SFacet[]assembleNonFileFacets(SFacet newsItem,SFacet coreItems){
		return newsItem==null?new SFacet[]{coreItems} 
			:new SFacet[]{newsItem,coreItems};
	}
	protected SFacet[]assembleFileFacets(SFacet newsItem,SFacet fileActions,
			SFacet recentFiles,SFacet coreItems){
		ItemList<SFacet>facets=new ItemList(SFacet.class);
		if(newsItem!=null)facets.add(newsItem);
		facets.addItems(fileActions,BREAK);
		if(recentFiles!=null)facets.addItems(recentFiles,BREAK);
		facets.addItem(coreItems);
		return facets.items();
	}
}