package applicable.eval.form;
import static facets.core.app.Dialogs.Response.*;
import static facets.util.Objects.*;
import static facets.util.tree.Nodes.*;
import facets.core.app.Dialogs;
import facets.core.app.Dialogs.Response;
import facets.core.superficial.SIndexing;
import facets.core.superficial.SToggling;
import facets.core.superficial.STrigger;
import facets.util.Debug;
import facets.util.HtmlBuilder.RenderTarget;
import facets.util.HtmlFormBuilder;
import facets.util.HtmlFormBuilder.FormTag;
import facets.util.Titled;
import facets.util.Tracer;
import facets.util.tree.DataNode;
import facets.util.tree.NodeList;
import facets.util.tree.TypedNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import applicable.eval.EvalCoded;
import applicable.eval.EvalContext;
import applicable.eval.EvalTypes;
import applicable.eval.IfValue;
import applicable.eval.Value;
import applicable.eval.ValueOf;
import applicable.eval.Values;
import applicable.eval.app.EvalCoder;
import applicable.eval.form.TickInput.CheckInput;
import applicable.eval.view.InputGroup;
import applicable.eval.view.TableEvaluation;
import applicable.eval.view.TableRow;
import applicable.eval.view.TableRows;
import applicable.eval.view.ViewEvaluation;
import applicable.treecode.TreeCodeType;
import applicable.treecode.TreeCoded;
import applicable.treecode.TreeCoded.Label;
/**
Core container for <b>Eval</b> code. 
<p>{@link EvalForm} contains 
<ul>
<li>a {@link DateStamp} providing a unique sequenced ID 
<li>an {@link EvalTypes} for creating its available {@link TreeCodeType}s 
<li>any number of {@link EvalRecord}s ?sharing its {@link DateStamp} 
and storing its input {@link Values} 
<li>any number of {@link EvalField}s for which it provides a namespace and 
returns as required with with {@link #getLabelled(Label)}.  
</ul>
 */
