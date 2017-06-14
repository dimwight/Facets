package facets.core.app;
import facets.util.shade.Shade;
import facets.util.shade.Shaded;
/**
Font features for rich text objects. 
 */
public interface Fonted extends Shaded{
	String fontFace();
	int fontSize();
	boolean fontIsBold();
	boolean fontIsItalic();
	void setFontFace(String fontFace);
	void setFontSize(int fontSize);
	void setFontBold(boolean bold);
	void setFontItalic(boolean italic);

}
