package facets.facet;
import static facets.facet.AreaFacets.*;
import static facets.util.Debug.*;
import facets.core.app.TypeKeyable;
import facets.core.superficial.Facetable;
import facets.core.superficial.FacetedTarget;
import facets.core.superficial.Notifying.Impact;
import facets.core.superficial.SFacet;
import facets.core.superficial.STarget;
import facets.core.superficial.SToggling;
import facets.core.superficial.app.SAreaTarget;
import facets.facet.AreaFacets.PaneLinking;
import facets.facet.HostingFacetSwing.Nested;
import facets.facet.kit.KMount;
import facets.facet.kit.KWrap;
import facets.facet.kit.KitFacet;
import facets.facet.kit.Toolkit;
import facets.facet.kit.swing.KitSwing;
import facets.util.Debug;
import facets.util.Objects;
import facets.util.Strings;
import facets.util.Util;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
/** <h4>Conversion table</h4>
<table border="1" cellpadding="10" cellspacing="0">
    <thead>
      <tr>
        <th align="left">content</th>
        <th>1</th>
        <th>2</th>
        <th>3</th>
        <th>4</th>
        <th>5</th>
        <th><b>=</b>
        </th>
      </tr>
    </thead>
    <tr>
      <td><b>splits</b>
      </td>
      <td>0</td>
      <td>1</td>
      <td>2</td>
      <td>3</td>
      <td>4</td>
      <td>content-1</td>
    </tr>
    <tr>
      <td><b>codes</b>
      </td>
      <td>0</td>
      <td>1</td>
      <td>3</td>
      <td>5</td>
      <td>7</td>
      <td>content==1?0:content*2-3</td>
    </tr>
    <tr>
      <td><b>mounts</b>
      </td>
      <td>1</td>
      <td>1</td>
      <td>2</td>
      <td>3</td>
      <td>4</td>
      <td>(codes+3)/2</td>
    </tr>
  </table>*/
