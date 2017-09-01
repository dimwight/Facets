package facets.facet.kit.swing;
import static facets.core.app.AppConstants.*;
import static facets.core.app.TableView.*;
import static facets.facet.AreaFacets.*;
import static java.awt.Cursor.*;
import static javax.swing.SortOrder.*;
import static javax.swing.SwingConstants.*;
import facets.core.app.AppConstants;
import facets.core.app.PathSelection;
import facets.core.app.TableView;
import facets.core.app.TextView;
import facets.core.app.TextView.LinkText;
import facets.core.app.TextView.LongText;
import facets.core.superficial.SIndexing;
import facets.core.superficial.STarget;
import facets.core.superficial.SToggling;
import facets.core.superficial.Notifying.Impact;
import facets.core.superficial.app.SSelection;
import facets.core.superficial.app.ViewerTarget;
import facets.facet.ViewerAreaMaster;
import facets.facet.kit.Decoration;
import facets.facet.kit.KitCore;
import facets.facet.kit.swing.KitSwing.LaF;
import facets.util.ArrayPath;
import facets.util.Debug;
import facets.util.Stateful;
import facets.util.Times;
import facets.util.Util;
import facets.util.ValueProxy;
import facets.util.app.BusyCursor.BusySettable;
import facets.util.tree.TypedNode;
import facets.util.tree.ValueMap;
import facets.util.tree.ValueNode;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.Format;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
final class TablePaneMaster extends ViewerMaster{
	private static final int KEY_WIDTHS=0,KEY_SORT=1,KEY_DOWN=2;
	private final class SmartTable extends JTable implements BusySettable{
		private static final int NORMAL_WIDTH=2000;
		private final Map<Class,TableCellRenderer>renderers=new HashMap();
		private final PropertyChangeListener colListener;
		SmartTable(TablePaneMaster master){
			super(model);
			setFillsViewportHeight(true);
			setAutoResizeMode(AUTO_RESIZE_SUBSEQUENT_COLUMNS);
			setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			if(false)setCellSelectionEnabled(false);
			if(false)setRowSelectionAllowed(true);
			storeRenderers();
			setRowSorter(sorter=new TableRowSorter(model){
				@Override
				public void toggleSortOrder(int col){
					int sortAt=col;
					final ViewerTarget viewer=viewerTarget();
					if((view().getColumnSort(0,true)!=null)){
						sortDown.set(sortCol.index()==sortAt+1?!sortDown.isSet():false);
						if(true)trace(".toggleSortOrder: sortAt="+sortAt+
								" sortDown="+sortDown.isSet());
						super.toggleSortOrder(col);
						sortCol.setIndex(sortAt+1);
						viewer.notifyParent(Impact.CONTENT);
					}
					else viewer.ensureActive(Impact.ACTIVE);
				}
				@Override
				protected boolean useToString(int column){
					return false;
				}
				@Override
				public void sort(){
					valueCache.clear();
					if(false||!view().sortInContent())super.sort();
				}
				@Override
				public Comparator getComparator(int col){
					Comparator c=view().getColumnSort(col,sortDown.isSet());
					if(false)trace(".getComparator: col="+col+" sortDown="+sortDown.isSet());
					return c!=null?c:super.getComparator(col);
				}
				public int getMaxSortKeys(){
					return 1;
				};
			});
			colListener=new PropertyChangeListener(){
				private int changes;
				public void propertyChange(PropertyChangeEvent e){
					final TableColumnModel cols=getColumnModel();
					final int colCount=cols.getColumnCount();
					JTableHeader header=getTableHeader();
					if(header!=null&&header.getResizingColumn()==null||changes++<colCount)return;
					int nowWidth=getWidth();
					changes=0;
					for(int c=0;c<colCount;c++){
						TableColumn col=cols.getColumn(c);
						widths.put((String)col.getHeaderValue(),
								col.getWidth()*colCount*NORMAL_WIDTH/nowWidth);
					}
					widths.updateNode();
					int sortAt=sortCol.index()-1;
					if(true||sortAt<0)return;
					sortCol.setIndex(0);
					if(view().sortInContent())
						viewerTarget().notifyParent(Impact.DEFAULT);
				}
			};
			if(view().hideHeader())setTableHeader(null);
			else{
				final JTableHeader superHeader=getTableHeader(),
						header=false?superHeader:new JTableHeader(){
					@Override
					public TableColumnModel createDefaultColumnModel(){
						return superHeader.getColumnModel();
					}
					@Override
					public String getToolTipText(MouseEvent e){
						String rubric=base.kit.decoration(getColumnName(columnAtPoint(e.getPoint()))).rubric;
						return rubric.equals("")?null:rubric;
					}
				};
				setTableHeader(header);
				KitSwing.adjustComponents(true,header);
				header.setFont(header.getFont().deriveFont(Font.BOLD));
				header.setForeground(Color.DARK_GRAY);
			  header.setReorderingAllowed(false);
			}
			final TableColumnModel cols=getColumnModel();
			final int colCount=cols.getColumnCount();
			for(int colAt=0;colAt<colCount;colAt++){
				TableColumn col=cols.getColumn(colAt);
				int width=widths.get(col.getHeaderValue())*getWidth()/NORMAL_WIDTH/colCount;
				if(width>0)col.setPreferredWidth(width);
				col.addPropertyChangeListener(colListener);
			}
			addMouseListener(new MouseAdapter(){
				@Override
				public void mouseReleased(MouseEvent e){
					if(e.isPopupTrigger()&&!view().contextClickSelects())return;
					int rowAt=rowAtPoint(e.getPoint());
					ListSelectionModel selection=getSelectionModel();
					if(selection.getLeadSelectionIndex()!=rowAt)
						selection.setSelectionInterval(rowAt,rowAt);
				}
			});
			getSelectionModel().addListSelectionListener(new ListSelectionListener(){
			  private boolean wasSelectionEmpty;
				public void valueChanged(ListSelectionEvent e){
			    if(e.getValueIsAdjusting())return;
			    ListSelectionModel list=(ListSelectionModel)e.getSource();
			    boolean selectionEmpty=list.isSelectionEmpty();
					if(selectionEmpty){
						wasSelectionEmpty=selectionEmpty;
						return;
					}
			  	int tableAt=list.getMinSelectionIndex();
		  		contentAt=sorter.convertRowIndexToModel(tableAt);
		  		if(false)trace(".valueChanged: tableAt="+tableAt+" contentAt="+contentAt);
				  if(tableAt!=thenTableAt||wasSelectionEmpty){
				  	thenTableAt=tableAt;
				  	TableView view=view();
						if(view.canChangeSelection()&&!view.allowSelectOnEdit()){
							viewerTarget().selectionChanged(new PathSelection(
									content,new ArrayPath(content,content[contentAt])));
						}
				  }
			  }
			});
			updateToSelection();
			KitSwing.adjustComponents(false,this);
		}
		@Override
		public TableCellRenderer getDefaultRenderer(Class c){
			return renderers!=null&&renderers.keySet().contains(c)?renderers.get(c)
				:super.getDefaultRenderer(c);
		}
		final ValueMap<Integer>widths=ValueMap.forIntegers(state,stateKeys()[KEY_WIDTHS]);
		@Override
		public void doLayout(){
			JScrollPane scroll=(JScrollPane)getParent().getParent();
			String upperRight=JScrollPane.UPPER_RIGHT_CORNER;
			if(scroll.getCorner(upperRight)==null){
				JPanel corner=new JPanel();
				if(KitSwing.laf==LaF.Windows)
					corner.setBackground(false?Color.red:new Color(249,250,253));
				scroll.setCorner(upperRight,corner);
			}
			int nowWidth=getWidth();
			if(nowWidth==0)return;
			final TableColumnModel cols=getColumnModel();
			final int colCount=cols.getColumnCount();
			for(int colAt=0;colAt<colCount;colAt++){
				TableColumn col=cols.getColumn(colAt);
				col.removePropertyChangeListener(colListener);
				int colWidth=widths.get(col.getHeaderValue())*nowWidth/NORMAL_WIDTH/colCount;
				if(colWidth>0)col.setPreferredWidth(colWidth);
				col.addPropertyChangeListener(colListener);
			}
			super.doLayout();
		}
		private void storeRenderers(){
			final TableCellRenderer superString=new JTable().getDefaultRenderer(String.class);
			final boolean pretty=true;
			if(!pretty)return;
			renderers.put(Boolean.class,new TableCellRenderer(){
		    public Component getTableCellRendererComponent(JTable table,Object value,
		        boolean selected,boolean hasFocus,int row,int col){
		    	JLabel label=(JLabel)superString.getTableCellRendererComponent(table,value,
		    			selected,false,row,col);
					label.setText(((Boolean)value)?HTML_CENTER+HTML_TICK:"");
					label.setHorizontalAlignment(SwingConstants.CENTER);
					if(pretty)label.setToolTipText(null);
		      return label;
		    }
		  });
			renderers.put(LongText.class,new TableCellRenderer(){
		    public Component getTableCellRendererComponent(JTable table,Object value,
		        boolean selected,boolean hasFocus,int row,int col){
		    	JLabel label=(JLabel)superString.getTableCellRendererComponent(table,value,
		    			selected,false,row,col);
					String text=((LongText)value).toString().trim();
					label.setText(text);
					if(pretty&&!text.equals(""))label.setToolTipText(text);
		      return label;
		    }
		  });
			renderers.put(Date.class,new TableCellRenderer(){
		    public Component getTableCellRendererComponent(JTable table,Object value,
		        boolean selected,boolean hasFocus,int row,int col){
		    	JLabel label=(JLabel)superString.getTableCellRendererComponent(table,value,
		    			selected,false,row,col);
					Format format=view().getColumnFormat(col);
					Date date=(Date)value;
					label.setText(date.getTime()<0?"":
							format!=null?format.format(value):(""+date.getTime()));
					if(pretty)label.setToolTipText(null);
		      return label;
		    }
		  });
			renderers.put(String.class,new TableCellRenderer(){
		    public Component getTableCellRendererComponent(JTable table,Object value,
		        boolean selected,boolean hasFocus,int row,int col){
		    	JLabel label=(JLabel)superString.getTableCellRendererComponent(table,value,
		    			selected,false,row,col);
		    	if(pretty)label.setToolTipText(null);
					String text=(String)value;
					if(text.equals(""))return label;
					Decoration d=kit.decoration(text);
					Object icon=d.icon;
					if(icon!=null){
						label.setIcon((Icon)icon);
			    	if(pretty)label.setToolTipText(d.rubric);
					}
					label.setText(icon!=null?"":text);
					label.setHorizontalAlignment(icon!=null?CENTER:LEADING);
		      return label;
		    }
		  });
			renderers.put(LinkText.class,new TableCellRenderer(){
				private int mouseRow,mouseCol;
				private final MouseAdapter mouser=new MouseAdapter(){
					@Override
					public void mouseMoved(MouseEvent e){
						Point point=e.getPoint();
						mouseRow=rowAtPoint(point);
						mouseCol=columnAtPoint(point);
						TextView.LinkText linked=mouseOverLink();
						setCursor(getPredefinedCursor(linked==null||linked.link.equals("")?
								DEFAULT_CURSOR:HAND_CURSOR));
						repaint();
					}
					@Override
					public void mouseDragged(MouseEvent e){
						mouseMoved(e);
					}
					@Override
					public void mouseClicked(MouseEvent e){
						if(e.isPopupTrigger())return;
						TextView.LinkText linked=mouseOverLink();
						if(linked!=null)linked.fireLink();
					}
					private TextView.LinkText mouseOverLink(){
						if(mouseRow<0||mouseCol<0||
								!TextView.LinkText.class.isAssignableFrom(getColumnClass(mouseCol)))
							return null;
						TextView.LinkText linked=(TextView.LinkText)getValueAt(mouseRow,mouseCol);
						return linked.link.equals("")?null:linked;
					}
				};
				{
					addMouseMotionListener(mouser);
					addMouseListener(mouser);
				}
				public Component getTableCellRendererComponent(JTable table,Object value,
				    boolean selected,boolean hasFocus,int row,int col){
					JLabel label=(JLabel)superString.getTableCellRendererComponent(table,value,
							selected,false,row,col);
					label.setHorizontalAlignment(SwingConstants.LEADING);
					TextView.LinkText linked=(TextView.LinkText)value;
					String text=linked.text;
					boolean empty=linked.link.equals(""),active=!empty&&row==mouseRow&&mouseCol==col;
					label.setText(empty?text
							:("<html><u><font color="+(selected?"white":
									active?"gray":linked.visited()?"purple":"blue")+
									">"+text+"</font></u>"));
					if(pretty)label.setToolTipText(active?linked.tooltip():null);
				  return label;
				}
			});
		}
		private boolean isSlave(){
			return view().hideHeader();
		}
		private SmartTable findPairedSlave(){
			if(true)throw new RuntimeException("Not implemented in "+Debug.info(this));
			SmartTable slave=null;
			if(true)return slave;
			for(Component c:KitSwing.allComponents(
					SwingUtilities.getAncestorOfClass(JSplitPane.class,this)))
				if(c!=this&&c instanceof SmartTable
					&&((SmartTable)c).isSlave())slave=(SmartTable)c;
			return slave;
		}
		private void sortToMaster(TablePaneMaster master){
			if(true)throw new RuntimeException("Not implemented in "+Debug.info(this));
			else getRowSorter().toggleSortOrder(master.sortCol.index()-1);
		}
		void updateToSelection(){
			int[]offsets=((PathSelection)viewerTarget().selection()).paths[0].offsets;
		  ListSelectionModel selections=getSelectionModel();
			int nowRow=selections.getMinSelectionIndex();
			final int setRow=offsets.length<2||offsets[1]<0||offsets[1]>model.getRowCount()?-1
					:sorter.convertRowIndexToView(offsets[1]);
			if(setRow<0)selections.clearSelection();
			else if(setRow!=nowRow){
				if(false)trace(".updateToSelection: setRow="+setRow+" nowRow="+nowRow);
				selections.setSelectionInterval(setRow,setRow);
			}
			if(true)SwingUtilities.invokeLater(new Runnable(){public void run(){
				scrollRectToVisible(getCellRect(setRow,0,true));
				if(false)Times.printElapsed("TablePaneMaster.updateToSelection~");
			}});
			invalidate();
			repaint();
		}
	}
	public void refreshAvatars(Impact impact){
		if(impact==Impact.DISPOSE){
			for(ValueProxy p:content){
				Stateful source=p.source;
				if(source instanceof TypedNode)((TypedNode)source).setContents(new Object[]{});
			}
			Debug.memCheck("TablePaneMaster.refreshAvatars: ");
			return;
		}
	  SSelection selection=viewerTarget().selection();
		ValueProxy[]contentNow=(ValueProxy[])selection.content();
		Object viewStateNow=view().stateStamp();
		int sortAt=sortCol.index()-1;
	  boolean changedContent=content.length!=contentNow.length,
	  	changedView=!viewState.equals(viewStateNow),
	  	changed=changedContent||changedView;
		content=contentNow;
		viewState=viewStateNow;
		SmartTable table=(SmartTable)avatarPane();
		if(changed){
			if(false)trace(".refreshAvatars: changedContent="+changedContent+" sortAt="+sortAt+
					"\n\t viewStateNow="+viewStateNow);
			valueCache.clear();
			((AbstractTableModel)table.getModel()).fireTableStructureChanged();
			sorter.modelStructureChanged();
			sorter.setSortKeys(sortAt<0?null:Collections.singletonList(
					new RowSorter.SortKey(sortAt,sortDown.isSet()?DESCENDING:ASCENDING)));
			if(false)table.getSelectionModel().setSelectionInterval(0,0);
		}
		table.updateToSelection();
	}
	@Override
	protected void traceOutput(String msg){
		if(false)Util.printOut(Debug.info(false?base:this)+msg);
	}
	private final TableModel model=new AbstractTableModel(){
    public Class getColumnClass(int col){
    	return getValueAt(0,col).getClass();
    }
    public Object getValueAt(int row,int col){
    	if(true)return content[row].get(col);
    	Integer key=content.length<<16|row<<8|col;
			Object got=valueCache.get(key);
			if(got==null)valueCache.put(key,got=content[row].get(col));
			return got;
		}
		public int getColumnCount(){
    	ValueProxy firstRow=content[0]; 
    	firstRow.get(0);
			return firstRow.valueCount();
    }
    public String getColumnName(int col){
    	return base().kit.getDecorationText(view().getColumnTitle(col),true);
    }
    public int getRowCount(){
    	return content.length;
    }
    public boolean isCellEditable(int row,int col){
    	return view().isColumnEditable(col);
    }
    public void setValueAt(Object value,int row,int col){
    	ValueProxy gotRow=content[row];
			gotRow.put(col,value);
			viewerTarget().selectionEdited(new PathSelection(content,
					new ArrayPath(content,content[contentAt])),new Object[]{gotRow},false);
		}
  };
  private final Map<Integer,Object>valueCache=new HashMap();
	private final SToggling sortDown=new SToggling("SortUp",false,new SToggling.Coupler());
	private final SIndexing sortCol=new SIndexing("SortCol",new SIndexing.Coupler(){
		Integer[]colSorts;
		public Object[]getIndexables(){
			if(colSorts!=null)return colSorts;
			colSorts=new Integer[model.getColumnCount()+1];
			colSorts[0]=-1;
			for(int sort=1;sort<colSorts.length;sort++)colSorts[sort]=sort-1;
			return colSorts;
		};
		public void indexSet(SIndexing i){
			int sortAt=i.index()-1;
			if(sortAt<0)sortDown.set(false);
			if(true)trace(".indexSet: sortAt="+sortAt);
			if(true)return;
			state.put(stateKeys()[KEY_SORT],sortAt);
			state.put(stateKeys()[KEY_DOWN],sortDown.isSet());
		};
	});
	private final ValueNode state;
	private final ViewerAreaMaster vam;
	private final KitCore kit;
  private ValueProxy[]content;
	private Object viewState=0;
	private TableRowSorter sorter;
	private int thenTableAt=-1,contentAt;
	TablePaneMaster(ValueNode state,ViewerAreaMaster vam,KitCore kit){
		this.state=state;
		this.vam=vam;
		this.kit=kit;
	}
	protected JComponent newAvatarPane(){
		content=(ValueProxy[])viewerTarget().selection().content();
	  return new SmartTable(this);
	}
	@Override
	protected void disposeAvatarPane(){}
	@Override
	protected STarget[]targets(){
		if(false)throw new RuntimeException("Debug");
		return new STarget[]{sortCol,sortDown};
	}
	private TableView view(){
		return(TableView)viewerTarget().view();
	}
	private String[]stateKeys(){
		String viewKey=view().typeKey(),tailKey=viewKey!=null?viewKey:vam.typeKey();
		return new String[]{TABLE_KEY_COLUMNS+tailKey,
				TABLE_KEY_SORT_COL+tailKey,TABLE_KEY_SORT_UP+tailKey};
	}
}
