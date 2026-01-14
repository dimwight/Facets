package applicable.treetext;
import static facets.facet.app.FacetPreferences.*;

import facets.core.app.FeatureHost;
import facets.core.app.PagedContenter;
import facets.core.app.SContenter;
import facets.core.superficial.app.SSurface;
import facets.facet.FacetFactory;
import facets.facet.app.FacetAppSurface;
import facets.facet.app.tree.TreeAppContenter;
import facets.facet.app.tree.TreeAppSpecifier;
import facets.util.FileSpecifier;
import facets.util.tree.XmlPolicy;

import java.io.File;
final class TreeTextSpecifier extends TreeAppSpecifier {
	public TreeTextSpecifier(){
		super(TreeTextSpecifier.class);
	}
	@Override
	protected PagedContenter[]adjustPreferenceContenters(SSurface surface,
			PagedContenter[]contenters){
		return true?contenters:new PagedContenter[]{
			contenters[PREFERENCES_VALUES],
			contenters[PREFERENCES_TRACE],
			contenters[PREFERENCES_GRAPH],
			contenters[PREFERENCES_VIEW],
		};
	}

    @Override
	public FileSpecifier[] getFileSpecifiers_(){
		return new FileSpecifier[]{
			new FileSpecifier("txt","Text lines"),
		};
	}
	@Override
	public Object getInternalContentSource_(){
		if(false)return new File("Default.txt");
		return new String[]{"First line","Second line"};
	}
	@Override
	public SContenter newContenter_(Object source, FacetAppSurface app){
		return new TreeTextContenter(source,app){};
	}

	public static void main(String[]args){
		new TreeTextSpecifier().buildAndLaunchApp(args);
	}

	@Override
	protected FacetAppSurface newApp(FacetFactory ff, FeatureHost host_){
		return new FacetAppSurface(this,ff){
			@Override
			public FileSpecifier[]getFileSpecifiers(){
				return new FileSpecifier[]{
					new FileSpecifier("txt","Text lines"),
				};
			}
			@Override
			protected Object getInternalContentSource(){
				if(false)return new File("Default.txt");
				return new String[]{"First line","Second line"};
			}
			@Override
			protected SContenter newContenter(Object source){
				return new TreeTextContenter(source, this){};
			}
		};
	}
}
