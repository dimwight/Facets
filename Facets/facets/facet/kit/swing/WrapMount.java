package facets.facet.kit.swing;
import static facets.facet.FacetFactory.*;
import static facets.facet.kit.swing.KitSwing.*;
import static java.lang.Math.*;
import facets.core.app.SurfaceStyle;
import facets.facet.FacetFactory;
import facets.facet.SwingPanelFacet;
import facets.facet.kit.*;
import facets.util.Debug;
import facets.util.StringFlags;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
final class WrapMount extends Widget{
	private final JLabel label;
	private final boolean formUsage;
  WrapMount(KitFacet facet,Decoration decorations,KWrap[]wraps,int hgap,int vgap){  	  	
		super(facet,null);
		StringFlags hints=decorations.hints;
		debug=hints.includeFlag(HINT_DEBUG);
		final boolean 
			grid=hints.includeFlag(HINT_GRID),
			square=hints.includeFlag(HINT_SQUARE),
			spread=hints.includeFlag(HINT_SPREAD),
			headed=hints.includeFlag(HINT_HEADED),
			wide=!spread&&!hints.includeFlag(HINT_TALL),
			bare=!spread&&hints.includeFlag(HINT_BARE),
			nimbus=false&&KitSwing.laf==LaF.Nimbus;
		if(spread&&wraps.length>1)throw new IllegalArgumentException(
				"Cannot spread multiple wraps "+Debug.info(this));
		hgap=px(hgap+(nimbus?8:0)+(headed?5:0));
		vgap=px(vgap+(nimbus?3:0)+(headed?5:0));
		formUsage=hints.includeFlag(HINT_USAGE_FORM);
	  int rows=wide?1:wraps.length,cols=wide?wraps.length:1;
	  if(square)switch(wraps.length){
		  case 3:case 4:rows=2;cols=2;break;
		  case 5:case 6:rows=3;cols=2;break;
	  }
	  JPanel packed=spread?null
	  		:new JPanel(wide&&!grid?new FlowLayout(FlowLayout.LEFT,hgap,vgap)
	  		:new GridLayout(rows,cols,hgap,debug?5:vgap));
	  if(packed!=null){
	  	for(int i=0;i<wraps.length;i++)
	  		if(wraps[i]==null)throw new IllegalStateException(
						"Null wrap in "+Debug.info(this));
	  		else packed.add(swingWrapped(wraps[i]));
	  	if(panelShade!=null)packed.setBackground(new Color(panelShade.rgb()));
	  }
		if(bare){
	  	label=null;
	  	setSwing(packed);
	  	if(debug)swing.setBackground(Color.red);
	  	return;
	  }
  	swingMap.put(SwingPanelFacet.KEY_LABELLED,packed);
	  JComponent labelled=new JPanel(wide?new FlowLayout(FlowLayout.LEFT,0,0)
		    :new BorderLayout(0,spread?vgap:headed?vgap/2:0));
	  if(panelShade!=null)labelled.setBackground(new Color(panelShade.rgb()));
	  label=(JLabel)new Label(facet,decorations).wrapped();
	  label.setBackground(Color.red);
		if(FacetFactory.surfaceStyle==SurfaceStyle.DESKTOP&&headed)
			label.setDisplayedMnemonic(decorations.mnemonic);
		label.setLabelFor(swingWrapped(wraps[0]));
	  if(headed)label.setFont(label.getFont().deriveFont(Font.BOLD));
	  if(spread){
		  labelled.add(label,BorderLayout.NORTH);
		  labelled.add(swingWrapped(wraps[0]),BorderLayout.CENTER);
	  }
	  else if(wide){
	  	labelled.add(label);
	  	if(false)labelled.add(Box.createHorizontalStrut(3));
	  	labelled.add(packed);
	  }
	  else{
		  JPanel spaced=new JPanel(new FlowLayout(FlowLayout.LEFT,0,0))
			  {public Insets getInsets(){return new Insets(0,5,0,0);}},
			  spacer=new JPanel(new FlowLayout(FlowLayout.LEFT,0,0))
			  {public Insets getInsets(){return new Insets(0,0,3,0);}};
		  if(panelShade!=null)spaced.setBackground(new Color(panelShade.rgb()));
		  spaced.add(packed);
		  KitCore.widgets++;
		  spacer.setBackground(spaced.getBackground());
		  labelled.add(label,BorderLayout.NORTH);
		  if(false||wraps.length==1)labelled.add(spacer,BorderLayout.CENTER);
		  labelled.add(spaced,BorderLayout.SOUTH);
	  }
	  setSwing(labelled);
  	swingMap.put(SwingPanelFacet.KEY_LABEL,label);
	  KitCore.widgets++;
  	if(debug)swing.setBackground(Color.red);
	}
	public void setEnabled(boolean enabled){
		swing.setEnabled(enabled);
		if(label!=null&&!formUsage)label.setEnabled(enabled);
	}
}
