package facets.facet.kit.swing;
import facets.facet.FacetFactory;
import facets.facet.kit.*;
import facets.util.Debug;
import facets.util.Util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
final class ListPane extends Widget implements KList,ListSelectionListener{
	private boolean ignoreActionEvents,indeterminate,
		selectAllEnabled,deselectAllEnabled;
	private final JList pane;
	private final JCheckBox selectAll,deselectAll;
	ListPane(KitFacet facet,KitCore kit,int width,int rows,boolean multiSelection){
		super(facet,null);
		final boolean spread=width<=0;
		final JScrollPane scroll=new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll.setViewportView(pane=new JList(new DefaultListModel()){
			public boolean getScrollableTracksViewportWidth(){
				return !spread?false:getPreferredSize().width<scroll.getViewport().getSize().width;
			};
		});
		KitSwing.adjustComponents(false,pane);
		JPanel swing=new JPanel(new BorderLayout());
		if(false)swing.setBorder(BorderFactory.createLineBorder(Color.blue));
		if(FacetFactory.panelShade!=null)
			swing.setBackground(new Color(FacetFactory.panelShade.rgb()));
		setSwing(swing);
		swing.add(scroll,BorderLayout.CENTER);
		if(!spread){
			pane.setFixedCellWidth(width);
			pane.setVisibleRowCount(rows);
		}
		pane.addListSelectionListener(this);
		pane.setSelectionMode(multiSelection?
				ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
				:ListSelectionModel.SINGLE_SELECTION);
		if(!multiSelection){
			selectAll=deselectAll=null;
			return;
		}
		JPanel selects=new JPanel(new FlowLayout(FlowLayout.LEADING));
		selectAll=new JCheckBox(kit.getDecorationText("Select All",true));
		deselectAll=new JCheckBox(kit.getDecorationText("Deselect All",true));
		selects.add(selectAll);selects.add(deselectAll);
		swing.add(selects,BorderLayout.SOUTH);
		selectAll.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if(selectAll.isSelected())pane.setSelectionInterval(0,
						pane.getModel().getSize()-1);
			}
		});
		deselectAll.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if(deselectAll.isSelected())pane.getSelectionModel().clearSelection();
			}
		});
		KitCore.widgets+=4;
	}
	public void valueChanged(ListSelectionEvent e) {
		if(pane.getValueIsAdjusting())return;
		if(selectAll!=null){
			if(false)trace(".valueChanged: ");
			int selectedCount=pane.getSelectedIndices().length;
			boolean allSelected=selectedCount==pane.getModel().getSize(),
				noneSelected=selectedCount==0,paneEnabled=pane.isEnabled();
			selectAll.setSelected(allSelected);
			selectAll.setEnabled(selectAllEnabled=!allSelected&&paneEnabled);
			deselectAll.setSelected(noneSelected);
			deselectAll.setEnabled(deselectAllEnabled=!noneSelected&&paneEnabled);
		}
		actionPerformed(null);
	}
	public void actionPerformed(ActionEvent e){
		if(ignoreActionEvents)return;
		super.actionPerformed(e);
	}
	public void setEnabled(boolean enabled){
		super.setEnabled(enabled);
		pane.setEnabled(enabled);
		if(selectAll==null)return;
		if(false) {
			trace(".setEnabled: ",enabled);
			trace("selectAllEnabled=",selectAllEnabled+
					", enabled&selectAllEnabled="+(enabled&selectAllEnabled));
		}
		selectAll.setEnabled(enabled&selectAllEnabled);
		deselectAll.setEnabled(enabled&deselectAllEnabled);
	}
	public int[]indices(){
		int[]indices=pane.getSelectedIndices();
		if(indeterminate)
			for(int i=0;i<indices.length;i++)indices[i]--;
		return indices;
	}
	public void setIndeterminate(boolean on){
		if(indeterminate==on)return;indeterminate=on;
		setEnabled(!on);
	}
	public void setTitles(String[]titles){
		DefaultListModel list=(DefaultListModel) pane.getModel();
		int itemCount=list.getSize();
		if(titles.length==itemCount){
			boolean sameItems=true;
			for(int i=0;i<titles.length;i++)
				sameItems&=titles[i]==list.getElementAt(i);
			if(sameItems)return;
		}
		ignoreActionEvents=true;
		if(pane.getSelectedIndex()>-1)list.removeAllElements();
		for(int i=0;i<titles.length;i++)list.addElement(titles[i].trim());		
		ignoreActionEvents=false;
	}
	public void setIndices(int[]indices){
		ignoreActionEvents=true;
	  pane.setSelectedIndices(indices);
		ignoreActionEvents=false;
	}
  public void setIndex(int index,boolean titleEditable){
    throw new RuntimeException("Not implemented in "+Debug.info(this));
  }
}
