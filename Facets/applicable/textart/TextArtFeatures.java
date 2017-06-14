package applicable.textart;

import static applicable.textart.TextArtConstants.*;
import facets.core.app.MenuFacets;
import facets.core.app.MountFacet;
import facets.core.superficial.SFacet;
import facets.core.superficial.SIndexing;
import facets.core.superficial.STargeter;
import facets.core.superficial.app.SAreaTarget;
import facets.core.superficial.app.SContentAreaTargeter;
import facets.core.superficial.app.SSelection;
import facets.core.superficial.app.ViewableFrame;
import facets.core.superficial.app.ViewerTarget;
import facets.facet.AppFacetsBuilder;
import facets.facet.AreaFacets;
import facets.facet.FacetFactory;
import facets.facet.ViewerAreaMaster;
import facets.facet.app.FacetAppSurface;
import facets.util.Debug;
import facets.util.ItemList;
import facets.util.NumberPolicy;
import facets.util.tree.ValueNode;

/**
Facet builder for {@link TextArt} content. 
 <p>Assembles facet layouts for use by {a @link TextLineContenter}.  
 */
public class TextArtFeatures extends FacetFactory{

	/**
	Manages surface elements created before {@link TextArtFeatures} itself. 
	 */
	public static final class AdvanceFacets{
		final SIndexing toolIndexing;
		private final FacetFactory ff;
		private final MountFacet toolsMount;
		private SFacet sidebarTree;

		public AdvanceFacets(FacetFactory ff){
			this.ff=ff;
			toolsMount = ff.switchMount("Bar Tools|Tools");
			toolIndexing=FacetFactory.switchMountIndexing(toolsMount,
					new SIndexing.Coupler() {
				public String[] newIndexableTitles(SIndexing s) {
					return TOOL_TITLES;
				}
			});
		}

		void attachSidebarTree(SAreaTarget area){
			AreaFacets areas=ff.areas();
			if(sidebarTree==null){
				sidebarTree = false?null:areas.viewerArea(area,new ViewerAreaMaster(){
					protected String hintString(){return HINT_BARE;}
				});
			}
			else areas.attachFacetArea(sidebarTree,area);
		}

		void adjustToolIndexing(ValueNode nodeThen,ValueNode nodeNow){
			TextArt thenLine = new TextArt(nodeThen), 
				nowLine = new TextArt(nodeNow);
			int toolsThen = toolIndexing.index(),
			toolsNow = nowLine.atX() == 0 && nowLine.atY() == 0 ? toolsThen
				: thenLine.atX() != nowLine.atX() 
					|| thenLine.atY() != nowLine.atY() ? TOOLS_XY
				: thenLine.atAngle() != nowLine.atAngle() ? TOOLS_ANGLE
				: !thenLine.fontFace().equals(nowLine.fontFace()) 
					|| !thenLine.shade().equals(nowLine.shade())
					|| thenLine.fontIsBold() != nowLine.fontIsBold()
					|| thenLine.fontIsItalic() != nowLine.fontIsItalic()
					|| thenLine.fontSize() != nowLine.fontSize() ? TOOLS_FONT
				: toolsThen;

			//Adjust tools display as required
			if (toolsThen != toolsNow)toolIndexing.setIndex(toolsNow);		
			
		}
		
	}

	//References to targeter tree members
	private final SContentAreaTargeter area;
	private final STargeter views, 
		states, 
		selectionCore, 
		selectionFull, 
		editFull, 
		editCore, 
		line, 
		fontStyle, 
		drawAt, 
		drawAngle, 
		text, 
		status, 
		textDialog, 
		fontSurface, 
		fontSize, 
		gridSnap, 
		angleSnap, 
		gridShow, 
		limits, 
		color, 
		toolbarSwitcher;
	
	private final AdvanceFacets advanceFacets;

