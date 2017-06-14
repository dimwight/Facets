package _doc;
import static javax.swing.SwingUtilities.*;
import static javax.swing.UIManager.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.Timer;
import javax.swing.UIManager;
public class InternalFrameSignals extends JFrame implements PropertyChangeListener{
	private final static int xOffset=30,yOffset=30;
	private final class SmartFrame extends JInternalFrame{
		public String toString(){
			return " ["+getTitle()+" maximum="+isMaximum
				+" selected="+isSelected()+" size="+getSize().width+"]";
		}
		SmartFrame(int number){
			super("Frame #"+number,true,true,true,true);
			setSize(300,300);
			setLocation(xOffset*number,yOffset*number);
			setVisible(true);
		}
		void trySetMaximum(boolean on){
			trace("trySetMaximum");
			try{
				setMaximum(on);
			}catch(PropertyVetoException e){
				throw new RuntimeException(e);
			}
			trace("~trySetMaximum");
		}
		void trySetIcon(boolean on){
			trace("trySetIcon");
			try{
				setIcon(on);
			}catch(PropertyVetoException e){
				throw new RuntimeException(e);
			}
			trace("~trySetIcon");
		}
		void trySetSelected(){
			trace("trySetSelected");
			try{
				setSelected(true);
			}catch(PropertyVetoException e){
				throw new RuntimeException(e);
			}
			trace("~trySetSelected");
		}
		private void trace(String msg){
			Util.printOut(msg+this);
		}
	}
	private final JDesktopPane desktop=new JDesktopPane();
	private final SmartFrame[]frames=new SmartFrame[2];
	private final boolean smartLaf;
	private boolean framesMaximise;
	@Override
	public void propertyChange(PropertyChangeEvent e){
		String property=e.getPropertyName();
		if(!"selected|maximum".contains(property))return;
		boolean now=(Boolean)e.getNewValue();
		SmartFrame source=(SmartFrame)e.getSource();
		Util.printOut(property+" "+e.getOldValue()+">"+now+source);
		if(true||smartLaf)return;
		if(property.equals("selected")){
			if(framesMaximise&&!source.isMaximum())try{
					source.setMaximum(true);
				}catch(PropertyVetoException e1){
					throw new RuntimeException(e1);
				}
		}
		else{
			framesMaximise=property.equals("maximum")&&now;
			if(!framesMaximise)try{
				for(JInternalFrame frame:frames)
					if(frame!=source&&frame.isMaximum())
						frame.setMaximum(false);
				if(!property.equals("icon"))
					((JInternalFrame)source).setSelected(true);
			}catch(PropertyVetoException v){
				throw new RuntimeException(v);
			}
		}
	}
	private InternalFrameSignals(){
		super(InternalFrameSignals.class.getSimpleName());
		smartLaf=getLookAndFeel().getName()=="Windows";
		setBounds(0,0,500,500);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setContentPane(desktop);
		for(int f=0;f<frames.length;f++){
			SmartFrame frame=new SmartFrame(f+1);
			frame.addPropertyChangeListener(this);
			desktop.add(frames[f]=frame);
		}
		JMenuBar menus=new JMenuBar();
		setJMenuBar(menus);
		JMenu windows=new JMenu("Window");
		menus.add(windows);
		ButtonGroup group=new ButtonGroup();
		for(final SmartFrame frame:frames){
			final JMenuItem window=new JRadioButtonMenuItem(
					new AbstractAction(frame.getTitle()){
				@Override
				public void actionPerformed(ActionEvent e){
					frame.trySetSelected();
				}
			});
			frame.addVetoableChangeListener(new VetoableChangeListener(){
				@Override
				public void vetoableChange(PropertyChangeEvent evt)
						throws PropertyVetoException{
					window.setSelected(!frame.isSelected());
				}
			});
			windows.add(window);
			group.add(window);
		}
		int sec=0;
		invokeLaterWait(new Runnable(){public void run(){
			frames[1].trySetSelected();
		}},sec++);
		invokeLaterWait(new Runnable(){public void run(){
			frames[1].trySetMaximum(true);
		}},sec-1);
		invokeLaterWait(new Runnable(){public void run(){
			frames[0].trySetSelected();
		}},sec++);
		if(true)return;
		invokeLaterWait(new Runnable(){public void run(){
			frames[1].trySetIcon(true);
		}},sec++);
		invokeLaterWait(new Runnable(){public void run(){
			frames[0].trySetMaximum(false);
		}},sec++);
	}
	private void invokeLaterWait(final Runnable r,final int sec){
		new Timer(sec*1000,new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				Util.printOut("invokeLaterWait sec="+sec);
				r.run();
			}
		}){public boolean isRepeats(){return false;}
		}.start();
	}
	private final static String[]plafs={
		UIManager.getSystemLookAndFeelClassName(),
		"com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel",
		"javax.swing.plaf.metal.MetalLookAndFeel",
		"com.sun.java.swing.plaf.motif.MotifLookAndFeel",
	};
	public static void main(final String[]args){
		if(args.length<1)throw new IllegalArgumentException(
				"Supply test number 1 to 3");
		invokeLater(new Runnable(){public void run(){
			int test=Integer.valueOf(args[0]);
			try{
				setLookAndFeel(plafs[--test]);
			}catch(Exception notSet){
				notSet.printStackTrace();
			}
			new InternalFrameSignals().setVisible(true);
		}});
	}
}
