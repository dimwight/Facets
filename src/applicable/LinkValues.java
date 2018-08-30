package applicable;
import facets.core.app.TextView.LinkText;
import facets.util.Tracer;
import facets.util.app.AppValues;
import facets.util.tree.ValueNode;
public class LinkValues extends Tracer{
	public static final String KEY_VISITED="linkValues";
	private final ValueNode store;
	String links;
	public LinkValues(AppValues values){
		store=values.nature();
		links=store.getString(KEY_VISITED);
	}
	final public LinkText newText(String text,String link){
		return new LinkText(text,link){
			private boolean visited=links.contains(link);
			@Override
			public void fireLink(){
				linkFired(this);
				visited=true;
				store.put(LinkValues.KEY_VISITED,links+=link+",");
			}
			public boolean visited(){
				return visited;
			};
			@Override
			public String tooltip(){
				return LinkValues.this.tooltip(this);
			}
		};
	}
	protected void linkFired(LinkText link){
		trace(".linkFired: linkText=",link.link);
	}
	protected String tooltip(LinkText link){
		return (link.visited()?"Already visited ":"Click for ")+link.link;
	}
}
