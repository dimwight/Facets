package pub.view;
import static applicable.HtmlEditEncoder.*;
import static facets.facet.app.FacetPagedSurface.WizardPaged.*;
import static pdft.PdfCore.*;
import static pub.view.FieldsSpec.*;
import static pub.view.PubsView.*;
import facets.core.app.PagedContenter;
import facets.core.app.PagedSurface;
import facets.core.superficial.SFacet;
import facets.core.superficial.SIndexing;
import facets.core.superficial.STarget;
import facets.core.superficial.STargeter;
import facets.core.superficial.STextual;
import facets.core.superficial.SToggling;
import facets.core.superficial.app.SAreaTarget;
import facets.core.superficial.app.SContentAreaTargeter;
import facets.core.superficial.app.SSurface;
import facets.facet.FacetFactory;
import facets.facet.ValueDialogContenter;
import facets.facet.app.FacetAppSpecifier;
import facets.facet.app.FacetPreferences;
import facets.util.Objects;
import facets.util.Util;
import facets.util.tree.ValueNode;
import java.awt.Dimension;
import java.io.File;
import pub.PubValues;
final class OpenPreferences extends FacetPreferences{
	public static final String TITLE_LONG="Startup",TITLE_SHORT="Open",
		TITLE_PROGRAMS="Programs";
	OpenPreferences(FacetAppSpecifier spec,SSurface surface,FacetFactory ff){
		super(spec,surface,ff);
	}
	@Override
	protected PagedContenter[]newArgContenters(){
		ValueNode workingArgs=false?(ValueNode)args.copyState():args;
		PagedContenter[]pv=new PagedContenter[]{
			new Startup(this,args,workingArgs),
//			new Programs(this,args,workingArgs),
		};
		return PubValues.userView?pv:Objects.join(PagedContenter.class,pv,
				super.newArgContenters());
	}
	private static final class Programs extends ValueDialogContenter{
		private final STextual pdf,strong,weak;
		private String[]passed;
		Programs(OpenPreferences p,ValueNode master,ValueNode working){
			super(TITLE_PROGRAMS,p.ff,p.parent,p.spec,master,working);
			pdf=content.newTextual(working,KEY_READER);
			strong=content.newTextual(working,KEY_STRONG);
			weak=content.newTextual(working,KEY_WEAK);
		}
		@Override
		protected void targetValuesUpdated(STarget target,ValueNode values,String keys){}
		@Override
		protected void contentRetargeted(ValueNode working){
			passed=new String[]{KEY_READER,KEY_STRONG,KEY_WEAK};
			for(String key:passed)if(working.getString(key).equals("")){
					passed=null;
					return;
				}
			boolean debug=false;
			if(debug)trace(".targetValuesUpdated: passed=",passed);
			for(String key:passed){
				File exe=new File(Util.programs32,working.getString(key));
				if(!exe.exists()||exe.isDirectory()
						||!exe.getName().toLowerCase().endsWith(".exe"))passed=null;
				if(debug)trace(".targetValuesUpdated~: passed=",passed);
			}
			if(debug)trace(".contentRetargeted: passed=",passed);
			if(passed==null)return;
//			pdf.setText(pdfReader=working.getString(KEY_READER));
			strong.setText(strongEditor=working.getString(KEY_STRONG));
			weak.setText(weakEditor=working.getString(KEY_WEAK));
		}
		@Override
		public void areaRetargeted(SContentAreaTargeter area){
			super.areaRetargeted(area);
			for(String title:new String[]{TITLE_NEXT,TITLE_FINISH})
				PagedSurface.findDialogTrigger(area,title).setLive(passed!=null);
		}
		@Override
		public STarget[]lazyContentAreaElements(SAreaTarget area){
			return new STarget[]{pdf,strong,weak};
		}
		@Override
		protected PanelFactory newPanelFactory(FacetFactory core){
			return new PanelFactory(core){
				@Override
				public SFacet newContentPanel(SContentAreaTargeter t){
					STargeter[]elements=t.elements();
					return rowPanel(t,0,10,HINT_PANEL_RIGHT,new SFacet[]{
						newField(elements[0]),BREAK,
						newField(elements[1]),BREAK,
						newField(elements[2]),BREAK,fill()
					});
				}
				private SFacet newField(STargeter t){
					return textualField(t,20,HINT_NONE);
				}
			};
		}
		@Override
		public Dimension contentAreaSize(){
			return new Dimension(275,150);
		}
	}
	private static final class Startup extends ValueDialogContenter{
		private static final boolean debug=false;
		private static final int SEARCH_AT=0,TREE_AT=1,OPEN_AT=2,VIEW_AT=3;
		private final STextual search;
		private final SToggling tree,open;
		private final SIndexing view;
		Startup(OpenPreferences p,ValueNode master,ValueNode working){
			super(debug?TITLE_LONG:TITLE_SHORT,p.ff,p.parent,p.spec,master,working);
			view=content.newIndexing(master,ARG_OPEN,VIEWS,getOpenView(master));
			if(false)ValueNode.putCheckKey=RecordContenter.ARG_OPEN;
			open=content.newToggling(master,RecordContenter.ARG_OPEN);
			search=content.newTextual(master,ListingSearcher.ARG_SEARCH);
			tree=content.newToggling(master,ListingContenter.ARG_TREE);
		}
		@Override
		protected void targetValuesUpdated(STarget target,ValueNode values,String keys){
			if(target==view)values.put(ARG_OPEN,((SIndexing)target).index());
		}
		@Override
		protected void contentRetargeted(ValueNode working){}
		@Override
		public STarget[]lazyContentAreaElements(SAreaTarget area){
			return new STarget[]{search,tree,open,view};
		}
		@Override
		protected PanelFactory newPanelFactory(FacetFactory core){
			return new PanelFactory(core){
				@Override
				public SFacet newContentPanel(SContentAreaTargeter t){
					STargeter[]elements=t.elements();
					return rowPanel(t,0,10,HINT_PANEL_INSET,join(new SFacet[]{
						indexingRadioButtons(elements[VIEW_AT],(true||debug?HINT_HEADED:HINT_BARE)
								+HINT_TALL),BREAK,
						togglingCheckboxes(elements[OPEN_AT],HINT_BARE)},
					debug?new SFacet[]{
						textualField(elements[SEARCH_AT],10,HINT_NONE),BREAK,
						togglingCheckboxes(elements[TREE_AT],HINT_BARE),
						BREAK,fill()
					}
					:new SFacet[]{BREAK,fill()}
					)
					);
				}
			};
		}
		@Override
		public Dimension contentAreaSize(){
			return new Dimension(250,debug?260:180);
		}
	}
}