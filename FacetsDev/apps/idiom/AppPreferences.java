package apps.idiom;
import static apps.idiom.AppContent.*;
import static apps.idiom.FacetsApp.*;
import static apps.idiom.FacetsApp.ContentTypes.*;
import static facets.core.app.AppSurface.ContentStyle.*;
import static facets.facet.app.FacetAppSpecifier.*;
import facets.core.app.ActionAppSurface;
import facets.core.app.PagedContenter;
import facets.core.app.SAreaTarget;
import facets.core.app.SContentAreaTargeter;
import facets.core.app.AppSurface.ContentStyle;
import facets.core.superficial.SFacet;
import facets.core.superficial.SIndexing;
import facets.core.superficial.STarget;
import facets.core.superficial.STargeter;
import facets.core.superficial.SToggling;
import facets.core.superficial.TargetCore;
import facets.core.superficial.app.SSurface;
import facets.facet.FacetFactory;
import facets.facet.ValueDialogContenter;
import facets.facet.app.FacetAppSpecifier;
import facets.facet.app.FacetPagedSurface;
import facets.facet.app.FacetPreferences;
import facets.util.Debug;
import facets.util.app.AppValues;
import facets.util.tree.ValueNode;
import java.awt.Dimension;
final class AppPreferences extends FacetPreferences{
	static final String TITLE_PAGE_TYPES=FacetPagedSurface.TITLE_TOP+ARG_TYPES,
		TITLE_BEHAVIOUR="Behaviour";
	static final Object[][]DECORATIONS=new Object[][]{
		{FacetAppSpecifier.PATH_ARGS,"Features"},
		{TITLE_PAGE_TYPES,"Content","","Adjust content types, views and behaviour"},
		{ARG_TYPES,"Types","","Also determines behaviour"},
		{ContentTypes.Text.toString(),"","","Text in single view"},
		{ContentTypes.Table.toString(),"","","Table in two views with simple layout control"},
		{ContentTypes.Both.toString(),"B&oth","","Choice of text or table, table has row text view"},
		{ContentTypes.Nested.toString(),"Nested","","Table with nested row text and table content"},
		{TITLE_BEHAVIOUR,"Behaviours","","Also depend on content style"},
		{ARG_KB,"Si&ze","","In KB to force memory usage"},
		{ARG_TIMES,"Simulate build","","Protract build to match size"},
		{ARG_CHANGES,"Simulate chan&ges","","When set triggers checks on close"},
		{ARG_NO_FILES,"With sa&ve","","Sets full file menu with dummy actions"},
		{ARG_SLAVABLE,"Add ne&w in external windows","",
			"For multiple content types in single or tabbed layout"},
		{TableContenter.ARG_NESTED2,"Doubly nested","",
				"Nested table has its own nested content"},
		{FacetPreferences.TITLE_SURFACE_STYLE,"Content Style","","Also affects content behaviour"},
	};
	AppPreferences(FacetAppSpecifier spec,SSurface parent,FacetFactory ff){
		super(spec,parent,ff);
	}
	@Override
	protected PagedContenter[]newArgContenters(){
		PagedContenter[]contenters=super.newArgContenters();
		return new PagedContenter[]{
			contenters[ARGS_SURFACE],
			new Content(ff,parent,spec,args),
		};
	}
	final class Content extends ValueDialogContenter{
		private static final int TYPES_AT=0,BEHAVIOURS_AT=1,
			SLAVABLE_AT=0,CHANGES_AT=1,SIZE_AT=2,TIMES_AT=3,EMBED2_AT=4;
		private final SIndexing types,size;
		private final SToggling slavable,changes,files,times,nested2;
		Content(FacetFactory ff,SSurface surface,AppValues app,final ValueNode working){
			super(TITLE_PAGE_TYPES,ff,surface,app,working,working);
			ContentTypes[]typeValues=ContentTypes.values();
			types=new SIndexing(ARG_TYPES,typeValues,typeValues[working.getOrPutInt(ARG_TYPES,0)],
					new SIndexing.Coupler(){
				public void indexSet(SIndexing i){
					targetValuesUpdated(i,content.working,ARG_TYPES);
				}
				@Override
				public boolean[]liveStates(SIndexing i){
					return true||style()!=SINGLE?super.liveStates(i)
						:new boolean[]{true,true,false,false};
				}
			});
			slavable=content.newToggling(working,ARG_SLAVABLE);
			changes=content.newToggling(working,ARG_CHANGES);
			files=content.newToggling(working,ARG_NO_FILES,true);
			size=content.newIndexing(working,ARG_KB,SIZES,working.getOrPutInt(ARG_KB,SIZES[0]));
			times=content.newToggling(working,ARG_TIMES);
			nested2=content.newToggling(working,TableContenter.ARG_NESTED2);
			contentRetargeted(working);
		}
		@Override
		protected void targetValuesUpdated(STarget target,ValueNode values,String keys){
			if(target==types)values.put(ARG_TYPES,((SIndexing)target).index());
			else if(target==size)values.put(ARG_KB,((SIndexing)target).indexed());
		}
		@Override
		protected void contentRetargeted(ValueNode working){
			ContentStyle style=style();
			int typesAt=types.index();
			boolean both=typesAt>=Both.ordinal(),canSlave=both&&style!=DESKTOP,
				mustSlave=canSlave&&style==SINGLE;
			slavable.setLive(canSlave);
			if(!canSlave)slavable.set(false);
			else if(mustSlave)slavable.set(true);
			boolean changes=this.changes.isSet();
			if(!changes)files.set(false);
			files.setLive(changes);
			boolean embedding=types.index()==ContentTypes.Nested.ordinal();
			nested2.setLive(embedding);
			if(!embedding)nested2.set(false);
		}
		private ContentStyle style(){
			return ContentStyle.values()[content.working.getInt(ContentStyle.NATURE_KEY)];
		}
		@Override
		public STarget[]lazyContentAreaElements(SAreaTarget area){
			return new STarget[]{types,new TargetCore(TITLE_BEHAVIOUR,slavable,
							new TargetCore("Changes+Files",changes,files),size,times,nested2)};
		}
		@Override
		protected PanelFactory newPanelFactory(FacetFactory core){
			return new PanelFactory(core){
				@Override
				public SFacet newContentPanel(SContentAreaTargeter t){
					STargeter elements[]=t.elements(),behaviour=elements[BEHAVIOURS_AT],
						behaviours[]=behaviour.elements();
					return rowPanel(t,0,0,HINT_PANEL_INSET,
						indexingRadioButtons(elements[TYPES_AT],HINT_TALL+HINT_HEADED),BREAK,
						spacerWide(30),togglingCheckboxes(behaviours[EMBED2_AT],HINT_BARE),BREAK,
						spacerTall(5),BREAK,
						rowPanel(behaviour,10,5,HINT_PANEL_BORDER+HINT_NONE,
							togglingCheckboxes(behaviours[SLAVABLE_AT],HINT_BARE),BREAK,
							togglingCheckboxes(behaviours[CHANGES_AT],HINT_BARE),BREAK,
							indexingDropdownList(behaviours[SIZE_AT],HINT_NONE),BREAK,
							togglingCheckboxes(behaviours[TIMES_AT],HINT_BARE),BREAK,
							BREAK,fill()),
						BREAK,fill()
					);
				}
			};
		}
		@Override
		public Dimension contentAreaSize(){
			return new Dimension(200,240);
		}
	}
}