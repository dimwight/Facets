package pdft.block;
import static facets.core.app.TextView.*;
import static pdft.block.pdfInspect.*;
import facets.core.app.HtmlView;
import facets.core.app.TextView;
import facets.core.app.avatar.AvatarView;
import facets.core.superficial.SFrameTarget;
import facets.core.superficial.SIndexing;
import facets.core.superficial.SNumeric;
import facets.core.superficial.STarget;
import facets.core.superficial.STargeter;
import facets.core.superficial.STextual;
import facets.core.superficial.STrigger;
import facets.core.superficial.TargetCore;
import facets.core.superficial.TargeterCore;
import facets.core.superficial.app.SSelection;
import facets.core.superficial.app.SView;
import facets.core.superficial.app.SViewer;
import facets.core.superficial.app.SelectingFrame;
import facets.core.superficial.app.SelectionView;
import facets.core.superficial.app.ViewableFrame;
import facets.facet.FacetFactory;
import facets.facet.app.FacetAppSurface;
import facets.util.Debug;
import facets.util.NumberPolicy;
import facets.util.Times;
import facets.util.Titled;
import facets.util.app.DirCache;
import facets.util.app.Disposer;
import facets.util.app.ProvidingCache.ItemProvider;
import facets.util.tree.ValueNode;
import java.util.List;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import pdft.block.PageAvatarPolicies.PageRenderView;
import pdft.block.PdfContenter.CosDisposer;
import pdft.block.TextBlock.Blocks;
/**
{@link ViewableFrame} that wraps a {@link COSDocument} as its {@link SFrameTarget#framed}. 
<p>The {@link SelectingFrame#selection()} is always a {@link COSDictionary} from the 
pages dictionary. 
 */
