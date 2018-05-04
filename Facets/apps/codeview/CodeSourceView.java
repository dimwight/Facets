package apps.codeview;

import static apps.codeview.CodeViewConstants.*;
import facets.core.app.HtmlView;
import facets.core.app.NodeViewable;
import facets.core.app.SViewer;
import facets.core.app.ViewableFrame;
import facets.core.superficial.SFrameTarget;
import facets.core.superficial.SIndexing;
import facets.core.superficial.STarget;
import facets.core.superficial.SToggling;
import facets.core.superficial.TargetCore;
import facets.core.superficial.app.SSelection;
import facets.util.Debug;
import facets.util.Objects;
import facets.util.tree.DataConstants;
import facets.util.tree.DataNode;
import facets.util.tree.ExceptionNode;
import facets.util.tree.Nodes;
import facets.util.tree.TypedNode;
import applicable.LiveExternalWindow;

/**
View that displays syntax-coloured Java code as HTML. 
 <p>Policy state is maintained in {@link STarget}s which are exposed via
 the {@link SFrameTarget} returned by {@link #newFrame()}. 
 */
public class CodeSourceView extends HtmlView{

	//(Empty) coupler for indexing targets
	private final SIndexing.Coupler indexingCoupler = new SIndexing.Coupler();

	//Coupler for toggling targets
	private final SToggling.Coupler togglingCoupler = new SToggling.Coupler() {

		//Implement interface method
		public void stateSet(SToggling t) {

			//Delegate to appropriate logic
			if (t == coloured)
				colouredSet(t.isSet());
			else if (t == javadocHTML)
				javadocHTMLSet(t.isSet());
		}

		//Logic for colour toggle
		private void colouredSet(boolean coloured) {
			styled.setLive(!coloured);
			javadocGroup.setLive(coloured);
			if (coloured) {
				styled.set(true);
			} else {
				javadocHTML.set(false);
				javadocFirst.set(false);
			}
		}

		//Logic for rendering toggle
		private void javadocHTMLSet(boolean html) {
			javadocFirst.setLive(html);
			javadocFirst.set(html);
		}
	};

	//Indexing targets
	private final SIndexing 
	fontSize = new SIndexing("Font Size",
		new String[]{
			"Small", "Medium", "Large"
		},
		1,
		indexingCoupler),
	tabSpaces = new SIndexing("Tab Spaces",
			new String[]{
			"2", "4"
		},
		0,
		indexingCoupler);

	//Toggling targets
	private final SToggling 
	styled = new SToggling("Use Bold/Italic",
		true,
		togglingCoupler), 
	coloured = new SToggling("Use Color",
		true,
		togglingCoupler), 
	imports = new SToggling("Show Imports",
			false,
			togglingCoupler), 
	code = new SToggling("Show HTML",
			false,
			togglingCoupler), 
	javadocHTML = new SToggling("Render HTML",
		true,
		togglingCoupler), 
	javadocFirst = new SToggling("First Line Only",
		CodeViewContenter.debugEditingSource? false : true,
		togglingCoupler);
		
	//Target groupings
	private final STarget 
	syntaxGroup = new TargetCore("Syntax", new STarget[]{
		styled, coloured
	}), 
	textGroup = new TargetCore("Text", new STarget[]{
		fontSize, tabSpaces, syntaxGroup
	}), 
	javadocGroup = new TargetCore("Javadoc", new STarget[]{
		javadocHTML, javadocFirst
	}), 
	codeGroup = new TargetCore("Code", new STarget[]{
			imports, javadocGroup, code
	});	
		
	//Generator and store for HTML renderings of source code
	transient JavaHtmlPages pages = new JavaHtmlPages();

	//To get window status
	private final LiveExternalWindow window;

	//HTML of last code selection in external format
	private String lastExternalHtml;

	/**
	 Unique constructor. 
	 @param title passed to superclass
	 @param window if non-<code>null</code> allows checking of external
	 viewer status
	 */
	public CodeSourceView(String title, LiveExternalWindow window) {
		
		//Title to superclass, set reference to browser proxy
		super(title);
		this.window = window;
		
		//Initial settings
		styled.setLive(false);
	}
	
