package applicable.textart;

import static facets.facet.FacetFactory.*;
import static java.awt.event.KeyEvent.*;
import facets.core.app.ActionViewerTarget;
import facets.core.app.HtmlView;
import facets.core.app.StatefulViewable;
import facets.core.app.ActionViewerTarget.Action;
import facets.core.superficial.app.ViewableAction;
import facets.facet.app.FacetConstants;
import facets.util.shade.Shade;
import facets.util.shade.Shades;
import facets.util.tree.ValueNode;




import java.awt.Font;

/**
 Constants for {@link TextArt} content. 
 */
public interface TextArtConstants extends FacetConstants{
	
	//Viewer actions and groups
	ViewableAction[] ACTIONS_ALL = {
			ActionViewerTarget.Action.ITERATE_BACK,
			ActionViewerTarget.Action.ITERATE_FORWARD,
			ActionViewerTarget.Action.SELECT_ALL,
			ActionViewerTarget.Action.COPY,
			ActionViewerTarget.Action.CUT,
			ActionViewerTarget.Action.PASTE,
			ActionViewerTarget.Action.DELETE,
			ActionViewerTarget.Action.UNDO,
			ActionViewerTarget.Action.REDO,
	},
	ACTION_GROUPS[] = {
			//Line iterators etc for toolbar and menu
			new ViewableAction[]{
				ActionViewerTarget.Action.ITERATE_BACK,
				ActionViewerTarget.Action.ITERATE_FORWARD,
			},	
			new ViewableAction[]{
				ActionViewerTarget.Action.ITERATE_BACK,
				ActionViewerTarget.Action.ITERATE_FORWARD,
				ActionViewerTarget.Action.SELECT_ALL
			},
			//Edit items for toolbar and menu
			new ViewableAction[]{
				ActionViewerTarget.Action.COPY,
				ActionViewerTarget.Action.CUT,
				ActionViewerTarget.Action.PASTE,
			},
			new ViewableAction[]{
				ActionViewerTarget.Action.COPY,
				ActionViewerTarget.Action.CUT,
				ActionViewerTarget.Action.PASTE,
				ActionViewerTarget.Action.DELETE,
			},
			new ViewableAction[]{
				ActionViewerTarget.Action.UNDO,
				ActionViewerTarget.Action.REDO,
			}
	};

	//Property keys for line and line set
	String 
		KEY_GRID_SNAP = "gridSnap",
		KEY_ANGLE_SNAP = "angleSnap",	
		KEY_LIMITS = "limits",
		KEY_LIMIT_WIDTH = "limitWidth",
		KEY_LIMIT_HEIGHT = "limitHeight",
		VALUE_NONE = "None";
	
	//Strings for indexings 
	String[] 
	  GRID_SNAPS = {
			VALUE_NONE, "10", "20", "50"
		}, 
		ANGLE_SNAPS = {
			VALUE_NONE, "15", "45"
		}, 
		FONT_FACES = {
			"Serif", "SansSerif", "Monospaced"
		}, 
		SHADE_TITLES = {
			"Red", "Magenta", "Blue", "Green", "Cyan"
		};
		Integer[] FONT_SIZES = {
			9, 12, 14, 16, 20, 24, 30
		}; 

	//Colours for use in indexing. 
	Shade[] SHADES = {
		Shades.red, 
		Shades.magenta, 
		Shades.blue, 
		Shades.green, 
		Shades.cyan
	};

	//Content for selection frame when no content is selected
	ValueNode NO_SELECTION = new TextArt("[No line selected]",
			0, 0, 0, SHADES[0],FONT_FACES[0],FONT_SIZES[0],true,false,"").sourceNode();

