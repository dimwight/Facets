package applicable.eval.view;
import static facets.util.Objects.*;
import static facets.util.tree.Nodes.*;
import facets.util.Debug;
import facets.util.HtmlBuilder;
import facets.util.Objects;
import facets.util.Tracer;
import facets.util.HtmlBuilder.RenderTarget;
import facets.util.HtmlFormBuilder;
import facets.util.HtmlFormBuilder.FormInput;
import facets.util.tree.TypedNode;
import facets.util.tree.Nodes;
import facets.util.tree.TypedNode;
import facets.util.tree.ValueNode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import applicable.eval.EvalCoded;
import applicable.eval.EvalContext;
import applicable.eval.EvalCoded;
import applicable.eval.IfValue;
import applicable.eval.Value;
import applicable.eval.ValueOf;
import applicable.eval.Values;
import applicable.eval.form.EvalForm;
import applicable.eval.form.EvalFormConsumer;
import applicable.eval.form.InputField;
import applicable.field.FieldFormBuilder;
import applicable.field.FieldSet;
import applicable.field.OptionField;
import applicable.field.TextField;
import applicable.field.ValueField;
import applicable.treecode.TreeCodeContext;
import applicable.treecode.TreeCodeType;
import applicable.treecode.TreeCoded;
public final class ViewEvaluation extends EvalFormConsumer{
	final public static TreeCodeType<ViewEvaluation>type=new TreeCodeType(
			ViewEvaluation.class.getSimpleName()){
		@Override
		public EvalCoded newCoded(TypedNode code,TreeCodeContext context){
			return new ViewEvaluation(code,(EvalContext)context);
		};
	};
	private ViewEvaluation(TypedNode source,EvalContext context){
		super(source,type,context);
	}
	@Override
	public void setForm(EvalForm form){//${_stringprompt:output:BOM}
		if(true)throw new RuntimeException("Not implemented in "+this);
		for(Values tab:newTyped(Values.class,codeds)){
			String filter=tab.title();
			if(false&&!filter.contains("Armature"))
				//Options  Combo
				continue;
			trace(".setForm: tab=",filter);
			for(TreeCoded c:tab.codeds){
				if(c instanceof IfValue){
					if(((EvalCoded)c).evaluatesFalse())break;
					else continue;
				}
				ValueOf v=(ValueOf)c;
				InputField field=(InputField)form.getLabelled(v.label);
				filter=field.title();
				if(v.evaluatesFalse()||(false&&!filter.contains("Inserts")))
					//Armature  Tellus mains
					continue;
				trace(".setForm: field="+filter+" value=",field.evaluate()[0].asText());
				trace(".setForm:  inputs=",field.inputs());
			}
		}
	}
}
