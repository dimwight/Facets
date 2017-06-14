package applicable.textart;

import static applicable.textart.TextArtConstants.*;
import static facets.util.geom.Angle.*;
import facets.core.app.avatar.Painter;
import facets.core.app.avatar.PainterSource;
import facets.core.app.avatar.PainterSource.Transform;
import facets.util.Debug;
import facets.util.Tracer;
import facets.util.geom.Point;
import facets.util.shade.Shade;
import applicable.textart.TextArtAvatarPolicies.DragStyle;

/** 
Depicts a {@link TextArt} in a viewer. 
<p>{@link TextArtPainters} creates low-level {@link Painter}s for
{@link TextArtAvatarPolicies} and {@link TextArtDragPolicy} by
<ul><li>storing properties of a text line
<li>lazily creating raw painters as required
<li>applying transforms to these painters based on its stored properties
(which may be edited externally during drags)
</ul>
 */
final class TextArtPainters extends Tracer{

	//Immutable properties
	private final String text, surface;
	private final Shade shade;
	private final boolean bold, italic;

	//Mutable property
	private int fontSize;

	//Mutable properties, package visible for drag edits
	int atX, atY, atAngle;
	
	//For creating and transforming painters
	private final PainterSource p;
	
	//Lazily-calculated properties
	private double 
		viewPaintLength = Double.NaN, 
		sizePaintLength = Double.NaN, 
		markWidth = Double.NaN;
	
	//Raw p for various line state elements
	private Painter textPlain, textLo, barPlain, barLo, turnMark, sizeMark;

	private int sizeThen;

	/**
	Unique constructor. 
	@param line to depict
	@param p ultimately wrapping GUI graphics context 
	 */
	TextArtPainters(TextArt line, PainterSource p) {
		
		//Set reference
		this.p=p;
		
		//Extract line properties
		text = line.text(); 
		surface = line.fontFace();
		shade = line.shade();
		bold = line.fontIsBold(); 
		italic = line.fontIsItalic();
		fontSize = line.fontSize();
		atX = line.atX();
		atY = line.atY();
		atAngle = line.atAngle();
	}
	
	/**
	Called from {@link TextArtAvatarPolicies}. 
	@param showSelection is the line selected?
	@param active is the viewer active?
	 */
	Painter[] newViewPainters(boolean showSelection) {
		//Do lazy evaluations?
		if (textPlain == null) textPlain = newPlainText();
		if (showSelection && barPlain == null) barPlain = newPlainBar();
		
		//Apply transforms
		applyTransforms(textPlain);
		if (showSelection) applyTransforms(barPlain);
		
		//Return appropriate painters
		return !showSelection ? new Painter[]{
					textPlain
				} 
			: new Painter[]{
					textPlain, barPlain
				};
	}

	/**
	Called from {@link TextArtAvatarPolicies}. 
	@param hitAt the mouse hit in untransformed painter coordinates
	@param selected is the line selected?
	 */
	Painter[] newPickPainters(Point hitAt, boolean selected) {
		
		//Check for turn pick
		DragStyle style = decideDragStyle(hitAt);
		
		//Do lazy evaluations?
		if (textLo == null) textLo = newLoText();
		if (selected && barLo == null) barLo = newLoBar();
		if (style == DragStyle.TURN && turnMark == null) turnMark = newTurnMark();
		else if (style == DragStyle.SIZE && sizeMark == null) sizeMark = newSizeMark();
		
		//Apply transforms
		applyTransforms(textLo);
		if (selected) applyTransforms(barLo);
		if (style == DragStyle.TURN) applyTransforms(turnMark);
		else if (style == DragStyle.SIZE) applyTransforms(sizeMark);
		
		//Return appropriate p
		return !selected? new Painter[]{textLo} 
			: style == DragStyle.TURN ? new Painter[]{textLo, barLo, turnMark} 
			: style == DragStyle.SIZE ? new Painter[]{textLo, barLo, sizeMark} 
			: new Painter[]{textLo, barLo};
	}

	/**
	Called from {@link TextArtDragPolicy}. 
	@param contentStyle defines the drag type
	 */
	Painter[] newDragPainters (DragStyle style){
		
		//Do lazy evaluations?
		if (textLo == null) textLo = newLoText();
		if (style == DragStyle.TURN && turnMark == null) turnMark = newTurnMark();
		
		//Apply transforms
		applyTransforms(textLo);
		if (style == DragStyle.TURN) applyTransforms(turnMark);
		
		//Return appropriate p
		return style == DragStyle.TURN? new Painter[]{textLo, turnMark} 
				: new Painter[]{textLo};
	}
	

