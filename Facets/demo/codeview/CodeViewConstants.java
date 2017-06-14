package demo.codeview;
/**
Constants shared by code viewer classes. 
 */
interface CodeViewConstants {
	int 
		//View frame tree indices
		VIEW_TEXT = 0, VIEW_CODE = 1,  
		TEXT_SIZE = 0, TEXT_TABS = 1, TEXT_SYNTAX = 2, 
		CODE_IMPORTS = 0, CODE_JAVADOC = 1, CODE_HTML = 2, 
		TEXT_SIZE_SMALL = 0, TEXT_SIZE_MEDIUM = 1, TEXT_SIZE_LARGE = 2,
		
		//Area tree indices
		AREA_EXTERNAL = 0, AREA_TEXT = 1, AREA_CODE = 2,
		
		//Pane indices
		PANE_TREE = 0, PANE_SOURCE = 1, PANE_DEBUG = 2;
}
