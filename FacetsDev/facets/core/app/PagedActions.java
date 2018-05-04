package facets.core.app;
import facets.core.superficial.STrigger;
import facets.util.Debug;
/**
Defines top-level controls for a {@link PagedSurface}. 
 */
public abstract class PagedActions{
	public static final PagedActions NONE=new PagedActions(){
		@Override
		public STrigger[]newTriggers(){
			return new STrigger[]{};
		}
	};
	private PagedSurface surface;
	/**
	Called from {@link PagedSurface} constructor
	 */
	protected void attachSurface(PagedSurface surface){
		this.surface=surface;
	}
	/**
	Allows subclasses to access the {@link PagedSurface}. 
	@return the surface attached with {@link #attachSurface(PagedSurface)}
	 */
	protected final PagedSurface surface(){
		if(surface==null)throw new RuntimeException("Null surface in "+Debug.info(this));
		else return surface;
	}
	/**
	Create {@link STrigger}s defining the dialog control buttons. 
	 */
	public abstract STrigger[]newTriggers();
}