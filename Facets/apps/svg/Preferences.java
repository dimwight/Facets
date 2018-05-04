package apps.svg;
import static apps.svg.PathContenter.*;
import facets.core.app.Dialogs;
import facets.core.app.HideableHost;
import facets.core.app.PagedActionDefaults;
import facets.core.app.PagedActions;
import facets.core.app.PagedContenter;
import facets.core.app.PagedSurface;
import facets.core.app.SAreaTarget;
import facets.core.app.SContentAreaTargeter;
import facets.core.superficial.SFacet;
import facets.core.superficial.SIndexing;
import facets.core.superficial.STarget;
import facets.core.superficial.STargeter;
import facets.core.superficial.SToggling;
import facets.core.superficial.app.SSurface.WindowAppSurface;
import facets.facet.FacetFactory;
import facets.facet.ValueDialogContenter;
import facets.facet.app.FacetAppSurface;
import facets.facet.app.FacetPagedSurface;
import facets.util.tree.ValueNode;
import java.awt.Dimension;
import apps.svg.SvgPathView.ReadValues;
final class Preferences{
	private final FacetAppSurface app;
	private final ValueNode state;
	Preferences(FacetAppSurface app,ValueNode state){
		this.app=app;
		this.state=state;
	}
	PagedContenter newReadContenter(final ValueNode working,final boolean invertAsk){
		return new ValueDialogContenter(ReadValues.TITLE,app.ff,app,app.spec, state, working){
			public STarget[]lazyContentAreaElements(SAreaTarget area){
				boolean set=working.getBoolean(KEY_NO_ASK);
				SToggling ask=new SToggling("As&k on each read|Don't as&k me again",
						invertAsk?!set:set,new SToggling.Coupler(){
					public void stateSet(SToggling t){
						boolean set=t.isSet();
						working.put(KEY_NO_ASK,invertAsk?!set:set);
					}
				});
				return new STarget[]{
					ReadValues.SF.newIndexing(working),
					ReadValues.SHIFT.newIndexing(working),
					ask
				};
			}
			protected PanelFactory newPanelFactory(FacetFactory core){
				return new PanelFactory(core){
					public SFacet newContentPanel(SContentAreaTargeter t){
						STargeter[]elements=t.elements();
						return rowPanel(t,0,0,HINT_PANEL_INSET,
								rowPanel(t,0,10,HINT_PANEL_RIGHT,
									indexingDropdownList(elements[0],HINT_NONE),BREAK,
									indexingDropdownList(elements[1],HINT_NONE)),
							BREAK,spacerWide(5),
							togglingCheckboxes(elements[2],HINT_BARE+(!invertAsk?HINT_TITLE1:"")),
							BREAK,fill());
					}
				};
			}
			public Dimension contentAreaSize(){
				return new Dimension(180,80);
			}
			protected void targetValuesUpdated(STarget target,ValueNode values,
					String keys){}
			protected void contentRetargeted(ValueNode working){}
		};
	}
	PagedContenter newRenderContenter(final ValueNode working){
		return new ValueDialogContenter("Render",app.ff,app,app.spec, state, working){
			protected void contentRetargeted(ValueNode working){}
			public STarget[]lazyContentAreaElements(SAreaTarget area){
				return new STarget[]{newRenderIndexing(working)};
			}
			protected void targetValuesUpdated(STarget target,ValueNode values,
					String keys){}
			protected PanelFactory newPanelFactory(FacetFactory core){
				return new PanelFactory(core){
					public SFacet newContentPanel(SContentAreaTargeter t){
						return rowPanel(t,10,10,HINT_PANEL_INSET,
								indexingRadioButtons(t.elements()[0],HINT_BARE),
								BREAK,fill());
					}
				};
			}
			@Override
			public Dimension contentAreaSize(){
				return new Dimension(0,0);
			}
		};
	}
	void launchReadPreferences(){
		app.dialogs().launchSurfaced(new Dialogs.Surfacer(){
			public PagedSurface newSurface(String title,
					HideableHost host,PagedActions actions,PagedContenter[]contents,WindowAppSurface app){
				return new FacetPagedSurface(title,host,actions,
						contents,(FacetAppSurface)app){
					public boolean isResizable(){
						return false;
					}
				};
			}
		},ReadValues.TITLE,PagedActionDefaults.newOk(),
		newReadContenter((ValueNode)state.copyState(),false));
	}
	static String newRenderIndexable(ValueNode values){
		return values.getOrPutString(KEY_RENDER,KEY_RENDER+RENDER_PEN
			).replace(KEY_RENDER,"");
	}
	static SIndexing newRenderIndexing(final ValueNode values){
		return new SIndexing("Render",new Object[]{RENDER_PEN,RENDER_FILL,RENDER_BOTH},
				newRenderIndexable(values),new SIndexing.Coupler(){
			public void indexSet(SIndexing i){
				values.put(KEY_RENDER,KEY_RENDER+i.indexed());
			}
		});
	}
}
