package facets.util;
import static facets.util.NumberValues.ValueType.*;
import static facets.util.Strings.*;
import static java.lang.Math.*;
import static java.lang.System.*;
import static java.util.Arrays.*;
import facets.util.tree.ValueNode;
import java.util.Arrays;
/**
Encapsulates management of numeric values stored in a {@link ValueNode}. 
 */
public abstract class NumberValues extends Tracer{
	enum ValueType{Int,Ints,Doubles}
	private final ValueType type;
	private final Object defaultValue;
	private final String keyTop;
	private final int checkCount;
	private Object value;
	private boolean hasSessionValue;
	@Override
	public String toString(){
		return keyTop+"="+hasDefaultValue();
	}
	protected NumberValues(Object defaultProposal,int checkCount,String keyTop){
		this.keyTop=keyTop;
		this.checkCount=checkCount;
		type=defaultProposal instanceof Integer?Int
				:defaultProposal instanceof int[]?Ints
				:defaultProposal instanceof double[]?Doubles
				:null;
		if(type==null)
			throw new RuntimeException("Not implemented for " +defaultProposal.getClass());
		defaultValue=type==Int?defaultProposal
				:type==Ints?validDefaultInts((int[])defaultProposal)
				:validDefaultDoubles((double[])defaultProposal);
	}
	protected abstract ValueNode store();
	protected abstract String tailKeySession();
	protected abstract String tailKeyDefaults();
	@Override
	protected void traceOutput(String msg){
		if(false&&type==Ints)super.traceOutput(msg);
	}
	protected double[]validDefaultDoubles(double[]proposed){
		if(proposed.length>=checkCount)return proposed;
		double[]defaults=new double[checkCount];
		arraycopy(proposed,0,defaults,0,min(proposed.length,checkCount));
		trace(": doubles count="+checkCount+
				" needed="+checkCount+" add=",sfString(defaults));
		return defaults;
	}
	protected int[]validDefaultInts(int[]proposed){
		if(proposed.length>=checkCount)return proposed;
		int[]defaults=new int[checkCount];
		arraycopy(proposed,0,defaults,0,min(proposed.length,checkCount));
		trace(": int count="+checkCount+
				" needed="+checkCount+" add=",intsString(defaults));
		return defaults;
	}
	final public boolean hasSessionValue(){
		return hasSessionValue;
	}
	final public boolean hasDefaultValue(){
		switch(type){
		case Int:return((int)getValue(false))==(int)defaultValue;
		case Ints:return Arrays.equals((int[])getValue(false),(int[])defaultValue);
		case Doubles:return Arrays.equals((double[])getValue(false),(double[])defaultValue);
		}
		throw new RuntimeException("Not implemented in "+this);
	}
	final public Object getValue(boolean getDefault){
		try{
			return(!getDefault&&(hasSessionValue=(getCount(tailKeySession())>=checkCount)))
				||getCount(tailKeyDefaults())>=checkCount?value:defaultValue;
		}catch(Exception e){
			return defaultValue;
		}
	}
	final public void putValue(Object value,boolean setDefault){
		ValueNode store=store();
		String key=keyTop+tailKeySession();
		if(false)ValueNode.putCheckKey=key;
		store.put(key,value);
		if(setDefault)store.put(keyTop+tailKeyDefaults(),value);
		hasSessionValue=true;
	}
	private int getCount(String keyTail){
		String key=keyTop+keyTail;
		return type==Int?intCount(key):type==Ints?intsCount(key)
			:doublesCount(key);
	}
	private int intCount(String key){
		int check=store().getInt(key);
		value=check;
		return check==ValueNode.NO_INT?0:1;
	}
	private int intsCount(String key){
		int[]check=store().getInts(key);
		value=copyOfRange(check,0,checkCount);
		return check.length;
	}
	private int doublesCount(String key){
		double[]check=store().getDoubles(key);
		value=copyOfRange(check,0,checkCount);
		return check.length;
	}
}
