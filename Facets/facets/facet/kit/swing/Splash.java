package facets.facet.kit.swing;
import facets.util.Util;
import facets.util.app.Events;
import facets.util.app.Events.EventTracer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JWindow;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
class Splash{
	private final JProgressBar progressBar=new JProgressBar();
	private Timer timer;
	private final Events.EventTracer buildMonitor=new Events.EventTracer(){
		private final int delay=200;
		protected void setBuildEstimate(final int estimate){
			(timer=new Timer(delay,new ActionListener(){
				public void actionPerformed(ActionEvent e){
					int then=progressBar.getValue(),now=then+delay;
					progressBar.setValue(now);
				}
			})).start();
			SwingUtilities.invokeLater(new Runnable(){public void run(){
				progressBar.setMaximum(estimate*5/6);
			}});
		}
		protected void update(final String msg,final int elapsed){
			SwingUtilities.invokeLater(new Runnable(){public void run(){
				message.setText(msg);
				if(false)progressBar.setValue(elapsed+delay);
			}});
		}
	};
	private final JFrame frame=new JFrame();
	private final Dimension screenSize=frame.getToolkit().getScreenSize(),
		splashSize=new Dimension(screenSize.width/3,screenSize.height/10);
	private final JWindow splash=new JWindow(frame);
	private final JLabel message=new JLabel("  ");
	Splash(String text,Object icon){
		Events.setBuildMonitor(buildMonitor);
		splash.setSize(splashSize);
		splash.setLocation(screenSize.width/2-splashSize.width/2,
				screenSize.height/2-splashSize.height/2);
		JLabel headline=new JLabel(text,SwingConstants.CENTER);
		KitSwing.adjustComponents(false,headline,message);
		headline.setIcon((Icon)icon);
		headline.setIconTextGap(20);
		headline.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createRaisedBevelBorder(),
				BorderFactory.createRaisedBevelBorder())
			);
		Container contentPane=splash.getContentPane();
		contentPane.add(progressBar,BorderLayout.NORTH);
		contentPane.add(headline,BorderLayout.CENTER);
		contentPane.add(message,BorderLayout.SOUTH);
	}
	void up(){
		splash.setCursor(new Cursor(Cursor.WAIT_CURSOR));
		splash.setVisible(true);
	}
	void down(){
		SwingUtilities.invokeLater(new Runnable(){public void run(){
			timer.stop();
			splash.setVisible(false);
			frame.dispose();
		}});
	}
}
