package applicable.eval;
import static facets.util.tree.Nodes.*;
import facets.util.Debug;
import facets.util.Objects;
import facets.util.tree.NodeList;
import facets.util.tree.TypedNode;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import applicable.eval.form.CalculatedField;
import applicable.eval.form.DateStamp;
import applicable.eval.form.EvalRecord;
import applicable.eval.form.NumberInput;
import applicable.eval.form.SwitchInput;
import applicable.eval.form.TickInput;
import applicable.eval.view.InputGroup;
import applicable.eval.view.TableEvaluation;
import applicable.eval.view.TableRow;
import applicable.eval.view.TableRows;
import applicable.eval.view.ViewEvaluation;
import applicable.treecode.TreeCodeType;
import applicable.treecode.TreeCodeTypes;
/**
{@link TreeCodeTypes} for an <b>Eval</b> application.
<p>{@link TreeCodeType}s returned by {@link #types()} can be aliased trivially
with {@link #aliasTypes}. 
 */
public class EvalTypes extends TreeCodeTypes{
	final protected static String evalTypesName=EvalTypes.class.getSimpleName();
	final Map<String,TreeCodeType>aliasTypes;
	final Map<TreeCodeType,String>typeAliases;
	public EvalTypes(TypedNode code){
		if(code==null)throw new IllegalArgumentException("Null code in "+Debug.info(this));
		else if(!code.type().equals(evalTypesName))
			throw new IllegalArgumentException("Bad code=" +code+" in "+Debug.info(this));
		TreeCodeType[]types=types();
		if(types==null||types.length==0)throw new IllegalArgumentException(
				"Null or empty types in "+Debug.info(this));
		Map<TreeCodeType,String>typeAliases=new HashMap();
		Map<String,TreeCodeType>aliasTypes=new HashMap();
		for(TreeCodeType type:types)
			if(type==null)throw new IllegalArgumentException(
					"Null type in "+Objects.toString(types,"\n"));
			else aliasTypes.put(type.name,type);
		for(TreeCodeType type:types)typeAliases.put(type,type.name);
		for(TypedNode child:code.children()){
			String typeName=child.title().replaceAll("[ :].*","");
			if(typeAliases.containsValue(typeName)){
				String alias=child.type();
				TreeCodeType type=aliasTypes.remove(typeName);
				aliasTypes.put(alias,type);
				typeAliases.put(type,alias);
			}
		}
		this.aliasTypes=Collections.unmodifiableMap(aliasTypes);
		this.typeAliases=Collections.unmodifiableMap(typeAliases);
	}
	protected TreeCodeType[]types(){
		return new TreeCodeType[]{
				Value.type,
				EvalRecord.type,
				Values.type,
				IfValue.type,
				SwitchInput.type,
				TickInput.type,
				NumberInput.type,
				ValueMatching.type,
				CalculatedField.type,
				ValueOf.type,
				DateStamp.type,
				TableEvaluation.type,
				TableRows.type,
				TableRow.type,
				ViewEvaluation.type,
				InputGroup.type
			};
		}
	@Override
	public String toString(){
		return aliasTypes.toString().replaceAll(",","\n");
	}
	public static EvalTypes fromCleanedTree(TypedNode tree){
		TypedNode src=child(tree,evalTypesName);
		if(src==null)throw new IllegalStateException(
				"Null child of type "+evalTypesName+" in "+tree);
		EvalTypes types=new EvalTypes(src);
		for(TypedNode node:descendants(tree))
			new NodeList(node,true).removeAll(children(node,
					evalTypesName,"Note","Date"));
		return types;
	}
}