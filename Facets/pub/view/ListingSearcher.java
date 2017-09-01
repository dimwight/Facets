package pub.view;
import static applicable.TextQuery.*;
import static applicable.field.CodeQuery.*;
import facets.core.app.Dialogs.MessageTexts;
import facets.core.superficial.Notifying.Impact;
import facets.core.superficial.SIndexing;
import facets.core.superficial.STarget;
import facets.core.superficial.STextual;
import facets.core.superficial.SToggling;
import facets.core.superficial.STrigger;
import facets.core.superficial.TargetCore;
import facets.facet.FacetFactory.SuggestionsCoupler;
import facets.facet.FacetFactory.TriggerCodeCoupler;
import facets.util.Stateful;
import facets.util.Times;
import facets.util.Titled;
import facets.util.Tracer;
import facets.util.Util;
import facets.util.tree.TypedNode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import pub.PubIndexer;
import pub.PubIssue;
import applicable.ItemHistory;
import applicable.field.CodeQuery;
final class ListingSearcher extends Tracer{
	public static final String TITLE_BOX="Search for",TITLE_MATCH="Match:",TITLE_EXACT="E&xact",
		TITLE_REFS="Texts",TITLE_MENU="Results",
		TITLE_NO_RESULTS="[No results]",TITLE_REMOVE=CodeQuery.CODE_WITHOUT+"|Remo&ve Item",
		TITLE_CLEAR="C&lear Results|Clear",TITLE_RETITLE="Change &Title...",ARG_SEARCH="searchNum";
	static final class Results{
		final List<Stateful>items;
		private String title;
		Results(String title,List<Stateful>items){
			this.title=title.replace(BARE_FIELD_TOP,"");
			this.items=new ArrayList(items);
		}
		void retitle(String title){
			this.title=title;
		}
		@Override
		public String toString(){
			return title;
		}
		@Override
		public boolean equals(Object o){
			Results that=(Results)o;
			return that.title.equals(title)&&that.items.equals(items);
		}
	}
	final SToggling refs=new SToggling(TITLE_REFS,false,new SToggling.Coupler(){
		@Override
		public void stateSet(SToggling t){
			PubIndexer.getRefsInstance();
		}
	});
	private final STextual box;
	private final STrigger.Coupler actionsCoupler=new STrigger.Coupler(){
		@Override
		public void fired(STrigger t){
			TypedNode target=(TypedNode)viewable.selection().single();
			List<Stateful>output=viewable.output;
			if(t==remove&&output.size()>1){
				int targetAt=output.indexOf(target);
				output.remove(targetAt);
				int size=output.size();
				if(size>1)viewable.defineSelection(output.get(targetAt+(targetAt>size-2?-1:0)));
				if(results!=null)results.pushItem(new Results(resultsTitle=
					"Removed "+((Titled)viewable.selection().single()).title()+" = "+output.size(),output));
				viewable.defineSelection(viewable.selection().single());
			}
			else{
				if(false)Times.printElapsed("TableViewable.actionsCoupler");
				output.clear();
				output.addAll(viewable.input);
				viewable.contenter.setResultsMessage(newEmptyMessage());
				viewable.contenter.table.resetSort();
			}
		}};
	final STrigger go,remove=new STrigger(TITLE_REMOVE,actionsCoupler),
			clear=new STrigger(TITLE_CLEAR,actionsCoupler);
	private final SIndexing match;
	private final SToggling exact;
	public final STarget targets;
	final ItemHistory<Results>results;
	String resultsTitle;
	private final ListingViewableCore viewable;
	ListingSearcher(final ListingViewableCore viewable){
		this.viewable=viewable;
		final List<Stateful>output=viewable.output;
		box=new STextual(TITLE_BOX,viewable.contenter.boxOpeningText(),
				new SuggestionsCoupler(viewable.contenter.app.spec){
			@Override
			protected STrigger commitTrigger(){
				return go;
			}
			@Override
			public String typeKey(){
				return Util.shortTypeNameKey(ListingViewable.class);
			}
		});
		go=new STrigger("Go",new TriggerCodeCoupler(){
			@Override
			public String[]codes(){
				return output.size()==viewable.input.size()?CODES_ALL:CODES_ADD;
			}
			@Override
			protected void firedCode(String code){
				goFired(code);
			}
		});
		match=new SIndexing(TITLE_MATCH,new Object[]{"A&ll","A&ny"},0,new SIndexing.Coupler());
		exact=new SToggling(TITLE_EXACT,false,new SToggling.Coupler());
		targets=new TargetCore("Search",new STarget[]{box,go,match,exact});
		resultsTitle=viewable.contenter.title()+" = "+viewable.output.size();
		results=new ItemHistory<Results>(10,new Results(resultsTitle,output),
				viewable.contenter.app.dialogs(),
				TITLE_MENU,TITLE_RETITLE,"Enter new title for current results."){
			@Override
			protected void itemIndexed(Results search){
				output.clear();
				output.addAll(search.items);
				resultsTitle=search.toString();
				viewable.contenter.table.resetSort();
				viewable.defineSelection(viewable.selection().single());
				viewable.notifyParent(Impact.DEFAULT);
			}
			@Override
			protected void retitleItem(Results item,String name){
				item.retitle(resultsTitle=name);
			}
		};
	}
	private void goFired(String code){
		String queryText=box.text().trim();
		if(queryText.equals(""))return;
		boolean any=match.index()==1,exact=this.exact.isSet();
		CodeQuery q=!refs.isSet()?PubFields.newFieldSetQuery(
				viewable.contenter.table.fields,code,
				queryText,any,exact,true)
					:new CodeQuery(code,queryText,any,exact){
			@Override
			protected Set<TypedNode>newDelta(Collection<TypedNode>searchable){
				Set<TypedNode>delta=new HashSet();
				for(PubIssue issue:PubIndexer.getRefsInstance().executeQuery(this)){
					TypedNode listed=viewable.listing().getTitled(issue.issue);
					if(!searchable.contains(listed))continue;
					delta.add(listed);
					RecordProxy.putPdfOpenPage(listed,issue);
				}
				return delta;
			}
		};
		final List<Stateful>output=viewable.output,input=viewable.input;
		MessageTexts texts=q.execute(output,input);
		if(output.isEmpty()){
			output.clear();
			output.addAll(input);
			texts=newEmptyMessage();
		}
		else{
			resultsTitle="'"+q.text+"' + " +q.code+
					" + "+((String)match.indexed()).replace("&","")+(exact?" + Exact":"")+
					" = "+output.size();
			results.pushItem(new Results(resultsTitle,output));
			((SuggestionsCoupler)box.coupler).updateSuggestions(queryText,false);
			viewable.contenter.spec.tryWriteValues("");
		}
		viewable.defineSelection(viewable.selection().single());
		viewable.contenter.table.resetSort();
		viewable.contenter.setResultsMessage(texts);
		if(false)results.retitle();
	}
	void setLives(){
		boolean refsOn=refs.isSet();
		match.setLive(!refsOn);
		exact.setLive(!refsOn);
		if(refsOn)match.setIndex(0);
	}
	private MessageTexts newEmptyMessage(){
		return new MessageTexts("No Active Results","This view contains "+viewable.output.size()+" records.","","");
	}
}