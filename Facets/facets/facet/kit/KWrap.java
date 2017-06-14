package facets.facet.kit;
/*
Abstracts the most general possible toolkit widget for use by a 
{@link KitFacet}.  
 */
public interface KWrap{
	public interface ItemSource{
		KWrap[]getItems();
	}
	final static KWrap BREAK=new KWrap(){
		public KitFacet facet(){return null;}
		public Object wrapped(){return null;}
    public Object newWrapped(Object parent){return null;}
    public String toString(){return "BREAK";}
	};
	/*
	The widget wrapped, for use by parent <code>KWrap</code>. 
		 */
	Object wrapped();
	/*
	Create wrapped widget and add to <code>parent</code>. 
		 */
	Object newWrapped(Object parent);
	KitFacet facet();
}