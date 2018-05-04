package facets.facet;
import static java.awt.RenderingHints.*;
import facets.core.app.AppConstants;
import facets.core.superficial.STarget;
import facets.core.superficial.STrigger;
import facets.core.superficial.Notifying.Impact;
import facets.facet.FacetFactory.TriggerCodeCoupler;
import facets.facet.kit.Decoration;
import facets.facet.kit.KWrap;
import facets.facet.kit.KitCore;
import facets.facet.kit.KitFacet;
import facets.facet.kit.swing.KitSwing;
import facets.util.Debug;
import facets.util.StringFlags;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.AffineTransform;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
final class TriggerCodeButtonSwing extends SimpleMaster{
	private final KitCore kit;
	private JButton button;
	TriggerCodeButtonSwing(KitCore kit){
		super(StringFlags.EMPTY);
		this.kit=kit;
	}
	@Override
	KWrap lazyBaseWrap(){
		button=new JButton(){
			private boolean dropLive;
			{
				Decoration d=kit.decoration(core.title());
				setText(d.caption+" ");
				setMnemonic(d.mnemonic);
				addMouseMotionListener(new MouseMotionAdapter(){
					@Override
					public void mouseMoved(MouseEvent e){
						dropLive=e.getPoint().x>getSize().width*2/3;
						repaint();
					}
				});
				addMouseListener(new MouseAdapter(){
					@Override
					public void mouseExited(MouseEvent e){
						dropLive=false;
						repaint();
					}
				});
				addActionListener(new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent e){
						if(dropLive)openDropdown();
						else core().targetNotify(getActionCommand(),false);
					}
				});
			}
			@Override
			public void paint(Graphics g){
				super.paint(g);
				Graphics2D d=(Graphics2D)g.create();
				d.setRenderingHint(KEY_ANTIALIASING,VALUE_ANTIALIAS_ON);
				double scale=0.6;
				d.setFont(getFont().deriveFont(AffineTransform.getScaleInstance(scale,scale)));
				Rectangle box=getBounds();
				Point dropBoxAt=new Point(box.width*72/100,box.height*71/100);
				if(dropLive)d.setColor(Color.gray);
				d.drawString(AppConstants.ARROW_DOWN,box.x+dropBoxAt.x,box.y+dropBoxAt.y);
			}
		};
		updateActiveCode(getTriggerCodes()[0]);
		final JPanel swing=new JPanel(new BorderLayout());
		swing.add(button);
		KitSwing.adjustComponents(false,swing);
		return new KWrap(){
			@Override
			public Object wrapped(){
				return swing;
			}
			@Override
			public Object newWrapped(Object parent){
				throw new RuntimeException("Not implemented in "+Debug.info(this));
			}
			@Override
			public KitFacet facet(){
				throw new RuntimeException("Not implemented in "+Debug.info(this));
			}
		};
	}
	@Override
	protected void notifyingSingle(STarget target,Object msg){
		((STrigger)core.target).fire();
	}
	private void openDropdown(){
		JPopupMenu menu=new JPopupMenu();
		for(final String code:getTriggerCodes()){
			Decoration d=kit.decoration(code);
			final String keyText=d.keyText;
			JMenuItem item=new JMenuItem(new AbstractAction(code){
				@Override
				public void actionPerformed(ActionEvent e){
					String command=e.getActionCommand();
					if(!keyText.equals(command))throw new IllegalStateException(
							"Mismatched texts: keyText="+keyText+" command="+command+
							" in "+Debug.info(this));
					else if(false)trace(".actionPerformed: keyText="+keyText+" command="+command);
					updateActiveCode(code);
					core().targetNotify(button.getActionCommand(),false);
				}
			});
			item.setText(d.caption);
			item.setActionCommand(keyText);
			item.setMnemonic(d.mnemonic);
			menu.add(item);
		}
		KitSwing.adjustComponents(true,menu);
		menu.show(button,10,button.getSize().height);
	}
	private void updateActiveCode(String code){
		Decoration d=kit.decoration(code);
		button.setToolTipText(d.caption);
		button.setActionCommand(d.keyText);
		coupler().setActiveCode(code);
	}
	private String[]getTriggerCodes(){
		return coupler().codes();
	}
	private TriggerCodeCoupler coupler(){
		return (TriggerCodeCoupler)((STrigger)core.target()).coupler;
	}
	@Override
	public void retargetedMultiple(STarget[]targets,Impact impact){
		if(button!=null)updateActiveCode(coupler().activeCode());
	}
	@Override
	KWrap[]lazyPartWraps(){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
}