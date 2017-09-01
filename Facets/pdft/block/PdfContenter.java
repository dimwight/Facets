package pdft.block;
import static facets.core.app.ActionViewerTarget.*;
import static facets.facet.AreaFacets.*;
import static facets.facet.FacetFactory.*;
import static facets.util.Debug.*;
import static facets.util.app.WatchableOperation.CancelStyle.*;
import static pdft.block.PdfPages.*;
import facets.core.app.ActionViewerTarget;
import facets.core.app.AreaRoot;
import facets.core.app.FeatureHost;
import facets.core.app.AppSurface.ContentCreationException;
import facets.core.app.FeatureHost.LayoutFeatures;
import facets.core.app.TextView;
import facets.core.app.ViewerContenter;
import facets.core.superficial.FacetedTarget;
import facets.core.superficial.Notice;
import facets.core.superficial.Notifying.Impact;
import facets.core.superficial.SFacet;
import facets.core.superficial.SFrameTarget;
import facets.core.superficial.SIndexing;
import facets.core.superficial.STarget;
import facets.core.superficial.STargeter;
import facets.core.superficial.SToggling;
import facets.core.superficial.app.SAreaTarget;
import facets.core.superficial.app.SContentAreaTargeter;
import facets.core.superficial.app.SHost;
import facets.core.superficial.app.SHost.FacetLayout;
import facets.core.superficial.app.SSelection;
import facets.core.superficial.app.SView;
import facets.core.superficial.app.ViewableFrame;
import facets.core.superficial.app.ViewerTarget;
import facets.facet.AreaFacets;
import facets.facet.FacetFactory;
import facets.facet.FacetMaster.Viewer;
import facets.facet.ViewerAreaMaster;
import facets.facet.app.FacetAppSurface;
import facets.util.Debug;
import facets.util.Util;
import facets.util.app.Disposer;
import facets.util.app.WatchableOperation;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import pdft.block.CosTreeView.TreeStyle;
import pdft.block.PageAvatarPolicies.PageRenderView;
import pdft.block.PageContent.TextStyle;
import facets.util.app.DirCache;
/**
{@link ViewerContenter} for PDF content. 
<p>{@link PdfContenter} can use the following types to represent and
expose PDF content to the user:
<ul>
<li>{@link PdfPages} wraps a {@link COSDocument} as its {@link SFrameTarget#framed}.
<li>{@link CosTreeMaster} displays regions of the {@link COSDocument}
defined by {@link CosTreeView}s.
<li>{@link PageContent} extracts text and code from the current page for display in rich
text panes defined by {@link PageTextView}s and enables 
{@link PageAvatarPolicies} to render pages graphically.  
</ul>
 */
