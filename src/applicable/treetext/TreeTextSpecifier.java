package applicable.treetext;
import static facets.facet.app.FacetPreferences.*;
import facets.core.app.FeatureHost;
import facets.core.app.PagedContenter;
import facets.core.app.SContenter;
import facets.core.superficial.app.SSurface;
import facets.facet.FacetFactory;
import facets.facet.app.FacetAppSurface;
import facets.facet.app.tree.TreeAppSpecifier;
import facets.util.FileSpecifier;

import java.io.File;
final class TreeTextSpecifier extends TreeAppSpecifier {
	public TreeTextSpecifier(){
		super(TreeTextSpecifier.class);
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
				return false?((TreeAppSpecifier)spec).xmlPolicy().fileSpecifiers():
						TreeTextSpecifier.this.fileSpecifiers();
			}
			@Override
			protected Object getInternalContentSource(){
				return((TreeAppSpecifier)spec).getInternalContentSource();
			}
			@Override
			protected SContenter newContenter(Object source){
				return TreeTextSpecifier.this.newContenter(source,this);
			}
		};
	}
	protected FileSpecifier[]fileSpecifiers(){
		return new FileSpecifier[]{
			new FileSpecifier("txt","Text lines"),
		};
	}
	public Object getInternalContentSource(){
		if(false)return new File("Default.txt");
		return new String[]{"First line","Second line"};
	}
	protected TreeTextContenter newContenter(Object source,FacetAppSurface app){
		return new TreeTextContenter(source,app){};
	}
	public static void main(String[]args){
		new TreeTextSpecifier().buildAndLaunchApp(args);
	}
}
