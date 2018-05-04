package facets.facet.kit.swing;
import static java.awt.RenderingHints.*;
import facets.core.app.AppConstants;
import facets.core.superficial.STrigger;
import facets.facet.FacetFactory.TriggerCodeCoupler;
import facets.facet.kit.Decoration;
import facets.facet.kit.KButton;
import facets.facet.kit.KitFacet;
import facets.util.Debug;
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
import javax.swing.JButton;
import javax.swing.JPanel;
final class DropdownButton_ extends Widget implements KButton{
	private static final int ARROW_X=14;
	private final KitSwing kit;
	private final JButton button;
	DropdownButton_(final KitFacet facet,final ActionListener listener,KitSwing kit){
		super(facet,new JPanel(new BorderLayout()));
		this.kit=kit;
		final Decoration d=kit.decoration(facet.title());
		button=new JButton(){
			private boolean dropLive;
			{
				setText(d.caption+" ");
				setMnemonic(d.mnemonic);
				addMouseMotionListener(new MouseMotionAdapter(){
					@Override
					public void mouseMoved(MouseEvent e){
						dropLive=e.getPoint().x>getSize().width-ARROW_X+1;
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
						if(dropLive)listener.actionPerformed(e);
						else facet.targetNotify(message(),false);
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
				Point arrowAt=new Point(box.width-ARROW_X,box.height*71/100);
				if(dropLive)d.setColor(Color.gray);
				d.drawString(AppConstants.ARROW_DOWN,box.x+arrowAt.x,box.y+arrowAt.y);
			}
		};
		KitSwing.adjustComponents(false,button);
		swing.add(button);
	}
	private void updateActiveCode(String code){
		Decoration d=kit.decoration(code);
		redecorate(d);
		((TriggerCodeCoupler)((STrigger)facet.target()).coupler).setActiveCode(code);
	}
	@Override
	public void redecorate(Decoration d){
		button.setToolTipText(d.caption);
		button.setActionCommand(d.keyText);
	}
	@Override
	public void setMessage(String msg){
		button.setActionCommand(msg);
	}
	@Override
	public String message(){
		String msg=button.getActionCommand();
		if(msg==null)throw new IllegalStateException("Null msg in "+Debug.info(this));
		else return msg;
	}
	@Override
	public boolean isSelected(){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	@Override
	public void setSelected(boolean selected){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
}