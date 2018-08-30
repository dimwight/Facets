package apps.idiom;
import static facets.core.app.AppConstants.*;
import static facets.facet.FacetFactory.*;
import static facets.facet.app.FacetAppActions.BarHide.*;
import facets.core.app.FeatureHost;
import facets.core.app.AppSurface.ContentStyle;
import facets.facet.FacetFactory;
import facets.facet.app.FacetAppActions;
import facets.facet.app.FacetAppSpecifier;
import facets.facet.app.FacetAppSurface;
import facets.facet.app.FacetConstants;
import facets.facet.app.FacetAppActions.BarHide;
import facets.facet.kit.*;
import facets.facet.kit.swing.KitSwing;
import facets.util.TypesKey;
import facets.util.app.HostBounds;
import facets.util.tree.Nodes;
import facets.util.tree.ValueNode;
import applicable.textart.TextArtConstants;
/**
{@link FacetAppSpecifier} defining demo application. 
 */
final public class SpikeApp extends FacetAppSpecifier {

	private final boolean minimal=false;

	/** Application-specific key. */		
	static final String NATURE_CODE_TREE = "codeWindows";
	
	SpikeApp() {
		
		//Pass suitable title to superclass
		super(SpikeApp.class);
	}
	
	@Override
	public ContentStyle contentStyle(){
		return minimal?ContentStyle.SINGLE:ContentStyle.DESKTOP;
	}

	@Override
	public boolean hasSystemAccess(){
		
		//Causes WebStart crash if true
		return true;
	}

	@Override
	protected void addNatureDefaults(ValueNode root) {
		
		//Merge with values to overwrite
		super.addNatureDefaults(root);
		Nodes.mergeContents(root,new Object[]{
			
			//Suitable for overwriting (also as arguments to main)
			HostBounds.NATURE_SIZER_DEFAULT+"=0, 0, 700 ,600",
			HostBounds.NATURE_SIZE_MIN + "=700, 700",
			NATURE_CONFIRM_CLOSE + "=" + false,
			NATURE_OPEN_EMPTY + "=" + false,
			NATURE_CODE_TREE + "=" + false,
			NATURE_RUN_WATCHED + "=" + false,
			NATURE_ICON_PATH + "=_image/jfgr"
		});
	}

	@Override
	public void adjustValues(){
		state(PATH_DEBUG).put(KEY_DRAG_NOTIFY,true);
		super.adjustValues();
	}
	
	@Override
	protected void addStateDefaults(ValueNode root){
		
		//Merge with values to overwrite
		super.addStateDefaults(root);
		Nodes.mergeContents(Nodes.guaranteedDescendant(root,PATH_APP),
				new Object[]{
			BarHide.key(TypesKey.EMPTY)+"=0,0,0"
		});
	}
	
	@Override
	public Object[][] decorationValues(){
		
		//Merge with superclass values
		return joinDecorations(super.decorationValues(),
				TextArtConstants.DECORATION_VALUES);
	}

	@Override
	public boolean canCreateContent(){
		return true;
	}
	
	@Override
	public boolean canEditContent(){
		return true;
	};
	
	@Override
	public boolean isFileApp(){
		return false;
	}
	
	public Toolkit newToolkit(){
		return new KitSwing(false,false,true||nature().getBoolean(FacetConstants.NATURE_SWING_SYSTEM));
	}

	@Override
	protected FacetAppSurface newApp(FacetFactory ff, FeatureHost host){
		//Define app surface
		return new SpikeAppSurface(this,ff,minimal);
	}

	/**
	Configures, creates and opens desktop application surface. 
	@param args can override nature values
	 */
	public static void main(String[] args) {
		
		//Create specifier to launch app
		new SpikeApp().buildAndLaunchApp(args);
	}
}