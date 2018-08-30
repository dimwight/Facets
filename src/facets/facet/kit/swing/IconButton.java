package facets.facet.kit.swing;
import facets.facet.kit.swing.KitSwing.LaF;
import facets.util.Debug;
import facets.util.Tracer;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.LookAndFeel;
import javax.swing.Painter;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.ButtonUI;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.synth.SynthButtonUI;
import javax.swing.plaf.synth.SynthConstants;
import javax.swing.plaf.synth.SynthContext;
final class IconButton extends JButton{
	private static final Tracer t=Tracer.newTopped("IconButton",true);
	private final static boolean inNimbus=KitSwing.laf==LaF.Nimbus,useNimbus=true&&inNimbus;
	private static UIDefaults nimbusUi;
	public void setUI(ButtonUI ui){
		super.setUI(useNimbus?false?new SynthButtonUI():ui:new BasicButtonUI(){
			@Override
			protected int getTextShiftOffset(){
				return 1;
			}
		});
	}
	IconButton(){
		setFocusable(false);
		if(!useNimbus)return;
		if(nimbusUi==null){
			final UIDefaults laf=UIManager.getLookAndFeelDefaults();
			nimbusUi=new UIDefaults();
			nimbusUi.put("Button.contentMargins",new Insets(2,1,2,1));
			for(final String state:new String[]{
					"Disabled","Enabled",
					"Default+MouseOver","Default+Pressed",
					"MouseOver","Pressed",
			}){
				String key="Button["+state+"].backgroundPainter";
				final Painter master=(Painter)laf.get(key);
				nimbusUi.put(key,new Painter<JButton>(){
					@Override
					public void paint(Graphics2D g,JButton b,int width,int height){
						Graphics2D g2=(Graphics2D)g.create();
						if(true&&"Disabled,Enabled".contains(state)){
							if(true)return;
							if(false)g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
									RenderingHints.VALUE_ANTIALIAS_ON);
							g.setColor(Color.gray);
							width-=1;height-=1;
							if(state.equals("Enabled")){
								g.setColor(Color.LIGHT_GRAY.brighter());
								g.fillRoundRect(0,0,width,height,6,6);
								g.setColor(Color.GRAY);
								g.drawRoundRect(0,0,width,height,6,6);
							}
							else g.drawRect(0,0,width,height);
						}
						else{
							g2.translate(-2,-2);
							width+=4;height+=4;
							master.paint(g2,b,width,height);
						}
					}
				});
			}
		}
		putClientProperty("Nimbus.Overrides.InheritDefaults",true);
		putClientProperty("Nimbus.Overrides",nimbusUi);
	}
	public Insets getMargin(){
		return useNimbus?null:new Insets(1,0,0,0);
	}
}