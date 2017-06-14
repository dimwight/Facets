package facets.facet.app.tree;
import static facets.util.tree.Nodes.*;
import facets.core.app.AppConstants;
import facets.core.app.Dialogs;
import facets.core.app.HideableHost;
import facets.core.app.NodeViewable;
import facets.core.app.PagedActionDefaults;
import facets.core.app.PagedActions;
import facets.core.app.PagedContenter;
import facets.core.app.PagedSurface;
import facets.core.app.Dialogs.Surfacer;
import facets.core.superficial.SFacet;
import facets.core.superficial.SIndexing;
import facets.core.superficial.STarget;
import facets.core.superficial.STextual;
import facets.core.superficial.app.SAreaTarget;
import facets.core.superficial.app.SContentAreaTargeter;
import facets.core.superficial.app.SSurface.WindowAppSurface;
import facets.facet.FacetFactory;
import facets.facet.FacetFactory.ComboCoupler;
import facets.facet.app.FacetAppSurface;
import facets.facet.app.FacetPagedContenter;
import facets.facet.app.FacetPagedSurface;
import facets.facet.app.FacetPagedContenter.PanelFactory;
import facets.util.tree.TypedNode;
import facets.util.tree.ValueNode;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
final class TreeTypeChange extends FacetPagedContenter{
	private static final boolean useCombo=false;
	static final String TITLE="Change Type",
		RUBRIC_CHANGE="Change type of selection to:";
	private final FacetAppSurface app;
	private final NodeViewable viewable;
	private final TypedNode selection;
	private final SIndexing types;
	private final STextual type;
	TreeTypeChange(FacetAppSurface app,NodeViewable viewable){
		super(TreeTypeChange.class.getSimpleName()+": "+viewable.title(),app.ff);
		this.app=app;
		selection=(ValueNode)(this.viewable=viewable).selection().single();
		Set<String>types=new HashSet();
		for(TypedNode each:descendants((TypedNode)viewable.framed))types.add(each.type());
		ArrayList sorter=new ArrayList(types);
		Collections.sort(sorter);
		sorter.add("[New Type]");
		this.types=new SIndexing(RUBRIC_CHANGE,sorter.toArray(),
				selection.type(),
			new ComboCoupler(){
				@Override
				public void indexedTitleEdited(String edit){
					trace(".indexedTitleEdited: edit=",edit);
				}
			});
		type=new STextual(RUBRIC_CHANGE,selection.type(),new STextual.Coupler() {
			@Override
			public boolean updateInterim(STextual t){
				return true;
			}
		});
	}
	@Override
	public void areaRetargeted(SContentAreaTargeter area){
		Object type=useCombo?types.indexed():this.type.text();
		PagedSurface.findDialogTrigger(area,AppConstants.TITLE_OK
				).setLive(!type.equals(selection.type()));
	}
	@Override
	public STarget[]lazyContentAreaElements(SAreaTarget area){
		return useCombo?new STarget[]{types}:new STarget[]{type};
	}
	@Override
	public void applyChanges(){
		selection.setValidType(useCombo?(String)types.indexed():type.text());
	}
	@Override
	public void reverseChanges(){}
	@Override
	public SFacet newContentPanel(SContentAreaTargeter t){
		return newPanelFactory(ff).newContentPanel(t);
	}
	@Override
	protected PanelFactory newPanelFactory(FacetFactory core){
		return new PanelFactory(core){
			@Override
			public SFacet newContentPanel(SContentAreaTargeter t){
				return rowPanel(t,0,10,HINT_PANEL_INSET+HINT_NONE,
					spacerTall(10),BREAK,
					useCombo?indexingDropdownList(t.elements()[0],HINT_NONE):
						textualField(t.elements()[0],15,HINT_USAGE_FORM),BREAK,
					fill()
				);
			}
		};
	}
	@Override
	public Dimension contentAreaSize(){
		return new Dimension(325,50);
	}
	static void launchDialog(TreeTypeChange tc){
		tc.app.dialogs().launchSurfaced(new Dialogs.Surfacer(){
			@Override
			public PagedSurface newSurface(String title,HideableHost host,PagedActions actions,
					PagedContenter[]contents,WindowAppSurface app){
				return new FacetPagedSurface(title,host,actions,
						contents,(FacetAppSurface)app){
					public boolean isResizable(){
						return false;
					}
				};
			}
		},TITLE,PagedActionDefaults.newOkCancel(),tc);
	}
}