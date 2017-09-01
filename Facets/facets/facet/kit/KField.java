package facets.facet.kit;
public interface KField extends KWidget{
	void setText(String text);
	String text();
	double value();
	void makeEditable();
	void setValue(double value);
	void requestFocus();
}