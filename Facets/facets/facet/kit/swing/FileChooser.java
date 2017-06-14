package facets.facet.kit.swing;
import static facets.core.app.AppConstants.*;
import static facets.facet.kit.swing.KitSwing.*;
import static facets.util.app.AppFileValues.*;
import static facets.util.app.AppValues.*;
import static javax.swing.SwingUtilities.*;
import facets.core.app.ActionAppSurface;
import facets.core.app.AppWindowHost;
import facets.core.app.Dialogs;
import facets.core.app.Dialogs.Response;
import facets.util.Debug;
import facets.util.FileSpecifier;
import facets.util.Util;
import facets.util.app.AppFileValues;
import facets.util.app.AppValues;
import facets.util.tree.Nodes;
import facets.util.tree.TypedNode;
import facets.util.tree.ValueNode;
import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JPopupMenu.Separator;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.filechooser.FileFilter;
import sun.swing.FilePane;
import sun.swing.SwingUtilities2;
final class FileChooser extends JFileChooser{
  private final AppFileValues values;
	private final ActionAppSurface app;
	protected FileSpecifier activeSpecifier;
	private JDialog dialog;
	private boolean moved;
	private int returnValue;
	FileChooser(ActionAppSurface app){
		this.app=app;
		values=new AppFileValues(app.spec){};
		if(false)KitSwing.adjustComponents(false,this);
	}
	File getFile(File proposed,FileSpecifier[]specifiers){
		boolean saving=proposed!=null;
		setDialogType(saving?SAVE_DIALOG:OPEN_DIALOG);
		if(!saving)setDialogTitle(app.title()+" - Open File");
		if(saving)setSelectedFile(proposed);
		setCurrentDirectory(values.stateGetPath());
		resetChoosableFileFilters();
		if(specifiers.length>0)setAcceptAllFileFilterUsed(false);
		ValueNode nature=app.spec.nature();
		int specAt=nature.getInt(FileSpecifier.KEY_AT);
		activeSpecifier=specifiers[specAt==ValueNode.NO_INT?specifiers.length-1:specAt];
		for(FileSpecifier specifier:specifiers)
			if(specifier!=activeSpecifier)setSpecifierFilter(specifier);
		setSpecifierFilter(activeSpecifier);
		if(false)rescanCurrentDirectory();
		int returnValue=ERROR_OPTION;
		File selected;
		if(false)Util.printOut("FileChooser.getFile: ",componentTree(this));
		Component[]components=allComponents(this);
		for(final Component c:components){
			if(!(c instanceof FilePane))continue;
			final FilePane pane=(FilePane)c;
			final ValueNode state=values.stateRoot();
			pane.setViewType(state.getOrPutInt(KEY_VIEW,pane.getViewType()));
			pane.addPropertyChangeListener("viewType",new PropertyChangeListener(){
				@Override
				public void propertyChange(PropertyChangeEvent e){
					state.put(KEY_VIEW,pane.getViewType());
				}
			});
			final Action delete=new AbstractAction("Delete"){
				@Override
				public void actionPerformed(ActionEvent e){
					File file=getSelectedFile();
					if(app.dialogs().confirmYesNo("Delete File?","Delete '"+file.getName()+"'?")
							!=Response.Yes)return;
					file.delete();
					setSelectedFile(null);
					rescanCurrentDirectory();
				}
			},
			backup=new AbstractAction("Create Backup"){
				@Override
				public void actionPerformed(ActionEvent e){
					try{
						new Util.FileBackup(getSelectedFile()).doBackup();
						setSelectedFile(null);
						rescanCurrentDirectory();
					}catch(IOException e1){
						throw new RuntimeException(e1);
					}
				}
			};
			JPopupMenu menu=pane.getComponentPopupMenu();
			menu.addPopupMenuListener(new PopupMenuListener(){
				@Override
				public void popupMenuWillBecomeVisible(PopupMenuEvent e){
					File file=getSelectedFile();
					delete.setEnabled(file!=null&&file.canWrite());
					backup.setEnabled(file!=null);
				}
				@Override
				public void popupMenuWillBecomeInvisible(PopupMenuEvent e){}
				@Override
				public void popupMenuCanceled(PopupMenuEvent e){}
			});
			Component[]items=menu.getComponents();
			menu.removeAll();
			for(Component item:items)if(item instanceof JMenuItem){
				JMenuItem button=(JMenuItem)item;
				String caption=button.getText();
				if(caption.contains("Folder"))continue;
				button.setMnemonic(caption.charAt(0));
				menu.add(item);
			}
			menu.add(new Separator());
			menu.add(newMnemonicItem(backup));
			menu.add(newMnemonicItem(delete));
		}
		while(true){
			returnValue=displayDialog((Component)app.host().wrapped());
			selected=getSelectedFile();
			boolean canQuit=saving||returnValue==CANCEL_OPTION;
			canQuit|=selected==null||(selected!=null&&selected.exists());
			if(canQuit)break;
		}
		values.statePutPath(getCurrentDirectory());
		for(int at=0;at<specifiers.length;at++)if(specifiers[at]==activeSpecifier)
			nature.put(FileSpecifier.KEY_AT,at);
		return returnValue!=APPROVE_OPTION?null
				:saving?activeSpecifier.specifiedFile(selected)
				:selected;
	}
	private JMenuItem newMnemonicItem(Action a){
		JMenuItem item=new JMenuItem(a);
		item.setMnemonic(item.getText().charAt(0));
		return item;
	}
	int displayDialog(Component parent){
		boolean parentVisible=parent.isVisible();
		String boundsKey=Dialogs.KEYTOP_BOUNDS+Util.shortTypeNameKey(this);
		ValueNode boundsValues=app.spec.state(STATE_TYPE_DIALOGS);
		int[]intsThen=boundsValues.getInts(boundsKey);
		Rectangle boundsThen=intsThen.length<4?null
				:new Rectangle(intsThen[0],intsThen[1],intsThen[2],intsThen[3]);
		if(dialog==null){
			dialog=createDialog(parentVisible?parent:null);
			dialog.addWindowListener(new WindowAdapter(){
				public void windowClosing(WindowEvent e){
					returnValue=CANCEL_OPTION;
				}
			});
			returnValue=ERROR_OPTION;
			rescanCurrentDirectory();
		}
		Point at=!moved?boundsThen!=null?boundsThen.getLocation()
				:((AppWindowHost)app.host()).newBounds().calculateSmartDialogAt(dialog.getSize())
			:dialog.getLocation();
		if(parentVisible)dialog.setLocation(at);
		dialog.setVisible(true);
		moved|=!dialog.getLocation().equals(at);
		Rectangle bounds=dialog.getBounds();
		int[]intsNow={bounds.x,bounds.y,bounds.width,bounds.height};
		if(parentVisible)boundsValues.put(boundsKey,intsNow);
		return returnValue;
	}
	public void approveSelection(){
		returnValue=APPROVE_OPTION;
		dialog.setVisible(false);
	}
  public void cancelSelection(){
		returnValue=CANCEL_OPTION;
		dialog.setVisible(false);
	}
	private void setSpecifierFilter(final FileSpecifier specifier){
		setFileFilter(new FileFilter(){
			@Override
			public String getDescription(){
				return specifier.rubric;
			}
			@Override
			public boolean accept(File path){
				FileSpecifier active=activeSpecifier=specifier;
				return path.isDirectory()||active==FileSpecifier.ALL
					||active.specifies(path);
			}
		});
	}
}