package facets.facet.kit.swing;
import static facets.facet.FacetFactory.*;
import facets.facet.SwingPanelFacet;
import facets.facet.kit.*;
import facets.util.StringFlags;
import facets.util.Times;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;
final class ListDropdown extends Widget implements KList{
	private boolean updating,indeterminate,allowInput;
	private final boolean formUsage;
	private final Color formInputColor,formNoInputColor;
	ListDropdown(KitCore kit,KitFacet facet,final boolean asCombo,StringFlags hints){
		super(facet,null);
		formUsage=hints.includeFlag(HINT_USAGE_FORM);
		final JComboBox box=new JComboBox(){
		  private Dimension fixedSize;
	    public Dimension getPreferredSize(){
	    	if(true)return super.getPreferredSize();
	      if(fixedSize==null){
			    setEditable(asCombo);
			    fixedSize=super.getPreferredSize();
			    setEditable(false);
	      }
	      return fixedSize;
	    }
		};
		JTextField text=new JFormattedTextField();
		formInputColor=text.getBackground();
		text.setEditable(false);
		formNoInputColor=text.getBackground();
		if(formUsage)box.setFont(text.getFont());
		else KitSwing.adjustComponents(false,box);
		if(false)box.setRenderer(true?new DefaultListCellRenderer(){
			public Component getListCellRendererComponent(JList list,Object value,
					int index,boolean isSelected,boolean cellHasFocus){
				JLabel c=(JLabel)super.getListCellRendererComponent(list,value,index,
						isSelected,cellHasFocus);
				if(!allowInput&&!isSelected)c.setBackground(formNoInputColor);
				return c;
			}
		}
		:new DefaultListCellRenderer(){
			public Component getListCellRendererComponent(JList list,Object value,
					int index,boolean isSelected,boolean cellHasFocus){
				JTextField field=new JTextField();
				field.setBorder(null);
				field.setText(value.toString());
				if(!allowInput&&!isSelected)field.setBackground(formNoInputColor);
				return field;
			}
		});
		box.addActionListener(this);
		box.setFocusable(!hints.includeFlag(HINT_NO_FOCUS));
  	swingMap.put(SwingPanelFacet.KEY_LABELLED,box);
  	box.setToolTipText(kit.decoration(facet.title(),hints).rubric);
		setSwing(box);
		if(!asCombo)return;
		final JTextField field=(JTextField)box.getEditor().getEditorComponent();
		field.addKeyListener(new KeyAdapter(){
	    public void keyPressed(KeyEvent e){
	      if(e.getKeyCode()==KeyEvent.VK_ESCAPE)
	        field.setText((String)box.getSelectedItem());
	      else super.keyPressed(e);
	    }
	  });
		field.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				facet().targetNotify(field.getText(),false);}
		});
	}
	public void actionPerformed(ActionEvent e){
		if(updating)return;
		super.actionPerformed(e);
	}
	public void setEnabled(boolean enabled){
		if(!formUsage){super.setEnabled(enabled);return;}
		allowInput=enabled;
		swing.setBackground(allowInput?formInputColor:formNoInputColor);
		swing.repaint();
	}
	public void setTitles(String[]titles){
		JComboBox box=(JComboBox)swing;
		int itemCount=box.getItemCount();
		if(titles.length==itemCount){
			boolean sameItems=true;
			for(int i=0;i<titles.length;i++)
				sameItems&=titles[i]==box.getItemAt(i);
			if(sameItems)return;
		}
		updating=true;
		box.setModel(new DefaultComboBoxModel(titles){
			public void setSelectedItem(Object item){
				if(!updating&&formUsage&&!allowInput)return;
				super.setSelectedItem(item);
			}
		});		
		updating=false;
	}
	public void setIndices(int[]indices){
		JComboBox box=((JComboBox)swing);
		updating=true;
	  box.setSelectedIndex(indices[0]);
		updating=false;
	}
	public int[]indices(){
		int boxIndex=((JComboBox)swing).getSelectedIndex();
		return new int[]{boxIndex-(indeterminate?1:0)};
	}
	public void setIndeterminate(boolean on){
		if(indeterminate==on)return;indeterminate=on;
		JComboBox box=(JComboBox)swing;
		updating=true;
		if(on){
			box.insertItemAt("",0);
			box.setSelectedIndex(0);
		}
		else {
			if(box.getItemAt(0).equals(""))box.removeItemAt(0);
			box.setSelectedIndex(box.getSelectedIndex()-1);
		}
		updating=false;
	}
  public void setIndex(int index,boolean titleEditable){
    setIndices(new int[]{index});
  	JComboBox box=((JComboBox)swing);
  	if(box.isEditable()!=titleEditable)box.setEditable(titleEditable);
  	if(titleEditable)box.getEditor().selectAll();
  }
}
