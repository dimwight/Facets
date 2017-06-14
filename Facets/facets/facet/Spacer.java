package facets.facet;
import facets.core.superficial.STarget;
import facets.facet.kit.*;
class Spacer extends FacetCore{
  private final int width,height;
  Spacer(Toolkit kit){
		this(0,kit);
	}
  Spacer(int width,Toolkit kit){
		this(width,0,kit);
	}
  Spacer(int width,int height,Toolkit kit){
		super(STarget.NONE,kit);
		this.width=width;
		this.height=height;
	}
	protected KWrap lazyBase(){
		return kit.spacer(this,width,height);
	}
  protected KWrap[]lazyParts(){return null;}
	final static class Filler extends Spacer{
	  Filler(Toolkit kit){
			super(0,0,kit);
		}
		protected KWrap lazyBase(){
			return kit.filler(this);
		}
	}
}
