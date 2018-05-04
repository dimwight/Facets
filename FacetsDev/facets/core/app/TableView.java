package facets.core.app;
import facets.core.superficial.app.SSelection;
import facets.core.superficial.app.SelectionView;
import facets.util.ArrayPath;
import facets.util.Debug;
import facets.util.OffsetPath;
import facets.util.Stateful;
import facets.util.ValueProxy;
import java.text.Format;
import java.util.Comparator;
/**
{@link SelectionView} that generates tabular viewer data. 
 */
public abstract class TableView extends SelectionView implements TypeKeyable{
	public static final int FACET_SORT_COL=0,FACET_SORT_DOWN=1;
	public TableView(String title){
		super(title);
	}
	/**
	Re-implementation that
<ol>
	<li>obtains a local  {@link PathSelection} by passing <code>viewable</code> to {@link #newTableSelection(PathSelection)}
	<li>obtains an array of data sources from {@link #getRowSources(PathSelection)}  
	<li>creates {@link ValueProxy} rows by 
	calling {@link #newRowProxy(Stateful)} for each source
	<li>returns a {@link PathSelection} of {@link ArrayPath}s created from the rows 
	and the local selection
	</ol>	
	 */
  @Override
	final public SSelection newViewerSelection(SViewer viewer,SSelection viewable){
		PathSelection table=newTableSelection((PathSelection)viewable);
		Stateful[]rowSources=getRowSources(table);
		if(rowSources==null||rowSources.length==0)throw new IllegalStateException(
				"Null or empty rowSources from "+viewable);
		ValueProxy[]rows=new ValueProxy[rowSources.length];
		for(int i=0;i<rows.length;i++)rows[i]=newRowProxy(rowSources[i]);
		OffsetPath[]rowPaths=new ArrayPath[table.paths.length];
		for(int i=0;i<rowPaths.length;i++)rowPaths[i]=new ArrayPath(table.paths[i].offsets);
		return new PathSelection(rows,rowPaths);
	}
	/**
	Return a local selection for use by {@link #newViewerSelection(SViewer, SSelection)}. 
	<p>Default implementation returns the {@link PathSelection} passed.
	@param viewable the {@link SSelection} passed to the calling method, 
	cast to a {@link PathSelection}
	 */
	protected PathSelection newTableSelection(PathSelection viewable){
		return viewable;
	}
	/**
	Return data sources for use by {@link #newViewerSelection(SViewer, SSelection)}. 
	<p>Default implementation casts the selection content to an array.
	@param table the {@link SSelection} passed to the calling method
	 */
	protected Stateful[]getRowSources(PathSelection table){
		return(Stateful[])table.content();
	}
	/**
	Create a {@link ValueProxy} that 
<ul>
<li>defines with {@link ValueProxy#valueCount()} the row length 
<li>supplies with {@link ValueProxy#get(int)} the values for each column
	</ul>	
	@param source will be the {@link ValueProxy#source} 
	 */
	protected abstract ValueProxy newRowProxy(Stateful source);
	public boolean allowMultipleSelection(){
		return false;
	}
	public boolean isColumnEditable(int col){
		return false;
	}
	public String getColumnTitle(int col){
		return "Column"+col;
	}
	public Format getColumnFormat(int col){
		return null;
	}
	public boolean allowSelectOnEdit(){
		return false;
	}
	public boolean hideHeader(){
		return false;
	}
	public Comparator getColumnSort(int col,boolean sortDown){
		return null;
	}
	public boolean sortInContent(){
		return false;
	}
	@Override
	public String typeKey(){
		return null;
	}
}