	//Assorted constants, keys, indices 
	int FONT_PLAIN = Font.PLAIN, FONT_BOLD = Font.BOLD,
		FONT_ITALIC = Font.ITALIC, LIMITS_EDGE = 10, LIMITS_NONE = 1000,
		GRID_NONE = 0, 
		APP_BAR_TOOLS = 0, 
		VIEWABLE_GRID = 0, VIEWABLE_ANGLE_SNAP = 1, VIEWABLE_LIMITS = 2, 
		VIEWABLE_TREE = 3, 
		ACTIONS_SELECTION_CORE = 0, ACTIONS_SELECTION_FULL = 1, 
		ACTIONS_EDIT_CORE = 2, ACTIONS_EDIT_FULL = 3, ACTIONS_UNDO_REDO = 4,
		VIEW_GRID_SHOW = 0, 
		LINE_DRAW_XY = 0,
		LINE_DRAW_ANGLE = 1, LINE_TEXT = 2, LINE_COLOR = 3, LINE_FONT_FACE = 4,
		LINE_FONT_SIZE = 5, 
		LINE_FONT_STYLE = 6, 
		LINE_TEXT_DIALOG= 7,  
		LINE_STATUS = 8,
		TOOLS_XY = 0, TOOLS_ANGLE = 1, TOOLS_FONT = 2, TOOLS_SHADE = 3;
	String TOOL_TITLE_XY = "Position", TOOL_TITLE_ANGLE = "Angle", 
		TOOL_TITLE_FONT = "Font", TOOL_TITLE_VIEW = "View",
		TOOL_TITLES [] = {
			TOOL_TITLE_XY, TOOL_TITLE_ANGLE, TOOL_TITLE_FONT, 
		},
		TAB_TITLE_CONSTRAINTS = "Constraints", TAB_TITLE_TREE = "Structure",
		TITLE_LINE_LEFT = "Left|"+ARROW_LEFT,
		TITLE_LINE_RIGHT = "Right|"+ARROW_RIGHT,
		TITLE_LINE_UP = "Up|"+ARROW_UP,
		TITLE_LINE_DOWN = "Down|"+ARROW_DOWN,
		TITLE_LINE_CCW = "Anticlockwise|"+ARROW_LEFT,
		TITLE_LINE_CW = "Clockwise|"+ARROW_RIGHT,TITLE_LIMITS = "Limi&ts",
		TITLE_BOLD = "Bold",TITLE_ITALIC = "Italic";
	
	//Decorations for facets
	Object[][]DECORATION_VALUES= {		
		{ActionViewerTarget.Action.COPY,"","Copy16.gif","Copy selection",VK_C+KEY_CTRL},
		{ActionViewerTarget.Action.CUT,"","Cut16.gif","Cut selection",VK_X+KEY_CTRL},
		{ActionViewerTarget.Action.DELETE,"","Delete16.gif","Delete selection",VK_DELETE},
		{ActionViewerTarget.Action.PASTE,"","Paste16.gif","Paste at selection",VK_V+KEY_CTRL},
		{ActionViewerTarget.Action.ITERATE_FORWARD,"","Forward16.gif","Select next line"},
		{ActionViewerTarget.Action.ITERATE_BACK,"","Back16.gif","Select previous line"},
		{TITLE_BOLD,"","Bold16.gif","Set bold"},
		{TITLE_ITALIC,"","Italic16.gif","Set italic"},
		{TITLE_LINE_RIGHT,"","Forward16.gif","Move selection right"},
		{TITLE_LINE_LEFT,"","Back16.gif","Move selection left"},
		{TITLE_LINE_CW,"","Forward16.gif","Turn selection clockwise"},
		{TITLE_LINE_CCW,"","Back16.gif","Turn selections anticlockwise"},
		{TITLE_LINE_UP,"","Up16.gif","Move selection up"},
		{TITLE_LINE_DOWN,"","Down16.gif","Move selection down"},
		{TITLE_LIMITS,"","AlignCenter16.gif","Apply limits"},
		{ActionViewerTarget.Action.UNDO,"","Undo16.gif","Undo previous edit"},
		{ActionViewerTarget.Action.REDO,"","Redo16.gif","Redo undone edit"},
		{TITLE_APP_CLOSE,"","Stop16.gif","Exit program"},
		{HtmlView.PAGE_NEXT,"","StepForward16.gif",""},
		{HtmlView.PAGE_PREVIOUS,"","StepBack16.gif",""},
	};
}
