package applicable.treetext;
import static facets.facet.app.FacetPreferences.*;

import facets.core.app.PagedContenter;
import facets.core.app.SContenter;
import facets.core.superficial.app.SSurface;
import facets.facet.app.FacetAppSurface;
import facets.facet.app.tree.TreeAppSpecifier;
import facets.util.FileSpecifier;

import java.io.File;
public abstract class TreeTextView extends TreeAppSpecifier {
	public TreeTextView(){
		super(TreeTextView.class);
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
	public Object getInternalContentSource(){
		if(false)return new File("Default.txt");
		return new String[]{"First line","Second line"};
	}

	public static void main(String[]args){
		new TreeTextView(){}.buildAndLaunchApp(args);
	}

	@Override
	protected SContenter newContenter(Object source, FacetAppSurface app) {
		return new TreePlusContenter(source, app) {};
	}

	@Override
	public FileSpecifier[] fileSpecifiers() {//protected
		return new FileSpecifier[]{
				new FileSpecifier("txt", "Text lines"),
		};
	}
}
