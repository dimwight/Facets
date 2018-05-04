package facets.facet.kit.swing;
import static facets.facet.AreaFacets.*;
import facets.core.app.ActionAppSurface;
import facets.core.app.AppConstants;
import facets.core.app.SAreaTarget;
import facets.core.app.ViewerTarget;
import facets.core.app.AppSurface.ContentStyle;
import facets.core.superficial.STarget;
import facets.core.superficial.SToggling;
import facets.core.superficial.STrigger;
import facets.core.superficial.Notifying.Impact;
import facets.facet.app.FacetAppSurface;
import facets.facet.kit.KWrap;
import facets.facet.kit.KitCore;
import facets.facet.kit.KitFacet;
import facets.util.Debug;
import facets.util.app.AppValues;
import facets.util.app.BusyCursor.BusySettable;
import facets.util.tree.ValueNode;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
final class AppMultiMount extends MountCore implements STarget.Targeted{
	private static final boolean indirect=false;
	final class DesktopContent extends JPanel implements BusySettable{
		final KitFacet facet;
		DesktopContent(KWrap wrap){
			facet=wrap.facet();
			setLayout(new BorderLayout());
			setBorder(BorderFactory.createLineBorder(
					false?Color.darkGray:ViewerBase.FOCUS_COLOR.darker()));
		  add((Component)wrap.wrapped(),BorderLayout.CENTER);
		}
		String windowTitle(){
			return facet.title();
		}
		boolean windowCanClose(){
			if(false)((SAreaTarget)facet.target()).ensureActive(Impact.ACTIVE);
			return app.tryCloseContent();
		}
		void windowSetMaximum(boolean onOff){
			areaValues.put(KEY_DESKTOP_MAXIMUM,onOff);
		}
		void windowActivated(){
			if(false)traceDebug(".windowActivated: facet=",facet);
			((SAreaTarget)facet.target()).ensureActive(Impact.ACTIVE);
		}
		Icon windowIcon(){
			return (Icon)kit.getDecorationIcon(AppConstants.NATURE_APP_ICON_INTERNAL, false);
		}
		@Override
		public String toString(){
			return Debug.info(this)+" "+Debug.info(facet);
		}
	}
	private final STrigger desktopTile=new STrigger(TITLE_DESKTOP_TILE,
			new STrigger.Coupler(){
		public void fired(STrigger t){
			desktop.tileWindows();
		}
	});
	private final SToggling deskTopScale=new SToggling(TITLE_DESKTOP_SCALE,true,
			new SToggling.Coupler(){
		public void stateSet(SToggling t){
			boolean on=t.isSet();
			if(swing!=null)desktop.setScaleToDesktop(on);
			areaValues.put(KEY_DESKTOP_NO_SCALE,!on);
		}
	});
  public STarget[]targets(){
	  return getMount()instanceof AppTabs?new STarget[]{}
			:new STarget[]{desktopTile,deskTopScale};
	}
  private final ActionAppSurface app;
	private final KitCore kit;
	private final ValueNode areaValues;
	private AppDesktop desktop;
	AppMultiMount(KitFacet facet,FacetAppSurface app,KitCore kit){
		super(facet,null);
		this.app=app;
		this.kit=kit;
		areaValues=app.spec.state(TYPE);
		deskTopScale.set(!areaValues.getBoolean(KEY_DESKTOP_NO_SCALE));
		JComponent mounting=new JPanel(new BorderLayout()),
			mountable=app.contentStyle==ContentStyle.DESKTOP?(desktop=new AppDesktop(new Color(COLOR_DESKTOP.rgb()),
					areaValues.getOrPutBoolean(KEY_DESKTOP_MAXIMUM,true),deskTopScale.isSet()
				))
			:new AppTabs(kit,app);
    if(indirect)mounting.add(mountable,BorderLayout.CENTER);
    setSwing(indirect?mounting:mountable);
    KitCore.widgets++;
  }
	public void setItems(KWrap... items){
		Component mount=getMount();
	  if(mount instanceof AppTabs)((AppTabs)mount).setItems(items);
		else{
	  	List<DesktopContent>disposeContents=new ArrayList(desktop.getContents()),
	  		nowContents=new ArrayList();
	  	for(KWrap item:items){
	  		DesktopContent addContent=null;
				for(DesktopContent thenContent:desktop.getContents())
	  			if(thenContent.facet==item.facet()){
	  				nowContents.add(addContent=thenContent);
	  				disposeContents.remove(thenContent);
	  			}
	  		if(addContent==null)nowContents.add(new DesktopContent(item));
		  }
	  	desktop.setContents(nowContents);
	  	desktopTile.setLive(nowContents.size()>1);
	  	boolean checkMem=false&&Debug.memCheck;
			if(checkMem)Debug.memCheck("AppMount.setItems");
	  	if(false)for(DesktopContent dispose:disposeContents){
				for(STarget d:((SAreaTarget)(dispose).facet.target()).descendants())
					if(d instanceof ViewerTarget)
						((ViewerBase)((KitFacet)((ViewerTarget)d).attachedFacet()).base()
								).disposeWrapped();
				dispose.removeAll();
			}
	  	if(checkMem)Debug.memCheck("AppMount.setItems~");
	  }
	}
	public void setActiveItem(KWrap item){
	  Component base=getMount();
	  if(base instanceof AppTabs)
	    ((AppTabs)base).setSelectedComponent(swingWrapped(item));
		else for(DesktopContent content:desktop.getContents())
			if((content).facet==item.facet())
				desktop.setActiveContent(content);
	}
	private Component getMount(){
		return indirect?swing.getComponent(0):swing;
	}
}
