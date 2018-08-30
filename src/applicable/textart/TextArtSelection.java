package applicable.textart;

import static applicable.textart.TextArtConstants.*;
import facets.core.app.AppConstants;
import facets.core.app.Dialogs;
import facets.core.superficial.Notifying;
import facets.core.superficial.SFrameTarget;
import facets.core.superficial.SIndexing;
import facets.core.superficial.SNumeric;
import facets.core.superficial.STarget;
import facets.core.superficial.STextual;
import facets.core.superficial.SToggling;
import facets.core.superficial.STrigger;
import facets.core.superficial.TargetCore;
import facets.core.superficial.Notifying.Impact;
import facets.util.NumberPolicy;
import facets.util.Util;
import facets.util.geom.AnglePolicy;
import facets.util.shade.Shade;
import facets.util.tree.ValueNode;


/**
 Selection frame for a single {@link TextArt} providing targets representing 
 its properties. 
 */
final class TextArtSelection extends SFrameTarget{

	//References for couplers
	private SNumeric 
		atX, 
		atY;

	//Targets representing text line contentStyle
	private SToggling 
		fontBold, 
		fontItalic;

	//The creating viewable model, dialogs (unless in applet)
	private final TextArtViewable viewable;
	private final Dialogs dialogs;

	/**
	 Unique constructor. 
	 @param line passed to the super constructor
	 @param viewable stored for use in {@link #lazyElements()}
	 @param dialogs stored for use in {@link #lazyElements()}
	 */
	TextArtSelection(ValueNode node, TextArtViewable viewable, Dialogs dialogs) {
		super(node);
		this.viewable = viewable;
		this.dialogs=dialogs;
	}

	/**
	Re-implementation of framework method. 
	<p>Defines application-specific elements and their behaviour,
	each setting a property of the (copy) line passed to the constructor. 
	@see facets.core.superficial.TargetCore#lazyElements()
	 */
	@Override
	protected STarget[] lazyElements() {
		
		//Wrap framed values
		final TextArt line = new TextArt ((ValueNode)framed);

		//Shared coupler for bold and italic togglings
		SToggling.Coupler boldItalic = new SToggling.Coupler() {

			//Respond to user input by setting font contentStyle
			public void stateSet(SToggling t) {
				line.setFontBold(fontBold.isSet());
				line.setFontItalic(fontItalic.isSet());
			}
		};
		
		//Create togglings
		fontBold = new SToggling(TITLE_BOLD, line.fontIsBold(), boldItalic);
		fontItalic = new SToggling(TITLE_ITALIC, line.fontIsItalic(), boldItalic);
		
		//Group as single target
		STarget fontStyle = new TargetCore("St&yle", fontBold, fontItalic);
		
		//For debugging number display
		boolean debuggingAngle = false, 
			debuggingX = NumberPolicy.debug;
		
		//Shared coupler for X and Y numerics
		SNumeric.Coupler xyCoupler = debuggingAngle ? 
				null 
				: new SNumeric.Coupler() {

			//The number policy to be used
			public NumberPolicy policy(final SNumeric n) {
				return viewable.xyPolicy(n.title().equals("X"));
			}

			//Called by numeric when value is set
			public void valueSet(SNumeric n) {
				line.setAt((int) atX.value(), 
						atY == null? 
								line.atY() 
								: (int) atY.value());
			}
		};
				
		//Create X and Y numerics, group as single target
		atX = debuggingAngle ? 
				null
				: new SNumeric("X", line.atX(), xyCoupler);
		atY = debuggingAngle || debuggingX ? 
				null
				: new SNumeric("Y", line.atY(), xyCoupler);
		STarget atXY = debuggingAngle ? 
				null
				: new TargetCore("XY", debuggingX ? 
						new STarget[]{
							atX
						}
						:new STarget[]{
							atX, atY
						});
		
		//Angle numeric with coupler
		SNumeric atAngle = debuggingX ? 
				null 
				: new SNumeric("Angle|Full|Fine",line.atAngle(), 
						new SNumeric.Coupler() {

			public NumberPolicy policy(SNumeric n) {
				return viewable.anglePolicy();
			}

			public void valueSet(SNumeric n) {
				line.setAngle((int)n.value());
			}
		});
		
		//Coerce position and angle to valid values under current policies
		if (!debuggingAngle) line.setAt((int) atX.value(), debuggingX ? 
							line.atY() : (int) atY.value());
		if (!debuggingX) line.setAngle((int)atAngle.value());
		
		//Other targets
		final STarget 
		
			//Textual with coupler
			text = new STextual("Text", line.text(), new STextual.Coupler() {
				
				//Value may not be blank
				public boolean isBlankable(STextual t){return false;}
	
				public void textSet(STextual t) {
					line.setText(t.text());
				}
			}),
			
			textDialog = new STrigger("Text...", new STrigger.Coupler() {
			
				@Override
				public void fired(STrigger t) {
					String input = dialogs.getTextInput("Text", "Enter text for line:", 
							line.text(),0).trim();
					if(input.equals("")) return;
					((STextual)text).setText(input);
					text.notifyParent(Impact.DEFAULT);
				}
			}),
			
			status = new STextual("Status", line.text(), new STextual.Coupler()),
		
			//Colour indexing with coupler 
			shade = new SIndexing("Sha&de", SHADES, line.shade(),
					new SIndexing.Coupler() {

				public void indexSet(SIndexing s) {
					line.setShade((Shade) s.indexed());
				}

				public String[] newIndexableTitles(SIndexing i) {
					return SHADE_TITLES;
				}
			}),
						
			//Surface indexing with coupler
			fontSurface = new SIndexing("Face", FONT_FACES, line.fontFace(),
					new SIndexing.Coupler() {
	
				public void indexSet(SIndexing s) {
					line.setFontFace((String) s.indexed());
				}
			}),
					
			//Size indexing with coupler
			fontSize = new SIndexing("Si&ze", FONT_SIZES, new Integer(line.fontSize()), 
					new SIndexing.Coupler() {

				public boolean canCycle(SIndexing i) {
					return false;
				}

				public void indexSet(SIndexing s) {
					line.setFontSize((Integer) s.indexed());
				}

				public String[] iterationTitles(SIndexing i) {
					return new String[]{
						AppConstants.ARROW_LEFT, AppConstants.ARROW_RIGHT
					};
				}
			});
		
		//Return elements created 
		return new STarget[]{
			debuggingAngle ? atAngle : atXY, 
			debuggingX ? atXY: atAngle, 
			text, 
			shade, 
			fontSurface, 
			fontSize,
			fontStyle, 
			textDialog,
			status,
		};
	}
}