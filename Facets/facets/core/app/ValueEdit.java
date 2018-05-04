package facets.core.app;
import facets.util.OffsetPath;
import facets.util.Tracer;
import facets.util.tree.NodePath;
import facets.util.tree.TypedNode;
import facets.util.tree.ValueNode;
/**
Encapsulates editing of a {@link ValueNode} using an input dialog.
 */
public abstract class ValueEdit extends Tracer{
	private final ValueNode node;
	private final String values[],valueLine,pairKey,proposal;
	private final int pathAt;
	public ValueEdit(PathSelection selection){
		node=(ValueNode)selection.single();
		NodePath path=selection.paths[0]==OffsetPath.singleMembered?
				new NodePath(new Object[]{node}):(NodePath)selection.paths[0];
		values=node.values();
		if(false)trace(".ValueEdit: node=",node);
		pathAt=path.valueAt();
		valueLine=values.length==0||pathAt<0?null:values[pathAt];
		String[]pair=valueLine==null?null:valueLine.split("=",2);
		pairKey=pair==null||pair.length==1?null:pair[0];
		proposal=valueLine==null?node.title():pairKey==null?valueLine:pair[1];
	}
	@Override
	protected void traceOutput(String msg){
		if(false)super.traceOutput(msg);
	}
	public final boolean dialogEdit(){
		String title=valueLine==null?"Change item title":"Set Value",
			rubricTail=valueLine==null?"title":pairKey==null?"value"
					:"value for '" +pairKey+"'",
			input=getDialogInput(title,"Enter new " +rubricTail,proposal);
		if(input==null||proposal.equals(input))return false;
		else input=input.trim();
		if(valueLine==null){
			boolean empty=input.equals("");
			if(!empty||acceptEmptyTitle())node.setTitle(empty?TypedNode.UNTITLED:input);
		}
		else if(pairKey==null){
			trace(".dialogEdit: pathAt=",pathAt);
			int valueAt=pathAt;
			Object[]contents=node.contents();
			for(int i=0;i<contents.length;i++)
				if(contents[i]instanceof TypedNode&&i<=valueAt)valueAt++;
			trace(".dialogEdit: valueAt=",valueAt);
			node.putAt(valueAt,input);
		}
		else{
			node.put(pairKey,input);
			try{
				checkKeyValue(node,pairKey);
			}
			catch(Exception e){
				node.put(pairKey,proposal);
			}
		}
		return true;
	}
	protected boolean acceptEmptyTitle(){
		return true;
	}
	protected void checkKeyValue(ValueNode node,String key){
		if(key.equals("text"))node.getString(key);
		else if(key.equals("keyedInts"))node.getInts(key);
	}
	protected abstract String getDialogInput(String title,String rubric,
			String proposal);
}