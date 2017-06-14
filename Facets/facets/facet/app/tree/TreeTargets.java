package facets.facet.app.tree;
import static facets.core.app.AppConstants.*;
import static facets.util.tree.DataConstants.*;
import static facets.util.tree.Nodes.*;
import facets.core.app.Dialogs;
import facets.core.app.NodeViewable;
import facets.core.app.PathSelection;
import facets.core.superficial.STarget;
import facets.core.superficial.STrigger;
import facets.core.superficial.TargetCore;
import facets.core.superficial.STrigger.Coupler;
import facets.facet.app.FacetAppSurface;
import facets.facet.app.FileAppActions;
import facets.util.Objects;
import facets.util.OffsetPath;
import facets.util.TextLines;
import facets.util.Tracer;
import facets.util.tree.DataConstants;
import facets.util.tree.DataNode;
import facets.util.tree.Nodes;
import facets.util.tree.TypedNode;
import facets.util.tree.ValueNode;
import facets.util.tree.XmlDocRoot;
import facets.util.tree.XmlPolicy;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
/**
Defines {@link STarget}s encapsulating useful actions on {@link DataNode} content. 
 */
public class TreeTargets extends Tracer{
	public static final int TARGET_SEARCH=0,TARGET_TYPE=1,TARGET_ENCODE=2,
		TARGET_NAMES=3,TARGET_TABLE=4,TARGETS_TREE=TARGET_TABLE+1;
	public static final String TYPE_TABLE_ROWS="TableRows",TYPE_TABLE_ROW="TableRow",
		TITLE_MENU="Tree",TITLE_FIND="Find...",TITLE_FIND_NEXT="Find &Next",
		TITLE_SEARCH_FIND="Find in Tree",TS=TreeSearch.class.getSimpleName(),
		TITLE_SEARCH_TYPE=TS+"Type",TITLE_SEARCH_TITLE=TS+"Title",
		TITLE_SEARCH_KEY=TS+"Key",TITLE_SEARCH_VALUE=TS+"Value",TITLE_SEARCH_RESULTS=TS+"Results";
	private final List<OffsetPath>searchStore=new ArrayList();
	private final Coupler encodeDecode=new Coupler(){
		@Override
		public void fired(STrigger t){
			ValueNode selected=selectedValueNode();
			if(selected==null)return;
			if(t==encode)Nodes.encode(selected,50);
			else Nodes.decode(selected);
			tree.updateStateStamp();
		}
	};
	private final STarget 
	find=new STrigger(TITLE_FIND,new Coupler(){
		@Override
		public void fired(STrigger t){
			ValueNode selected=selectedValueNode();
			if(selected!=null)TreeSearch.launchDialog(
					new TreeSearch(app,viewable,searchStore));
			findNext.setLive(searchStore.size()>0);
		}
	}),
	findNext=new STrigger(TITLE_FIND_NEXT,new Coupler(){
		@Override
		public void fired(STrigger t){
			if(searchAt>searchStore.size()-1)searchAt=0;
			viewable.defineSelection(new PathSelection(tree,searchStore.get(searchAt++)));
		}
	}),
	encode=new STrigger("Encode",encodeDecode),
	decode=new STrigger("Decode",encodeDecode),
	type=new STrigger("Change &Type...",new Coupler(){
		@Override
		public void fired(STrigger t){
			ValueNode selected=selectedValueNode();
			String typeThen=selected.type();
			if(selected!=null)TreeTypeChange.launchDialog(
					new TreeTypeChange(app,viewable));
			String typeNow=selected.type();
			if(typeNow.equals(typeThen))return;
			if(app.dialogs().confirmYesNo("Generalise Change?",
					"Change all nodes of type " +typeThen+" to type " +typeNow+"?")
					==Dialogs.Response.Yes)
				for(TypedNode node:descendantsTyped(tree,typeThen))
					node.setValidType(typeNow);
			tree.updateStateStamp();
		}
	}),
	insertTable=new STrigger("Insert &Table Rows",new STrigger.Coupler(){
		@Override
		public void fired(STrigger t){
			ValueNode selected=selectedValueNode();
			if(selected==null)return;
			String title=selected.title();
			File file=new File(((FileAppActions)app.actions).values().stateGetPath(),
					title+"."+DataConstants.TYPE_TXT);
			DataNode rows=new ValueNode(TYPE_TABLE_ROWS,title);
			try{
				new XmlDocRoot(rows,new XmlPolicy(){
					@Override
					protected boolean treeAsXmlRoot(){
						return true;
					}
				}).readFromSource(newTableRowsXml(file));
			}catch(IOException e){
				app.dialogs().errorMessage("Insert failed","Could not read table from "+file);
				return;
			}
			DataNode copy=(DataNode)selected.copyState();
			copy.setTitle("_"+title);
			selected.setChildren(Objects.join(TypedNode.class,
					new TypedNode[]{copy},rows.children()));
			viewable.defineSelection(copy);
			selected.put("date",new Date().toString());
			tree.updateStateStamp();
		}
	}),
	listNames=new STrigger("List "+getNameListType()+" &Names",new STrigger.Coupler(){
		@Override
		public void fired(STrigger t){
			ValueNode selected=selectedValueNode();
			if(selected==null)return;
			Set<String>out=new HashSet();
			for(TypedNode node:descendantsTyped(selected,getNameListType()))
				out.add(node.title());
			List<String>sort=new ArrayList(out);
			Collections.sort(sort);
			trace(".fired: " +t.title(),sort);
		}
	});
	private final FacetAppSurface app;
	private final NodeViewable viewable;
	private final TypedNode tree;
	private int searchAt;
	public TreeTargets(FacetAppSurface app,NodeViewable viewable){
		this.app=app;
		this.viewable=viewable;
		tree=(TypedNode)viewable.framed;
		findNext.setLive(false);
	}
	public final STarget[]appTargets(){
		TargetCore codecs=new TargetCore("Encoding",encode,decode),
			finds=new TargetCore("Find Nodes",find,findNext);
		return new STarget[]{finds,type,codecs,listNames,insertTable};
	}
	protected String getNameListType(){
		return TYPE_DATA;
	}
	private ValueNode selectedValueNode(){
		Object selection=viewable.selection().single();
		return!(selection instanceof ValueNode)?null:(ValueNode)selection;
	}
	public static TextLines newTableRowsXml(File tabbed)throws IOException{
		StringBuilder out=new StringBuilder(TextLines.newXmlTop());
		out.append("<"+TYPE_TABLE_ROWS+">");
		for(String line:new TextLines(tabbed).readLines())
			out.append("<"+TYPE_TABLE_ROW+">"+line.replaceAll("\t","\n")+"</"+TYPE_TABLE_ROW+">\n");
		out.append("</"+TYPE_TABLE_ROWS+">");
		return TextLines.newBuffer(out.toString().split("\n"));
	}
	static void main(String[]args)throws IOException{
		TextLines.setDefaultEncoding(true);
		if(true){
			String fileName="Picks";
			XmlDocRoot root=new XmlDocRoot(new DataNode(TYPE_TABLE_ROWS,DataNode.UNTITLED),
					new XmlPolicy());
			File dir=new File("C:/eclipse/workspace/Config"),
				tabbedIn=new File(dir,fileName+"."+TYPE_TXT),
				xmlOut=new File(dir,fileName+"."+TYPE_XML);
			TextLines xmlIn=newTableRowsXml(tabbedIn);
			root.readFromSource(xmlIn);
			root.writeToSink(xmlOut);
			return;
		}
	}
}
