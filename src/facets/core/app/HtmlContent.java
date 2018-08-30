package facets.core.app;
import facets.core.superficial.app.SSelection;
import facets.util.Debug;
import facets.util.Stateful;
import facets.util.StatefulCore;
import facets.util.shade.Shade;
/**
{@link Stateful} container for mutable HTML source. 
 */
final public class HtmlContent extends StatefulCore{	
	/**
	Defines start and end of text selection in rendering and source. 
	 */
	static public class HtmlSelected implements Fonted{
		public final int renderStart,renderStop,codeStart,codeStop;
		public HtmlSelected(int renderStart,int renderStop,int codeStart,int codeStop){
			this.renderStart=renderStart;
			this.renderStop=renderStop;
			this.codeStart=codeStart;
			this.codeStop=codeStop;
		}
		@Override
		public String toString(){
			return Debug.info(this)+"[" +renderStart+
					"," +renderStop+
					"," +codeStart+
					"," +codeStop+
					"]";
		}
		@Override
		public Shade shade(){
			throw new RuntimeException("Not implemented in "+Debug.info(this));
		}
		@Override
		public void setShade(Shade shade){
			throw new RuntimeException("Not implemented in "+Debug.info(this));
		}
		@Override
		public String fontFace(){
			throw new RuntimeException("Not implemented in "+Debug.info(this));
		}
		@Override
		public int fontSize(){
			throw new RuntimeException("Not implemented in "+Debug.info(this));
		}
		@Override
		public boolean fontIsBold(){
			throw new RuntimeException("Not implemented in "+Debug.info(this));
		}
		@Override
		public boolean fontIsItalic(){
			throw new RuntimeException("Not implemented in "+Debug.info(this));
		}
		@Override
		public void setFontFace(String fontFace){
			throw new RuntimeException("Not implemented in "+Debug.info(this));
		}
		@Override
		public void setFontSize(int fontSize){
			throw new RuntimeException("Not implemented in "+Debug.info(this));
		}
		@Override
		public void setFontBold(boolean bold){
			throw new RuntimeException("Not implemented in "+Debug.info(this));
		}
		@Override
		public void setFontItalic(boolean italic){
			throw new RuntimeException("Not implemented in "+Debug.info(this));
		}
	}
	private String source;
	/**
	Unique constructor. 
	@param title passed to superclass
	@param source valid HTML text, non-<code>null</code> but possibly empty
	 */
	public HtmlContent(String title,String source){
		super(title);
		setState(source);
	}
	/**
	The HTML currently wrapped, possibly set via {@link #setState(Object)}. 
	@return valid HTML text, non-<code>null</code> but possibly empty
	 */
	public String source(){
		return source;
	}
	public void setState(Object src){
		source=src instanceof String?(String)src:((HtmlContent)src).source;		
		if(source==null)throw new IllegalArgumentException("Null source in "+Debug.info(this));
	}
	public Stateful copyState(){
		return new HtmlContent(title(),source);
	}
	public boolean stateEquals(Stateful s){
		HtmlContent html=(HtmlContent)s;
		return title().equals(html.title())&&source.equals(html.source);
	}
	public SSelection newSelection(int start,int stop,boolean inSource){
		final HtmlSelected selected=inSource?new HtmlSelected(-1,-1,start,stop)
				:new HtmlSelected(start,stop,-1,-1);
		return new SSelection(){
			public Object content(){return source;}
			public Object single(){return selected;}
			public Object[]multiple(){
				return new Object[]{selected};
			}
		};
	}
}
