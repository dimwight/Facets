package pub.view;
import static applicable.field.DateField.*;
import static facets.util.Regex.*;
import static facets.util.tree.ValueNode.*;
import static pub.PubIssue.*;
import static pub.PubValues.*;
import facets.core.app.AppContenter;
import facets.core.app.TextView;
import facets.core.app.TextView.LinkText;
import facets.core.app.TextView.LongText;
import facets.facet.AreaFacets;
import facets.facet.app.FacetAppSurface;
import facets.util.Debug;
import facets.util.HtmlBuilder;
import facets.util.Objects;
import facets.util.Regex;
import facets.util.Stateful;
import facets.util.Util;
import facets.util.ValueProxy;
import facets.util.app.AppValues;
import facets.util.tree.TypedNode;
import facets.util.tree.ValueNode;
import java.text.Format;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import pdft.PdfCore;
import pub.PubIssue;
import pub.PubValues;
import applicable.LinkValues;
import applicable.SimpleFilter;
import applicable.TextQuery;
import applicable.TextSearchable;
import applicable.field.BooleanField;
import applicable.field.CodeQuery;
import applicable.field.DateField;
import applicable.field.FieldProxy;
import applicable.field.FieldSet;
import applicable.field.OptionField;
import applicable.field.PriorityField;
import applicable.field.TableComparator;
import applicable.field.TextField;
import applicable.field.TextField.LongTextField;
import applicable.field.ValueField;
abstract class PubFields extends FieldSet{
	private static final String BASE_KEYS="PubNum,Rev,PubDetails,Comments,Type,Status,"+
			"Release,Order,Originator,Due,ExternalSupplier,PageCount";// Priority
	static final String LIST_KEYS=BASE_KEYS+
		",PDF,SummaryNotes,SummaryLinks,SummaryAttachments", //EcoTail,
			STATE_KEYS=BASE_KEYS+",LinksText"; //EcoTail,
	static final int STD_LINKNUM=0,STD_ISSUE=1,STD_DETAILS=2,STD_COMMENTS=3,
		STD_TYPE=4,STD_STATUS=5,STD_RELEASE=6,STD_ORDER=7,STD_ORIGINATOR=8,STD_STD=9,
		STD_PREVIOUS=10,STD_PRIORITY=11,STD_DUE=12,STD_NUMBER=13,STD_PDF=14,
		STD_NOTES=15,STD_LINKS=16,STD_ATTACHED=17,STD_PAGES=18;
	private static final String PDF_PAGE_AT=" - p";
	static PriorityField newPriorityField(final boolean listing,AppValues spec){
		return new PriorityField(listing,spec){
			@Override
			public void putInputValue(ValueNode values,String text){
				super.putInputValue(values,text);
				spec.tryWriteValues("Priority set to "+text+" for "+values.parent().title()+": ");
			}
			@Override
			protected String getTableKey(ValueNode values){
				return values.parent().title();
			}
			@Override
			protected String[]statePath(){
				return userView?new String[]{PATH}:new String[]{PATH,KEY_ADMIN};
			}
		};
	}
	static ValueField[]newStandards(final PubsView spec){
		ValueField pageCount=new TextField("Pages"){
			@Override
			protected String valueKey(){
				return "PageCount";
			}
			@Override
			public TableComparator sorter(boolean sortDown){
				return new TableComparator<String>(sortDown);
			}
		},
		number=new ValueField<LinkText>("Number"){
			final LinkValues links=spec==null?null:new LinkValues(spec){
				@Override
				protected void linkFired(TextView.LinkText link){
					String texts[]=link.link.split(PDF_PAGE_AT);
					int page=texts.length==1?1:Integer.valueOf(texts[1]);
					spec.openPdf(texts[0],page);
				}
				@Override
				protected String tooltip(TextView.LinkText link){
					String path=link.link;
					return (true?path:(link.visited()?"Already viewed ":"Click to view ")
						+path.substring(path.lastIndexOf("/")+1));
				}
			};
			@Override
			protected String valueKey(){
				return FIELD_PUBNUM;
			}
			@Override
			protected LinkText textToValue(String text,ValueNode values){
				String type=values.getString("Type").trim();
				if(type.equals("[Admin]"))return new TextView.LinkText(text,"");
				int pageNo=values.getInt(PdfCore.KEY_PAGE);
				String link=values.getString("PDF")+(pageNo>1?(PDF_PAGE_AT+pageNo):"");
				if(links==null)throw new IllegalStateException("Null links in "+this);
				else return links.newText(text,link.equals(PDF_NONE)?"":link);
			}
			@Override
			protected LinkText newNullValue(ValueNode values){
				return new TextView.LinkText(true?"":("["+values.parent().title()+"]"),""){
					@Override
					public void fireLink(){
						throw new RuntimeException("Not implemented in "+Debug.info(this));
					}
				};
			}
			@Override
			public TableComparator<LinkText>sorter(boolean sortDown){
				return new TableComparator<LinkText>(sortDown){
					@Override
					public boolean isEmpty(TextView.LinkText t){
						return (false?t.link:t.text).equals("");
					}
	
					@Override
					public int compareNonEmpties(LinkText p,LinkText q){
						return p.text.compareToIgnoreCase(q.text);
					}
					@Override
					protected Integer integerValue(TextView.LinkText t){
						throw new RuntimeException("Not implemented in "+Debug.info(this));
					}
				};
			}
		};
		return new ValueField[]{
	number,
	new TextField("Issue"){
		@Override
		protected String valueKey(){
			return "Rev";
		}
		@Override
		public TableComparator sorter(boolean sortDown){
			return new TableComparator<String>(sortDown);
		}
		@Override
		public int inputCols(){
			return 2;
		}
	},
	new LongTextField("Details"){
		@Override
		protected String valueKey(){
			return "PubDetails";
		}
		@Override
		protected LongText textToValue(String text,ValueNode values){
			String comments=values.get("Comments"),status=values.getString("Status");
			if(false&&comments!=null&&!"Planned|Draft".contains(status))text+=" {"+comments+"}";
			return new LongText(text.replace("...",""));
		}
		@Override
		public int inputCols(){
			return 25;
		}
	},
	new LongTextField("Comments"){
		@Override
		protected LongText textToValue(String text,ValueNode values){
			return new LongText(false&&"Planned|Draft".contains(values.getString("Status"))?""
					:text.replace("...",""));
		}
		@Override
		public int inputCols(){
			return 10;
		}
	},
	new TextField("Type"),
	new TextField("Status"){
		@Override
		public TableComparator sorter(boolean sortDown){
			return new TableComparator<String>(sortDown){
				protected Integer integerValue(String str){
					return str.equals("Archived")?5:str.equals("Released")?4:
						str.equals("Ready")?3:str.equals("Draft")?2:
							str.equals("Planned")?1:0;
				}
			};
		}
	},
	new DateField("Release"){
		@Override
		public Format format(){
			return DATES_MONTH;
		}
	},
	new LongTextField("Order"){
		@Override
		public TableComparator sorter(boolean sortDown){
			return new TableComparator<LongText>(sortDown){
				final String tail="(\\D+.*)*",patterns[]={
						"\\d{6,}"+tail,
						"\\d{3,5}"+tail,
						"SP?0\\d+"+tail,
						"SP?\\d+"+tail,
						"(ECO)?5[/-]\\d+"+tail,
						};
				@Override
				public int compareNonEmpties(LongText p,LongText q){
					int prefix=prefixValue(p).compareTo(prefixValue(q));
					return prefix!=0?prefix:intValue(p).compareTo(intValue(q));
				}
				Integer prefixValue(LongText text){
					String str=text.toString();
					int i=0;
					for(;i<patterns.length;i++)
						if(str.matches(patterns[i]))return i;
					return i;
				}
				Integer intValue(LongText text){
					String str=text.toString(),
						digits=find(replaceAll(str,
								"(6\\d+)/.*","$1",
								"(ECO)?5[/-]",""),"\\d+");
					if(digits.equals(""))return 0;
					Integer value=0;
					try{
						value=Integer.valueOf(digits);
					}catch(NumberFormatException e){}
					return -value;
				}
			};
		}//?-Dpv${string_prompt:mode (or clear for search):Admin User}
		@Override
		public int inputCols(){
			return 11;
		}
	},
	new TextField("Originator"){
		@Override
		public int inputCols(){
			return 5;
		}
	},
	new BooleanField("Std"){
		@Override
		protected String valueKey(){
			return "ExternalSupplier";
		}
		@Override
		protected Boolean textToValue(String text,ValueNode values){
			return new Boolean(text.equals("1")&&!values.getString("Type").equals("EIN"));
		}
	},
	new BooleanField("Previous"){
		@Override
		protected String valueKey(){
			return "PDF";
		}
		@Override
		protected Boolean textToValue(String text,ValueNode values){
			return !text.equals(PDF_NONE)&&text.contains("previous");
		}
	},
	newPriorityField(true,spec),
	new TextField("Due"){
		@Override
		public int inputCols(){
			return 6;
		}
	},
	new TextField("Number"){
		@Override
		protected String valueKey(){
			return FIELD_PUBNUM;
		}
		@Override
		protected String newNullValue(ValueNode values){
			return true?"":("["+values.parent().title()+"]");
		}
		@Override
		public boolean isLocked(){
			return true;
		}
	},
	new BooleanField("PDF"){
		@Override
		protected String valueKey(){
			return "PDF";
		}
		@Override
		protected Boolean textToValue(String text,ValueNode values){
			return !text.equals(PDF_NONE);
		}
	},
	new LongTextField("Notes"){
		protected String valueKey(){
			return "SummaryNotes";
		};
	},
	new LongTextField("Links"){
		@Override
		protected String getValue(ValueNode v,String key){
			String got=v.get(key),pubNum=v.get(FIELD_PUBNUM),rev=v.get(FIELD_REV),
				issue=pubNum==null?"":pubNum+(rev==null||rev.equals(NULL_MARKER)?"":("_"+rev));
			got=got==null?"":got;
			if(!got.contains(issue))got+=((got.equals("")?"":" ")+issue);
			if(false)trace(".Links: value="+got);
			return got;
		}
		protected String valueKey(){
			return "SummaryLinks";
		};
	},
	new LongTextField("Attached"){
		protected String valueKey(){
			return "SummaryAttachments";
		};
	},
	pageCount};
	}
	final String newExportRow(TypedNode source,final boolean tabbed){
		return new RecordProxy(source,liveFields()){
			@Override
			protected Object newFieldText(final ValueField field){
				Object value=getFieldValue(field);
				String text;
				boolean center=false;
				if(field.name.equals("Number")&&value instanceof LinkText){
					TextView.LinkText link=(TextView.LinkText)value;
					if(false)trace(".newFieldText: link=",link.link);
					text=link.text;
					String linking=link.link;
					text=linking.equals("")?text:
						"<a href=\""+FILE_IPUBS+linking+"\">"+text+"</a>";
				}
				else if(value instanceof Date){
					Date date=(Date)value;
					text=date.equals(nullDate)?"":DATES_MONTH.format(value);
				}
				else if(value instanceof Boolean){
					text=(Boolean)value?"&#10003;":"&nbsp;";
					center=true;
				}
				else text=replaceAll(value.toString(),"<","&lt;");
				center|=text.length()<3;
				return "<td" +(center?" align=\"center\"":"")+">"+
						(text.trim().equals("")?"&nbsp;":text)
						+"</td>";
			}
			@Override
			public String newFieldTexts(){
				return tabbed?(Regex.replaceAll(super.newFieldTexts(),
						"<td[^>]*>","\t",
						"</td>","",
						"&nbsp;","",
						"^\t","",
						"(^[^\t/]+\t)","$1\t",
						"<a href=\""+FILE_IPUBS+"(.+pdf)\">([^<]+)</a>","$2\t$1"
					))
					:("<tr>"+super.newFieldTexts().replace("<td></td>","<td>&nbsp;</td>")+"</tr>");
			}
		}.newFieldTexts();
	}
	static ValueField[]newData(final PubsView spec){
		ValueField[]standards=newStandards(spec);
		return new ValueField[]{
				standards[STD_NUMBER],
				standards[STD_ISSUE],
				standards[STD_DETAILS],
				standards[STD_COMMENTS],
				standards[STD_ORDER],
				standards[STD_ORIGINATOR],
				standards[STD_DUE],
				newPriorityField(false,spec),
				new OptionField("Type","Addendum,Manual,EIN,ECO,[Admin]"){
					@Override
					protected String newNullValue(ValueNode values){
						return "[Unspecified]";
					}
					@Override
					public int inputCols(){
						if(true)throw new RuntimeException("Not implemented in "+this);
						return super.inputCols();
					}
				},
				new OptionField("Status","Planned,Draft,Ready,Released,Archived"),
				new DateField("Release"){
					@Override
					public Format format(){
						return DATES_DAY;
					}
				},			
				new TextField("Links"){
					@Override
					protected String textToValue(String text,ValueNode values){
						return text.replaceAll("(\\w[^\\[]+)\\[([^]]+)\\]",
							"<a href=\""+HtmlBuilder.HTTP+"$2\">$1</a>");
					}
					@Override
					protected String valueKey(){
						return "LinksText";
					}
				},			
				new TextField("External"){
					@Override
					protected String valueKey(){
						return "ExternalSupplier";
					}
					@Override
					protected String textToValue(String text,ValueNode values){
						return text.equals("1")&&!values.getString("Type").equals("EIN")?"Yes":"No";
					}
					@Override
					public boolean isLocked(){
						return true;
					}
				},
		};
	}
	static ValueField[]newLinks(PubsView app){
		ValueField[]standards=newStandards(app);
		return new ValueField[]{
				false?new TextField("Number"){
					@Override
					protected String valueKey(){
						return FIELD_PUBNUM;
					}
				}
				:standards[STD_LINKNUM],
				standards[STD_ISSUE],
				standards[STD_DETAILS],
				standards[STD_ORDER],
				standards[STD_RELEASE],
				standards[STD_ORIGINATOR],
		};
	}
	static ValueField[]newPubSearch(){
		final String nullMarker=ValueNode.NULL_MARKER;
		return new ValueField[]{
	new TextField("Number"){
		@Override
		protected String textToValue(String text,ValueNode values){
			String link=values.getString("PDF");
			if(false&&text.matches("30038(6|8|9).+"))Util.printOut("PubFields: text="+text+" link=",link);
			return text+":"+link;
		}
		@Override
		protected String valueKey(){
			return FIELD_PUBNUM;
		}
		@Override
		protected String newNullValue(ValueNode values){
			return nullMarker;
		}
		@Override
		public String toString(){
			return super.toString()+":"+PubIssue.PDF_NONE;
		}
	},
	new TextField("Issue"){
		@Override
		protected String valueKey(){
			return "Rev";
		}
		@Override
		protected String newNullValue(ValueNode values){
			return nullMarker;
		}
	},
	new TextField("Details"){
		@Override
		protected String valueKey(){
			return "PubDetails";
		}
		@Override
		protected String textToValue(String text,ValueNode values){
			String comments=values.get("Comments"),status=values.getString("Status");
			if(comments!=null&&!"Planned|Draft".contains(status))text+=" {"+comments+"}";
			return text.replaceAll("\\s+"," ");
		}
		@Override
		protected String newNullValue(ValueNode values){
			return nullMarker;
		}
	},
	newPubSearchField("Type"),
	new TextField("Release"){
		@Override
		protected String newNullValue(ValueNode values){
			return nullMarker;
		}
		@Override
		protected String textToValue(String text,ValueNode values){
			try{
				return DATES_MONTH.format(DATES_DAY.parse(
						false?text.replaceAll("^\\d+/(\\d+/\\d+).*","$1"):text));
			}catch(ParseException e){
				return text;
			}
		}
		@Override
		public Format format(){
			return DATES_MONTH;
		}
	},
	newPubSearchField("Order"), 
	newPubSearchField("Originator")};
	}
	private static ValueField newPubSearchField(String name){
		return new TextField(name){
			@Override
			protected String newNullValue(ValueNode values){
				return NULL_MARKER;
			}
		};
	}	
	final FieldsSpec spec;
	private LinkValues linkValues;
	PubFields(FieldsSpec spec,AppValues values,int liveMin,ValueField[]fields){
		super(spec.toString(),fields,liveMin);
		this.spec=spec;
		if(values==null)return;
		loadState(values.state(AreaFacets.TYPE));
		linkValues=new LinkValues(values);
	}
	@Override
	protected boolean isObligatoryField(ValueField field){
		return "Number".contains(field.name);
	}
	AppContenter newTableContenter(ListingContent listing,FacetAppSurface app){
		return new ListingContenter(listing.disposable(),this,app);
	}
	public final FieldProxy newProxy(Stateful stateful){
		return new RecordProxy((TypedNode)stateful,liveFields());
	}
	@Override
	public String toString(){
		return Objects.toString(liveFields(),"\t");
	};
	final RecordProxy newQualifiedSearchable(TypedNode source,final String qualifier, 
			final String separator,boolean liveOnly){
		return new RecordProxy(source,!liveOnly?allFields:liveFields()){
			@Override
			protected Object newFieldText(ValueField field){
				Object value=getFieldValue(field);
				if((value.equals(false)
						||value.toString().trim().equals("")||value.equals(nullDate)))
					return "";
				if(!qualifier.equals("")){
					if(!qualifier.toLowerCase().contains(field.shortName(shortLength)))
						return "";
					else if(qualifier.startsWith(TextQuery.BARE_FIELD_TOP))
						value=qualifier.toLowerCase();
				}
				if(value instanceof Date)value=DATES_MONTH.format(value);
				return(value+separator);
			}
		};
	}
	void executeInputQueries(List<Stateful>buffer,List<Stateful>input){
		for(TextQuery q:inputQueries())((CodeQuery)q).execute(buffer,input);
	}
	protected TextQuery[]inputQueries(){
		return new TextQuery[]{};
	}
	static CodeQuery newFieldSetQuery(final FieldSet fields,String code,String text,
			boolean any,boolean exact,final boolean liveOnly){
		return new CodeQuery(code,text,any,exact){
			@Override
			protected Set<TypedNode>newDelta(Collection<TypedNode>searchable){
				Set<TypedNode>delta=new HashSet();
				Collection<RecordProxy>proxies=new ArrayList();
				for(TypedNode node:searchable)
					proxies.add(((PubFields)fields).newQualifiedSearchable(node,
							qualifier,patterns?"":"|",liveOnly));
				SimpleFilter<RecordProxy>filter=newFilter();
				for(TextSearchable each:filter.filter(proxies))
					delta.add((TypedNode)((ValueProxy)each).source);
				return delta;
			}
		};
	}
}