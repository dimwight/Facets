package tones.view.paint;
import static tones.Tone.*;
import static tones.page.PageItem.PageTie.TieType.*;
import facets.core.app.avatar.Painter;
import facets.core.app.avatar.PainterMaster.Outlined;
import facets.core.app.avatar.PainterSource;
import facets.core.app.avatar.PainterSource.Transform;
import facets.util.geom.Vector;
import facets.util.shade.Shade;
import applicable.SvgPath;
import tones.bar.Bar;
import tones.page.PageItem.PageTie;
import tones.page.PageItem.PageTie.TieType;
import tones.view.PageView;
public final class TiePainters extends PagePainters{
	private static final SvgPath 
		BeforeAfter_=new SvgPath("TieFromTo","M213.0 72.13c7.665,7.444 1.127,13.46 -6.062,6.475 -22.96,-18.09 -48.72,-30.72 -98.74,-31.47 -50.02,0.7481 -75.77,13.38 -98.74,31.47 -7.203,6.997 -13.67,0.9138 -6.062,-6.475 26.2,-29.19 66.25,-42.94 104.8,-42.56 38.75,0.0 78.4,13.15 104.8,42.56z",2),
		AfterNullLong_=new SvgPath("TieFromLong","M173.4 58.53c-16.6,-6.798 -37.06,-10.98 -65.2,-11.4 -50.02,0.7481 -75.77,13.38 -98.74,31.47 -7.203,6.997 -13.67,0.9138 -6.062,-6.475 26.2,-29.19 66.25,-42.94 104.8,-42.56 23.01,0.0 46.33,4.635 67.01,14.56l-1.816 14.41z",2),
		AfterNullShort_=new SvgPath("TieFromShort","M108.2 29.57c-38.55,-0.3749 -78.6,13.37 -104.8,42.56 -7.609,7.389 -1.141,13.47 6.062,6.475 22.96,-18.09 48.72,-30.72 98.74,-31.47l0.0 -17.57z",2),
		BeforeNull_=new SvgPath("TieTo","M0.0 29.57c38.55,-0.3749 78.6,13.37 104.8,42.56 7.609,7.389 1.141,13.47 -6.062,6.475 -22.96,-18.09 -48.72,-30.72 -98.74,-31.47l0.0 -17.57z",2);
	private final double noteWidth,noteHeight;
	private final PageTie tie;
	private final boolean tailsUp;
	private final Shade shade;
	public TiePainters(PageView page,PageTie tie,PainterSource p){
		super(page,p);
		this.tie=tie;
		if(false)trace(": tie=",tie);	
		noteWidth=Bar.WIDTH_NOTE*unitX;
		noteHeight=unitY*2;
		tailsUp=tie.voice.tailsUp;
		shade=selectionShade(tie.selected,false);
	}
	@Override
	public
	Painter[]newViewPainters(boolean selected){
		return false?new Painter[]{}:new Painter[]{
				tie.type==BeforeNull?beforeNull(tie):after(tie)
			};
	}
	private Painter beforeNull(PageTie tie){
		Vector afterAt=tie.afterAt;
		double x=tie.bar.pageX,y=afterAt.y,width=afterAt.x-x;
		Painter painter=p.mastered(BeforeNull_.newOutlined(shade,null,false));
		p.applyTransforms(new Transform[]{
			p.transformAt(x*unitX,y*unitY-noteHeight*(tailsUp?1.5:-1)),
			p.transformScale(width/unitX*(tailsUp?2.3:1.8),noteHeight*(tailsUp?1:-1)),
		},true,painter);
		return painter;
	}
	private Painter after(PageTie tie){
		TieType type=tie.type;
		double pastNote=noteWidth*.7,
				x=tie.beforeAt.x+pastNote,y=tie.beforeAt.y;
		final boolean isBoth=type==BeforeAfter;
		Outlined path=(isBoth?BeforeAfter_:true?AfterNullLong_:AfterNullShort_
				).newOutlined(shade,null,false);
		Vector bounds=path.bounds().scaled(noteHeight);
		double toAfter=(isBoth?tie.afterAt.x-x+2:0)*unitX, 
			toBarEnd=(tie.bar.pageX+tie.bar.staveWidth-x)*unitX,
			stretch=(isBoth?toAfter:toBarEnd)/bounds.x;
		Painter painter=p.mastered(path);
		p.applyTransforms(new Transform[]{
			p.transformAt(x*unitX+(isBoth?(tie.before.tone.beats==NOTE_QUARTER?-2:-1):2)
					+(tailsUp?2:-2),
					y*unitY-noteHeight*(tailsUp?1.5:-1.5)),
			p.transformScale(noteHeight*stretch,noteHeight*(tailsUp?1:-1)),
		},true,painter);
		return painter;
	}
}