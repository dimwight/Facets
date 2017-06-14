package facets.util.shade;
import facets.util.Debug;
import facets.util.Objects;
import facets.util.Util;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;
/**
Extensible, sortable set of {@link Shade}s. 
 */
public class ShadeSet{
	/**Sort index. */
	final public static int SORT_TITLE = 0;
	final private static int SORTS_HSB = SORT_TITLE + 1,SORTS_RGB = SORTS_HSB + 3;
	/**Sort index. */
	final public static int SORT_SNAP = SORTS_RGB + 3,
		SORT_HUE = SORTS_HSB + 0, SORT_SATURATION = SORTS_HSB + 1,
		SORT_BRIGHTNESS = SORTS_HSB + 2, SORT_RED = SORTS_RGB + 0,
		SORT_GREEN = SORTS_RGB + 1, SORT_BLUE = SORTS_RGB + 2,SORTS=SORTS_RGB + 3;
	final private Comparator sorter = new Comparator() {
		public int compare(Object o1, Object o2) {
			Shade shade1 = (Shade) o1, shade2 = (Shade) o2;
			double h1, h2;
			if (sort == SORT_TITLE)
				return shade1.compareTo(shade2);
			if (sort < SORTS_RGB) {
				h1 = shade1.valuesHSB()[sort - SORTS_HSB];
				h2 = shade2.valuesHSB()[sort - SORTS_HSB];
			} else if (sort < SORT_SNAP) {
				h1 = shade1.valuesRGB()[sort - SORTS_RGB];
				h2 = shade2.valuesRGB()[sort - SORTS_RGB];
			} else {
				int snapBits = snapBits();
				h1 = shade1.snapRGB(snapBits);
				h2 = shade2.snapRGB(snapBits);
			}
			return h1 > h2 ? -1 : h1 == h2 ? 0 : 1;
		}
	};
	private final Map<Object,Shade>reference;
	private int sort = SORT_TITLE;
	private final SortedMap<String,Shade> working;
	/**
	Constructor for public API. 
	@param master has its reference set shared by the instance, which creates 
	its own working set. 
	<p>A suitable master is {@link Shades#HTML_SET} which can be shared between
	instances. 
	 */
	public ShadeSet(ShadeSet master){this(null,master);}
	/**
	Core constructor, not public API.
	<p>One of <code>shades</code> or <code>master</code> may be <code>null</code>.  
	@param shades if non-<code>null</code> and <code>master</code> is <code>null</code>
	are stored as an internal reference set, 
	with duplicates (by either colour or title) removed
	@param master if non-<code>null</code> shares its reference set 
	with the new instance; in which case <code>shades</code> are ignored. 
	<p>In either case an extensible working set is initialised from the reference set;
	it is this working set that is accessible via <code>shades</code>. 
	<p>This constructor is not public API and should be regarded
	as package-private except for documentation purposes. 
	 */
	protected ShadeSet(Shade[]shades,ShadeSet master){
	  if(master==null){
	    if(shades==null)throw new IllegalArgumentException("Null shades in "+Debug.info(this));
			shades=Objects.uniqued(Shade.class,shades);
			reference=new HashMap();
			int duplicates=0,snapBits=snapBits();
			for(int i=0;i<shades.length;i++){
				Integer snapKey=new Integer(shades[i].snapRGB(snapBits));
		  	Shade snapCheck=reference.get(snapKey);
			  if(snapCheck==null){
			        reference.put(snapKey,shades[i]);
			        reference.put(shades[i].title(),shades[i]);	          
			  }
			  else{
			    if(false)Util.printOut("Snap key for ", shades[i]+
			      		" already used for " +snapCheck+" in "+Debug.info(this));
			    else duplicates++;
			  }
			}      
			if(false)Util.printOut("ShadeSet: ", shades.length+
				" input, " +reference.values().size()/2+" stored, " 
		    +duplicates+" duplicate snap keys");        
	  }
	  else reference=master.reference;        
	  working=new TreeMap();
	  Map.Entry[]entries=reference.entrySet().toArray(new Map.Entry[]{});
	  for(int i=0;i<entries.length;i++){
	    Object key=entries[i].getKey();
	    if(key instanceof String)working.put((String)key,reference.get(key));
	  }
	}
	/**
	Adds <code>shade</code> to the working set, 
	providing no equivalent shade is already stored.
	<p>Any shade of the same title is replaced by <code>shade</code>.     
	@param shade must be non-<code>null</code>. 
	@return <code>shade</code> if successful, otherwise the existing equivalent shade. 
	 */
	final public Shade addShade(Shade shade){
	  if(shade==null)throw new IllegalArgumentException("Null shade in "+Debug.info(this));
	  Shade[]shades=shades();
	  for(int i=0;i<shades.length;i++)if(shades[i].equals(shade)){
	    String title=shades[i].title();
	    if(isAddedShade(shade))working.remove(title);
	    else shade=shades[i];
	  }
	  working.put(shade.title(),shade);
	  return shade;
	}
	/**
	 True if <code>shade</code> is not a member of the internal reference set.  
	 @param shade must be non-<code>null</code>
	 */
	final public boolean isAddedShade(Shade shade){
		return reference.get(shade.title())==null;
	}
	/**
	Sets the sort used by <code>shades</code>. 
	@param sort should be one of the <code>SORT_XXX</code> constants. 
	 */
	final public void setSort(int sort){
		this.sort=sort;
		if(sort<SORT_TITLE||sort>SORTS)
			throw new IllegalArgumentException("Bad sort in "+Debug.info(this));
	}
	/**
	The sorted contents of the working set of {@link Shade}s. 
	 */
	final public Shade[]shades(){
		List<Shade>sortable=new ArrayList(working.values());
	  Collections.sort(sortable,sorter);
	  return Objects.newTyped(Shade.class,sortable.toArray());
	}
	/**
	Checks whether <code>shade</code> can be 'snapped' to an existing shade. 
	<p>Returns the existing shade if found, otherwise <code>shade</code>. 
	 @param shade must be non-<code>null</code>
	 */
	final public Shade snapShade(Shade shade){
	  Shade snap=reference.get(new Integer(shade.snapRGB(snapBits())));
	  return snap!=null?snap:shade;
	}
	/**
	The sort used by <code>shades</code>. 
	<p>Sort will be one of the <code>SORT_XXX</code> constants. 
	 */
	final public int sort(){return sort;}
	/**
	The bit length to be used when 'snapping' new to existing shades. 
	<p>Default is 9.
	@see facets.util.shade.Shade#snapRGB(int)  
	 */
	protected  int snapBits(){return 9;}
}
  