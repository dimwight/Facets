package facets.facet;
import facets.core.superficial.Notifying;
import facets.core.superficial.STarget;
import facets.core.superficial.STextual;
import facets.core.superficial.STrigger;
import facets.core.superficial.Notifying.Impact;
import facets.core.superficial.STextual.Update;
import facets.facet.FacetFactory.SuggestionsCoupler;
import facets.facet.kit.KField;
import facets.facet.kit.KWrap;
import facets.util.StringFlags;
import facets.util.Util;
import facets.util.tree.Nodes;
import facets.util.tree.ValueNode;
import java.util.Collection;
final class TextualField extends SimpleMaster{
  private KField field;
	private final int cols;
  TextualField(int cols,StringFlags hints){
		super(hints);
		this.cols=cols;
	}
  @Override
  protected void notifyingSingle(STarget target,Object msg){
  	super.notifyingSingle(target,msg);
  	if(msg==Update.Commit)
  		((SuggestionsCoupler)((STextual)target).coupler).commitTrigger().fire();
  }
  @Override
  public void attachedToFacet(){
  	STextual t=(STextual)target();
  	field=toolkit().textField(core,cols,hints);
  	if(cols>0)field.makeEditable();
  }
  @Override
	void notifyingMultiple(STarget[]targets,Object msg){
	  String text=field.text();
		for(int i=0;i<targets.length;i++){
			STextual textual=(STextual)targets[i];
			if(true||!textual.text().equals(text))textual.setText(text);
		}
  }
  @Override
	public void retargetedMultiple(STarget[]targets,Notifying.Impact impact){
		Object first=((STextual)targets[0]).text();
		boolean allSame=true;
		for(int i=0;allSame&&i<targets.length;i++)
			allSame&=((STextual)targets[i]).text().equals(first);
	  field.setIndeterminate(!allSame);		
		if(!allSame)return;
  	STextual t=(STextual)targets[0];
		field.setText(t.text());
		field.setEnabled(t.isLive());
	}
  @Override
	public void respondTargetWantsFocus(){
		trace(".respondTargetWantsFocus: ");
		field.requestFocus();
	}
  @Override
	public String toString(){
		return super.toString()+", "+field.text();
	}
  @Override
	KWrap lazyBaseWrap(){
		return field;
	}
  @Override
	KWrap[]lazyPartWraps(){
		return null;
	}
}
