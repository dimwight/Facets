package demo.appidiom;
import static demo.appidiom.AppContent.*;
import static facets.facet.AreaFacets.*;
import static facets.facet.FacetFactory.*;
import facets.core.app.ActionViewerTarget;
import facets.core.app.AppContenter;
import facets.core.app.AreaRoot;
import facets.core.app.ArrayPath;
import facets.core.app.HtmlView;
import facets.core.app.NestedView;
import facets.core.app.PathSelection;
import facets.core.app.TableView;
import facets.core.app.TextView;
import facets.core.app.TreeView;
import facets.core.app.ViewerContenter;
import facets.core.app.FeatureHost.LayoutFeatures;
import facets.core.app.HtmlView.InputView;
import facets.core.app.TextView.LongText;
import facets.core.superficial.FacetedTarget;
import facets.core.superficial.SFacet;
import facets.core.superficial.SFrameTarget;
import facets.core.superficial.STarget;
import facets.core.superficial.STargeter;
import facets.core.superficial.SToggling;
import facets.core.superficial.app.SAreaTarget;
import facets.core.superficial.app.SContentAreaTargeter;
import facets.core.superficial.app.SSelection;
import facets.core.superficial.app.SViewer;
import facets.core.superficial.app.SelectionView;
import facets.core.superficial.app.ViewableFrame;
import facets.facet.AreaFacets;
import facets.facet.FacetFactory;
import facets.facet.ViewerAreaMaster;
import facets.facet.app.FacetAppSurface;
import facets.util.Debug;
import facets.util.HtmlBuilder;
import facets.util.Stateful;
import facets.util.ValueProxy;
import facets.util.HtmlFormBuilder.FormInput;
abstract class TableContenterBase extends ViewerContenter{
	protected final FacetAppSurface app;
	protected final boolean nested2;
	private static final String TAIL_ANALYSIS=" - Anal&ysis";
	protected TableContenterBase(AppContent content,FacetAppSurface app){
		super(content);
		this.app=app;
		nested2=app.spec.args().getBoolean(ARG_NESTED2);
	}
	@Override
	final protected FacetedTarget[]newContentViewers(ViewableFrame viewable){
		final boolean nested=this instanceof NestedTable;
		String title=title();
		SFrameTarget basic=newTableFrame(nested?title:TITLE_NESTED0,false),
			full=nested?null:newTableFrame(TITLE_NESTED0+TAIL_ANALYSIS,true),
			text=newTextFrame(
					(true?((this instanceof NestedTable?title():TITLE_NESTED0)+" - "):"")+TITLE_TEXT),
			nestedText=newTextAreaFrame(),
			nestedTable=title==TITLE_NESTED2?null:newTableAreaFrame();
		return ActionViewerTarget.newViewerAreas(viewable,
				chooseViewFrames(basic,full,text,nestedText,nestedTable,
						newTreeFrame(title),newFormFrame(TITLE_NESTED0)));
	}
	@Override
	final protected ViewableFrame newContentViewable(Object source){
		AppContent content=(AppContent)source;
		final Object[]rows=content.asRows();
		return new ViewableFrame(content.title(),rows){{
				setSelection(new PathSelection(rows,new ArrayPath(rows,rows[0])));
			}
			@Override
			protected SSelection newViewerSelection(SViewer viewer){
				return ((SelectionView)viewer.view()).newViewerSelection(viewer,selection());
			}
			@Override
			protected void viewerSelectionChanged(SViewer viewer,SSelection selection){
				Object source=((ValueProxy)((PathSelection)selection).single()).source;
				for(Object row:rows)if(source==row){
						setSelection(new PathSelection(rows,new ArrayPath(rows,row)));
						break;
					}
			}
			@Override
			protected void viewerSelectionEdited(SViewer viewer,Object edit,boolean interim){
				trace(".viewerSelectionEdited: name=",((FormInput)edit).name);
			}
		};
	}
	@Override
	public
	final STarget[]lazyContentAreaElements(SAreaTarget area){
		return app.ff.areas().panesGetTarget(area).elements();
	}
	static final String ARG_NESTED2="nested2";
	private static final String TITLE_TEXT="Row Te&xt",TITLE_NESTED0="Paragraphs",
		TITLE_NESTED1="Sentences",TITLE_NESTED2="Words";
	private static abstract class NestedTable extends TableContenterBase{
		private final String title;
		NestedTable(AppContent source,FacetAppSurface app,String title){
			super(source,app);
			this.title=title;
		}
		@Override
		public String title(){
			return title;
		}
		@Override
		protected SFrameTarget[]chooseViewFrames(SFrameTarget basic,SFrameTarget full,
				SFrameTarget text,SFrameTarget nestedText,SFrameTarget nestedTable, SFrameTarget tree, SFrameTarget form){
			return nested2&&title()==TITLE_NESTED1?
					new SFrameTarget[]{basic,false?text:nestedText,nestedTable}
				:new SFrameTarget[]{basic,false&&nested2?text:nestedText
				};
		}
		@Override
		protected void attachAreaPanes(AreaFacets areas,final SAreaTarget area,
				SFacet[]viewers){
			if(area.indexableTargets().length>2)areas.attachPanes(area,viewers,
					new int[]{PANE_SPLIT_HORIZONTAL,PANE_UPPER,PANE_SPLIT_HORIZONTAL},
					new double[]{0.66,0.6});
			else areas.attachPanes(area,viewers,PANE_SPLIT_VERTICAL);
		}
	}
	private SFrameTarget newTableAreaFrame(){
		final boolean nested=this instanceof NestedTable;
		return new SFrameTarget(new NestedView(nested?TITLE_NESTED2:TITLE_NESTED1){
			@Override
			public AppContenter newViewerContenter(Object source){
				String title=title();
				AppContent content=(AppContent)source;
				return nested?new NestedTable(content,app,title){} 
					:new NestedTable(content,app,title){};
			}
		});
	}
	private SFrameTarget newTextAreaFrame(){
		return new SFrameTarget(new NestedView(
				(this instanceof NestedTable?title():TITLE_NESTED0)+" - "+TITLE_TEXT){
			@Override
			public AppContenter newViewerContenter(Object source){
				AppContent content=(AppContent)source;
				String title=title();
				FacetFactory ff=app.ff;
				return title.startsWith(TITLE_NESTED2)?new RowTextContenter(content,ff,title){}
					:title.startsWith(TITLE_NESTED1)?new RowTextContenter(content,ff,title){}
						:new RowTextContenter(content,ff,title){};
			}
		});
	}
	protected abstract SFrameTarget[]chooseViewFrames(SFrameTarget basic,SFrameTarget full,
			SFrameTarget text,SFrameTarget nestedText,SFrameTarget nestedTable, SFrameTarget tree, SFrameTarget form);
	@Override
	final protected void attachContentAreaFacets(final AreaRoot area){
		final FacetFactory ff=app.ff;
		AreaFacets areas=ff.areas();
		attachAreaPanes(areas,area,areas.viewerAreaChildren(area,new ViewerAreaMaster(){
			@Override
			public String typeKey(){
				return layoutKey(area);
			}
			@Override
			protected String hintString(){
				return HINT_PANEL_ABOVE;
			}
			@Override
			protected SFacet newViewTools(STargeter viewTargeter){
				STargeter[]elements=viewTargeter.elements();
				return !viewTargeter.title().startsWith(TITLE_TEXT)&&elements.length==0?null
					:ff.toolGroups(viewTargeter,HINT_NONE,
							ff.togglingCheckboxes(elements[0],HINT_BARE));
			}
		}));
	}
	protected String layoutKey(SAreaTarget area){
		return title();
	}
	protected abstract void attachAreaPanes(AreaFacets areas,SAreaTarget area,SFacet[]viewers);
	@Override
	public LayoutFeatures newContentFeatures(SContentAreaTargeter area){
		return null;
	}
	static SFrameTarget newTextFrame(final String title){
		final SToggling wrap=new SToggling("Wrap",true,new SToggling.Coupler());
		return new SFrameTarget(new TextView(title){
			@Override
			public SSelection newViewerSelection(SViewer viewer,final SSelection viewable){
				return new SSelection(){
					@Override
					public Object content(){
						return((AppContent)viewable.single()).asText();
					}
					@Override
					public Object single(){
						return content();
					}
					@Override
					public Object[]multiple(){
						throw new RuntimeException("Not implemented in "+Debug.info(this));
					}
				};
			}
			@Override
			public boolean wrapLines(){
				return wrap.isSet();
			}
		}){
			@Override
			protected STarget[]lazyElements(){
				return new STarget[]{wrap};
			}
		};
	}
	static SFrameTarget newTableFrame(final String title,final boolean full){
		return new SFrameTarget(new TableView(title){
			@Override
			protected ValueProxy newRowProxy(Stateful source){
				return new ValueProxy(source){
					@Override
					protected Object[]lazyValues(){
						String values[]=((AppContent)source).newRowNode().values();
						Object at=values[TABLE_ROW],rowText=new LongText(values[AppContent.TABLE_TEXT]);
						return !full?new Object[]{at,rowText}
							:new Object[]{rowText,Integer.valueOf(values[TABLE_COUNT]),at,
								Boolean.valueOf(values[TABLE_SHORT])};
					}
				};
			}
			@Override
			public String getColumnTitle(int col){
				return AppContent.fieldTitles(full)[col];
			}
			@Override
			public String typeKey(){
				return !title.contains("Embed")?"TableView":title;
			}
		});
	}
	static SFrameTarget newFormFrame(String title){
		title+=TAIL_ANALYSIS;
		return new SFrameTarget(false?new HtmlView(title){
			@Override
			public SSelection newViewerSelection(SViewer viewer,SSelection viewable){
				return newFormSelection(viewable,this);
			}
		}
		:new InputView(title){
			@Override
			public SSelection newViewerSelection(SViewer viewer,SSelection viewable){
				return newFormSelection(viewable,this);
			}
		});
	}
	private static SSelection newFormSelection(SSelection viewable,HtmlView view){
		final HtmlBuilder form=AppContent.newRowForm(((AppContent)viewable.single()).newRowNode(),
				view instanceof InputView);
		return new SSelection(){
			@Override
			public Object content(){
				return form.buildPage();
			}
			@Override
			public Object single(){
				throw new RuntimeException("Not implemented in "+Debug.info(this));
			}
			@Override
			public Object[]multiple(){
				throw new RuntimeException("Not implemented in "+Debug.info(this));
			}
		};
	}
	static SFrameTarget newTreeFrame(String title){
		return new SFrameTarget(new TreeView(title){
			@Override
			public SSelection newViewerSelection(SViewer viewer,SSelection viewable){
				return PathSelection.newMinimal(((AppContent)viewable.single()).asTree());
			}
			@Override
			public boolean canChangeSelection(){
				return false;
			}
		});
	}
}