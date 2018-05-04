package applicable.field;
import static facets.core.app.PagedActionDefaults.*;
import static facets.facet.app.FacetAppActions.BarHide.*;
import static facets.util.app.AppValues.*;
import facets.core.app.Dialogs;
import facets.core.app.FacetHostable;
import facets.core.app.HideableHost;
import facets.core.app.PagedActions;
import facets.core.app.PagedContenter;
import facets.core.app.PagedSurface;
import facets.core.app.SAreaTarget;
import facets.core.app.SContentAreaTargeter;
import facets.core.superficial.SFacet;
import facets.core.superficial.STarget;
import facets.core.superficial.STargeter;
import facets.core.superficial.SToggling;
import facets.core.superficial.STrigger;
import facets.core.superficial.TargetCore;
import facets.core.superficial.Notifying.Impact;
import facets.core.superficial.app.SSurface.WindowAppSurface;
import facets.facet.FacetFactory;
import facets.facet.app.FacetAppSurface;
import facets.facet.app.FacetPagedContenter;
import facets.facet.app.FacetPagedSurface;
import facets.facet.app.FacetPagedContenter.PanelFactory;
import facets.util.Debug;
import facets.util.Objects;
import facets.util.Stateful;
import facets.util.Times;
import facets.util.Titled;
import facets.util.Tracer;
import facets.util.TypesKey;
import facets.util.Util;
import facets.util.tree.ValueNode;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
public class FieldSet extends Tracer implements Titled{
	public static final String TITLE_CHOOSE="Choose &Fields",
		TITLE_CHOOSE_SIDEBAR="Field Chooser",
		KEY_TOP_USE="fieldSetUse_";
	public final Map<String,ValueField>nameFields;
	public final ValueField[]allFields;
	public final String typeKey=Util.shortTypeNameKey(this);
	private final String keyUse=KEY_TOP_USE+typeKey;
	protected final int shortLength;
	private final List<ValueField>live=new ArrayList();
	private final String title;
	private final int liveMin;
	private ValueNode state;
	private ValueField[]liveFields;
	public FieldSet(String title,ValueField[]allFields,int liveMin){
		this.title=title;
		this.allFields=allFields;
		this.liveMin=liveMin;
		setDefaults();
		int longest=0,length=0;
		for(int i=0;i<allFields.length;i++)longest=Math.max(allFields[i].name.length(),longest);
		Set<String>shorts=new HashSet();
		for(;length<longest;length++){
			for(ValueField field:allFields)shorts.add(field.shortName(length));
			if(shorts.size()==allFields.length)break;
			else shorts.clear();
		}
		shortLength=Math.max(3,length);
		if(allFields.length>0&&shorts.isEmpty())throw new IllegalStateException(
				"Empty shorts (duplicate field name?) in "+Debug.info(this));
		else if(false)trace(".FieldSet: " +title()+" shortLength="+shortLength+" shorts=",shorts);
		Map<String,ValueField>nameFields=new HashMap();
		for(ValueField field:allFields)nameFields.put(field.name,field);
		this.nameFields=Collections.unmodifiableMap(nameFields);
	}
	private void setDefaults(){
		live.clear();
		for(ValueField field:allFields)if(isDefaultField(field))live.add(field);
	}
	final public void loadState(ValueNode state){
		if(state==null)throw new IllegalArgumentException(
				"Null state in "+Debug.info(this));
		else this.state=state;
		String useList=state.getString(keyUse);
		if(useList.length()==0)return;
		live.clear();
		for(ValueField field:allFields)
			if(useList.contains(field.name))live.add(field);
	}
	@Override
	final public String title(){
		return title;
	}
	final public ValueField[]liveFields(){
		if(false)return liveFields!=null?liveFields:(liveFields=live.toArray(new ValueField[]{}));
		else return live.toArray(new ValueField[]{});
	}
	final public ValueField getNamed(String name){
		ValueField field=nameFields.get(name);
		if(field==null)throw new IllegalStateException(
				"Null field name="+name+" in "+Debug.info(this));
		else return field;
	}
	protected boolean isDefaultField(ValueField field){
		return true;
	}
	protected boolean isObligatoryField(ValueField field){
		return true;
	}
	public FieldProxy newProxy(Stateful stateful){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	final private class Chooser extends FacetPagedContenter{
		private final class FieldCoupler extends SToggling.Coupler{
			private final ValueField field;
			FieldCoupler(ValueField field){
				this.field=field;
			}
			@Override
			public void stateSet(SToggling t){
				Set<ValueField>check=new HashSet(live);
				if(t.isSet())check.add(field);
				else check.remove(field);
				live.clear();
				for(ValueField each:allFields)if(check.contains(each))live.add(each);
			}
			void updateToField(SToggling t){
				t.set(live.contains(field));
				boolean notMin=live.size()>liveMin;
				t.setLive(!isObligatoryField(field)&&(notMin?true:!t.isSet()));
			}
		}
		private final SToggling[]togglings;
		private final FacetAppSurface app;
		private final boolean forSidebar;
		Chooser(FacetAppSurface app,boolean forSidebar){
			super(TITLE_CHOOSE,app.ff);
			this.app=app;
			this.forSidebar=forSidebar;
			List<STarget>elements=new ArrayList();
			for(final ValueField field:allFields){
				FieldCoupler coupler=new FieldCoupler(field);
				SToggling toggling=new SToggling(field.name,live.contains(field),coupler);
				coupler.updateToField(toggling);
				elements.add(toggling);
			}
			togglings=elements.toArray(new SToggling[]{});
		}
		@Override
		public boolean equals(Object o){
			Chooser that=(Chooser)o;
			for(int t=0;t<togglings.length;t++)
				if(togglings[t].isSet()!=that.togglings[t].isSet())return false;
			return true;
		}
		@Override
		public void areaRetargeted(SContentAreaTargeter root){
			for(SToggling each:togglings)
				((FieldCoupler)each.coupler).updateToField(each);
			app.surfaceTargeter().retargetFacets(Impact.DEFAULT);
		};
		@Override
		public void applyChanges(){
			if(state==null)throw new IllegalStateException(
					"Null state in "+Debug.info(this));
			else state.put(keyUse,Objects.toString(live.toArray()));
		}
		@Override
		public void reverseChanges(){
			loadState(state);
		}
		@Override
		public STarget[]lazyContentAreaElements(SAreaTarget area){
			return new STarget[]{
				new TargetCore(TITLE_CHOOSE,togglings),
				new STrigger("Use Defaults",new STrigger.Coupler(){
					@Override
					public void fired(STrigger t){
						FieldSet.this.setDefaults();
						for(SToggling each:togglings)
							((FieldCoupler)each.coupler).updateToField(each);
					}
				})
			};
		}
		@Override
		protected PanelFactory newPanelFactory(FacetFactory core){
			return new PanelFactory(core){
				@Override
				public SFacet newContentPanel(SContentAreaTargeter t){
					STargeter[]elements=t.elements();
					return rowPanel(t,0,0,forSidebar?HINT_NONE:HINT_PANEL_INSET,
							togglingCheckboxes(elements[0],HINT_TALL+HINT_NO_MNEMONICS),BREAK,
							triggerButtons(elements[1],HINT_BARE),
							BREAK,fill());
				}
			};
		}
		@Override
		public Dimension contentAreaSize(){
			return new Dimension(150,(1+allFields.length)*21+30);
		}
	}
	final public STrigger newChooseTrigger(final FacetAppSurface app){
		return new STrigger(TITLE_CHOOSE+"...",
				new STrigger.Coupler(){
			@Override
			public void fired(STrigger t){
				app.dialogs().launchSurfaced(new Dialogs.Surfacer(){
					@Override
					public PagedSurface newSurface(String title,HideableHost host,PagedActions actions,
							PagedContenter[]contents,WindowAppSurface parent){
						return new FacetPagedSurface(title,host,actions,
								contents,(FacetAppSurface)parent){
							public boolean isResizable(){
								return false;
							}
						};
					}
				},TITLE_CHOOSE.replace("&",""),newOkCancel(),
				new Chooser(app,false));
			}
		});
	}
	public final static class SidebarChooser extends FacetHostable{
		private final class FieldsCoupler extends SToggling.Coupler{
			final FieldSet fields;
			FieldsCoupler(FieldSet fields){
				this.fields=fields;
			}
			@Override
			public void stateSet(SToggling t){
				setShowSidebar(t.isSet());
			}
		}
		private final FacetAppSurface app;
		private final ValueNode appState;
		private final TypesKey features;
		private boolean showThen;
		private boolean facetRetargeted;
		private FieldSet thenFields;
		public SidebarChooser(FacetAppSurface app,TypesKey features){
			super();
			this.features=features;
			appState=(this.app=app).spec.state(PATH_APP);
			if(true&&!app.isBuilt())setShowSidebar(false);
		}
		final public SToggling newSidebarToggling(FieldSet fields){
			return new SToggling(TITLE_CHOOSE_SIDEBAR,getShowSidebar(),new FieldsCoupler(fields));
		}
		@Override
		public void facetRetargeted(Hosting host,STarget target,Impact impact){
			if(facetRetargeted)return;
			else facetRetargeted=true;
			boolean showSidebar=getShowSidebar();
			SToggling toggling=(SToggling)target;
			toggling.set(showSidebar);
			if(!showSidebar){
				facetRetargeted=false;
				return;
			}
			FieldSet fields=((FieldsCoupler)toggling.coupler).fields;
			if(thenFields!=fields){
				if(false)traceDebug(".facetRetargeted: toggling="+toggling+" fields=",fields);
				host.refreshPaged(TITLE_CHOOSE_SIDEBAR,newOkCancel(),thenFields=fields,app);
			}
			facetRetargeted=false;
		}
		@Override
		public PagedContenter[]newPagedContenters(Object source){
			return new PagedContenter[]{((FieldSet)source).new Chooser(app,true)};
		}
		private void setShowSidebar(boolean show){
			if(getShowSidebar()==show)return;
			Sidebar.writeHide(appState,!show,features);
			app.host().updateLayout(app);
		}
		private boolean getShowSidebar(){
			return !Sidebar.readHide(appState,features);
		}
		public void updateToggling(STargeter t){
			((SToggling)t.target()).set(getShowSidebar());
		}
	}
}