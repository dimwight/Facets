package facets.util.app;
import facets.util.Debug;
import facets.util.Tracer;
public class Disposer<T>extends Tracer{
	private T disposable;
	public Disposer(T disposable){
		this.disposable=disposable;;
	}
	final public T disposable(){
		if(disposable==null)throw new IllegalStateException("Null disposable in "+Debug.info(this));
		else return disposable;
	}
	public void dispose(){
		disposable=null;
	}
}
