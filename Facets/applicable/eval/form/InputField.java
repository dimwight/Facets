package applicable.eval.form;
import facets.util.Objects;
import facets.util.tree.DataNode;
import facets.util.tree.TypedNode;
import applicable.eval.EvalCoded;
import applicable.eval.EvalContext;
import applicable.eval.Value;
import applicable.eval.Values;
import applicable.field.OptionField;
import applicable.treecode.TreeCodeType;
import applicable.treecode.TreeCoded;
public abstract class InputField extends EvalField{
	public InputField(TypedNode source,TreeCodeType type,EvalContext context){
		super(source,type,context);
		debug=false&&label.text.equals("Armature inserts");
	}
	final public void setValue(Value value){
		boolean valueFound=false;
		for(int evalAt=0;evalAt<codeds.length;evalAt++)
			if((valueFound|=codeds[evalAt]instanceof Value)){
				codeds[evalAt]=value.copyValue();
				break;
			}
		if(!valueFound)throw new IllegalStateException("Value '"+value.asText()+
				"' not found in "+this);
	}
	public void updateValue(String text){
		if(debug)trace(".updateValue: text=",text);
		codeds[0]=Value.newValue(text);
	}
	public TypedNode newRecordCode(){
		TypedNode code=new DataNode(context.codeForType(Values.type),label.text);
		code.setChildren((TypedNode)codeds[0].source.copyState());
		return code;
	}
	public void updateValueState(TypedNode c){
		codeds[0].source.setState(c.children()[0]);
	}
	@Override
	protected Value[]doEvaluation(){
		return asValues((Value)codeds[0]);
	}
	final public Value[]inputs(){
		for(TreeCoded eval:codeds)
			if(eval instanceof InputValues)
				return((EvalCoded)eval).evaluate();
		return values();
	}
	final public int valueIndex(){
		Value values[]=false?inputs():values(),value=evaluate()[0];
		if(debug) {
			trace(".valueIndex: values=",values);
			trace(".valueIndex: value=",value);
		}
		for(int i=0;i<values.length;i++)if(values[i].equals(value))return i;
		throw new IllegalStateException("Bad value in "+this);
	}
	private Value[]values(){
		for(TreeCoded eval:codeds)
			if(eval instanceof Values)
				return((EvalCoded)eval).evaluate();
		return evaluate();
	}
	final public void _validate(){
		throw new RuntimeException("Not implemented in "+this);
	}
}
