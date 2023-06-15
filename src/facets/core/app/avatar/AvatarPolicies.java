package facets.core.app.avatar;

import facets.core.app.SViewer;
import facets.core.app.ViewableFrame;
import facets.core.superficial.app.SSelection;
import facets.util.Debug;
import facets.util.Tracer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
/**
Mix-in for avatar creation, painting and drag policies. 
<p>{@link AvatarPolicies} enables content policies for an {@link AvatarView}
to be separated cleanly from the display policy. 
 */
public abstract class AvatarPolicies extends Tracer implements Serializable{
	/**
	Return painter for non-avatar view elements. 
	@param viewer for references to view, selection
	@param p must be supplied by a facet builder
	@return a non-<code>null</code> {@link Painter}; by default {@link Painter#EMPTY} 
	 */
	public Painter getBackgroundPainter(SViewer viewer,PainterSource p){
		return Painter.EMPTY;
	}
	/**
	Defines how an avatar should paint its content. 
	@param content to be painted
	@param p must be supplied by a facet builder
	 */
	public AvatarPolicy avatarPolicy(SViewer viewer,AvatarContent content,PainterSource p){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	/**
	Defines how the viewer should drag an avatar selection. 
    @param content content of the avatars selected 
   @param hit specifies where the drag was initiated in <code>selection</code>
	@param p must be supplied by a facet builder
	@param view will use the policy
	 */
	public DragPolicy dragPolicy(AvatarView view,AvatarContent[]content, 
			Object hit, PainterSource p){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	public boolean isContentSelectable(AvatarContent content){
		return true;
	}
	/**
	Return avatar content to be displayed by viewer. 
	<p>Facilitates delegation by implementations of ;
	@param viewer controlled by the {@link AvatarView} returning the receiver 
	as {@link AvatarView#avatars()}
	 @param viewable the current {@link ViewableFrame#selection()}
	 */
	public SSelection newAvatarSelection(SViewer viewer,SSelection viewable){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	final public static Painter[]joinPainters(Painter[]...in){
		List<Painter>out=new ArrayList();
		for(Painter[]painters:in)
			if(painters!=null)out.addAll(Arrays.asList(painters));
		return out.toArray(new Painter[0]);
	}
}