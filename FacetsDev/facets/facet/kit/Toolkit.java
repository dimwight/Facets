package facets.facet.kit;
import static java.lang.System.*;
import facets.core.app.AppSpecifier;
import facets.core.app.AppWindowHost;
import facets.core.app.FeatureHost;
import facets.core.app.HideableHost;
import facets.core.app.SView;
import facets.core.app.Dialogs.ExceptionTexts;
import facets.core.app.StatefulViewable.ClipperSource;
import facets.core.superficial.SFacet;
import facets.core.superficial.SNumeric;
import facets.core.superficial.app.SSurface.WindowAppSurface;
import facets.facet.AreaFacets.PaneLinking;
import facets.facet.FacetFactory;
import facets.facet.ViewerAreaMaster;
import facets.facet.app.FacetAppSurface;
import facets.facet.kit.KButton.Type;
import facets.facet.kit.KWrap.ItemSource;
import facets.util.NumberPolicy;
import facets.util.StringFlags;
import facets.util.app.AppValues;
import facets.util.app.ProvidingCache;
import facets.util.tree.ValueNode;
/**
Represents the GUI context of a {@link FacetFactory}. 
<p>{@link Toolkit} defines an abstract API for {@link SFacet}s created by {@link FacetFactory} to 
request concrete GUI elements.  
 */
public interface Toolkit{
	int inAcross=8,inTop=34;
	String KEY_TAB_CLOSE_LO="TabCloseButtonLo",KEY_TAB_CLOSE_LIVE="TabCloseButtonLive",
		KEY_TAB_CLOSE_FIRE="TabCloseButtonHi";
	boolean inWindows=getProperty("os.name").indexOf("Windows")>-1,
			inWindows7=inWindows&&getProperty("os.name").indexOf("7")>-1;
	void adjustMenuMnemonics(KWrap[]items);
	ClipperSource statefulClipperSource(boolean useSystemClipboard);
	KList dropdownList(KitFacet facet,boolean asCombo, StringFlags hints);
	KWrap filler(KitFacet facet);
	KWrap label(KitFacet facet,String title,StringFlags hints);
	KWrap menu(KitFacet facet,String title,
			ItemSource itemSource,StringFlags hints);
	KMount switchMount(KitFacet facet,KWrap[]items, StringFlags hints);
	KMount spreadMount(KitFacet facet, boolean inset);
	KMount splitMount(KitFacet facet,boolean wide,SNumeric ratio);
	KMount appMultiMount(KitFacet facet,FacetAppSurface app);
	KWrap ribbonTab(KitFacet tab, KitFacet[] panels);
	KMount rowMount(KitFacet facet,int hgap,int vgap,
			StringFlags hints);
	HideableHost newOrphanDialogHost(AppValues values);
	KMount packedMount(KitFacet facet, StringFlags hints);
	KTargetable sliderPanel(KitFacet facet,
			int width,KWrap label,KWrap box,StringFlags hints);
	KWrap spacer(KitFacet facet,int width,int height);
	KField textField(KitFacet facet,int cols,StringFlags hints);
	KField numberField(KitFacet facet,NumberPolicy policy,
			StringFlags hints);
	KWrap viewerTabs(SFacet[]viewers);
	KWrap wrapMount(KitFacet facet,KWrap[]contents,
			int hgap,int vgap,StringFlags hints);
  KWrap nudgersPanel(KitFacet facet,KWrap[]buttons,KWrap[]boxes,
  		KWrap[]labels, StringFlags hints);
  KField colorShader(KitFacet facet,StringFlags hints);
	KMount areaTabs(SFacet[]areas,StringFlags hints);
	KMount paneTabs(SFacet[]areas,SFacet active,KWrap control);
	double layoutFactor();
	KList listPane(KitFacet facet,int width,int rows);
	KList listPaneMultiple(KitFacet facet,int width,int rows);
	KList listPaneChecked(KitFacet facet,int width,int rows);
	KMount tabMount(KWrap[]items,String[]titles);
	KField textLabel(KitFacet facet,StringFlags hints);
	KViewer masteredViewer(KitFacet facet,ViewerAreaMaster vam,SView view,
			ValueNode stateNode);
	void warningCritical(ExceptionTexts tt,Exception e,boolean inOpen);
	FeatureHost newAppletHost(int width, int height, String name);
	KButton button(KitFacet facet,Type type,int usage,String title,
			StringFlags hints);
	KWrap menu(KitFacet facet,String title,KWrap[] items,StringFlags hints);
	void readDecorationValues(AppValues values);
	void setCache(ProvidingCache cache);
	AppWindowHost newWindowHost(WindowAppSurface app,AppSpecifier spec);
	KMount hideMount(KitFacet facet);
	KMount paneLinksGroup(PaneLinking panes);
}