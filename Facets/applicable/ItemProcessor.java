package applicable;
import static facets.util.Debug.*;
import facets.util.Debug;
import facets.util.Util;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public abstract class ItemProcessor<I>{
	private final List<I>included=new ArrayList();
	private final Map<I,Exception>errors=new HashMap();
	private final String title;
	private final int processedMod;
	private int existing,processed;
	public ItemProcessor(String title,List<I>list,int processedMod,boolean memChecks){
		this.title=title;
		memCheck=memChecks;
		this.processedMod=processedMod;
		for(I item:list)if(includeItem(item))included.add(item);
			traceTitled("includes="+included.size());
	}
	protected abstract boolean includeItem(I item);
	public final void processIncluded(){
		String process="processIncluded [" +title+"]" ;
		memCheck(process+": ");
		int existings=3;
		for(I item:included)try{
			if(processItem(item))processed++;
			else if(++existing<existings)traceTitled("existing=" +(existing)+" item="+item
					+(existing<existings-1?"":"..."));
			if(processedMod>0&&processed>0&&processed%processedMod==0){
				if(memCheck)memCheck(title+": processed="+processed+" ");
				else traceTitled("processed="+processed);
			}
		}
		catch(Exception e){
			if(throwErrors())throw new RuntimeException(e);
			errors.put(item,e);
			traceTitled("processing item="+item+": error="+e.getMessage());
		}
		memCheck(process+"~: ");
		memCheck=false;
		int errorCount=errors.size();
		traceTitled("existing="+existing+": processed="+processed+" errors="+errorCount);
		if(listErrors()&&errorCount>0)
			for(I item:errors.keySet())
				Util.printOut(Debug.exceptionInfo(errors.get(item),item.toString()));
	}
	protected boolean listErrors(){
		return false;
	}
	protected boolean throwErrors(){
		return false;
	}
	protected abstract boolean processItem(I item)throws IOException;
	private void traceTitled(String msg){
		Util.printOut(title +": "+msg);
	}
}