	/**
	Main constructor for used by {@link TextArtContenter}. 
	@param facets core passed to superclass
	@param rootTargeter retargeted on an {@link SAreaTarget} content root returned by
	{@link TextArtContenter}
	@param sharedFacets ultimately created by application surface
	 */
	public TextArtFeatures(FacetFactory facets, SContentAreaTargeter rootTargeter, 
			AdvanceFacets sharedFacets) {
		
		//Construct from standard builder
		super(facets);
		
		//Store references passed
		this.area = rootTargeter;
		this.advanceFacets = sharedFacets;

		//Viewer references
		STargeter[] viewerElements = rootTargeter.viewer().elements(
				)[ViewerTarget.TARGETS_VIEWABLE].elements();
		selectionCore = viewerElements[ACTIONS_SELECTION_CORE];
		selectionFull = viewerElements[ACTIONS_SELECTION_FULL];
		editCore = viewerElements[ACTIONS_EDIT_CORE];
		editFull = viewerElements[ACTIONS_EDIT_FULL];
		states = viewerElements[ACTIONS_UNDO_REDO];
		toolbarSwitcher = advanceFacets==null?null
				:rootTargeter.elements()[APP_BAR_TOOLS];

		//Content references
		STargeter[] viewableElements = rootTargeter.content().elements();
		gridSnap = viewableElements[VIEWABLE_GRID];
		angleSnap = viewableElements[VIEWABLE_ANGLE_SNAP];
		limits = viewableElements[VIEWABLE_LIMITS];
		gridShow = rootTargeter.view().elements()[VIEW_GRID_SHOW];

		//Views reference
		views = rootTargeter.views();

		//Line selection references
		line = rootTargeter.selection();
		STargeter[] lineElements = line.elements();
		fontStyle = lineElements[LINE_FONT_STYLE];
		drawAt = lineElements[LINE_DRAW_XY];
		drawAngle = lineElements[LINE_DRAW_ANGLE];
		text = lineElements[LINE_TEXT];
		status = lineElements[LINE_STATUS];
		textDialog = lineElements[LINE_TEXT_DIALOG];
		fontSurface = lineElements[LINE_FONT_FACE];
		fontSize = lineElements[LINE_FONT_SIZE];
		color = lineElements[LINE_COLOR];
	}


	/**
	Minimal constructor. 
	 */
	public TextArtFeatures(FacetFactory facets, SContentAreaTargeter rootTargeter, SFacet contentFacet) {
		super(facets);
		
		this.area = rootTargeter;
		
		advanceFacets = null;
		editCore = editFull = states = selectionCore = selectionFull =
		toolbarSwitcher = gridSnap = angleSnap = limits = 
		gridShow = views = line = status = fontStyle = drawAt = drawAngle = text = 
		textDialog = fontSurface = fontSize = color = null;
	}

	/**
	Creates toolbar. 
	 */
	SFacet newToolbar() {
		
		
		//Define facet and layout
		SFacet []
				
			//Position tools
			toolsXY = NumberPolicy.debug ? null : new SFacet[]{
				numericNudgeButtons(drawAt, HINT_NUMERIC_FIELDS + HINT_TITLE1), 
				indexingDropdownList(gridSnap, HINT_NONE), 
				spacerWide(3),
				togglingButtons(limits, HINT_NONE), 
			}, 
			
			//Angle tools	
			toolsAngle = {
				numericNudgeButtons(drawAngle, HINT_NUMERIC_FIELDS + HINT_BARE), 
				indexingDropdownList(angleSnap, HINT_TITLE1), 
			}, 
			
			//Font tools
			toolsFont = {
				indexingDropdownList(fontSurface, HINT_NONE), 
				togglingButtons(fontStyle, HINT_BARE), 
				indexingDropdownList(fontSize, HINT_NONE), 
				indexingIteratorButtons(fontSize, HINT_BARE), 
				spacerWide(3), 
				indexingDropdownList(color, HINT_NONE), 
			}, 
			
			//Tools switcher panels
			toolPanels = rowPanels(new SFacet[][]{
					NumberPolicy.debug ? toolsAngle : toolsXY, 
					toolsAngle, 
					toolsFont, 
				}, area, HINT_PANEL_MIDDLE);
					
		//Fill tools switcher, define and return complete panel
		advanceFacets.toolsMount.setFacets(toolPanels);
		return toolGroups(line, HINT_NONE, new SFacet[]{
				triggerButtons(editCore, HINT_GRID), 
				triggerButtons(states, HINT_GRID + HINT_BARE), 
				BREAK, 
				triggerButtons(selectionCore, HINT_NONE), 
				textualField(text, 15, HINT_BARE), 
				BREAK, 
				indexingDropdownList(views, HINT_NONE), 
				BREAK, 
				indexingDropdownList(toolbarSwitcher, HINT_TITLE1), 
				advanceFacets.toolsMount
		});
	}

