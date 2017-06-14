package demo.appidiom;

import static applicable.textart.TextArt.*;
import static applicable.textart.TextArtConstants.*;
import facets.core.app.ActionAppSurface;
import facets.core.app.AppSurface;
import facets.core.app.FeatureHost;
import facets.core.app.SurfaceServices;
import facets.core.app.ViewerContenter;
import facets.core.app.FeatureHost.LayoutFeatures;
import facets.core.superficial.Notice;
import facets.core.superficial.SFacet;
import facets.core.superficial.STarget;
import facets.core.superficial.STargeter;
import facets.core.superficial.STextual;
import facets.core.superficial.STrigger;
import facets.core.superficial.TargetCore;
import facets.core.superficial.Notifying.Impact;
import facets.core.superficial.app.SContentAreaTargeter;
import facets.core.superficial.app.SContenter;
import facets.facet.FacetFactory;
import facets.facet.app.FacetAppActions;
import facets.facet.app.FacetAppSpecifier;
import facets.facet.app.FacetAppSurface;
import applicable.textart.TextArtContenter;
import applicable.textart.TextArtFeatures;
import applicable.textart.TextArtFeatures.AdvanceFacets;
import demo.codeview.CodeViewContenter;
import demo.codeview.CodeViewFacets;

/** 
{@link FacetAppSurface} for {@link SpikeApp}.  
<p>{@link SpikeAppSurface} extends its superclass to define a windowed 
desktop application with default {@link TextArtContenter} content; an
alternative {@link CodeViewContenter} exposes the
source code of the {@link demo} package tree.  
 */
final class SpikeAppSurface extends FacetAppSurface{

	/**
	{@link CodeViewContenter} for use within {@link SpikeAppSurface}. 
	 */
	private final class CodeContenter extends CodeViewContenter {

		CodeContenter(ViewerContenter.ContentSource source, FacetFactory facets) {
			
			//Pass parameters to superclass with null view and browser references
			super(source, null, null, facets, SpikeAppSurface.class.getSimpleName()); 
		}
		
		@Override
		public LayoutFeatures newContentFeatures(final SContentAreaTargeter area){
			
			//Define and return layout
			return new CodeViewFacets(ff, area) {
						
				//Interface methods
				@Override
				public SFacet[]header(){
					SFacet				
						contentMenus[] = contentMenuRoots(),
						mergedMenus[] = new TextArtFeatures(this, area, advanceFacets
							).newMenuRoots(contentMenus, SpikeAppSurface.this);
					return mergedMenus;
				}
				@Override
				public SFacet status(){
					return toolGroups(area,
							HINT_NONE, SpikeAppSurface.this.newDebugSwitchLabel(spacerWide(0)));
				}
				@Override
				public SurfaceServices services(){
					return this;
				}
			};
		}
		
		@Override
		public void areaRetargeted(SContentAreaTargeter area) {
			
			//Layout
			setLayoutTargetsLive(false);
		}
	}

	//Content source for code trees (also flags content type)
	private final Object codeSource = CodeViewContenter.newTreeSource();
	
	//Shared between text line contenters
	private final AdvanceFacets advanceFacets;

	/**
	Unique constructor. 
	@param values passed to superclass
	@param ff passed to superclass
	@param minimal debug flag 
	 */
	SpikeAppSurface(FacetAppSpecifier values, FacetFactory ff, boolean minimal) {
		
		//Pass parameters to superclass together with surface type
		super(values,ff);
		advanceFacets=minimal?null:new TextArtFeatures.AdvanceFacets(ff);
	}
	
	@Override
	protected CachingStyle cachingStyle(){
		return CachingStyle.checkItemCount;
	}
	
	/**
	 Re-implements abstract framework method. 
	 @see ActionAppSurface#getFixedOpeningContentSources()
	 */
	@Override
	protected Object[] getFixedOpeningContentSources() {
		
		//Return appropriate source references
		return new Object[]{
				//Text lines
				LINES_SOURCE,
//				LINES_SOURCE,
				//codeSource
			};
	}
	
	/**
	Implements abstract framework method. 
	@see ActionAppSurface#getInternalContentSource()
	 */
	@Override
	protected Object getInternalContentSource() {
		
		//Return text lines source
		return LINES_SOURCE;
	}

	/**
	Overrides default framework method. 
	@see ActionAppSurface#lazyAppAreaElements()
	 */
	@Override
	protected STarget[] lazyAppAreaElements() {
		
		//Get defaults, return these if no code windows
		STarget[] defaults = super.lazyAppAreaElements();
		if (!spec.nature().getBoolean(SpikeApp.NATURE_CODE_TREE)) 
			return defaults;
		
		//Retrieve and modify default elements
		STarget 
		
		//Get reference to default element
		defaultNews = defaults[FacetAppActions.TARGETS_NEW].elements()[0],
		
		//Create trigger to add code content		
		codeTrigger = new STrigger("Code &Tree",
				new STrigger.Coupler() {
			
			public void fired(STrigger t) {
				
				//Call framework method to add code as new content
				addContent(codeSource);							
			}
		}),

		//Wrap default with trigger in new target group
		newsGroup = new TargetCore("New", new STarget[]{defaultNews, codeTrigger}); 

		//Insert group into defaults and return
		defaults[FacetAppActions.TARGETS_NEW] = newsGroup;					
		return defaults;
	}
	
	/**
	Implements abstract framework method. 
	@see AppSurface#newContenter(Object)
	 */
	@Override
	protected SContenter newContenter(Object source) {
		
		//Return suitable contenter for source object passed
		return source == codeSource ? 
			new CodeContenter((ViewerContenter.ContentSource) source, ff)
			:new TextArtContenter(source, 
					this, 
					advanceFacets, 
					spec, 
					dialogs(), 
					(FeatureHost)host());
	}

	@Override
	protected void notifiedFlash(Notice notice){
		if(findActiveContent()==emptyContent)return;
		String flash=notice.flashText();
		STargeter[]lineElements=activeContentTargeter().selection().elements();
		STargeter text=lineElements[LINE_TEXT],status=lineElements[LINE_STATUS];
		((STextual)status.target().elements()[0]).setText(
				!flash.trim().equals("")?flash:((STextual)text.target().elements()[0]).text());
		if(false)status.retargetFacets(Impact.DEFAULT);
	}
}
