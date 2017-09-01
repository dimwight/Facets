package facets.facet.kit.swing;
import static facets.facet.FacetFactory.*;
import facets.core.app.PagedContenter;
import facets.core.superficial.Notifying.Impact;
import facets.core.superficial.app.SAreaTarget;
import facets.core.superficial.app.SContentAreaTargeter.ContentArea;
import facets.core.superficial.SFacet;
import facets.facet.kit.Decoration;
import facets.facet.kit.KWrap;
import facets.facet.kit.KitCore;
import facets.facet.kit.KitFacet;
import facets.facet.kit.swing.KitSwing.Dimensioned;
import facets.util.Debug;
import facets.util.IndexingIterator;
import facets.util.StringFlags;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
final class AreaTabs extends MountCore{
	private final SFacet[]areas;
	AreaTabs(final KitCore kit,final SFacet[]areas,final StringFlags hints){
		super(NO_FACET,null);
		this.areas=areas;
		setSwing(new Pane(kit,hints));
	}
	private final class Pane extends JTabbedPane implements KitSwing.Dimensioned{
		Pane(final KitCore kit,final StringFlags hints){
			KitSwing.adjustComponents(false,this);
			new IndexingIterator<SFacet>(areas){
				protected void itemIterated(SFacet item,int at){
					KitFacet area=(KitFacet)item;
					Decoration decorations=kit.decoration(area.title(),hints);
					KitCore.widgets++;
					addTab(decorations.caption,true?null:(Icon)decorations.icon,
							(JComponent)area.base().wrapped());
				}
			}.iterate();
			addChangeListener(new ChangeListener(){
				public void stateChanged(ChangeEvent e){
					ensureAreaActive();
				}
			});			
			addMouseListener(new MouseAdapter(){
				@Override
				public void mouseClicked(MouseEvent e){
					ensureAreaActive();
				}
			});
			setSelectedIndex(0);
		}
		private void ensureAreaActive(){
			int tabAt=getSelectedIndex();
			if(tabAt<0)throw new RuntimeException("Not implemented in "+this);
			SAreaTarget target=(SAreaTarget)((KitFacet)areas[tabAt]).target();
			target.ensureActive(Impact.ACTIVE);
			if(target instanceof ContentArea){
				Dimension size=((PagedContenter)((ContentArea)target).contenter).contentAreaSize();
				firePropertyChange(Dimensioned.PROPERTY,size.width,size.height<<8);
			}
		}
	}
	public void setActiveItem(KWrap item){
	  ((JTabbedPane)swing).setSelectedComponent((Component)item.wrapped());
	}
	public void setItems(KWrap... items){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
}