package facets.core.app;
import static facets.core.app.TextView.FontFamily.*;
import facets.core.app.TextView.FontFamily;
import facets.core.superficial.app.SelectionView;
import javax.swing.text.View;
/**
{@link SView} displaying plain or rich text. 
 */
public class TextView extends SelectionView{
	public enum FontFamily{Dialog,Monospaced,Serif,SansSerif;
		public String toString(){
			return this==Serif?"S&erif":this==SansSerif?"S&ansSerif":name();
		}
	}
	public static final class LongText{
		private final String text;
		public LongText(String text){
			this.text=text;
		}
		@Override
		public String toString(){
			return text;
		}
		@Override
		public boolean equals(Object that){
			return this==that||that instanceof LongText&&text.equals(((LongText)that).text);
		}
		@Override
		public int hashCode(){
			return text.hashCode();
		}
	}
	public static class LinkText{
		public final String text,link;
		public LinkText(String text,String link){
			this.text=text;
			this.link=link;
		}
		public void fireLink(){}
		public boolean visited(){
			return false;
		}
		public String tooltip(){
			return "Click for "+link;
		}
		@Override
		public String toString(){
			return text;
		}
	}
	/** Title for iterating between pages.*/
	public static final String PAGE_UP="Previous Page|\u25b2",
		PAGE_DOWN="Next Page|\u25bc",
		PAGE_PREVIOUS="Previous Page|\u25c4",
		PAGE_NEXT="Next Page|\u25ba",	
		PAGE_BACK="Back|\u25c4",
		PAGE_FORWARD="Forward|\u25ba",
		FONT_LARGER="Larger|\u25b2",
		FONT_SMALLER="Smaller|\u25bc",
		FONT_OFFSET="fontOffset",
		DEBUG_TEXT="The quick brown fox jumped over the laziest dog. ";
	public TextView(String title){
		super(title);
	}
	/**
	Default returns <code>false</code> suiting plain text. 
	 */
	public boolean wrapLines(){
		return false;
	}
	public FontFamily fontFamily(){
		return true?Dialog:Monospaced;
	}
}