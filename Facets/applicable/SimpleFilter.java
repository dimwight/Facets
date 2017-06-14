package applicable;
import facets.util.Tracer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
public abstract class SimpleFilter<F>extends Tracer{
	final public Collection<F>filter(Collection<F>in){
		List<F>results=new ArrayList();
		try{
			for(F f:in)if(passes(f))results.add(f);
		}
		catch(Exception e){
			results.clear();
			results.add(newExceptionResult(e));
		}
		return results;
	}
	protected abstract boolean passes(F f);
	protected F newExceptionResult(Exception e){
		throw new RuntimeException(e);
	}
}