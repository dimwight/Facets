package facets;
import static facets.core.app.PathSelection.*;
import static facets.facet.FacetFactory.*;
import static facets.facet.app.FacetConstants.*;
import static facets.util.tree.Nodes.*;
import facets.core.app.ActionAppSurface;
import facets.core.app.AppSpecifier;
import facets.core.app.ListView;
import facets.core.app.NodeViewable;
import facets.core.app.PathSelection;
import facets.core.app.TreeView;
import facets.core.app.AppSurface.ContentStyle;
import facets.core.superficial.SFacet;
import facets.core.superficial.SIndexing;
import facets.core.superficial.STarget;
import facets.core.superficial.STargeter;
import facets.core.superficial.app.SSelection;
import facets.core.superficial.app.SView;
import facets.core.superficial.app.SViewer;
import facets.core.superficial.app.SelectingFrame;
import facets.facet.FacetFactory;
import facets.facet.app.FacetAppSpecifier;
import facets.facet.app.FacetAppSurface;
import facets.facet.app.tree.TreeAppSpecifier;
import facets.facet.kit.Toolkit;
import facets.facet.kit.swing.KitSwing;
import facets.util.Debug;
import facets.util.Objects;
import facets.util.OffsetPath;
import facets.util.Tracer;
import facets.util.Util;
import facets.util.tree.DataNode;
import facets.util.tree.TypedNode;
import facets.util.tree.ValueNode;
import facets.util.tree.XmlSpecifier;
/**
{@link TreeAppSpecifier} that defines a utility application for viewing and editing 
simple tree content.
<p>{@link XmlView} provides a minimal subset of the functionality defined
 by {@link TreeAppSpecifier}:
<ul>
  <li>viewing and editing of XML files
  <li>search by XML tags, content, attributes and attribute values 
  <li>changing XML tag names case by case or globally 
  <li>save and read of compressed files 
  <li>encoding and decoding of content as ASCII text 
  <li>multiple document interface 
  <li>quick-expand tree display 
  <li>status bar with information on selected element
  <li>content selection restored on file reload 
</ul>
<p>More complex applications can be based on {@link TreeAppSpecifier} by 
reimplementing methods as listed below; 
{@link XmlView} itself provides examples which can be switched on by 
passing {@link #ARG_NO_FILES} to {@link #main(String[])}. 

<p>In {@link TreeAppSpecifier}:
<ul>
<li>{@link #xmlPolicy()} to redefine {@link XmlSpecifier}s used in file dialogs
<li>{@link #newContentViews(NodeViewable)} to add and customise content viewers
<li>{@link #newContentRootTargets(FacetAppSurface)} to define additional {@link STarget}s
 to be exposed in the {@value facets.facet.app.tree.TreeTargets#TITLE_MENU} menu; 
 and {@link #newTreeMenuItems(FacetFactory, STargeter[], STargeter[])}
  to define the {@link SFacet}s exposing them.  
<li>{@link #getInternalContentSource()} to redefine default content
</ul>
<p>In {@link FacetAppSpecifier}:
<ul>
<li>{@link #canCreateContent()} to toggle content creation 
<li>{@link #isFileApp()} to toggle file open/close 
<li>{@link #canSaveContent()} to toggle file save/restore 
<li>{@link #contentStyle()} to redefine content presentation
<li>{@link #newToolkit()} to redefine GUI kit used or LaF
</ul>
<p>In {@link AppSpecifier}:
<ul>
<li>{@link #canEditContent()} to toggle editing
<li>{@link #canOverwriteContent()} to toggle overwriting files
<li>{@link #offersHelp()} if help resources are available  
</ul>
<p>In {@link Tracer}:
<ul>
<li>{@link #traceOutput(String)} to toggle debug tracing
</ul>
 */
final public class XmlView extends TreeAppSpecifier{
	final private static boolean releaseReady=System.getProperty("XmlViewDebug")==null;
	private final DataNode demoRoot=new DataNode("Demo","appName");
	private int demos;
	public XmlView(){
		super(XmlView.class);
	}
	@Override
	public String[]argumentKeys(){
		return releaseReady?new String[]{ARG_GRAPH_BUILD,ARG_NO_FILES,ARG_PREFERENCES}
			:super.argumentKeys();
	}
	@Override
	public ContentStyle contentStyle(){
		return releaseReady?ContentStyle.DESKTOP:super.contentStyle();
	}
	@Override
	public Toolkit newToolkit(){
		return releaseReady?new KitSwing(true,false,true):super.newToolkit();
	}
	@Override
	protected void addNatureDefaults(ValueNode root){
		super.addNatureDefaults(root);
		mergeContents(root,new Object[]{NATURE_DEBUG+"="+!releaseReady});
	}
	@Override
	public boolean isFileApp(){
		return isDemo()?false:super.isFileApp();
	}
	private boolean isDemo(){
		return nature().getBoolean(ARG_NO_FILES)&&releaseReady;
	}
	@Override
	protected Object getInternalContentSource(){
		if(!isDemo())return super.getInternalContentSource();
		demoRoot.setChildren((TypedNode)nature().copyState(),(TypedNode)state().copyState());
		demoRoot.setTitle("Demo"+demos++);
		return demoRoot;
	}
	@Override
	protected SView[]newContentViews(NodeViewable viewable){
		if(!isDemo())return super.newContentViews(viewable);
		viewable.defineSelection(((TypedNode)viewable.framed).children()[0]);
		final boolean liveViews=canEditContent();
		return new SView[]{
			new TreeView("Outline"){
				public boolean isLive(){
					return liveViews;
				}
			},
			new TreeView("Node"){
				@Override
				public SSelection newViewerSelection(SViewer viewer,
						SSelection viewable){
					PathSelection pathsIn=(PathSelection)viewable;
					Object[]members=pathMembers(pathsIn,0);
					return procrust(pathsIn,members[members.length-1]);
				}
				@Override
				public boolean isLive(){
					return liveViews;
				};
			}
		};
	}
	@Override
	protected STarget[]newContentRootTargets(final FacetAppSurface app){
		if(!isDemo())return super.newContentRootTargets(app);
		return new STarget[]{
			new SIndexing("Select Node",demoRoot.children(),
					0,new SIndexing.Coupler(){
				@Override
				public String[]newIndexableTitles(SIndexing i){
					String titles="";
					for(TypedNode node:(TypedNode[])i.indexables())
						titles+=node.type()+"\n";
					return titles.split("\n");
				}
				@Override
				public void indexSet(SIndexing i){
					DataNode node=(DataNode)i.indexed();
					app.dialogs().infoMessage("Selecting Node",node.type());
					((SelectingFrame)app.findActiveContent().contentFrame()).defineSelection(node);
				}
			})
		};
	}
	@Override
	protected SFacet[]newTreeMenuItems(FacetFactory ff,STargeter[]treeLinks,
			STargeter[]contentLinks){
		if(!isDemo())return super.newTreeMenuItems(ff,treeLinks,contentLinks);
		else return new SFacet[]{
			ff.indexingRadioButtonMenu(contentLinks[0],FacetFactory.HINT_NONE)
		};
	}
	public static void main(String[]args){
		if(false)for(int i=1;i<12;i+=2)
			newTestTree("Count",i);
		else if(true)new XmlView().buildAndLaunchApp(args);
	}
}
