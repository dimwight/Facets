package apps;
import static apps.TmxUnit.*;
import static facets.core.app.ActionViewerTarget.Action.*;
import static facets.core.app.AppConstants.*;
import static facets.util.Objects.*;
import static facets.util.Strings.*;
import static facets.util.Util.*;
import static facets.util.tree.Nodes.*;
import static facets.util.tree.TypedNode.*;
import static java.time.LocalDateTime.*;
import facets.core.app.AppSurface.ContentStyle;
import facets.core.app.NodeViewable;
import facets.core.app.PathSelection;
import facets.core.app.SContenter;
import facets.core.app.SView;
import facets.core.app.SViewer;
import facets.core.app.TreeView;
import facets.core.app.ViewableAction;
import facets.core.superficial.SFacet;
import facets.core.superficial.STarget;
import facets.core.superficial.STargeter;
import facets.core.superficial.STrigger;
import facets.core.superficial.app.SSelection;
import facets.facet.FacetFactory;
import facets.facet.app.FacetAppSurface;
import facets.facet.app.tree.TreeAppSpecifier;
import facets.facet.kit.Toolkit;
import facets.facet.kit.swing.KitSwing;
import facets.util.TextLines;
import facets.util.Times;
import facets.util.Util;
import facets.util.tree.DataNode;
import facets.util.tree.NodeList;
import facets.util.tree.NodePath;
import facets.util.tree.Nodes;
import facets.util.tree.TypedNode;
import facets.util.tree.ValueNode;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Function;
public class TmxView extends TreeAppSpecifier{
	private abstract class DaysView extends TreeView{
		final Map<String,TypedNode> tmxUnits;
		private DaysView(String title,Map<String,TypedNode> tmxUnits){
			super(title);
			this.tmxUnits=tmxUnits;
		}
		@Override
		public boolean allowMultipleSelection(){
			return true;
		}
		@Override
		public String nodeRenderText(TypedNode node){
			if(!isUnit(node))return node.title();
			String use=getUnitSource(node);
			return "["+use.substring(0,Math.min(use.length(),50))+"...]";
		}
	}
	private static final DateTimeFormatter Formatter=DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
	private int extracted=1;
	private STarget extract;
	private TmxView(){
		super(TmxView.class);
	}
	@Override
	protected Object getInternalContentSource(){
		DataNode tmx=Nodes.decode(new ValueNode("tmx",new Object[]{
			"789C6D52CF6B5341101EF3A324D440D20AB562A1DE14ED7B49FC4120E6D21FB481B41E12A598D3266F936E7D791B77E7A5690F82173D7811513C5AA4172108E21FE0494FA2E2A557D173C19B88079D7D49DA885E96DDD9EF9B6FE69BE91D40D45730552D6DB20EB384B4CA5C09E68A1D567379FEE5A778AFBC7F500C0174DB00104618EF70A585F40A19EB925670AAC1EA1CB5E5A3702D549C5B3798EBF335E9F00BB1B31FDFAEFE7C63C80AA6FF012E32640637B5FFE8FAD317E7CB21385685585D7AC83DD40893FDA26C97794DFB5A6D93D731DFFD9F6265BBCD1D93E9F9EF7799CFD528658A1421249C128CB599A26C08A74B7D9A6D68B6A1D987B47C0922480F848911C5322AE135E92FDE311D5502406A04B0E032AD4D4527472B2A2343DEF0DD05A9F8D557A95FEFC3F6B710844A10D5C85A6D6A6B2445917A6D7245225114E8F2DB700762DD36F99A0AE66140D60034F17577EFC7DDFB39B2A908D1A026D24E1EE1D6FC568DAB7BBD2733E38FBF3C188E6C19216C5916DD16497C6C833387ABCE5FC47EA7BBDFF7723BF9D7F301917662607F1F32B0FFE187F567497DCE1D268F231C674E4B78065420A361A6AE3843DA0F876C2864D3995C3A9BCD54D2B9CB57B2176F92C3C37FE1149C2D259A1B349C33C3204AE916563853A8658BCF5656D767971C8152214C8F62863B98B3D25606214962CC4CB0E07BB73CB9457524E41CB61A47EFA4E6CD00A1B9D92F9A1724B4AA0FEA9E5B9ED7C6FB707026A8B3157349D265895C8BD4A4B36D0293BE394F5018FE00E59A12F93A030000"	
		}));
		return new DataNode("Internal","Extracted"+extracted++,new Object[]{tmx});
	}
	@Override
	protected void addNatureDefaults(ValueNode root){
		super.addNatureDefaults(root);
		mergeContents(root,new Object[]{
				NATURE_OPEN_EMPTY+"="+true,
			});
	}
	@Override
	public ContentStyle contentStyle(){
		return true?ContentStyle.TABBED:ContentStyle.DESKTOP;
	}
	@Override
	public Toolkit newToolkit(){
		return new KitSwing(true,false,true);
	}
	@Override
	public boolean isFileApp(){
		return true;
	}
	@Override
	public boolean canCreateContent(){
		return false;
	}
	@Override
	public boolean canEditContent(){
		return true;
	}
	@Override
	protected boolean usesTreeTargets(){
		return true;
	}
	private static void updateDays(NodeList days,NodeList units,
			Map<String,TypedNode>dayUnits,Map<String,TypedNode>tmxUnits){
		Times.printElapsed("TmxView.updateDays");
		dayUnits.clear();tmxUnits.clear();days.clear();
		Consumer<NodeList>closeDay=new Consumer<NodeList>(){
			@Override
			public void accept(NodeList day){
				day.updateParent();
				String title=day.parent.title()+" [units="+day.size()
//						+", words="+((ValueNode)day.parent).get("words")
						+ "]";
				day.parent.setTitle(title);
			}
		};
		NodeList day=null;
		LocalDate after=null;
		int dayWords=0;
		for(TypedNode unit:units){
			final LocalDate before=new TmxUnit(unit).updated.toLocalDate();
			int gap=after==null?0:after.getDayOfYear()-before.getDayOfYear();
			if(after==null||Math.abs(gap)>0){
				if(day!=null)closeDay.accept(day);
				dayWords=0;
				day=new NodeList(new ValueNode("Day",before.toString()),false);
				days.add(day.parent);
			}
			after=before;
			String key=newUnitKey(unit);
			TypedNode got=tmxUnits.get(key);
			if(got!=null)Util.printOut("Duplicate source: "+key+ 
					"\n"+treeString(unit).replaceAll("\n\\.*","")+
					"\n"+treeString(got).replaceAll("\n\\.*",""));
			tmxUnits.put(key,unit);
			unit=(TypedNode)unit.copyState();
			dayUnits.put(key,unit);   
			dayWords+=((ValueNode)unit).getInt("words");
			((ValueNode)day.parent).put("words",dayWords);
			day.add(unit);
		}
		if(day==null)throw new IllegalStateException("Null day in "+days.parent);
		closeDay.accept(day);
		days.updateParent();
		Times.printElapsed("TmxView.updateDays~");
	}
	private TreeView tmx,days;
	private SSelection unitSelection;
	@Override
	protected STarget[]newContentRootTargets(final FacetAppSurface app){
		extract=new STrigger("E&xtract Selection",new STrigger.Coupler(){
			@Override
			public void fired(STrigger t){
				TypedNode root=(TypedNode)getInternalContentSource();
				NodeList extract=new NodeList(descendantTyped(root,"body"),true);
				for(TypedNode unit:newTyped(TypedNode.class,unitSelection.multiple()))
					extract.add((TypedNode)unit.copyState());
				app.addContent(root);
			}
		});
		extract.setLive(false);
		return new STarget[]{extract};
	}
	@Override
	protected ViewableAction[]viewerActions(SView view){
		ViewableAction[]all={COPY,CUT,PASTE,PASTE_INTO,DELETE,MODIFY,UNDO,REDO};
		return view.isLive()?new ViewableAction[]{DELETE,COPY,PASTE,UNDO,REDO}
			:new ViewableAction[]{COPY};
	}
	private static NodeList newCheckedUnits(TypedNode root){
		Times.printElapsed("TmxView.newCheckedUnits");
		TypedNode bodies[]=descendantsTyped(root,"body"),
				bodyNow=bodies[0];
		List<TmxUnit>unitsNow=newBodyUnits(bodyNow),
				unitsThen=bodies.length==1?null:newBodyUnits(bodies[1]),
				check=new ArrayList(unitsNow),trim=new ArrayList();
		if(unitsThen!=null){
			int added=0;
			printOut("Checking unitsThen="+unitsThen.size());
			for(TmxUnit unit:unitsThen) {
				if(unitsNow.contains(unit))continue;
				unitsNow.add(unit);
				printOut("Added "+(++added)+ " unit=",unit);
			}
		}
		Collections.sort(unitsNow);
		for(TmxUnit unit:unitsNow){
			if(trim.contains(unit))printOut("Removed duplicate: "+unit);
			else trim.add(unit);
		}
		unitsNow=trim;
		Times.printElapsed("TmxView.newCheckedUnits~");
		if(!unitsNow.equals(check))
			if(false)throw new IllegalStateException("Unequal check ");
			else getHeader(root).put("creationdate",now().format(Formatter));
		return newBodyList(bodyNow,trim);
	}
	@Override
	protected SView[]newContentViews(NodeViewable viewable){
		TypedNode root=(TypedNode)viewable.framed;
		NodeList units=newCheckedUnits(root);
		Function<TypedNode,Long>sinceModified=node->{
			long now=now().toEpochSecond(ZoneOffset.UTC),
					modified=parse(
							getHeader(node).get("creationdate").replace("Z",""),
							Formatter).toEpochSecond(ZoneOffset.UTC);
				return now-modified;
			};
		if(sinceModified.apply(root)<1)viewable.updateAfterEditAction();
		final Map<String,TypedNode>dayUnits=new HashMap(),tmxUnits=new HashMap();
		NodeList days=new NodeList(new ValueNode("Days",UNTITLED),false);
		updateDays(days,units,dayUnits,tmxUnits);
		this.tmx=new TreeView("TMX"){
			@Override
			public boolean allowMultipleSelection(){
				return true;
			}
			@Override
			public boolean hideRoot(){
				return true;
			}
			@Override
			public boolean isLive(){
				return true;
			}
			@Override
			public String nodeRenderText(TypedNode node){
				String superText=super.nodeRenderText(node);
				if(!isUnit(node))return superText;
				return superText+" ["+new TmxUnit(node).updated+"]";
			}
		};
		this.days=new DaysView("Days",tmxUnits){
			private int countThen;
			@Override
			public SSelection newViewerSelection(SViewer viewer,SSelection input){
				int countNow=descendantTyped((TypedNode)viewable.framed,"body").children().length;
				if(countThen>0&&countNow!=countThen) {
					units.clear();units.addAll(units.parent.children());
					updateDays(days,units,dayUnits,tmxUnits);
				}
				countThen=countNow;
				Object[]inputs=input.multiple();
				if(false)trace(".newViewerSelection: inputs=",inputs.length);
				TypedNode root=days.parent;
				if(!isUnit((TypedNode)inputs[0]))return PathSelection.newMinimal(root);
				Set<TypedNode>days=new HashSet();
				for(TypedNode unit:newTyped(TypedNode.class,inputs))
					days.add(dayUnits.get(newUnitKey(unit)).parent());
				List<NodePath>outputs=new ArrayList();
				for(TypedNode day:days)
					outputs.add(new NodePath(ancestry(day)));
				return new PathSelection(root,outputs.toArray(new NodePath[]{}));
			}
		};
		return new SView[]{this.tmx,this.days};
	}
	@Override
	public boolean viewerSelectionChanged(NodeViewable viewable,SViewer viewer,
			SSelection input){
		SView view=viewer.view();
		if(view!=days)return false;
		Object[]inputs=input.multiple();
		TypedNode root=(TypedNode)viewable.framed,first=(TypedNode)inputs[0];
		PathSelection output=PathSelection.newMinimal(root);
		Function<TypedNode,NodePath>newTmxPath=new Function<TypedNode,NodePath>(){
			@Override
			public NodePath apply(TypedNode dayUnit){
				return new NodePath(ancestry(((DaysView)view).tmxUnits.get(newUnitKey(dayUnit))));
			}
		};
		if(isUnit(first))output=new PathSelection(root,newTmxPath.apply(first));
		else if(first.type().equals("Day")){
			List<NodePath>outputs=new ArrayList();
			for(TypedNode day:newTyped(TypedNode.class,inputs))
				for(TypedNode unit:day.children())outputs.add(newTmxPath.apply(unit));
			output=new PathSelection(root,outputs.toArray(new NodePath[]{}));
		}
		viewable.defineSelection(this.unitSelection=output);
		extract.setLive(true);
		return true;
	}
	
