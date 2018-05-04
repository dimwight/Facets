package applicable.refs;
import static applicable.refs.TextReferences.*;
import static facets.util.Debug.*;
import static facets.util.Times.*;
import static facets.util.Util.*;
import static facets.util.tree.Nodes.*;
import static java.lang.Math.*;
import static java.util.Arrays.*;
import static java.util.Collections.*;
import facets.util.Debug;
import facets.util.Identified;
import facets.util.Objects;
import facets.util.Tracer;
import facets.util.Util;
import facets.util.tree.DataNode;
import facets.util.tree.NodeList;
import facets.util.tree.Nodes;
import facets.util.tree.TypedNode;
import facets.util.tree.ValueNode;
import facets.util.tree.XmlPolicy;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import applicable.refs.TextReferences.Policy;
import applicable.refs.TextReferences.ReadSourceProvider;
import applicable.refs.TextReferences.RefsStrategy;
final class StringReferences<Source extends Identified> extends Tracer 
		implements Serializable{
	final static class RefsFilter{
		static final RefsFilter NONE=new RefsFilter(RefsStrategy.None,-1);
		final RefsStrategy strategy;
		private final int threshold;
		RefsFilter(RefsStrategy strategy,int threshold){
			this.strategy=strategy;
			this.threshold=threshold;
		}
		boolean passes(Refs refs){
			return weighting(refs)>threshold;
		}
		int compareWeightings(Refs p,Refs q){
			return weighting(p)-weighting(q);
		}
		private int weighting(Refs refs){
			int refCount=refs.refCount(),strCount=refs.strCount;
			return strategy==RefsStrategy.None?0
				:strategy==RefsStrategy.ByStrs?refCount*strCount
						:(int)sqrt(refCount*strCount*strCount);
		}
		public String toString(){
			return strategy+" threshold="+threshold;
		}
	}
	static final boolean miniTrace=false;
	private static final boolean halfRanges=true,arrays=true;
	final static int arrayDefault=miniTrace?1:1;
	static final int microLimit=5;
	static final int BYTES_LIMIT=miniTrace?100
			:Byte.MAX_VALUE-(halfRanges?0:Byte.MIN_VALUE), 
		SHORTS_LIMIT=miniTrace?1000:Short.MAX_VALUE-(halfRanges?0:Short.MIN_VALUE);
	static final String 
		TYPE_SOURCE_REF="sourceRef",TYPE_SOURCE_REF_MAP="sourceRefMap",
		TYPE_STR_REFS="strRefs",TYPE_STR_REFS_MAP="strRefsMap",
		TYPE_REFS="refs",TYPE_VALUES="values",TYPE_STR_COUNT="strCount",
		TYPE_REFS_LIST="refsList",KEY_REFS_AT="refsAt",
		KEY_CASE="caseSensitive",KEY_COUNT="sourceCount",KEY_DATE="date",KEY_VERBOSE="verbose";
	static final XmlPolicy XML_POLICY=new XmlPolicy(){
		protected boolean treeAsXmlRoot(){ 
			return true;
		}
		protected ValueNode getTitleAttributeNames(){
			return newTitleAttributeNames("title",new String[]{
					TYPE_SOURCE_REF+"=ref",
					TYPE_STR_REFS+"=str",
					TYPE_REFS+"=at",
					TYPE_VALUES+"=count",
					TYPE_SOURCE_REF_MAP+"=count",
					TYPE_STR_REFS_MAP+"=count",
					TYPE_REFS_LIST+"=count",
			});
		}
	};
	private static class Refs implements java.io.Serializable{
		private final transient StringReferences sr;
		private final Set<Number>list=arrays?null:new HashSet();
		private int[]ints;
		private short[]shorts;
		private byte[]bytes;
		private int nextAt;
		int strCount;
		Refs(StringReferences sr){
			this.sr=sr;
			ints=!arrays||!(sr.ref instanceof Integer)?null:new int[arrayDefault];
			shorts=!arrays||!(sr.ref instanceof Short)?null:new short[arrayDefault];
			bytes=!arrays||!(sr.ref instanceof Byte)?null:new byte[arrayDefault];
		}
		Refs(StringReferences sr,String[]refs){
			this(sr);
			for(String ref:refs)try{
				addRef(sr.newRef(Integer.valueOf(ref)));
			}catch(Exception e){
				Util.printOut("StringReferences.Refs: ref='"+ref+"' e=",e);
			}
		}
		void addRef(Number ref){
			if(list!=null)list.add(ref);
			else if(ints!=null&&(nextAt==0||ints[nextAt-1]!=ref.intValue())){
				if(nextAt>ints.length-1)ints=copyOf(ints,nextAt*2);
				ints[nextAt++]=ref.intValue();
			}
			else if(shorts!=null&&(nextAt==0||shorts[nextAt-1]!=ref.shortValue())){
				if(nextAt>shorts.length-1)shorts=copyOf(shorts,nextAt*2);
				shorts[nextAt++]=ref.shortValue();
			}
			else if(bytes!=null&&(nextAt==0||bytes[nextAt-1]!=ref.shortValue())){
				if(nextAt>bytes.length-1)bytes=copyOf(bytes,nextAt*2);
				bytes[nextAt++]=ref.byteValue();
			}
		}
		int refCount(){
			return list!=null?list.size():nextAt;
		}
		Collection<Number>refs(){
			if(list!=null)return list;
			ArrayList<Number>asList=new ArrayList();
			for(int at=0;at<nextAt;at++)
				if(ints!=null)asList.add(new Integer(ints[at]));
				else if(shorts!=null)asList.add(new Short(shorts[at]));
				else asList.add(new Byte(bytes[at]));
			return asList;
		}
		void trim(){
			if(list!=null){
				ArrayList refs=new ArrayList(list);
				list.clear();
				list.addAll(refs);
			}
			else if(ints!=null)ints=copyOf(ints,max(nextAt,arrayDefault));
			else if(shorts!=null)shorts=copyOf(shorts,max(nextAt,arrayDefault));
			else bytes=copyOf(bytes,max(nextAt,arrayDefault));
		}
		public boolean equals(Object o){
			if(false)return this==o;
			Refs that=(Refs)o;
			boolean equals=list!=null?list.equals(that.list)
					:ints!=null?Arrays.equals(ints,that.ints)
							:shorts!=null?Arrays.equals(shorts,that.shorts)
									:Arrays.equals(bytes,that.bytes);
			if(true)return equals;
			printOut("StringReferences.Refs.equals: ",equals);
			printOut("",this.refs());
			printOut("",that.refs());
			return equals;
		}
		public int hashCode(){
			return list!=null?list.hashCode()
					:ints!=null?ints.hashCode()
							:shorts!=null?shorts.hashCode()
									:bytes.hashCode();
		}
	}
	Number ref;
	Number newRef(int value){
		Number newRef=null;
		if(ref instanceof Integer)newRef=new Integer(value);
		else if(ref instanceof Short)newRef=new Short((short)value);
		else if(ref instanceof Byte)newRef=new Byte((byte)value);
		else throw new IllegalStateException("Null newRef for value="+value);
		return newRef;
	}
	private final Map<Number,Source>sourceRefMap=new HashMap(); 
	private final Map<String,Refs>strRefsMap=new HashMap();
	private final List<Refs>refsList=new ArrayList();
	private final int sourcesMax;
	private final boolean microTrace;
	private Source sourceThen;
	private int bytesThen;
	boolean closed;
	StringReferences(int sourcesMax){
		if((this.sourcesMax=sourcesMax)<=0)throw new IllegalArgumentException(
				"Nil sourcesMax in "+Debug.info(this));
		microTrace=miniTrace&&sourcesMax<=microLimit;
		if(!arrays||sourcesMax>SHORTS_LIMIT)
			ref=new Integer(miniTrace||halfRanges?-1:Integer.MIN_VALUE);
		else if(sourcesMax>BYTES_LIMIT)
			ref=new Short(miniTrace||halfRanges?-1:Short.MIN_VALUE);
		else ref=new Byte(miniTrace||halfRanges?-1:Byte.MIN_VALUE);
		traceStateAndSize("Empty");
	}
	void addSourceReference(String str,Source source){
		if(str==null||str.trim().equals(""))throw new IllegalStateException(
				"Null or empty str='"+str+"'");
		if(closed)throw new IllegalStateException("Cannot add to closed "+Debug.info(this));
		if(source!=sourceThen){
			if(sourceRefMap.containsValue(source))
				throw new IllegalArgumentException("Duplicate source="+info(source));
			else if(sourceRefMap.size()>sourcesMax)throw new IllegalStateException(
					"sourceRefMap=" +sourceRefMap.size()+" too big for sourcesMax="+sourcesMax);
			else if(sourceThen!=null&&microTrace)traceStateAndSize("Closed source");
			ref=newRef(ref.intValue()+1);
			if(microTrace)traceStateAndSize("Updated ref");
			sourceRefMap.put(ref,sourceThen=source);
		}
		if(ref==null)throw new IllegalStateException(
				"Null ref in "+Debug.info(this));
		Refs refs=strRefsMap.get(str);
		if(refs==null){
			strRefsMap.put(str,refs=new Refs(this));
			if(microTrace)traceStateAndSize("Added refs str="+str);
		}
		refs.addRef(ref);
		if(microTrace)traceStateAndSize("Added ref str="+str);
	}
	void close(Policy policy){
		traceStateAndSize("Closing refsFilter="+policy.refsFilter());
		List<Refs>refsValues=new ArrayList(new HashSet(strRefsMap.values()));
		Set<Refs>refsSet=new HashSet();
		List<Entry<String,Refs>>pairs=new ArrayList(strRefsMap.entrySet());
		final RefsFilter filter=policy.refsFilter();
		boolean noRefs=filter.strategy==RefsStrategy.None;
		for(Entry<String,Refs>pair:pairs){
			Refs refs=noRefs?pair.getValue()
					:refsValues.get(refsValues.indexOf(pair.getValue()));
			pair.setValue(refs);
			refs.strCount++;
			refsSet.add(refs);
		}
		closed=true;
		if(!noRefs)for(Refs refs:refsSet)
			if(filter.passes(refs))refsList.add(refs);
		sort(refsList,new Comparator<Refs>(){
			public int compare(Refs p,Refs q){
				int weighting=filter.compareWeightings(p,q);
				return weighting!=0?weighting:p.refCount()-q.refCount();
			}
		});
		traceStateAndSize("Closed");
	}
	void buildTree(ValueNode root){
		boolean verbose=root.getBoolean(KEY_VERBOSE);
		NodeList sourceRefMap=new NodeList(new DataNode(TYPE_SOURCE_REF_MAP,
				""+this.sourceRefMap.size()),false),
			strRefsMap=new NodeList(new DataNode(TYPE_STR_REFS_MAP,
					""+this.strRefsMap.size()),false),
			refsList=new NodeList(new DataNode(TYPE_REFS_LIST,
					""+this.refsList.size()),false);
		root.setChildren(sourceRefMap.parent,strRefsMap.parent,refsList.parent);
		List<Number>sourceRefs=new ArrayList(this.sourceRefMap.keySet());
		sort(sourceRefs,new Comparator<Number>(){
			public int compare(Number p,Number q){
				return p.intValue()-q.intValue();
			}
		});
		for(Number ref:sourceRefs)sourceRefMap.add(
				new DataNode(TYPE_SOURCE_REF,""+ref,
					new Object[]{this.sourceRefMap.get(ref).identity()}));
		sourceRefMap.updateParent();
		printElapsed("StringReferences.buildTree: root="+root.title()+
				" "+debugNodeData(sourceRefMap));
		List<Entry<String,Refs>>strRefsPairs=new ArrayList(this.strRefsMap.entrySet());
		final Map<String,Integer>strRefsAtMap=new HashMap();
		for(Entry<String,Refs>strRefs:strRefsPairs)
			strRefsAtMap.put(strRefs.getKey(),this.refsList.indexOf(strRefs.getValue()));
		sort(strRefsPairs,new Comparator<Entry<String,Refs>>(){
			public int compare(Entry<String,Refs> p,Entry<String,Refs> q){
				String thisStr=p.getKey(),thatStr=q.getKey();
				int thisRefsAt=strRefsAtMap.get(thisStr),thatRefsAt=strRefsAtMap.get(thatStr),
					thisRefCount=p.getValue().refCount(),
					thatRefCount=q.getValue().refCount(),
					compareRefsAts=thisRefsAt-thatRefsAt,
					compareRefCounts=thisRefCount-thatRefCount;
				return compareRefsAts!=0?compareRefsAts
						:compareRefCounts!=0?compareRefCounts:thisStr.length()-thatStr.length();
			}
		});
		for(Entry<String,Refs>strRefs:strRefsPairs){
			Refs refs=strRefs.getValue();
			int refsAt=strRefsAtMap.get(strRefs.getKey());
			String str=strRefs.getKey();
			if(str==null||str.trim().equals(""))throw new IllegalStateException(
					"Null or empty str='"+str+"'");
			strRefsMap.add(new ValueNode(TYPE_STR_REFS,str,new Object[]{
					refsAt>-1?(KEY_REFS_AT+"="+refsAt):newRefsValues(refs,verbose)}));
		}
		strRefsMap.updateParent();
		for(Refs refs:this.refsList)refsList.add(
				new DataNode(TYPE_REFS,""+this.refsList.indexOf(refs),
					verbose?new Object[]{TYPE_STR_COUNT+"="+refs.strCount,newRefsValues(refs,verbose)}
					:new Object[]{newRefsValues(refs,verbose)}
				));
		refsList.updateParent();
		printElapsed("StringReferences.~buildTree: "+debugNodeData(strRefsMap)+
				" "+debugNodeData(refsList));
	}
	private Object newRefsValues(Refs refs,boolean verbose){
		String values=Objects.toString(refs.refs().toArray());
		return!verbose?values:new DataNode(TYPE_VALUES,""+refs.refCount(),
				new Object[]{values});
	}
	void buildFromTree(ValueNode root,ReadSourceProvider rsp){
		if(trace)printElapsed("StringReferences.buildFromTree: root="+Nodes.descendants(root).length);
		if(sourceRefMap.size()>0)throw new IllegalStateException(
				"Already read into "+Debug.info(this));
		for(TypedNode child:child(root,TYPE_SOURCE_REF_MAP).children())
			sourceRefMap.put(newRef(Integer.valueOf(child.title())),
					(Source)rsp.newReadSource(child.contents()[0]));
		if(trace)printElapsed("StringReferences.buildFromTree: sourceRefMap="+sourceRefMap.size());
		boolean verbose=root.getBoolean(KEY_VERBOSE);
		for(TypedNode child:child(root,TYPE_REFS_LIST).children())
			refsList.add(newNodeValueRefs(child,verbose));
		if(trace)printElapsed("StringReferences.buildFromTree: refsList="+refsList.size());
		for(TypedNode child:child(root,TYPE_STR_REFS_MAP).children()){
			int refAt=((ValueNode)child).getInt(KEY_REFS_AT);
			strRefsMap.put(child.title(),refAt>Integer.MIN_VALUE?refsList.get(refAt)
					:newNodeValueRefs(child,verbose));
		}
		List<Refs>refsSet=new ArrayList(new HashSet(strRefsMap.values()));
		for(Entry<String,Refs>pair:strRefsMap.entrySet())
			pair.setValue(refsSet.get(refsSet.indexOf(pair.getValue())));
		if(trace)printElapsed("StringReferences.buildFromTree: strRefsMap="+strRefsMap.size());
		traceStateAndSize("Closed read");
	}
	private Refs newNodeValueRefs(TypedNode node,boolean verbose){
		return new Refs(this,((String)
				(!verbose?node:child(node,TYPE_VALUES)).contents()[0]).split(","));
	}
	private String debugNodeData(NodeList list){
		DataNode node=list.parent;
		return node.type()+"=" +node.children().length+
				" :="+Util.sf(Nodes.treeString(node).length());
	}
	Set<Source>getReferenceSources(String str){
		if(str.trim().equals(""))return new HashSet(sourceRefMap.values());
		if(false)trace(": str="+str);
		Set<Source>sources=new HashSet();
		for(String key:strRefsMap.keySet())
			if(!key.contains(str))continue;
			else{
				if(false&&!TextReferences.trace)
					trace(": matched key="+key);
				for(Number ref:strRefsMap.get(key).refs())
					sources.add(sourceRefMap.get(ref));
			}
		return sources;
	}
	static Collection<String>getTreeStrings(DataNode tree){
		ArrayList<String> strings=new ArrayList();
		for(TypedNode strRefs:descendantsTyped(tree,TYPE_STR_REFS))
			strings.add(strRefs.title());
		return strings;
	}
	private void traceStateAndSize(String msg){
		if(!trace||!msg.startsWith("Clos"))return;
		for(Refs refList:strRefsMap.values())refList.trim();
		int bytes=Util.byteCount(this);
		msg="StringReferences: " +msg+
		": arrays="+arrays+
		", ref="+Debug.info(ref)+
		(true?"":(", sourceRef:="+Util.byteCount((Serializable)sourceRefMap.get(new Byte((byte)0)))))+
		",\n sourceRefs="+sourceRefMap.size()+
		":="+sf(Util.byteCount((Serializable)sourceRefMap))+
		", strRefs="+new HashSet(strRefsMap.values()).size()+
		":="+sf(Util.byteCount(new HashSet(strRefsMap.values()).toArray()))+
		", refs="+refsList.size()+
		":="+sf(Util.byteCount(refsList.toArray()))+
		", :="+sf(bytes)+(true?"":(" +"+sf(bytes-bytesThen)));
		bytesThen=bytes;
		List<String>keys=new ArrayList(strRefsMap.keySet());
		if(false&&keys.size()>100)Collections.sort(keys,new Comparator<String>(){
			public int compare(String key1,String key2){
				return false?key1.compareTo(key2)
					:((Integer)strRefsMap.get(key2).refCount()
						).compareTo(strRefsMap.get(key1).refCount());
			}
		});
		int keyCount=keys.size();
		if(false&&0<keyCount&&keyCount<100){
			msg+="\n";
			for(String key:keys)
				msg+=(" "+key+">"+strRefsMap.get(key).refCount());
		}
		trace(msg);
		printElapsed("StringReferences.~traceStateAndSize: ");
	}
	Collection<String>wordSet(){
		return new TreeSet(strRefsMap.keySet());
	} 
}