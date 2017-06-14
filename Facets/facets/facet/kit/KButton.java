package facets.facet.kit;
public interface KButton extends KWidget{
	enum Type{Fire,Radio,Check,FireDropdown}
	final public static int USAGE_MENU=0,USAGE_PANEL=1,USAGE_TOOLBAR=2,USAGE_ICON=3;
	void redecorate(Decoration decorations);
	boolean isSelected();
	void setSelected(boolean selected);
	String message();
	void setMessage(String msg);
}