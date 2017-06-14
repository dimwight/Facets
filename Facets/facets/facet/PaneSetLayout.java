package facets.facet;
import static facets.core.app.AppConstants.*;
import static facets.core.app.PagedActionDefaults.*;
import static facets.core.superficial.TargeterCore.*;
import static facets.facet.AreaFacets.*;
import static facets.util.Strings.*;
import static java.util.Arrays.*;
import facets.core.app.AppConstants;
import facets.core.app.Dialogs;
import facets.core.app.HideableHost;
import facets.core.app.PagedActions;
import facets.core.app.PagedContenter;
import facets.core.app.PagedSurface;
import facets.core.app.TypeKeyable;
import facets.core.superficial.SFacet;
import facets.core.superficial.SIndexing;
import facets.core.superficial.SNumeric;
import facets.core.superficial.STarget;
import facets.core.superficial.STargeter;
import facets.core.superficial.SToggling;
import facets.core.superficial.STrigger;
import facets.core.superficial.TargetCore;
import facets.core.superficial.Notifying.Impact;
import facets.core.superficial.SToggling.Coupler;
import facets.core.superficial.SToggling.Togglings;
import facets.core.superficial.app.SAreaTarget;
import facets.core.superficial.app.SSurface.WindowAppSurface;
import facets.facet.app.FacetAppSurface;
import facets.facet.app.FacetPagedSurface;
import facets.facet.kit.Toolkit;
import facets.util.Debug;
import facets.util.NumberPolicy;
import facets.util.NumberValues;
import facets.util.Strings;
import facets.util.Tracer;
import facets.util.Util;
import facets.util.tree.ValueNode;
import java.util.Arrays;
final class PaneSetLayout extends Tracer{
	private class Numbers extends NumberValues{
		Numbers(Object defaultValue,int checkCount,String keyTop){
			super(defaultValue,checkCount,keyTop);
		}
		@Override
		protected String tailKeyDefaults(){
			return tailKeyDefault;
		}
		@Override
		protected String tailKeySession(){
			return tailKeySession;
		}
		@Override
		protected ValueNode store(){
			return state;
		}
	}
	final int[][]codesTree;
	final String[]groups;
	final STargeter control;
	int flip;
	Togglings stacks,shows;
	SNumeric[]splits;
	boolean showOptions;
	private final String tailKeyDefault=Util.shortTypeNameKey(this),
			tailKeySession;
	final private STarget ask;
	final private ValueNode state;
	final private int paneCount,swapDefaults[];
	final private NumberValues flipValue,stackAndHideValues,splitValues,swapValues;
	final private boolean stackedOnly;
	private STrigger maximise,restore;
	private STarget layout,show,select,selectOptions;
	private boolean resetting;
	private Boolean doValidate;
	@Override
	protected void traceOutput(String msg){
		if(true)super.traceOutput(msg);
	}
	PaneSetLayout(SAreaTarget area,ValueNode state,final int[][]codesTree,
			double[]splitDefaults,int[]stackAndHideDefaults,String[]groups){
		ViewerAreaMaster vam=AreaFacets.activeMaster(area);
		tailKeySession=vam!=null?vam.typeKey()
			:((TypeKeyable)area.activeFaceted().attachedFacet()).typeKey();
		stackedOnly=vam!=null&&vam.hints().includeFlag(HINT_PANE_STACK);
		this.codesTree=codesTree;
		this.state=state;
		paneCount=area.indexableTargets().length;
		int groupCount=codesTree.length-1;
		if(groups.length!=groupCount){
			groups=new String[groupCount];
			for(int i=0;i<groups.length;i++)groups[i]="Group"+i;
		}
		this.groups=groups;
		flipValue=new Numbers(PANE_SPLIT_VERTICAL,1,PANE_KEY_FLIP);
		splitValues=new Numbers(splitDefaults,paneCount-1,PANE_KEY_SPLITS){
			@Override
			protected double[]validDefaultDoubles(double[]proposed){
				if(proposed.length>=paneCount-1)return proposed;
				boolean vertical=codesTree[0][0]==PANE_SPLIT_VERTICAL;
				double[]splits=new double[paneCount-1];
				for(int i=0,q=paneCount,limit=splits.length;i<limit;i++,q--){
					double d=1d/q;
					splits[i]=vertical?1-d:d;
				}
				return splits;
			}
		};
		stackAndHideValues=new Numbers(stackAndHideDefaults,groupCount+paneCount,
				PANE_KEY_TABS_AND_HIDES);
		ask=new SToggling(PANE_TITLE_ASK,state.getOrPutBoolean(PANE_TITLE_ASK,true),
				new Coupler(){
			@Override
			public void stateSet(SToggling t){
				PaneSetLayout.this.state.put(PANE_TITLE_ASK,t.isSet());
			}
		});
		control=newRetargeted(new SIndexing("Control",
				new String[]{PANE_TITLE_MAXIMISE,PANE_TITLE_RESTORE},0,new SIndexing.Coupler(){
			private int atThen;
			@Override
			public void indexSet(SIndexing i){
				int at=i.index();
				if(atThen==at)return;
				else if((atThen=at)==1)maximise.fire();
				else restore.fire();
			}
			@Override
			public boolean canCycle(SIndexing i){
				return true;
			}
			@Override
			public String[]iterationTitles(SIndexing i){
				return (String[])i.indexables();
			}
		}),true);
		swapDefaults=new int[paneCount];
		for(int i=0;i<swapDefaults.length;i++)swapDefaults[i]=i;
		swapValues=new Numbers(swapDefaults,paneCount,PANE_KEY_SWAPS);
	}
	void panesLaidOut(boolean notMaximised){
		show.setLive(notMaximised);
		select.setLive(notMaximised);
		selectOptions.setLive(notMaximised);
		layout.setLive(notMaximised&&paneCount>1);
		int displayed=0;
		for(SToggling t:shows.togglings)if(t.isSet())displayed++;
		boolean oneDisplayed=displayed==1;
		for(SToggling t:shows.togglings)if(t.isSet())t.setLive(!oneDisplayed);
		restore.setLive(!notMaximised);
		maximise.setLive(notMaximised&&!oneDisplayed);
		layout.elements()[0].setLive(layout.isLive()&&!oneDisplayed);
		storeState(false);
	}
	void storeState(boolean asDefaults){
		if(doValidate==null)doValidate=!flipValue.hasSessionValue();
		if(false)trace(".storeState: doValidate=",doValidate);
		flipValue.putValue(flip,asDefaults);
		double[]splits=new double[this.splits.length];
		for(int i=0;i<splits.length;i++)splits[i]=this.splits[i].value()/100;
		splitValues.putValue(splits,asDefaults);
		String stacks=intsString(this.stacks.stateInts()),
			stackAndHide=stacks+(stacks.equals("")?"":",")+intsString(shows.stateInts());
		if(false&&groups.length==0)trace(".storeState: stackAndHide=",stackAndHide);
		stackAndHideValues.putValue(stackAndHide,asDefaults);
		boolean unchanged=swapValues.hasDefaultValue();
		if(!stackedOnly)for(NumberValues values:new NumberValues[]{flipValue,splitValues,stackAndHideValues})
			unchanged&=values.hasDefaultValue();
		layout.elements()[1].setLive(!unchanged);
	}
	STarget buildPaneTargets(final PaneSet panes){
		final SAreaTarget area=(SAreaTarget)panes.target();
		flip=(Integer)flipValue.getValue(false);
		SNumeric.Coupler splitCoupler=new SNumeric.Coupler(){
			@Override
			public NumberPolicy policy(SNumeric n){
				return SASH_SPLIT_POLICY;
			}
			@Override
			public void valueSet(SNumeric n){
				if(!resetting)storeState(false);
			}
		};
		double[]splitFetch=(double[])splitValues.getValue(false);
		splits=new SNumeric[splitFetch.length];
		for(int i=0;i<splits.length;i++)
			splits[i]=new SNumeric("Split",splitFetch[i]*100,splitCoupler);
		int[]stackAndHideFetch=(int[])stackAndHideValues.getValue(false);
		stacks=new Togglings(groups,copyStackInts(stackAndHideFetch)){
			@Override
			protected void togglingSet(int at){
				if(!resetting)panes.applyLayout();
			}
		};
		final STarget[]areas=area.indexableTargets();
		shows=new Togglings(areas,copyHideInts(stackAndHideFetch)){
			@Override
			protected boolean zeroAsTrue(){
				return true;
			}
			@Override
			protected void togglingSet(int at){
				if(resetting)return;
				STarget active=null;
				if(togglings[at].isSet())active=areas[at];
				else for(int i=0;active==null&&i<areas.length;i++)
					if(togglings[i].isSet())active=areas[i];
				if(active==null)throw new IllegalStateException(
						"Null active in "+Debug.info(this));
				else((SAreaTarget)active).ensureActive(Impact.MINI);
				panes.applyLayout();
			}
		};
		STrigger.Coupler maximiseCoupler=new STrigger.Coupler(){
			@Override
			public void fired(STrigger t){
				panes.setMaximisedActive(t==maximise);
				((SIndexing)control.target()).setIndex(t==maximise?1:0);
				t.notifyParent(Impact.ACTIVE);
			}
		};
		return new TargetCore(PaneSet.class.getSimpleName(),
			layout=new TargetCore("Layout",
				new STrigger(PANE_TITLE_FLIP,new STrigger.Coupler(){
					@Override
					public void fired(STrigger t){
						flip=flip==PANE_SPLIT_VERTICAL?PANE_SPLIT_HORIZONTAL:PANE_SPLIT_VERTICAL;
						panes.applyLayout();
					}
			  }),
			  new STrigger(PANE_TITLE_RESET,new STrigger.Coupler(){
					@Override
				  public void fired(STrigger t){
						resetting=true;
						flip=(Integer)flipValue.getValue(true);
						double[]splitFetch=(double[])splitValues.getValue(true);
						for(int i=0;i<splits.length;i++)splits[i].setValue(splitFetch[i]*100);
						int[]stackAndHide=(int[])stackAndHideValues.getValue(true);
						stacks.setStates(copyStackInts(stackAndHide));
						shows.setStates(copyHideInts(stackAndHide));
						swapValues.putValue(swapDefaults,false);
						resetting=false;
				  	panes.applyLayout();
					}
				})),
			show=new TargetCore("Sho&w Panes|Panes: ",shows.togglings),
			new TargetCore("Active",
				maximise=new STrigger(PANE_TITLE_MAXIMISE,maximiseCoupler),
				restore=new STrigger(PANE_TITLE_RESTORE,maximiseCoupler)),
			select=newSelectTrigger(panes,false),	
			selectOptions=newSelectTrigger(panes,true));
	}
	void putSwaps(int[]swaps){
		swapValues.putValue(swaps,false);
		storeState(false);
	}
	int[]getSwaps(){
		return(int[])swapValues.getValue(false);
	}
	void validate(){
		if(false)trace(".validate: doValidate=",doValidate);
		if(((SToggling)ask).isSet()&&doValidate)((STrigger)select).fire();
		doValidate=false;
	}
	private STrigger newSelectTrigger(final PaneSet panes,final boolean withOptions){
		final String title=PaneSetContenter.TITLE;
		return new STrigger(title+"...",new STrigger.Coupler(){
			@Override
			public void fired(STrigger t){
				final SAreaTarget area=(SAreaTarget)panes.target();
				FacetAppSurface app=TargetCore.findNotifiableTyped(FacetAppSurface.class,area);
				if(app==null)throw new IllegalStateException("Null app in "+Debug.info(this));
				app.dialogs().launchSurfaced(new Dialogs.Surfacer(){
					@Override
					public PagedSurface newSurface(String title,HideableHost host,
							PagedActions actions,PagedContenter[]contents,WindowAppSurface parent){
						return new FacetPagedSurface(title,host,actions,
								contents,(FacetAppSurface)parent){
							@Override
							public boolean isResizable(){
								return true;
							}
						};
					}
				},
				title,newOkCancel(),
				new PaneSetContenter(app.ff,PaneSetLayout.this,panes,layout,withOptions?ask:null));
			}
		});
	}
	private int[]copyStackInts(int[]stackAndHide){
		return copyOfRange(stackAndHide,0,groups.length);
	}
	private int[]copyHideInts(int[]stackAndHide){
		return copyOfRange(stackAndHide,groups.length,stackAndHide.length);
	}
}