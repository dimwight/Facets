package facets.facet.app.tree;
import static java.util.regex.Pattern.*;
import facets.core.app.NodeViewable;
import facets.core.app.PathSelection;
import facets.core.superficial.SIndexing;
import facets.core.superficial.STarget;
import facets.core.superficial.STextual;
import facets.core.superficial.SToggling;
import facets.core.superficial.STrigger;
import facets.core.superficial.TargetCore;
import facets.core.superficial.STrigger.Coupler;
import facets.facet.FacetFactory;
import facets.facet.FacetFactory.SuggestionsCoupler;
import facets.util.Times;
import facets.util.Tracer;
import facets.util.app.AppValues;
import facets.util.tree.DataNode;
import facets.util.tree.NodePath;
import facets.util.tree.TypedNode;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
final public class TreeSearcher extends Tracer{
	static final String INPUT_PROMPT=false?"[Type, title, value]":"";
	public final STarget inputTextual,resultsIndexing,matchCount,targets,exact;
	private final ArrayList<String>results=new ArrayList();
	public TreeSearcher(AppValues app,final NodeViewable viewable){
		final DataNode tree=(DataNode)viewable.framed;
		inputTextual=new STextual("Find:",INPUT_PROMPT,
				new FacetFactory.SuggestionsCoupler(app){
			@Override
			public void textSet(STextual t){
				String text=t.text();
				if(text.equals("")||text.equals(INPUT_PROMPT))return;
				if(((SToggling)exact).isSet())text="\\b"+text+"\\b";
				new SearchText(tree).doSearch(text);
			}
			@Override
			protected STrigger commitTrigger(){
				return new STrigger("dummy commit",new Coupler(){
					@Override
					public void fired(STrigger t){}
				});
			}
		});
		exact=new SToggling("Exact",false,new SToggling.Coupler(){
			@Override
			public void stateSet(SToggling t){
				STextual input=(STextual)inputTextual;
				if(!input.text().equals(INPUT_PROMPT))input.coupler.textSet(input);
			}
		});
		resultsIndexing=new SIndexing("Results ",new SIndexing.Coupler(){
			@Override
			public Object[]getIndexables(){
				return results.isEmpty()?new Object[]{"[No results]"}:results.toArray();
			}
			@Override
			public void indexSet(SIndexing ix){
				String splits[]=((String)ix.indexed()).split(",");
				int[]offsets=new int[splits.length];
				for(int i=0;i<offsets.length;i++)
					offsets[i]=Integer.parseInt(splits[i]);
				viewable.defineSelection(new PathSelection(tree,new NodePath(offsets)));
			}
			@Override
			public boolean canCycle(SIndexing i){
				return true;
			}
			@Override
			public String[]iterationTitles(SIndexing i){
				return new String[]{"Previous","Next"};
			}
		});
		resultsIndexing.setLive(false);
		matchCount=new STextual("Matches","",new STextual.Coupler());
		matchCount.setLive(false);
		targets=new TargetCore(getClass().getSimpleName(),
				inputTextual,exact,resultsIndexing,matchCount);
	}
	private final class SearchText{
		private final String text;
		SearchText(DataNode tree){
			StringBuilder sb=new StringBuilder();
			buildNodeText(sb,tree,"0,");
			text=sb.toString().replaceAll("\\n+","\n");
			if(false)trace(".SearchText: text=",text);
		}
		void buildNodeText(StringBuilder sb,DataNode node,String at){
			sb.append("\n,"+at+node.type());
			String title=node.title();
			if(!title.equals(TypedNode.UNTITLED))sb.append(" "+title);
			for(String value:node.values())
				sb.append("\t"+value);
			Object[]items=node.children();
			for(int i=0;i<items.length;i++){
				Object item=items[i];
				buildNodeText(sb,(DataNode)item,at+i+",");
			}
		}
		void doSearch(String toFind){
			results.clear();
			String _nodeAt="\\n,([\\d,]+),";
			Matcher skip=compile(_nodeAt).matcher(text),
					search=compile(toFind,CASE_INSENSITIVE).matcher(text);
			int end=text.length(),from=0,to=-1;
			skip.find(from);
			while(to<end){
				String header=text.substring(skip.start(),skip.end());
				if(false)trace(": header=",header);
				from=skip.end();
				boolean isMore=skip.find(from);
				to=isMore?skip.start():end;
				search.region(from,to);
				if(false)trace(": region=",text.substring(from,to));
				while(true&&search.find()){
					String valueAt="";
					String upToEnd=text.substring(from,search.end());
					if(false)trace(": upToEnd=",upToEnd);
					if(upToEnd.contains("\t")){
						int tabs=0;
						for(int tabAt=0;tabAt>-1;tabs++)
							tabAt=upToEnd.indexOf("\t",tabAt+1);
						valueAt=",-1,"+(tabs-2);
					}
					String offsets=header.replaceAll(_nodeAt,"$1")+valueAt;
					if(false)trace(": offsets=",offsets);
					if(!results.contains(offsets))results.add(offsets);
				}
			}
			boolean none=results.isEmpty();
			STextual box=(STextual)inputTextual,matches=(STextual)matchCount;
			if(none){
				box.setText(INPUT_PROMPT);
				matches.setText("No matches");
			}
			else{
				if(false)trace(".doSearch: toFind=",toFind);
				((SuggestionsCoupler)box.coupler).updateSuggestions(
						toFind.replaceAll("\\\\b",""),false);
				((SIndexing)resultsIndexing).setIndexed(results.get(0));
				((STextual)matchCount).setText(results.size()+" matches");
			}
			resultsIndexing.setLive(!none);
			matchCount.setLive(!none);
		}
	}
}
