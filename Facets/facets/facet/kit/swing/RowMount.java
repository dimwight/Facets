package facets.facet.kit.swing;
import static facets.facet.FacetFactory.*;
import static facets.facet.kit.swing.KitSwing.*;
import static java.awt.GridBagConstraints.*;
import static javax.swing.BorderFactory.*;
import facets.core.app.PagedContenter;
import facets.core.superficial.STarget;
import facets.core.superficial.app.SContentAreaTargeter.ContentArea;
import facets.facet.kit.Decoration;
import facets.facet.kit.KWrap;
import facets.facet.kit.KitCore;
import facets.facet.kit.KitFacet;
import facets.util.Debug;
import facets.util.StringFlags;
import facets.util.Util;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
final class RowMount extends MountCore{
	private final boolean debug,gridSplit;
	private final int align,hgap,vgap;
	private KWrap[]lastItems;
	private int nowRow=-1;
	private boolean inLastRow;
	private final JLabel header;
	@Override
	protected void traceOutput(String msg){
		if(false&&debug)super.traceOutput(msg);
	}
	RowMount(KitFacet facet,KitSwing kit,int hgap,int vgap,final StringFlags hints){
	  super(facet,null);
		this.hgap=px(hgap);
		this.vgap=px(vgap);
	  debug=false||hints.includeFlag(HINT_DEBUG);
	  align=hints.includeFlag(HINT_PANEL_RIGHT)?NORTHEAST
	  		:hints.includeFlag(HINT_PANEL_MIDDLE)?WEST
	  		:hints.includeFlag(HINT_PANEL_CENTER)?CENTER
	  		:NORTHWEST;
		gridSplit=hints.includeFlag(HINT_GRID);
		if(gridSplit)throw new RuntimeException("Not implemented in "+Debug.info(this));
		trace(".RowMount: hints="+hints+" ",this);
		JPanel swing=new JPanel(new BorderLayout()){
			public Insets getInsets(){
				int px=px(10);
				return!hints.includeFlag(HINT_PANEL_INSET)
					||(lastItems!=null&&lastItems.length==1)?super.getInsets()
						:new Insets(px-RowMount.this.vgap,px,px,px);
			}
		};
		setSwing(swing);
		KitSwing.adjustComponents(false,swing);
		if(debug)swing.setBackground(Color.red);
		Decoration decoration=kit.decoration(facet.title(),hints);
		String headerCaption=decoration.caption;
		header=!hints.includeFlag(HINT_PANEL_BORDER)?null:new JLabel(headerCaption);
		if(header==null)return;
		Font font=swing.getFont().deriveFont(Font.BOLD);
		if(false){
			TitledBorder border=createTitledBorder(createEmptyBorder());
			border.setTitle(
					!(!hints.includeFlag(HINT_BARE))?"":(" "+headerCaption+" "));
			border.setTitleFont(font);
			border.setTitleColor(Color.DARK_GRAY);
			border.setTitleJustification(TitledBorder.LEADING);
			((JComponent)swing).setBorder(border);
		}
		else{
			header.setFont(font);
			header.setToolTipText(decoration.rubric);
		}
	}
	final private class RowGridLayout extends GridBagLayout{		
		final int rowCount;
		RowGridLayout(KWrap[]items){
			int i=0,rowCount=1;
			while(i<items.length)if(items[i++]==BREAK)rowCount++;
			rowWeights=new double[rowCount];
			for(i=0;i<rowWeights.length;i++)rowWeights[i]=1;
			rowWeights[rowCount-1]=1000;
			columnWeights=gridSplit?new double[]{0,1}:new double[]{1};
			this.rowCount=rowCount;
		}
	}
	private void setNewItems(KWrap[]items){
		swing.removeAll();
		if(header!=null)swing.add(header,BorderLayout.NORTH);
		final RowGridLayout rowGrid=new RowGridLayout(items);
		int rowCount=rowGrid.rowCount;
		JPanel rows=new JPanel(rowGrid),rowMount=null;
		if(debug)rows.setBackground(Color.magenta);
		else if(panelShade!=null)rows.setBackground(new Color(panelShade.rgb()));
		KitSwing.debugSwing(rows,facet,this);
		GridBagConstraints rowConstraints=null;
		swing.add(rows,BorderLayout.CENTER);
		for(int itemAt=0;itemAt<items.length;itemAt++){
			KWrap item=items[itemAt];
			if(item==BREAK){
				if(itemAt==0||itemAt==items.length-1)throw new IllegalStateException(
						"BREAK must not be first or last item:" +Debug.info(item)+
						" in \n"+Debug.arrayInfo(items));
				else rows.add(rowMount,rowConstraints);
			}
			if(itemAt==0||item==BREAK){
				inLastRow=++nowRow==rowCount-1;
				rowMount=new JPanel(rowCount>1&&inLastRow?new BorderLayout()
					:new GridBagLayout());
				rowConstraints=new GridBagConstraints(){{
				  	ipady=vgap;
				  	ipadx=hgap;
						anchor=align;
						if(inLastRow)fill=BOTH;
						gridx=gridSplit?1:0;
						gridy=nowRow;
					}};
				if(debug)rowMount.setBackground(Color.green);
				else if(panelShade!=null)rowMount.setBackground(new Color(panelShade.rgb()));
				KitCore.widgets++;
				KitSwing.debugSwing(rowMount,facet,this);
				if(item==BREAK)continue;
		  }
			traceDebug(".setNewItems: item=",item);
		  JComponent wrapped=(JComponent)swingWrapped(item);
			if(rowCount>1&&inLastRow)rowMount.add(wrapped);
			else if(!gridSplit)rowMount.add(wrapped,new GridBagConstraints(){{
					ipadx=hgap;
					anchor=align;
			  }});
		  else{
		  	Component components[]=wrapped.getComponents(),
		  		first=components.length<2?wrapped:components[0];
				rows.add(first,new GridBagConstraints(){{
			  	anchor=EAST;
			  	gridx=0;
			  	gridy=nowRow;
			  }});
		  	for(Component component:components)
		  		if(component!=first)rowMount.add(wrapped,new GridBagConstraints(){{
				  	anchor=WEST;
				  }});
		  }
		}
		rows.add(rowMount,rowConstraints);
	}
	public void setItems(KWrap... items){
		trace(".setItems: items=",items);
		if(lastItems==null||items.length!=lastItems.length)
			setNewItems(lastItems=items);
		else{
			int i=0;for(;i<items.length&&i<items.length;i++)
				if(items[i]!=lastItems[i])break;
			if(i<items.length)setNewItems(lastItems=items);
		}
	}
}
