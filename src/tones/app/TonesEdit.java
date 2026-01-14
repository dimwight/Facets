package tones.app;

import applicable.treetext.TreeTextContenter;
import applicable.treetext.TreeTextFeatures;
import applicable.treetext.TreeTextViewable;
import facets.core.app.AppSurface.ContentStyle;
import facets.core.app.*;
import facets.core.superficial.*;
import facets.core.superficial.STarget.Targeted;
import facets.facet.AreaFacets;
import facets.facet.FacetFactory;
import facets.facet.SwingPanelFacet;
import facets.facet.ViewerAreaMaster;
import facets.facet.app.FacetAppSurface;
import facets.facet.app.tree.TreeAppContenter;
import facets.facet.app.tree.TreeAppSpecifier;
import facets.util.FileSpecifier;
import facets.util.ItemList;
import facets.util.Util;
import facets.util.tree.DataNode;
import facets.util.tree.XmlPolicy;
import tones.bar.VoicePart;
import tones.view.PageView;

import javax.swing.*;
import java.io.File;

import static facets.core.app.AppSurface.ContentStyle.*;
import static facets.facet.FacetFactory.*;
public final class TonesEdit extends TreeTextContenter{
  public static final String ARG_BAR_FROM="barFrom",
          ARG_RESCALE="rescale",
          ARG_TREE="tree";

  public TonesEdit(Object source,FacetAppSurface app){
    super(source,app);
  }
  //@Override
  protected TreeTextViewable newViewable(DataNode data){
    return new TonesViewable(data,
            app.ff.statefulClipperSource(false),app);
  }
  //@Override
  protected SFrameTarget[]newViewTargets(TreeView debugTree,boolean liveViews){
    TonesViewable viewable=(TonesViewable)this.contentFrame();
    int barFrom=app.spec.state().getOrPutInt(TonesEdit.ARG_BAR_FROM,1);
    if(viewable.bars.barCount()<barFrom)barFrom=1;
    SFrameTarget page=PageView.newFramed(8,app.spec,
        viewable.bars.barCount(),barFrom);
    return new SFrameTarget[]{page,
        viewable.barsView,
        new SFrameTarget(debugTree)};
  }
  @Override
  protected void attachContentAreaFacets(AreaRoot area){
    final FacetFactory ff=app.ff;
    ViewerAreaMaster vam=new ViewerAreaMaster(){
      protected ViewerAreaMaster newChildMaster(SAreaTarget child){
        SView view = ((SViewer) child.activeFaceted()).view();
        return !(view instanceof PageView)?null
            :new ViewerAreaMaster(){
          protected SFacet newViewTools(STargeter viewTargeter){
            STargeter elements[]=viewTargeter.elements(),
                barStart=elements[PageView.TARGET_BAR];
            SwingPanelFacet lastBar=(SwingPanelFacet)ff.textualLabel(
                elements[PageView.TARGET_LAST],HINT_PANEL_CENTER);
            ((JLabel)lastBar.components().values().toArray()[0]
                ).setBorder(BorderFactory.createEmptyBorder(0,2,2,0));
            return ff.toolGroups(viewTargeter,HINT_PANEL_CENTER,ff.spacerWide(8),
                false?null:ff.spacerTall(45),
                ff.numericSliders(barStart,200,
                    HINT_SLIDER_TICKS+HINT_SLIDER_LABELS+HINT_SLIDER_LOCAL),
                ff.numericNudgeButtons(barStart,
                    HINT_NUMERIC_FIELDS+HINT_NUMERIC_NUDGERS_FIRST),
                lastBar);
          }
          protected String hintString(){
            return HINT_NO_FLASH+HINT_PANEL_BORDER+HINT_BARE;
          }
        };
      }
    };
    ff.areas().attachViewerAreaPanes(area,vam,AreaFacets.PANE_SPLIT_HORIZONTAL);
  }
  //@Override
  protected TreeTextFeatures newFeatures(SContentAreaTargeter area){
    final STargeter selection=area.selection(),
        code=true?null: selection.elements()[0];
    return new TreeTextFeatures(app,area){
      @Override
      protected SFacet[]adjustToolbarItems(ItemList<SFacet>facets){
        if (true) return facets.items();
        if(false)facets.clear();
        facets.add(0,rowPanel(selection,textualField(code,45,HINT_NONE)));
        return facets.items();
      }
    };
  }
  @Override
  public void wasAdded(){
    STarget[]viewers=app.activeContentTargeter().areaTarget().indexableTargets();
    if(viewers.length==1)return;
    SAreaTarget debug=(SAreaTarget)viewers[1];
    SIndexing expand=(SIndexing)((Targeted)debug.activeFaceted().attachedFacet()
        ).targets()[0];
    expand.setIndex(3);
  }

//  @Override
  public FileSpecifier[] sinkFileSpecifiers() {
    return new FileSpecifier[]{
            new FileSpecifier("tones.xml","Tones tree"),
            new FileSpecifier("tones.txt", "Tones"),
    };
  }

  public static void main(String[]args){
    new TonesSpecifier().buildAndLaunchApp(args);
  }

  private static class TonesSpecifier extends TreeAppSpecifier {
    public TonesSpecifier() {
      super(TonesEdit.class);
    }

    //      @Override
          public ContentStyle contentStyle() {
            return true?
                    values()[args().getOrPutInt(NATURE_KEY,0)]
                    :DESKTOP;
          }

    @Override
    public boolean isFileApp() {
      return false;
    }

    @Override
    public Object getInternalContentSource_() {
      File runDir = Util.runDir(),
          txt = new File(runDir, "E major.tones.txt"),
          xml = new File(runDir, "E major.tones.xml");
      boolean useTree=args().getBoolean(ARG_TREE);
      File use = useTree ? xml : txt;
      return use.exists() ? use : VoicePart.TEST_CODES;
    }

    @Override
    public TreeTextContenter newContenter_(Object source, FacetAppSurface app) {
      return new TonesEdit(source, app);
    }

      @Override
      protected FacetAppSurface newApp(FacetFactory ff,FeatureHost host_){
          return new FacetAppSurface(this,ff){
              @Override
              public FileSpecifier[]getFileSpecifiers(){
                return xmlPolicy().fileSpecifiers();
              }
              @Override
              protected Object getInternalContentSource(){
                File runDir = Util.runDir(),
                    txt = new File(runDir, "E major.tones.txt"),
                    xml = new File(runDir, "E major.tones.xml");
                boolean useTree=args().getBoolean(ARG_TREE);
                File use = useTree ? xml : txt;
                return use.exists() ? use : VoicePart.TEST_CODES;
              }
              @Override
              protected SContenter newContenter(Object source){
                return new TonesEdit(source, this);
              }
          };
      }
  }
}
