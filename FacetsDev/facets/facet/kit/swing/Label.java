package facets.facet.kit.swing;
import static facets.facet.FacetFactory.*;
import facets.facet.kit.Decoration;
import facets.facet.kit.KField;
import facets.facet.kit.KitFacet;
import facets.util.Debug;
import facets.util.StringFlags;
import java.awt.Graphics;
import java.awt.Rectangle;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.border.Border;
final class Label extends Widget implements KField{
	Label(KitFacet facet,Decoration d){
		this(facet,StringFlags.EMPTY);
		redecorate(d);
	}
	Label(KitFacet facet,StringFlags hints){
		super(facet,null);
		final boolean autoclear=hints.includeFlag(HINT_LABEL_AUTOCLEAR);
		debug=hints.includeFlag(HINT_DEBUG);
		JLabel swing=new JLabel(Debug.info(facet)){
			private String thenText;
			public void setText(String text){
				super.setText(text);
				if(autoclear&&thenText==text){
					Graphics g=getGraphics();
					if(g!=null){
						Rectangle b=getBounds();
						g.clearRect(b.x,b.y,b.width,b.height);
						paint(g);
					} 
				}
				thenText=text;
			}
			protected void paintComponent(Graphics g){
				if(debug)trace("..paintComponent: ");
				super.paintComponent(g);
			}
		};
		KitSwing.adjustComponents(false,swing);
		setSwing(swing);
	}
	public void redecorate(Decoration d){
		setText(d.caption);
		if(!d.rubric.equals(""))((JLabel)swing).setToolTipText(d.rubric);
	}
	public void setText(String text){
		if(debug)trace(".setText:" ,Debug.info(this)+" "+text);
		((JLabel)swing).setText(text);
	}
	public String toString(){
		return super.toString()+" "+((JLabel)swing).getText();
	}
	public String text(){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	public double value(){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	public void makeEditable(){
		throw new RuntimeException("Not implemented in "+Debug.info(this));		
	}
	public void setValue(double value){
		throw new RuntimeException("Not implemented in "+Debug.info(this));		
	}
	@Override
	public void requestFocus(){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
}
