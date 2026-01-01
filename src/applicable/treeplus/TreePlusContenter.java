package applicable.treeplus;

import facets.core.app.AreaRoot;
import facets.core.app.ViewerContenter;
import facets.facet.AreaFacets;
import facets.facet.app.FacetAppSurface;

public abstract class TreePlusContenter extends ViewerContenter {
    protected final FacetAppSurface app;

    public TreePlusContenter(Object source, FacetAppSurface app) {
        super(source);
        this.app=app;
    }

    @Override
    protected void attachContentAreaFacets(AreaRoot area){
        app.ff.areas().attachViewerAreaPanes(area,"", AreaFacets.PANE_SPLIT_VERTICAL);
    }
}
