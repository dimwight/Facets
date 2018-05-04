package applicable.eval.form;
import facets.util.Debug;
import facets.util.Objects;
import facets.util.tree.DataNode;
import facets.util.tree.Nodes;
import facets.util.tree.TypedNode;
import facets.util.tree.ValueNode;
import facets.util.tree.XmlDocRoot;
import facets.util.tree.XmlPolicy;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import applicable.eval.EvalTypes;
import applicable.eval.view.ViewableForm;
import applicable.eval.view.TableEvaluation;
import applicable.eval.view.ViewEvaluation;
import applicable.treecode.TreeCodeType;
/**
Storage and encoding of {@link EvalForm}s and {@link EvalRecord}s. 
 */
public final class EvalForms extends EvalTypes{
	public static void main(String[]args){
		File source=new File("V875.config.xml");
		testConsumers(source,false);
	}
	public static void testConsumers(Object source, boolean picks){
		ValueNode doc;
		try{
			doc=new ValueNode(EvalForm.class.getSimpleName(),TypedNode.UNTITLED);
			new XmlDocRoot(doc,new XmlPolicy()
				).readFromSource(source);
		}catch(Exception e){
			throw new RuntimeException(e);
		}
		TypedNode code=doc.children()[0];
		EvalForms forms=new EvalForms(code);
		EvalForm form=new ViewableForm(code,null,null);
		TreeCodeType type=picks?TableEvaluation.type:ViewEvaluation.type;
		EvalFormConsumer consumer=(EvalFormConsumer)type.newCoded(
				Nodes.child(code,type.name),form);
		consumer.setForm(true?form
				:forms.newRecordForm(forms.newDecodedRecord(forms.newEncodedRecord(form))));
		System.exit(0);
	}
	@Override
	protected TreeCodeType[]types(){
		return Objects.join(TreeCodeType.class,super.types(), 
		new TreeCodeType[]{});
		}
	private final Map<DateStamp,EvalForm>forms=new HashMap();
	private EvalForms(TypedNode src){
		super(src);
	}
	/**
	Creates a new {@link EvalForm} matching a {@link EvalRecord}. 
	@param record must be output of {@link #newEncodedRecord(EvalForm)}
	@return {@link EvalForm} matching that passed to {@link #newEncodedRecord(EvalForm)}, 
	updated from the decoded {@link EvalRecord}
	 */
	private EvalForm newRecordForm(EvalRecord record){
//		EvalForm form=forms.get(record.stamp);
//		if(form==null)throw new IllegalStateException("Null form in "+Debug.info(this));
		throw new RuntimeException("Not implemented in "+this);
//		form.updateRecord();
//		return form;
	}
	/**
	Generates encoded text of a {@link EvalForm}'s current {@link EvalRecord}. 
	@param form will be added to store if not already present
	@return text suitable for passing to {@link #newDecodedRecord(String)}
	 */
	public String newEncodedRecord(EvalForm form){
		if(true)throw new RuntimeException("Not implemented in "+this);
//		forms.put(form.stamp,form);
		return Nodes.encode((DataNode)form.active.source,0).values()[0];
	}
	/**
	Creates a {@link EvalRecord} from encoded text. 
	@param encoding must be output of {@link #newEncodedRecord(EvalForm)}
	 */
	public EvalRecord newDecodedRecord(String encoding){
		ValueNode code=new ValueNode(EvalRecord.type.name,new Object[]{encoding});
		Nodes.decode(code);
		return EvalRecord.type.newCoded(code,null);
	}
}
