package applicable.eval.view;
import static facets.core.app.Dialogs.Response.*;
import facets.core.app.Dialogs;
import facets.core.app.Dialogs.Response;
import facets.core.superficial.SIndexing;
import facets.core.superficial.SToggling;
import facets.core.superficial.STrigger;
import facets.util.HtmlBuilder.RenderTarget;
import facets.util.HtmlFormBuilder;
import facets.util.HtmlFormBuilder.FormInput;
import facets.util.tree.TypedNode;
import applicable.eval.app.EvalCoder;
import applicable.eval.form.EvalForm;
import applicable.eval.form.EvalRecord;
final public class ViewableForm extends EvalForm{
	private class ActiveCleaner{
		private final String activeTitle=active.title();
		private final boolean checkAbandon;
		ActiveCleaner(boolean checkAbandon){
			this.checkAbandon=checkAbandon;
		}
		final boolean tryClean(){
			String storeTitle=null;
			Response abandon=Cancel;
			while(storeTitle==null&&abandon==Cancel){
				if(storeTitle==null){
					if(checkAbandon)abandon=dialogs.confirmYesNo("Data Changed",
							"Keep changes to '"+activeTitle+"'?")==No?Yes:No;
					else abandon=No;					
				}
				if(abandon!=Yes)
					storeTitle=dialogs.getTextInput("Store Active","Store as: ",activeTitle,15);
			}
			if(storeTitle==null)switch(abandon){
				case Yes:revertActive();return true;
				case No:keepActive();return false;
				case Cancel:throw new IllegalStateException(
						"Impossible response for "+ViewableForm.this.title());
				}
			else if(storeTitle.equals(activeTitle))storeActive();
			else copyActive(storeTitle);
			updateSources();
			return true;
		}
		void revertActive(){
			active.revertState();
		}
		void keepActive(){}
		void storeActive(){
			active.storeState();
		}
		EvalRecord copyActive(String title){
			EvalRecord copy=active.copyState(title);
			active.revertState();
			records.add(0,copy);
			return copy;
		}
	}
	final public SIndexing indexing=new SIndexing(recordType,
			new SIndexing.Coupler(){
		@Override
		public void indexSet(SIndexing i){
			EvalRecord indexed=(EvalRecord)i.indexed();
			if(false)trace(".indexSet: indexed=",indexed);
			if(active==indexed)return;
			if(!active.hasChanged())active=indexed;
			else new ActiveCleaner(true){
				@Override
				void storeActive(){
					super.storeActive();
					active=indexed;
					if(active!=records.get(i.index()))throw new IllegalStateException(
							"Mismatched active="+active.title());
				}
				@Override
				EvalRecord copyActive(String title){
					i.setIndexed(active=super.copyActive(title));
					return null;
				}
				@Override
				void revertActive(){
					super.revertActive();
					active=indexed;
				}
				@Override
				void keepActive(){
					super.keepActive();
					indexing.setIndexed(active);
				}
			}.tryClean();
			active.updateToFields(fields);
		}
		@Override
		public Object[]getIndexables(){
			return records.toArray();
		}
		@Override
		public String[]newIndexableTitles(SIndexing ix){
			String[]titles=new String[records.size()];
			for(int at=0;at<titles.length;at++){
				EvalRecord record=records.get(at);
				titles[at]=record.title()+(false&&record.hasChanged()?"*":"");
			}
			return titles;
		}
	});
	
	public final STrigger storer=new STrigger("Store",new STrigger.Coupler(){
		@Override
		public void fired(STrigger t){
			new ActiveCleaner(false){
				@Override
				EvalRecord copyActive(String title){
					indexing.setIndexed(active=super.copyActive(title));
					return null;
				}
				@Override
				void revertActive(){
					super.revertActive();
				}
				@Override
				void keepActive(){
					super.keepActive();
					indexing.setIndexed(active);
				}
			}.tryClean();
		}
	}),
	remover=new STrigger("Remove",new STrigger.Coupler(){
		@Override
		public void fired(STrigger t){
			if(dialogs.confirmYesNo("Remove Active","Remove '"+active.title()+"'?"
					)!=Response.Yes)return;
			records.remove(active);
			indexing.setIndexed(records.get(0));
			active=(EvalRecord)indexing.indexed();
			active.updateToFields(fields);
			updateSources();
		}
	});
	final public SToggling codesToggling=new SToggling("Codes",showCodes,
			new SToggling.Coupler(){
		@Override
		public void stateSet(SToggling t){
			showCodes=t.isSet();
		}
	});
	final private Dialogs dialogs;
	final private EvalCoder coder;
	HtmlFormBuilder page;
	public ViewableForm(TypedNode code,Dialogs dialogs,EvalCoder coder){
		super(code);
		this.dialogs=dialogs;
		this.coder=coder;
	}
	public boolean isClean(){
		return !active.hasChanged()||new ActiveCleaner(true).tryClean();
	}
	protected void updateSources(){
		super.updateSources();
		coder.updateFromRecords(records,recordType);
	}
	public boolean canRemoveActive(){
		return records.size()>1&&(true||!active.hasChanged());
	}
	public void readEdit(Object edit){
		page.readEdit((FormInput)edit);
	}
	public String newPageContent(){
		return (page=newInputsBuilder()).newPageContent();
	}
	public HtmlFormBuilder newInputsBuilder(){
		String[]nameSet=newInputNames();
		return new HtmlFormBuilder(RenderTarget.Swing,nameSet){
			@Override
			public String newPageContent(){//<font size=
				String superContent=super.newPageContent();
				String pageTop="<table border=0 cellspacing=3 cellpadding=0>"
						+ "<tr><td><table><tr>\n";
				return superContent.replace(pageTop,pageTop+
						"\n<td><b><font size=+1>"
						+ "Configuration: </b>"+(active.hasChanged()?"*":"")
						+ active.title()
						+ "</font><br>&nbsp;</td></tr>\n");
			}
			@Override
			protected Object getValue(String name){
				return getFieldValue(name);
			}
			@Override
			protected boolean useInputField(String name){
				return fieldCanInput(name);
			}
			@Override
			protected String newFieldCode(String name,Object value){
				return newFieldHtml(name);
			}
			@Override
			public void readEdit(FormInput edit){
				fieldEdited(edit.name,edit.value);
			}
			@Override
			protected void traceOutput(String msg){
				traceOutputWithClass(msg);
			}
		};
	}
}
