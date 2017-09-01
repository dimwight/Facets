package pub.view;
import static applicable.field.CodeQuery.*;
import facets.core.app.ActionAppSurface;
import facets.core.app.Dialogs;
import facets.core.app.Dialogs.Response;
import facets.core.app.AppSurface;
import facets.core.app.NodeViewable;
import facets.core.app.PathSelection;
import facets.core.app.TreeView;
import facets.core.superficial.SFrameTarget;
import facets.core.superficial.SIndexing;
import facets.core.superficial.STarget;
import facets.core.superficial.STrigger;
import facets.core.superficial.app.SSelection;
import facets.core.superficial.app.SView;
import facets.core.superficial.app.SViewer;
import facets.facet.app.FacetAppSurface;
import facets.util.ArrayPath;
import facets.util.Debug;
import facets.util.Stateful;
import facets.util.ValueProxy;
import facets.util.tree.DataNode;
import facets.util.tree.NodePath;
import facets.util.tree.TypedNode;
import facets.util.tree.ValueNode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import applicable.field.FieldTableView;
import applicable.field.OptionField;
import applicable.field.TextField;
import applicable.field.ValueField;
abstract class ListingViewableCore extends NodeViewable{
	static final String TITLE_COMMENTS="WIPComments",TITLE_PRIORITY="WIPPriority";
	static final int TARGET_SELECTION_PRIORITY=0,TARGET_SELECTION_COMMENTS=1;
	final List<Stateful>output=new ArrayList(),input;
	final ListingContenter contenter;
	private boolean calledBefore;
	private SSelection lastTreeSelection;
	@Override
	public SFrameTarget selectionFrame(){
		return new SFrameTarget(selection().single()){
			final ValueNode values=RecordProxy.fieldValues((ValueNode)framed);
			final ValueField priority=PubFields.newPriorityField(false,contenter.spec);
			TextField comments=new TextField(TITLE_COMMENTS){
				@Override
				protected String valueKey(){
					return "Comments";
				}
				@Override
				public int inputCols(){
					return 20;
				}
			};
			@Override
			protected STarget[]lazyElements(){
				return new STarget[]{new SIndexing(TITLE_PRIORITY,((OptionField)priority).options,
						priority.newValue(values),new SIndexing.Coupler(){
					@Override
					public void indexSet(SIndexing i){
						RecordContent content=newSelectionContent();
						String indexed=(String)i.indexed();
						content.setField(priority,indexed,false);
						contenter.table.resetSort();
					}
				}),
				new STrigger(TITLE_COMMENTS+"...",new STrigger.Coupler(){
					@Override
					public void fired(STrigger t){
						Dialogs dialogs=contenter.app.dialogs();
						String proposal=comments.newValue(values).trim()+" ",
							input=dialogs.getTextInput(TITLE_COMMENTS,"",proposal,comments.inputCols());
						if(input!=null&&!input.trim().equals(proposal.trim())
								&&dialogs.confirmYesNo(TITLE_COMMENTS,"Store '"+input+"'?")==Response.Yes){
							RecordContent content=newSelectionContent();
							try{
								content.setField(comments,input.trim(),true);
							}catch(Exception e){
								PubFiles.checkLockException(e,dialogs);
								content.resetState();
							}
						}
					}
				})};
			}
		};
	}
	final protected RecordContent newSelectionContent(){
		return new RecordContent((ValueNode)selection().single(),listing().disposable());
	}
	ListingViewableCore(DataNode root,ListingContenter contenter){
		super(root);
		this.contenter=contenter;
		List<Stateful>input=new ArrayList(Arrays.asList(root.children()));
		output.addAll(input);
		((PubFields)contenter.table.fields).executeInputQueries(output,input);
		Collections.sort(output,ListingContent.SORT_MODIFIED);
		if(output.isEmpty())throw new IllegalStateException(
				"Empty input in "+Debug.info(this));
		else this.input=Collections.unmodifiableList(new ArrayList(output));
	}
	@Override
	final public String title(){
		return contenter==null?(super.title()+"[No contenter]")
				:contenter.title();
	}
	@Override
	public SSelection defineSelection(Object definition){
		TypedNode root=(TypedNode)framed;
		for(TypedNode node:root.children())node.setParent(root);
		return super.defineSelection(
				output.contains(definition)?definition:output.get(0));
	}
	@Override
	protected SSelection newViewerSelection(SViewer viewer){
		SView view=viewer.view();
		Object selected=selection().single();
		if(view instanceof TreeView)return lastTreeSelection!=null?lastTreeSelection
				:newOutputSelection(selected,true);
		FieldTableView table=(FieldTableView)view;
		int selectedAt=output.indexOf(selected);
		if(calledBefore&&table.sortInContent())table.sortContent(viewer,output);
		calledBefore=true;
		if(selectedAt<0)selected=defineSelection(output.get(0)).single();
		return table.newViewerSelection(viewer,newOutputSelection(selected,false));
	}
	protected SSelection newOutputSelection(Object selected,boolean forTree){
		ValueNode[]nodes=output.toArray(new ValueNode[]{});
		if(!forTree)return new PathSelection(nodes,new ArrayPath(nodes,selected));
		DataNode root=(DataNode)framed;
		return new PathSelection(root,new NodePath(new Object[]{root,selected}));
	}
	@Override
	protected void viewerSelectionChanged(SViewer viewer,SSelection selection){
		Object selected=selection.single();
		if(viewer.view()instanceof TreeView){
			lastTreeSelection=null;
			if(Arrays.asList(listing().tree).contains(selected))defineSelection(selected);
			else lastTreeSelection=selection;
		}
		else defineSelection(((ValueProxy)selected).source);
	}
	final protected ListingContent listing(){
		return((ListingContent)contenter.sink());
	}
}