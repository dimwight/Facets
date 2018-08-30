package facets.facet;
import facets.core.superficial.STarget;
import facets.core.superficial.Notifying.Impact;
import facets.facet.kit.*;
import facets.util.Debug;
import facets.util.StringFlags;
import facets.util.Titled;
import facets.util.Util;
abstract class SimpleMaster extends FacetMaster.Simple implements Titled{
	static abstract class Buttons extends SimpleMaster{
	  KButton[]buttons;
		Buttons(StringFlags hints){
			super(hints);			
		}
	  final protected int buttonIndex(){
	  	if(buttons!=null)for(int i=0;i<buttons.length;i++)
		      if(buttons[i].isSelected())return i;
	    return -1;
	  }
	  KWrap lazyBaseWrap(){return null;}
	  KWrap[]lazyPartWraps(){
	  	if(buttons==null)throw new IllegalStateException(
	  			"Null buttons in "+Debug.info(this));
	    return !isMenu()?buttons
	    	:new KWrap[]{toolkit().menu(core(),title(),buttons,hints)};
	  }
	}
	final StringFlags hints;
	final boolean debug;
	protected SimpleCore core;
	SimpleMaster(StringFlags hints){
		this.hints=hints;
		debug=hints.includeFlag(FacetFactory.HINT_DEBUG);
	}
	abstract KWrap lazyBaseWrap();
	abstract KWrap[]lazyPartWraps();
	public void retargetedSingle(STarget target,Impact impact){
		retargetedMultiple(new STarget[]{target},impact);
	}
	public void retargetedMultiple(STarget[]targets,Impact impact){
		if(false)core().setEnables(targets.length==1);
	}
	protected void notifyingSingle(STarget target,Object msg){
		if(true)notifyingMultiple(new STarget[]{target},msg);
		else throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	void notifyingMultiple(STarget[]targets,Object msg){
		for(int i=0;i<targets.length;i++)notifyingSingle(targets[i],msg);
	}
	void setEnables(STarget target){}
  boolean forMenu(){return false;}
  boolean isMenu(){return false;}
	boolean inDialog(){return false;}
	final Toolkit toolkit(){
		return core().kit;
	}
  final SimpleCore core(){
		if(core==null)throw new IllegalStateException(
				"Null core in "+Debug.info(this));
		return core;
	}
	/**
	Implements interface method. 
	@return the title of the current facet target. 
	 */
	final public String title(){
		return (false?(Util.helpfulClassName(this)+" "):"")+core().target().title();
	}
	public String toString(){
  	return Debug.info(this);
  }
	final public STarget target(){
		return core().target();
	}
}