class PdfPages extends ViewableFrame{
	static final int GO_TO_PAGE=0,PAGE_COUNT=1,FONTS=2,RENDER=3,BLOCK_SELECTED=4,
		BLOCK_EDITING=5;
	static final int RENDER_TEXT=0,RENDER_GRAPHICS=1,RENDER_FINE=2;
	final Blocks blocks;
	final SIndexing render;
	private final FacetAppSurface app;
	private final int pageCount;
	private final SNumeric goToPage;
	private int viewPageAt;
	private final STrigger.Coupler blockTriggers=new STrigger.Coupler(){
		@Override
		public void fired(STrigger t){
			pageViews.setIndex(t==blockOpenEdit?1:0);
			if(t==blockDelete)blocks.deleteBlock((TextBlock)selectionFrame().framed,
					viewPageAt);
		}
	};
	final STrigger blockOpenEdit=new STrigger("Edit Block",blockTriggers),
		blockCloseEdit=new STrigger("E&xit Edit",blockTriggers),
		blockDelete=new STrigger("Delete Block",blockTriggers);
	private final DirCache disk;
	private SIndexing pageViews;
	SIndexing newPageViews(SFrameTarget[]views){
		return pageViews=new SIndexing("Page Views",views,
		0,new SIndexing.Coupler(){
			@Override
			public void indexSet(SIndexing i){
				setRenderViewBox((PageRenderView)
						((SFrameTarget)pageViews.indexed()).framed);
			}
		});
	}
	@Override
	protected STarget[]lazyElements(){
		return new STarget[]{
				goToPage,
				new STextual("pageCount"," / "+pageCount,new STextual.Coupler()),
				FacetFactory.fontIndexing,
				render,
				new TargetCore("Text Block Selected",blockOpenEdit,blockDelete),
				new TargetCore("Text Block Open",blockCloseEdit)
			};
	}
	PdfPages(String title,CosDisposer dispose,DirCache disk,FacetAppSurface app,
			boolean forBlockTexts){
		super(title,dispose);
		this.disk=disk;
		this.app=app;
		this.blocks=!forBlockTexts?null:new Blocks(app.spec,title);
		final ValueNode args=app.spec.args();
		render=new SIndexing("Page Render",
				new Object[]{"Text Only","Text and &Graphics","Fine Graphics"},
				args.getOrPutBoolean(ARG_GRAPHICS,false)?RENDER_GRAPHICS:RENDER_TEXT,
			new SIndexing.Coupler(){
				@Override
				public void indexSet(SIndexing i){
					args.put(ARG_GRAPHICS,i.index()>RENDER_TEXT);
				}
			}
		);
		List<COSDictionary>cosPages=cosPages();
		pageCount=cosPages.size();
		int pageAt=true||!title.startsWith("Default")?0
				:Integer.valueOf(title.replaceAll("[^\\d]+",""));
		defineSelection(cosPages.get(pageAt));
		goToPage=new SNumeric("Page",pageAt,new SNumeric.Coupler(){
			@Override
			public void valueSet(SNumeric n){
				COSDictionary pageNow=cosPages().get((int)n.value()-1);
				if(pageNow!=selection().single())defineSelection(pageNow);
			}
			@Override
			public NumberPolicy policy(SNumeric n){
				return new NumberPolicy(1,pageCount){
					@Override
					public String[]incrementTitles(){
						return new String[]{PAGE_PREVIOUS,PAGE_NEXT};
					}
				};
			}
		});
	}
	private List<COSDictionary>cosPages(){
		return((CosDisposer)framed).pages();
	}
	void setRenderViewBox(PageRenderView view){
		view.setViewBox(getPageContent().drawBox);
	}
	@Override
	public SSelection defineSelection(final Object definition){
		if(definition instanceof SView &&(pageViews!=null&&pageViews.index()!=0))
			return selection();
		if(definition instanceof TextBlock){
			return setSelection(new SSelection(){
				@Override
				public Object content(){
					return framed;
				}
				@Override
				public Object single(){
					return definition;
				}
				@Override
				public Object[]multiple(){
					throw new RuntimeException("Not implemented in "+Debug.info(this));
				}
			});
		}
		if(definition instanceof SView)getPageContent().clearSelection();
		final COSDictionary cos=definition instanceof COSDictionary?
			(COSDictionary)definition:cosPages().get((int)goToPage.value()-1);
		SSelection selection=setSelection(new SSelection(){
			@Override
			public Object content(){
				return framed;
			}
			@Override
			public Object single(){
				return cos;
			}
			@Override
			public Object[]multiple(){
				throw new RuntimeException("Not implemented in "+Debug.info(this));
			}
		});
		if(goToPage!=null)goToPage.setValue(cosPages().indexOf(cos)+1);
		if(pageViews!=null)pageViews.setIndex(0);
		return selection;
	}
	public SFrameTarget selectionFrame(){
		final Object selected=selection().single();
		String title=selected instanceof Titled?((Titled)selected).title()
				:Debug.info(selected);
		return new SFrameTarget(title,selected){
			@Override
			public STargeter newTargeter(){
				return new TargeterCore((selected instanceof COSDictionary?new Object(){}
					:selected).getClass());
			}
		};
	}
	@Override
	protected SSelection newViewerSelection(SViewer viewer){
		SelectionView view=(SelectionView)viewer.view();
		final SSelection selection=selection();
		if(view instanceof CosTreeView)return view.newViewerSelection(viewer,selection);
		else if(view instanceof TextView&&!(view instanceof HtmlView))return new SSelection(){
			@Override
			public Object[]multiple(){
				throw new RuntimeException("Not implemented in "+Debug.info(this));
			}
			@Override
			public Object single(){
				return selection.single().toString().replaceAll(",","\n");
			}
			@Override
			public Object content(){
				throw new RuntimeException("Not implemented in "+Debug.info(this));
			}
		};
		else if(false)throw new RuntimeException("Page content disabled in "+Debug.info(this));
		final int pageAt=(int)goToPage.value()-1;
		if(viewPageAt!=pageAt&&view instanceof AvatarView){
			if(render.index()==RENDER_FINE)render.setIndex(RENDER_GRAPHICS);
			view.updateStateStamp();
			viewPageAt=pageAt;
		}
		PageContent pc=getPageContent();
		return pc.newViewSelection(view,selection.single());
	}
	private final PageContent getPageContent(){
		if(false)throw new RuntimeException("Page content disabled in "+Debug.info(this));
		else if(false)Times.printElapsed("PdfPages.getPageContent");
		final int pageAt=(int)(goToPage.value()-1);
		PageContent pc=new ItemProvider<PageContent>(app.ff.providingCache(),
				((Disposer)framed).disposable(),"PdfPages.getPageContent"){
			@Override
			protected PageContent newItem(){
				PageContent pc=new PageContent(((CosDisposer)framed),cosPages().get(pageAt),pageAt,
						app,disk,blocks);
				pc.inBlockEdit=pageViews!=null&&pageViews.index()!=0;
				return pc;
			}
			@Override
			protected long buildByteCount(){
				return 0;
			}
			@Override
			protected long finalByteCount(PageContent item){
				return item.estimateSize();
			}
			protected boolean passThrough(){
				return false;
			}
		}.getForValues(pageAt);
		if(false)Times.printElapsed("PdfPages.getPageContent~ pc="+pc);
		if(pc==null)throw new IllegalStateException("Null page in "+Debug.info(this));
		else return pc;
	}
	@Override
	protected void viewerSelectionChanged(SViewer viewer,SSelection selection){
		defineSelection(selection.single());
	}
	@Override
	protected void viewerSelectionEdited(SViewer viewer,Object edit,
			boolean interim){
		if(pageViews==null)return;
		TextBlock block=(TextBlock)((Object[])edit)[0];
		defineSelection(block);
		blockOpenEdit.fire();
	}
	@Override
	public void setFramedState(Object stateSpec,boolean interim){}
}