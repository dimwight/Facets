package facets.facet.kit.swing;
import facets.core.app.MenuFacets;
import facets.core.app.SurfaceServices;
import facets.core.superficial.SFacet;
import facets.core.superficial.app.ViewerTarget;
import facets.facet.FacetFactory;
import facets.facet.kit.*;
import facets.util.Debug;
import facets.util.ItemList;
import facets.util.Objects;
import facets.util.Util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
final class ListPaneTable extends Widget implements KList{
	private static final int COL_CHECK=0,COL_TITLE=1;
	private boolean ignoreActionEvents,indeterminate,
	selectAllEnabled,deselectAllEnabled;
	private final JCheckBox selectAll,deselectAll;
	private int editRow;
	private String[]titles={"Dummy"};
	private boolean[]flags=new boolean[titles.length];
	private final JTable pane=new JTable(new AbstractTableModel(){
		public int getColumnCount(){
			return 2;
		}
		public int getRowCount(){
			return titles.length;
		}
		public Object getValueAt(int row,int col){
			return col==0?flags[row]:titles[row].trim();
		}
		public Class<?> getColumnClass(int col){
			return col==0?Boolean.class:String.class;
		}
		public boolean isCellEditable(int row,int col){
			return col==0||editRow>=0&&row==editRow;
		}
		public void setValueAt(Object value,int row,int col){
			if(col==COL_CHECK)flags[row]=(Boolean)value;
			else titles[row]=(String)value;
			ListPaneTable.this.selectionChanged();
		}
	}){{
			KitSwing.adjustComponents(false,this);
			TableColumnModel cols=getColumnModel();
			TableColumn colCheck=cols.getColumn(COL_CHECK),colTitle=cols.getColumn(COL_TITLE);
			colCheck.setMinWidth(25);
			colTitle.setPreferredWidth(20000);
			if(false)setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
			setTableHeader(null);
			setFillsViewportHeight(true);
			setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			String spacebarKey="Spacebar";
			getInputMap().put(KeyStroke.getKeyStroke((char)32),spacebarKey);
			AbstractAction spaceBar=new AbstractAction(){
				public void actionPerformed(ActionEvent e){
					if(pane.getSelectedColumn()==COL_CHECK)return;
					int row=pane.getSelectedRow();
					flags[row]=!flags[row];
					ListPaneTable.this.selectionChanged();
				}
			};
			getActionMap().put(spacebarKey,spaceBar);
		 	if(false)addMouseListener(popup);
		}
		final Color enabledColor=getBackground(),disabledColor=new JPanel().getBackground();
		public void setEnabled(boolean enabled){
			super.setEnabled(enabled);
			setBackground(enabled?enabledColor:disabledColor);
		}
	};
	private final MouseListener popup=new MouseAdapter(){
		final JPopupMenu popup=new JPopupMenu();
		public void mousePressed(MouseEvent e){
			if(e.isPopupTrigger())popupRequested(e.getX(),e.getY());
			else super.mousePressed(e);
		}
	  public void mouseReleased(MouseEvent e){
			if(e.isPopupTrigger())popupRequested(e.getX(),e.getY());
		}
		final void popupRequested(int atX,int atY){
			final int row=pane.rowAtPoint(new Point(atX,atY));
			if(pane.getSelectionModel().getAnchorSelectionIndex()!=row)return;
			popup.removeAll();
			popup.add(new AbstractAction("Edit '"+titles[row]+"'"){
				public void actionPerformed(ActionEvent e){
					editRow=row;
					pane.editCellAt(row,COL_TITLE);
					editRow=-1;
				}
			});
			popup.show(pane,atX,atY);		
		}
	};
	private boolean updating;
	ListPaneTable(KitFacet facet,KitCore kit,int width,int rows){
		super(facet,null);
		JPanel panel=new JPanel(new BorderLayout());
		if(FacetFactory.panelShade!=null)
			panel.setBackground(new Color(FacetFactory.panelShade.rgb()));
		setSwing(panel);
		JComponent scroll=new JScrollPane(pane,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll.setPreferredSize(new Dimension(width,KitSwing.px(rows*14)));
		panel.add(scroll,BorderLayout.CENTER);
		if(false){
			selectAll=deselectAll=null;
			return;
		}
		JPanel selects=new JPanel(new FlowLayout(FlowLayout.LEADING));
		selectAll=new JCheckBox(kit.getDecorationText("Select All",true));
		deselectAll=new JCheckBox(kit.getDecorationText("Deselect All",true));
		selects.add(selectAll);selects.add(deselectAll);
		panel.add(selects,BorderLayout.SOUTH);
		selectAll.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if(!selectAll.isSelected())return;
				setAllFlags(true);
				selectionChanged();
			}
		});
		deselectAll.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if(!deselectAll.isSelected())return;
				setAllFlags(false);
				selectionChanged();
			}
		});
		KitCore.widgets+=4;
	}
	public void setIndices(int[]indices){
		if(false)trace(".setIndices: ",Objects.toString(indices));
		for(int i=0;i<flags.length;i++)flags[i]=false;
		for(Integer index:indices)flags[index]=true;
		updating=true;
		selectionChanged();
	}
	private void selectionChanged(){
		pane.repaint();
		if(false)trace(".selectionChanged: ");
		if(selectAll!=null){
			int selectedCount=indices().length;
			if(false)trace(".selectionChanged: ",selectedCount);
			boolean allSelected=selectedCount==pane.getModel().getRowCount(),
				noneSelected=selectedCount==0,
				paneEnabled=pane.isEnabled();
			selectAll.setSelected(allSelected);
			selectAll.setEnabled(selectAllEnabled=!allSelected&&paneEnabled);
			deselectAll.setSelected(noneSelected);
			deselectAll.setEnabled(deselectAllEnabled=!noneSelected&&paneEnabled);
		}
		if(!updating)actionPerformed(null);
		updating=false;
	}
	public void setTitles(String[]titles){
		this.titles=titles;
		flags=new boolean[titles.length];
	}
	public int[]indices(){
		List<Integer>list=new ArrayList();
		for(int i=0;i<flags.length;i++)if(flags[i])list.add(i);
		int[]indices=new int[list.size()];
		for(int i=0;i<indices.length;i++)indices[i]=list.get(i);
		if(false)trace(".indices: ",Objects.toString(indices));
		return indices;
	}
	public void setEnabled(boolean enabled){
		super.setEnabled(enabled);
		pane.setEnabled(enabled);
		if(selectAll==null)return;
		selectAll.setEnabled(enabled&selectAllEnabled);
		deselectAll.setEnabled(enabled&deselectAllEnabled);
	}
	private void setAllFlags(boolean on){
		for(int i=0;i<flags.length;i++)flags[i]=on;
	}
	public void setIndex(int index,boolean titleEditable){
	  throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	public void setIndeterminate(boolean on){
	  if(on)throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
}
