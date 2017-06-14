package facets.core.superficial;
import facets.core.superficial.Notifying.Impact;
import facets.util.Debug;
import java.util.ArrayList;
import java.util.List;
/**
Object to be passed by {@link Notifying} to {@link Notifiable}.
 */
public class Notice{
	public final Impact impact;
	public final List<Notifying>sources=new ArrayList();
	public Notice(Notifying source,Impact impact){
		this.impact=impact;
		addSource(source);
	}
	final public Notice addSource(Notifying source){
		if(source==null)throw new IllegalArgumentException(
				"Null source in "+Debug.info(this));
		sources.add(source);
		return this;
	}
	public String toString(){
		return Debug.info(this)+" "+(false?"":impact)+
		":\n\t"+Debug.arrayInfo(sources.toArray()).replaceAll("[^{]+\\{\\s*([^}]+)\\s*\\}.*","$1"
				).replaceAll("\n"," : ");
	}
	public String flashText(){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	public static Notifying findElement(Notifying t,int...offsets){
		for(int i:offsets)t=t.elements()[i];
		return t;
	}
}
