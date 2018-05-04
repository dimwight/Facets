package applicable;
import facets.facet.app.FacetAppSurface;
import facets.util.Debug;
import facets.util.Times;
import facets.util.Tracer;
import facets.util.app.WatchableOperation;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Date;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
public abstract class FileModifiedChecker extends Tracer implements ActionListener{
	private final static boolean debug=false;
	@Override
	protected void traceOutput(String msg){
		if(debug)super.traceOutput(msg);
	}
	protected final FacetAppSurface app;
	protected final File file;
	private long sourceModified;
	private Timer fileCheck;
	public FileModifiedChecker(FacetAppSurface app,File file){
		super(FileModifiedChecker.class);
		this.app=app;
		sourceModified=(this.file=file).lastModified();
	}
	final public void startChecking(){
		traceDebug(".startChecking: ",this);
		if(fileCheck!=null)fileCheck.stop();
		if(file.exists())(fileCheck=new Timer(debug?3000:1000,this)).start();
	}
	@Override
	public void actionPerformed(ActionEvent e){
		long modifiedNow=file.lastModified();
		boolean modified=sourceModified!=modifiedNow;
		if(true&&debug)trace(".checkModified: modifiedNow="
				+new Date(modifiedNow).toString().replaceAll(".*(\\d{2}\\:\\d{2}\\:\\d{2}).*","$1"));
		if(!modified)return;
		for(;modifiedNow!=sourceModified;sourceModified=modifiedNow)
			trace(".checkModified: sourceModified=",modifiedNow-sourceModified);
		fileCheck.stop();
		int pause=300;
		if(false)Times.printElapsed("FileModifiedChecker.actionPerformed: pause="+pause);
		(new Timer(pause,new ActionListener(){
			public void actionPerformed(ActionEvent e){
				final WatchableOperation fileChanged=new WatchableOperation(
						FileModifiedChecker.class.getSimpleName()+".fileChanged"){
					@Override
					public void doSimpleOperation(){
						fileChanged();
					}};
				if(app!=null)app.runWatchedLater(fileChanged);
				else SwingUtilities.invokeLater(new Runnable(){public void run(){
					fileChanged.doOperations();
				}});
			}
		}){
			public boolean isRepeats(){
				return false;
			}
		}).start();
	}
	protected abstract void fileChanged();
	final public void stopChecking(){
		traceDebug(".stopChecking: ",this);
		fileCheck.stop();
		fileCheck.removeActionListener(this);
	}
}