package facets.core.app;
import static facets.core.superficial.Notifying.Impact.*;
import static facets.util.Debug.*;
import static facets.util.app.Events.*;
import facets.core.superficial.Notice;
import facets.core.superficial.SFacet;
import facets.core.superficial.STarget;
import facets.core.superficial.STargeter;
import facets.core.superficial.Notifying.Impact;
import facets.core.superficial.app.SHost;
import facets.core.superficial.app.SSurface;
import facets.core.superficial.app.SHost.FacetLayout;
import facets.facet.FacetFactory;
import facets.util.Debug;
import facets.util.Tracer;
import facets.util.Util;
/**
 Minimal {@link SSurface}.
 <p>{@link SimpleSurface} has
 <ul>
 <li>a {@link FeatureHost} passed during construction. 
 <li>a single, replaceable {@link AppContenter} defining domain functionality 
 <li>a {@link #surfaceTargeter()} identical with the 
 {@link SContentAreaTargeter} created by its content
 </ul>
 */
public class SimpleSurface extends Tracer implements SSurface{
	private final FeatureHost host;
	private SContentAreaTargeter targeter;
	private AppContenter content;
	public SimpleSurface(FeatureHost host,AppContenter content){
		this.host=host;
		this.content=content;
		traceEvent(">Created surface "+info(this));
	}
	@Override
	protected void traceOutput(String msg){
		Util.printOut(SimpleSurface.class.getSimpleName()+"#"+Debug.id(this)+msg);
	}
	@Override
	final public void buildRetargeted(){
		traceEvent(">Opening surface "+info(this));
		traceEvent(">Creating areas for "+info(content));
		SAreaTarget root=content.newContentArea(true);
		targeter=(SContentAreaTargeter)root.newTargeter();
		targeter.setNotifiable(this);
		traceEvent(">Creating targeter "+info(targeter));
		targeter.retarget(root,DEFAULT);
		FacetLayout layout=host.newLayout(root.attachedFacet(),
				content.newContentFeatures(targeter));
		traceEvent(">Creating layout "+info(layout));
		host.setLayout(layout);
		content.areaRetargeted(targeter);
		targeter.retargetFacets(DEFAULT);
		traceEvent(">Opened surface "+info(this)+"\n");
	}
	final public void replaceContent(AppContenter content){
		if(false)traceDebug(".replaceContent: ",this);
		if(content.getClass()!=this.content.getClass())
			throw new IllegalArgumentException("Non-matching content in "+info(this));
		SAreaTarget rootThen=(AreaRoot)targeter.target(),
			rootNow=content.newContentArea(SAreaTarget.mutableAreaFacets);
		content.alignContentAreas(rootThen,rootNow);
	  ((AreaRoot)rootNow).attachThenFacets(rootThen);
	  targeter.retarget(rootNow,DEFAULT);
		targeter.retargetFacets(DEFAULT);
	  this.content.wasRemoved();
	  (this.content=content).wasAdded();
		traceEvent(">Replaced content in surface " + info(this));
		if(false)notify(new Notice(rootNow,DEFAULT));
	}
	@Override
	public void notify(Notice notice){
		if(trace)traceEvent(">Surface "+info(this)+" notified with "+notice);
		STarget root=targeter.target();
		targeter.retarget(root,notice.impact);
	  if(trace)traceEvent((">Targeters retargeted in "+info(this)));
		content.areaRetargeted(targeter);
		targeter.retargetFacets(notice.impact);
	  if(trace)traceEvent(">Facets retargeted in "+info(this));
	}
	@Override
	final public String title(){
		return content.title();
	}
	@Override
	final public AreaTargeter surfaceTargeter(){
		if(targeter==null)throw new IllegalStateException(
				"Null targeter in "+Debug.info(this));
		else return targeter;
	}
	@Override
	final public SHost host(){
		return host;
	}
	@Override
	final public boolean isBuilt(){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
}