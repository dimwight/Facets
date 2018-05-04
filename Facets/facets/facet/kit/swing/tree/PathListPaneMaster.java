package facets.facet.kit.swing.tree;
import facets.core.app.ListView;
import facets.core.app.PathSelection;
import facets.core.app.SViewer;
import facets.core.app.TreeView;
import facets.core.app.ViewerTarget;
import facets.core.superficial.Notifying.Impact;
import facets.util.Debug;
import facets.util.OffsetPath;
import facets.util.tree.NodePath;
import facets.util.tree.TypedNode;
import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
public class PathListPaneMaster extends PathNodePaneMaster{
  private Object[]itemsThen;
  public void refreshAvatars(Impact impact){
	  PathSelection selection=(PathSelection)viewerTarget().selection();
		OffsetPath path=selection.paths[0];
	  int index=pathSelectionIndex(path);
	  JList list=(JList)avatarPane();
	  if(index>-1)list.setSelectedIndex(index);
		SViewer viewer=viewerTarget();
		Object[]itemsNow=((TreeView)viewer.view()).nodeContents(
				viewer,(TypedNode)selection.content());
	  if(itemsNow==itemsThen)return;itemsThen=itemsNow;
		list.setListData(itemsNow); 
	}
	private int pathSelectionIndex(OffsetPath path){
		boolean debug=false;
		if(debug)trace("NodeViewer " +Debug.info(this)+": ",
				path.members(null));
		TypedNode root=(TypedNode)viewerTarget().selection().content();
		TypedNode[]children=root.children();
		Object indexed=path.target(root);
		if(debug)trace("pathSelectionIndex: ",indexed);
		if(children.length==0)return -1;
		for(int i=0;i<children.length;i++)if(children[i]==indexed)return i;
		return -1;
	}
  protected JComponent newAvatarPane(){
    final JList list=new JList();
    ListView view=((ListView)viewerTarget().view());
		if(view.isHorizontal()){
			list.setLayoutOrientation(JList.VERTICAL_WRAP);
			list.setVisibleRowCount(-1);
		}
    list.setCellRenderer(new DefaultListCellRenderer(){
      public Component getListCellRendererComponent(JList list,Object value,
          int at,boolean selected,boolean cellHasFocus){
        JLabel label=(JLabel)super.getListCellRendererComponent
        			(list,"",at,selected,cellHasFocus);
        PathListPaneMaster.this.modifyNodeValueRendering(value,label);
        return label;
      }
    });
		list.addListSelectionListener(new ListSelectionListener(){
			public void valueChanged(ListSelectionEvent e){
		  	ViewerTarget viewerTarget=PathListPaneMaster.this.viewerTarget();
		    PathSelection selection=(PathSelection)viewerTarget.selection();
				int listIndex=list.getSelectedIndex(),
					rootIndex=pathSelectionIndex(selection.paths[0]);
				if(list.getValueIsAdjusting()
						||rootIndex==listIndex
						||itemsThen==null||listIndex==-1)return;
				Object selected=itemsThen[listIndex];
				OffsetPath path=new NodePath(
						new Object[]{selection.content(),selected});
				viewerTarget.selectionChanged(
						new PathSelection(selection.content(),path));
			}
		});
		list.addKeyListener(new KeyAdapter(){
			public void keyReleased(KeyEvent e){
				char keyChar=e.getKeyChar();
				if(Character.isLetterOrDigit(keyChar))selectNextCharItem(list,keyChar);
			}
		});
    return list;
  }  
	protected void selectNextCharItem(JList list,char keyChar){
		String key=String.valueOf(keyChar);
	  int index=list.getSelectedIndex(),start=index>-1?index+1:0,
			found=-1;
	  if(false)trace("PathsListViewer: "+
	  		"key="+key+", index="+index+", start="+start);
	  for(int i=start;i<itemsThen.length&&found<0;i++){	  	
	  	String title=((TypedNode)itemsThen[i]).title();
			if(title.length()>0&&title.substring(0,1).equalsIgnoreCase(key))
				found=i;
			}
	  if(found<0)for(int i=0;i<start&&found==-1;i++){	  	
	  	String title=((TypedNode)itemsThen[i]).title();
			if(title.length()>0&&title.substring(0,1).equalsIgnoreCase(key))
				found=i;
			}
	  if(found>-1)list.setSelectedIndex(found);
	}
}
