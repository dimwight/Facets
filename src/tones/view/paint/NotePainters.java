package tones.view.paint;
import static facets.util.Util.*;
import static tones.Tone.*;
import facets.core.app.avatar.Painter;
import facets.core.app.avatar.PainterSource;
import facets.util.ItemList;
import facets.util.Objects;
import facets.util.Util;
import facets.util.geom.Line;
import facets.util.geom.Point;
import facets.util.shade.Shade;
import facets.util.shade.Shades;
import java.util.Collection;
import applicable.SvgPath;
import tones.ScaleNote;
import tones.Tone.Dissonance;
import tones.bar.Bar;
import tones.page.PageNote;
import tones.page.PageNote.Dot;
import tones.view.PageView;
public final class NotePainters extends PagePainters{
	private static final SvgPath Empty=new SvgPath("Empty","",0),
		Double=new SvgPath("Double","M68.6174 -39.5421c15.8781,-10.4427 44.6966,-4.21473 59.6674,15.5225 14.9707,19.7372 15.6271,54.3268 -0.118545,64.4808 -15.7457,10.1541 -45.0672,3.33459 -59.103,-15.5223 -14.0357,-18.8571 -16.3239,-54.0384 -0.445739,-64.4814zm-78.1195 -13.6518c-4.20299,0.0 -7.64183,3.43882 -7.64183,7.64183l0.0 92.9195c0.0,4.20299 3.43882,7.64183 7.64183,7.64183l5.15872 0.0c4.20299,0.0 7.64183,-3.43882 7.64183,-7.64183l0.0 -33.1629c12.9095,31.341 68.3402,40.1608 95.26,40.2947 26.1969,-0.130281 79.4014,-8.48277 94.1375,-37.8144l0.0 30.6826c0.0,4.20299 3.43882,7.64183 7.64183,7.64183l5.15872 0.0c4.20299,0.0 7.64183,-3.43882 7.64183,-7.64183l0.0 -92.9195c0.0,-4.20299 -3.43882,-7.64183 -7.64183,-7.64183l-5.15872 0.0c-4.20299,0.0 -7.64183,3.43882 -7.64183,7.64183l0.0 29.6698c-14.7358,-29.3316 -67.9403,-37.6841 -94.1375,-37.8144 -26.92,0.133861 -82.3508,8.95377 -95.26,40.2947l0.0 -32.1501c0.0,-4.20299 -3.43882,-7.64183 -7.64183,-7.64183l-5.15872 0.0z",2),
		Whole=new SvgPath("Whole","M68.0558 -39.2871c15.8781,-10.4427 44.6966,-4.21473 59.6674,15.5225 14.9707,19.7372 15.6271,54.3268 -0.118545,64.4808 -15.7457,10.1541 -45.0672,3.33459 -59.103,-15.5223 -14.0357,-18.8571 -16.3239,-54.0384 -0.445739,-64.4814zm29.9413 -14.1546c-30.5183,0.151762 -97.6918,11.4603 -97.9974,54.0981 0.305712,42.6378 67.4793,53.9463 97.9974,54.0981 30.5183,-0.151762 97.6918,-11.4603 97.9974,-54.0981 -0.305712,-42.6378 -67.4793,-53.9463 -97.9974,-54.0981z",2),
		Half=new SvgPath("Half","M14.6974 36.484c-15.2398,-18.0589 16.8303,-35.9157 42.4361,-53.1108 25.6056,-17.1948 51.3167,-37.712 65.4342,-19.7368 14.1177,17.975 -18.9201,37.3933 -42.7381,53.3737 -23.8179,15.9806 -49.8925,37.5326 -65.1326,19.4739zm118.465 -79.0158c-12.2722,-17.7904 -72.8097,-12.6657 -100.238,5.83558 -27.4291,18.5015 -40.9083,62.2032 -27.9907,80.9466 12.9177,18.7433 73.9979,10.3481 99.6181,-7.62012 25.6199,-17.9684 40.8839,-61.3721 28.6114,-79.162z",2),
		Solid=new SvgPath("Solid","M4.81178 38.6605c-14.2225,-21.887 4.69011,-59.1499 34.232,-78.2229 29.5417,-19.0731 71.8603,-18.839 85.3504,2.44987 13.4897,21.2889 -6.04603,60.3632 -34.2318,78.2229 -28.1858,17.8598 -71.1274,19.4369 -85.3504,-2.45007z",2),
		DotBelow=new SvgPath("DotBelow","M203.764 28.579c12.1479,0.0 21.9993,9.85141 21.9993,21.9993 0.0,12.1479 -9.85141,21.9993 -21.9993,21.9993 -12.1479,0.0 -21.9993,-9.85141 -21.9993,-21.9993 0.0,-12.1479 9.85141,-21.9993 21.9993,-21.9993z",2),
		DotLevel=new SvgPath("DotLevel","M203.764 -21.0603c12.1479,0.0 21.9993,9.85141 21.9993,21.9993 0.0,12.1479 -9.85141,21.9993 -21.9993,21.9993 -12.1479,0.0 -21.9993,-9.85141 -21.9993,-21.9993 0.0,-12.1479 9.85141,-21.9993 21.9993,-21.9993z",2);
	private final double x,y,width,height;
	static boolean firstInBar;
	private final PageNote note;
	private final Line tail;
	private final Shade shade;
	private final Collection<Dissonance>dissonances;
	public NotePainters(PageView page,PageNote note,PainterSource p){
		super(page,p);
		this.note=note;
		width=Bar.WIDTH_NOTE*unitX;
		x=note.pageX*unitX;
		y=(note.pageY-1)*unitY;
		height=unitY*2;
		tail=note.tail;
		dissonances=note.incipit.content.getDissonances(note.tone);
		boolean selected=note.selected;
		shade=selectionShade(selected,false&&
				!dissonances.isEmpty());
		if(false&&firstInBar)Util.printOut("NotePainters: ",note+", x="+fx(x)+", y="+fx(y));
		firstInBar=false;
	}
	public Painter[]newViewPainters(boolean selected){
		ItemList<Painter>painters=new ItemList(Painter.class);
		if(note.tone.pitch!=ScaleNote.PITCH_REST){
			if(note.ledgerLines!=0)painters.addItems(staveLinePainters(
					x,y+note.ledgerLineShift*unitY,width,note.ledgerLines));
			painters.addItems(newBeadPainters(shade));
		}
		return painters.items();
	}
	private Painter[]newBeadPainters(Shade shade){
		Dot dotAt=note.dotAt;
		double time=note.tone.beats;
		if(false&&time<NOTE_QUARTER)shade=Shades.gray;
		SvgPath dot=dotAt==Dot.NONE?Empty
					:dotAt==Dot.BELOW?DotBelow:DotLevel,
				bead=time<NOTE_HALF?Solid:time<NOTE_WHOLE?Half
						:time<NOTE_DOUBLE?Whole:Double;
		ItemList<Painter>painters=new ItemList(Painter.class);
		Painter dotPainter=p.mastered(dot.newOutlined(shade,null,false));
		if(dotAt==Dot.ABOVE)
			p.applyTransforms(new PainterSource.Transform[]{
					p.transformAt(0,-unitY),
			},true,dotPainter);
		painters.addItems(dotPainter,
				p.mastered(bead.newOutlined(shade,null,true)));
		p.applyTransforms(new PainterSource.Transform[]{
				p.transformAt(x+width*0.1,y+unitY),
				p.transformScale(height*.9,height*.9),
		},true,painters.items());
		if(tail!=null){
			painters.add(p.line(new Line(
					new Point(tail.from.at().plus(note.at).scaled(scaleToPage)),
					new Point(tail.to.at().plus(note.at).scaled(scaleToPage))
					),shade,1,false));
		}
		return painters.items();
	}
	public Painter[]newPickPainters(){
		String text=dissonances.isEmpty()?null:dissonances.toString().replaceAll(" ,","");
		Painter[]beadPainters=newBeadPainters(shade.darker());
		return Objects.join(Painter.class,beadPainters,
				text==null?new Painter[]{}
				:new Painter[]{true?unscaledText(text,x,y,-1):tooltipText(text,x,y,-1)});
	}
}
