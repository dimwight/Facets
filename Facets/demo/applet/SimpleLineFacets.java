package demo.applet;

import static applicable.textart.TextArtConstants.*;
import static facets.core.superficial.app.ViewerTarget.*;
import facets.core.app.MountFacet;
import facets.core.superficial.SFacet;
import facets.core.superficial.STargeter;
import facets.core.superficial.app.SAreaTarget;
import facets.core.superficial.app.SContentAreaTargeter;
import facets.core.superficial.app.ViewerTarget;
import facets.facet.FacetFactory;
import facets.util.ItemList;
import facets.util.NumberPolicy;
import facets.util.Util;
import applicable.textart.TextArt;
import applicable.textart.TextArtFeatures;

/**
Facet builder for {@link TextArt} content in spike applet. 
 <p>Assembles facet layouts for use by {@link SimpleLineContenter} in
 applet.  
 <p>This class is a simplified version of {@link TextArtFeatures} as used
in the spike application. 
 */
final class SimpleLineFacets extends FacetFactory{

	//References to targeter tree members
	private final STargeter  
		line, 
		selection,
		fontStyle, 
		drawAt, 
		drawAngle, 
		text, 
		fontSurface, 
		fontSize, 
		color, 
		toolbarSwitcher;

	//Container for tools 
	private final MountFacet toolsSwitchMount;

	/**
	Unique constructor. 
	@param ff core passed to superclass
	@param area retargeted on an {@link SAreaTarget} content area returned by
	{@link SimpleLineContenter}
	@param toolsSwitchMount created in {@link SimpleLineContenter}
	 */
	public SimpleLineFacets(FacetFactory ff, SContentAreaTargeter area, 
			MountFacet toolsSwitchMount) {
		
		//Construct from standard builder
		super(ff);
		
		//Store reference passed
		this.toolsSwitchMount = toolsSwitchMount;

		//Viewer references
		STargeter[] viewerElements = area.viewer().elements(
				)[TARGETS_VIEWABLE].elements();
		selection = viewerElements[ACTIONS_SELECTION_CORE];
		toolbarSwitcher = area.elements()[APP_BAR_TOOLS];

		//Line selection references
		line = area.selection();
		STargeter[] lineElements = line.elements();
		fontStyle = lineElements[LINE_FONT_STYLE];
		drawAt = lineElements[LINE_DRAW_XY];
		drawAngle = lineElements[LINE_DRAW_ANGLE];
		text = lineElements[LINE_TEXT];
		fontSurface = lineElements[LINE_FONT_FACE];
		fontSize = lineElements[LINE_FONT_SIZE];
		color = lineElements[LINE_COLOR];
	}


	/**
	Creates tools panel for applet. 
	 */
	SFacet newToolsPanel() {
		
		//Define facet and layout
		SFacet 
				
			//Position tools
			toolsXY[] ={
				numericSliders(drawAt, 235, 
						HINT_TALL + HINT_SLIDER_FIELDS_TICKS_LABELS + HINT_SLIDER_LOCAL), 
				spacerWide(15), 
				numericNudgeButtons(drawAt, HINT_BARE + HINT_TALL), 
			}, 
			
			//Angle tools	
			toolsAngle[] = NumberPolicy.debug ? null : new SFacet[]{
					numericSliders(drawAngle, 195, HINT_TALL 
							+ HINT_SLIDER_FIELDS_TICKS_LABELS + HINT_SLIDER_LOCAL), 
					spacerWide(8), 
					numericNudgeButtons(drawAngle, HINT_BARE + HINT_TALL), 
					BREAK, 
					numericSliders(drawAngle, 300, HINT_TALL + HINT_BARE
							+ HINT_SLIDER_TICKS + HINT_SLIDER_LABELS), 
					BREAK, 
					fill()
			}, 
			
			//Font tools
			toolsFont[] = {
				spacerWide(5), 
				indexingRadioButtons(fontSurface, HINT_TALL), 
				rowPanel(line, 0, 5, HINT_NONE, 
				new SFacet[]{
					rowPanel(fontSize, 0, 0, HINT_PANEL_MIDDLE, new SFacet[]{
						indexingDropdownList(fontSize, HINT_NONE), 
						indexingIteratorButtons(fontSize, HINT_BARE), 
					}), 
					BREAK, 
					togglingCheckboxes(fontStyle, HINT_NONE), 
				}), 
				BREAK, 
				indexingPaneSingle(color, 55, 6, HINT_TALL), 
			}, 
			
			//Contents of tools switcher	
			toolPanels[][] = {
				toolsXY, 
				NumberPolicy.debug ? null : toolsAngle, 
				toolsFont, 
			};
					
		//Assemble tools switcher, define and return complete panel
		toolsSwitchMount.setFacets(rowPanels(toolPanels, line, HINT_NONE));	
		SFacet iterators=triggerButtons(selection, HINT_TITLE1);
		return false?rowPanel(line, iterators) 
			: rowPanel(line,
					rowPanel(line, 0, 7, HINT_PANEL_MIDDLE, 
						iterators, 
						spacerWide(5), 
						textualField(text, 19, HINT_BARE), 
						BREAK, 
						indexingRadioButtons(toolbarSwitcher, HINT_NONE), 
						BREAK, 
						spacerTall(5)
					), 
					BREAK, toolsSwitchMount
			);
	}


	/**
	 Creates menus for applet. 
	 */
	SFacet[] newMenuRoots() {
	
		//Line menu layout
		ItemList<SFacet> lineItems = new ItemList(SFacet.class);
		lineItems.addItem(triggerMenuItems(selection, HINT_BARE));
		lineItems.addItem(BREAK);
		lineItems.addItems(numericNudgeMenuItems(drawAt, HINT_BARE));
		lineItems.addItem(BREAK);
		lineItems.addItems(numericNudgeMenuItems(drawAngle, HINT_BARE));
		
		//Define other layouts and complete menus
		SFacet
		
			//Line menu from layout
			lineMenu = menuRoot(selection, "Line", lineItems.items()), 
			
			//Font layout and menu
			fontMenu = menuRoot(line, "Font", new SFacet[]{
					indexingRadioButtonMenu(fontSurface, HINT_NONE), 
					indexingRadioButtonMenu(fontSize, HINT_NONE), 
					togglingCheckboxMenu(fontStyle, HINT_BARE), 
					indexingRadioButtonMenu(color, HINT_NONE), 
				});
		
		//Return menus
		return new SFacet[]{
			lineMenu, fontMenu, 
		};
	}
}