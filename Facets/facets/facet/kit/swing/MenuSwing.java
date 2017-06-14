package facets.facet.kit.swing;
import static facets.facet.FacetFactory.*;
import static facets.facet.kit.swing.KitSwing.*;
import static facets.facet.kit.swing.KitSwing.LaF.*;
import facets.core.app.AppConstants;
import facets.core.superficial.app.SurfaceStyle;
import facets.facet.kit.Decoration;
import facets.facet.kit.KWidget;
import facets.facet.kit.KWrap;
import facets.facet.kit.KitCore;
import facets.facet.kit.KitFacet;
import facets.util.StringFlags;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
final class MenuSwing extends Widget{
	MenuSwing(final KitFacet facet,String title,final ItemSource itemSource,
		final StringFlags hints,final KitCore kit){
	super(facet,null);
	if(title==null||title=="")throw new RuntimeException("No title in "+facet);
	final Decoration d=kit.decoration(title,hints);
	final JMenu swing=new JMenu(d.caption.replaceAll(":\\s*",""));
	if(surfaceStyle==SurfaceStyle.DESKTOP)swing.setMnemonic(d.mnemonic);
	final JPopupMenu popup=swing.getPopupMenu();
	if(panelShade!=null){
		swing.setBackground(new Color(panelShade.rgb()));
		popup.setBackground(new Color(panelShade.rgb()));
	}
	adjustComponents(false,swing);
	PopupMenuListener listener=new PopupMenuListener(){
		private KWrap[]itemsThen;
		public void popupMenuCanceled(PopupMenuEvent e){}
		public void popupMenuWillBecomeInvisible(PopupMenuEvent e){}
		public void popupMenuWillBecomeVisible(PopupMenuEvent e){
			if(false)swing.requestFocusInWindow();
			KWrap[]items=itemSource.getItems();
			if(hints.includeFlag(HINT_DEBUG))trace(".popupMenuWillBecomeVisible: items=",items);
			if(items==itemsThen)return;
			itemsThen=items;
			popup.removeAll();
			for(int i=0;i<items.length;i++)
				if(items[i]==KWidget.BREAK)popup.addSeparator();
				else popup.add((JMenuItem)items[i].wrapped());
		}
	};
	listener.popupMenuWillBecomeVisible(null);
	popup.addPopupMenuListener(listener);
	setSwing(!hints.includeFlag(HINT_USAGE_PANEL)?swing
		:newPanelMenu(d.caption,d.mnemonic,d.rubric,popup));
	}
	static JButton newPanelMenu(final String caption,
			final int mnemonic,final String rubric,final JPopupMenu popup){
		JButton button=new JButton(new AbstractAction(caption,new Icon(){
			@Override
			public void paintIcon(Component c,Graphics g,int x,int y){
				Graphics2D g2=(Graphics2D)g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				    RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setFont(g.getFont().deriveFont(10f));
				g2.setColor(c.isEnabled()?Color.black:Color.LIGHT_GRAY);
				double layoutFactor=KitSwing.layoutFactorSwing();
				g2.drawString(AppConstants.ARROW_RIGHT,x,
						y+(laf==Windows?(int)(11*layoutFactor):13));
			}
			public int getIconWidth(){
				return 4;
			}
			public int getIconHeight(){
				return 16;
			}
		}){
			public void actionPerformed(ActionEvent e){
				JComponent swing=(JComponent)e.getSource();
				popup.show(swing,10,swing.getSize().height);
			}
		});
		button.setHorizontalTextPosition(SwingConstants.LEADING);
		button.setMnemonic(mnemonic);
		if(!rubric.equals(""))button.setToolTipText(rubric);
		KitSwing.adjustComponents(true,button);
		return button;
	}
}