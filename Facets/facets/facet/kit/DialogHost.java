package facets.facet.kit;
import static java.lang.Math.*;
import facets.core.app.AppWindowHost;
import facets.core.app.HideableHost;
import facets.core.app.PagedSurface;
import facets.core.app.Dialogs.Response;
import facets.core.superficial.SFacet;
import facets.core.superficial.app.SSurface.DialogSurface;
import facets.core.superficial.app.SSurface.WindowAppSurface;
import facets.util.Debug;
import facets.util.Tracer;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
public abstract class DialogHost extends Tracer implements HideableHost{
	private final class Layout implements FacetLayout{
		final KitFacet content,buttons,extras;
		Layout(SFacet content,SFacet buttons,SFacet extras){
			this.content=(KitFacet)content;
			this.buttons=(KitFacet)buttons;
			this.extras=(KitFacet)extras;
		}
	}
	protected final AppWindowHost appHost;
	protected final KitCore kit;
	protected Object wrapped;
	protected Response response;
	private Layout layout;
	protected DialogHost(AppWindowHost appHost,KitCore kit){
		this.appHost=appHost;
		this.kit=kit;
	}
	public final FacetLayout newLayout(SFacet content,SFacet buttons,SFacet extras){
		return new Layout(content,buttons,extras);
	}
	public final void setLayout(FacetLayout layout){
		this.layout=(Layout)layout;		
	}
	private DialogSurface surface;
	public final Response launchWindowedSurface(PagedSurface surface,WindowAppSurface app){
		this.surface=surface;
		if(wrapped==null){
			KitCore.widgets++;
			wrapped=newWrapped();
			if(layout==null)throw new IllegalStateException(
					"Null layout for surface=\n"+Debug.info(surface));
			else buildDialog(layout.content,layout.buttons,layout.extras,surface.trim);
		}
		updateLayout(app);
		response=null;
		double factor=kit.layoutFactor();
		launchSurfaceInWindow(surface);
	  Rectangle bounds=getClosingBounds();
		surface.dialogDismissed(new Rectangle(
				bounds.getLocation(),scaleDimension(bounds.getSize(),factor,true)));
	  if(response==null)throw new IllegalStateException(
				"Null response in "+Debug.info(this));
	  else return response;
	}
	protected abstract Object newWrapped();
	protected abstract void buildDialog(KitFacet content,KitFacet buttons, 
			KitFacet extras,Dimension trimmings);
	protected abstract void launchSurfaceInWindow(DialogSurface surface);
	protected abstract Rectangle getClosingBounds();
	public final Object wrapped(){
		return wrapped;
	}
	public abstract void setWindowExtension(SFacet facet,boolean extendSideways);
	protected final Rectangle scaledLaunchBounds(DialogSurface surface,double factor){
		Rectangle bounds=surface.getLaunchBounds();
		Point at=bounds.getLocation();
		if(at.x==DialogSurface.AT_NOT_SET) 
			at=appHost==null?new Point(0,0):appHost.newBounds().calculateSmartDialogAt(bounds.getSize());
		return new Rectangle(at,DialogHost.scaleDimension(bounds.getSize(),factor,false));
	}
	public static Dimension scaleDimension(Dimension size,double factor,boolean invert){
		if(invert)factor=1/factor;
		return new Dimension((int)rint(size.width*factor),(int)rint(size.height*factor));
	}
}