	/**
 	Re-implements method from viewers framework. 
	@see facets.core.superficial.app.SelectionView#newViewerSelection(
	facets.core.app.SViewer, facets.core.superficial.app.SSelection)
	 */
	@Override
	public synchronized SSelection newViewerSelection(SViewer viewer, 
			SSelection selection) {

		//Handle exception?
		if(selection.content() instanceof ExceptionNode)
			return newHTMLSelection("Empty", "[No Content]");
		
		//Return HTML as text (non-)selection
		return newHTMLSelection("Titled HTML", getSelectionHtml(selection, false));
	}

	public final String getSelectionHtml(SSelection selection, boolean external) {
		
		//Find selected node and get source child or create dummy
		TypedNode 
			selected = (TypedNode) selection.multiple()[0], 
			sourceNode = Nodes.child(selected, DataConstants.TYPE_SOURCE, "");
		if(sourceNode == null)sourceNode = new DataNode(DataConstants.TYPE_SOURCE, 
				TypedNode.UNTITLED);
		
		//Get lines as strings
		Object[] contents = sourceNode.contents();
		String lines[] = Objects.newTyped(String.class, contents),
			title = sourceNode.title();
		
		return getHtml(title, lines, external);
	}

	private SSelection newHTMLSelection(String title, final String html) {
		return new SSelection(){
			public Object[] multiple(){
				throw new RuntimeException("Not implemented in "+Debug.info(this));
			}
			public Object single(){
				throw new RuntimeException("Not implemented in "+Debug.info(this));
			}
			public Object content(){
				return html;
			}
		};
	}

	/**
	Re-implementation. 
	<p>(For debugging only)
	@see facets.core.app.HtmlView#showSource()
	 */
	@Override
	public boolean showSource(){
		return code.isSet();
	}

	/**
	Creates a frame for the view instance. 
	 */
	synchronized SFrameTarget newFrame() {
		
		return new SFrameTarget(this) {	
			@Override
			protected STarget[] lazyElements() {
				
				//Return the private target groups
				return new STarget[]{
					textGroup, codeGroup
				};
			}			
		};
	}

	/**
	 Align another frame including all element settings. 
	 <p>Called from {@link CodeViewContenter#newContentViews(NodeViewable)}
	 */
	void alignToView(CodeSourceView src) {
	
		//Share content store
		pages = src.pages;
		
		//Update to source state
		styled.set(src.styled.isSet());
		coloured.set(src.coloured.isSet());
		fontSize.setIndex(src.fontSize.index());
		tabSpaces.setIndex(src.tabSpaces.index());
		javadocHTML.set(src.javadocHTML.isSet());
		javadocFirst.set(src.javadocFirst.isSet());
		imports.set(src.imports.isSet());
	}

	/**
	 Gets HTML formatted code. 
	 <p>Called twice from {@link #newViewerSelection(SViewer, SSelection)}
	 @param sourceNode contains code
	 @param external determines size of font etc
	 */
	private synchronized String getHtml(String title, String[] lines, 
			boolean external) {
	
		//Read flag
		boolean shrinkExternal = window == null ? false 
				: window.shrinkExternal.isSet();
		
		//Set point size and leading, page size, tab size
		int textSizeIndex = fontSize.index();
		double 
		points = 
			textSizeIndex == TEXT_SIZE_SMALL
				? external
						? shrinkExternal
								? 70
								: 80
						: 11
			: textSizeIndex == TEXT_SIZE_MEDIUM
					? external
							? shrinkExternal
									? 75
									: 90
							: 12
					: external
							? shrinkExternal
									? 90
									: 100
							: 14, 
		leading = 
			external
				? 0
				: textSizeIndex == TEXT_SIZE_MEDIUM
						? 2
						: 0;
		int width = true? -1:(int) (points * (
					external
						? 9
						: 50));
	
		//Return HTML rendering
		return pages.getHtmlPage(lines, title, points, leading, width,
			tabSpaces.index() == 0 ? 2 : 4,
			styled.isSet(),
			coloured.isSet(),
			imports.isSet(),
			javadocHTML.isSet(),
			javadocFirst.isSet());
	}
}