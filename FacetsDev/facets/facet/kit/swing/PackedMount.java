package facets.facet.kit.swing;
import static facets.facet.FacetFactory.*;
import facets.facet.kit.KWrap;
import facets.facet.kit.KitFacet;
import facets.util.Debug;
import facets.util.StringFlags;
import facets.util.Util;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
final class PackedMount extends MountCore{
	private static final boolean debug=false;
	private final StringFlags hints;
	PackedMount(KitFacet facet,final StringFlags hints){
		super(facet,new JPanel());
		trace("PackedMount: ",facet.title());
		this.hints=hints;
		swing.setLayout(new GridBagLayout() {{
			rowWeights=new double[]{0,0,0,1};
			columnWeights=new double[]{1};
		}});
		JComponent fill=new JPanel(null);
		if(false)fill.setBorder(BorderFactory.createLineBorder(Color.RED));
		swing.add(fill, new GridBagConstraints(){{
			anchor=NORTHWEST;
			fill=BOTH;
			gridy=3;
		}});
		if(debug)((JComponent)swing).setBorder(BorderFactory.createLineBorder(Color.RED));
	}
	public void setItem(KWrap item){
		JComponent add=(JComponent)item.wrapped();
		if(debug)add.setBorder(BorderFactory.createLineBorder(Color.yellow));
		swing.add(add, new GridBagConstraints(){{
			anchor=NORTHWEST;
			fill=BOTH;
			gridy=1;
		}});
	}
	public void setItems(KWrap... items){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	public void setViewerTools(KWrap tools,final StringFlags hints){
		if(true)throw new RuntimeException("Debug");
		JComponent panel=new JPanel();
		KitSwing.addToolGroups(panel,(JComponent)tools.wrapped());
		swing.add(panel,new GridBagConstraints(){{
			anchor=NORTHWEST;
			fill=BOTH;
			gridx=1;
			gridy=hints.includeFlag(HINT_PANEL_BELOW)?2:0;
		}});
	}
}