package facets.facet.kit.swing;
import static facets.facet.kit.swing.KitSwing.*;
import static facets.util.FileSpecifier.*;
import static facets.util.app.AppFileValues.*;
import facets.core.app.ActionAppSurface;
import facets.core.app.AppWindowHost;
import facets.core.app.Dialogs;
import facets.core.app.Dialogs.Response;
import facets.facet.app.FacetAppSurface;
import facets.facet.app.FileAppActions;
import facets.util.FileSpecifier;
import facets.util.Tracer;
import facets.util.Util;
import facets.util.app.AppFileValues;
import facets.util.tree.ValueNode;
import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JPopupMenu.Separator;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.filechooser.FileFilter;
import sun.swing.FilePane;
final class FileChooser extends JFileChooser{
	private final Tracer t=Tracer.newTopped("FileChooser",false);
	private final ActionAppSurface app;
  private final AppFileValues files;
  private FileSpecifier appSpecs[],activeSpec;
  private File selected;
	private JDialog dialog;
	private boolean moved;
	private int returnValue;
	FileChooser(ActionAppSurface app){
		this.app=app;
		files=((FileAppActions)app.actions).values();
		setDialogTitle(app.title()+" - Files");
		if(false)KitSwing.adjustComponents(false,this);
		addPropertyChangeListener(FILE_FILTER_CHANGED_PROPERTY,
				new PropertyChangeListener(){
			@Override
			public void propertyChange(PropertyChangeEvent e){
				FileFilter filter=getFileFilter();
				if(filter==null||appSpecs==null||selected==null)return;
				String check=filter.getDescription(),name=selected.getName();
				if(name.equals(""))return;
				for(FileSpecifier spec:appSpecs)
					if(spec.rubric.equals(check)){
						setSelectedFile(selected=new File(selected.getParentFile(),
								(activeSpec=spec).newFileName(name.substring(0,name.indexOf(".")))));
					}
			}
		});
	}
	File getFile(final File proposed,FileSpecifier[]specs){
		boolean saving=proposed!=null;
		setDialogType(saving?SAVE_DIALOG:OPEN_DIALOG);
		setCurrentDirectory(!saving?files.stateGetPath():proposed.getParentFile());
		resetChoosableFileFilters();
		appSpecs=((FacetAppSurface)app).getFileSpecifiers();
		if(true)t.trace(".getFile: proposed="+proposed+"\n appSpecs=",appSpecs);
		if(appSpecs.length>0)setAcceptAllFileFilterUsed(false);
		File recent[]=files.recentFiles(),lastOpened=recent.length>0?recent[0]:null;
		FileSpecifier matchSpecs[]=null;
		ValueNode nature=app.spec.nature();
		if(!saving&&(lastOpened==null
				||!lastOpened.getParentFile().equals(getCurrentDirectory())))
			activeSpec=appSpecs[nature.getOrPutInt(KEY_AT,0)];
		else{
			final String ALL="All file types",
					_toFirstDot="^([^.]+)\\.",_toLastDot=".+\\.([^.]+)",
					toMatch=(saving?proposed:lastOpened).getName();
			List<FileSpecifier>matched=new ArrayList();
			for(FileSpecifier spec:appSpecs)
				if(!spec.rubric.equals(ALL)
						&&(toMatch.replaceAll(_toFirstDot,"").matches(spec.extension))
							||toMatch.replaceAll(_toLastDot,"$1").matches(spec.extension))
					activeSpec=spec;
				else matched.add(spec);
			t.trace(".getFile: activeSpec=",activeSpec);
			if(activeSpec==null)activeSpec=matched.remove(0);
			matched.add(activeSpec);
			matchSpecs=matched.toArray(new FileSpecifier[]{});
		}
		for(FileSpecifier spec:matchSpecs!=null?matchSpecs:appSpecs)
			if(spec!=activeSpec)setSpecifierFilter(spec);
		setSpecifierFilter(activeSpec);
		setSelectedFile(selected=saving?proposed:new File(""));
		t.trace(".getFile: ",getSelectedFile());
		if(dialog==null)setUpFilePane();
		if(false)rescanCurrentDirectory();
		int returnValue=ERROR_OPTION;
		while(true){
			returnValue=displayDialog((Component)app.host().wrapped());
			selected=getSelectedFile();
			t.trace(".getFile: selected=",selected);
			if(saving||returnValue==CANCEL_OPTION
					||selected==null||selected.exists())break;
		}
		files.statePutPath(getCurrentDirectory());
		for(int at=0;at<appSpecs.length;at++)
			if(appSpecs[at]==activeSpec)nature.put(KEY_AT,at);
		return returnValue!=APPROVE_OPTION?null
				:saving?activeSpec.specifiedFile(selected)
				:selected;
	}
	private void setSpecifierFilter(final FileSpecifier spec){
		setFileFilter(new FileFilter(){
			@Override
			public String getDescription(){
				return spec.rubric;
			}
			@Override
			public boolean accept(File path){
				return path.isDirectory()||spec==FileSpecifier.ALL
					||spec.specifies(path);
			}
		});
	}
	private void setUpFilePane(){
		if(false)Util.printOut("FileChooser.getFile: ",componentTree(this));
		for(Component c:allComponents(this)){
			if(!(c instanceof FilePane))continue;
			final FilePane pane=(FilePane)c;
			final ValueNode state=files.stateRoot();//FILE_FILTER_CHANGED_PROPERTY
			pane.setViewType(state.getOrPutInt(KEY_VIEW,pane.getViewType()));
			pane.addPropertyChangeListener(KEY_VIEW,new PropertyChangeListener(){
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
}