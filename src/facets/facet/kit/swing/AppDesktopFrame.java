package facets.facet.kit.swing;
import facets.facet.kit.swing.AppDesktop.FrameSignals;
import facets.facet.kit.swing.AppMultiMount.DesktopContent;
import facets.util.Debug;
import facets.util.Util;
import facets.util.app.BusyCursor.BusySettable;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.beans.PropertyVetoException;
import javax.swing.JInternalFrame;
import javax.swing.plaf.InternalFrameUI;
import javax.swing.plaf.basic.BasicInternalFrameUI;
final class AppDesktopFrame extends JInternalFrame implements BusySettable{
	@Override
	public void setUI(InternalFrameUI ui){
		super.setUI(false?new BasicInternalFrameUI(this):ui);
	}
	static final boolean trace=false;
	final DesktopContent content;
	private final FrameSignals signals;
	private Rectangle restoreBounds;
	AppDesktopFrame(final AppDesktop desktop,final DesktopContent content){
		setMaximizable(true);
		setResizable(true);
		setClosable(true);
		setIconifiable(true);
		setVisible(true);
		this.content=content;
		getContentPane().add(content);
		updateTitle();
		pack();		
		signals=desktop.new FrameSignals(this,trace);
		addVetoableChangeListener(signals);
		addInternalFrameListener(signals);
		addPropertyChangeListener(signals);
	}
	void setRestoreBounds(Rectangle bounds){
		if(bounds==null)throw new IllegalArgumentException(
				"Null bounds in "+Debug.info(this));
		restoreBounds=bounds;
		if(!isMaximum)setBounds(bounds);
		if(false)trace("~setRestoreBounds: size="+bounds.width+" ");
	}
	Rectangle getRestoreBounds(){
		return restoreBounds;
	}
	@Override
	public void setBounds(int x,int y,int width,int height){
		super.setBounds(x,y,width,height);
		if(false)trace("~setBounds: size="+width+" ");
	}
	void trySetMaximum(boolean on){
		trace("trySetMaximum: "+on+" ");
		if(true)try{
			setMaximum(on);
			repaint();
		}catch(PropertyVetoException e){
			throw new RuntimeException(e);
		}
		else isMaximum=on;
		trace("~trySetMaximum: "+on+" ");
	}
	@Override
	public void repaint(){
		updateTitle();
		super.repaint();
	}
	void trySetSelected(){
		try{
			trace("trySetSelected");
			setVisible(true);
			setSelected(true);
			trace("~trySetSelected");
		}catch(PropertyVetoException e){
			throw new RuntimeException(e);
		}
	}
	void updateTitle(){
		if(content==null)return;
		setFrameIcon(content.windowIcon());
		setTitle(content.windowTitle());
	}
	int getIconisedHeight(){
		return desktopIcon.getSize().height;
	}
	void trace(String msg){
		if(trace)Util.printOut("Frame."+msg+": "+this);
	}
	public String toString(){
		Dimension size=getSize(),
			restore=restoreBounds==null?null:(restoreBounds.getSize());
		return getTitle()+" icon="+isIcon+" selected="+isSelected()+
			(false?(" "+Debug.info(content))
					:(" maximum="+isMaximum+" size="+size.width)+
					(restore==null||restore.equals(size)?"":"|"+restore.width)+" visible="+isVisible()
				).replaceAll("java[^\\[]+","");
	}
}