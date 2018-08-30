package facets.facet.kit;
import static facets.facet.FacetFactory.*;
import facets.core.app.AppConstants;
import facets.facet.FacetFactory;
import facets.util.Debug;
import facets.util.StringFlags;
import facets.util.Tracer;
import facets.util.Util;
import java.util.Collection;
import java.util.Map;
final public class Decoration extends Tracer{
	final public StringFlags hints;
  final public Object icon,disable,keyStroke;
	final public String keyText,caption,mnemonicCaption,rubric,titleSplits[];
	final public int mnemonic;
	private final KitCore kit;
	private final boolean debug;
	Decoration(String keyText,KitCore kit,StringFlags hints){
	  if((this.keyText=keyText)==null||keyText.equals(""))
	  	throw new IllegalArgumentException("Null or empty keyText in "+Debug.info(this));
	  else if((this.kit=kit)==null)
	  	throw new IllegalArgumentException("Null kit in "+Debug.info(this));
	  else if((this.hints=hints)==null||keyText.equals(""))
	  	throw new IllegalArgumentException("Null or empty hints in "+Debug.info(this));
	  debug=true&&hints.includeFlag(HINT_DEBUG)||keyText.equals("");
		icon=kit.getDecorationIcon(keyText,false);
		disable=kit.getDecorationIcon(keyText,true);
		keyStroke=kit.getDecorationKeyStroke(keyText);
		String rubric=kit.rubrics.get(keyText),
				workText=kit.getDecorationText(keyText,false);
		titleSplits=workText.split("\\|");
		int titles=titleSplits.length;
		if(titles>1)workText=titleSplits[hints.includeFlag(HINT_TITLE2)?
				titles>2?2:1
			:hints.includeFlag(HINT_TITLE1)?1:0];
		if(rubric==null)rubric=kit.rubrics.get(workText);
		this.rubric=rubric==null?"":rubric;
		workText=kit.getDecorationText(workText,false);
	  int mnemonicAt=workText.indexOf('&')+1;
	  mnemonic=true||workText.length()>1?workText.toUpperCase().charAt(mnemonicAt):0;
		String captionTop=workText.substring(0,mnemonicAt-(mnemonicAt==0?0:1)),
			captionTail=workText.substring(mnemonicAt);
		caption=captionTop+captionTail;
		mnemonicCaption=captionTop+"&"+captionTail;
		trace(":",this);
	}
	@Override
	protected void traceOutput(String msg){
		if(debug)super.traceOutput(msg);
	}
	public Decoration recreate(String keyText){
		return new Decoration(keyText,kit,hints);
	}
	public String toString(){
		return Debug.info(this)+" keyText="+keyText+" caption="+caption
		+"\n mnemonic="+mnemonic+" mnemonicCaption="+mnemonicCaption+" rubric="+rubric
		+"\n hints="+hints;
	}
	public String tabCaption(){
	  int spaceAt=caption.indexOf(' ');
	  String forTab=true||spaceAt<0?caption:caption.substring(0,spaceAt);
	  int chars=caption.length();
	  return forTab=chars<20?forTab:
	  	forTab.substring(0,8)+"..."+forTab.substring(chars-8);
	}
}
