package facets.facet;
import static facets.facet.kit.swing.KitSwing.*;
import facets.core.app.AppContenter;
import facets.core.app.AppSurface;
import facets.core.app.AreaRoot;
import facets.core.app.FacetHostable;
import facets.core.app.FeatureHost;
import facets.core.app.HideableHost;
import facets.core.app.NestedView;
import facets.core.app.PagedActions;
import facets.core.app.PagedContenter;
import facets.core.app.PagedSurface;
import facets.core.app.SimpleSurface;
import facets.core.app.SurfaceServices;
import facets.core.app.TypeKeyable;
import facets.core.app.Dialogs.Response;
import facets.core.app.FacetHostable.Hosting;
import facets.core.app.FeatureHost.LayoutFeatures;
import facets.core.superficial.Notice;
import facets.core.superficial.SFacet;
import facets.core.superficial.SFrameTarget;
import facets.core.superficial.SIndexing;
import facets.core.superficial.STarget;
import facets.core.superficial.SToggling;
import facets.core.superficial.Notifying.Impact;
import facets.core.superficial.STarget.Targeted;
import facets.core.superficial.app.SAreaTarget;
import facets.core.superficial.app.SHost;
import facets.core.superficial.app.SSelection;
import facets.core.superficial.app.SSurface;
import facets.core.superficial.app.SView;
import facets.core.superficial.app.SelectingFrame;
import facets.core.superficial.app.ViewerTarget;
import facets.core.superficial.app.SContentAreaTargeter.ContentArea;
import facets.core.superficial.app.SHost.FacetLayout;
import facets.facet.app.FacetAppSurface;
import facets.facet.app.FacetPagedSurface;
import facets.facet.kit.KWrap;
import facets.facet.kit.KitCore;
import facets.facet.kit.KitFacet;
import facets.facet.kit.Toolkit;
import facets.facet.kit.swing.BusyPanel;
import facets.facet.kit.swing.KitSwing;
import facets.util.Debug;
import facets.util.Tracer;
import facets.util.Util;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.geom.Dimension2D;
import javax.swing.JLabel;
import javax.swing.JPanel;
abstract class HostingFacetSwing extends Tracer implements Hosting,KitFacet,Targeted,
		TypeKeyable{
	protected final FacetHostable hostable;
	protected STarget target;
	protected boolean retarget;
	@Override
  public void dispose(){
  	traceDebug(".dispose: ",this);
  };
	HostingFacetSwing(FacetHostable hostable){
		super(HostingFacetSwing.class);
		this.hostable=hostable;
	}
	public void retarget(STarget target,Impact impact){
		if(true)retarget=true;
		if(retarget)this.target=target;
		hostable.facetRetargeted(this,target,impact);
		retarget=false;
	}
	@Override
	public String toString(){
		return Debug.info(this)+" target="+target;
	}
	@Override
	final public STarget target(){
		return target;
	}
	@Override
	public String typeKey(){
		return Util.shortTypeNameKey(this);
	}
	@Override
	final public STarget[]targets(){
		return new STarget[]{};
	}
	@Override
	final public KWrap[]items(){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	@Override
	final public void targetNotify(Object msg,boolean interim){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	KWrap newHostWrap(final SHost host){
		return new KWrap(){
			@Override
			public Object wrapped(){
				return host.wrapped();
			}
			@Override
			public KitFacet facet(){
				return HostingFacetSwing.this;
			}
			@Override
			public Object newWrapped(Object parent){
				throw new RuntimeException("Not implemented in "+Debug.info(this));
			}
			@Override
			public String toString(){
				return facet().toString();
			}
		};
	}
	@Override
	protected void traceOutput(String msg){
		super.traceOutput(title()+msg);
	}
	final static class Nested extends HostingFacetSwing{
		private final JPanel swing=new BusyPanel(new BorderLayout()){};
		private final FeatureHost host;
		private final KWrap base;
		private SimpleSurface surface;
		private SAreaTarget area;
		private SelectingFrame sourceFrame;
		private Object content;
		Nested(NestedView hostable,Toolkit kit){
			super(hostable);
			base=newHostWrap(host=new FeatureHost(){
				class Layout implements FacetLayout{
					final Container content;
					Layout(SFacet content){
						this.content=(Container)((KitFacet)content).base().wrapped();
					}
				}
				@Override
				public FacetLayout newLayout(SFacet content,LayoutFeatures features){
					return new Layout(content);
				}
				@Override
				public void setLayout(FacetLayout layout){
					swing.removeAll();
					swing.add(((Layout)layout).content,BorderLayout.CENTER);
				}
				@Override
				public Object wrapped(){
					return swing;
				}
				@Override
				public void setTitle(String title){}
				@Override
				public void updateLayout(SSurface surface){
					throw new RuntimeException("Not implemented in "+Debug.info(this));
				}
				@Override
				public void showExtras(boolean on){
					throw new RuntimeException("Not implemented in "+Debug.info(this));
				}
				@Override
				public void openHostedSurface(){
					throw new RuntimeException("Not implemented in "+Debug.info(this));
				}
				@Override
				public SurfaceServices activeServices(){
					throw new RuntimeException("Not implemented in "+Debug.info(this));
				}
			});
		}
		@Override
		public KWrap base(){
			return base;
		}
		@Override
		public String title(){
			return "["+(target==null?"Untargeted":target.title())+"]";
		}
		@Override
		public void refreshPaged(String title,PagedActions actions,Object source,
				AppSurface app){
			throw new RuntimeException("Not implemented in "+Debug.info(this));
		}
		@Override
		public void refreshViewer(Object source){
			SAreaTarget area=(SAreaTarget)source;
			ViewerTarget viewer=(ViewerTarget)area.activeFaceted();
			if(!viewer.isLive())return;
			SView view=viewer.view();
			boolean newArea=this.area!=null&&this.area!=area,
				newSource=view instanceof NestedView||newArea;
			if(newSource){
				if(!newArea&&view!=hostable)throw new IllegalStateException(
						"Bad view="+Debug.info(this));
				else sourceFrame=(SelectingFrame)
						((ContentArea)area.areaParent()).contenter.contentFrame();
			}
			if(false)traceDebug(".refreshViewer: sourceFrame=",sourceFrame);
			this.area=area;
			Object content=hostable.getSourceSelectionContent(sourceFrame.selection());
			boolean newContent=content!=null&&this.content!=content;
			if(!newContent)return;
			else this.content=content;
			if(false)traceDebug(".refreshViewer: content=",content);
			AppContenter contenter=hostable.newViewerContenter(content);
			if(surface==null)(surface=new SimpleSurface(host,contenter){
				@Override
				public void notify(Notice notice){
					if(retarget)return;
					super.notify(notice);
					if(target!=null)target.notifyParent(notice.impact);
				}
			}).buildRetargeted();
			else surface.replaceContent(contenter);
			AreaRoot root=(AreaRoot)surface.surfaceTargeter().target();
			area.setIndexing(SIndexing.newDefault(surface.title(),new STarget[]{root}));
		}
	}
	final static class Paged extends HostingFacetSwing{
		@Override
		public final void refreshPaged(String title,PagedActions actions,Object source,
				AppSurface app){
			PagedContenter[]contenters=hostable.newPagedContenters(source);
			if(surface==null){
				(surface=new FacetPagedSurface(title,host,actions,contenters,(FacetAppSurface)app){
					private boolean notify;
					@Override
					public void notify(Notice notice){
						if(notify)return;
						else notify=true;
						super.notify(notice);
						if(target!=null&&!retarget)target.notifyParent(Impact.DEFAULT);
						notify=false;
					}
				}).buildRetargeted();
				Dimension size=surface.getLaunchBounds().getSize();
				swing.setMinimumSize(new Dimension(px(size.width),px(size.height)));
			}
			else surface.replaceContents(contenters);
		}
		private final JLabel titleBar=new JLabel(getClass().getSimpleName());
		private final JPanel swing=new BusyPanel(new BorderLayout(0,5)){
			@Override
			public Dimension getPreferredSize(){
				return getMinimumSize();
			};
		};
		private final HideableHost host;
		private final KWrap base;
		private SToggling sidebar;
		private PagedSurface surface;
		Paged(FacetHostable hostable,final Toolkit kit){
			super(hostable);
			base=newHostWrap(host=new HideableHost(){
				class Layout implements FacetLayout{
					final Container content,buttons;
					Layout(SFacet content,SFacet buttons){
						this.content=(Container)((KitFacet)content).base().wrapped();
						this.buttons=(Container)((KitFacet)buttons).base().wrapped();
					}
				}
				@Override
				public void setTitle(String text){
					titleBar.setText(((KitCore)kit).decodeTitleText(text));
				}
				@Override
				public FacetLayout newLayout(SFacet content,SFacet buttons,SFacet extras){
					return new Layout(content,buttons);
				}
				@Override
				public void setLayout(FacetLayout layout){
					Layout l=(Layout)layout;
					swing.add(l.content,BorderLayout.CENTER);
					if(l.buttons!=null)swing.add(l.buttons,BorderLayout.SOUTH);
				}
				@Override
				public void hide(Response response){
					if(sidebar==null)throw new IllegalStateException(
							"Null toggling in "+Debug.info(this));
					else sidebar.set(false);
				}
				@Override
				public Object wrapped(){
					return swing;
				}
				@Override
				public void updateLayout(SSurface surface){
					throw new RuntimeException("Not implemented in "+Debug.info(this));
				}
			});
			swing.add(titleBar,BorderLayout.NORTH);
			KitSwing.adjustComponents(true,swing);
			Font font=titleBar.getFont();
			titleBar.setFont(font.deriveFont(Font.BOLD).deriveFont(font.getSize2D()*1.2f));
		}
		@Override
		public void retarget(STarget target,Impact impact){
			if(true||target instanceof SToggling)sidebar=(SToggling)target;
			if(!sidebar.isSet())return;
			super.retarget(target,impact);
			KitSwing.adjustComponents(true,swing);
		}
		@Override
		public KWrap base(){
			return base;
		}
		@Override
		public String title(){
			return titleBar.getText();
		}
		@Override
		public void refreshViewer(Object source){
			throw new RuntimeException("Not implemented in "+Debug.info(this));
		}
	}
	final static class Viewer extends HostingFacetSwing{
		private final FeatureHost host;
		private final KWrap base;
		private SimpleSurface surface;
		private STarget target;
		Viewer(FacetHostable hostable,Toolkit kit){
			super(hostable);
			base=newHostWrap(host=((KitSwing)kit).newViewerHost());
		}
		@Override
		public KWrap base(){
			return base;
		}
		@Override
		public String title(){
			return Util.helpfulClassName(this);
		}
		@Override
		public void retarget(STarget target,Impact impact){
			this.target=target;
			super.retarget(target,impact);
		}
		@Override
		public void refreshViewer(Object source){
			AppContenter contenter=hostable.newViewerContenter(source);
			if(surface==null)(surface=new SimpleSurface(host,contenter)).buildRetargeted();
			else surface.replaceContent(contenter);
		}
		@Override
		public void refreshPaged(String title,PagedActions actions,Object source,
				AppSurface app){
			throw new RuntimeException("Not implemented in "+Debug.info(this));
		}
	}
}