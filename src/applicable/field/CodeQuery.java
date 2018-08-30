package applicable.field;
import facets.core.app.Dialogs.MessageTexts;
import facets.util.Stateful;
import facets.util.tree.TypedNode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import applicable.TextQuery;
public abstract class CodeQuery extends TextQuery{
	public static final String CODE_NEW="Basic Search",CODE_ADD="&Add to Results",
		CODE_WITHIN="S&earch in Results",CODE_WITHOUT="Remo&ve from Results",
		CODES_ADD[]={CODE_NEW,CODE_ADD,CODE_WITHOUT,CODE_WITHIN},
		CODES_ALL[]={CODE_NEW,CODE_WITHOUT,CODE_WITHIN};
	public final String code;
	public CodeQuery(String code,String text,boolean any,boolean exact){
		super(any,exact,text);
		this.code=code;
	}
	final public MessageTexts execute(List<Stateful>buffer,Collection<Stateful>input){
		Collection<TypedNode>searchable=new ArrayList(code==CODE_WITHIN
				||code==CODE_WITHOUT?buffer:input);
		Set<TypedNode>bufferNow=new HashSet(buffer);
		Set<TypedNode>delta;
		delta=new HashSet();
		if(code==CODE_NEW)bufferNow.clear();
		delta=newDelta(searchable);
		if(code==CODE_WITHOUT)bufferNow.removeAll(delta);
		else if(code==CODE_WITHIN)bufferNow.retainAll(delta);
		else bufferNow.addAll(delta);
		int thenCount=buffer.size();
		buffer.clear();
		buffer.addAll(bufferNow);
		MessageTexts texts=buffer.equals(bufferNow)?
				new MessageTexts(code,"Results unchanged: "+thenCount,"","")
				:new MessageTexts(code,"Results before search: "+thenCount,"",
						"Results after search: "+buffer.size());
		return texts;
	}
	protected abstract Set<TypedNode>newDelta(Collection<TypedNode> searchable);
}