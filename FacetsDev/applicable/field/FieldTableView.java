package applicable.field;
import facets.core.app.NodeViewable;
import facets.core.app.SViewer;
import facets.core.app.TableView;
import facets.core.app.ViewerTarget;
import facets.core.superficial.SIndexing;
import facets.core.superficial.STarget;
import facets.core.superficial.SToggling;
import facets.util.Debug;
import facets.util.Objects;
import facets.util.Stateful;
import java.text.Format;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
public abstract class FieldTableView extends TableView{
	final public SIndexing newMoveIndexing(final NodeViewable viewable){
		return(mover=new ViewMoveCoupler(){
			@Override
			protected void updateMovedSelection(Object toMove){
				if(sorts==null)throw new IllegalStateException("Null sorts in "+Debug.info(this));
				else((SIndexing)sorts[FACET_SORT_COL]).setIndex(0);
				noBaseSort=true;
				viewable.defineSelection(toMove);
			}
			@Override
			protected int contentsSelectedAt(List contents){
				return contents.indexOf(viewable.selection().single());
			}
		}).indexing;
	}
	final public void updateMover(List contents){
		mover.updateToContents(contents);
	}
	final public void resetSort(){
		sortValues="";
	}
	@Override
	protected void traceOutput(String msg){
		if(false&&title().equals("WIP"))super.traceOutput(msg);
	}
	final public void sortContent(SViewer viewer,List<Stateful>content){
		sorts=findSorts(viewer);
		SIndexing sortCol=(SIndexing)sorts[FACET_SORT_COL];
		sortAt=sortCol.index()-1;
		trace(".sortContent: sortAt=",sortAt);
		ValueField sortField=sortAt<0?null:sortables[sortAt];
		sortables=fields.liveFields();
		if(sortField!=null)for(sortAt=0;sortAt<sortables.length;sortAt++)
			if(sortables[sortAt]==sortField)break;
		if(sortAt==sortables.length)sortAt=-1;
		if(sortCol.index()!=sortAt+1)sortCol.setIndex(sortAt+1);
		boolean sortDown=((SToggling)sorts[FACET_SORT_DOWN]).isSet();
		String sortValues="[sortField:"+(sortField==null?"null":sortField.name)+",sortDown:"+sortDown+"]";
		if(this.sortValues.equals(sortValues))return;
		this.sortValues=sortValues;
		Comparator<Stateful>baseSort=baseSort();
		if(baseSort==null)throw new IllegalStateException(
				"Null baseSort in "+Debug.info(this));
		else if(!(noBaseSort&=sortAt<0))Collections.sort(content,baseSort);
		if(sortAt<0)return;
		List<FieldProxy>proxies=new ArrayList();
		for(Stateful c:content)proxies.add(fields.newProxy(c));
		final TableComparator sort=sortField.sorter(sortDown);
		Collections.sort(proxies,new TableComparator<FieldProxy>(sortDown){
			@Override
			public boolean isEmpty(FieldProxy p){
				return sort.isEmpty(p.get(sortAt));
			}
			@Override
			public int compareNonEmpties(FieldProxy p,FieldProxy q){
				return sort.compareNonEmpties(p.get(sortAt),q.get(sortAt));
			}
		});
		if(sortDown)Collections.reverse(proxies);
		content.clear();
		for(FieldProxy proxy:proxies)content.add(proxy.source);
		contentSorted(content);
	}
	protected void contentSorted(List<Stateful>content){
		trace(".sortContent: content=",content.subList(0,5));
	}
	@Override
	public String typeKey(){
		return fields.typeKey;
	}
	final public FieldSet fields;
	private STarget[]sorts;
	private ViewMoveCoupler mover;
	private ValueField[]sortables;
	private int sortAt;
	private boolean noBaseSort,sortDown;
	private String sortValues="[sortField:null,sortDown:false]";
	public FieldTableView(String title,FieldSet fields){
		super(title);
		sortables=(this.fields=fields).liveFields();
	}
	@Override
	final public String getColumnTitle(int col){
		return fields.liveFields()[col].name;
	}
	@Override
	final public Format getColumnFormat(int col){
		return fields.liveFields()[col].format();
	}
	@Override
	public Comparator getColumnSort(int col,boolean sortDown){
		return fields.liveFields()[col].sorter(sortDown);
	}
	@Override
	final public Object stateStamp(){
		return "["+Objects.toString(fields.liveFields())+"]+"+sortValues;
	}
	private STarget[] findSorts(SViewer viewer){
		return ((STarget)viewer).elements()[ViewerTarget.TARGETS_FACET].elements();
	}
	protected abstract Comparator<Stateful>baseSort();
}