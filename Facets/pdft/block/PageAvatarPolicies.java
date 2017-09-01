package pdft.block;
import static facets.core.superficial.app.SViewer.*;
import static facets.util.shade.Shades.*;
import facets.core.app.avatar.AvatarContent;
import facets.core.app.avatar.AvatarContent.State;
import facets.core.app.avatar.AvatarPolicies;
import facets.core.app.avatar.AvatarPolicy;
import facets.core.app.avatar.AvatarView;
import facets.core.app.avatar.DragPolicy;
import facets.core.app.avatar.Painter;
import facets.core.app.avatar.PainterSource;
import facets.core.app.avatar.PlaneView;
import facets.core.app.avatar.PlaneViewWorks;
import facets.core.superficial.SIndexing;
import facets.core.superficial.app.SSelection;
import facets.core.superficial.app.SViewer;
import facets.util.Debug;
import facets.util.app.Disposer;
import facets.util.geom.Point;
import facets.util.geom.Vector;
import facets.util.shade.Shades;
import java.awt.Graphics;
import java.util.Arrays;
import applicable.TextAvatar;
import applicable.TextAvatar.BoundsBox;
/**
{@link AvatarPolicies} that uses {@link PageContent} and {@link PagePainters} 
to render the current page graphically. 
 */
final class PageAvatarPolicies extends AvatarPolicies{
	private static final double MARGINS=40;
	/**
	{@link PlaneView} that uses {@link PageAvatarPolicies} to depict its content. 
	<p>Trivial subclass of {@link PlaneViewWorks} apart from 
	{@link #setViewBox(SSelection)}. 
	 */
	final static class PageRenderView extends PlaneViewWorks{
		PageRenderView(PageAvatarPolicies policies){
			super("Re&ndering",0,0,new Vector(0,0),policies);
		}
		@Override
		public Object backgroundStyle(){
			return Shades.gray;
		}
		void setViewBox(BoundsBox box){
			double across=box.width,down=box.height;
			setShowValues(across+MARGINS,down+MARGINS,new Vector(MARGINS,MARGINS).scaled(0.5),1);
		}
	}
	final boolean forBlockEdit;
	PageContent page;
	Disposer<Painter>pageDisposer;
	private int pickCursor;
	private final SIndexing render;
	PageAvatarPolicies(SIndexing render,boolean forBlockEdit){
		this.render=render;
		this.forBlockEdit=forBlockEdit;
	}
	@Override
	public Painter getBackgroundPainter(SViewer viewer,final PainterSource p){
		final PlaneView view=(PlaneView)viewer.view();
		final int renderAt=render.index();
		pageDisposer=new Disposer(page.pagePainters.newPainter(renderAt));
		return new Painter(){
			@Override
			public void paintInGraphics(Object graphics){
				Graphics g=(Graphics)graphics;
				p.bar(0,0,view.showWidth()-MARGINS,view.showHeight()-MARGINS,
						forBlockEdit?gray.brighter():white,false
					).paintInGraphics(graphics);
				pageDisposer.disposable().paintInGraphics(g.create());
			}
			@Override
			public int hashCode(){
				return false&&(PagePainters.timePaint||PagePainters.timeImage)?super.hashCode()
						:pageDisposer.disposable().hashCode()==Integer.MAX_VALUE?(int)System.currentTimeMillis()
						:Arrays.hashCode(new Object[]{page.pageAt,renderAt});
			}
		};
	}
	/**
	Called from {@link PdfPages#newViewerSelection(SViewer)}. 
	 */
	void pageSelected(PageContent page){
		if(page==null)throw new IllegalStateException(
				"Null page in "+Debug.info(this));
		if(this.page!=page)page.clearSelection();
		this.page=page;
	}
	@Override
	public AvatarPolicy avatarPolicy(SViewer viewer,final AvatarContent content,
			final PainterSource p){
		if(content instanceof TextBlock)((TextBlock)content).p=p;
		return new AvatarPolicy(){
			@Override
			public Painter[]newViewPainters(boolean selected,boolean active){
				return new Painter[]{((TextAvatar)content).newViewPainter(selected)};
			}
			@Override
			public Painter[]newPickPainters(Object hit,boolean selected){
				pickCursor=hit==page?CURSOR_CROSSHAIR
						:hit instanceof TextBlock?CURSOR_DEFAULT:CURSOR_TEXT;
				TextAvatar avatar=(TextAvatar)hit;
				BoundsBox bounds=avatar.getBounds();
				return false?new Painter[]{p.pointMark(bounds.getCenter(),
							hit instanceof TextBlock?cyan:hit instanceof PageChar?blue:red,false
					)}
					:avatar.newPickPainters(selected);
			}
			@Override
			public Object stateCursor(State state){
				return state==Painter.Style.PickedSelected?pickCursor:
					CURSOR_DEFAULT;
			}
		};
	}
	@Override
	public DragPolicy dragPolicy(AvatarView view,final AvatarContent[]content,
			final Object hit,final PainterSource p){
		page.clearSelection();
		return new DragPolicy(){
			final boolean blockDefine=pickCursor==CURSOR_CROSSHAIR;
			@Override
			public Object stateCursor(State state){
				return pickCursor;
			}
			public Painter[]newDragPainters(Point anchorAt,Point dragAt){
				return page.newDragPainters(anchorAt,dragAt,blockDefine?p:null);
			}
			public Object[]newDragDropEdits(Point anchorAt,Point dragAt){
				return blockDefine?new Object[]{page.newDraggedBlock(anchorAt,dragAt)}
					:content;
			}
		};
	}
	void dispose(){
		if(pageDisposer!=null)pageDisposer.dispose();
		page=null;
	}
}