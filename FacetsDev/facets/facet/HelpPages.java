package facets.facet;
import static facets.core.app.PathSelection.*;
import static facets.core.app.TextTreeView.*;
import static facets.core.app.TextView.*;
import static facets.facet.FacetFactory.*;
import static facets.util.HtmlBuilder.*;
import static facets.util.Util.*;
import facets.core.app.PathSelection;
import facets.core.app.SViewer;
import facets.core.app.TextTreeView;
import facets.core.app.ViewableFrame;
import facets.core.superficial.SFacet;
import facets.core.superficial.SIndexing;
import facets.core.superficial.STarget;
import facets.core.superficial.STextual;
import facets.core.superficial.Notifying.Impact;
import facets.core.superficial.SIndexing.Coupler;
import facets.core.superficial.app.SSelection;
import facets.util.FileNode;
import facets.util.HtmlBuilder;
import facets.util.IndexingIterator;
import facets.util.ItemList;
import facets.util.OffsetPath;
import facets.util.Tracer;
import facets.util.app.AppValues;
import facets.util.tree.DataNode;
import facets.util.tree.ExceptionNode;
import facets.util.tree.NodePath;
import facets.util.tree.Nodes;
import facets.util.tree.TypedNode;
import facets.util.tree.ValueNode;
import java.util.ArrayList;
final class HelpPages extends Tracer{
	public static final int TARGET_TOPIC=0,TARGET_HISTORY=1,TARGET_FONT=2;
	private int appFontThen=Integer.MAX_VALUE;
	public String newPathNodeText(TypedNode[]path,final String text){
		if(!helpFound())return root.values()[0];
		String title=tagContents(text,"title");
		topic.setText(title);
		final StringBuilder header=new StringBuilder("<p class=\"path\">");
		new IndexingIterator<TypedNode>(path){
			protected void itemIterated(TypedNode node,int at){
				if(!node.type().equals("html"))header.append(itemCode(findFolderIndex(node),at));
				else if(!node.title().equals("index"))header.append(itemCode(node,at));
			}
			private String itemCode(TypedNode node,int at){
				final StringBuilder path=new StringBuilder();
				new IndexingIterator<TypedNode>(Nodes.ancestry(node)){
					protected void itemIterated(TypedNode node,int at){
						if(at>0)path.append(node.title());
						if(at<itemCount-1)path.append("/");
						else path.append(".html");
					}
				}.iterate();
				return (at>0?"<b> &gt; </b>":"")+"<a href=\""+path+"\">"+ 
				tagContents(nodeText(node),"title")+"</a>";
			}
		}.iterate();
		header.append("\n<h1>"+title);
		final String content=header+tagContents(text,"body").replaceAll("\t","  ")+"\n";
		return new HtmlBuilder(){
			@Override
			protected String[]buildPageStyles(double points){
				if(appFontThen!=fontSizeAt)fontSize.setIndex(appFontThen=fontSizeAt);
				double para=points/2;
				return new String[]{
	"h1{font-family:sans-serif;font-size:" +sf(points*1.33)+"pt;"+
		" margin-top:" +para*2+"pt;margin-left:0pt;margin-bottom:0pt;}",
	"h2{font-family:sans-serif;font-size:" +sf(points*1.1)+"pt;"+
		" margin-top:" +para*2+"pt;margin-left:0pt;margin-bottom:0pt;}",
	"p,dd{font-family:sans-serif;font-size:" +points+"pt;line-height:normal;"+
		" margin-top:" +para+"pt;margin-left:0pt;margin-bottom:0pt;}",
	"dt{font-family:sans-serif;font-size:"+points+"pt;" +
		"	font-style:italic;font-weight:normal;color:blue;"+
		" margin-top:" +para*2+"pt;margin-left:0pt;margin-bottom:0pt;}",
	"dl{margin-top:0pt;margin-left:0pt;margin-bottom:" +para+"pt;}",
	"ul{font-family:sans-serif;font-size:+" +points+"pt;\n"+
	" margin-top:" +para+"pt;margin-left:24pt;margin-bottom:"+para+"pt;" +
		" list-style-type:disc}",
	"li{font-family:sans-serif;font-size:" +points+"pt;\n"+
		" margin-top:" +para+"pt;margin-left:0pt;margin-bottom:0pt;"+
		" list-contentStyle-position:outside;}"
	};
			}
			@Override
			public String newPageContent(){
				return content;
			}
			@Override
			protected String pageColor(){
				return "#ffffdd";
			};
			@Override
			protected double pagePoints(){
				return((Integer)fontSize.indexed());
			}		
		}.buildPage();
	}
	private final SIndexing fontSize=new SIndexing("Font Size",fontSizes,fontSizeAt,
			new Coupler(){
		@Override
		public String[]iterationTitles(SIndexing i){
			return new String[]{FONT_SMALLER,FONT_LARGER};
		}
		@Override
		public void indexSet(SIndexing i){
			facet.retarget(STarget.NONE,Impact.DEFAULT);
		}
	}),
	history=new SIndexing("History",new Coupler(){
		@Override
		public Object[] getIndexables(){
			return paths.items();
		}
		@Override
		public String[] iterationTitles(SIndexing i){
			return new String[]{PAGE_BACK,PAGE_FORWARD};
		}
		@Override
		public void indexSet(SIndexing i){
			Object indexed=i.indexed();
			if(indexed!=viewable.selection()) viewable.defineSelection(indexed);
		}
	});
	private final STextual topic=new STextual("Topic:","",new STextual.Coupler());
	public final STarget[]targets=new STarget[]{topic,history,fontSize};
	public final DataNode root;
	public final ViewableFrame viewable;
	private final ItemList<OffsetPath>paths=new ItemList(OffsetPath.class);
	private SFacet facet;
	public HelpPages(AppValues values){
		String title=values.appName;
		topic.setLive(false);
		root=new DataNode("help",title,new Object[]{"No help available for "+title});
		viewable=new ViewableFrame(title,root){
			@Override
			public SSelection defineSelection(Object definition){
				if(definition instanceof OffsetPath)
					definition=new PathSelection(root,((OffsetPath)definition));
				super.defineSelection(definition);
				if(facet==null)return selection();
				OffsetPath path=((PathSelection)selection()).paths[0];
				int historyAt=history.index(),historyCount=paths.size();
				if(history.indexables()==SIndexing.NO_INDEXABLES||
						!path.equals(history.indexed())){
					if(historyAt<historyCount-2)paths.removeAll(new ArrayList(
							paths.subList(historyAt+1,historyCount)));
					paths.remove(path);
					paths.add(path);
					history.setIndexed(path);
				}
				facet.retarget(STarget.NONE,Impact.DEFAULT);
				return selection();
			}
			@Override
			protected void viewerSelectionChanged(SViewer viewer,SSelection selection){
				Object selected=selection.single();
				if(selected instanceof FileNode
						&&((FileNode)selected).type().equals(FileNode.TYPE_FOLDER))
					return;
				PathSelection viewable=viewer.view()instanceof TextTreeView?
						TextTreeView.newHtmlPathSelection((PathSelection)selection(),(String)selected)
					:(PathSelection)selection;
				defineSelection(viewable);
			}
		};
		viewable.defineSelection(newMinimal(root));
	}
	public void updateFacet(SFacet facet){
		if(facet==this.facet)return;
		this.facet=facet;
		if(!(root.contents()[0]instanceof TypedNode)){
			ExceptionNode.throwExceptions=true;
			FileNode.deserializeTree(root,true);
			ExceptionNode.throwExceptions=true;
			if(helpFound())viewable.defineSelection(new PathSelection(root,
					new NodePath(new Object[]{root,findFolderIndex(root)})));
			else fontSize.setLive(false);
		}
	}
	private boolean helpFound(){
		if(false)trace(".helpFound: root.contents=",root.contents()[0]);
		return!(root.contents()[0]instanceof String);
	}
	private TypedNode findFolderIndex(TypedNode folder){
		TypedNode index=null;
		for(TypedNode child:folder.children())
			if(child.title().equals("index"))index=child;
		if(index==null)throw new IllegalStateException("Read not found in "+folder);
		return index;
	}
}