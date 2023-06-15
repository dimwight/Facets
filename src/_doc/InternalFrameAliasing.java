package _doc;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.Icon;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
public class InternalFrameAliasing extends JFrame{
	public InternalFrameAliasing(final int test){
		super(InternalFrameAliasing.class.getSimpleName());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(test*50+100,test*75+5,250,100);
		JDesktopPane desktop=new JDesktopPane();
		setContentPane(desktop);
		final JInternalFrame frame=new JInternalFrame("Am I anti-aliased? [" +
			lafNames[test].replaceAll("[^A-Z]+(.+)LookAndFeel","$1")+"]");
		frame.setSize(300,300);
		frame.add(new JLabel("And am I?"));
		frame.setVisible(true);
		if(false)frame.setFrameIcon(new Icon(){
			@Override
			public void paintIcon(Component c,Graphics g,int x,int y){
				Graphics2D g2=(Graphics2D)g;
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				    RenderingHints.VALUE_ANTIALIAS_ON);
				if(test==1)g.setFont(g.getFont().deriveFont(16f));
				g.setFont(g.getFont().deriveFont(Font.BOLD));
				g.drawString("WA",x,y+14);
				g.drawRect(x,y,getIconWidth(),getIconHeight());
			}
			@Override
			public int getIconWidth(){
				return 24;
			}
			@Override
			public int getIconHeight(){
				return 16;
			}
		});
		desktop.add(frame);
		if(false)SwingUtilities.invokeLater(new Runnable(){public void run(){
			try{
				frame.setSelected(true);
				frame.setMaximum(true);
			}catch(java.beans.PropertyVetoException e){
				e.printStackTrace();
			}
		}});
	}
	private final static String[]lafNames= {
			"javax.swing.plaf.metal.MetalLookAndFeel",
			UIManager.getSystemLookAndFeelClassName(),
			"com.sun.java.swing.plaf.motif.MotifLookAndFeel",
			"com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel"				
	};
	public static void main(final String[]args){
		if(args.length<1)throw new IllegalArgumentException(
				"Supply test number 1 to 4");
		SwingUtilities.invokeLater(new Runnable(){public void run(){
			int test=Integer.valueOf(args[0]);
			try{
				UIManager.setLookAndFeel(lafNames[--test]);
			}catch(Exception notSet){
				notSet.printStackTrace();
			}
			new InternalFrameAliasing(test).setVisible(true);
		}});
	}
}
