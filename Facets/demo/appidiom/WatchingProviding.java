package demo.appidiom;
import static demo.appidiom.WatchingProviding.Caching.*;
import static demo.appidiom.WatchingProviding.Watch.*;
import static java.awt.Cursor.*;
import static javax.swing.JOptionPane.*;
import static javax.swing.SwingUtilities.*;
import facets.util.Bytes;
import facets.util.Objects;
import facets.util.Util;
import facets.util.ItemList.TreeItems;
import facets.util.app.AppValues;
import facets.util.app.AppWatcher;
import facets.util.app.BusyCursor;
import facets.util.app.DirCache;
import facets.util.app.MemoryChecks;
import facets.util.app.ProvidingCache;
import facets.util.app.WatchableOperation;
import facets.util.app.WatcherCoupler;
import facets.util.app.BusyCursor.BusySettable;
import facets.util.app.ProvidingCache.ItemProvider;
import facets.util.tree.ValueNode;
import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import demo.appidiom.AppContent.Tasks;
/**
{@link IdiomPane} for {@link AppWatcher} and {@link ProvidingCache}. 
<p>{@link WatchingProviding}
 */
public final class WatchingProviding extends IdiomPane{
	private static final boolean allTrace=true;
	static final int timeoutSec=9;
	enum Watch{NoWatch,Busy,Cursor,Ops,Cancel;
		boolean includes(Watch i){
			return ordinal()>=i.ordinal();
		}
		static Watch fromArgs(AppValues values){
			ValueNode nature=values.nature();
			for(Watch w:Watch.values()){
				if(nature.getBoolean(w.toString().toLowerCase()))return w;
			}
			return Cancel;
		}
	}
	final Watch watch=Watch.fromArgs(values);
	enum Caching{NoCache,Count,Memory,Disk;
		boolean includes(Caching i){
			return ordinal()>=i.ordinal();
		}
		static Caching fromArgs(AppValues values){
			ValueNode nature=values.nature();
			for(Caching w:Caching.values()){
				if(nature.getBoolean(w.toString().toLowerCase()))return w;
			}
			return Memory;
		}
	}
	final Caching caching=Caching.fromArgs(values);
	final Tasks tasks=watch.includes(Ops)?
			new Tasks("For watch",new int[]{0},new int[]{200,5000,0,-1})
		:new Tasks("For cache",new int[]{5*1000},new int[]{200,400,800,1500,3000});
	final boolean nestWatches=false&&caching!=NoCache;
	final String titleTop=getClass().getSimpleName();
	final AppWatcher watcher;
	final ProvidingCache cache;
	WatchingProviding(String[]args){
		super(args);
		watcher=new AppWatcher(){
			AWTEvent eventThen;
			JDialog dialog;
			@Override
			protected boolean checkBusy(){
				AWTEvent event=Toolkit.getDefaultToolkit().getSystemEventQueue().peekEvent();
				boolean busy=event!=null&&event==eventThen;
				if(!watch.includes(Cursor))trace(titleTop+".checkBusy: busy="+busy);
				eventThen=event;
				invokeLater(new Runnable(){public void run(){}});
				return busy;
			}
			@Override
			protected boolean doTrace(){
				return allTrace;
			}
			@Override
			protected void openBlockingDialog(WatchableOperation op){
				JOptionPane pane=new JOptionPane(op,
						JOptionPane.INFORMATION_MESSAGE);
				pane.setOptions(new Object[]{"Cancel"});
				dialog=pane.createDialog(null,titleTop);
				dialog.setCursor(getPredefinedCursor(WAIT_CURSOR));
				if(worker==null)return;
				dialog.setVisible(true);
			}
			@Override
			protected void cancelBlockingDialog(){
				Runnable cancel=new Runnable(){public void run(){
					if(dialog!=null)dialog.setVisible(false);
				}};
				invokeLater(cancel);
			};
		};
		if(watch.includes(Busy))watcher.setCoupler(new WatcherCoupler(){
			@Override
			protected int systemSec(){
				return timeoutSec;
			}
			@Override
			protected boolean checkTimeouts(){
				return watch.includes(Cancel);
			}
			@Override
			protected void handleException(List<WatchableOperation>ops,Exception e){
				if(!watch.includes(Watch.Ops))throw new IllegalStateException(
						"Unexpected watch="+watch);
				else showMessageDialog(null,e,titleTop,WARNING_MESSAGE);
			}
			@Override
			protected boolean retryCancel(List<WatchableOperation>ops){
				if(!watch.includes(Cancel))throw new IllegalStateException(
						"Unexpected watch="+watch);
				else return showConfirmDialog(null,"Cancel "+ops.get(0)+"?",titleTop,YES_NO_OPTION
						)!=YES_OPTION;
			}
			@Override
			protected void traceOutput(String msg){
				if(false&allTrace)super.traceOutput(msg);
			}
		});
		if(caching.includes(Memory)&&allTrace)values.nature().put(MemoryChecks.ARG_TRACE,true);
		AppWatcher cacheWatcher=watch.includes(Ops)?watcher:null;
		cache=caching==NoCache?null:caching==Count?new ProvidingCache(10,cacheWatcher) {
			@Override
			protected boolean doTrace(){
				return allTrace;
			}
		}
		:new ProvidingCache(new MemoryChecks(values),cacheWatcher){
			@Override
			protected ItemValuer getItemValuer(ItemProvider p, Object[]itemValues){
				return super.getItemValuer(p, itemValues);
			}
			@Override
			protected boolean doTrace(){
				return allTrace;
			}
		};
	}
	@Override
	protected Component newPanel(final Container pane){
		setRubric("<html>"+
				"Watch="+watch+(watch==NoWatch?"":(" TimeoutSec="+timeoutSec))+
				"<p>Caching="+caching+
				"<p>Loads="+tasks.title +
				"<p><p>Press button to build or retrieve test task.");
		if(watch.includes(Cursor))watcher.pushCursor(new BusyCursor(){
			@Override
			protected void addSettables(Set<BusySettable> settables){
				throw new RuntimeException("Not implemented in "+this);
			}
			@Override
			protected void setCursors(boolean busy,Set<BusySettable>settables){
				throw new RuntimeException("Not implemented in "+this);
			}
		});
		final JButton button=new JButton(){
			@Override
			public Dimension getPreferredSize(){
				return new Dimension(300,super.getPreferredSize().height);
			}
			@Override
			public boolean isFocusPainted(){
				return false;
			}
		};
		button.setAction(new AbstractAction(){
			AppContent task=tasks.newTask();
			@Override
			public void actionPerformed(ActionEvent e){
				if(e==null){
					button.setText(task.toString());
					return;
				}
				Object run=watch.includes(Ops)?
					watcher.runWatched(new WatchableOperation(titleTop+":"+task){
						CancelStyle cancel=task.time<0?CancelStyle.Dialog:CancelStyle.Timeout;
						@Override
						public CancelStyle cancelStyle(){
							return nestWatches||caching!=NoCache?CancelStyle.None:cancel;
						}
						@Override
						public Object doReturnableOperation(){
							return!nestWatches?runTask(task)
								:watcher.runWatched(new WatchableOperation(titleTop+".nested:"+task){
									@Override
									public CancelStyle cancelStyle(){
										return caching!=null?CancelStyle.None:cancel;
									}
									@Override
									public Object doReturnableOperation(){
										return runTask(task);
									}
								});
						}
					})
				:runTask(task);
				if(allTrace)Util.printOut(titleTop+": task="+task+" -> run="+run);
				task=tasks.newTask();
				button.setText(task.toString());
			}
		});
		button.getAction().actionPerformed(null);
		JPanel panel=new JPanel();
		panel.add(button);
		return panel;
	}
	private AppContent runTask(final AppContent task){
		return cache!=null?new ItemProvider<AppContent>(cache,this,titleTop+"."+caching){
			final DirCache disk=caching!=Disk?null:new DirCache(new File(AppValues.userDir(),titleTop));
			@Override
			protected String newDiskName(Object[]values){
				return disk==null?null:Objects.toString(values,"-");
			}
			protected AppContent getDiskItem(String diskName){
				return(AppContent)Bytes.unpack((byte[])disk.get(diskName));
			};
			protected void putDiskItem(String diskName,AppContent item){
				disk.put(diskName,Bytes.pack(item));
			};
			@Override
			protected AppContent newItem(){
				return task.build();
			}
			@Override
			protected long buildByteCount(){
				return task.kb*1024;
			};
			@Override
			protected long finalByteCount(AppContent item){
				return buildByteCount();
			}
			@Override
			public CancelStyle cancelStyle(){
				return task.time<0?CancelStyle.Dialog:CancelStyle.Timeout;
			};
			@Override
			protected boolean passThrough(){
				return false;
			}
		}.getForValues(task.kb,task.time)
		:task.build();
	}
	public static void main(final String[]args){
		invokeLater(new Runnable(){public void run(){
			new WatchingProviding(args).buildAndLaunch(false);
		}});
	}
}
