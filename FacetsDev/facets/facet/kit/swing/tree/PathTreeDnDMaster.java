package facets.facet.kit.swing.tree;
import facets.core.app.NodeViewable;
import facets.core.app.PathSelection;
import facets.core.app.StatefulViewable;
import facets.core.app.ViewerTarget;
import facets.core.superficial.Notifying.Impact;
import facets.core.superficial.app.SSelection;
import facets.facet.kit.swing.ClipperSwing;
import facets.facet.kit.swing.ViewerMaster;
import facets.util.Debug;
import facets.util.Stateful;
import facets.util.Tracer;
import facets.util.tree.NodePath;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import javax.swing.DropMode;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
public final class PathTreeDnDMaster extends PathTreePaneMaster{
	MouseEvent lastEvent;
	private JTree pane;
	public PathTreeDnDMaster(){
		super(false);
	}
	@Override
	protected JComponent newAvatarPane(){
		pane=(JTree)super.newAvatarPane();
		pane.setDragEnabled(true);
		pane.setTransferHandler(handler);
		pane.setDropMode(DropMode.USE_SELECTION);
		return pane;
	}
	private final static TransferHandler handler=new TransferHandler(){
		Tracer t=Tracer.newTopped("PathTreeDnDMaster",false);
		private ViewerTarget from,to;
		private NodeViewable fromViewable,toViewable;
		private PathSelection fromSelection,toSelection;
		private DataFlavor toFlavor;
		public int getSourceActions(JComponent c){
			t.traceDebug(".getSourceActions: ",c);
			from=getTreeViewer(c);
			if(from==null)throw new IllegalStateException("Null from in "+Debug.info(c));
			fromViewable=(NodeViewable)from.viewable;
			fromSelection=(PathSelection)fromViewable.selection();
			return COPY_OR_MOVE;
		}
		protected Transferable createTransferable(JComponent c){
			t.traceDebug(".createTransferable: ",c);
			if(getTreeViewer(c)!=from)
				throw new IllegalArgumentException("Bad component "+Debug.info(c));
			return new ClipperSwing(fromViewable,false).newClip();
		}
		public boolean canImport(JComponent c,DataFlavor[]flavors){
			to=getTreeViewer(c);
			toViewable=(NodeViewable)to.viewable;
			if(to==null||from==null
					||(to!=from&&toViewable==fromViewable))return false;
			toSelection=(PathSelection)toViewable.selection();
			toFlavor=new ClipperSwing(toViewable,false).dataFlavor;
			for(int i=0;i<flavors.length;i++)
				if(flavors[i].equals(toFlavor)) return true;
			return false;
		}
		public boolean importData(JComponent c,Transferable data){
			if(!canImport(c,data.getTransferDataFlavors()))return false;
			t.traceDebug(".importData: ",data);
			if(to!=from)transferTo(data);
			return true;
		}
		protected void exportDone(JComponent c,Transferable data,int action){
			if(getTreeViewer(c)!=from) throw new IllegalArgumentException(
					"Bad component "+Debug.info(c));
			t.traceDebug(".exportDone: ",c);
			if(action==MOVE){
				if(to==from){
					if(((NodePath)fromSelection.paths[0]).isParent(toSelection.paths[0]))
						toViewable.defineSelection(fromSelection);
					else fromViewable.insertWithDelete(
							newTransferableStatefuls(data),fromSelection);
				}
				else{
					fromViewable.deleteSelection(false);
					fromViewable.updateAfterEditAction();
				}
			}
			else if(to==from)transferTo(data);
			if(to!=null)to.ensureActive(Impact.ACTIVE);
			fromViewable.notifyParent(Impact.CONTENT);
			from=to=null;
		}
		private void transferTo(Transferable data){
			toViewable.insertStatefuls(false,newTransferableStatefuls(data));
			toViewable.updateAfterEditAction();
		}
		private Stateful[]newTransferableStatefuls(Transferable data){
			Stateful[]statefuls;
			try{
				statefuls=(Stateful[])data.getTransferData(toFlavor);
			}catch(UnsupportedFlavorException ufe){
				throw new RuntimeException(ufe);
			}catch(IOException ioe){
				throw new RuntimeException(ioe);
			}
			return statefuls;
		}
		private ViewerTarget getTreeViewer(JComponent c){
			return!(c instanceof MasterPathTree)?null
					:((MasterPathTree)c).master.viewerTarget();
		}
	};

}
