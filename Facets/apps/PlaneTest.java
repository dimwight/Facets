package apps;
import static facets.facet.app.FacetPreferences.*;
import facets.core.app.ActionViewerTarget;
import facets.core.app.AreaRoot;
import facets.core.app.FeatureHost;
import facets.core.app.PagedContenter;
import facets.core.app.SAreaTarget;
import facets.core.app.FeatureHost.LayoutFeatures;
import facets.core.app.SContentAreaTargeter;
import facets.core.app.SContenter;
import facets.core.app.SView;
import facets.core.app.SViewer;
import facets.core.app.ViewableFrame;
import facets.core.app.ViewerContenter;
import facets.core.app.ViewerTarget;
import facets.core.app.avatar.AvatarContent;
import facets.core.app.avatar.AvatarPolicies;
import facets.core.app.avatar.AvatarPolicy;
import facets.core.app.avatar.Painter;
import facets.core.app.avatar.PainterSource;
import facets.core.app.avatar.PainterSource.Transform;
import facets.core.app.avatar.PlaneView;
import facets.core.app.avatar.PlaneViewWorks;
import facets.core.superficial.SFacet;
import facets.core.superficial.SNumeric;
import facets.core.superficial.STarget;
import facets.core.superficial.STargeter;
import facets.core.superficial.app.FacetedTarget;
import facets.core.superficial.app.SSelection;
import facets.core.superficial.app.SSurface;
import facets.facet.AppFacetsBuilder;
import facets.facet.AreaFacets;
import facets.facet.FacetFactory;
import facets.facet.app.FacetAppSpecifier;
import facets.facet.app.FacetAppSurface;
import facets.util.Doubles;
import facets.util.ItemList;
import facets.util.NumberPolicy;
import facets.util.Objects;
import facets.util.geom.Vector;
import facets.util.shade.Shades;
final class PlaneTest extends ViewerContenter{
	final private FacetAppSurface app;
	private final AvatarPolicies policies=new AvatarPolicies(){
		@Override
		public AvatarPolicy avatarPolicy(SViewer viewer,AvatarContent content,
				PainterSource p){
			return new AvatarPolicy(){
				@Override
				public Painter[]newViewPainters(boolean selected,boolean active){
					ItemList<Painter>painters=new ItemList(Painter.class);
					boolean single=false;
					for(int stop=single?1:5,i=0;i<stop;i++){
						Painter painter=p.textOutline(single?content.toString():(""+i%5),"Sans-Serif",
								(single?12:(int)plane.plotShift().y)+(plane.scaleToViewer()?1:20),
								false,false,Shades.gray,null);
						double x=single?10:Math.random()*(plane.showWidth()-10),
								y=single?x:Math.random()*(plane.showHeight()-10);
						p.applyTransforms(new Transform[]{p.transformAt(x,y)},
							true,new Painter[]{painter});
						painters.add(painter);
					}
					return painters.items();
				}
				@Override
				public Painter[]newPickPainters(Object hit,boolean selected){
					return new Painter[]{};
				}
			};
		}
	};
	private final double fontHeight=12;
	private final PlaneView plane=new PlaneViewWorks("Text",100,100,
			new Vector(0,fontHeight),policies){
		public void setShowValues(double w,double h,Vector plot,double scale){
			trace(".setShowValues: "+Objects.toString(Doubles.toObjects(
					new double[]{w,h,plot.x,plot.y,scale})));
			super.setShowValues(w,h,plot,scale);
		}
		@Override
		public boolean scaleToViewer(){
			return true;
		}
		@Override
		public int ySign(){
			return 1;
		}
	};
	private PlaneTest(Object source,FacetAppSurface app){
		super(source);
		this.app=app;
	}
	@Override
	protected ViewableFrame newContentViewable(Object source){
		ViewableFrame viewable=new ViewableFrame("Content",source){
			@Override
			protected void viewerSelectionChanged(SViewer viewer,
					SSelection selection){}
		};
		viewable.defineSelection(new SSelection(){
			@Override
			public Object content(){
				return source;
			}
			@Override
			public Object single(){
				return multiple()[0];
			}
			@Override
			public Object[]multiple(){
				return new Object[]{content()};
			}
		});
		return viewable;
	}
	@Override
	protected FacetedTarget[]newContentViewers(ViewableFrame viewable){
		return ActionViewerTarget.newViewerAreas(viewable,ViewerTarget.newViewFrames(
				new SView[]{plane}
			));
	}
	@Override
	public STarget[]lazyContentAreaElements(SAreaTarget area){
		int barFrom=15,barCount=24;
		STarget barAt=new SNumeric("From",barFrom,new SNumeric.Coupler(){
			@Override
			public void valueSet(SNumeric n){
				int value=(int)n.value();
			}
			@Override
			public NumberPolicy policy(SNumeric n){
				return new NumberPolicy.Ticked(1,barCount,10){
					@Override
					public int format(){
						return FORMAT_DECIMALS_0;
					}
					@Override
					public int labelSpacing(){
						return TICKS_DEFAULT;
					}
				};
			}
		});
		return new STarget[]{barAt};
	}
	@Override
	protected void attachContentAreaFacets(AreaRoot area){
		app.ff.areas().attachViewerAreaPanes(area,"",AreaFacets.PANE_SPLIT_HORIZONTAL);
	}
	@Override
	public LayoutFeatures newContentFeatures(SContentAreaTargeter area){
		return new FacetFactory(app.ff){
			@Override
			public SFacet[]header(){
				return new SFacet[]{
					menuRoot(new AppFacetsBuilder(this,area).newMenuFacets()),
				};
			}
			@Override
			public SFacet toolbar(){
				if(true)return null;
				ItemList<SFacet>facets=new ItemList(SFacet.class);
				STargeter barStart=area.elements()[0];
				facets.add(numericSliders(barStart,200,
						HINT_SLIDER_FIELDS_TICKS_LABELS+HINT_SLIDER_LOCAL));
				return toolGroups(area,HINT_PANEL_MIDDLE,facets.items());
			}
		};
	}
	public static void main(String[] args){
		new FacetAppSpecifier(PlaneTest.class){
			@Override
			public PagedContenter[]adjustPreferenceContenters(SSurface surface,
					PagedContenter[]contenters){
				return false?contenters:new PagedContenter[]{
					contenters[PREFERENCES_TRACE],
					contenters[PREFERENCES_GRAPH],
//					contenters[PREFERENCES_VALUES],
					contenters[PREFERENCES_VIEW],
				};
			}
			@Override
			public boolean isFileApp(){
				return false;
			}
			public boolean canCreateContent(){
				return false;
			}
			@Override
			protected FacetAppSurface newApp(FacetFactory ff,FeatureHost host){
				return new FacetAppSurface(this,ff){
					@Override
					protected Object getInternalContentSource(){
						return new AvatarContent[]{
								new AvatarContent(){
									@Override
									public String toString(){
										return "Aa";
									}
								}
						};
					};
					@Override
					protected SContenter newContenter(Object source){
						return new PlaneTest(source,this);
					}
				};
			}
		}.buildAndLaunchApp(args);
	}
}
