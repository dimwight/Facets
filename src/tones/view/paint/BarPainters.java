package tones.view.paint;
import static tones.page.PageItem.*;
import facets.core.app.avatar.Painter;
import facets.core.app.avatar.PainterSource;
import facets.util.ItemList;
import facets.util.geom.Line;
import facets.util.shade.Shades;
import tones.Clef;
import tones.page.PageBar;
import tones.view.PageView;
public final class BarPainters extends PagePainters{
	private final int at;
	private final double rise,height,width,x,trebleY,bassY;
	public BarPainters(PageView page,PageBar bar,PainterSource p){
		super(page,p);
		at=bar.content.at;
		x=bar.pageX*unitX;
		trebleY=bar.pageYs[Clef.TREBLE.staveAt]*unitY;
		bassY=bar.pageYs[Clef.BASS.staveAt]*unitY;
		width=bar.content.width*bar.pageXScale*unitX;
		rise=bar.content.rise;
		height=(rise+STAVE_GRID*2+bar.staveGap+bar.content.fall)*unitY;
		NotePainters.firstInBar=true;
	}
	public Painter[]newViewPainters(boolean selected){
		ItemList<Painter>painters=new ItemList(Painter.class);
		if(true)painters.addItem(unscaledText(at+1+"",x+5,bassY-rise*unitY-1,.7));
		painters.addItems(staveLinePainters(x,trebleY,width,STAVE_GRID/2));
		painters.addItems(staveLinePainters(x,bassY,width,STAVE_GRID/2));
		painters.addItem(barLine());
		if(false)painters.addItem(p.rectangle(x,trebleY-rise*unitY,width,height,
				selected?Shades.blue:Shades.red));
		return painters.items();
}
	private Painter barLine(){
		Line line=new Line(new double[]{x+width,trebleY,x+width,bassY+8*unitY});
		return false?p.backgroundLines(new Line[]{line},Shades.gray)
				:p.line(line,Shades.lightGray,
					false?PainterSource.HAIRLINE:false?(int)unitX:1,false);
	}
	public Painter[]newPickPainters(){
		return new Painter[]{p.rectangle(x,trebleY-rise*unitY,width,height,Shades.green)};
	}
}
