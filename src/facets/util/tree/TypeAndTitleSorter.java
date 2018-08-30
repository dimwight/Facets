package facets.util.tree;

import java.util.Comparator;

class TypeAndTitleSorter implements Comparator{
	public int compare(Object o1,Object o2){
		TypedNode node1=(TypedNode)o1,node2=(TypedNode)o2;
		int types=compareTypes(node1.type(),node2.type());
		return types!=0?types:compareTitles(node1.title(),node2.title());
	}
	protected int compareTitles(String title1,String title2){			
		return title1.compareTo(title2);
	}
	protected int compareTypes(String type1,String type2){			
		return type1.compareTo(type2);
	}
}