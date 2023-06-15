package _doc;
import java.awt.GridLayout;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
public class TooltipOutline extends JFrame{
	public TooltipOutline(final int test){
		super("[" +lafNames[test].replaceAll("[^A-Z]+(.+)LookAndFeel","$1")+"] "
				+TooltipOutline.class.getSimpleName());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(test*50+100,test*75+5,250,200);
		setLayout(new GridLayout(1,2));
		JCheckBox enabled=new JCheckBox("Check me?"),
			disabled=new JCheckBox("Can't check me!");
		disabled.setEnabled(false);
		enabled.setToolTipText("I'm outlined!");
		disabled.setToolTipText(test!=3?"So am I!":"But I'm not and look buggy!");
		add(enabled);
		add(disabled);
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
			new TooltipOutline(test).setVisible(true);
		}});
	}
}