final class PaneSet extends AreaMount implements PaneLinking{
	private final class Mount{
		KitFacet content;
		Mount(KitFacet content){
			this.content=content;
		}
	}
	final STarget targets;
	final PaneSetLayout layout;
	private final List<Mount>showMounts=new ArrayList(),linkMounts=new ArrayList();
	private final Map<Object,KWrap>controlWraps=new HashMap(){};
	private final List<KitFacet>contents=new ArrayList();
	private final Mount[]mounts;
	private final int mountCount,rootCount;
	private final LinkGroup[]linkGroups;
	private final SToggling[]shows;
	private final SplitPane rootPane;
	private Mount maximisedActive,maximisedThen;
	private Mount singleLive;
	private int splitAt,mountAt;
	private boolean tracing;
	@Override
	protected void traceOutput(String msg){
		if(tracing){
			if(false)Util.printOut(Debug.info(this)+msg);
			else super.traceOutput(msg);
		}
	}
	PaneSet(SAreaTarget area,Toolkit kit,PaneSetLayout layout){
		super(area,kit);
		FacetedTarget[]viewers=Objects.newTyped(FacetedTarget.class,area.indexableTargets());
		if(viewers.length<2)throw new IllegalArgumentException(
				"Empty or single viewers in "+Debug.info(this));
		else mounts=new Mount[viewers.length];
		mountCount=mounts.length;
		rootCount=layout.codesTree.length;
		ViewerAreaMaster vam=activeMaster(area);
		linkGroups=vam!=null&&vam.hints().includeFlag(FacetFactory.HINT_BARE)?
				null:rootCount<3?new LinkGroup[]{new LinkGroup()}
			:new LinkGroup[]{new LinkGroup(),new LinkGroup(),new LinkGroup()};
		targets=(this.layout=layout).buildPaneTargets(this);
		shows=layout.shows.togglings;
		for(int m=0;m<mountCount;m++){
			KitFacet content=(KitFacet)viewers[m].attachedFacet();
			if(content instanceof PaneSet)
				throw new IllegalArgumentException(info(content)+" not nestable inside "+info(this));
			mounts[m]=new Mount(content);
			contents.add(content);
			controlWraps.put(mounts[m],newControlWrap());
		}
		for(int rootAt=0;rootAt<rootCount;rootAt++)controlWraps.put(rootAt,newControlWrap());
		rootPane=new SplitPane(0);
		applyLayout();
	}
	void applyLayout(){
		tracing=false;
		mountAt=0;
		rootPane.dispose();
		SplitPane[]rootChildren=null;
		for(int rootAt=0;rootAt<rootCount;rootAt++){
			SplitPane pane=rootAt==0?rootPane:rootChildren[rootAt-1],children[]=null;
			final int codes[]=layout.codesTree[rootAt],codeCount=codes.length,
					codeMounts=(codeCount+3)/2;
			if(codeCount==0)throw new IllegalStateException(
					"Empty codes in "+Debug.info(this));
			boolean rootStack=rootCount==1&&codes[0]==PANE_STACK,
				stacking=rootStack||rootAt>0&&layout.stacks.togglings[rootAt-1].isSet();
			trace(".applyLayout: rootAt="+rootAt+" stacking="+stacking+
					" codeCount="+codeCount+" codeMounts="+codeMounts);
			for(int codeAt=0,codeMountAt=0;mountAt<mountCount
						&&(stacking?codeMountAt<codeMounts:codeAt<codeCount);
				codeAt+=2,codeMountAt++,mountAt+=rootAt>0||rootStack?1:0){
				if(true)trace(".applyLayout: codeMountAt="+codeMountAt+" codeAt="+codeAt
						+" mountAt="+mountAt);
				if(stacking)pane.addStacked();
				else{
					if(children!=null)pane=children[codes[codeAt-1]];
					else if(rootAt>0)mountAt++;
					children=pane.split(codes[codeAt]);
				}
				if(children!=null)trace(".applyLayout: children=",children.length);
			}
			if(rootAt==0)rootChildren=children;
		}
		if(mountAt>=mountCount+1)throw new IllegalStateException(
				"Not enough mounts=" +mountCount+" in "+Debug.info(this));
		for(int m=0;m<mountCount;m++)if(shows[m].isSet())showMounts.add(mounts[m]);
		splitAt=layout.splits.length-1;
		mountAt=0;
		boolean maximised=maximisedActive!=null;
		Mount active=maximised?maximisedActive:null;
		for(int m=0;active==null&&m<mountCount;m++){
			Mount mount=mounts[m];
			if((mountArea(mount)).isActive()&&shows[m].isSet())active=mount;
		}
		for(int m=0;active==null&&m<mountCount;m++)
			if(shows[m].isSet())active=mounts[m];
		if(active==null)throw new IllegalStateException(
				"Null active mount in "+Debug.info(this));
		else mountArea(active).ensureActive(Impact.MINI);
		singleLive=showMounts.size()==1&&!maximised?showMounts.get(0):null;
		tracing=true||mountCount>2;
		int[]swaps=layout.getSwaps();
		KitFacet before=mounts[0].content,after;
		for(int at=0;at<mounts.length;at++)mounts[at].content=contents.get(swaps[at]);
		after=mounts[0].content;
		if(after!=before)((FacetedTarget)mounts[0].content.target()).ensureActive(Impact.ACTIVE);
		linkMounts.clear();
		rootPane.layOutMounts();
		List<KWrap>linkables=new ArrayList();
		for(Mount show:linkMounts)linkables.add(show.content.base());
		if(showMounts.size()>1)kit.paneLinksGroup(this).setItems(
				linkables.toArray(new KWrap[0]));
		layout.panesLaidOut(!maximised);
		maximisedThen=null;
		showMounts.clear();
		if(false)Debug.memCheck("PaneSet.applyLayout: ");
		if(false)((Facetable)target).retargetFacets(Impact.ACTIVE);
		tracing=true;
	}
	private final class LinkGroup{
		List<Mount>mounts=new ArrayList();
		SFacet activeContent;
		boolean stacked;
		boolean linkDefined(KitFacet from,KitFacet to){
			Mount fromMount=contentMount(from),toMount=contentMount(to);
			if(!mounts.contains(fromMount))return false;
			if(stacked&&mounts.contains(toMount)){
				List<KitFacet>stack=new ArrayList();
				for(Mount mount:mounts)stack.add(mount.content);
				stack.add(stack.indexOf(to),stack.remove(stack.indexOf(from)));
				Iterator<KitFacet>stackings=stack.iterator();
				for(Mount mount:mounts)mount.content=stackings.next();
			}
			else{
				toMount.content=from;
				fromMount.content=to;
			}
			return true;
		}
		void update(List<Mount>mounts,boolean stacked){
			this.mounts=mounts;
			this.stacked=stacked;
		}
		void checkActive(STarget active){
			for(Mount mount:mounts)
				if(mount.content.target()==active)activeContent=mount.content;
		}
	}
	final private class SplitPane{
		final KMount base;
		private final List<Integer>stackedAts=new ArrayList();
		private final List<KMount>tabMounts=new ArrayList();
		private final KWrap control;
		private final boolean isRoot;
		private final LinkGroup group;
		private SplitPane split0,split1;
		private int splitCode;
		SplitPane(int rootAt){
			this.control=controlWraps.get(rootAt);
			if(false)trace(".SplitPane: rootAt="+rootAt+" linkGroups=",linkGroups);
			isRoot=rootAt==0;
			group=linkGroups==null||(linkGroups.length>1&&isRoot)
					||control==null?null:linkGroups[rootAt];
			PaneSet panes=PaneSet.this;
			base=kit.spreadMount(panes,false);
		}
		void dispose(){
			stackedAts.clear();
			if(split0!=null){
				split0.dispose();
				split1.dispose();
				split0=split1=null;
			}
			for(KMount mount:tabMounts)mount.setItem(null);
			tabMounts.clear();
			base.setItem(null);
		}
		boolean layOutMounts(){
			boolean showMe=false;
			int stacked=stackedAts.size();
			Mount mount;
			if(maximisedActive!=null){
				while((mount=mounts[mountAt++])!=maximisedActive);
				addSingleMount(mount);
				showMe=true;
			}
			else if(stacked>0){
		  	List<Mount>areas=new ArrayList();
		  	if(false)trace(".layOutMounts: stackedAts=",stackedAts.size());
		  	for(Integer tabMountAt:stackedAts){
					mount=mounts[tabMountAt];
			  	boolean showMount=shows[contents.indexOf(mount.content)].isSet();
			  	FacetedTarget area=((SAreaTarget)mount.content.target()).activeFaceted();
			  	area.setLive(showMount);
			  	showMe|=showMount;
			  	if(showMount)areas.add(mount);
			  	mountAt++;
			  }
		  	splitAt-=(stacked+1)/2;
		  	if(showMe)base.setItem(tabbedBase(areas.toArray(new Mount[]{}),
		  			singleLive!=null||stacked==mountCount?null:this.control));
		  }
		  else if(split0==null){
				mount=mounts[mountAt];
		  	showMe=shows[contents.indexOf(mount.content)].isSet();
		  	FacetedTarget viewer=((SAreaTarget)mount.content.target()).activeFaceted();
				viewer.setLive(showMe);
				if(false)trace(".layOutMounts: showMe="+showMe+" mountAt=",mountAt);
				if(showMe)addSingleMount(mount);
				mountAt++;
		  }
			else{
				if(false)trace(".layOutMounts: showMe="+showMe+" mountAt=",mountAt);
				boolean show0=split0.layOutMounts(),show1=split1.layOutMounts();
				showMe=show0|show1;
				if(showMe){
					KMount splits=kit.splitMount(PaneSet.this,
							splitCode!=PANE_SPLIT_VERTICAL^(layout.flip==PANE_SPLIT_VERTICAL),
							layout.splits[splitAt--]);
					base.setItem(splits);
					splits.setItems(show0&&show1?new KWrap[]{split0.base,split1.base}
						:new KWrap[]{show0?split0.base:split1.base});
				}
			}
			if(group!=null)group.update(new ArrayList(linkMounts),stacked>0);
		  return showMe;
		}
		SplitPane[]split(int splitCode){
			this.splitCode=splitCode;
			if(false)trace(".split: mountAt=",mountAt);
			return new SplitPane[]{
				split0=new SplitPane(isRoot?1:-1),
				split1=new SplitPane(isRoot?2:-1)
			};
		}
		void addStacked(){
			stackedAts.add(mountAt);
			if(false)trace(".addStacked: mountAt=",mountAt);
		}
		private void addSingleMount(Mount mount){
			base.setItem(linkGroups==null?mount.content.base()
					:tabbedBase(new Mount[]{mount},
							singleLive!=null?null:controlWraps.get(mount))
			);
		}
		private KMount tabbedBase(Mount[]tabbed,KWrap control){
			for(Mount tab:tabbed)linkMounts.add(tab);
			KMount mount=kit.paneTabs(mountContents(tabbed),group==null?null
					:group.activeContent,control);
			tabMounts.add(mount);
			return mount;
		}
	}
	@Override
	protected void retargeted(SAreaTarget area,Impact impact){
		super.retargeted(area,impact);
		STarget indexed=area.indexedTarget();
		if(linkGroups!=null&&indexed.isLive())
			for(LinkGroup group:linkGroups)group.checkActive(indexed);
		layout.control.retargetFacets(impact);
	}
	private Mount contentMount(KitFacet content){
		for(Mount mount:mounts)if(mount.content==content)return mount;
		throw new IllegalStateException("Null mount in "+this);
	}
	private KitFacet[]mountContents(Mount[]mounts){
		KitFacet[]contents=new KitFacet[mounts.length];
		for(int i=0;i<contents.length;i++)contents[i]=mounts[i].content;
		return contents;
	}
	private SAreaTarget mountArea(Mount mount){
		return (SAreaTarget)mount.content.target();
	}
	@Override
	public void dispose(){
		if(false)rootPane.dispose();
	}
	@Override
	public boolean canLink(KWrap tab){
		return singleLive==null&&maximisedActive==null
				&&((SAreaTarget)findLinkContent(tab).target()).isActive();
	}
	private KitFacet findLinkContent(KWrap link){
		KitFacet content=link.facet();
		if(!(content instanceof Nested))return content;
		Collection<KitFacet>contents=Arrays.asList(mountContents(mounts));
		for(STarget d:((SAreaTarget)content.target()).descendants()){
			SFacet attached=((FacetedTarget)d).attachedFacet();
			if(contents.contains(attached))return(KitFacet)attached;
		}
		throw new IllegalArgumentException("No mount for "+link);
	}
	private KWrap newControlWrap(){
		return Indexings.Iterator.newIcon(layout.control,kit).base();
	}
	@Override
	public void linkDefined(KWrap from,KWrap to){
		KitFacet fromContent=findLinkContent(from),toContent=findLinkContent(to);
		for(LinkGroup group:linkGroups){
			if(!group.linkDefined(fromContent,toContent))continue;
			int[]swaps=new int[mountCount];
			for(int m=0;m<mountCount;m++)swaps[m]=contents.indexOf(mounts[m].content);
			layout.putSwaps(swaps);
			break;
		}
		applyLayout();
		target().notifyParent(Impact.DEFAULT);
	}
	KWrap getViewerControl(SFacet content){
		Mount mount=null;
		for(Mount m:mounts)
			if(m.content==content)mount=m;
		if(mount==null)throw new IllegalStateException(
				"Null mount in "+this);
		else return linkGroups!=null||mount==singleLive?null:controlWraps.get(mount);
	}
	void setMaximisedActive(boolean on){
		maximisedThen=maximisedActive;
		maximisedActive=null;
		if(on){
			for(Mount mount:mounts)
				if(mountArea(mount).isActive())
					maximisedThen=maximisedActive=mount;
			if(maximisedActive==null)throw new IllegalStateException(
					"Null active in "+Debug.info(this));
		}
		applyLayout();
	}
	protected KWrap lazyBase(){
		return rootPane.base;
	}
	@Override
	public String toString(){
		return super.toString()+">"+target;
	}
}