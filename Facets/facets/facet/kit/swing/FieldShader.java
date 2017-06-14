package facets.facet.kit.swing;
import facets.facet.kit.KButton;
import facets.facet.kit.KField;
import facets.facet.kit.KitFacet;
import facets.util.Debug;
import facets.util.StringFlags;
import facets.util.Strings;
import facets.util.Util;
import facets.util.shade.Shade;
import facets.util.shade.ShadeSet;
import facets.util.shade.Shades;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.colorchooser.ColorSelectionModel;
import javax.swing.colorchooser.DefaultColorSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

final class FieldShader extends Widget implements KField{
  private final ColorSelectionModel model=new DefaultColorSelectionModel();
  private final JPanel shader=new JColorChooser(model).getChooserPanels()[1],
		previewOld=new JPanel(),previewNew=new JPanel();
  private static final String NO_TITLE="Untitled";
  private final JLabel titleLabel=new JLabel(NO_TITLE);
  private final ShadeSet shades=new ShadeSet(Shades.HTML_SET);
  private final JButton button;
  private final FocusListener focusMonitor=new FocusAdapter(){
    public void focusLost(FocusEvent e){
  	  if(false)traceDebug("FieldColorChooser: ",e.getSource());
    }
  };
  private final MouseListener mouseMonitor=new MouseAdapter(){
    public void mouseClicked(MouseEvent e){e.getComponent().requestFocus();}
  };
  private boolean settingModel;
  private ChangeListener frameMonitor=new ChangeListener(){
    public void stateChanged(ChangeEvent e){
      Color color=model.getSelectedColor();
      previewNew.setBackground(color);
      if(settingModel)return;
      int rgb=0xFFFFFF&color.getRGB();
      Shade snapCheck=new Shade(rgb,"Snap Check"),
      	snapShade=shades.snapShade(snapCheck);
      boolean snap=snapCheck!=snapShade;
      titleLabel.setText(snap?snapShade.title():
        Shade.TITLE_CUSTOM+" "+Strings.hexString(rgb));
      if(!snap)return;
      settingModel=true;
      model.setSelectedColor(new Color(snapShade.rgb()));
      settingModel=false;
    }
  };
	private final static String tagMount="Mount",tagBlanker="Blank";
	private final CardLayout layout=new CardLayout();
  public FieldShader(KitFacet facet,KButton button){
	  super(facet,null);
	  model.addChangeListener(frameMonitor);
	  this.button=(JButton)button.wrapped();
	  GridBagLayout grid=new GridBagLayout(){{
	    rowHeights=new int[]{0,20,20};
	    columnWidths=new int[]{5,100,100,25};
	  }};
	  JPanel mount=new JPanel(grid){};
	  mount.add(shader,new GridBagConstraints(){{
	    gridwidth=5;
	  }});
	  mount.add(previewOld,new GridBagConstraints(){{
	    gridx=1;gridy=1;gridheight=2;fill=BOTH;
	  }});
	  mount.add(previewNew,new GridBagConstraints(){{
	    gridx=2;gridy=1;gridheight=2;fill=BOTH;
	  }});
    mount.add(titleLabel,new GridBagConstraints(){{
	    gridx=4;gridy=1;anchor=NORTHWEST;
	  }});
	  mount.add(this.button,new GridBagConstraints(){{
	    gridx=4;gridy=2;anchor=WEST;
	  }});
	  Component[]components=KitSwing.allComponents(mount);
	  for(int i=0;i<(false?1:components.length);i++){
	    if(false)traceDebug("FieldColorChooser: ",components[i]);
      Component c=components[i];
      c.setBackground(Color.white);
      if(false)c.setFocusable(true);
      c.addFocusListener(focusMonitor);
      c.addMouseListener(mouseMonitor);
    }
		JPanel swing=new JPanel(layout);
		JLabel blanker=new JLabel("Shader is Disabled",SwingConstants.CENTER);
		blanker.setEnabled(false);
  	swing.add(mount,tagMount);
  	swing.add(blanker,tagBlanker);
		layout.show(swing,tagBlanker);
	  setSwing(swing);
	}
  public void setEnabled(boolean enabled){	
		layout.show(swing,enabled?tagMount:tagBlanker);
	}
  public double value(){return model.getSelectedColor().getRGB()&0xFFFFFF;}
  public void setValue(double value){
    Color color=new Color((int)value);
    swing.setVisible(false);
    previewOld.setBackground(color);previewNew.setBackground(color);
    model.setSelectedColor(color);
    swing.setVisible(true);
  }
  public void makeEditable(){}
  public void setText(String text){}
  public String text(){return "Not implemented in "+Debug.info(this);}
	@Override
	public void requestFocus(){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
}
