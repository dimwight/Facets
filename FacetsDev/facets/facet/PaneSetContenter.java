package facets.facet;
import static facets.facet.AreaFacets.*;
import static java.lang.Math.*;
import static java.util.Arrays.*;
import facets.core.app.PagedSurface;
import facets.core.app.SAreaTarget;
import facets.core.app.SContentAreaTargeter;
import facets.core.superficial.SFacet;
import facets.core.superficial.STarget;
import facets.core.superficial.STargeter;
import facets.core.superficial.SToggling;
import facets.core.superficial.STrigger;
import facets.core.superficial.TargetCore;
import facets.core.superficial.SToggling.Coupler;
import facets.core.superficial.SToggling.Togglings;
import facets.facet.app.FacetPagedContenter;
import facets.facet.app.FacetPagedSurface;
import facets.util.ItemList;
import facets.util.Objects;
import java.awt.Dimension;
final class PaneSetContenter extends FacetPagedContenter{
	private static final double CHAR_WIDTH=5.5;
	final static String TITLE="Select Panes";
	private static final String TITLE_STACK="Stacking:";
	private final PaneSetLayout layout;
	private final int[]copyStacks,copyHides;
	private final SToggling defaults,ask;
	private final STarget options,flipReset,targets[];
	private final int copyFlip,width,height;
	private final boolean copyAsk;
	private boolean okPressed;
	private PagedSurface dialog;
	protected boolean aligning;
	private final PaneSet panes;
	PaneSetContenter(FacetFactory ff,final PaneSetLayout layout,PaneSet panes,
			STarget flipReset,STarget ask){
		super(TITLE,ff);
		this.layout=layout;
		this.panes=panes;
		this.flipReset=flipReset;
		copyStacks=layout.stacks.stateInts();
		copyHides=layout.shows.stateInts();
		copyFlip=layout.flip;
		this.ask=(SToggling)ask;
		copyAsk=ask!=null&&this.ask.isSet();
		Togglings stacks=new Togglings(layout.stacks.togglings,copyStacks){
			@Override
			protected void togglingSet(int at){
				if(!aligning)layout.stacks.togglings[at].set(togglings[at].isSet());
			}
		},
		shows=new Togglings(layout.shows.togglings,copyHides){
			@Override
			protected boolean zeroAsTrue(){
				return true;
			}
			@Override
			protected void togglingSet(int at){
				if(!aligning)layout.shows.togglings[at].set(togglings[at].isSet());
			}
		};
		int width=0,height=0;
		final ItemList<STarget>groups=new ItemList(STarget.class);
		String[]layouts=layout.groups;
		if(layouts.length==0)layouts=new String[]{"Panes"};
		int groupCount=layouts.length;
		boolean singleGroup=groupCount==1;
		for(int showsNextAt=0,groupAt=0;groupAt<groupCount;groupAt++){
			int showsCount=singleGroup?shows.togglings.length
				:1+(layout.codesTree[groupAt+1].length+1)/2;
			STarget group=new TargetCore(layouts[groupAt],
				copyOfRange(shows.togglings,showsNextAt,showsNextAt+showsCount));
			STarget[]togglings=group.elements();
			int titleChars=singleGroup?0:group.title().length();
			if(singleGroup)for(STarget toggling:togglings)
				titleChars=max(titleChars,toggling.title().replaceAll("\\|.*","")
						.length());
			width+=17+titleChars*CHAR_WIDTH;
			trace(".PaneSetContenter: titleChars="+titleChars+" width=",width);
			height=max(height,togglings.length*23);
			groups.add(group);
			showsNextAt+=showsCount;
			if(!singleGroup)stacks.togglings[groupAt].setLive(togglings.length>1);
		}
		defaults=new SToggling(PANE_TITLE_DEFAULT,false,new Coupler());
		options=ask==null?null:new TargetCore("O&ptions",defaults,ask);
		if(options!=null)options.setLive(layout.showOptions);
		if(false)trace(": showOptions="+layout.showOptions+" options=",options);
		final STarget triggers[]=flipReset.elements();
		targets=Objects.join(STarget.class,new STarget[]{
			new TargetCore("Shows",shows.togglings),
			new TargetCore("Groups",groups.items()),
			new TargetCore("Buttons",new STrigger(triggers[0].title(),new STrigger.Coupler(){
					public void fired(STrigger t){
						((STrigger)triggers[0]).fire();
					}
				}),
				new STrigger(triggers[1].title(),new STrigger.Coupler(){
					public void fired(STrigger t){
						((STrigger)triggers[1]).fire();
					}
				})
			),
			new TargetCore(TITLE_STACK,stacks.togglings),
		},
		options==null?new STarget[]{}:new STarget[]{
			options,
			new SToggling("ShowOptions|"+options.title()+">>",
					layout.showOptions,new Coupler(){
				@Override
				public void stateSet(SToggling t){
					options.setLive(layout.showOptions=t.isSet());
					if(false)traceDebug(".stateSet: showOptions="+layout.showOptions+
							" options=",options);
				}
			})}
		);
		if(!singleGroup){
			height+=12+28;//group title,shows 
			width+=5+TITLE_STACK.length()*CHAR_WIDTH;
		}
		trace(".PaneSetContenter: width=",width);
		height=max(height,options==null?50:80);//buttons
		height+=10*2;//insets
		width+=10+65;//gap,buttons
		trace(".PaneSetContenter: width=",width);
		width+=10*2;//insets
		this.width=width;
		this.height=height;
		if(false)trace(".PaneSetContenter: width="+width+" height="+height);
	}
	@Override
	public void areaRetargeted(SContentAreaTargeter area){
		STargeter[]shows=area.elements()[0].elements(),
			triggers=area.elements()[2].elements(),
			tabs=area.elements()[3].elements();
		aligning=true;
		for(int i=0;i<shows.length;i++){
			SToggling here=(SToggling)shows[i].target(),there=layout.shows.togglings[i];
			here.setLive(there.isLive());
			here.set(there.isSet());
		}
		for(int i=0;i<triggers.length;i++){
			STarget[]there=flipReset.elements();
			triggers[i].target().setLive(there[i].isLive());
		}
		for(int i=0;i<tabs.length;i++)
			((SToggling)tabs[i].target()).set(layout.stacks.togglings[i].isSet());
		aligning=false;
	}
	@Override
	protected void traceOutput(String msg){
		if(false)super.traceOutput(msg);
	}
	@Override
	protected PanelFactory newPanelFactory(FacetFactory core){
		return new PanelFactory(core){
			@Override
			public SFacet newContentPanel(SContentAreaTargeter t){
				final ItemList<SFacet>list=new ItemList(SFacet.class);
				STargeter elements[]=t.elements(),
					groups[]=elements[1].elements(),
					triggers=elements[2],tabs=elements[3],
					options=null,showOptions=null;
				if((elements.length>4)){
					options=elements[4];
					showOptions=elements[5];
					((FacetPagedSurface)dialog).addExtensionPanel(rowPanel(options,10,15,HINT_NONE,
							togglingCheckboxes(options,HINT_TALL+HINT_BARE),
							BREAK,fill()),true);
				}
				for(STargeter group:groups)list.add(togglingCheckboxes(group,HINT_TALL+
							(groups.length==1?HINT_BARE:HINT_NONE)));
				if(groups.length>1)list.addItems(
						BREAK,spacerTall(5),
						BREAK,togglingCheckboxes(tabs,HINT_NONE));
				list.addItems(BREAK,fill());
				SFacet groupPanel=rowPanel(t,0,0,HINT_NONE,list.items());
				list.clear();
				list.addItems(groupPanel,spacerWide(10));
				SFacet buttons=triggerButtons(triggers,HINT_NONE+HINT_TALL+HINT_BARE+
						HINT_USAGE_PANEL);
				list.add(options!=null?
					rowPanel(triggers,0,0,HINT_PANEL_RIGHT+HINT_NONE,
						buttons,BREAK,spacerTall(5),BREAK,
						togglingButtons(showOptions,HINT_BARE+HINT_TITLE1+HINT_USAGE_PANEL),
						BREAK,fill())
					:rowPanel(triggers,0,0,HINT_PANEL_RIGHT+HINT_NONE,
						buttons,
						BREAK,fill())
					);
				list.addItems(BREAK,fill());
				return rowPanel(t,0,0,HINT_PANEL_INSET+HINT_NONE,list.items());
			}
		};
	}
	@Override
	public Dimension contentAreaSize(){
		return new Dimension(width,height);
	}
	@Override
	public void setSurface(PagedSurface surface){
		this.dialog=surface;
	}
	@Override
	public STarget[]lazyContentAreaElements(SAreaTarget area){
		return targets;
	}
	@Override
	public void applyChanges(){
		okPressed=true;
	}
	@Override
	public void reverseChanges(){
		layout.stacks.setStates(copyStacks);
		layout.shows.setStates(copyHides);
		layout.flip=copyFlip;
		if(ask!=null)ask.set(copyAsk);
		panes.applyLayout();
	}
	@Override
	public void hostHidden(){
		if(!okPressed)reverseChanges();
		else layout.storeState(defaults.isSet());
	}
}