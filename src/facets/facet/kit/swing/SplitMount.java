package facets.facet.kit.swing;
import static facets.util.Util.*;
import facets.core.superficial.SNumeric;
import facets.facet.kit.KWrap;
import facets.facet.kit.KitFacet;
import facets.util.Debug;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JSplitPane;
final class SplitMount extends MountCore{
  private final boolean wide;
  private static final int SPLIT_COMPONENT_COUNT=3,DIVIDER_SIZE=3;
  SplitMount(KitFacet facet,final boolean wide,final SNumeric ratio){
		super(facet,null);
		this.wide=wide;
    JSplitPane swing=new JSplitPane(wide?JSplitPane.HORIZONTAL_SPLIT
    		:JSplitPane.VERTICAL_SPLIT,false){
  		Dimension thenSize;
      private int thenComponents;
  		public void doLayout(){
  			setBorder(false?BorderFactory.createLineBorder(Color.magenta):null);
  			int components=getComponentCount();
  			boolean split=components==SPLIT_COMPONENT_COUNT, 
  				newSplit=split&&thenComponents<components;
			  setDividerSize(split?DIVIDER_SIZE:0);
  			super.doLayout();
  			thenComponents=components;
  			Dimension nowSize=getSize();
    		double thenRatio=ratio.value()/100,
    			pixels=wide?nowSize.width:nowSize.height,    		  
    		  nowRatio=pixels==0?thenRatio:(getDividerLocation()/pixels);
        if(false){
        	if(false)Debug.printStackTrace(5);
					trace(": newSplit="+newSplit+" thenRatio="+fx(thenRatio)
							+" nowRatio="+fx(nowRatio));
				}
        if(thenSize==null||!nowSize.equals(thenSize)||newSplit){
  			  setDividerLocation((int)Math.rint(thenRatio*pixels));
  			  super.doLayout();
  			  thenSize=nowSize;
  		  }
  			else if(split)ratio.setValue(nowRatio*100);
  		}
  	};
		if(false)swing.setBackground(Color.red);
  	swing.setDividerSize(DIVIDER_SIZE);
  	setSwing(swing);
  }
	public void setItems(KWrap...items){
		if(items==null||items[0]==null
				||(items.length==2&&items[1]==null))throw new IllegalArgumentException(
				"Null items in "+Debug.info(this));
		JSplitPane pane=(JSplitPane)swing;
		if(pane.getComponentCount()>1)throw new IllegalStateException(
				"Items not replaceable in "+Debug.info(this));
		Container first=swingWrapped(items[0]),second=items.length<2?null
				:swingWrapped(items[1]);
		if(wide){
			pane.setLeftComponent(first);
			pane.setRightComponent(second);
		}else{
			pane.setTopComponent(first);
			pane.setBottomComponent(second);
		}
	}
}