package facets.facet.kit.swing;
import facets.facet.kit.KMount;
import facets.facet.kit.KWrap;
import facets.facet.kit.KitFacet;
import facets.util.Debug;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Insets;
import javax.swing.JPanel;
abstract class MountCore extends Widget implements KMount{
	MountCore(KitFacet facet,Container swing){
		super(facet,swing);
	}
	@Override
	public void setItem(KWrap item){
		if(swing==null)throw new IllegalStateException("Null swing in "+Debug.info(this));
		else swing.removeAll();
		if(item==null)return;
		swing.add((Component)item.wrapped(),BorderLayout.CENTER);
		swing.validate();
		if(false)trace(".setItem: item=",item.facet());
		if(false)KitSwing.adjustComponents(false,KitSwing.allComponents(swing));
	}
	@Override
	public void setItems(KWrap...items){
		if(items.length>1)throw new IllegalArgumentException(
				"Null items in "+Debug.info(this));
		else setItem(items[0]);
	}
	@Override
	public void setHidden(boolean hidden){
		throw new RuntimeException("Not implemented in "+Debug.info(this));		
	}
	@Override
	public void setActiveItem(KWrap item){}
	@Override 
	public void setEnabled(boolean enabled){}
	static JPanel newSpreadPanel(boolean inset){
		int in=KitSwing.px(10);
		final Insets insets=!inset?null:new Insets(in,in,in,in);
		return new JPanel(new BorderLayout()){
			@Override
			public Insets getInsets(){
				return insets==null?super.getInsets():insets;
			}
		};
	}
}
