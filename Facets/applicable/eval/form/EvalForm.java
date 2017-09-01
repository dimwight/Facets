package applicable.eval.form;
import static facets.util.Objects.*;
import static facets.util.tree.Nodes.*;
import facets.core.app.TreeView;
import facets.core.superficial.SIndexing;
import facets.util.Debug;
import facets.util.HtmlBuilder.RenderTarget;
import facets.util.HtmlFormBuilder;
import facets.util.IndexingIterator;
import facets.util.ItemList;
import facets.util.Stateful;
import facets.util.tree.DataNode;
import facets.util.tree.TypedNode;
import facets.util.tree.ValueNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import applicable.eval.EvalCoded;
import applicable.eval.EvalContext;
import applicable.eval.EvalTypes;
import applicable.eval.IfValue;
import applicable.eval.ValueOf;
import applicable.eval.Values;
import applicable.eval.view.InputGroup;
import applicable.eval.view.TableEvaluation;
import applicable.eval.view.TableRow;
import applicable.eval.view.TableRows;
import applicable.eval.view.ViewEvaluation;
import applicable.field.FieldFormBuilder;
import applicable.field.FieldSet;
import applicable.field.ValueField;
import applicable.treecode.TreeCodeType;
import applicable.treecode.TreeCoded;
import applicable.treecode.TreeCoded.Label;
/**
Core container for <b>Eval</b> code. 
<p>{@link EvalForm} contains 
<ul>
<li>a {@link DateStamp} providing a unique sequenced ID 
<li>an {@link EvalTypes} for creating its available {@link TreeCodeType}s 
<li>a {@link EvalRecord} sharing its {@link DateStamp} and storing its input {@link Values} 
<li>any number of {@link EvalField}s for which it provides a namespace and 
returns as required with with {@link #getLabelled(Label)}.  
</ul>
 */
