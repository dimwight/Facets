package facets.facet.kit.swing;
import static javax.swing.UIManager.*;
import facets.facet.kit.swing.AppMultiMount.DesktopContent;
import facets.facet.kit.swing.KitSwing.LaF;
import facets.util.Tracer;
import facets.util.Util;
import facets.util.app.BusyCursor.BusySettable;
import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import javax.swing.JDesktopPane;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
/**
Manages content windows within an internal desktop.  
<p>{@link AppDesktop} extends {@link JDesktopPane} by defining the 
essential contract of an internal desktop with content added 
by means of {@link DesktopContent}. 
<h4>Design</h4>
<ul>
<li> Content is adjusted by passing new content to {@link #setContents(Iterable)} and 
by calling {@link #setActiveContent(DesktopContent)}. 
<li>Content frames are maintained inside the {@link AppDesktop} independently of 
the {@link JDesktopPane} that displays them. 
<li>Content frame bounds are defined by {@link AppDesktopSizer}.   
<li>{@link FrameSignals} handles user input to frame controls 
(including workaround for maximisation bug). 
</ul>
 */
final class AppDesktop extends AppDesktopCore implements BusySettable{
	final class FrameSignals extends Tracer implements InternalFrameListener,
			PropertyChangeListener,VetoableChangeListener{
		final private AppDesktopFrame frame;
		final private boolean trace;
		private boolean adjustingMaximum;
		FrameSignals(AppDesktopFrame frame,boolean trace){
			this.frame=frame;
			this.trace=trace;
		}
		public void propertyChange(PropertyChangeEvent e){
			String property=e.getPropertyName();
			if(!"maximum".equals(property)||adjustingMaximum)return;
			String msg="Maximum new="+e.getNewValue();
			trace(msg);
			frame.content.windowSetMaximum(framesMaximum=frame.isMaximum());
			if(framesMaximum)frame.setRestoreBounds(frame.getNormalBounds());
			else frame.setBounds(frame.getRestoreBounds());
			adjustMaximumVisibles();
			trace("~"+msg);
		}
		public void internalFrameDeactivated(InternalFrameEvent e){
			trace("Deactivated");
			if(adjustMaximums&&framesMaximum&&!adjustingMaximum)adjustMaximum(false);
		}
		public void internalFrameActivated(InternalFrameEvent e){
			trace("Activated");
			if(adjustMaximums&&framesMaximum&&!adjustingMaximum)adjustMaximum(true);
			frame.content.windowActivated();
		}
		private void adjustMaximum(boolean on){//swingSystem
			adjustingMaximum=true;
			frame.trySetMaximum(on);
			try{
				frame.setSelected(on);
			}catch(PropertyVetoException p){
				throw new RuntimeException(p);
			}
			if(!on)frame.setBounds(frame.getRestoreBounds());
			adjustingMaximum=false;
		}
		public void vetoableChange(PropertyChangeEvent e)throws PropertyVetoException{
			if(!e.getPropertyName().contains("closed"))return;
			trace("Closing?");
			if(!frame.isSelected())frame.trySetSelected();
			if(!frame.content.windowCanClose())throw new PropertyVetoException("",e);
		}
		public void internalFrameIconified(InternalFrameEvent e){
			trace("Iconified");
			if(frame.isMaximum())frame.trySetMaximum(false);
			if(thenSize.iconGap>0)return;
			AppDesktopSizer nowSize=new AppDesktopSizer(thenSize,frame.getIconisedHeight());
			trace("Iconified: thenSize="+thenSize+" nowSize="+nowSize);
			nowSize.adjustFrames(frames,scaleFrames,thenSize);
			thenSize=nowSize;
		}
		public void internalFrameDeiconified(InternalFrameEvent e){
			trace("Deiconified");
		}
		public void internalFrameOpened(InternalFrameEvent e){
			trace("Opened");
		}
		public void internalFrameClosing(InternalFrameEvent e){
			trace("Closing");
		}
		public void internalFrameClosed(InternalFrameEvent e){
			trace("Closed");
		}
		@Override
		protected void traceOutput(String msg){
			if(trace)Util.printOut(msg+": "+frame);
		}
	}
	private final boolean adjustMaximums;
	/**
	Unique constructor. 
	@param background the background colour for the desktop
	@param defaultMaximum should the first window be opened maximised?
	@param scaleToDesktop equivalent to calling {@link #setScaleToDesktop(boolean)}   
	 */
	AppDesktop(Color background,boolean defaultMaximum,boolean scaleToDesktop){
		super(background,defaultMaximum,scaleToDesktop);
		adjustMaximums=true||KitSwing.laf!=LaF.Windows;
	}
	@Override
	protected AppDesktopFrame newFrame(DesktopContent content){
		return new AppDesktopFrame(this,content);
	}
}