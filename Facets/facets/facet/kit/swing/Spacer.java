package facets.facet.kit.swing;
import facets.facet.kit.KitFacet;

import java.awt.FlowLayout;
import java.awt.Insets;

import javax.swing.JPanel;
class Spacer extends Widget{
	Spacer(KitFacet facet,final int width,final int height){
    super(facet,new JPanel(new FlowLayout(0,0,0)){
			public Insets getInsets(){
				return new Insets(height,width,0,0);
			}
  	});
  }
	static class Filler extends Spacer{
		Filler(KitFacet facet){
			super(facet,0,0);
		}
	}
}
