package facets.facet.kit.swing;
import static java.awt.datatransfer.DataFlavor.*;
import facets.core.app.StatefulViewable;
import facets.core.app.StatefulViewable.Clipper;
import facets.util.Stateful;
import facets.util.Tracer;
import facets.util.Util;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import javax.swing.JLabel;
final public class ClipperSwing extends Tracer implements Clipper{
	private final Clipboard clipboard;
	private static Clip vmClip;
	private abstract class Clip implements Transferable,ClipboardOwner{
		public void lostOwnership(Clipboard clipboard,Transferable contents){}
	}
	private final StatefulViewable viewable;
	final public DataFlavor dataFlavor;
	public ClipperSwing(StatefulViewable viewable,boolean useSystemClipboard){
		this.clipboard=!useSystemClipboard?null
				:new JLabel().getToolkit().getSystemClipboard();
		dataFlavor=new DataFlavor((this.viewable=viewable).getClass(),null);
	}
	public void copySelection(){
		Clip clip=newClip();
		if(clipboard!=null)clipboard.setContents(clip,clip);
		else vmClip=clip;
	}
	public boolean canPaste(){
		if(clipboard==null)return vmClip!=null
			&&vmClip.isDataFlavorSupported(dataFlavor);
		Class viewableClass=viewable.getClass();
		DataFlavor flavors[],pastable=null;
		try {
			flavors=clipboard.getAvailableDataFlavors();
		}
		catch (IllegalStateException e){return false;}
		for(int f=0;f<flavors.length;f++){
			Class checkClass=flavors[f].getRepresentationClass();
			for(;checkClass!=null;checkClass=checkClass.getSuperclass())
				if(checkClass==viewableClass)pastable=flavors[f];
			if(pastable==null&&flavors[f]==stringFlavor)
				pastable=stringFlavor;
		}
		if(pastable==null)return false;
		if(pastable==dataFlavor)return true;
	  try{
	  	Transferable clipData=clipboard.getContents(viewable);
	    return viewable.textSeemsPastable((String)clipData.getTransferData(
	    		stringFlavor));
	  } 
	  catch (Exception e) {
	    return false;
	  }
	}
	public Stateful[]newStatefuls(){
		try{
			Transferable clip=clipboard==null?vmClip:clipboard.getContents(viewable);
			Object data=clip.getTransferData(dataFlavor);
			return newCopyStatefuls((Stateful[])data);
		}catch(Exception e){
			if(true)throw new RuntimeException(e);
			return null;
		}
	}
	public Clip newClip(){
		final Stateful[]copies=newCopyStatefuls(
				StatefulViewable.newStatefulArray(viewable.selection().multiple()));
		return new Clip(){
			public DataFlavor[]getTransferDataFlavors(){
				return new DataFlavor[]{stringFlavor,dataFlavor};
			}
			public boolean isDataFlavorSupported(DataFlavor flavor){
				return flavor==stringFlavor||
				flavor.getRepresentationClass()==viewable.getClass();
			}
			public Object getTransferData(DataFlavor flavor)
					throws UnsupportedFlavorException,IOException{
				if(!isDataFlavorSupported(flavor))
					throw new UnsupportedFlavorException(flavor);
				try {
					return flavor==stringFlavor?viewable.newStatefulsText(copies):copies;		
				}
				catch(Exception e){throw new IOException(e.getMessage());}
			}
		};
	}
	static Stateful[]newCopyStatefuls(Stateful[]statefuls){
		Stateful[]copies=new Stateful[statefuls.length];
		for(int i=0;i<copies.length;i++)
			copies[i]=(Stateful)Util.deserializedCopy(statefuls[i]);
		return copies;
	}
}