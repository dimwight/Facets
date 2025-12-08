package apps;

import applicable.textart.TextArtContenter;
import applicable.textart.TextArtFeatures;
import facets.core.app.FeatureHost;
import facets.core.app.PagedContenter;
import facets.core.app.SContenter;
import facets.core.superficial.app.SSurface;
import facets.facet.FacetFactory;
import facets.facet.app.FacetAppSpecifier;
import facets.facet.app.FacetAppSurface;

import static facets.facet.app.FacetPreferences.*;

public class TextArt extends FacetAppSpecifier {
    public TextArt() {
        super(TextArt.class);
    }

    @Override
    public PagedContenter[] adjustPreferenceContenters(SSurface surface,
                                                       PagedContenter[] contenters) {
        return false ? contenters : new PagedContenter[]{
                contenters[PREFERENCES_TRACE],
                contenters[PREFERENCES_GRAPH],
				contenters[PREFERENCES_VALUES],
                contenters[PREFERENCES_VIEW],
        };
    }

    @Override
    public boolean isFileApp() {
        return false;
    }

    public boolean canCreateContent() {
        return false;
    }

    @Override
    protected FacetAppSurface newApp(FacetFactory ff, FeatureHost host) {
        return new FacetAppSurface(this, ff) {
            @Override
            protected Object getInternalContentSource() {
                return applicable.textart.TextArt.LINES_SOURCE;
            }

            @Override
            public SContenter newContenter(Object source) {
                return new TextArtContenter(source, this,
                        new TextArtFeatures.AdvanceFacets(ff),
                        null,
                        null,
                        null);
            }
        };
    }
    public static void main(String[] args){
        new TextArt().buildAndLaunchApp(args);
    }

}
