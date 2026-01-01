package applicable.treeplus;

import facets.core.app.NodeViewable;
import facets.core.app.SViewer;
import facets.core.app.StatefulViewable;
import facets.core.superficial.app.SSelection;
import facets.util.tree.TypedNode;

public abstract class TreePlusViewable extends NodeViewable {
    protected String textViewerEdit;

    public TreePlusViewable(TypedNode tree, ClipperSource clipperSource) {
        super(tree, clipperSource);
    }

    protected abstract SSelection newNonTreeViewerSelection(SViewer viewer);

    protected abstract void nonTreeViewerSelectionChanged(SViewer viewer,
                                  SSelection selection);

    protected abstract void nonTreeViewerSelectionEdited(SViewer viewer, Object edit,
                                                         boolean interim);

    protected void textViewerSelectionEdited(SViewer viewer, Object edit,
                                             boolean interim) {
        textViewerEdit = (String) edit;
        maybeModify();
        updateAfterEditAction();
        textViewerEdit = null;
    }
}