	/**
	Creates status bar. 
	@param app 
	 */
	SFacet newStatus(FacetAppSurface app) {
		return toolGroups(line,HINT_NONE, app.newDebugSwitchLabel(textualLabel(status,HINT_NONE)));
	}

	/**
	 Creates text line menus. 
	 */
	SFacet[] newLineMenuRoots() {
	
		//Line menu layout
		ItemList<SFacet> lineItems = new ItemList(SFacet.class);
		lineItems.addItem(triggerMenuItems(selectionFull, HINT_BARE));
		lineItems.addItem(BREAK);
		lineItems.addItems(numericNudgeMenuItems(drawAt, HINT_NONE));
		lineItems.addItem(BREAK);
		lineItems.addItems(numericNudgeMenuItems(drawAngle, HINT_BARE));
		if(false) {
			lineItems.addItem(BREAK);
			lineItems.addItem(togglingCheckboxMenuItems(limits,HINT_BARE));
			lineItems.addItem(BREAK);
			lineItems.addItem(triggerMenuItems(textDialog, HINT_NONE));
		}
		
		//Define other layouts and complete menus
		SFacet
		
			//Line menu from layout
			lineMenu = menuRoot(selectionCore, "Line", lineItems.items()), 
			
			//Font layout and menu
			fontMenu = menuRoot(line, "F&ont", new SFacet[]{
					indexingRadioButtonMenu(fontSurface, HINT_NONE), 
					indexingRadioButtonMenu(fontSize, HINT_NONE), 
					togglingCheckboxMenu(fontStyle, HINT_BARE), 
					indexingRadioButtonMenu(color, HINT_NONE), 
				}), 
			
			//View layout and menu
			viewMenu = menuRoot(views, "View", new SFacet[]{
					indexingRadioButtonMenuItems(views, HINT_NONE), 
					indexingIteratorMenu(views, HINT_TITLE1), 
					BREAK, 
					indexingRadioButtonMenu(gridSnap, HINT_NONE), 
					togglingCheckboxMenuItems(gridShow, HINT_NONE), 
					indexingRadioButtonMenu(angleSnap, HINT_NONE), 
					//togglingCheckboxMenuItems(limits, HINT_NONE), 
				}), 
				
			//Edit layout and menu for desktop app only
			editMenu = menuRoot(area, "Edit", new SFacet[]{
					triggerMenuItems(states, HINT_TITLE1), 
					BREAK, 
					triggerMenuItems(editFull, HINT_NONE)
				});
		
		//Return menus
		return new SFacet[]{
					editMenu, lineMenu, fontMenu, viewMenu
			};
	}

	/**
	Creates facet source for context menus. 
	 */
	MenuFacets newContextFacets() {
		
		return new MenuFacets(area, "ContextFacets") {
	
			//Facets created when required
			private SFacet[] lineContextFacets, viewContextFacets;
	
			@Override
			//Has to decide which to return for each invocation
			public SFacet[] getContextFacets(ViewerTarget viewer, SFacet[]viewerFacets){
				
				if(viewerFacets!=null)return viewerFacets;
				
				//Line selected?
				SSelection selection = ((ViewableFrame) 
						area.content().target()).selection();
				boolean lineSelection = selection.multiple()[0] != selection.content();
				
				//Create/return appropriate facet
				return lineSelection
	
						//Line if selection
						? lineContextFacets == null
								? lineContextFacets = new SFacet[]{
									triggerMenuItems(editFull, HINT_NONE),
									BREAK,
									indexingRadioButtonMenu(fontSurface, HINT_NONE), 
									indexingRadioButtonMenu(fontSize, HINT_NONE), 
									togglingCheckboxMenu(fontStyle, HINT_BARE), 
									indexingRadioButtonMenu(color, HINT_NONE), 
									BREAK, 
									true?null:triggerMenuItems(textDialog, HINT_NONE)
								} 
								: lineContextFacets 
								
						//Otherwise view
						: viewContextFacets == null
								? viewContextFacets = new SFacet[]{
									triggerMenuItems(editFull, HINT_NONE),
									BREAK,
									indexingRadioButtonMenu(views, HINT_NONE), 
								} 
								: viewContextFacets;
			}
		};
	}

