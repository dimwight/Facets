package facets.facet;
import facets.core.app.AppConstants;
import facets.core.app.PagedSurface;
import facets.core.app.SContentAreaTargeter;
import facets.core.app.ValueContent;
import facets.core.superficial.Notice;
import facets.core.superficial.Notifying;
import facets.core.superficial.STarget;
import facets.core.superficial.app.SSurface;
import facets.facet.app.FacetPagedContenter;
import facets.util.Util;
import facets.util.app.AppValues;
import facets.util.tree.ValueNode;
/**
{@link FacetPagedContenter} for {@link ValueNode} content such as 
that of {@link AppValues}.
 */
public abstract class ValueDialogContenter extends FacetPagedContenter{
	protected final SSurface parent;
	protected final ValueContent content;
	protected final AppValues app;
	public ValueDialogContenter(String title,FacetFactory ff,SSurface parent,
			AppValues app,ValueNode master,ValueNode working){
		super(title,ff);
		this.parent=parent;
		this.app=app;
		content=new ValueContent(title,master,working){
			public void targetValuesUpdated(STarget target,ValueNode values,String keys){
				ValueDialogContenter.this.targetValuesUpdated(target,values,keys);
			}
		};
	}
	public void areaRetargeted(SContentAreaTargeter area){
		contentRetargeted(content.working);
		if(false)Util.printOut("ValueDialogContenter.rootRetargeted: content="+content);
		STarget applyTrigger=PagedSurface.findDialogTrigger(area,AppConstants.TITLE_APPLY);
		applyTrigger.setLive(content.hasChanged());
	}
	protected abstract void targetValuesUpdated(STarget target,ValueNode values,
			String keys);
	protected abstract void contentRetargeted(ValueNode working);
	public void applyChanges(){
		content.applyChanges();
		app.adjustValues();
		parent.host().updateLayout(parent);
		if(parent.isBuilt())parent.notify(new Notice(contentFrame(),Notifying.Impact.DEFAULT));
	}
	final public void reverseChanges(){
		content.reverseChanges();
		applyChanges();
	}
}