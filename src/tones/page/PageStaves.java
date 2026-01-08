package tones.page;
import static tones.view.PageView.*;
import facets.util.ItemList;
import facets.util.Objects;
import facets.util.Util;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import tones.Mark;
import tones.Mark.Tails;
import tones.Mark.Tie;
import tones.Tone;
import tones.Voice;
import tones.bar.Bar;
import tones.bar.Bars;
import tones.page.PageItem.PageTails;
import tones.page.PageItem.PageTie;
import tones.view.PageView;
public final class PageStaves{
	static final double PAGE_X_SCALE=1.5;
	final List<Bar>thisBars=new ArrayList();
	private final Voice selectedVoice;
	double rise=0,staveGap=0,fall=0,pageXUsed=0;
	final Bar endBar;
	PageStaves(Iterator<Bar>bars,Bar bar,final double useWidth,Voice selectedVoice){
		this.selectedVoice=selectedVoice;
		while(bars.hasNext()||bar!=null){
			if(bar==null)bar=bars.next();
			if(bar==null)throw new IllegalStateException(
					"Null bar in "+this);
			double barWidth=bar.width;
			if(pageXUsed+barWidth>useWidth)break;
			pageXUsed+=barWidth;
			rise=Math.max(rise,bar.rise);
			staveGap=Math.max(staveGap,bar.staveGap);
			fall=Math.max(fall,bar.fall);
			thisBars.add(bar);
			bar=null;
		}
		endBar=bar;
	}
	PageItem[]newItems(double pageY,double pageXScale){
		double pageX=0;
		ItemList<PageItem>items=new ItemList(PageItem.class);
		for(Bar bar:thisBars){
			PageBar pageBar=new PageBar(bar,pageX,pageY,staveGap,pageXScale,selectedVoice);
			items.addItems(pageBar.newItems());
			pageX+=pageBar.staveWidth;
		}
		List<PageItem>all=new ArrayList(items);
		all.removeIf(item->!(item instanceof PageNote));
		PageNote[]notes=Objects.newTyped(PageNote.class,all.toArray());
		for(PageNote note:notes){
			Collection<Mark>marks=note.tone.marks;
			if(marks.isEmpty())continue;
			if(false&&note.tone.barAt==6&&note.tone.voice==Voice.Alto)
				note.trace(".newItems: marks=",note);
			for(Mark mark:marks)
				if(mark instanceof Tie)items.add(newPageTie((Tie)mark,note,notes));
				else if(mark instanceof Tails)items.add(newPageTails((Tails)mark,notes));
		}
		return items.items();
	}
	private PageItem newPageTie(Tie tie,PageNote tied,PageNote[]staveNotes){
		boolean isBefore=tie.before==tied.tone;
		PageNote other=null;
		for(PageNote check:staveNotes)
			if(check==tied)continue;
			else if(isBefore?check.tone==tie.after:check.tone==tie.before){
				other=check;
				break;
			}
		return new PageTie(isBefore?tied:other,isBefore?other:tied,tied.bar,
				selectedVoice==tied.tone.voice);
	}
	private PageItem newPageTails(Tails tails,PageNote[]staveNotes){
		ItemList<PageNote>beamed=new ItemList(PageNote.class);
		boolean selected=false;
		for(Tone tone:tails.tones)
			for(PageNote check:staveNotes)
				if(check.tone==tone){
					selected=selectedVoice==tone.voice;
					beamed.add(check);
				}
		return new PageTails(beamed.items(),selected);
	}
	public static PageItem[]newPageItems(Bars content,PageView page){
		int barStart=page.barStart(),lastBarAt=barStart+1;
		Iterator<Bar>bars=content.barsFrom(barStart).iterator();
		final double pageWidth=page.showWidth()-2*INSET,
			useHeight=page.showHeight()-1.5*INSET,
			unitY=page.unitY(),
			unitX=unitY*page.widthForPitch();
		double pageY=0,pageXScale=PAGE_X_SCALE;
		ItemList<PageItem>items=new ItemList(PageItem.class);
		Bar bar=null;
		while(bars.hasNext()||bar!=null){
			PageStaves block=new PageStaves(bars,
					bar,
					pageWidth/unitX,Voice.Empty

			);
			bar=block.endBar;
			double blockHeight=PageItem.STAVE_GRID*2+block.staveGap+block.fall;
			if(((pageY+=block.rise)+blockHeight)*unitY>useHeight){
				break;
			}
			double scaleUpdate=pageWidth/(block.pageXUsed*unitX);
			pageXScale=bars.hasNext()?scaleUpdate:Math.min(scaleUpdate,pageXScale);
			List<Bar>blockBars=block.thisBars;
			if(!blockBars.isEmpty()){
				lastBarAt=blockBars.get(blockBars.size()-1).at;
			}
			PageItem[] pageItems = block.newItems(pageY, pageXScale);
			items.addItems(pageItems);
			pageY+=blockHeight;
		}
		page.setBarStop(lastBarAt+1);
		return items.items();
	}
}
