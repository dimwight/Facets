package pdft.inspect;
import facets.core.superficial.SFrameTarget;
import facets.core.superficial.SIndexing;
import facets.core.superficial.STarget;
import facets.core.superficial.app.SSelection;
import facets.core.superficial.app.SViewer;
import facets.core.superficial.app.SelectionView;
import facets.util.Debug;
import facets.util.Titled;
import facets.util.Util;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdfviewer.ArrayEntry;
import org.apache.pdfbox.pdfviewer.MapEntry;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
final class CosTreeView extends SelectionView{
	enum TreeStyle{
		Document(3),
		Pages(2),
		Trailer(Integer.MAX_VALUE);
		final int pagePathMaxDepth;
		TreeStyle(int pagePathMaxDepth){
			this.pagePathMaxDepth=pagePathMaxDepth;
		}
		String title(){
			return this==Pages?"Pa&ges":this==Document?"Doc&ument":toString();
		}
	}
	final private static Set pagePathKeys=Collections.unmodifiableSet(
		new HashSet(Arrays.asList(new Object[]{
			COSName.getPDFName("Root"),
			COSName.getPDFName("Pages"),
			COSName.getPDFName("Kids")
		})));
	final private class PagePaths{
		final Map<COSDictionary,TreePath>paths=new HashMap();
		private final TreeModel model;
		PagePaths(TreeModel model){
			this.model=model;
		}
		void storePaths(){
			addPagePaths(new TreePath(model.getRoot()),style.pagePathMaxDepth);
			if(true)return;
			Util.printOut("PagePaths.storePaths: ",paths.keySet().toArray());
			for(TreePath path:paths.values())
				for(Object node:path.getPath()) {
					Util.printOut("PagePaths.storePaths: class="+node.getClass());
					if(node instanceof MapEntry)
					Util.printOut("PagePaths.storePaths: key="+((MapEntry)node).getKey());
				}
		
		}
		private void addPagePaths(TreePath path,int depthLeft){
			if(--depthLeft<0)return;
			Object node=path.getLastPathComponent();
			if(node instanceof MapEntry&&!pagePathKeys.contains(
					((MapEntry)node).getKey()))return;
			if(CosTreeMaster.pageValue(node)!=null){
				COSDictionary key=(COSDictionary)
					(node instanceof ArrayEntry?((ArrayEntry)node).getValue():node);
				paths.put(key,path);
				if(false)Util.printOut("CosTreeView.PagePaths.addPagePaths: " +Debug.info(key)+
						Debug.arrayInfo(path.getPath()));
				return;
			}
			for(int childAt=0;childAt<model.getChildCount(node);childAt++)
				addPagePaths(path.pathByAddingChild(model.getChild(node,childAt)),depthLeft);
		}
	}
	final TreeStyle style;
	CosTreeView(TreeStyle style){
		super(style.title());
		this.style=style;
	}
	final Map<COSDictionary,TreePath>newPagePaths(TreeModel model){
		PagePaths paths=new PagePaths(model);
		paths.storePaths();
		return paths.paths;
	}
	@Override
	final public SSelection newViewerSelection(SViewer viewer,final SSelection viewable){
		return newSelection(style,viewable);
	}
	private static SSelection newSelection(final TreeStyle style,final SSelection viewable){
		final COSDocument document=(COSDocument)viewable.content();
		final COSArray array=new COSArray();
		for(Object each:new PDDocument(document).getDocumentCatalog().getAllPages())
			array.add(((PDPage)each).getCOSObject());
		return new SSelection(){
			@Override
			public Object content(){
				if(style!=TreeStyle.Pages)
					return style==TreeStyle.Document?document:document.getTrailer();
				return array;
			}
			@Override
			public Object single(){
				throw new RuntimeException("Not implemented in "+Debug.info(this));
			}
			@Override
			public Object[]multiple(){
				return viewable.multiple();
			}
		};
	}
	static STarget newIndexing(){
		return SIndexing.newDefault("COS Objects",new Titled[]{
				new SFrameTarget(new CosTreeView(TreeStyle.Pages)),
				new SFrameTarget(new CosTreeView(TreeStyle.Trailer)),
				new SFrameTarget(new CosTreeView(TreeStyle.Document)),
		});
	}
}
