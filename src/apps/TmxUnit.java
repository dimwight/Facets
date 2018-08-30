package apps;
import static facets.util.tree.Nodes.*;
import static java.lang.Integer.*;
import facets.util.Objects;
import facets.util.Tracer;
import facets.util.tree.NodeList;
import facets.util.tree.TypedNode;
import facets.util.tree.ValueNode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
final class TmxUnit extends Tracer implements Comparable<TmxUnit>{
	final ValueNode node;
	final LocalDateTime updated;
	private final String source;
	TmxUnit(TypedNode node){
		if(!isUnit(node))throw new IllegalArgumentException("Not a tu: "+node);
		this.node=(ValueNode)node;
		updated=readUpdated();
		source=getUnitSource(node);
	}
	static boolean isUnit(TypedNode node){
		return node.type().equals("tu");
	}
	private LocalDateTime readUpdated(){
		ValueNode atts=node;
		String attr=atts.get("changedate");
		if(attr==null)attr=atts.get("creationdate");
		if(attr==null)return LocalDateTime.now();
		String raw=attr.replaceAll("(\\d{4})(\\d{2})(\\d{2})T(\\d{2})(\\d{2})(\\d{2})Z",
				"$1 $2 $3 $4 $5 $6"),
			splits[]=raw.split(" ");
		int year=valueOf(splits[0]),month=valueOf(splits[1]),day=valueOf(splits[2]),
				hour=valueOf(splits[3]),min=valueOf(splits[4]),sec=valueOf(splits[5]);
		return LocalDateTime.of(year,month,day,hour,min,sec);
	}
	static String getUnitSource(TypedNode node){
		if(!isUnit(node))throw new IllegalArgumentException("Not a tu: "+node);
		else return Objects.toString(descendantTyped(node,"seg").values()," ");
	}
	static String newUnitKey(TypedNode node){
		String source=getUnitSource(node);
		if(false)((ValueNode)node).put("words",source.split("[ ,;\\-:]+").length);
		return new TmxUnit(node).updated+source;
	}
	@Override
	public int compareTo(TmxUnit u){
		int update=u.updated.compareTo(updated);
		return update!=0?update:u.source.compareTo(source);
	}
	@Override
	public boolean equals(Object o){
		TmxUnit u=(TmxUnit)o;
		return compareTo(u)==0&&u.source.equals(source);
	}
	@Override
	public int hashCode(){
		return updated.hashCode();
	}
	@Override
	public String toString(){
		return updated.toString()+">"+getUnitSource(node);
	}
	static List<TmxUnit>newBodyUnits(TypedNode body){
		List<TmxUnit>units=new ArrayList();
		for(TypedNode child:body.children())units.add(new TmxUnit(child));
		return units;
	}
	static NodeList newBodyList(TypedNode body,List<TmxUnit>units){
		NodeList list=new NodeList(body,false);
		list.clear();
		for(TmxUnit unit:units)list.add(unit.node);
		list.updateParent();
		return list;
	}
}
