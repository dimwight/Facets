package facets.facet.kit.swing;
import static facets.facet.kit.swing.KitSwing.*;
import static java.awt.event.KeyEvent.*;
import facets.facet.FacetFactory.SuggestionsCoupler;
import facets.facet.kit.swing.KitSwing.LaF;
import facets.util.Debug;
import facets.util.Tracer;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.event.ListDataListener;
final class FieldTextSuggester extends Tracer{
	private final class ListItems extends ArrayList<String>implements ListModel{
		SuggestionsCoupler src;
		void updateToField(){
			if(src==null)return;
			clear();
			String text=field.getText();
			if(false)trace(".updateToField: active="+paneActivated+" text=",text);
			Collection<String>suggestions=src.suggestions();
			if(text.equals("")){
				if(!paneActivated)addAll(suggestions);
			}
			else for(String item:suggestions)
				if(item.startsWith(text))add(item);
			if(size()==1&&get(0).equals(text))clear();
			boolean cleanNoSelection=true;
			if(cleanNoSelection)add("cleanNoSelection");
			list.setModel(this);
			int lastAt=size()-1;
			list.setSelectedIndex(lastAt);
			if(cleanNoSelection)remove(lastAt);
			if(false){
				list.setVisibleRowCount(size());
				pane.pack();
			}
			list.repaint();
		}
		void removeCurrentItem(){
			if(src==null)return;
			int removeAt=list.getSelectedIndex();
			if(removeAt>getSize()-1)return;
			String remove=get(removeAt);
			src.updateSuggestions(remove,true);
			remove(remove);
			if(isEmpty())deactivatePane();
			else{
				list.setModel(this);
				list.setSelectedIndex(removeAt);
				list.repaint();
			}
		}
		@Override
		public int getSize(){
			return size();
		}
		@Override
		public String getElementAt(int at){
			return get(at);
		}
		@Override
		public void addListDataListener(ListDataListener listener){}
		@Override
		public void removeListDataListener(ListDataListener l){}
	}
	private final JPopupMenu pane=new JPopupMenu();
	private final JList list=new JList();
	private final JTextField field;
	private final ListItems items;
	private boolean paneActivated;
	@Override
	protected void traceOutput(String msg){
		if(false)super.traceOutput(msg);
	}
	FieldTextSuggester(final JTextField field){
		this.field=field;
		list.setModel(items=new ListItems());
		Font font=list.getFont(),setFont=true?font:font.deriveFont(font.getSize2D()*.99f);
		list.setFont(setFont);
		if(true||setFont!=font)list.setFixedCellHeight(setFont.getSize()*12/10);
		list.addMouseMotionListener(new MouseMotionAdapter(){
			@Override
			public void mouseMoved(MouseEvent e){
				list.setSelectedIndex(list.locationToIndex(e.getPoint()));
			}
		});
		list.addMouseListener(new MouseAdapter(){
			@Override
			public void mousePressed(MouseEvent e){
				if(!list.isSelectionEmpty())field.setText((String)list.getSelectedValue());
				deactivatePane();
			}
		});
		list.addKeyListener(new KeyAdapter(){
			@Override
			public void keyPressed(KeyEvent e){
				if(e.isActionKey())return;
				char ch=e.getKeyChar();
				int code=e.getKeyCode();
				String text=field.getText();
				int chars=text.length();
				if(!Character.isISOControl(ch))
					field.setText(text+Character.toString(ch));
				else if(code==VK_DELETE&&(true||chars==0)){
					if(!list.isSelectionEmpty())items.removeCurrentItem();
					return;
				}
				else if(false&&code==VK_DELETE)
					throw new RuntimeException("Not implemented in "+Debug.info(this));
				else if(code==VK_BACK_SPACE&&chars>0)
					field.setText(text.substring(0,chars-1));
				else if(code==VK_ENTER&&!list.isSelectionEmpty()){
					field.setText((String)list.getSelectedValue());
					deactivatePane();
				}
				items.updateToField();
				if(items.size()==0)deactivatePane();
			}
		});
		JScrollPane scroll=new JScrollPane();
		scroll.setViewportView(list);
		pane.add(scroll);
		field.addKeyListener(new KeyAdapter(){
	    public void keyPressed(KeyEvent e){
	    	int code=e.getKeyCode();
				if(code==KeyEvent.VK_DOWN||code==KeyEvent.VK_UP)tryActivatePane();
	    }
	  });
		field.addMouseListener(new MouseAdapter(){
			@Override
			public void mousePressed(MouseEvent e){
				if(field.isFocusOwner())tryActivatePane();
			}
		});
	}
	private void tryActivatePane(){
		if(paneActivated==true)deactivatePane();
		if(false)trace(".tryActivatePane: match=",pane.isVisible()==paneActivated);
		items.updateToField();
		if(items.size()==0)return;
		list.setFixedCellWidth(field.getWidth()-field.getInsets().right);
		if(laf==LaF.Windows)adjustComponents(true,pane);
		else setNimbusSmaller(pane);
		pane.show(field,5,field.getHeight());
		list.requestFocus();
		paneActivated=true;
	}
	private void deactivatePane(){
		paneActivated=false;
		if(pane.isVisible())pane.setVisible(false);
		if(false)trace(".deactivatePane: match=",pane.isVisible()==paneActivated);
		items.updateToField();
	}
	void setSource(SuggestionsCoupler src){
		items.src=src;
	}
}