public abstract class EvalForm extends EvalContext<EvalCoded>implements Titled{
	protected final String recordType=codeForType(EvalRecord.type);
	final protected List<EvalRecord>records=new ArrayList();
	protected EvalRecord active;
	final protected Map<Label,EvalCoded>fields;
	protected boolean showCodes=false;
	final private Map<String,FieldCoder>nameCoders=new HashMap(); 
	final private List<String>names=new ArrayList();
	public EvalForm(TypedNode code){
		super(code,EvalTypes.fromCleanedTree(code));
		Map<Label,EvalCoded>fields=new HashMap();
		DateStamp stamp=null;
		TreeCoded[]codeds=newAliasedCodeds(code.children());
		for(TreeCoded coded:codeds)
			if(coded instanceof EvalField)fields.put(coded.label,(EvalCoded)coded);
			else if(coded instanceof EvalRecord)records.add((EvalRecord)coded);
			else if(coded instanceof DateStamp)stamp=(DateStamp)coded;
		this.fields=Collections.unmodifiableMap(fields);
		if(records.isEmpty())records.add(EvalRecord.fromFormCodeds(this,codeds));
		(active=records.get(0)).updateToFields(fields);
	}
	protected void updateSources(){
		updateRecordSources(source,records,recordType,0);
	}
	public static void updateRecordSources(TypedNode root,List<EvalRecord>records,
			String recordType,int recordsAt){
		List<TypedNode>buffer=new ArrayList();
		NodeList all=new NodeList(root,false);
		for(TypedNode each:all)
			if(each.type().equals(recordType))buffer.add(each);
		all.removeAll(buffer);
		buffer.clear();
		for(EvalRecord record:records)
			buffer.add((TypedNode)record.source.copyState());
		all.addAll(recordsAt,buffer);
		all.updateParent();
	}
	@Override
	public EvalCoded getLabelled(Label label){
		EvalCoded got=fields.get(label);
		if(got==null)throw new IllegalStateException("No field with " +label+
				" in "+Debug.arrayInfo(fields.values().toArray()));
		else return got;
	}
	private static class FieldCoder extends Tracer{
		final String useName;
		private final EvalField field;
		private final boolean showCodes;
		private final String valueText;
		FieldCoder(EvalField field,boolean showCodes){
			this.field=field;
			this.showCodes=showCodes;
			this.valueText=field.evaluate()[0].asText();
			String raw=field.label.text,splits[]=raw.split(" ",2);
			useName=field instanceof CheckInput?
					splits[splits.length==1||this.showCodes?0:1]:raw;
		}
		Object getFieldValue(){
			return valueText;
		}
		boolean fieldCanInput(){
			return field instanceof InputField;
		}
		String newFieldCode(){
			if(field instanceof SwitchInput){
				List<String>options=new ArrayList<>();
				String selection=null;
				for(Value v:((SwitchInput)field).inputs()){
					String raw=v.asText();
					if(raw.startsWith("#"))continue;
					String splits[]=raw.split(" ",2),cooked=splits[splits.length==1||showCodes?0:1];
					if(asCode(raw).equals(valueText))selection=cooked;
					options.add(cooked);
				}
				if(selection==null)throw new IllegalStateException(
						"Null selection for "+field+" value="+valueText);
				return FormTag.newSelect(useName,options.toArray(new String[]{}),selection);
			}
			else if(field instanceof CheckInput)
				return FormTag.newInputCheckbox(useName,
						field.evaluate()[0].asText().toLowerCase());
			else if(field instanceof NumberInput)
				return FormTag.newInputText(useName,3,field.evaluate()[0].asText());
			else return getFieldValue().toString();
		}
		private String asCode(String raw){
			return raw.split(" ",2)[0];
		}
		void updateField(String value){
			if(field instanceof SwitchInput){
				SwitchInput switcher=(SwitchInput)field;
				for(Value input:switcher.inputs()){
					String raw=input.asText();
					if(asCode(raw).equals(value)||raw.endsWith(value))
						switcher.codeds[0].source.setTitle(asCode(raw));
				}
			}
			else if(field instanceof CheckInput)((CheckInput)field).updateValue(value);
			else((InputField)field).updateValue(value);
		}
	}
	final protected String[]newInputNames(){
		nameCoders.clear();
		names.clear();
		for(TypedNode child:children(child(source,
				codeForType(ViewEvaluation.type)),
				codeForType(InputGroup.type))){
			InputGroup tab=InputGroup.type.newCoded(child,this);
			if(tab.isIgnorable())continue;
			for(TreeCoded coded:tab.codeds)
				if(coded instanceof IfValue){
					if(((EvalCoded)coded).evaluatesFalse())break;
					else continue;
				}
				else{
					InputGroup group=(InputGroup)coded;
					if(group.isIgnorable())continue;
					for(ValueOf v:newTyped(ValueOf.class,group.codeds)){
						if(v.isIgnorable()||v.evaluatesFalse())continue;
						for(EvalField field:((EvalField)getLabelled(v.label)).formFields())
							addFieldCoder(field);
					}
				}
		}
		String[]nameSet=names.toArray(new String[]{});
		return nameSet;
	}
	protected void addFieldCoder(EvalField field){
		FieldCoder coder=new FieldCoder(field,showCodes);
		nameCoders.put(coder.useName,coder);
		names.add(coder.useName);
	}
	final public String newPicks(){
		final StringBuilder picks=new StringBuilder();
		for(DataNode row:newTyped(DataNode.class,
				children(child(child(
						source,codeForType(TableEvaluation.type)),
						codeForType(TableRows.type)),
						codeForType(TableRow.type)
				))){
			String fields[]=row.values(),
					id=fields[0],desc=fields[1],label=fields[2],test=fields[3],
					pick=(showCodes?(test+"\t"):"")+id+"\t"+desc;
			EvalCoded target=getLabelled(new Label(label));
			Value[]values=target.evaluate();
			int count=asNumber(values[0]);
			if(count>0)picks.append(pick+(count>1?("\t"+count):"")+"\n");
			else for(Value value:values)
				if(value.asText().equals(test))picks.append(pick+"\n");
		}
		return picks.toString();
	}
	private int asNumber(Value value){
		try {
			int count=Integer.valueOf(value.asText());
			trace(".asNumber: count=",count);
			return count<0?count*-1:count;
		} catch (Exception e) {
			if(e instanceof NumberFormatException)return 0;
			else throw new RuntimeException(e);
		}
	}
	@Override
	public String title(){
		return source.title();
	}
	final protected Object getFieldValue(String name){
		FieldCoder input=nameCoders.get(name);
		if(input==null)throw new IllegalStateException("No inputter for "+name);
		else return input.getFieldValue();
	}
	final protected boolean fieldCanInput(String name){
		return nameCoders.get(name).fieldCanInput();
	}
	final protected String newFieldHtml(String name){
		return nameCoders.get(name).newFieldCode();
	}
	final protected void fieldEdited(String name,String value){
		FieldCoder input=nameCoders.get(name);
		if(input==null)throw new IllegalStateException("Null input for "+name);
		input.updateField(value);
		active.updateFromFields(fields);
	}
}
