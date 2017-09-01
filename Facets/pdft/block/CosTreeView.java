package pdft.block;
import static pdft.block.CosTreeView.TreeStyle.*;
import facets.core.superficial.SFrameTarget;
import facets.core.superficial.SIndexing;
import facets.core.superficial.STarget;
import facets.core.superficial.app.SSelection;
import facets.core.superficial.app.SViewer;
import facets.core.superficial.app.SelectionView;
import facets.util.Debug;
import facets.util.Times;
import facets.util.Titled;
import facets.util.Util;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.pdfviewer.ArrayEntry;
import org.apache.pdfbox.pdfviewer.MapEntry;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import pdft.block.PdfContenter.CosDisposer;
final class CosTreeView extends SelectionView{
	enum TreeStyle{
		Document(3,null),
		Pages(2,null),
		Trailer(100,Arrays.asList(new COSName[]{
				COSName.getPDFName("Root"),
				COSName.getPDFName("Pages"),
				COSName.getPDFName("Kids")
			}));
		final int pagePathMaxDepth;
		final Collection<COSName>pagePathNames;
		TreeStyle(int pagePathMaxDepth,Collection<COSName>pagePathKeys){
			this.pagePathMaxDepth=pagePathMaxDepth;
			this.pagePathNames=pagePathKeys;
		}
		String title(){
			return this==Pages?"Pa&ges":this==Document?"Doc&ument":toString();
		}
	}
	final TreeStyle style;
	CosTreeMaster master;
	CosTreeView(TreeStyle style){
		super(style.title());
		this.style=style;
	}
	@Override
	final public SSelection newViewerSelection(SViewer viewer,final SSelection viewable){
		return newSelection(style,viewable);
	}
	private static SSelection newSelection(final TreeStyle style,final SSelection viewable){
		final CosDisposer disposer=(CosDisposer)viewable.content();
		return new SSelection(){
			@Override
			public Object content(){
				COSDocument doc=disposer.disposable();
				return style==Pages?disposer.pages():style==Trailer?doc.getTrailer():doc;
			}
			@Override
			public Object single(){
				return viewable.single();
			}
			@Override
			public Object[]multiple(){
				throw new RuntimeException("Not implemented in "+Debug.info(this));
			}
		};
	}
}