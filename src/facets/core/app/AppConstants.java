package facets.core.app;
import static facets.core.app.AppConstants.*;
import static facets.util.app.Events.*;
import facets.util.app.AppValues;
/**
Constants useful for abstract applications, especially those based on {@link AppValues}. 
 */
public interface AppConstants{
	/**
	Unicode strings for arrow "logos". 
	 */
	String ARROW_LEFT="\u25c4",ARROW_RIGHT="\u25ba",ARROW_UP="\u25b2",
		ARROW_DOWN="\u25bc",
		TITLE_UP="Up|"+ARROW_UP,TITLE_DOWN="Down|"+ARROW_DOWN;
	String HTML_CENTER="<html><p align=\"center\">",HTML_TICK="&#10003";
	/**
	Title for menu target. 
	 */
	String TITLE_APP_REOPEN="Re&fresh View",TITLE_APP_PREFERENCES="Pre&ferences",
	TITLE_APP_ABOUT="About",TITLE_APP_HELP="Sho&w Help",
	TITLE_APP_CLOSE="E&xit",TITLE_SLAVE_CLOSE="C&lose",
	TITLE_CONTENT_NEW="New Content",TITLE_FILE_NEW="New File",TITLE_FILE_OPEN="Open...",
	TITLE_FILE_SAVE="Save",TITLE_FILE_SAVE_AS="Save &as...",
	TITLE_FILE_REVERT="Reload",
	TITLE_FILE_CLOSE="Close|Close File",TITLE_FILE_CLOSEALL="C&lose All",
	TITLE_WINDOW_NEW="New|New Window",TITLE_WINDOW_CLOSE="Close|Close Window",
	TITLE_WINDOW_ACTIVATE="Activate Window";
	/**Title for standard application menu. */
	final String TITLE_CORE_MENU="Application",TITLE_CORE_SLAVE="Window",
		TITLE_FILE_MENU="File",TITLE_EDIT_MENU="Edit",
		TITLE_WINDOW_MENU="Window",TITLE_HELP_MENU="Help";
	/**
	For inserting application title into eg rubrics. 
	 */
	String TAG_APP_TITLE="$appTitle",FIND_APP_TITLE="\\$appTitle";
	/**
	Title/rubric for use in dialogs. 
	 */
	String TITLE_OK="OK",TITLE_APPLY="Appl&y",TITLE_CANCEL="Cancel",
	TITLE_REVERT="Revert",TITLE_CLOSE="Close",TITLE_CONFIRM="Con&firm",
	TITLE_CLOSE_REQUESTED="Close Requested",
	RUBRIC_CONFIRM_CLOSE="Close " +TAG_APP_TITLE+"?",
	TITLE_CLOSE_CONTENT="Close this content?",
	TITLE_NO_FILE="No valid file",
	TITLE_NO_NEW="Cannot create content",
	TITLE_FILE_OPEN_PREVIOUS="Open Previous File?";
	String 
	NATURE_APP_ICON_INTERNAL="appIconInternal",
	NATURE_APP_ICON_SMALL="appIconSmall",
	NATURE_APP_ICON_LARGE="appIconLarge",
	NATURE_ICON_PATH="iconPath",
	NATURE_DOC_PATH="docPath",
	NATURE_ICON_STORE_PATH="storePath",
	NATURE_CONFIRM_CLOSE="confirmClose",
	NATURE_CONFIRM_STATE_SAVE="confirmStateSave",
	NATURE_ENSURE_FIRST_ROOT_ACTIVE="ensureFirstRootActive",
	NATURE_OPEN_EMPTY="openEmpty",
	NATURE_RUN_WATCHED="runWatched",
	NATURE_RUN_WATCHED_DEBUG="runWatchedDebug",
	NATURE_PREFERENCES="showPreferences";
}
