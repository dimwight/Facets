package facets.core.app;
import facets.core.superficial.Notifiable;
import facets.core.superficial.SIndexing;
import facets.core.superficial.SNumeric;
import facets.core.superficial.STarget;
import facets.core.superficial.STargeter;
import facets.core.superficial.STextual;
import facets.core.superficial.SToggling;
import facets.util.Debug;
import facets.util.NumberPolicy;
import facets.util.Objects;
import facets.util.Stateful;
import facets.util.Titled;
import facets.util.Tracer;
import facets.util.Util;
import facets.util.tree.ValueNode;
import java.util.ArrayList;
import java.util.List;
/**
Encapsulates common operations on {@link ValueNode} content. 
<ul>
<li>Constructed around both its content and a deep working copy of that 
content (passed in rather than created internally to allow sharing between 
contenters). 
<li>Creates a further backup copy of its content so that its 
its original state can be restored even after updating from the working copy.
<li>Provides convenience methods to create targets from working copy values, 
with couplers feeding into a general purpose update method. 
</ul>
 */
public abstract class ValueContent extends Tracer implements Titled{
	/**Content passed to the constructor, working and backup copies.*/
	public final ValueNode working;
	private final ValueNode master,copy;
	private final String title;
	private final boolean direct;
	/**
	Unique constructor. 
	@param master values to be updated with any changes  
	@param working stored as {@link #working}; 
	must not be <code>master</code> but must be state equal ie a deep copy; should be 
	shared with any other active {@link ValueContent}s updating the same master 
	 */
	public ValueContent(String title,ValueNode master,ValueNode working){
		this.title=title;
		this.master=master;
		direct=master==working;
		if(!direct&&!master.stateEquals(working))throw new IllegalArgumentException(
				"Unequal states in "+Debug.info(this)+":\n"+master+"\n"+working);
		this.working=direct?master:working;
		copy=(ValueNode)master.copyState();
	}
	/**
	Does the working copy differ from the master? 
	<p>Checks state equality of {@link #working} with master passed to constructor.
	 */
	final public boolean hasChanged(){
		return!master.stateEquals(working);
	}
	/**
	Implements abstract method. 
	<p>Sets the state of master passed to constructor from {@link #working}. 
	 */
	final public void applyChanges(){
		master.setState(working.copyState());
		if(false)targetValuesUpdated(null,master,null);
	}
	/** 
	Implements abstract method.
	<p>Sets the state of {@link #working} to a backup of 
	master passed to constructor and calls {@link #applyChanges()}. 
	 */
	final public void reverseChanges(){
		working.setState(copy);
		applyChanges();
	}
	/**
	Called by the couplers of targets created with convenience methods. 
	@param target created in the calling method
	@param values as passed to and (usually) updated by the method
	@param keys as passed to the method (maybe concatenated) 
	 */
	public abstract void targetValuesUpdated(STarget target,ValueNode values,
			String keys);
	/**
	Creates a textual target representing the specified value. 
	<p>Attaches a coupler which
	<ol><li>puts the text state as the value
	<li>calls {@link #targetValuesUpdated(STarget,ValueNode,String)}
	with the textual and the parameters passed
</ol>
	@param values contains the value
	@param key retrieves the value
	 */
	public final STextual newTextual(final ValueNode values,final String key){
		if(values==null)throw new IllegalArgumentException("Null values in "+Debug.info(this));
		trace(".newTextual: key="+key+" values="+values+"in "+this);
		return new STextual(key,values.getString(key),new STextual.Coupler(){
			public void textSet(STextual t) {
				values.put(key,t.text());
				targetValuesUpdated(t,values,key);
			}
		});
	}
	/**
	Convenience method for normal toggling creation.
	<p>Calls {@link #newToggling(ValueNode,String,boolean)} with 
	<code>invertState</code> passed <code>false</code>.
	 */
	public final SToggling newToggling(ValueNode values,String key){
		return newToggling(values,key,false);
	}
	protected void traceOutput(String msg){
		if(false)Util.printOut(ValueContent.class.getSimpleName()+msg);
	}
	/**
	Creates a toggling target representing the specified value. 
	<p>Attaches a coupler which
	<ol><li>puts the toggling state as the value
	<li>calls {@link #targetValuesUpdated(STarget,ValueNode,String)}
	with the toggling and the parameters passed
</ol>
	@param values contains the value
	@param title for the toggling, as key (up to <b>|</b>) retrieves the value
	@param invertValue should values be exposed as the reverse of their stored state
	and vice-versa 
	 */
	public final SToggling newToggling(final ValueNode values,final String title,
			final boolean invertValue){
		if(values==null)throw new IllegalArgumentException("Null values in "+Debug.info(this));
		final String key=title.replaceAll("\\|.*","");
		trace(".newToggling: title="+title+" key="+key+" values="+values+"in "+this);
		boolean set=values.getBoolean(key);
		SToggling t=new SToggling(title,invertValue?!set:set,new SToggling.Coupler(){
			public void stateSet(SToggling t){
				boolean set=t.isSet();
				values.put(key,invertValue?!set:set);
				targetValuesUpdated(t,values,key);
			}
		});
		return t;
	}
	/**
	Creates a multiple indexing from flag values. 
	<p>Attaches a coupler which
		<ol><li>updates the values from the keys and {@link SIndexing#indices()}
		<li>calls {@link #targetValuesUpdated(STarget,ValueNode,String)}
		with the indexing, values and the keys concatenated
	</ol>
	 @param values contains the flags
	  @param title passed to indexing
	  @param keys become the indexables; the flags they retrieve define the indices
	 */
	public final SIndexing newMultipleIndexing(final ValueNode values,
			final String title,final String[]keys,final boolean invertValues){
		trace(".newMultipleIndexing: keys="+keys[0]+"... values="+values+"in "+this);
		SIndexing indexing=new SIndexing(title,new SIndexing.Coupler(){
			public void indexSet(SIndexing ix){
				int[]indices=ix.indices();
				for(String key:keys)values.put(key,invertValues);
				for(int i=0;i<indices.length;i++)
					values.put(keys[indices[i]],!invertValues);
				targetValuesUpdated(ix,values,Objects.toString(keys,"|"));
			}
			public Object[]getIndexables(){
				return keys;
			}
		});
		List<Integer>ints=new ArrayList();
		for(int i=0;i<keys.length;i++) {
			boolean valueTrue=values.getBoolean(keys[i]);
			if(valueTrue&!invertValues||!valueTrue&invertValues)ints.add(i);
		}
		int[]indices=new int[ints.size()];
		for(int i=0;i<indices.length;i++)indices[i]=ints.get(i);
		if(values.values().length>0)indexing.setIndices(indices);
		return indexing;
	}
	/**
	Creates an indexing related to the specified values. 
	<p>This is purely a convenience method as it adds no functionality 
	beyond constructing the indexing. 
	 @param values passed by the empty coupler to 
	 {@link #targetValuesUpdated(STarget,ValueNode,String)} together with 
	 the indexing and <code>title</code>
	  @param title passed to indexing
	  @param indexables passed to indexing
	  @param indexed passed to indexing
	 */
	public final SIndexing newIndexing(final ValueNode values,
			final String title,Object[]indexables,Object indexed){
		return new SIndexing(title,indexables,indexed,new SIndexing.Coupler(){
			public void indexSet(SIndexing i){
				targetValuesUpdated(i,values,title);
			}
		});
	}
	final public SNumeric newNumeric(final ValueNode values,final String key,
			final NumberPolicy policy){
		trace(".newNumeric: key="+key+" values="+values+"in "+this);
		final boolean isInt=policy.format()==NumberPolicy.FORMAT_DECIMALS_0;
		if(false)trace(".newNumeric: ",isInt);
		double value=isInt?values.getInt(key):values.getDouble(key);
		if(value!=value||(isInt&&(int)value==ValueNode.NO_INT)) {
			if(true) return null;
			else throw new IllegalArgumentException("No value for key "+key);
		}
		if(isInt)values.put(key,(int)value);
		else values.put(key,value);
		return new SNumeric(key,value,new SNumeric.Coupler(){
			public void valueSet(SNumeric n){
				double value=n.value();
				if(isInt)values.put(key,(int)value);
				else values.put(key,value);
				targetValuesUpdated(n,values,key);
			}
			public NumberPolicy policy(SNumeric n){
				return policy;
			}
		});
	}
	public final String title(){
		return title;
	}
	public String toString(){
		return title+" " +hasChanged()+
				" master="+master+"working="+working+"copy="+copy;
	}
}
