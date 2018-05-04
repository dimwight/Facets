package facets.facet.kit.swing;
import facets.core.app.HtmlView;
import facets.core.app.TreeView;
import facets.core.app.ViewerTarget;
import facets.core.superficial.Notifying.Impact;
import facets.core.superficial.app.SSelection;
import facets.facet.SwingViewerMaster;
import facets.util.Debug;
import facets.util.Tracer;
import facets.util.tree.TypedNode;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.html.HTMLEditorKit;
final class HtmlTreePaneMaster extends SwingViewerMaster{
	private static final class _SetWrap extends JPanel implements Scrollable{
		private final JEditorPane editor;
		private boolean wrapNow;
		_SetWrap(JEditorPane editor){
			super(new BorderLayout());
			add(this.editor=editor);
		}
		public boolean getScrollableTracksViewportWidth(){
			return true;
		}
		public boolean getScrollableTracksViewportHeight(){
			return true;
		}
		public Dimension getPreferredScrollableViewportSize(){
			return!wrapNow?getPreferredSize():editor.getPreferredScrollableViewportSize();
		}
		public int getScrollableBlockIncrement(Rectangle visibleRect,
				int orientation,int direction){
			return editor.getScrollableBlockIncrement(visibleRect,orientation,direction);
		}
		public int getScrollableUnitIncrement(Rectangle visibleRect,
				int orientation,int direction){
			return editor.getScrollableUnitIncrement(visibleRect,orientation,direction);
		}
		void setToView(HtmlView view){
			wrapNow=view.wrapLines();
		}
	}
	private static final String _href="(<a href=\")([^\"]+\")";
	private static final boolean swapForWrap=true;
	public void refreshAvatars(Impact impact){
		JComponent avatarPane=avatarPane();
		ViewerTarget target=viewerTarget();
		HtmlView view=new HtmlView("Dummy"){
			@Override
			public boolean wrapLines(){
				return true;
			}
		};
		JEditorPane editor;
		if(!swapForWrap){
			_SetWrap setWrap=(_SetWrap)avatarPane;
			editor=setWrap.editor;
			setWrap.setToView(view);
		}else{
			HtmlWrapPane swapWrap=(HtmlWrapPane)avatarPane;
			swapWrap.refreshForWrap(view.wrapLines());
			editor=swapWrap.editor();
		}
		editor.setEditorKit(HtmlView.showAllSources?new StyledEditorKit():new HTMLEditorKit());
		String text=((TreeView)target.view()).nodeRenderText(
				(TypedNode)target.selection().single());
		editor.setText(text.replaceAll(_href,"$1http:$2"));
		editor.getCaret().setDot(-1);
	}
	protected JComponent newAvatarPane(){
		JEditorPane editor=new JEditorPane();
		editor.setEditable(false);
		editor.addHyperlinkListener(new HyperlinkListener(){
			public void hyperlinkUpdate(final HyperlinkEvent e){
				if(e.getEventType()==HyperlinkEvent.EventType.ACTIVATED)
					viewerTarget().selectionChanged(new SSelection(){
						public Object[]multiple(){
							throw new RuntimeException("Not implemented in "+Debug.info(this));
						}
						public Object single(){
							return e.getURL().getPath();
						}
						public Object content(){
							throw new RuntimeException("Not implemented in "+Debug.info(this));
						}
					});
			}
		});
		return swapForWrap?new HtmlWrapPane(editor):new _SetWrap(editor);
	}
}