	/**
	 Creates complete menus. 
	 */
	public SFacet[] newMenuRoots(SFacet[] contentRoots, FacetAppSurface app){
		
		//Get appropriate standard facet sources
		final MenuFacets appMenuFacets = new AppFacetsBuilder(this,area).newMenuFacets(), 
			windowMenuFacets = windowMenuFacets(area,true), 
			helpMenuFacets = helpMenuFacets(area);
				
		//Merge with content roots, return
		ItemList<SFacet> roots = new ItemList(SFacet.class);
		roots.addItem(menuRoot(appMenuFacets));
		roots.addItems(contentRoots);
		roots.addItem(menuRoot(windowMenuFacets));
		roots.addItem(menuRoot(helpMenuFacets));
		return roots.items();
	}


	/**
		Creates contents for sidebar. 
		 */
	SFacet newSidebar() {
		
		//Define facet and layout
		SFacet 
				
			//Position tab
			tabXY = rowPanel(line, 0, 5, HINT_PANEL_INSET, new SFacet[]{
				NumberPolicy.debug ? numericFields(drawAt, HINT_NONE)
					: numericSliders(drawAt, 150, 
						HINT_TALL + HINT_SLIDER_FIELDS_TICKS_LABELS + HINT_SLIDER_LOCAL), 
				BREAK, 
				indexingRadioButtons(gridSnap, HINT_TALL), 
				togglingCheckboxes(limits, HINT_BARE+HINT_USAGE_PANEL), 
				BREAK, 
				fill()
			}), 
			
			//Angle tab	
			tabAngle = NumberPolicy.debug ? null 
				: rowPanel(line, 0, 5, HINT_PANEL_INSET, new SFacet[]{
				numericSliders(drawAngle, 150, 
						HINT_SLIDER_FIELDS_TICKS_LABELS + HINT_TALL + HINT_TITLE1), 
				BREAK, 
				numericSliders(drawAngle, 120, HINT_TALL + HINT_TITLE2
						+ HINT_SLIDER_FIELDS_TICKS_LABELS + HINT_SLIDER_LOCAL), 
				BREAK, 
				numericNudgeButtons(drawAngle, HINT_USAGE_PANEL + HINT_BARE
						+ HINT_TALL), 
				BREAK, 
				indexingRadioButtons(angleSnap, HINT_TALL + HINT_TITLE1), 
				BREAK, 
				fill()
			}), 
			
			//Font tab
			tabFont = rowPanel(line, 0, 5, HINT_PANEL_INSET, new SFacet[]{
						indexingRadioButtons(fontSurface, HINT_TALL), 
						BREAK, 
						togglingCheckboxes(fontStyle, HINT_USAGE_PANEL + HINT_BARE), 
						BREAK, 
						rowPanel(fontSize, 0, 0, HINT_PANEL_MIDDLE, new SFacet[]{
							indexingDropdownList(fontSize, HINT_NONE), 
							indexingIteratorButtons(fontSize, HINT_BARE), 
						}), 
						BREAK, 
						indexingPaneSingle(color, 120, 6, HINT_TALL), 
						BREAK, 
						fill()
					}), 
				
			//Constraints tab
			tabConstraints = rowPanel(views, 10, 5, HINT_PANEL_INSET, new SFacet[]{
						indexingRadioButtons(gridSnap, HINT_TALL), 
						indexingRadioButtons(angleSnap, HINT_TALL), 
						BREAK, 
						togglingCheckboxes(limits, HINT_BARE+HINT_USAGE_PANEL), 
						BREAK, 
						fill()
					}), 				
			
			//View tab
			tabView = rowPanel(line, 0, 5, HINT_PANEL_INSET, new SFacet[]{
					indexingRadioButtons(views, HINT_BARE + HINT_TALL), 
					BREAK, 
					indexingIteratorButtons(views, HINT_TITLE1), 
					BREAK, 
					togglingCheckboxes(gridShow, HINT_BARE+HINT_NO_FOCUS), 
					BREAK, 
					fill()
			}); 
			
		if(advanceFacets.sidebarTree==null)throw new IllegalStateException(
				"Null tabTree in "+Debug.info(this));
					
		//Define and return complete panel
		return tabMount(area, new SFacet[]{
				tabFont, 
				advanceFacets.sidebarTree,
				tabView, 
				tabConstraints, 
				tabXY, 
				NumberPolicy.debug ? null : tabAngle, 
			}, new String[]{
				TOOL_TITLE_FONT, 
				TAB_TITLE_TREE, 
				TOOL_TITLE_VIEW,
				TAB_TITLE_CONSTRAINTS, 
				TOOL_TITLE_XY, 
				TOOL_TITLE_ANGLE, 
		});
	}
	
}