	/**
	Called from {@link TextArtDragPolicy}. 
	@param scale defines latest size
	 */
	Painter[] newSizeDragPainters(double scale) {
		
		int 
			sizes = FONT_SIZES.length, 
			smallest = 0, 
			largest = sizes - 1, 
			below = smallest, 
			above = largest,
			sourceAt = -1;
		
		for(int i=0;i<FONT_SIZES.length;i++)
			if(FONT_SIZES[i]==fontSize)sourceAt=i;
		if(sourceAt == -1)throw new IllegalStateException("Bad font size in "+Debug.info(this));
		
		if (viewPaintLength != viewPaintLength) checkPaintMeasures();
		
		double 
			interval = viewPaintLength / sizes,
			offset = interval * sourceAt,
			rawAt = (scale * viewPaintLength + offset) / interval;
		for (int i = smallest; i < sizes; i++)
			if (i < rawAt) below = i;
		for (int i = largest; i >= 0; i--)
			if (i >= rawAt) above = i;
	
		int useAt = rawAt - below * interval < above * interval - rawAt ? 
				above : below;
		fontSize=FONT_SIZES[useAt];

		textLo = newLoText();
		sizeMark = newSizeMark();
		applyTransforms(textLo);
		applyTransforms(sizeMark);
		return new Painter[]{
			textLo, sizeMark
		};
	}

	/**
	Interprets the position of the hit in relation to the painter. 
	<p>Called from {@link TextArtAvatarPolicies} and 
	{@link TextArtDragPolicy}.  
	@param hitAt position of the hit in raw painter coordinates
	 */
	DragStyle decideDragStyle(Point hitAt) {
		
		checkPaintMeasures();
		
		double hitToOrigin = hitAt.distance(new Point(0, 0));
		return hitToOrigin > viewPaintLength * 0.9
				? DragStyle.TURN : hitToOrigin < viewPaintLength * 0.1
						? DragStyle.SIZE : DragStyle.SHIFT;
	}

	/**
	Makes sure that there is a full set of valid measures used by p. 
	 */
	private void checkPaintMeasures() {
		
		//Check from change
		int sizeNow = fontSize;
		if(sizeNow == sizeThen)return;
		sizeThen = sizeNow;
		
		//Set units
		markWidth = (double) sizeNow / 15;
		sizePaintLength = p.textLength(text, surface, sizeNow, bold, italic);
		if (viewPaintLength != viewPaintLength) viewPaintLength = sizePaintLength;
	}

	/**
	Applies shift and turn based on state of line copy. 
	@param painter to transform
	 */
	private void applyTransforms(Painter painter) {

		//Define and apply transforms
		Transform 
			at = p.transformAt(atX, atY),
			turn = p.transformTurn(toRadians(atAngle), 0, 0);
		p.applyTransforms(new Transform[]{at, turn}, false, new Painter[]{painter});
	}

	/**
	Remaining methods do low-level work. 
	 */
	private Painter newPlainText() {
		return p.textOutline(text, surface, fontSize, bold, italic, 
				shade, null);
	}

	private Painter newLoText() {
		return p.textOutline(text, surface, fontSize, bold, italic, shade.darker(), null);
	}

	private Painter newPlainBar() {
		checkPaintMeasures();
		return p.bar(0, markWidth * 5, viewPaintLength, markWidth, shade, true);
	}

	private Painter newLoBar() {
		return p.bar(0, markWidth * 5, viewPaintLength, markWidth, 
				shade.darker(), true);
	}

	private Painter newTurnMark() {
		checkPaintMeasures();
		return p.turnMark(fontSize * 2.5, markWidth * 4, shade.darker());
	}

	private Painter newSizeMark() {
		checkPaintMeasures();
		return p.stretchMark(fontSize, sizePaintLength, markWidth * 3, shade.darker());
	}

	double paintLength() {
		checkPaintMeasures();
		return viewPaintLength;
	}

	TextArt toTextLine(){
		return new TextArt(text,atX,atY,atAngle,shade,surface,fontSize,bold,italic,"");
	}
}
