package tones.view;
import static tones.view.PageView.*;
import facets.core.app.PathSelection;
import facets.core.app.SViewer;
import facets.core.app.avatar.AvatarContent;
import facets.core.app.avatar.AvatarPolicies;
import facets.core.app.avatar.AvatarPolicy;
import facets.core.app.avatar.AvatarView;
import facets.core.app.avatar.DragPolicy;
import facets.core.app.avatar.Painter;
import facets.core.app.avatar.PainterSource;
import facets.core.app.avatar.PlaneView;
import facets.core.superficial.app.SSelection;
import facets.util.Debug;
import facets.util.Times;
import facets.util.shade.Shades;
import tones.bar.Bars;
import tones.page.PageBar;
import tones.page.PageItem;
import tones.page.PageItem.PageTails;
import tones.page.PageItem.PageTie;
import tones.page.PageNote;
import tones.page.PageStaves;
import tones.view.paint.BarPainters;
import tones.view.paint.TailPainters;
import tones.view.paint.NotePainters;
import tones.view.paint.PagePainters;
import tones.view.paint.TiePainters;
final class PagePolicies extends AvatarPolicies{
	@Override
	public SSelection newAvatarSelection(SViewer viewer,SSelection viewable){//?
		Bars bars=(Bars)viewable.content();
		boolean timing=false;
		if(timing)Times.printElapsed("PagePolicies.newAvatarSelection");
		PageItem[]items=PageStaves.newPageItems(bars,(PageView)viewer.view());
		if(timing)Times.printElapsed("PagePolicies.newAvatarSelection~");
		return PathSelection.newMinimal(items);
	}
	@Override
	public AvatarPolicy avatarPolicy(SViewer viewer,final AvatarContent content,
			final PainterSource p){
		PageView view=(PageView)viewer.view();
		PageItem item=(PageItem)content;
		final PagePainters painters=
			item instanceof PageNote?
				new NotePainters(view,(PageNote)item,p)
			:item instanceof PageBar?new BarPainters(view,(PageBar)item,p)
			:item instanceof PageTie?new TiePainters(view,(PageTie)item,p)
			:item instanceof PageTails?new TailPainters(view,(PageTails)item,p)
					:null;
		if(painters==null)
			throw new IllegalStateException("Null painters for "+Debug.info(item));
		else return new AvatarPolicy(){
			public Painter[]newViewPainters(boolean selected,boolean active){
				return painters.newViewPainters(selected);
			}
			public Painter[]newPickPainters(Object hit,boolean selected){
				return painters.newPickPainters();
			}
		};
	}
	@Override
	public DragPolicy dragPolicy(AvatarView view,AvatarContent[]content,
			Object hit,PainterSource p){
		return null;
	}
	@Override
	public Painter getBackgroundPainter(SViewer viewer, 
			PainterSource p){
		PlaneView plane=(PlaneView)viewer.view();
		double margin=INSET*0.75;
		return false?Painter.EMPTY:p.bar(
				-margin,-margin,plane.showWidth()-2*(INSET-margin),
				plane.showHeight()-2*(INSET-margin),Shades.white,false
			);
	}
}
