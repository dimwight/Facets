package facets.core.app;
import static facets.util.Debug.*;
import static facets.util.app.Events.*;
import facets.core.superficial.FacetedTarget;
import facets.core.superficial.SFrameTarget;
import facets.core.superficial.STarget;
import facets.core.superficial.app.SAreaTarget;
import facets.core.superficial.app.SContentAreaTargeter;
import facets.core.superficial.app.SContenter;
import facets.core.superficial.app.SSelection;
import facets.core.superficial.app.ViewableAction;
import facets.core.superficial.app.ViewableFrame;
import facets.core.superficial.app.ViewerTarget;
import facets.util.Debug;
import facets.util.FileSpecifier;
import facets.util.Objects;
import facets.util.Tracer;
import facets.util.TypesKey;
import facets.util.tree.Nodes;
import java.io.IOException;
import java.util.Arrays;
/**
{@link SContenter} that builds content roots containing viewers. 
<p>{@link ViewerContenter} extends its superclass to build 
  one or more roots containing viewers exposing content created from a source
  passed to its constructor. 
<p>Its implementation of {@link #newContentArea(boolean)}
defines template methods encapsulating two distinct approaches to the
build process.
<p>Since {@link ViewerContenter} primarily creates content for 
{@link AppSurface}, it provides a basic implementation of 
 {@link AppContenter}; it also defines methods for saving content back
 to a source such as a file that can also function as a sink.
*/
public abstract class ViewerContenter extends Tracer implements AppContenter{
 	private static final ViewableFrame VIEWABLE_NONE=new ViewableFrame("No viewable set",""){{
 		 setSelection(new SSelection(){
			@Override
			public Object content(){
				return framed;
			}
			@Override
			public Object single(){
				return framed;
			}
			@Override
			public Object[] multiple(){
				throw new RuntimeException("Not implemented in "+Debug.info(this));
			}
		});
 		}
 	};
	/**
	Can create content. 
	<p>{@link ContentSource}s can encapsulate generation of 
	content for use by implementations of {@link SContenter#newContentArea(boolean)}. 
	 */
	public abstract static class ContentSource<T>{
		/**
		Create new content. 
		 */
		public abstract T newContent();
	}
	private Object sourceOrSink;
	private ViewableFrame viewable=VIEWABLE_NONE;
	/**
	Unique constructor. 
	@param source passed by {@link #newContentArea(boolean)} to 
	{@link #newContentViewable(Object)}
	 */
	protected ViewerContenter(Object source){
		if((sourceOrSink=source)==null)
			throw new IllegalArgumentException("Null source or sink in "+info(this));
	}
	@Override
	public boolean useActiveFeatures(SContentAreaTargeter active){
		return false;
	}
	public static TypesKey viewerFeaturesKey(ViewerContenter active,SContentAreaTargeter use,
			boolean withViewer){
		Class activeType=active.targetType(),useType=use.targetType();
		Class[]types=Objects.join(Class.class,
				useType==activeType?new Class[]{activeType}:new Class[]{activeType,useType},
						withViewer?new Class[]{use.viewer().targetType()}:new Class[]{});
		return new TypesKey(types);
	}
	@Override
	public TypesKey featuresKey(SContentAreaTargeter use){
		return viewerFeaturesKey(this,use,false);
	}
	@Override
	public Class targetType(){
		return getClass();
	}
	/**
	Implements abstract method. 
	<p>Calls template methods as follows:
	<ol>
	<li>{@link #newContentViewableArea(Object, boolean)} which can be implemented to 
	create a content area complete with attached facet, which 
	is then returned without the remaining methods being called; only recommended for 
	very simple or very unusual implementations.   
  <li>{@link #newContentViewable(Object)} to create content from the source
  passed to the constructor and wrap it in a {@link ViewableFrame}. 
  <li>{@link #newContentViewers(ViewableFrame)}to define one 
    or more {@link ViewerTarget}s for incorporation in a 
    content area, with multiple viewers contained in {@link FacetedTarget}s 
    so as to meet the contract of {@link SAreaTarget}.
	<li>{@link #newContentAreaTitle(ViewableFrame)} and 
	{@link #lazyContentAreaElements(SAreaTarget)} to specify the remaining
	features of an {@link SAreaTarget} to be returned as the area of 
	the viewers area tree, followed by 
	{@link #newViewersArea(String, FacetedTarget[])} to create the area
	<li>(if <code>faceted</code> is <code>true</code>)
	{@link #attachContentAreaFacets(AreaRoot)} 
	to create and attach a facet to the area tree.  
	</ol>
	@param faceted if <code>false</code> can signal a fresh invocation that requires
	return of an unfaceted area wrapping the existing viewable
	 */
	public final SAreaTarget newContentArea(boolean faceted){
	  traceEvent(">Creating content area in "+info(this));
	  return newContentViewableArea(sourceOrSink,faceted);
	}
	/**
	May create an area target tree complete with facets. 
	<p>Called by {@link #newContentArea(boolean)} to return 
	an {@link SAreaTarget} to meet the same contract. 
	<p>Default returns <code>null</code>, requiring valid implementations of 
	subsequent methods (recommended).  
	@param source may be either a content source such as a file, or
	the content itself; as passed to the constructor or changed by {@link #setSink(Object)}  
	 * @param faceted if <code>true</code>, the area returned 
	 should have a facet attached
	 */
	protected AreaRoot newContentViewableArea(Object source,boolean faceted){
		if(viewable==VIEWABLE_NONE){
		  traceEvent(">Creating viewable from "+info(source));
	  	viewable=newContentViewable(source);
	  }
	  if(viewable==null)throw new IllegalStateException(
	  		"No viewable in "+Debug.info(this));
	  else traceEvent(">Creating viewers for "+info(viewable));
	  FacetedTarget[]viewers=newContentViewers(viewable);
	  if(viewers==null)throw new IllegalStateException("Invalid null viewers in "+info(this));
	  else if(!((viewers.length==1&&viewers[0]instanceof ViewerTarget)||
	  		viewers instanceof SAreaTarget[]))
	  	throw new IllegalStateException("Invalid viewer array in "+info(this));
	  String areaTitle=newContentAreaTitle(viewable);
	  if(areaTitle==null||areaTitle.equals(""))
	  	throw new IllegalStateException("Null or empty content area title in "+info(this));
	  AreaRoot area=newViewersArea(areaTitle,viewers);
	  if(area==null)throw new IllegalStateException("Invalid null area in "+info(this));
	  else traceEvent(">Created content area " +info(area));
		if(!faceted)return area;
		attachContentAreaFacets(area);
		traceEvent(">Attached area facets to " +info(area));
	  try{
	  	area.attachedFacet();
	  }
	  catch(Exception e){	  	
	  	throw new IllegalStateException(Debug.info(area)+" has no facet in "+info(this));
	  }
		return area;
	}
	/**
	Create a {@link ViewableFrame} framing content. 
	<p>Called by the first invocation of {@link #newContentArea(boolean)}; 
	default implementation is an invalid stub.
	<p>A valid implementation should create content for the {@link ViewableFrame} 
	from <code>source</code> or suitable default content, possibly using a 
	{@link ViewerContenter.ContentSource} passed to the constructor.
	<p>The {@link ViewableFrame} returned is passed to {@link #newContentViewers(ViewableFrame)}. 
	@param source may be either a content source such as a file, or
	the content itself; as passed to the constructor or changed by {@link #setSink(Object)}  
	 */
	protected ViewableFrame newContentViewable(Object source){
		throw new RuntimeException("Not implemented in "+this);
	}
	/**
	Create a {@link FacetedTarget}[] defining an arrangement of 
	 viewers for <code>viewable</code>. 
	<p>Called by {@link #newContentArea(boolean)} with the {@link ViewableFrame} 
	returned by {@link #newContentViewable(Object)}. 
	 <p>The {@link FacetedTarget}s returned must comprise either 
	 <ul><li>a single {@link facets.core.superficial.app.ViewerTarget}, or
	 <li> any number of {@link SAreaTarget}s defining an arrangement of 
	 {@link facets.core.superficial.app.ViewerTarget}s. 
	</ul>
	<p>Default implementation is an invalid stub;
	 {@link ActionViewerTarget#newViewerAreas(ViewableFrame, STarget[])} 
	 can be used for implementations 
	 where viewers share the same {@link ViewableAction}[].
	 @param viewable was returned by {@link #newContentViewable(Object)}
	 and should be returned as the <code>viewable</code> of all 
	 {@link facets.core.superficial.app.ViewerTarget}s 
	 constructed in this method.    
	 */
	protected FacetedTarget[]newContentViewers(ViewableFrame viewable){
		throw new RuntimeException("Not implemented in "+this);
	}
	/**
	Attach viewer and area facets to the tree headed
	by <code>area</code> using a suitable facet builder. 
	<p>Called by the first invocation of {@link #newContentArea(boolean)}, 
	with the {@link SAreaTarget} returned by 
	{@link #newViewersArea(String, FacetedTarget[])} 	
	<p>Default implementation is an invalid stub. 
	@param area was returned by {@link #newContentArea(boolean)}
	 */
	protected void attachContentAreaFacets(AreaRoot area){
		throw new RuntimeException("Not implemented in "+this);
	}
	/**
	Return the title for a content area. 
	<p>The default implementation returns the <code>title</code> of <code>viewable</code>.
	@param viewable was constructed in {@link #newContentViewable(Object)}
	 */
	protected String newContentAreaTitle(ViewableFrame viewable){
		return viewable.title();
	}
	/**
	Creates a content area suitable for the containing surface. 
	<p>Creates a package-private subclass of {@link SAreaTarget} 
	suitable for multi-content application returning <code>elements</code> defined in 
	{@link #lazyContentAreaElements(SAreaTarget)}, with <code>viewers</code>
	as its children. 
	<p>Called by {@link #newContentArea(boolean)}, should also be called from 
	implementations of {@link #newContentViewableArea(Object, boolean)}. 
	<p><b>Note</b> For use in a {@link PagedSurface} must be reimplemented to return a
	{@link PagedContentArea}. 
	 @param title was returned by {@link #newContentAreaTitle(ViewableFrame)}
	 @param viewers were created in {@link #newContentViewers(ViewableFrame)}
	 */
	final protected AreaRoot newViewersArea(String title,FacetedTarget[]viewers){
		return new AppAreas.ViewerContentArea(title,viewers,this);
	}
	/**
	Return elements for the content area. 
	<p>The default implementation returns an empty <code>Target[]</code> to
	meet the contract of <code>TargetCore.lazyElements</code>. 
	@param area contains the viewers returned by 
		{@link #newContentViewers(ViewableFrame)}.
	 */
	public STarget[]lazyContentAreaElements(SAreaTarget area){
		return new STarget[]{};
	}
	public void alignContentAreas(SAreaTarget existing,SAreaTarget added){}
	/**
	Implements interface method. 
	<p>Empty implementation.  
	 */
	public void areaRetargeted(SContentAreaTargeter area){}
	/**
	Returns the {@link ViewableFrame} framing content generated by the 
	<code>source</code> of this {@link ViewerContenter}.
	<p>The {@link ViewableFrame} is unique to this instance.  
	 */
	final public ViewableFrame contentFrame(){
		if(viewable==null)throw new IllegalStateException("No viewable in "+Debug.info(this));
		else return viewable;
	}
	/**
	Implements interface method. 
	<p>Default returns <code>true</code>. 
	 */
	public boolean hasChanged(){return true;}
	/**
	Implements interface method. 
	<p>Default is empty stub. 
	 */
	public void wasAdded(){}
	/**
	Implements interface method. 
	<p>Default is empty stub. 
	 */
	public void wasRemoved(){}
	/**
	 Implements abstract method. 
	 <p>Returns the title of {@link #contentFrame()}. 
	 */
	public String title(){
		return contentFrame().title();
	}
  /**
	Data sink to which the {@link ViewerContenter} can be persisted. 
	<p>Set during construction to the source.
	 */
	final public Object sink(){
		return sourceOrSink;
	}
	/**
	Attempts to set a data sink for the content. 
	@param sink becomes the new {@link #sink()}
	@return <code>true</code> by default; return <code>false</code> to signal 
	unacceptable sink
	 */
	public boolean setSink(Object sink){
		if(sink==null)throw new IllegalArgumentException("Null sink in "+info(this));
		else this.sourceOrSink=sink;
		return true;
	}
	/**
	Attempt to save to a sink. 
	<p>Default is invalid stub.
	 @param sink typically a file
	 @throws IOException
	 */
	public void saveToSink(Object sink)throws IOException{
		if(true)throw new IOException("No save in "+info(this));		
		else throw new RuntimeException("Not implemented in "+info(this));		
	}
	/**
	Specify file passed to {@link #saveToSink(Object)}. 
	@return one or more {@link FileSpecifier}s; default is invalid stub 
	 */
	public FileSpecifier[]sinkFileSpecifiers(){
		throw new RuntimeException("Not implemented in "+this);
	}
}