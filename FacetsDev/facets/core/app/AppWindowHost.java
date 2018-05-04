package facets.core.app;
import facets.core.superficial.app.SSurface.WindowAppSurface;
import facets.util.Debug;
import facets.util.Tracer;
import facets.util.app.AppValues;
import facets.util.app.HostBounds;
import facets.util.tree.ValueNode;
import java.awt.Rectangle;
/**
{@link FeatureHost} that manages a window for a {@link WindowAppSurface}. 
<p>{@link AppWindowHost} extends its superclass to manage 
<ul>
<li>opening and closing of the application and its host window
<li>validation and persistance of the window bounds
<li>an opening splash screen
</ul> 
<p>Concrete subclasses will define an appropriate event loop for their GUI toolkit. 
*/
public abstract class AppWindowHost extends Tracer implements FeatureHost{
	protected final WindowAppSurface app;
	protected final ValueNode nature,stateApp;
	protected AppWindowHost(WindowAppSurface app,AppValues values){
		this.app=app;
		nature=values.nature();
		stateApp=values.state(AppValues.PATH_APP);
	}
	/**
	Open the application window. 
	<p>Call from {@link WindowAppSurface#openApp()} once the application surface
	is ready for realisation as a GUI. 
	<p>This implementation just sets checked window bounds, concrete classes should
	override as required to create and open a suitable window.  
	 */
	public void openWindow(){
		setWindowBounds(newBounds().newCheckedBounds());
	}
	/**
	Create a concrete {@link HostBounds} wrapping the {@link AppValues} passed 
	to the constructor. 
	 */
	public abstract HostBounds newBounds();
	/**
	Set the window bounds. 
	<p>Called from {@link #openWindow()}. 
	 */
	protected abstract void setWindowBounds(Rectangle bounds);
	/**
	Allow the application to close itself. 
	<p>Should simulate pressing the Close button. 
	 */
	public abstract void closeWindow();
	/**
	Create a suitable {@link Dialogs} for use by an {@link AppSurface}. 
	@param app should host the dialogs launched
	 */
	public abstract Dialogs newDialogs(ActionAppSurface app);
	/**
	Put up a splash screen while the application is loading. 
	@param msg should be displayed on the screen
	 */
	public abstract void splashUp(String msg);
	/**
	Take down any splash screen. 
	 */
	public abstract void splashDown();
	@Override
	public void showExtras(boolean on){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
}