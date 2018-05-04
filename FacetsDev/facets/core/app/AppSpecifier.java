package facets.core.app;
import facets.core.app.AppSurface.ContentStyle;
import facets.util.Debug;
import facets.util.app.AppValues;
import java.util.ArrayList;
import java.util.List;
/**
{@link AppValues} that defines policy for an {@link AppSurface}.
<p><b>Note</b> Some methods are only used by {@link ActionAppSurface} and its subclasses.
 */
public abstract class AppSpecifier extends AppValues{
	private final List<AppSpecifier>hostings=new ArrayList();
	private final boolean forSlave;
	protected AppSpecifier(Class nameClass){
		super(nameClass);
		forSlave=false;
		hostings.add(this);
	}
	public AppSpecifier(AppSpecifier master){
		super(master);
		forSlave=true;
		hostings.addAll(master.hostings);
		hostings.add(0,this);
	}
	/**
	@return by default {@link ContentStyle#SINGLE}
	 */
	public ContentStyle contentStyle(){
		return ContentStyle.SINGLE;
	}
	/**
	Used to compose an {@link ActionAppSurface}. 
	 */
	protected abstract AppActions newActions(ActionAppSurface app);
	/**
	Referenced by default of {@link #canEditContent()}
	@return by default <code>false</code>
	 */
	public boolean canCreateContent(){
		return false;
	}
	/**
	Referenced by default of {@link #canSaveContent()}
	@return by default {@link #canCreateContent()}
	 */
	public boolean canEditContent(){
		return canCreateContent();
	}
	/**
	Referenced by default of {@link #canOverwriteContent()}
	@return by default {@link #canEditContent()}
	 */
	public boolean canSaveContent(){
		return canEditContent();
	}
	/**
	@return by default {@link #canSaveContent()}
	 */
	public boolean canOverwriteContent(){
		return canSaveContent();
	}
	/**
	Are help features available? 
	@return by default false
	 */
	public boolean offersHelp(){
		return false;
	}
	final public boolean forSlave(){
		return forSlave;
	}
	final public boolean inSlave(){
		for(AppSpecifier hosting:hostings)if(hosting.forSlave)return true;
		return false;
	}
	@Override
	public String toString(){
		return Debug.info(this)+" inSlave="+inSlave();
	}
}