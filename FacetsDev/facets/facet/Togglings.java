package facets.facet;
import facets.core.superficial.Notifying;
import facets.core.superficial.SIndexing;
import facets.core.superficial.STarget;
import facets.core.superficial.SToggling;
import facets.core.superficial.Notifying.Impact;
import facets.core.superficial.SToggling.Coupler;
import facets.facet.FacetFactory.ComboCoupler;
import facets.facet.kit.KButton;
import facets.facet.kit.KWrap;
import facets.util.Debug;
import facets.util.StringFlags;
import facets.util.Util;
import facets.util.app.Events;
final class Togglings{
	static class Button extends SimpleMaster{
		private KButton button;
		private final int usage;
	  Button(int usage,StringFlags hints){
	  	super(hints);
	  	this.usage=usage;
	  }
	  public void attachedToFacet(){
	  	button=core.newRegisteredButtons(KButton.Type.Check,usage,
	  			new String[]{title()},hints)[0];
	  }
		public void retargetedMultiple(STarget[]targets,Impact impact){
			boolean first=((SToggling)targets[0]).isSet(),allSame=true;
			for(int i=0;allSame&&i<targets.length;i++)
				allSame&=((SToggling)targets[i]).isSet()==first;
			if(allSame)button.setSelected(first);
			else if(true)button.setIndeterminate(true);
		}
		protected void notifyingSingle(STarget target,Object msg){
			((SToggling)target).set(button.isSelected());
		}
		KWrap lazyBaseWrap(){
			return button;
		}
		KWrap[]lazyPartWraps(){
			return null;
		}
	}
}
