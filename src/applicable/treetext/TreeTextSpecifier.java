package applicable.treetext;
import static facets.facet.app.FacetPreferences.*;
import facets.core.app.FeatureHost;
import facets.core.app.PagedContenter;
import facets.core.app.SContenter;
import facets.core.superficial.app.SSurface;
import facets.facet.FacetFactory;
import facets.facet.app.FacetAppSpecifier;
import facets.facet.app.FacetAppSurface;
import facets.util.FileSpecifier;
import facets.util.tree.NodeList;
import facets.util.tree.TypedNode;
import facets.util.tree.ValueNode;
import facets.util.tree.XmlPolicy;
import facets.util.tree.XmlSpecifier;
import java.io.File;
public abstract class TreeTextSpecifier extends FacetAppSpecifier{
	public TreeTextSpecifier(Class appClass){
		super(appClass);
	}
	@Override
	public PagedContenter[]adjustPreferenceContenters(SSurface surface,
			PagedContenter[]contenters){
		return true?contenters:new PagedContenter[]{
			contenters[PREFERENCES_VALUES],
			contenters[PREFERENCES_TRACE],
			contenters[PREFERENCES_GRAPH],
			contenters[PREFERENCES_VIEW],
		};
	}
	@Override
	public boolean headerIsRibbon(){
		return args().getOrPutBoolean(ARG_RIBBON,false);
	}
	@Override
	final protected FacetAppSurface newApp(FacetFactory ff,FeatureHost host){
		return new FacetAppSurface(this,ff){
			@Override
			public FileSpecifier[]getFileSpecifiers(){
				return TreeTextSpecifier.this.fileSpecifiers();
			}
			@Override
			protected Object getInternalContentSource(){
				return((TreeTextSpecifier)spec).getInternalContentSource();
			}
			@Override
			protected SContenter newContenter(Object source){
				return TreeTextSpecifier.this.newContenter(source,this);
			}
		};
	}
	public FileSpecifier[]fileSpecifiers(){//protected
		return new FileSpecifier[]{
			new FileSpecifier("txt","Text lines"),
		};
	}
	protected Object getInternalContentSource(){
		if(false)return new File("Default.txt");
		return new String[]{"First line","Second line"};
	}
	protected TreeTextContenter newContenter(Object source,FacetAppSurface app){
		return new TreeTextContenter(source,app){};
	}
	public static void main(String[]args){
		new TreeTextSpecifier(TreeTextSpecifier.class){}.buildAndLaunchApp(args);
	}
}
