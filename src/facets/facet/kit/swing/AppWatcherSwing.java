package facets.facet.kit.swing;
import static facets.core.app.ActionAppSurface.*;
import static java.awt.Cursor.*;
import static javax.swing.SwingUtilities.*;
import facets.util.app.AppWatcher;
import facets.util.app.WatchableOperation;
import java.awt.AWTEvent;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.Toolkit;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
final class AppWatcherSwing extends AppWatcher{
	private final EventQueue system=Toolkit.getDefaultToolkit().getSystemEventQueue();
	private EventQueue active=system;
	private JDialog dialog;
	private AWTEvent eventThen;
	Container blockParent;
	@Override
	protected boolean checkBusy(){
		if(active==null)return false;
		AWTEvent event=active.peekEvent();
		boolean busy=event!=null&&event==eventThen;
		if(false&&busy)trace(".checkBusy: event=",event);
		eventThen=event;
		invokeLater(new Runnable(){public void run(){}});
		return busy;
	}
	@Override
	protected void openBlockingDialog(WatchableOperation op){
		String[]texts=op.getBlockingCancelTexts();
		JOptionPane pane=new JOptionPane("<html>"+texts[BLOCKING_RUBRIC_1],
				JOptionPane.INFORMATION_MESSAGE);
		pane.setOptions(new Object[]{"Cancel"});
		dialog=pane.createDialog(blockParent,texts[BLOCKING_TITLE_0]);
		dialog.setLocation(blockParent.getBounds().getLocation());
		if(worker==null)return;
		trace(".openBlockingDialog: op=",op);
		dialog.setCursor(getPredefinedCursor(WAIT_CURSOR));
		dialog.setVisible(true);
		trace(".~openBlockingDialog: cancellable=",worker);
	}
	@Override
	protected void cancelBlockingDialog(){
		Runnable cancel=new Runnable(){public void run(){
			if(dialog!=null)dialog.setVisible(false);
		}};
		if(false)cancel.run();
		SwingUtilities.invokeLater(cancel);
	}
	public static AppWatcher getSingle(){
		return single!=null?single:new AppWatcherSwing();
	}
}