final class PdfContenter extends ViewerContenter{
	static final class CosDisposer extends Disposer<COSDocument>{
		private final List<COSDictionary>pages=new ArrayList();
		CosDisposer(COSDocument cosDoc){
			super(cosDoc);
			for(Object page:new PDDocument(disposable()).getDocumentCatalog().getAllPages())
				pages.add(((PDPage)page).getCOSDictionary());
		}
		public void dispose(){
			try{
				disposable().close();
			}catch(IOException e){
				throw new RuntimeException(e);
			}
			for(COSDictionary cos:pages)cos.clear();
			pages.clear();
			super.dispose();
		}
		List<COSDictionary>pages(){
			return pages;
		}
	}
	static final String ARG_BLOCK="editBlock";
	private final static boolean minimal=false;
	private static int defaults=1;
	private final FacetAppSurface app;
	private final boolean forBlockTexts;
	private PageRenderView renderPage;
	private boolean wasRemoved;
	private PageAvatarPolicies pagePolicies;
	public PdfContenter(Object source,FacetAppSurface app){
		super(source);
		if((this.app=app)==null)throw new IllegalArgumentException(
				"Null app in "+Debug.info(this));
		forBlockTexts=app.spec instanceof pdfBlockTexts;
	}
	@Override
	protected ViewableFrame newContentViewable(final Object source){
		final boolean defaultSource=source instanceof ContentSource;
		final String title=defaultSource?PdfApp.TITLE_DEFAULT+defaults++:((File)source).getName(),
			useTitle=newUseTitle(title);
		final PdfApp pdfApp=(PdfApp)app.spec;
		WatchableOperation op=new WatchableOperation<COSDocument>(
				"PdfContenter.newContentViewable"){
			@Override
			public String[]getBlockingCancelTexts(){
				return newContentCreationTexts(app.title(),source);
			}
			@Override
			public CancelStyle cancelStyle(){
				return defaultSource?false?Dialog:Timeout:
					((File)source).length()/Util.MB<5?Timeout:Dialog;
			}	
			@Override
			public COSDocument doReturnableOperation(){
				return defaultSource?((ContentSource<COSDocument>)source).newContent()
						:pdfApp.newCosDocument((File)source);				
			}
		};
		COSDocument cosDoc=(COSDocument)app.runWatched(op);
		if(cosDoc==null)throw new ContentCreationException(
				"Content creation was interrupted for "+source+".");
		return new PdfPages(title,new CosDisposer(cosDoc),
				false?null:new DirCache(Util.getDir(pdfApp.getAppDir(),
						useTitle.toLowerCase().replaceAll("\\s+","-"))),
						app,forBlockTexts){
			@Override
			public SSelection defineSelection(Object definition){
				SSelection selection=super.defineSelection(definition);
				if(true&&definition instanceof COSDictionary&&renderPage!=null)
					setRenderViewBox(renderPage);
				return selection;
			}
		};
	}
	@Override
	final protected FacetedTarget[]newContentViewers(ViewableFrame viewable){
		PdfPages pages=(PdfPages)viewable;
		SFrameTarget pageTree=new SFrameTarget(false?new TextView("DebugTree")
				:new CosTreeView(TreeStyle.Pages)),
			documentTree=new SFrameTarget(new CosTreeView(TreeStyle.Document)),
			trailerTree=new SFrameTarget(new CosTreeView(TreeStyle.Trailer)),
			renderPage=new SFrameTarget(this.renderPage=new PageRenderView(
					pagePolicies=new PageAvatarPolicies(pages.render,false))),
			renderBlock=new SFrameTarget(new PageRenderView(
					new PageAvatarPolicies(pages.render,true))),
			extracted=new PageTextView(TextStyle.Extracted,app.spec).newFramed(),
			stream=new PageTextView(TextStyle.Stream,app.spec).newFramed();
		pages.setRenderViewBox(this.renderPage);
		if(!forBlockTexts)return newViewerAreas(viewable,
				minimal?new SFrameTarget[]{documentTree}
				:new SFrameTarget[]{pageTree,documentTree,trailerTree,renderPage,extracted,stream}
			);
		SIndexing pageViews=pages.newPageViews(new SFrameTarget[]{renderPage,renderBlock});
		return new SAreaTarget[]{SAreaTarget.newSingleViewerArea(new ActionViewerTarget(
				pageViews.title(),viewable,pageViews){})};
	}
	@Override
	public STarget[]lazyContentAreaElements(SAreaTarget area){
		return minimal?new STarget[]{}:app.ff.areas().panesGetTarget(area).elements();
	}
	@Override
	protected void attachContentAreaFacets(AreaRoot area){
		final FacetFactory ff=app.ff;
		ViewerAreaMaster vam=newViewerAreaMaster(ff);
		AreaFacets areas=ff.areas();
		if(minimal||forBlockTexts)areas.attachViewerAreaPanes(area,
				vam,AreaFacets.PANE_SPLIT_VERTICAL);
		else areas.attachPanes(area,areas.viewerAreaChildren(area,vam),
			new int[][]{{PANE_SPLIT_VERTICAL},
				{PANE_SPLIT_HORIZONTAL,PANE_LOWER,PANE_SPLIT_HORIZONTAL},
				{PANE_SPLIT_VERTICAL,PANE_LEFT,PANE_SPLIT_HORIZONTAL},
			},new double[]{0.33,0.5,0.5,0.33,0.5},
			new int[]{1,0,0,1,1,0,1,1},
			new String[]{"Structure","Page"});
	}
	private static ViewerAreaMaster newViewerAreaMaster(final FacetFactory ff){
		return new ViewerAreaMaster(){
			protected ViewerAreaMaster newChildMaster(SAreaTarget area){
				final SView view=((ViewerTarget)area.activeFaceted()).view();
				final boolean forPage=view instanceof PageRenderView,
					forTree=view instanceof CosTreeView,
					forStream=view instanceof PageTextView
						&&((PageTextView)view).style==PageContent.TextStyle.Stream;
				return new ViewerAreaMaster(){
					public Viewer viewerMaster(){
						return forTree?new CosTreeMaster():null; 
					}
					protected String hintString(){
						return forPage?HINT_BARE:forStream?HINT_PANEL_ABOVE:HINT_NONE;
					}
					protected SFacet newViewTools(STargeter targeter){
						if(!forStream)return null;
						STargeter[]elements=targeter.elements();
						return ff.toolGroups(targeter,HINT_NONE,
				  		ff.textualField(elements[PageTextView.TARGET_LINES],5,HINT_USAGE_FORM),
				  		ff.textualField(elements[PageTextView.TARGET_CHARS],6,HINT_USAGE_FORM),
				  		ff.togglingCheckboxes(elements[PageTextView.TARGET_WRAP],HINT_BARE),
				  		ff.spacerTall(30)
						);
					}
				};
			}
		};
	}
	static String newUseTitle(String title){
		return title.startsWith(PdfApp.TITLE_DEFAULT)?PdfApp.TITLE_DEFAULT
					:title.toLowerCase().replace(".pdf","");
	}
	@Override
	public void areaRetargeted(SContentAreaTargeter root){
		if(minimal||app.findActiveContent()==app.emptyContent)return;
		if(forBlockTexts){
			root.content().elements()[FONTS].target().setLive(false);
			return;
		}
		Object activeView=((SFrameTarget)root.view().target()).framed;
		STargeter[]pane=root.elements(),paneShow=pane[PANE_SHOW].elements(),
			content=root.content().elements();
		boolean extractedOrCodeIsActive=activeView instanceof PageTextView,
			renderIsActive=activeView instanceof PageRenderView,
			extractedOrCodePaneSet=((SToggling)paneShow[4].target()).isSet()
				||((SToggling)paneShow[5].target()).isSet(),
			renderPaneSet=((SToggling)paneShow[3].target()).isSet(),
			noPaneMaximised=pane[PANE_ACTIVE].elements()[PANE_ACTIVE_MAXIMISE].
				target().isLive();
		content[FONTS].target().setLive(
				extractedOrCodeIsActive||(extractedOrCodePaneSet&&noPaneMaximised));
		content[RENDER].target().setLive(
				renderIsActive||(renderPaneSet&&noPaneMaximised));
	}
	@Override
	public LayoutFeatures newContentFeatures(SContentAreaTargeter area){
		return new PdfFeatures(app,area,forBlockTexts);
	}
	@Override
	public void wasRemoved(){
		wasRemoved=true;
		memCheck=false;
		Debug.memCheck("PdfContenter.wasRemoved");
		((Disposer)contentFrame().framed).dispose();
		app.ff.providingCache().clear();
		pagePolicies.dispose();
		FacetFactory.fontIndexing.setNotifiable(null);
		Debug.memCheck("PdfContenter.wasRemoved~");
	}
	@Override
	public void wasAdded(){
	final WatchableOperation op=new WatchableOperation("PdfContenter.wasAdded"){
		@Override
		protected void doSimpleOperation(){
			if(!forBlockTexts)app.ff.areas().panesValidateLayout(
					(SAreaTarget)app.activeContentTargeter().target());
			else if(app.spec.args().getBoolean(ARG_BLOCK)){
				PdfPages pages=(PdfPages)contentFrame();
				pages.blockOpenEdit.fire();
				pages.defineSelection(new TextBlock(pages.blocks.pageBlocks(0).children()[0],true));
				app.notify(new Notice(pages,Impact.DEFAULT));
			}
		}};
		if(false)app.runWatchedLater(op);
		else if(false)SwingUtilities.invokeLater(new Runnable(){public void run(){
			op.doOperations();
		}});
	}
}
