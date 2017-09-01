package pub.view;
import static applicable.field.CodeQuery.*;
import static pub.PubValues.*;
import static pub.view.PubFields.*;
import facets.core.app.AppContenter;
import facets.core.superficial.app.SContenter;
import facets.core.superficial.app.ViewableFrame;
import facets.facet.app.FacetAppSurface;
import facets.util.Debug;
import facets.util.Stateful;
import facets.util.Titled;
import facets.util.tree.TypedNode;
import facets.util.tree.ValueNode;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import pub.PubIndexer;
import pub.PubValues;
import applicable.TextQuery;
import applicable.field.CodeQuery;
import applicable.field.FieldSet;
import applicable.field.ValueField;
enum FieldsSpec{
	VIEW_WIP("WIP"){},
	VIEW_ACTIVE("Acti&ve"){},
	VIEW_All("All"){},
	VIEW_SUMMARY("&Summaries"){},
	VIEW_PUBSEARCH("PubSearch"){},
	VIEW_REFS("Index Debug"){},
	LINKS("Links"){},
	DATA("Data"),
	MCIR("For MCIR");
	static FieldsSpec VIEWS[]=Arrays.copyOf(values(),
			(userView&&!searchView?VIEW_PUBSEARCH:VIEW_REFS).ordinal());
	public static final String ARG_OPEN="openView";
	private final String title;
	private Map<String,ValueField>fields;
	static FieldsSpec getOpenView(ValueNode args){
		return values()[args.getOrPutInt(ARG_OPEN,VIEW_WIP.ordinal())];
	}
	FieldsSpec(String title){
		this.title=title;
	}
	@Override
	public String toString(){
		return title;
	}
	boolean isTitle(Titled t){
		return title.equals(t.title());
	}
	Comparator<Stateful>baseSort(){
		return ListingContent.SORT_MODIFIED;
	}
	TextQuery[]queries(FieldSet fields){
		CodeQuery adminOrUser=newFieldSetQuery(fields,CODE_WITHOUT,
				"\\typ "+(userView?"admin":"nonsense"),false,false,false);
		return this==VIEW_WIP?new TextQuery[]{
				newFieldSetQuery(fields,CODE_NEW,"\\sta draft planned",true,false,false),
				newFieldSetQuery(fields,CODE_WITHIN,"\\det",false,false,false),
				adminOrUser,
		}
		:this==VIEW_ACTIVE?new TextQuery[]{
				newFieldSetQuery(fields,CODE_NEW,"\\sta draft ready released",true,false,false),
				newFieldSetQuery(fields,CODE_WITHOUT,"\\\\num \\?",false,false,false),
				adminOrUser,
		}
		:this==VIEW_All?new TextQuery[]{
				adminOrUser,
		}
		:this==VIEW_PUBSEARCH?new TextQuery[]{
				newFieldSetQuery(fields,CODE_NEW,"\\num",false,false,false),
				newFieldSetQuery(fields,CODE_WITHOUT,"\\sta planned",false,false,false),
				newFieldSetQuery(fields,CODE_WITHOUT,"\\typ admin",false,false,false),
		}
		:new TextQuery[]{};
	}
	PubFields newFields(PubsView app){
		ValueField[]standards=newStandards(app),mcir=newPubSearch(),
			allOrSummary=new ValueField[]{
				standards[STD_LINKNUM],
				standards[STD_ISSUE],
				standards[STD_DETAILS],
				standards[STD_COMMENTS],
				standards[STD_TYPE],
				standards[STD_STATUS],
				standards[STD_RELEASE],
				standards[STD_ORDER],
				standards[STD_ORIGINATOR],
				standards[STD_STD],
				standards[STD_PDF],
				standards[STD_PAGES],
				standards[STD_NOTES],
				standards[STD_LINKS],
				standards[STD_ATTACHED],
		};
		return this==VIEW_All?new PubFields(this,app,3,allOrSummary){
			@Override
			protected boolean isDefaultField(ValueField field){
				return "Number,Issue,Details,Type,Status,Release,Order,Originator".contains(field.name);
			}
			@Override
			protected
			TextQuery[]inputQueries(){
				return VIEW_All.queries(this);
			}
		} 
		:this==VIEW_SUMMARY?new PubFields(this,app,3,allOrSummary){
			@Override
			protected boolean isDefaultField(ValueField field){
				return "Number,Details,Notes,Links,Attached".contains(field.name);
			}
		} 
		:this==LINKS?new PubFields(this,app,0,newLinks(app)){}
		:this==DATA?new PubFields(this,app,0,newData(app)){
			@Override
			AppContenter newTableContenter(ListingContent listing,FacetAppSurface app){
				throw new RuntimeException("Not implemented in "+Debug.info(this));
			};
		}
		:this==MCIR?new PubFields(this,app,mcir.length,mcir){
			@Override
			AppContenter newTableContenter(ListingContent listing,FacetAppSurface app){
				throw new RuntimeException("Not implemented in "+Debug.info(this));
			};
		}
		:this==VIEW_PUBSEARCH?new PubFields(this,app,0,false?newPubSearch():
			searchView?new ValueField[]{
				standards[STD_LINKNUM],
				standards[STD_ISSUE],
				standards[STD_DETAILS],
				standards[STD_TYPE],
				standards[STD_STATUS],
				standards[STD_RELEASE],
				standards[STD_ORDER],
				standards[STD_ORIGINATOR],
			}
		:	new ValueField[]{
			standards[STD_LINKNUM],
			standards[STD_ISSUE],
			standards[STD_DETAILS],
			standards[STD_TYPE],
			standards[STD_STATUS],
			standards[STD_RELEASE],
			standards[STD_ORDER],
			standards[STD_ORIGINATOR],
			standards[STD_PDF],
		}){
			@Override
			protected
			TextQuery[]inputQueries(){
				return VIEW_PUBSEARCH.queries(this);
			}
		}
		:this==VIEW_WIP?new PubFields(this,app,3,
			new ValueField[]{
				standards[STD_PRIORITY],
				standards[STD_NUMBER],
				standards[STD_ISSUE],
				standards[STD_STATUS],
				standards[STD_DUE],
				standards[STD_DETAILS],
				standards[STD_COMMENTS],
				standards[STD_ORDER],
				standards[STD_TYPE],
				standards[STD_ORIGINATOR],
		}){
			@Override
			protected boolean isObligatoryField(ValueField field){
				return "Number,Details".contains(field.name);
			}
			@Override
			protected boolean isDefaultField(ValueField field){
				return "Priority,Number,Due,Details,Comments,Order,Type,Originator".contains(field.name);
			}
			@Override
			protected
			TextQuery[]inputQueries(){
				return VIEW_WIP.queries(this);
			}
		} 
		:this==VIEW_ACTIVE?new PubFields(this,app,3,new ValueField[]{
				standards[STD_LINKNUM],
				standards[STD_ISSUE],
				standards[STD_TYPE],
				standards[STD_DETAILS],
				standards[STD_ORDER],
				standards[STD_STATUS],
				standards[STD_COMMENTS],
				standards[STD_RELEASE],
				standards[STD_PRIORITY],
		}){
			@Override
			protected boolean isObligatoryField(ValueField field){
				return "Number,Status".contains(field.name);
			}
			@Override
			protected boolean isDefaultField(ValueField field){
				return "Number,Issue,Type,Details,Order,Status,Release".contains(field.name);
			}
			@Override
			protected
			TextQuery[]inputQueries(){
				return VIEW_ACTIVE.queries(this);
			}
		} 
		:this==VIEW_REFS?new PubFields(this,app,3,standards){
			@Override
			protected boolean isDefaultField(ValueField field){
				return "Number,Issue,Details,Release,Type,Pages".contains(field.name);
			}
			@Override
			protected boolean isObligatoryField(ValueField field){
				return "Number,Pages".contains(field.name);
			}
			@Override
			AppContenter newTableContenter(ListingContent listing,FacetAppSurface app){
				return new ListingContenter(listing.disposable(),this,app){
					@Override
					protected ViewableFrame newContentViewable(Object source){
						ListingViewable viewable=(ListingViewable)super.newContentViewable(source);
						viewable.search.refs.set(true);
						return viewable;
					}
					@Override
					protected String boxOpeningText(){
						return PubIndexer.TEST_TERMS;
					}
				};
			}
		} 
		:new PubFields(this,app,(false?newPubSearch():new ValueField[]{
			standards[STD_LINKNUM],
			standards[STD_ISSUE],
			standards[STD_DETAILS],
			standards[STD_TYPE],
			standards[STD_STATUS],
			standards[STD_RELEASE],
			standards[STD_ORDER],
			standards[STD_ORIGINATOR],
		}).length,false?newPubSearch():new ValueField[]{
			standards[STD_LINKNUM],
			standards[STD_ISSUE],
			standards[STD_DETAILS],
			standards[STD_TYPE],
			standards[STD_STATUS],
			standards[STD_RELEASE],
			standards[STD_ORDER],
			standards[STD_ORIGINATOR],
		}){
			@Override
			AppContenter newTableContenter(ListingContent listing,FacetAppSurface app){
				throw new RuntimeException("Not implemented in "+Debug.info(this));
			};
		};
	}
}