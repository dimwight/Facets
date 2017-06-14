package facets.facet.app;
import static facets.facet.FacetFactory.*;
import static java.awt.event.KeyEvent.*;
import facets.core.app.ActionAppSurface;
import facets.core.app.AppConstants;
import facets.core.app.AppSurface;
import facets.core.app.HtmlView;
import facets.core.app.TreeView;
import facets.core.app.ActionViewerTarget.Action;
import facets.core.app.AppSurface.ContentStyle;
import facets.core.app.avatar.PainterMaster.Textual;
import facets.facet.AreaFacets;
import facets.facet.app.tree.TreeTargets;
import facets.facet.kit.Toolkit;
import facets.util.app.AppValues;
import facets.util.app.Events;
import facets.util.tree.DataConstants;
import facets.util.tree.ExceptionNode;
import java.awt.event.InputEvent;
/**
Includes default decorations. 
 */
public interface FacetConstants extends AppConstants{
	String NATURE_SWING_SYSTEM="swingSystem";
	int KEY_CTRL=InputEvent.CTRL_MASK<<16,KEY_SHIFT=InputEvent.SHIFT_MASK<<16;
	/**Index into {@link #DECORATION_VALUES}. */
	int DECORATION_KEY=0,DECORATION_TITLE=1,DECORATION_ICON=2,
		DECORATION_RUBRIC=3,DECORATION_KEYCODE=4;
	/** Default decorations. */
	Object[][]DECORATION_VALUES={
		{TITLE_UP,"","Up16.gif"},
		{TITLE_DOWN,"","Down16.gif"},
		{AreaFacets.PANE_TITLE_MAXIMISE,"","maximize.gif","Maximise this pane"},
		{AreaFacets.PANE_TITLE_RESTORE,"","restore.gif","Restore all panes"},
		{FacetAppSpecifier.PATH_ARGS,"Configuration"},
		{TITLE_APP_PREFERENCES+"...","","","Launch preferences dialog"},
		{FacetPreferences.TITLE_SURFACE,"Surface","",
			"Adjust content layout style and Java look-and-feel"},
		{FacetPreferences.TITLE_SURFACE_STYLE,"Content Style"},
		{ContentStyle.SINGLE,"Sin&gle","","Single content in top-level window"},
		{ContentStyle.TABBED,"Tabbed","","Multiple content in tabs"},
		{ContentStyle.DESKTOP,"Des&ktop","","Multiple content in internal desktop windows"},
		{NATURE_OPEN_EMPTY,"Do no&t create content on open","","Applies to desktop style only"},
		{FacetPreferences.TITLE_SURFACE_LAF,"Look and Feel","","For application launch onwards"},
		{NATURE_SWING_SYSTEM,"Use s&ystem look-and-feel","","For application launch onwards"},
		{FacetPreferences.TITLE_GRAPH,"Graph","","Set build and location of debug targeter graph"},
		{FacetPreferences.TITLE_TRACE,"Trace","","Set trace behaviour"},
		{FacetPreferences.TITLE_VALUES,"Values","","Current trees for nature and session state"},
		{FacetPreferences.TITLE_DRAG,"Drag"},
		{FacetPreferences.TITLE_FEATURES,"Features"},
		{FacetPreferences.TITLE_VIEW,"View"},
		{AppValues.TYPE_NATURE,"","cu_obj.gif"},
		{AppValues.TYPE_STATE,"","cu_change.gif"},
		{NATURE_APP_ICON_SMALL,"","Facets48.png"},//facets16.gif
		{NATURE_APP_ICON_LARGE,"","Facets48.png"},
		{NATURE_APP_ICON_INTERNAL,"","Facets48.png"},
		{ARG_GRAPH_BUILD,"Build targeter graph &viewer","","Set by parameter '"+ARG_GRAPH_BUILD+"'"},
		{KEY_GRAPH_WHERE,"Show graph"},
		{KEY_GRAPH_FIND,"F&ind node","","Enter text to match in node title"},
		{Textual.KEY_TEXT_ALIASING,"Anti-alias graphics text"},
		{AppSurface.TITLE_EMPTY,"No Content"},
		{AreaFacets.PANE_TITLE_ASK,"Al&ways ask","","Open this dialog for any unknown content"},
		{AreaFacets.PANE_TITLE_DEFAULT,"Set as &default","",
			"Use this layout for any unknown content"},
		{AreaFacets.PANE_TITLE_FLIP,"","","Reverse all panes"},
		{AreaFacets.PANE_TITLE_RESET,"","","Restore last user defaults"},
		{Toolkit.KEY_TAB_CLOSE_LO,"Close tab","","",VK_W+KEY_CTRL},//rem_co.gif
		{Toolkit.KEY_TAB_CLOSE_FIRE,"Close tab","","",VK_W+KEY_CTRL},//remove.gif
		{KEY_DRAG_NOTIFY,"Retarget during drag"},
		{KEY_DRAG_PAUSE,"Pause delay","","Wait for pause of at least (millis)"},
		{DataConstants.TYPE_XML,"","generic_xml_obj.gif"},
		{DataConstants.ROOT_XML,"","generic_xml_obj.gif"},
		{DataConstants.TYPE_DATA,"","tree_mode.gif"},
		{DataConstants.TYPE_TEXT,"","mark_occurrences.gif"},
		{DataConstants.VALUE,"","write_obj.gif"},
		{DataConstants.VIEW_DATA,"","edtsrclkup_co.gif"},
		{ExceptionNode.TYPE,"","error_log.gif"},
		{FacetAppSurface.KEY_SPLASH,"Sp&lash screen on startup","","[Not applicable in MDI]"},
		{FacetAppSurface.KEY_DEBUG_STATUS,"Debug info in status bar"},
		{ActionAppSurface.KEY_TIMEOUTS+"|"+FacetPreferences.TITLE_FEATURES,
			"Timeout checking","","Warn if long operations time out"},
		{ActionAppSurface.KEY_TIMED_OUT,"Session Timed Out","",
			"The previous session of " +TAG_APP_TITLE+" appears to have timed out." +
					(true?"":"<br>Time out checking will be switched on. ")},
		{ActionAppSurface.KEY_TIMEOUTS,"Timeout checks","",
			"Timeout checking is switched on which may affect program performance.<br>" +
			"Continue running " +TAG_APP_TITLE+" with timeout checking?" +(true?"":
					" (You can change<br>this setting in Help>Debug.) ")},
		{Events.KEY_EVENTS,"Construction and &retargeting"},
		{Events.KEY_FILTERS,"Include detail:"},
		{Events.KEY_TIMES,"Elapsed times","","As instrumented"},
		{Events.KEY_MEM,"Memory checks","","On content build and disposal"},
		{Events.KEY_TIMES_RESET,"Wait before reset (ms)"},
		{Events.KEY_TRACE,"Show &trace in console"},
		{TreeView.KEY_DEBUG,"Render trees with debug info"},
		{HtmlView.FONT_OFFSET,"Font size","","Reference size for rich text"},
		{HtmlView.KEY_SOURCE,"Show &HTML source in rich text viewers"},
		{TITLE_NO_FILE,"","","No valid file found or supplied - "+TAG_APP_TITLE +" will create default content"},
		{TITLE_NO_NEW,"","","No valid file found or supplied - "+TAG_APP_TITLE +" will close"},
		{TITLE_FILE_OPEN_PREVIOUS,"","","Re-open  "},
		{NATURE_CONFIRM_STATE_SAVE,"Save state?","","Save session state before close?"},
		{TITLE_CONTENT_NEW,"","","Create new application content",VK_N+KEY_CTRL},
		{TITLE_APP_CLOSE,"","terminate.gif","Exit program"},
		{TITLE_APP_HELP,"","help.gif",""},
		{TITLE_FILE_OPEN,"","folder_open.gif","",VK_O+KEY_CTRL},
		{TITLE_FILE_CLOSE,"","","",VK_W+KEY_CTRL},
		{TITLE_WINDOW_CLOSE,"","","Close active window",VK_W+KEY_CTRL},
		{TITLE_SLAVE_CLOSE,"","","",VK_W+KEY_CTRL},
		{TITLE_FILE_SAVE,"","save.gif","",VK_S+KEY_CTRL},
		{TITLE_FILE_REVERT,"","refresh_nav.gif"},
		{TreeTargets.TITLE_FIND,"","","",VK_F+KEY_CTRL},
		{TreeTargets.TITLE_FIND_NEXT,"","","",VK_F+KEY_CTRL+KEY_SHIFT},
		{TreeTargets.TITLE_SEARCH_TYPE,"Type","","= XML tag name"},
		{TreeTargets.TITLE_SEARCH_TITLE,"Title","","= primary attribute where defined"},
		{TreeTargets.TITLE_SEARCH_KEY,"Key","","= XML attribute name"},
		{TreeTargets.TITLE_SEARCH_VALUE,"Value","","= XML attribute value or content line"},
		{TreeTargets.TITLE_SEARCH_RESULTS,"Results","",""},
		{Action.REDO,"","redo_edit.gif"},
		{Action.UNDO,"","undo_edit.gif"},
		{Action.COPY,"","copy_edit.gif","Copy selection",VK_C+KEY_CTRL},
		{Action.CUT,"","cut_edit.gif","Cut selection",VK_X+KEY_CTRL},
		{Action.DELETE,"","delete_edit.gif","Delete selection",VK_DELETE},
		{HtmlView.PAGE_BACK,"","backward_nav.gif","Back"},//linkBack.gif
		{HtmlView.PAGE_FORWARD,"","forward_nav.gif","Forward"},//linkForward.gif
		{HtmlView.PAGE_PREVIOUS,"","back.gif","Previous page"},// pageUp.gif
		{HtmlView.PAGE_NEXT,"","forward.gif","Next page"},//pageDown.gif
		{HtmlView.FONT_SMALLER,"","reduce_font.gif","Decrease text size"},//fontSmaller.gif zoomout.gif
		{HtmlView.FONT_LARGER,"","magnify_font.gif","Increase text size"},//zoomin.gif fontLarger.gif
		{Action.ITERATE_BACK,"","back.gif","",VK_TAB+KEY_SHIFT},
		{Action.ITERATE_FORWARD,"","forward.gif","",VK_TAB},
		{Action.SELECT_ALL,"","","",VK_A+KEY_CTRL},
		{Action.PASTE,"","paste_edit.gif","Paste at selection",VK_V+KEY_CTRL},
		{Action.PASTE_INTO,"","addtsk_tsk.gif","Paste into selection",VK_V+KEY_SHIFT+KEY_CTRL},
		// history_obj.gif stepinto_co.gif
		{Action.EDIT,"","text_edit.gif","Modify or rename selection",VK_F2},
		//write_obj.gif
	};
}
