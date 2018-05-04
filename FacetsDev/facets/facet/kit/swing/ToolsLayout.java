package facets.facet.kit.swing;
import facets.util.Debug;
import facets.util.Util;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Insets;
import javax.swing.JComponent;
import javax.swing.border.Border;
final class ToolsLayout extends BorderLayout{
	private Component tools,content;
	private final Object toolsAt;
	public ToolsLayout(Object toolsAt){
		this.toolsAt=toolsAt;
	}
  public void addLayoutComponent(String at,Component item){
		synchronized(item.getTreeLock()){
		if(NORTH.equals(at)||WEST.equals(at)){
			if(toolsAt==NORTH||toolsAt==WEST)tools=item;
			else if(toolsAt==EAST||toolsAt==SOUTH)content=item;
			else throw new IllegalArgumentException("Bad at in "+Debug.info(this));
		}
		else if(SOUTH.equals(at)||EAST.equals(at)){
			if(toolsAt==SOUTH||toolsAt==EAST)tools=item;
			else if(toolsAt==NORTH||toolsAt==WEST)content=item;
			else throw new IllegalArgumentException("Bad at in "+Debug.info(this));
		}
  }}
	public void layoutContainer(Container target){
			synchronized(target.getTreeLock()){
		if(tools==null||content==null)return;
		Insets insets=target.getInsets();
		int vgap=false?5:0,hgap=vgap,
			top=insets.top,bottom=target.getHeight()-insets.bottom,
			left=insets.left,right=target.getWidth()-insets.right,
			height=bottom-top;			
    tools.setSize(right-left,tools.getHeight());
    tools.doLayout();
		Insets toolsInsets=((JComponent)tools).getInsets();
    Component toolItems[]=((Container)tools).getComponents(),
  		lastTool=toolItems.length==0?null:toolItems[toolItems.length-1];
    int toolsHeight=lastTool==null?tools.getPreferredSize().height:
  		lastTool.getY()+lastTool.getHeight(),
  		toolsWidth=tools.getPreferredSize().width;
 		if(toolsAt==NORTH){
      int maxNorth=top+toolsHeight+toolsInsets.bottom,
				southMin=bottom-content.getMinimumSize().height,
				heightNorth=Math.min(maxNorth,southMin-vgap);
      tools.setBounds(left,top,right-left,heightNorth);
      top+=heightNorth;
      content.setBounds(left,top,right-left,bottom-top);
    }
		else if(toolsAt==SOUTH){
      int minSouth=toolsHeight,
	      minNorth=content.getMinimumSize().height,
	      heightSouth=Math.min(minSouth,bottom-minNorth-vgap),
	      heightNorth=bottom-heightSouth-vgap;
      content.setBounds(left,top,right-left,heightNorth);
      top+=heightNorth;
      tools.setBounds(left,top,right-left,heightSouth);
    }
		else if(toolsAt==WEST){
      int maxWest=left+toolsWidth,
				eastMin=right-content.getMinimumSize().width,
				widthWest=Math.min(maxWest,eastMin-vgap);
      tools.setBounds(left,top,widthWest,height);
      left+=widthWest+hgap;
      content.setBounds(left,top,right-left,height);
    }
		else if(toolsAt==EAST){
      int minEast=toolsWidth,
	      minWest=content.getMinimumSize().width,
	      widthEast=Math.min(minEast,right-minWest-hgap),
	      widthWest=right-widthEast-hgap;
      content.setBounds(left,top,widthWest,height);
      left+=widthWest+hgap;
      tools.setBounds(left,top,widthEast,height);
    }
		else throw new IllegalArgumentException("Bad toolsAt in "+Debug.info(this));
 }}
}
