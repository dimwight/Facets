package tones.view.paint;
import facets.core.app.avatar.Painter;
import facets.core.app.avatar.PainterSource;
import facets.util.ItemList;
import facets.util.geom.Line;
import facets.util.geom.Point;
import facets.util.geom.Vector;
import tones.page.PageItem.PageTails;
import tones.view.PageView;
public final class TailPainters extends PagePainters{
	private final Line line;
	private final boolean tailsUp,single;
	private final boolean paintSelected;
	public TailPainters(PageView page,PageTails tails,PainterSource p){
		super(page,p);
		Line geom=tails.geom;
		paintSelected=tails.from.selected;
		tailsUp=tails.from.tone.voice.tailsUp;
		single=tails.single;
		line=new Line(new Point(geom.from.at().plus(tails.from.at).scaled(scaleToPage)),
			new Point(geom.to.at().plus(tails.to.at
					).scaled(scaleToPage).plus(new Vector(0.5,0))));
	}
	@Override
	public Painter[]newViewPainters(boolean selected){
		Vector shift=new Vector(0,0.5*(tailsUp?-1:1));
		Point from=new Point(line.from.at().minus(shift)),
				to=new Point(line.to.at().minus(shift));
		ItemList<Painter>painters=new ItemList(Painter.class);
		boolean debug=false;
		for(int i=0;i<(debug||single?1:8);i++){
			from.shift(shift);to.shift(shift);
			painters.addItem(p.line(new Line(from,to),
					selectionShade(paintSelected,false).brighter(),
					debug?-1:1,false));
		}
		return painters.items();
	}
}