	@Override
	protected SFacet[]newTreeMenuItems(FacetFactory ff,STargeter[]treeLinks,
			STargeter[]contentLinks){
		return new SFacet[]{ff.triggerMenuItems(contentLinks[0],FacetFactory.HINT_NONE)};
	}
	@Override
	protected SContenter newContenter(Object source,FacetAppSurface app){
		class AttsCheck{
			private final String[]lines;
			AttsCheck(String[]lines){
				this.lines=lines;
			}
			void list(){
				SortedSet<String>atts=new TreeSet();
				for(String line:lines){
					String att_=".*\\s([a-z\\-]+)=\".*";
					while(line.matches(att_)) {
						String att=line.replaceAll(att_,"$1");
						atts.add(att);
						line=line.replace(att,"");
						if(false)trace(".listAttributes: att="+att+" line="+line);
				}
				}
				trace(".listAttributes: atts=",atts.toArray());
			}
			String[]clean(){
				String[]clean=new String[lines.length];
				for(int i=0;i<lines.length;i++)
					clean[i]=lines[i].replaceAll("words=\"[^\"]+\"","");
				return clean;
			}
			
		}
		if(source instanceof File)try{
				File file=(File)source;
				String[]lines=new TextLines(file).readLines();
				if(false){
					new AttsCheck(lines).list();
					System.exit(0);
				}
				else if(false){
					String[]clean=new AttsCheck(lines).clean();
					if(!linesString(lines).equals(linesString(clean))){
						file=new File(file.getParent(),"Clean of "+file.getName());
						app.dialogs().infoMessage(
								"Cleaned file!",
								"File loaded will be "+file.getName());
						new TextLines(file).writeLines(clean);
						return super.newContenter(file,app);
					}
				}
			}catch(IOException e){
				throw new RuntimeException(e);
			}
		return super.newContenter(source,app);
	}
	private static ValueNode getHeader(TypedNode root){
		return (ValueNode)descendantTyped(root,"header");
	}
	public static void main(String[]args){
		new TmxView().buildAndLaunchApp(args);
	}
}
