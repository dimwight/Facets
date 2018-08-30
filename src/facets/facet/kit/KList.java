package facets.facet.kit;
public interface KList extends KWidget{
	int[]indices();
	void setTitles(String[]titles);
	void setIndices(int[]indices);
  void setIndex(int index,boolean titleEditable);
}