final public class EvalForm extends EvalContext<EvalCoded>implements Stateful{
	final DateStamp stamp;
	final private EvalRecord[]records;
	EvalRecord active;
	final private Map<Label,EvalCoded>fields;
	final private TypedNode copyState;
	final private boolean showCodes=true;
	/**
	Unique constructor. 
	@param source the root node of the code
	@param types enables aliasing of code types
	 */
	public EvalForm(TypedNode source,EvalTypes types){
		super(source,types);
		Map<Label,EvalCoded>fields=new HashMap();
		DateStamp stamp=null;
		List<EvalRecord>records=new ArrayList();
		for(TreeCoded eval:newAliasedCodeds(source.children()))
			if(eval instanceof EvalField)fields.put(eval.label,(EvalCoded)eval);
			else if(eval instanceof EvalRecord)records.add((EvalRecord)eval);
			else if(eval instanceof DateStamp)stamp=(DateStamp)eval;
		this.fields=Collections.unmodifiableMap(fields);
		if(records.isEmpty())throw new IllegalStateException(
				"No records found in "+Debug.info(this));
		else active=(this.records=records.toArray(new EvalRecord[]{}))[0];
		active.updateToFields(fields);
		if(stamp!=null)this.stamp=stamp;
		else if(true)this.stamp=DateStamp.newNow();
		else throw new IllegalStateException("Null stamp in "+Debug.info(this));
		copyState=(TypedNode)source.copyState();
	}
	public SIndexing newIndexing(){
		return new SIndexing(codeForType(EvalRecord.type),new SIndexing.Coupler(){
			@Override
			public Object[]getIndexables(){
				return records;
			}
			@Override
			public void indexSet(SIndexing i){
				active=records[i.index()];
				active.updateToFields(fields);
			}
		});
	}
	public boolean hasChanged(){
		return !copyState().stateEquals(source);
	}
	@Override
	public EvalCoded getLabelled(Label label){
		EvalCoded got=fields.get(label);
		if(got==null)throw new IllegalStateException("No field with " +label+
				" in "+Debug.arrayInfo(fields.values().toArray()));
		else return got;
	}
	public HtmlFormBuilder newInputsBuilder(){
		final List<ValueField>fields=new ArrayList<>(); 
		final List<String>values=new ArrayList(),names=new ArrayList();
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
						final EvalCoded field=getLabelled(v.label);
						String keyTitle=field.title().replace(' ','_');
						new IndexingIterator<ValueField>(
								((EvalField)field).newFormFields(keyTitle,showCodes)){
							@Override
							protected void itemIterated(ValueField item,int at){
								fields.add(item);
								String title=item.title();
								if(false)trace(".itemIterated: title=",title);
								values.add(title+"="+field.evaluate()[at].asText());
								names.add(title);
							}
						}.iterate();
					}
				}
		}
		if(false)trace(".newInputsBuilder: ",fields.toArray());
		return new FieldFormBuilder(RenderTarget.Swing,
				new FieldSet(title(),fields.toArray(new ValueField[]{}),-1),
				new ValueNode("FieldValues",values.toArray(new String[]{})),
				names.toArray(new String[]{})){
			@Override
			public void readEdit(FormInput edit){
				super.readEdit(edit);
				if(false)EvalForm.this.trace(".readEdit: name="+edit.name+"\n",values.values());
				formEdited(values.values());
			}
			@Override
			protected boolean useInputField(String name){
				return true;
			}
			@Override
			public String newPageContent(){
				String content=super.newPageContent(),
						_uscores="(<b>[^>]+)_([^>]+</b>)";
				while(content.matches("(?s).*"+_uscores+".*"))
					content=content.replaceAll(_uscores,"$1 $2");
				if(false)EvalForm.this.trace(".newPageContent: content=",content);
				return content;	
			}
		};
	}
	private void formEdited(String[]pairs){
		final String _uscores="^([^=]+)_([^=]+)=";
		for(int i=0;i<pairs.length;i++)
			while(pairs[i].matches(_uscores+".*"))
				pairs[i]=pairs[i].replaceAll(_uscores,"$1 $2=");
		for(String pair:pairs){
			String[]splits=pair.split("=");
			if(splits.length==0)continue;
			String[]names=splits[0].replaceAll("(.*[a-z])([^a-z ]+)","$1:$2")
					.split(":");
			EvalCoded field=fields.get(new Label(names[0]));
			if(field==null)throw new IllegalArgumentException(
					"Null field "+splits[0]);
			if(field instanceof CalculatedField)continue;
			InputField input=(InputField)field;
			input.updateValue((names.length>1?(names[1]+"="):"")+splits[1]);
			active.updateFromFields(fields);
		}
	}
	public StringBuilder newPicksBuilder(){
		final StringBuilder picks=new StringBuilder();
		for(DataNode row:newTyped(DataNode.class,
				children(child(child(
						source,codeForType(TableEvaluation.type)),
						codeForType(TableRows.type)),
						codeForType(TableRow.type)
				))){
				String values[]=row.values(),
						id=values[0],desc=values[1],label=values[2],value=values[3],
						pickValue=getLabelled(new Label(label)
								).evaluate()[0].asText().equals(value)?value:null;
				if(pickValue!=null)picks.append((showCodes?(value+"\t"):"")+
								id+"\t"+desc+"\n");
		}
		return picks;
	}
	public EvalRecord activeRecord(){
		return active;
	}
	@Override
	public String title(){
		return source.type()+" : "+source.title();
	}
	@Override
	public Stateful copyState(){
		return copyState;
	}
	@Override
	public void setState(Object src){
		throw new RuntimeException("Not implemented in "+this);
	}
	@Override
	public boolean stateEquals(Stateful s){
		throw new RuntimeException("Not implemented in "+this);
	}
	@Override
	public Object updateStateStamp(){
		throw new RuntimeException("Not implemented in "+this);
	}
	@Override
	public Object stateStamp(){
		throw new RuntimeException("Not implemented in "+this);
	}
	void updateRecord(EvalRecord src){
		trace(".updateRecord: src=",src.title());
	}
	public EvalField[]_getValidatedFields(boolean inputsOnly){
		if(true)throw new RuntimeException("Not implemented in "+this);
		List<EvalCoded>fields=new ArrayList();
		for(EvalCoded field:this.fields.values()){
			if(field instanceof InputField)((InputField)field)._validate();
			if(false)continue;
			else if(field instanceof InputField||!inputsOnly)fields.add(field);
		}
		Collections.sort(fields);
		return fields.toArray(new EvalField[]{});
	}
}
