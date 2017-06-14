package facets.core.app;
import facets.core.superficial.SFacet;
import facets.core.superficial.STargeter;
import facets.core.superficial.app.ViewerTarget;
import facets.util.Debug;
/**
Creates {@link SFacet}s for a top-level menu. 
<p>{@link MenuFacets} supplies facets for a pull-down or 
context menu, facets that may in turn manage single or multiple
menu items or sub-menus. The facets and the top-level menu itself must be created 
using an appropriate facet builder. 
 */
public abstract class MenuFacets{
	final public STargeter targeter;
	final private String title;
  /**
  Unique constructor. 
   * @param targeter is set as {@link #targeter}
   * @param title is returned by {@link #title} if non-empty and that method 
  is not overridden
   */
  public MenuFacets(STargeter targeter,String title){
		if((this.targeter=targeter)==null)throw new IllegalArgumentException(
				"Null targeter in "+Debug.info(this));
		else if((this.title=title)==null)throw new IllegalArgumentException(
				"Null title in "+Debug.info(this));
	}
	/**
	Return facets managing items for a menu. 
	 */
	public SFacet[]getFacets(){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	/**
	Allows context sensitivity and merging of facets. 
	@param viewer 
	@param viewerFacets 
	@return by default {@link #getFacets()}
	 */
	public SFacet[]getContextFacets(ViewerTarget viewer, SFacet[]viewerFacets){
		return getFacets();
	}
	/**
	 Returns the title for a pull-down menu. 
	 <p>Returns either any non-empty title passed during construction, 
	 or that of the targeter passed. 
	 */
	final public String title(){
		return !title.equals("")?title:targeter.title();
	}
}
