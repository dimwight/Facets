package applicable.eval.form;
import facets.util.Debug;
import facets.util.Stateful;
import facets.util.tree.DataNode;
import facets.util.tree.NodeList;
import facets.util.tree.TypedNode;
import facets.util.tree.TypedNode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import applicable.eval.EvalCoded;
import applicable.eval.EvalContext;
import applicable.eval.Value;
import applicable.eval.Values;
import applicable.treecode.TreeCodeContext;
import applicable.treecode.TreeCodeType;
import applicable.treecode.TreeCoded;
import applicable.treecode.TreeCoded.Label;
/**
Container for input {@link Value}s of a {@link EvalForm}. 
 */
public final class EvalRecord extends TreeCoded{
	final public static TreeCodeType<EvalRecord>type=new TreeCodeType(
			EvalRecord.class.getSimpleName()){
		@Override
		public EvalRecord newCoded(TypedNode source,TreeCodeContext context){
			return new EvalRecord(source);
		};
	};
	private TypedNode copySource;
	private boolean debug;
	private TypedNode newSourceCopy(){
		return(TypedNode)source.copyState();
	}
	private EvalRecord(TypedNode source){
		super(source,type);
		copySource=newSourceCopy();
		debug=true&&label.text.equals("Base-mounted");
	}
	public void updateToFields(Map<Label,EvalCoded>fields){
		for(TypedNode c:source.children()){
			Label label=new Label(c.title());
			InputField field=(InputField)fields.get(label);
			if(field==null)throw new IllegalStateException("Null field for "+label);
			else field.updateValueState(c);
		}
	}
	void updateFromFields(Map<Label,EvalCoded>fields){
		for(TypedNode c:source.children())
			c.setChildren(((InputField)fields.get(new Label(c.title()))
					).newRecordCode().children());
	}
	public boolean hasChanged(){
		return!copySource.stateEquals(source);
	}
	public void storeState(){
		copySource=newSourceCopy();
	}
	public void revertState(){
		source.setState(copySource);
	}
	public EvalRecord copyState(String title){
		TypedNode copy=newSourceCopy();
		copy.setTitle(title);
		return new EvalRecord(copy);
	}
	static EvalRecord fromFormCodeds(EvalForm form,TreeCoded[]codeds){
		NodeList codes=new NodeList(new DataNode(form.codeForType(type),"Default"),false);
		for(TreeCoded coded:codeds)
			if(coded instanceof InputField)codes.add(((InputField)coded).newRecordCode());
		codes.updateParent();
		return new EvalRecord(codes.parent);
	}
}
