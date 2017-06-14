package facets.core.superficial.app;
import static java.awt.Cursor.*;
/** 
Supplies {@link SView} policy and {@link SSelection} content to 
a viewer facet. 
<p>{@link SViewer} defines the basic interface for a viewer, 
	providing a suitable viewer facet with its view policy and content selection 
	and enabling it to report mouse-defined actions. 
 */
public interface SViewer{
	 int CURSOR_DEFAULT=DEFAULT_CURSOR,CURSOR_TEXT=TEXT_CURSOR,
	  CURSOR_HAND=HAND_CURSOR,CURSOR_CROSSHAIR=CROSSHAIR_CURSOR,
	  CURSOR_WAIT=WAIT_CURSOR,CURSOR_MOVE=MOVE_CURSOR,
	  CURSOR_W=W_RESIZE_CURSOR,CURSOR_N=N_RESIZE_CURSOR,
	  CURSOR_S=S_RESIZE_CURSOR,CURSOR_E=E_RESIZE_CURSOR,
	  CURSOR_NW=NW_RESIZE_CURSOR,CURSOR_SE=SE_RESIZE_CURSOR,
	  CURSOR_NE=NE_RESIZE_CURSOR,CURSOR_SW=SW_RESIZE_CURSOR;
	/**
	Defines the viewer facet type and supplies its policy. 
	 */
	SView view();
	/**
	Defines a region of viewable content to be displayed by the viewer facet. 
	 */
	SSelection selection();
	/**
	To be called by the viewer facet whenever it makes a 
	selection change. 
	 @param selection defines the new selection. 
	 */
	void selectionChanged(SSelection selection);
	/**
  To be called by the viewer facet whenever it proposes an
  edit to the selection passed. 
	@param selection if <code>null</code> implies edit should be applied to the 
	current selection; otherwise the selection should be changed to match
	@param edit defines the change proposed to the selection
	@param interim if <code>true</code> the edit forms part of a sequence 
   */
  void selectionEdited(SSelection selection,Object edit, boolean interim);
  /**
	Does the viewer facet have focus?  
	 */
	boolean isActive();
}
