package tones.app;
import static facets.core.app.ActionViewerTarget.Action.*;
import static tones.app.TonesEdit.ARG_TREE;

import facets.core.app.PathSelection;
import facets.core.app.SView;
import facets.core.app.SViewer;
import facets.core.app.TreeView;
import facets.core.app.ViewableAction;
import facets.core.app.avatar.AvatarView;
import facets.core.superficial.SFrameTarget;
import facets.core.superficial.STarget;
import facets.core.superficial.STextual;
import facets.core.superficial.app.SSelection;
import facets.facet.app.FacetAppSurface;
import facets.util.tree.DataNode;
import facets.util.tree.TypedNode;
import facets.util.tree.ValueNode;
import applicable.treetext.TreeTextViewable;
import tones.Voice;
import tones.bar.Bars;
import tones.page.PageNote;
import tones.view.PageView;
public final class TonesViewable extends TreeTextViewable{
	public final Bars bars;
	SFrameTarget barsView=new SFrameTarget(new TreeView("Bar Contents"){
		@Override
		public boolean hideRoot(){
			return true;
		}
		@Override
		public boolean canChangeSelection(){
			return false;
		}
		@Override
		public String nodeRenderText(TypedNode node){
			return node.title();
		}
	}){
	};
	TonesViewable(TypedNode tree, ClipperSource clipperSource, FacetAppSurface app){
		super(tree,clipperSource,app);
        if (tree.children()[0].children().length==5){
            Bars fromTxt = new Bars(this);
			if (!app.spec.args().getBoolean(ARG_TREE)){
				bars=fromTxt;
				return;
			}
            DataNode xml = fromTxt.newDataTree(0, 0);
            tree.setChildren(xml.children());
        }
		bars= new Bars(this, (DataNode) tree);
	}
	private int barStart,checkShowThen[];
	private PageView page;
	final private static String NO_CODES="[No codes]";
	private String before,after,show;
	private Voice voiceThen;
	private STextual textual;
	public SFrameTarget selectionFrame(){
		barStart=page.barStart();
		return new SFrameTarget(selection().single()){
			protected STarget[]lazyElements(){
				return new STarget[]{};
			}
		};
	}
	private void doUndoableEdit(ValueNode selected,String src){
		selected.setValues(new String[]{src});
		textViewerEdit=src;
		maybeModify();
		updateAfterEditAction();
	}
	@Override
	protected void editUndoneOrRedone(){
		show=null;
	}
	@Override
	public ViewableAction[] viewerActions(SView view){
		return new ViewableAction[]{UNDO,REDO,
				//        DELETE,
				//        MODIFY
		};
	}
	@Override
	protected SSelection newNonTreeViewerSelection(SViewer viewer){
		SView view=viewer.view();
		if(view instanceof AvatarView){
			page=(PageView)view;
			return page.avatars().newAvatarSelection(viewer,
					new SSelection(){//?
				@Override
				public Object content(){
					return bars;
				}
				@Override
				public Object single(){
					throw new RuntimeException("Not implemented in "+this);
				}
				@Override
				public Object[] multiple(){
					throw new RuntimeException("Not implemented in "+this);
				}
			});
		}
		DataNode root = bars.newDataTree(barStart, page == null ? 0 : page.barStop());
		return ((TreeView)view).newViewerSelection(viewer,PathSelection.newMinimal(root));
	}
	@Override
	protected void nonTreeViewerSelectionChanged(SViewer viewer,
			SSelection selection){
		Object single=selection.single();
		if(single instanceof PageView) {
		}
		else if(single instanceof PageNote) {
		}
	}
	public TypedNode contentTree(){
		return ((TypedNode)framed).children()[0];
	}
}
