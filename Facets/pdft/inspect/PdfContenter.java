package pdft.inspect;
import static facets.core.app.ActionAppSurface.CachingStyle.*;
import static facets.core.app.ActionViewerTarget.*;
import static facets.core.app.AppConstants.*;
import static facets.core.app.AppSurface.ContentStyle.*;
import static facets.core.app.TextView.*;
import static facets.facet.AreaFacets.*;
import static facets.facet.FacetFactory.*;
import static facets.util.Debug.*;
import static facets.util.tree.Nodes.*;
import static pdft.inspect.PdfPages.*;
import facets.core.app.ActionAppSurface;
import facets.core.app.AppSurface;
import facets.core.app.AreaRoot;
import facets.core.app.FeatureHost.LayoutFeatures;
import facets.core.app.PagedContenter;
import facets.core.app.SurfaceServices;
import facets.core.app.ViewerContenter;
import facets.core.superficial.FacetedTarget;
import facets.core.superficial.SFacet;
import facets.core.superficial.SFrameTarget;
import facets.core.superficial.STarget;
import facets.core.superficial.STargeter;
import facets.core.superficial.SToggling;
import facets.core.superficial.TargetCore;
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
import facets.facet.app.FacetPreferences;
import facets.util.Debug;
import facets.util.FileSpecifier;
import facets.util.Util;
import facets.util.app.WatchableOperation;
import java.io.File;
import java.io.IOException;
import javax.swing.SwingUtilities;
import org.apache.pdfbox.cos.COSDocument;
import pdft.PdfCore;
import pdft.inspect.PageAvatarPolicies.PageRenderView;
final class PdfContenter extends ViewerContenter{
	public static final String ARG_MARK="mark",ARG_RENDER="renderGraphics",
		ARG_WRAP="wrapCode";
	private static final String NAME_DEFAULT="Default";
	static final String TITLE_REOPEN="Re-open file?";
	final static FileSpecifier pdfFiles=new FileSpecifier("pdf",
			"PDF Portable Document Format");
	private static int defaults=1;
	private final PageAvatarPolicies pagePolicies;
	private final FacetAppSurface app;
	private PageRenderView pageView;
	private boolean wasRemoved;
	PdfContenter(Object source,FacetAppSurface app,PageAvatarPolicies pagePolicies){
		super(source);
		if((this.app=app)==null)throw new IllegalArgumentException(
				"Null app in "+Debug.info(this));
		else if((this.pagePolicies=pagePolicies)==null)throw new IllegalArgumentException(
				"Null avatars in "+Debug.info(this));
	}
	@Override
	protected ViewableFrame newContentViewable(final Object source){
		final boolean defaultSource=source instanceof COSDocument;
		WatchableOperation op=new WatchableOperation("pdfInspect.newContentViewable"){
			@Override
			protected String[]newContentCreationTexts(String appTitle,Object source){
				return new String[]{
		"Creating Content",
		"Please wait while "+appTitle+" creates content from " +sourceAsFile(source)+".",
		"Cancel Requested",
		"If you cancel this operation, $appTitle may slow down or close without warning.<br>"+
		"Wait for the operation to complete?",
				};
			}
			@Override
			public CancelStyle cancelStyle(){
				long mb=sourceAsFile(source).length()/Util.MB;
				return mb<5?CancelStyle.Timeout:CancelStyle.Dialog;
			}	
			@Override
			public Object doReturnableOperation(){
				return newCosDocument(sourceAsFile(source));				
			}
			private File sourceAsFile(final Object source){
				File pdf=(File)source;
				return pdf;
			}};
		COSDocument cosDoc=defaultSource?(COSDocument)source
				:(COSDocument)(app.watcher==null?op.doOperations()
						:app.watcher.runWatched(op));
		if(cosDoc==null)throw new AppSurface.ContentCreationException(
				"Content creation was interrupted for "+source+".");
		String title=defaultSource?NAME_DEFAULT+defaults++:
				((File)source).getName(),
				toMark=app.spec.nature().getString(ARG_MARK);
		if(!toMark.equals(""))Texts.markDocPages(cosDoc,toMark);
		return new PdfPages(title,cosDoc,app){
			@Override
			public SSelection defineSelection(Object definition){
				SSelection selection=super.defineSelection(definition);
				if(pageView!=null)setPageViewToRotation(pageView);
				return selection;
			}
			@Override
			protected STarget[]lazyElements(){
				return TargetCore.join(super.lazyElements(),new STarget[]{pagePolicies.views});
			}
		};
	}
	@Override
	protected FacetedTarget[]newContentViewers(ViewableFrame viewable){
		SFrameTarget pages=new SFrameTarget(new CosTreeView(CosTreeView.TreeStyle.Pages)),
			document=new SFrameTarget(new CosTreeView(CosTreeView.TreeStyle.Document)),
			trailer=new SFrameTarget(new CosTreeView(CosTreeView.TreeStyle.Trailer)),
			page=pagePolicies.newFramedView(viewable),
			extracted=new PageTextView(Texts.TextStyle.Extracted,app.spec).newFramed(),
			stream=new PageTextView(Texts.TextStyle.Stream,app.spec).newFramed();
		((PdfPages)viewable).setPageViewToRotation(pageView=(PageRenderView)page.framed);
		return newViewerAreas(viewable,
				new SFrameTarget[]{pages,document,trailer,page,extracted,stream});
	}
	
