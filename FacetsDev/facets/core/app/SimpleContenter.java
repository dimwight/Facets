package facets.core.app;
import facets.core.app.SContentAreaTargeter.ContentArea;
import facets.core.superficial.SFacet;
import facets.core.superficial.SFrameTarget;
import facets.core.superficial.STarget;
import facets.util.Debug;
import facets.util.Tracer;
import facets.util.Util;

import java.awt.Dimension;
/** 
{@link SContenter} that builds a content area with no 
viewers. 
<p>{@link SimpleContenter} is suitable for content that cannot 
  (or need not) appear in viewers, being exposed by an arrangement of 
  simple facets. 
<p>The content passed to the constructor is represented to the surface by 
targets returned by {@link #lazyContentAreaElements(SAreaTarget)}.
<p>Since {@link SimpleContenter} primarily creates content for 
{@link PagedSurface}, it provides a basic implementation of 
{@link facets.core.app.PagedContenter}.
 */
public abstract class SimpleContenter extends Tracer implements PagedContenter{
	private final String title;
	private SFrameTarget contentFrame;
	/**
	Unique constructor. 
	@param title returned by {@link #title()}
	passed to {@link #lazyContentAreaElements(SAreaTarget)}
	 */
	public SimpleContenter(String title){
		if(title==null||title.equals(""))throw new IllegalArgumentException(
				"Null or empty title in "+Debug.info(this));
		this.title=title;
	}
	/**
	Implements abstract method. 
	<p>Creates a trivial subclass of {@link facets.core.app.PagedContentArea} 
	returning <code>elements</code> defined in 
	{@link #lazyContentAreaElements(SAreaTarget)}, with a single {@link SFrameTarget}
	child created from the content. 
	<p><b>Note</b> If the {@link SimpleContenter} is to be used in an 
	{@link AppSurface} this method must be re-implemented to return a
	{@link facets.core.app.AppAreas.ViewerContentArea}.  
	 */
	public SAreaTarget newContentArea(boolean faceted){
		ContentArea area=new PagedContentArea(title,
				new STarget[]{contentFrame=new SFrameTarget(title,Debug.info(SimpleContenter.this))},
				this);
		if(faceted)attachAreaMountFacet(area);
		return area;
	}
	/**
	Attach a facet to the tree headed by <code>area</code>. 
	<p>Called by {@link #newContentArea(boolean)}; 
	the facet attached should be a suitable {@link MountFacet} for the 
	facet returned by {@link PagedContenter#newContentPanel(SContentAreaTargeter)}. 
	@param area was created in {@link #newContentArea(boolean)}
	 */
	protected abstract void attachAreaMountFacet(SAreaTarget area);
	/**
	Implements interface method. 
	<p>Empty impementation.  
	 */
	public void areaRetargeted(SContentAreaTargeter area){}
	/**
	Implements abstract method. 
	<p>Returns the content passed to the constructor. 
	 */
	final public SFrameTarget contentFrame(){
		if(contentFrame==null)throw new IllegalStateException(
				"No contentFrame in "+Debug.info(this));
		return contentFrame;
	}
	public STarget[]lazyContentAreaElements(SAreaTarget area){
		return new STarget[]{};
	}
	/**
	Implements abstract method. 
	<p>Returns the title passed to the constructor. 
	 */
	final public String title(){
		return title;
	}
	/**
	Empty implementation. 
	 */
	@Override
	public void setSurface(PagedSurface surface){}
	@Override
	public void reverseChanges(){}
	@Override
	public Dimension contentAreaSize(){
		return new Dimension(0,0);
	}
	@Override
	public void applyChanges(){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	@Override
	public void hostHidden(){}
	@Override
	public Class targetType(){
		return getClass();
	}
}