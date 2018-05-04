package facets.facet.kit.swing;
import static java.lang.Math.*;
import static javax.swing.SwingUtilities.*;
import facets.facet.kit.swing.AppMultiMount.DesktopContent;
import facets.util.Debug;
import facets.util.Tracer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyBoundsAdapter;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JRootPane;
import javax.swing.Timer;
import javax.swing.plaf.DesktopPaneUI;
import javax.swing.plaf.basic.BasicDesktopPaneUI;
abstract class AppDesktopCore extends JDesktopPane{
	@Override
	public void setUI(DesktopPaneUI ui){
		super.setUI(false?new BasicDesktopPaneUI():ui);
	}
	final protected Tracer t=Tracer.newTopped(getClass().getSimpleName().replace("App",""),
			AppDesktopFrame.trace);
	final protected List<AppDesktopFrame>frames=new ArrayList();
	protected AppDesktopSizer thenSize=new AppDesktopSizer(new Dimension(),0);
	protected boolean framesMaximum,settingActive,scaleFrames;
	final Timer layoutWait=new Timer(250,new ActionListener(){
		@Override
		public void actionPerformed(ActionEvent e){
			layoutWait.stop();
			updateLayout.run();
		}
	});
	private Runnable updateLayout;
	@Override
	public void doLayout(){
		Dimension size=getSize();
		t.trace(".doLayout: size="+size.height+" thenSize="+thenSize);
		if(size.height==0||size.equals(thenSize))return;
		if(layoutWait.isRunning())layoutWait.stop();
		updateLayout=new Runnable(){public void run(){
			AppDesktopSizer nowSize=new AppDesktopSizer(getSize(),thenSize.iconGap);
			t.trace(".doLayout: nowSize=",nowSize);
			nowSize.adjustFrames(frames,scaleFrames,thenSize);
			thenSize=nowSize;
		}};
		if(thenSize.height==0)updateLayout.run();
		else layoutWait.start();
	}
	/**
	Unique constructor. 
	@param background the background colour for the desktop
	 @param defaultMaximum should the first window be opened maximised?
	 @param scaleToDesktop equivalent to calling {@link #setScaleToDesktop(boolean)}   
	 */
	AppDesktopCore(Color background,boolean defaultMaximum,boolean scaleToDesktop){
		setBackground(background);
		framesMaximum=defaultMaximum;
		scaleFrames=scaleToDesktop;
	}
	/**
	Main updating method. 
	<ol>
		<li>Returns immediately if no content set.  
		<li>Where content has changed, updates frames and icons and
		calls {@link AppDesktopSizer#tileFrames(Iterable)} 
		<li>Activates and maybe maximises single frame.  
		<li>Calls {@link AppDesktopSizer#adjustFrames(Collection, boolean, AppDesktopSizer)}. 
		</ol>
	<p>Called from 
	<ul>
		<li>{@link #doLayout()} with non-zero current size
		<li>{@link #setContents(Collection)} with current size
		</ul>
	@param nowSize current size
	 */
	private void updateLayout(AppDesktopSizer nowSize){
		if(frames.isEmpty())return;
		if(false&&!nowSize.equals(getSize()))throw new IllegalArgumentException(
				"Bad nowSize="+nowSize+" for size="+getSize());
		else t.trace(".updateLayout: nowSize="+nowSize);
		if((getComponentCount()==0))for(AppDesktopFrame frame:frames){
				frame.updateTitle();
				frame.setVisible(true);
				add(!frame.isIcon()?frame:frame.getDesktopIcon());
				nowSize.tileFrames(frames);
			}
		if(frames.size()==1){
			AppDesktopFrame firstFrame=frames.get(0);
			t.trace(".updateLayout: single firstFrame=",firstFrame);
			firstFrame.trySetSelected();
			firstFrame.trySetMaximum(framesMaximum);
			nowSize=new AppDesktopSizer(nowSize,0);
		}
		nowSize.adjustFrames(frames,scaleFrames,thenSize);
		thenSize=nowSize;
		repaint();
		t.trace(".~updateLayout: frames=",frames.size());
		if(false&&frames.size()==0)invokeLater(new Runnable(){public void run(){
			JRootPane rootPane=getRootPane();
			if(rootPane!=null)rootPane.requestFocusInWindow();
		}});
	}
	/**
	Sets contents (possibly none) to be displayed in desktop windows. 
	<p>Each call must be followed by a call to {@link #setActiveContent(DesktopContent)};
	underlying window sizes are set on each call as if by a call to {@link #tileWindows()};  
	@param contents will be returned by {@link #getContents()}
	 */
	public final void setContents(Collection<DesktopContent>contents){
		if(contents==null)throw new IllegalArgumentException(
				"Null panels in "+Debug.info(this));
		else if(false)t.trace(".setContents: contents=",contents.size());
		List<AppDesktopFrame>nowFrames=new ArrayList(),thenFrames=new ArrayList(frames);
		for(DesktopContent content:contents){
			AppDesktopFrame addFrame=null;
			for(AppDesktopFrame thenFrame:frames)
				if(thenFrame.content==content){
					nowFrames.add(addFrame=thenFrame);
					thenFrames.remove(addFrame);
				}
			if(addFrame==null)nowFrames.add(addFrame=newFrame(content));
		}
		frames.clear();
		frames.addAll(nowFrames);
		if(frames.size()==0){
			setSelectedFrame(null);
			requestFocusInWindow();
		}
		for(AppDesktopFrame frame:thenFrames)frame.setUI(null);
		removeAll();
		repaint();
		updateLayout(thenSize);
		if(false)t.trace(".~setContents: frames=",frames.size());
	}
	/**
	Set the (possibly iconised) window to be displayed as active. 
	@param content contained by the window to be activated
	 */
	public final void setActiveContent(DesktopContent content){
		if(content==null)throw new IllegalArgumentException(
				"Null content in "+Debug.info(this));
		AppDesktopFrame selected=(AppDesktopFrame)getSelectedFrame();
		if(selected.content==content)return;
		t.trace(".setActiveContent: selected=",selected);
		settingActive=true;
		for(AppDesktopFrame frame:frames)
			if(frame.content==content){
				frame.trySetSelected();
				adjustMaximumVisibles();
				t.trace(".~setActiveContent: selected=",frame);
			}
		settingActive=false;
	}
	final protected void adjustMaximumVisibles(){
		JInternalFrame selected=getSelectedFrame();
		for(JInternalFrame frame:frames)
			if(!framesMaximum||frame!=selected)frame.setVisible(!framesMaximum);
	}
	/**
	Gets contents currently displayed in desktop windows. 
	@return contents last passed to {@link #setContents(Collection)} 
	 */
	final public List<DesktopContent>getContents(){
		List<DesktopContent>contents=new ArrayList();
		for(AppDesktopFrame frame:frames)contents.add(frame.content);
		if(false)t.trace(".getContents: contents=",contents.size());
		return Collections.unmodifiableList(contents);
	}
	protected abstract AppDesktopFrame newFrame(DesktopContent content);
	/**
	Restores, resizes and arranges all non-iconified content windows. 
	<p>Once a window has been iconified, windows are tiled above
	a 'gap' to avoid obscuring icons; the gap is reset to zero if all windows are 
	closed.    
	 */
	final public void tileWindows(){
		((AppDesktopFrame)getSelectedFrame()).trySetMaximum(false);
		thenSize.tileFrames(frames);
		thenSize.adjustFrames(frames,scaleFrames,thenSize);
	}
	/**
	Defines the behaviour of the {@link AppDesktop} on resize.
	<ul>
	<li><b>Off</b> - Window icon locations scaled to desktop, 
	active windows unaffected.    
	<li><b>On</b> - Icons rearranged, active windows resized to the new desktop dimensions. 
	</ul> 
	@param onOff sets the behaviour
	 */
	final public void setScaleToDesktop(boolean onOff){
		scaleFrames=onOff;
	}
}