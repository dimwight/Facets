package facets.facet.app.tree;
import static facets.facet.app.tree.TreeTargets.*;
import static facets.util.Strings.*;
import static facets.util.tree.Nodes.*;
import facets.core.app.Dialogs;
import facets.core.app.HideableHost;
import facets.core.app.NodeViewable;
import facets.core.app.PagedActionDefaults;
import facets.core.app.PagedActions;
import facets.core.app.PagedContenter;
import facets.core.app.PagedSurface;
import facets.core.app.PathSelection;
import facets.core.app.Dialogs.Surfacer;
import facets.core.superficial.Notice;
import facets.core.superficial.SFacet;
import facets.core.superficial.SIndexing;
import facets.core.superficial.STarget;
import facets.core.superficial.Notifying.Impact;
import facets.core.superficial.SIndexing.Coupler;
import facets.core.superficial.app.SAreaTarget;
import facets.core.superficial.app.SContentAreaTargeter;
import facets.core.superficial.app.SSurface.WindowAppSurface;
import facets.facet.FacetFactory;
import facets.facet.app.FacetAppSurface;
import facets.facet.app.FacetPagedContenter;
import facets.facet.app.FacetPagedSurface;
import facets.facet.app.FacetPagedContenter.PanelFactory;
import facets.util.IndexingIterator;
import facets.util.OffsetPath;
import facets.util.Strings;
import facets.util.tree.DataNode;
import facets.util.tree.NodePath;
import facets.util.tree.TypedNode;
import facets.util.tree.ValueNode;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
final class TreeSearch extends FacetPagedContenter{
	private static final int MAX_CHARS=50;
	private final FacetAppSurface app;
	private final NodeViewable viewable;
	private final TypedNode tree;
	private final TypedNode selection;
	private final List<OffsetPath>searchStore,searchCopy=new ArrayList(),
		resultStore=new ArrayList();
	private final SIndexing types,titles,keys,values,results=new SIndexing(TITLE_SEARCH_RESULTS,
			new Coupler(){
		@Override
		public Object[]getIndexables(){
			resultStore.clear();
			for(final TypedNode node:descendants(tree)){
				if((types.index()!=0&&!node.type().equals(types.indexed()))
					||(titles.index()!=0&&!node.title().equals(titles.indexed())))continue;
				if(keys.index()==0&&values.index()==0){
					addResultPath(node,-1);
					continue;
				}
				new IndexingIterator<String>(((DataNode)node).values()){
					@Override
					protected void itemIterated(String value,int at){
						if(isKeyPair(value)){
							String[]pair=splitPair(value);
							if(keys.index()!=0&&keys.indexed().equals(pair[0]))
								addResultPath(node,at);
							else if(keys.index()==0&&values.index()!=0&&values.indexed().equals(pair[1]))
								addResultPath(node,at);
						}
						else if(values.index()!=0&&values.indexed().equals(value))
							addResultPath(node,at);
					}
				}.iterate();
			};
			StringBuilder indexables=new StringBuilder();
			for(OffsetPath result:resultStore){
				for(TypedNode node:ancestry((TypedNode)result.target(tree))) 
					indexables.append((node.parent()==null?"":">")+
							node.type()+":"+node.title());
				indexables.append("\n");
			}
			return indexables.toString().split("\n");
		}
		void addResultPath(TypedNode node,int valueAt){
			resultStore.add(new NodePath(ancestry(node)).valueAtChecked(valueAt));
		}
		@Override
		public void indexSet(SIndexing i){
			if(resultStore.size()==0)return;
			viewable.defineSelection(new PathSelection(viewable.framed,resultStore.get(i.index())));
			app.notify(new Notice(i,Impact.SELECTION));
		}
	});
	TreeSearch(FacetAppSurface app,NodeViewable viewable,List<OffsetPath>searchStore){
		super(TreeSearch.class.getSimpleName()+": "+viewable.title(),app.ff);
		this.app=app;
		this.searchStore=searchStore;
		searchCopy.addAll(searchStore);
		selection=(ValueNode)(this.viewable=viewable).selection().single();
		tree=(TypedNode)viewable.framed;
		Set<String>types=new HashSet(),titles=new HashSet(),keys=new HashSet(),
			values=new HashSet();
		for(TypedNode each:descendants(tree)){
			types.add(trimToLength(each.type(),MAX_CHARS));
			titles.add(trimToLength(each.title(),MAX_CHARS));
			for(String value:((DataNode)each).values()){
				if(isKeyPair(value)){
					String[]pair=splitPair(value);
					keys.add(trimToLength(pair[0],MAX_CHARS));
					if(pair.length>1)values.add(trimToLength(pair[1],MAX_CHARS));
				}
				else values.add(trimToLength(value,MAX_CHARS));
			}
		}
		Coupler coupler=new Coupler(){
			@Override
			public void indexSet(SIndexing i){
				results.setIndex(0);
			}
		};
		this.types=TreeSearch.newFieldIndexing(types,coupler,TITLE_SEARCH_TYPE);
		this.titles=TreeSearch.newFieldIndexing(titles,coupler,TITLE_SEARCH_TITLE);
		this.keys=TreeSearch.newFieldIndexing(keys,coupler,TITLE_SEARCH_KEY);
		this.values=TreeSearch.newFieldIndexing(values,coupler,TITLE_SEARCH_VALUE);
	}
	@Override
	public STarget[]lazyContentAreaElements(SAreaTarget area){
		return new STarget[]{types,titles,keys,values,results};
	}
	@Override
	public void applyChanges(){
		viewable.defineSelection(new PathSelection(viewable.framed,resultStore.get(results.index())));
		searchStore.clear();
		searchStore.addAll(resultStore);
	}
	@Override
	public void reverseChanges(){
		searchStore.clear();
		searchStore.addAll(searchCopy);
	}
	@Override
	public SFacet newContentPanel(SContentAreaTargeter t){
		return newPanelFactory(ff).newContentPanel(t);
	}
	@Override
	protected PanelFactory newPanelFactory(FacetFactory core){
		return new PanelFactory(core){
			@Override
			public SFacet newContentPanel(SContentAreaTargeter t){
				return rowPanel(t,0,5,HINT_PANEL_INSET,
					rowPanel(t,0,5,HINT_GRID,
						indexingDropdownList(t.elements()[0],HINT_NONE),BREAK,
						indexingDropdownList(t.elements()[1],HINT_NONE),BREAK,
						fill()
					),
					spacerWide(20),
					rowPanel(t,0,5,HINT_GRID,
						indexingDropdownList(t.elements()[2],HINT_NONE),BREAK,
						indexingDropdownList(t.elements()[3],HINT_NONE),BREAK,
						fill()
					),
					BREAK,
					indexingPaneSingle(t.elements()[4],-0,1,HINT_SPREAD)
				);
			}
		};
	}
	@Override
	public Dimension contentAreaSize(){
		return new Dimension(350,200);
	}
	private static SIndexing newFieldIndexing(Set<String>set,SIndexing.Coupler coupler,
			String fieldName){
		ArrayList sorter=new ArrayList(set);
		Collections.sort(sorter);
		sorter.add(0,"[Any "+fieldName.replace(TS,"")+"]");
		return new SIndexing(fieldName,sorter.toArray(),0,coupler);
	}
	static void launchDialog(TreeSearch search){
		search.app.dialogs().launchSurfaced(new Dialogs.Surfacer(){
			@Override
			public PagedSurface newSurface(String title,HideableHost host,PagedActions actions,
					PagedContenter[]contents,WindowAppSurface app){
				return new FacetPagedSurface(title,host,actions,
						contents,(FacetAppSurface)app){
					public boolean isResizable(){
						return true;
					}
				};
			}
		},TITLE_SEARCH_FIND,PagedActionDefaults.newOkCancel(),search);
	}
}