package applicable.field;
import static facets.core.app.AppConstants.*;
import facets.core.app.AppConstants;
import facets.core.superficial.SIndexing;
import facets.util.Debug;
import facets.util.Util;
import java.util.Arrays;
import java.util.List;
abstract class ViewMoveCoupler extends SIndexing.Coupler{
	public final SIndexing indexing;
	private int lastAt=0;
	private List contents;
	public ViewMoveCoupler(){
		indexing=new SIndexing("Move",this);
	}
	@Override
	final public String[]iterationTitles(SIndexing i){
		return new String[]{TITLE_UP,TITLE_DOWN};
	}
	@Override
	final public Object[]getIndexables(){
		return contents.toArray();
	}
	@Override
	public void indexSet(SIndexing i){
		int at=i.index();
		if(at==lastAt)return;
		else if(contents==null)throw new IllegalStateException(
				"Null contents in "+Debug.info(this));
		Object items[]=contents.toArray(),toMove=items[lastAt];
		items[lastAt]=items[at];
		items[at]=toMove;
		contents.clear();
		contents.addAll(Arrays.asList(items));
		indexing.setIndex(lastAt=at);
		updateMovedSelection(toMove);
	}
	protected abstract void updateMovedSelection(Object moved);
	public final void updateToContents(List contents){
		int selectedAt=contentsSelectedAt(this.contents=contents);
		if(selectedAt<0)throw new IllegalStateException(
				"Empty contents in "+Debug.info(this));
		else if(indexing.index()!=selectedAt)indexing.setIndex(lastAt=selectedAt);
	}
	protected abstract int contentsSelectedAt(List contents);
}