	@Override
	protected void attachContentAreaFacets(AreaRoot area){
		final FacetFactory ff=app.ff;
		ViewerAreaMaster vam=newViewerAreaMaster(ff);
		AreaFacets areas=ff.areas();
		areas.attachPanes(area,areas.viewerAreaChildren(area,vam),
			new int[][]{{PANE_SPLIT_VERTICAL},
				{PANE_SPLIT_HORIZONTAL,PANE_LOWER,PANE_SPLIT_HORIZONTAL},
			{PANE_SPLIT_VERTICAL,PANE_LEFT,PANE_SPLIT_HORIZONTAL},
		},
			new double[]{0.33,0.5,0.5,0.33,0.5},
			new int[]{1,0,0,1,1,0,1,1},
			new String[]{"Structure","P&age"});
	}
	private static ViewerAreaMaster newViewerAreaMaster(final FacetFactory ff){
		return new ViewerAreaMaster(){
			protected ViewerAreaMaster newChildMaster(SAreaTarget area){
				final SView view=((ViewerTarget)area.activeFaceted()).view();
				final boolean forPage=view instanceof PageRenderView,
					forTree=view instanceof CosTreeView,
					forStream=view instanceof PageTextView
						&&((PageTextView)view).style==Texts.TextStyle.Stream;
				return new ViewerAreaMaster(){
					public Viewer viewerMaster(){
						return forTree?new CosTreeMaster():null; 
					}
					protected String hintString(){
						return forPage?HINT_BARE:forStream?HINT_PANEL_ABOVE:HINT_NONE;
					}
					protected SFacet newViewerTools(STargeter targeter){
						if(!forStream)return null;
						STargeter[]elements=targeter.elements();
						return ff.toolGroups(targeter,HINT_NONE,
					  		ff.textualField(elements[PageTextView.TARGET_COUNT],5,HINT_USAGE_FORM),
					  		ff.togglingCheckboxes(elements[PageTextView.TARGET_WRAP],HINT_BARE),
					  		true?null:ff.togglingButtons(elements[PageTextView.TARGET_WRAP],HINT_BARE),
					  		ff.spacerTall(30)
							);
					}
				};
			}
		};
	}
	@Override
	public STarget[]lazyContentAreaElements(SAreaTarget area){
		return app.ff.areas().panesGetTarget(area).elements();
	}
	@Override
	public void areaRetargeted(SContentAreaTargeter root){
		if(wasRemoved||app.findActiveContent()==app.emptyContent)return;
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
		content[COS_FONTS].target().setLive(
				extractedOrCodeIsActive||(extractedOrCodePaneSet&&noPaneMaximised));
		content[COS_LAST+1].target().setLive(
				renderIsActive||(renderPaneSet&&noPaneMaximised));
	}
	@Override
	public LayoutFeatures newContentFeatures(SContentAreaTargeter area){
		return new PdfFeatures(app,area);
	}
	@Override
	public void wasAdded(){
	final WatchableOperation op=new WatchableOperation("pdfInspect.wasAdded"){
		@Override
		public void doSimpleOperation(){
			app.ff.areas().panesValidateLayout(
					(SAreaTarget)app.activeContentTargeter().target());
		}};
		if(false)app.runWatchedLater(op);
		else SwingUtilities.invokeLater(new Runnable(){public void run(){
			op.doOperations();
		}});
	}
	@Override
	public void wasRemoved(){
		wasRemoved=true;
		app.ff.providingCache().clear();
		try{
			((COSDocument)contentFrame().framed).close();
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}
	static COSDocument newCosDocument(File pdf){
		try{
			return new PdfCore(pdf).document.getDocument();
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}
}