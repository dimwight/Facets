package apps.hello;
import facets.core.app.SurfaceStyle;
import facets.core.superficial.app.SHost;
import facets.facet.kit.swing.KitSwing;
import facets.util.app.AppValues;
import java.awt.Dimension;
import javax.swing.SwingUtilities;
final public class HelloSwing extends HelloApp{
	public static final String ARG_APPLET="applet";
	public HelloSwing(AppValues values,String[]args){
		super(values,args);
	}
	protected void openInAppletWindow(Dimension size,String title){
		final KitSwing kit=new KitSwing(false,true,false);
		final SHost host=kit.newAppletHost(size.width,size.height,title);
		SwingUtilities.invokeLater(new Runnable(){public void run(){
			newSurface(kit,host,HelloContenter.class).buildRetargeted();
		}});
	}
	protected SurfaceStyle surfaceStyle(){
		return values.nature().getBoolean(ARG_APPLET)?SurfaceStyle.APPLET
				:super.surfaceStyle();
	}
	public static void main(final String[]args){
		KitSwing.debug=true;
		new HelloSwing(new AppValues(HelloSwing.class),args).openApp();
	}
}
