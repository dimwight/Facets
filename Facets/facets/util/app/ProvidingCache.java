package facets.util.app;
import static facets.util.Debug.*;
import static facets.util.Util.*;
import static java.lang.Math.*;
import static java.lang.System.*;
import facets.util.Debug;
import facets.util.Objects;
import facets.util.Times;
import facets.util.Tracer;
import facets.util.Util;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
Cache that encapsulates the creation of items to be stored.
 */
public class ProvidingCache extends Tracer{
	public static class ItemValuer{
		final protected double newValue(int request,long memFinal,long timeCost){
			double recent=recentValue(request),space=spaceValue(memFinal),
				create=creationValue(timeCost),trio[]={space,recent,create};
			for(int t=0;t<trio.length;t++)if(Double.isInfinite(trio[t])||trio[t]!=trio[t])
					throw new IllegalStateException(new String[]{"space","recent","create"}[t]+
							"=infinity or NaN in "+this);
			return mergeValues(recent,space,create);
		}
		final protected double recentValue(int request){
			return request;
		}
		final protected double spaceValue(long memFinal){
			return memFinal<=0?0:(1/sqrt(memFinal/1024d));
		}
		final protected double creationValue(long timeCost){
			return timeCost<50?0:sqrt(timeCost);
		}
		protected double mergeValues(double recent,double space,double create){
			return recent*5+space*10+(false?0:create/10);
		}
	}
	public static final int PASS_THROUGH=Integer.MAX_VALUE;
	private static final boolean verbose=false,timing=false;
	private static final long loadTime=newTime();
	private int requests;
	protected boolean doTrace(){
		return false;
	}
	protected void traceOutput(String msg){
		if(!doTrace())return;
		if(memChecks!=null){
			memChecks.trace(msg);
			return;
		}
		msg=Debug.info(this)+": "+msg;
		if(timing)Times.printElapsed(msg);
		else Util.printOut(msg);
	}
	public ProvidingCache(MemoryChecks memChecks,AppWatcher watcher){
		this.memChecks=memChecks;
		countMax=-1;
		this.watcher=watcher;
	}
	public ProvidingCache(int countMax,AppWatcher watcher){
		this.countMax=countMax;
		this.watcher=watcher;
		memChecks=null;
	}
	private final class Key{
		final Object[]values;
		final int request;
		long memBuild=-1,memFinal=-1,timeCost=-1;
		final ItemValuer valuer;
		Key(ItemProvider p,Object[]values){
			if(values.length==0)throw new IllegalArgumentException(
					"Empty values in "+Debug.info(this));
			else this.values=Objects.join(Object.class,new Object[]{p.getClass(),p.source},values);
			request=requests++;
			valuer=getItemValuer(p,values);
		}
		boolean matches(Object[]values){
			boolean matches=true;
			for(int i=0;i<values.length;i++)
				matches&=values[i]==null||values[i].equals(this.values[i]);
			return matches;
		}
		public boolean equals(Object o){
			return Arrays.equals(values,(((Key)o).values));
		}
		@Override
		public int hashCode(){
			return Arrays.hashCode(values);
		}
		double value(){
			return valuer.newValue(request,memFinal,timeCost);
		}
		public String toString(){
			String[]ids=new String[values.length-2];
			for(int i=0;i<ids.length;i++)ids[i]=Debug.id(values[i+2]);
			return "["+values[0].toString().replaceFirst("[^A-Z]+","")+","
						+info(values[1])+"," +Objects.toString(ids)+"]"
					+" {#"+request+(timeCost<0?memBuild<0?"}":(","+kbs(memBuild)+"}")
							:(","+kbs(memFinal)+","+timeCost+"}="+sfs(value())));
		}
	}
	private final Map<Key,Object>items=new HashMap();
	private final int countMax;
	private final AppWatcher watcher;
	private final MemoryChecks memChecks;
	protected ItemValuer getItemValuer(ItemProvider p,Object[]itemValues){
		return new ItemValuer();
	}
	private Object getItemForValues(ItemProvider p,Object[]values){
		if(false)throw new RuntimeException("Disabled in "+Debug.info(this));
		Key key=new Key(p,values);
		if(verbose)trace("GETTING " +key);
		boolean passThrough=countMax==PASS_THROUGH||p.passThrough();
		Object item=passThrough?null:items.get(key);
			try{
		String diskName=p.newDiskName(values);
		if(item==null&&diskName!=null){
			inhibitGc(true);
			long beforeGet=newTime();
			item=p.getDiskItem(diskName);
			if(item!=null){
				key.memFinal=p.finalByteCount(item);
				key.timeCost=newTime()-beforeGet;
			}
			trace("DISK "+key+" > "+info(item));
			inhibitGc(false);
		}
		if(item!=null){
			for(Key itemKey:items.keySet())
				if(itemKey.equals(key)){
					key.memFinal=itemKey.memFinal;
					key.timeCost=itemKey.timeCost;
					break;
				}
			items.put(key,item);
			if(verbose)trace("GOT "+key+" > "+info(item));
			return item;
		}
		inhibitGc(true);
		int itemCountStart=items.size(),maximumCount=countMax<1?0:countMax;
		if(maximumCount>0){
			List<Key>matches=keysMatching(p.source,p.getClass());
			int matchCount=matches.size();
			if(matchCount>=maximumCount){
				Collections.sort(matches,new Comparator<Key>(){
					@Override
					public int compare(Key o,Key p){
						return new Long(o.request).compareTo(new Long(p.request));
					}
				});
				if(false)trace(".getItemForValues: matches=",matches);
				Key remove=matches.get(0);
				traceDebug("REMOVED maximumCount="+maximumCount+" matchCount="+matchCount+
						"\n\t"+remove+" > ",items.remove(remove));
				itemCountStart=items.size();
			}
		}
		long itemMems=0;
		for(Key k:items.keySet())itemMems+=k.memFinal;
		long memNeeded=memChecks!=null?key.memBuild=p.buildByteCount():-1,
			memAvailable=memNeeded>=0?memChecks.checkFree(" itemMems="+Util.mbs(itemMems))
					:memNeeded;
		if(false)trace("CHECK "+(itemCountStart)+
				(true?"":(" available="+Util.mbs(memAvailable)))+" "+key);
		if(memNeeded>memAvailable||(memNeeded<0&&itemCountStart>countMax)){
			List<Key>purgeSort=new ArrayList(items.keySet());
			Collections.sort(purgeSort,new Comparator<Key>(){
				final long checkTime=newTime()-loadTime;
				@Override
				public int compare(Key o,Key p){
					return new Double(o.value()).compareTo(new Double(p.value()));
				}
			});
			trace("PURGING ",purgeSort);
			long purged=0;
			List<Key>purge=purgeSort.subList(0,purgeSort.size()/4);
			for(Key r:purge){
				items.remove(r);
				purged+=r.memFinal;
			}
			gc();
			trace("PURGED "+Util.mbs(purged)+" ",purge);
		}
		long beforeNew=newTime();
		item=watcher==null?p.doReturnableOperation():watcher.runWatched(p);
		if(passThrough||item==null){
			inhibitGc(false);
			return item;
		}
		key.timeCost=newTime()-beforeNew;
		if(diskName!=null)p.putDiskItem(diskName,item);
		key.memFinal=true||memNeeded>=0?p.finalByteCount(item):memNeeded;
		items.put(key,item);
		traceDebug("PUT "+items.size()+" "+key+" > ",item);
			}
			catch(OutOfMemoryError e){
				MemoryChecks.checkStatus(e+": Cannot create item for "+p+", ");
				clear();
			}
		inhibitGc(false);
		return item;
	}
	/**
	{@link WatchableOperation} that creates items for storage and return by 
	{@link ProvidingCache}.
	 */
	public abstract static class ItemProvider<T>extends WatchableOperation{
		public final Object source;
		private final ProvidingCache c;
		public ItemProvider(ProvidingCache c,Object source,String title){
			super(title);
			this.c=c;
			this.source=source;
		}
		/**
		Re-implementation. 
	@return {@link WatchableOperation.CancelStyle#Timeout} by default. 
		 */
		@Override
		public CancelStyle cancelStyle(){
			return CancelStyle.None;
		}
		@Override
		final public Object doReturnableOperation(){
			return newItem();
		}
		final public T getForValues(Object...values){
			T t=(T)c.getItemForValues(this,values);
			return t;
		}
		final public T getNewForValues(Object...values){
			c.removeItemForValues(this,values);
			T t=(T)c.getItemForValues(this,values);
			return t;
		}
		final public boolean hasForValues(Object...values){
			return c.containsItemForValues(this,values);
		}
		public final void removeForValues(Object...values){
			c.removeItemForValues(this,values);
		}
		public final void removeAll(){
			c.removeMatchingItems(source,getClass());
		}
		protected abstract T newItem();
		protected boolean passThrough(){
			return false;
		}
		protected long buildByteCount(){
			throw new RuntimeException("Not implemented in "+Debug.info(this));
		}
		protected long finalByteCount(T item){
			return buildByteCount();
		}
		protected String newDiskName(Object[]values){
			return null;
		}
		protected void putDiskItem(String diskName,T item){
			throw new RuntimeException("Not implemented in "+Debug.info(this));
		}
		protected T getDiskItem(String diskName){
			throw new RuntimeException("Not implemented in "+Debug.info(this));
		}
	}
	private boolean containsItemForValues(ItemProvider p,Object[]values){
		return items.get(new Key(p,values))!=null;
	}
	private void removeItemForValues(ItemProvider p,Object[]values){
		items.remove(new Key(p,values));
	}
	private void removeMatchingItems(Object...values){
		inhibitGc(true);
		int sizeThen=items.size();
		for(Object remove:keysMatching(values))
			traceDebug("Removing ",items.remove(remove));
		int sizeNow=items.size();
		if(sizeThen-sizeNow>0)trace("REMOVE MATCHING "+Debug.arrayInfo(values)+
					" sizeThen="+sizeThen+" sizeNow="+sizeNow);
		inhibitGc(false);
	}
	private List<Key>keysMatching(Object...values){
		List<Key>removes=new ArrayList();
		for(Key k:items.keySet())if(k.matches(values))removes.add(k);
		return removes;
	}
	public final void clear(){
		items.clear();
		requests=0;
		gc();
		trace("CLEAR: ");
		if(memChecks!=null)memChecks.checkFree("");
	}
	private void inhibitGc(boolean on){
		if(memChecks!=null)memChecks.pauseChecks(on);
	}
	private static long newTime(){
		return nanoTime()/1000/1000;
	}
}
