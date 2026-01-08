package tones.view.paint;
import facets.core.app.avatar.Painter;
import facets.core.app.avatar.PainterSource;
import facets.util.Debug;
import facets.util.ItemList;
import facets.util.Tracer;
import facets.util.geom.Line;
import facets.util.geom.Vector;
import facets.util.shade.Shade;
import facets.util.shade.Shades;
import tones.view.PageView;
public abstract class PagePainters extends Tracer{
	private final static Shade SHADE_NOTE_PLAIN=Shades.blue,
			SHADE_NOTE_HILIT=Shades.red,
			SHADE_NOTE_SELECTED=Shades.green;
	static final int TEXT_POINTS=13;
	final PageView page;
	final PainterSource p;
	final double unitY,unitX;
	final Vector scaleToPage;
	PagePainters(PageView page,PainterSource p){
		this.page=page;
		this.p=p;
		unitY=page.unitY();
		unitX=unitY*page.widthForPitch();
		scaleToPage=new Vector(unitX,unitY);
	}
	public abstract Painter[]newViewPainters(boolean selected);
	final protected Painter unscaledText(String text,double x,double y,double dropFactor){
		double scaledPoints=TEXT_POINTS/page.scale(),
			textDrop=scaledPoints*dropFactor;
		return p.textCaption(text,x,y+textDrop,
				"face=Tahoma","shade=magenta","points="+(int)scaledPoints);
	}
	final protected Painter tooltipText(String text,double x,double y,double dropFactor){
		double scaledPoints=TEXT_POINTS/page.scale(),
			textDrop=scaledPoints*dropFactor;
		return p.textTooltip(text,x,y+textDrop);
	}
	final protected Painter[]staveLinePainters(double fromX,double fromY,
			double width,int count){
		Line[]lines=new Line[Math.abs(count)];
		for(int i=0;i<lines.length;i++){
			double lineY=fromY+unitY*2*i*(count>0?1:-1);
			lines[i]=new Line(new double[]{fromX,lineY,fromX+width,lineY});
		}
		ItemList<Painter>painters=new ItemList(Painter.class);
		painters.add(p.backgroundLines(lines,Shades.gray));
		return painters.items();
	}
	public Painter[]newPickPainters(){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	protected final Shade selectionShade(boolean selected,boolean dissonant){
		return selected?SHADE_NOTE_SELECTED:dissonant?SHADE_NOTE_HILIT:SHADE_NOTE_PLAIN;
	}
}
